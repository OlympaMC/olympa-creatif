package fr.olympa.olympacreatif.plot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;

import fr.olympa.api.holograms.Hologram;
import fr.olympa.api.holograms.Hologram.HologramLine;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OCparam;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.perks.KitsManager.KitType;
import fr.olympa.olympacreatif.perks.UpgradesManager.UpgradeType;
import fr.olympa.olympacreatif.plot.PlotPerm.PlotRank;
import fr.olympa.olympacreatif.world.WorldManager;
import net.minecraft.server.v1_16_R3.TileEntity;
import net.minecraft.server.v1_16_R3.TileEntityTypes;

public class Plot {
	
	private OlympaCreatifMain plugin;
	
	private PlotMembers members;
	private PlotParameters parameters;
	private PlotId plotId;
	
	private PlotCbData cbData;
	private PlotStoplagChecker stoplagChecker;
	
	private Set<Player> playersInPlot = new HashSet<Player>();
	private List<Entity> entitiesInPlot = new ArrayList<Entity>();
	
	private boolean allowLiquidFlow = false;
	
	private static Map<Player, List<ItemStack>> inventoryStorage = new HashMap<Player, List<ItemStack>>();
	
	//constructeur pour un plot n'existant pas encore
	public Plot(OlympaCreatifMain plugin, OlympaPlayerCreatif p) {
		this.plugin = plugin;
		
		plotId = PlotId.createNew(plugin);
		
		parameters = new PlotParameters(plugin, plotId);
		members = new PlotMembers(UpgradeType.BONUS_MEMBERS_LEVEL.getValueOf(p.getUpgradeLevel(UpgradeType.BONUS_MEMBERS_LEVEL)));
		
		members.set(p, PlotRank.OWNER);
		
		cbData = new PlotCbData(plugin, this, 
				UpgradeType.CB_LEVEL.getValueOf(p.getUpgradeLevel(UpgradeType.CB_LEVEL)), 
				p.hasKit(KitType.HOSTILE_MOBS) && p.hasKit(KitType.PEACEFUL_MOBS), p.hasKit(KitType.HOSTILE_MOBS));
		
		stoplagChecker = new PlotStoplagChecker(plugin, this);
		
		if (p.hasKit(KitType.FLUIDS))
			allowLiquidFlow = true;
		
		executeCommonInstanciationActions();
	}
	
	//chargement d'un plot déjà existant
	public Plot(AsyncPlot ap) {
		this.plugin = ap.getPlugin();
		this.parameters = ap.getParameters();
		this.members = ap.getMembers();
		this.plotId = ap.getId();
		this.stoplagChecker = new PlotStoplagChecker(plugin, this);
		
		allowLiquidFlow = ap.getAllowLiquidFlow();
		
		cbData.setPlot(this);
		
		executeCommonInstanciationActions();
	}
	
	//actions to execute, wether if the plot is a new one or loaded from an asyncplot
	private void executeCommonInstanciationActions() {
		
		//exécution des actions d'entrée pour les joueurs étant arrivés sur le plot avant chargement des données du plot
		for (Player p : Bukkit.getOnlinePlayers())
			if (plotId.isInPlot(p.getLocation()))
				if (canEnter(p))
					executeEntryActions(p, null);
				else
					teleportOut(p);
		
		loadInitialEntitiesOnChunks();
		
		//forceload 1 chunk à l'origine du plot
		/*for (int x = plotId.getLocation().getChunk().getX() ; x < plotId.getLocation().getChunk().getX() + 2 ; x++)
			for (int z = plotId.getLocation().getChunk().getZ() ; z < plotId.getLocation().getChunk().getX() + 2 ; z++)
				plugin.getWorldManager().getWorld().setChunkForceLoaded(x, z, true);*/
		/*plugin.getWorldManager().getWorld().getChunkAtAsync(plotId.getLocation(), new Consumer<Chunk>() {

			@Override
			public void accept(Chunk t) {
				plugin.getWorldManager().getWorld().setChunkForceLoaded(t.getX(), t.getZ(), true);
			}
			
		});*/
		
		//add entities from already loaded chunks
		for (int x = plotId.getLocation().getChunk().getX() ; x < plotId.getLocation().getChunk().getX() + OCparam.PLOT_SIZE.get() / 16 ; x++)
			for (int z = plotId.getLocation().getChunk().getZ() ; z < plotId.getLocation().getChunk().getZ() + OCparam.PLOT_SIZE.get() / 16 ; z++)
				if (plugin.getWorldManager().getWorld().isChunkLoaded(x, z))
					
					Arrays.asList(plugin.getWorldManager().getWorld().getChunkAt(x, z).getEntities()).forEach(e -> {	
						if (e.getType() != EntityType.PLAYER)
							if (plotId.equals(plugin.getPlotsManager().getBirthPlot(e)))
								addEntityInPlot(e);
							else 
								e.remove();
					});
	}
	
	private void loadInitialEntitiesOnChunks() {

		//Bukkit.broadcastMessage("x min : " + initialX + " - max : " + initialX + chunksRowCount);
		//Bukkit.broadcastMessage("z min : " + initialZ + " - max : " + initialZ + chunksRowCount);
		
		getLoadedChunks().forEach(chunk -> {
			new ArrayList<Entity>(Arrays.asList(chunk.getEntities())).forEach(e -> {
				if (e.getType() != EntityType.PLAYER)
					addEntityInPlot(e);
			});
		});	
	}
	
	private Set<Chunk> getLoadedChunks() {

		int initialX = plotId.getIndexX() * (Math.floorDiv(OCparam.PLOT_SIZE.get() + WorldManager.roadSize, 16));
		int initialZ = plotId.getIndexZ() * (Math.floorDiv(OCparam.PLOT_SIZE.get() + WorldManager.roadSize, 16));
		int chunksRowCount = Math.floorDiv(OCparam.PLOT_SIZE.get(), 16);
		
		Set<Chunk> set = new HashSet<Chunk>();
		
		for (int x = initialX ; x < initialX + chunksRowCount ; x++)
			for (int z = initialZ ; z < initialZ + chunksRowCount ; z++)
				if (plugin.getWorldManager().getWorld().isChunkLoaded(x, z))
					set.add(plugin.getWorldManager().getWorld().getChunkAt(x, z));
		
		return set;
	}
	
	public long getCommandBlocksCount() {
		return getLoadedChunks().stream().mapToLong(ch -> ((CraftChunk)ch).getHandle().getTileEntities().values()
				.stream().filter(tile -> tile.getTileType().equals(TileEntityTypes.COMMAND_BLOCK)).count()).sum();
	}
	
	public PlotParameters getParameters() {
		return parameters;
	}
	
	public PlotMembers getMembers() {
		return members;
	}
	
	public PlotCbData getCbData() {
		return cbData;
	}
	
	public PlotStoplagChecker getStoplagChecker() {
		return stoplagChecker;
	}
	
	public void addPlayerInPlot(Player p) {
		if (!playersInPlot.contains(p))
			playersInPlot.add(p);
	}
	
	public void removePlayerInPlot(Player p) {
		playersInPlot.remove(p);
	}
	
	//ajoute l'entité à la liste des entités du plot, et supprime la plus vieille entité si le quota est dépassé
	public void addEntityInPlot(Entity e) {
		if (entitiesInPlot.contains(e) || e.getType() == EntityType.PLAYER)
			return;
		
		if (entitiesInPlot.size() == OCparam.MAX_TOTAL_ENTITIES_PER_PLOT.get()) 
			removeEntityInPlot(entitiesInPlot.get(0), true);
		
		int count = 0;
		Entity toRemove = null;
		
		for (Entity ent : entitiesInPlot)
			if (ent.getType() == e.getType()) {
				count++;
				if (toRemove == null)
					toRemove = ent;
			}
		
		if (count >= OCparam.MAX_ENTITIES_PER_TYPE_PER_PLOT.get() && toRemove != null) {
			removeEntityInPlot(toRemove, true);
		}
			
		entitiesInPlot.add(e);
	}
	
	public void removeEntityInPlot(Entity e, boolean killEntity) {
		if (killEntity)
			e.remove();
		entitiesInPlot.remove(e);
		cbData.clearEntityDatas(e);
	}
	
	public Set<Player> getPlayers(){
		return Collections.unmodifiableSet(playersInPlot);
	}
	
	
	public synchronized List<Entity> getEntities(){
		return new ArrayList<Entity>(entitiesInPlot);
	}
	
	public long getLoadedTileEntitiesCount() {
		long tiles = 0;
		
		for (int x = plotId.getLocation().getBlockX() ; x < plotId.getLocation().getBlockX() + OCparam.PLOT_SIZE.get() ; x+=16)
			for (int z = plotId.getLocation().getBlockZ() ; z < plotId.getLocation().getBlockZ() + OCparam.PLOT_SIZE.get() ; z+=16)
				if (plugin.getWorldManager().getNmsWorld().isChunkLoaded(x / 16, z / 16))
					tiles += plugin.getWorldManager().getNmsWorld().getChunkAt(x / 16, z / 16).getTileEntities().size();
	
		return tiles;
	}
	
	
	public boolean hasLiquidFlow() {
		return allowLiquidFlow;
	}
	
	public void setAllowLiquidFlow() {
		allowLiquidFlow = true;
	}
	
	public void teleportOut(Player p) {
		p.teleport(getOutLoc());
	}
	
	public Location getOutLoc() {
		Location loc = plotId.getLocation().clone().subtract(WorldManager.roadSize / 2, 0, WorldManager.roadSize / 2);
		loc.setY(WorldManager.worldLevel + 1);
		return loc;
	}

	public void unload() {
		cbData.unload();
		
		//unload du forced loaded chunk
		plugin.getWorldManager().getWorld().setChunkForceLoaded(plotId.getLocation().getChunk().getX(), plotId.getLocation().getChunk().getX(), false);
	}

	public PlotId getId() {
		return plotId;
	}
	
	public boolean hasStoplag() {
		if (parameters.getParameter(PlotParamType.STOPLAG_STATUS) >= 1)
			return true;
		else
			return false;
	}
	
	public void sendMessage(OlympaPlayerCreatif pc, String msg) {
		if (PermissionsList.USE_COLORED_TEXT.hasPermission(pc))
			msg = ChatColor.translateAlternateColorCodes('&', msg);
		
		msg = "§7[Parcelle " + getId() + "] §r" + 
		pc.getGroupNameColored() + " " + pc.getPlayer().getName() + " §r§7: " + msg;
		
		for (Player p : getPlayers())
			p.sendMessage(msg);
	}
	
	@Override
	public String toString() {
		return plotId.toString();
	}
	
	public boolean canEnter(Player p ) {
		return canEnter((OlympaPlayerCreatif) AccountProvider.get(p.getUniqueId()));
	}
	
	public boolean canEnter(OlympaPlayerCreatif pc) {
		if (!pc.hasStaffPerm(StaffPerm.BYPASS_KICK_BAN))
			if (parameters.getParameter(PlotParamType.BANNED_PLAYERS).contains(pc.getId())) {
				OCmsg.PLOT_CANT_ENTER_BANNED.send(pc, this);
				return false;
				
			}else if (!parameters.getParameter(PlotParamType.ALLOW_VISITORS) && members.getPlayerRank(pc) == PlotRank.VISITOR) {
				OCmsg.PLOT_CANT_ENTER_CLOSED.send(pc, this);
				return false;
			}
		
		return true;
	}

	
	/**
	 * Execute entry actions for this player for the plot
	 * @param p concerned player
	 * @param teleportPlayer 
	 * @return true si le joueur est autorisé à entrer, false sinon
	 */
	public void executeEntryActions(Player p, Location tpLoc) {
		executeEntryActions((OlympaPlayerCreatif) AccountProvider.get(p.getUniqueId()), tpLoc);
	}
		
	/**
	 * Execute entry actions for this player for the plot
	 * @param p concerned player
	 * @param teleportPlayer 
	 * @return true si le joueur est autorisé à entrer, false sinon
	 */
	public void executeEntryActions(OlympaPlayerCreatif pc, Location tpLoc) {
		if (!canEnter(pc))
			return;
		
		Player p = pc.getPlayer();
		pc.setCurrentPlot(this);
		
		//ajoute le joueur aux joueurs du plot s'il n'a pas la perm de bypass les commandes vanilla
		if (!pc.hasStaffPerm(StaffPerm.GHOST_MODE))
			addPlayerInPlot(p);
		
		//exécution instruction commandblock d'entrée
		plugin.getCommandBlocksManager().executeJoinActions(this, p);
		
		//clear les visiteurs en entrée & stockage de leur inventaire
		if (parameters.getParameter(PlotParamType.CLEAR_INCOMING_PLAYERS) && !PlotPerm.BYPASS_ENTRY_ACTIONS.has(this, pc)) {
			List<ItemStack> list = new ArrayList<ItemStack>();
			for (ItemStack it : p.getInventory().getContents()) {
				if (it != null && it.getType() != Material.AIR)
				list.add(it);
			}
			
			inventoryStorage.put(p, list);
			p.getInventory().clear();
			
			for (PotionEffect effect : p.getActivePotionEffects())
				p.removePotionEffect(effect.getType());
		}
		
		if (!PlotPerm.BYPASS_ENTRY_ACTIONS.has(this, pc))
			//tp au spawn de la zone
			if (parameters.getParameter(PlotParamType.FORCE_SPAWN_LOC) && !parameters.getParameter(PlotParamType.SPAWN_LOC).toLoc().equals(tpLoc)) {
				plugin.getTask().runTaskLater(() -> {
					parameters.getParameter(PlotParamType.SPAWN_LOC).teleport(p);
					OCmsg.TELEPORTED_TO_PLOT_SPAWN.send(pc);
				}, 1);
			
			
			//définition du gamemode
			p.setGameMode(parameters.getParameter(PlotParamType.GAMEMODE_INCOMING_PLAYERS));
			
			//définition du flymode
			p.setAllowFlight(parameters.getParameter(PlotParamType.ALLOW_FLY_INCOMING_PLAYERS));
		}
		
		//définition de l'heure du joueur
		p.setPlayerTime(parameters.getParameter(PlotParamType.PLOT_TIME), false);
		
		//définition de la météo
		p.setPlayerWeather(parameters.getParameter(PlotParamType.PLOT_WEATHER));
		
		//joue la musique par défaut du plot
		plugin.getPerksManager().getSongManager().startSong(p, getParameters().getParameter(PlotParamType.SONG));
		
		//reset fly speed if needed
		if (!PlotPerm.DEFINE_OWN_FLY_SPEED.has(this, pc) && getParameters().getParameter(PlotParamType.RESET_VISITOR_FLY_SPEED))
			pc.getPlayer().setFlySpeed(0.1f);
		
		
		//send stoplag alert if activated
		if (hasStoplag())
			OCmsg.PLOT_ENTER_STOPLAG_ACTIVATED.send(pc, this);
	}

	/**
	 * Execute quit actions for this player on the plot
	 * @param p
	 */
	public void executeExitActions(Player p) {

		OlympaPlayerCreatif pc = AccountProvider.get(p.getUniqueId());
		
		pc.setCurrentPlot(null);
		removePlayerInPlot(p);

		plugin.getCommandBlocksManager().excecuteQuitActions(this, p);
		
		plugin.getPerksManager().getSongManager().stopSong(p);

		//rendu inventaire si stocké
		if (inventoryStorage.containsKey(p)) {
			p.getInventory().clear();
			for (ItemStack it : inventoryStorage.get(p))
				p.getInventory().addItem(it);
			inventoryStorage.remove(p);
		}
		
		p.setGameMode(GameMode.CREATIVE);
		p.setAllowFlight(true);
		p.resetPlayerTime();
		p.resetPlayerWeather();
		
		if (plugin.getWEManager() != null)
			plugin.getWEManager().clearClipboard(this, p);
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof Plot && ((Plot)o).getId().equals(plotId);
	}
}
