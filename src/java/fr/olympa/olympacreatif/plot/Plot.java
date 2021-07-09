package fr.olympa.olympacreatif.plot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OCparam;
import fr.olympa.olympacreatif.data.OcPermissions;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.perks.KitsManager.KitType;
import fr.olympa.olympacreatif.perks.UpgradesManager.UpgradeType;
import fr.olympa.olympacreatif.plot.PlotPerm.PlotRank;
import fr.olympa.olympacreatif.world.WorldManager;

public class Plot {
	
	private OlympaCreatifMain plugin;

	private int timeUpdateScheduler = -1;
	private int destroyScheduler = -1;
	private int entitiesCheckupScheduler = -1;

	private int tilesCount = 0;
	private int currentTime = -1;

	private PlotMembers members;
	private PlotParameters parameters;
	private PlotId plotId;
	
	private PlotCbData cbData;
	private PlotStoplagChecker stoplagChecker;
	
	private Set<Player> playersInPlot = new HashSet<Player>();
	private List<Entity> entitiesInPlot = new ArrayList<Entity>();
	//private List<Entity> hangingsInPlot = new ArrayList<Entity>();
	
	private boolean allowLiquidFlow = false;
	
	private static Map<Player, List<ItemStack>> inventoryStorage = new HashMap<Player, List<ItemStack>>();
	
	//constructeur pour un plot n'existant pas encore
	public Plot(OlympaCreatifMain plugin, OlympaPlayerCreatif p) {
		this.plugin = plugin;
		
		plotId = PlotId.createNew(plugin);
		
		parameters = new PlotParameters(plugin, plotId);
		members = new PlotMembers(UpgradeType.BONUS_MEMBERS_LEVEL.getDataOf(p).value);
		
		members.set(p, PlotRank.OWNER);
		
		cbData = new PlotCbData(plugin, UpgradeType.CB_LEVEL.getDataOf(p).value, 
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
		this.cbData = ap.getCbData();
		
		allowLiquidFlow = ap.getAllowLiquidFlow();
		
		executeCommonInstanciationActions();
	}
	
	//actions to execute, wether if the plot is a new one or loaded from an asyncplot
	private void executeCommonInstanciationActions() {
		destroyScheduler = plugin.getTask().scheduleSyncRepeatingTask(() -> {
			if (getPlayers().size() == 0 && !Bukkit.getOnlinePlayers().stream().anyMatch(p -> getMembers().getPlayerRank(p) != PlotRank.VISITOR)) {
				long l = System.currentTimeMillis();
				unload();
				plugin.getPlotsManager().unloadPlot(this);
				plugin.getLogger().info("Plot " + this + " unload took " + (System.currentTimeMillis() - l) + "ms to proceed.");
			}
			
		}, 20 * 5 * 60, 20 * 5 * 60);
		
		entitiesCheckupScheduler = plugin.getTask().scheduleSyncRepeatingTask(() -> {
			Iterator<Entity> entIterator = getEntities().iterator();
			while (entIterator.hasNext()) {
				Entity ent = entIterator.next();
				if (!plotId.isInPlot(ent.getLocation()))
					removeEntityInPlot(ent, true);
			}
		}, 20 * 20, 20 * 20);

		cbData.setPlot(this);
		
		//exécution des actions d'entrée pour les joueurs étant arrivés sur le plot avant chargement des données du plot
		for (Player p : Bukkit.getOnlinePlayers())
			if (plotId.isInPlot(p.getLocation()))
				if (((OlympaPlayerCreatif)AccountProviderAPI.getter().get(p.getUniqueId())).setPlot(this)) 
					executeEntryActions(p, p.getLocation());
				else
					teleportOut(p);
		
		int i = 1;
		for (Chunk ch : getLoadedChunks())
			plugin.getTask().runTaskLater(() -> Arrays.asList(ch.getEntities())
					.forEach(e -> {
						if (e.getType() != EntityType.PLAYER)
							addEntityInPlot(e);
					}), i+= 2);
	}
	
	public void getLoadedChunks(Consumer<Chunk> consumer) {

		int initialX = plotId.getIndexX() * (Math.floorDiv(OCparam.PLOT_SIZE.get() + WorldManager.roadSize, 16));
		int initialZ = plotId.getIndexZ() * (Math.floorDiv(OCparam.PLOT_SIZE.get() + WorldManager.roadSize, 16));
		int chunksRowCount = Math.floorDiv(OCparam.PLOT_SIZE.get(), 16);
		
		//Set<Chunk> set = new HashSet<Chunk>();
		
		for (int x = initialX ; x < initialX + chunksRowCount ; x++)
			for (int z = initialZ ; z < initialZ + chunksRowCount ; z++)
				if (plugin.getWorldManager().getWorld().isChunkLoaded(x, z))
					consumer.accept(plugin.getWorldManager().getWorld().getChunkAt(x, z));
	}
	
	public List<Chunk> getLoadedChunks() {
		int initialX = plotId.getIndexX() * (Math.floorDiv(OCparam.PLOT_SIZE.get() + WorldManager.roadSize, 16));
		int initialZ = plotId.getIndexZ() * (Math.floorDiv(OCparam.PLOT_SIZE.get() + WorldManager.roadSize, 16));
		int chunksRowCount = Math.floorDiv(OCparam.PLOT_SIZE.get(), 16);
		
		return Stream.of(plugin.getWorldManager().getWorld().getLoadedChunks()).filter(ch -> 
		ch.getX() >= initialX &&
		ch.getX() < initialX + chunksRowCount && 
		ch.getZ() >= initialZ &&
		ch.getZ() < initialZ + chunksRowCount).collect(Collectors.toList());
	}
	
	/*public long getCommandBlocksCount() {
		return getLoadedChunks().stream().mapToLong(ch -> ((CraftChunk)ch).getHandle().getTileEntities().values()
				.stream().filter(tile -> tile.getTileType().equals(TileEntityTypes.COMMAND_BLOCK)).count()).sum();
	}*/

	//private int nextAllowedTilesCheckup = 0;

	public void updateTime() {
		currentTime = -1;
		plugin.getTask().cancelTaskById(timeUpdateScheduler);

		if (getParameters().getParameter(PlotParamType.PLOT_TIME_CYCLE)){
			currentTime = getParameters().getParameter(PlotParamType.PLOT_TIME);

			timeUpdateScheduler = plugin.getTask().scheduleSyncRepeatingTask(() -> {
				currentTime = (currentTime + 20 * 20) % 24_000;
				getPlayers().forEach(p -> p.setPlayerTime(currentTime, false));
			}, 20*20, 20*20);

			getPlayers().forEach(p -> p.setPlayerTime(currentTime, false));
		}else
			getPlayers().forEach(p -> p.setPlayerTime(getParameters().getParameter(PlotParamType.PLOT_TIME), false));

	}

	public int getTilesCount() {
		/*if (nextAllowedTilesCheckup >= Bukkit.getCurrentTick())
			return tilesCount;
		
		nextAllowedTilesCheckup = Bukkit.getCurrentTick() + 5;*/
		tilesCount = 0;
		getLoadedChunks(ch -> tilesCount += ((CraftChunk)ch).getHandle().tileEntities.size());
		//tilesCount = getLoadedChunks().stream().mapToInt(ch -> ((CraftChunk)ch).getHandle().tileEntities.size()).sum();
		
		return tilesCount;
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
		
		if (/*!isHanging(e) && */entitiesInPlot.size() == OCparam.MAX_TOTAL_ENTITIES_PER_PLOT.get()) 
			removeEntityInPlot(entitiesInPlot.get(0), true);
		
		List<Entity> list = entitiesInPlot.stream().filter(ent -> ent.getType() == e.getType()).collect(Collectors.toList());
		long count = list.size();
		Entity toRemove = count == 0 ? null : list.get(0);
		
		if (/*isHanging(e) ? count >= OCparam.MAX_HANGINGS_PER_PLOT.get() : */count >= OCparam.MAX_ENTITIES_PER_TYPE_PER_PLOT.get() 
				&& toRemove != null) {
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
	
	public Set<Player> getPlayers() {
		return Collections.unmodifiableSet(playersInPlot);
	}
	
	
	public synchronized List<Entity> getEntities() {
		return new ArrayList<Entity>(entitiesInPlot);
	}
	
	/*
	public long getLoadedTileEntitiesCount() {
		long tiles = 0;
		
		for (int x = plotId.getLocation().getBlockX() ; x < plotId.getLocation().getBlockX() + OCparam.PLOT_SIZE.get() ; x+=16)
			for (int z = plotId.getLocation().getBlockZ() ; z < plotId.getLocation().getBlockZ() + OCparam.PLOT_SIZE.get() ; z+=16)
				if (plugin.getWorldManager().getNmsWorld().isChunkLoaded(x / 16, z / 16))
					tiles += plugin.getWorldManager().getNmsWorld().getChunkAt(x / 16, z / 16).getTileEntities().size();
	
		return tiles;
	}*/
	
	
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
		plugin.getTask().cancelTaskById(destroyScheduler);
		plugin.getTask().cancelTaskById(entitiesCheckupScheduler);
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
		if (OcPermissions.USE_COLORED_TEXT.hasPermission(pc))
			msg = ChatColor.translateAlternateColorCodes('&', msg);
		
		msg = "§7[Parcelle " + getId() + "] §r" + 
		pc.getGroupNameColored() + " " + pc.getName() + " §r§7: " + msg;
		
		for (Player p : getPlayers())
			p.sendMessage(msg);
	}
	
	@Override
	public String toString() {
		return plotId.toString();
	}
	
	public boolean canEnter(Player p ) {
		return canEnter((OlympaPlayerCreatif) AccountProviderAPI.getter().get(p.getUniqueId()));
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
		executeEntryActions((OlympaPlayerCreatif) AccountProviderAPI.getter().get(p.getUniqueId()), tpLoc);
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
		
		Player p = (Player) pc.getPlayer();
		
		//ajoute le joueur aux joueurs du plot s'il n'a pas la perm de bypass les commandes vanilla
		if (!pc.hasStaffPerm(StaffPerm.GHOST_MODE))
			addPlayerInPlot(p);
		
		//exécution instruction commandblock d'entrée
		plugin.getCommandBlocksManager().executeEntryActions(this, p);
		
		//clear les visiteurs en entrée & stockage de leur inventaire
		if (parameters.getParameter(PlotParamType.CLEAR_INCOMING_PLAYERS) && !PlotPerm.BYPASS_ENTRY_ACTIONS.has(this, pc)) {
			List<ItemStack> list = new ArrayList<>();
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
		p.setPlayerTime(currentTime == -1 ? parameters.getParameter(PlotParamType.PLOT_TIME) : currentTime, false);
		
		//définition de la météo
		p.setPlayerWeather(parameters.getParameter(PlotParamType.PLOT_WEATHER));
		
		//joue la musique par défaut du plot
		plugin.getPerksManager().getSongManager().startSong(p, getParameters().getParameter(PlotParamType.SONG));
		
		//reset fly speed if needed
		if (!PlotPerm.DEFINE_OWN_FLY_SPEED.has(this, pc) && getParameters().getParameter(PlotParamType.RESET_VISITOR_FLY_SPEED))
			p.setFlySpeed(0.1f);
		
		
		//send stoplag alert if activated
		if (hasStoplag())
			OCmsg.PLOT_ENTER_STOPLAG_ACTIVATED.send(pc, this);
	}

	/**
	 * Execute quit actions for this player on the plot
	 * @param p
	 */
	public void executeExitActions(Player p) {

		//OlympaPlayerCreatif pc = AccountProviderAPI.getter().get(p.getUniqueId());
		
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
		
		plugin.getPerksManager().getArmorStandManager().closeFor(p);
		
		
	}
	
	/*private boolean isHanging(Entity ent) {
		return ent.getType() == EntityType.ARMOR_STAND || ent.getType() == EntityType.ITEM_FRAME || ent.getType() == EntityType.PAINTING;
	}*/
	
	@Override
	public boolean equals(Object o) {
		return o instanceof Plot && ((Plot)o).getId().equals(plotId);
	}
}
