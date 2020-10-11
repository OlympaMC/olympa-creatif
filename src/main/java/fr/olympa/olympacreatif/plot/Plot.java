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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.world.World;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.perks.KitsManager.KitType;
import fr.olympa.olympacreatif.perks.UpgradesManager.UpgradeType;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;
import fr.olympa.olympacreatif.world.WorldManager;

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
		
		cbData = new PlotCbData(plugin, 
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
		this.cbData = ap.getCbData();
		this.stoplagChecker = new PlotStoplagChecker(plugin, this);
		
		allowLiquidFlow = ap.getAllowLiquidFlow();
		
		executeCommonInstanciationActions();
	}
	
	private void executeCommonInstanciationActions() {
		
		cbData.executeSynchronousInit();
		
		//exécution des actions d'entrée pour les joueurs étant arrivés sur le plot avant chargement des données du plot
		for (Player p : Bukkit.getOnlinePlayers())
			if (plotId.isInPlot(p.getLocation()))
				executeEntryActions(p);
		
		loadInitialEntitiesOnChunks();
		
		//forceload 2*2 chunks à l'origine du plot
		for (int i = plotId.getLocation().getChunk().getX() ; i < plotId.getLocation().getChunk().getX() + 2 ; i++)
			for (int j = plotId.getLocation().getChunk().getZ() ; j < plotId.getLocation().getChunk().getX() + 2 ; j++)
				plugin.getWorldManager().getWorld().setChunkForceLoaded(i, j, true);
	}
	
	private void loadInitialEntitiesOnChunks() {

		int initialX = plotId.getIndexX() * (Math.floorDiv(WorldManager.plotSize + WorldManager.roadSize, 16));
		int initialZ = plotId.getIndexZ() * (Math.floorDiv(WorldManager.plotSize + WorldManager.roadSize, 16));
		int chunksRowCount = Math.floorDiv(WorldManager.plotSize, 16);

		//Bukkit.broadcastMessage("x min : " + initialX + " - max : " + initialX + chunksRowCount);
		//Bukkit.broadcastMessage("z min : " + initialZ + " - max : " + initialZ + chunksRowCount);
		
		
		for (int x = initialX ; x < initialX + chunksRowCount ; x++)
			for (int z = initialZ ; z < initialZ + chunksRowCount ; z++)
				if (plugin.getWorldManager().getWorld().isChunkLoaded(x, z))
					new ArrayList<Entity>(Arrays.asList(plugin.getWorldManager().getWorld().getChunkAt(x, z).getEntities())).forEach(e -> {
						if (e.getType() != EntityType.PLAYER)
							addEntityInPlot(e);
						//Bukkit.broadcastMessage("detected entity " + e + " on chunk " + chunk);
					});
			
			
	}
	
	public PlotParameters getParameters() {
		return parameters;
	}
	
	public PlotMembers getMembers(){
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
		if (entitiesInPlot.size() == WorldManager.maxTotalEntitiesPerPlot) 
			entitiesInPlot.remove(0).remove();
		
		int count = 0;
		Entity toRemove = null;
		
		for (Entity ent : entitiesInPlot)
			if (ent.getType() == e.getType()) {
				count++;
				if (toRemove == null)
					toRemove = ent;
			}
		
		if (count >= WorldManager.maxEntitiesPerTypePerPlot && toRemove != null) {
			entitiesInPlot.remove(toRemove);
			toRemove.remove();
		}
			
		entitiesInPlot.add(e);
	}
	
	public void removeEntityInPlot(Entity e, boolean killEntity) {
		if (killEntity)
			e.remove();
		entitiesInPlot.remove(e);
	}
	
	public Set<Player> getPlayers(){
		return Collections.unmodifiableSet(playersInPlot);
	}
	
	
	public synchronized List<Entity> getEntities(){
		return new ArrayList<Entity>(entitiesInPlot);
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
		Location loc = plotId.getLocation().add(-3, 0, -3);
		loc.setY(WorldManager.worldLevel + 1);
		return loc;
	}

	public void unload() {
		cbData.unload();
		
		//unload des forced chunks
		for (int i = plotId.getLocation().getChunk().getX() ; i < plotId.getLocation().getChunk().getX() + 2 ; i++)
			for (int j = plotId.getLocation().getChunk().getZ() ; j < plotId.getLocation().getChunk().getX() + 2 ; j++)
				plugin.getWorldManager().getWorld().setChunkForceLoaded(i, j, false);
	}

	public PlotId getPlotId() {
		return plotId;
	}
	
	public boolean hasStoplag() {
		if ((int)parameters.getParameter(PlotParamType.STOPLAG_STATUS) == 0)
			return false;
		else
			return true;
	}
	
	public void sendMessage(OlympaPlayerCreatif pc, String msg) {
		if (PermissionsList.USE_COLORED_TEXT.hasPermission(pc))
			msg = ChatColor.translateAlternateColorCodes('&', msg);
		
		msg = "§7[Parcelle " + getPlotId() + "] §r" + 
		pc.getGroupNameColored() + " " + pc.getPlayer().getName() + " §r§7: " + msg;
		
		for (Player p : getPlayers())
			p.sendMessage(msg);
	}
	
	@Override
	public String toString() {
		return plotId.toString();
	}
	
	
	/**
	 * Execute entry actions for this player for the plot
	 * @param p concerned player
	 * @return
	 */
	public boolean executeEntryActions(Player p) {
		
		OlympaPlayerCreatif pc = AccountProvider.get(p.getUniqueId());
		pc.setCurrentPlot(this);
		
		//si le joueur est banni, téléportation en dehors du plot
		if (((HashSet<Long>) getParameters().getParameter(PlotParamType.BANNED_PLAYERS)).contains(pc.getId())) {
			
			if (!pc.hasStaffPerm(StaffPerm.BYPASS_KICK_AND_BAN)) {
				p.sendMessage(Message.PLOT_CANT_ENTER_BANNED.getValue(members.getOwner().getName()));
				return false;	
			}
		}
		
		//ajoute le joueur aux joueurs du plot s'il n'a pas la perm de bypass les commandes vanilla
		if (!pc.hasStaffPerm(StaffPerm.BYPASS_VANILLA_COMMANDS))
			addPlayerInPlot(p);
		
		//exécution instruction commandblock d'entrée
		plugin.getCommandBlocksManager().executeJoinActions(this, p);
		
		//clear les visiteurs en entrée & stockage de leur inventaire
		if ((boolean)parameters.getParameter(PlotParamType.CLEAR_INCOMING_PLAYERS) && members.getPlayerRank(pc) == PlotRank.VISITOR) {
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
		
		//tp au spawn de la zone
		if ((boolean) parameters.getParameter(PlotParamType.FORCE_SPAWN_LOC)) {
			p.teleport(parameters.getSpawnLoc());
			p.sendMessage(Message.TELEPORTED_TO_PLOT_SPAWN.getValue());
		}
		
		if (members.getPlayerRank(pc) == PlotRank.VISITOR) {
			//définition du gamemode
			p.setGameMode((GameMode) parameters.getParameter(PlotParamType.GAMEMODE_INCOMING_PLAYERS));
			
			//définition du flymode
			p.setAllowFlight((boolean) parameters.getParameter(PlotParamType.ALLOW_FLY_INCOMING_PLAYERS));

			//set max fly speed
			p.setFlySpeed(0.1f);
		}
		
		//définition de l'heure du joueur
		p.setPlayerTime((int) parameters.getParameter(PlotParamType.PLOT_TIME), false);
		
		//définition de la météo
		p.setPlayerWeather((WeatherType) parameters.getParameter(PlotParamType.PLOT_WEATHER));
		
		return true;
	}

	/**
	 * Execute quit actions for this player on the plot
	 * @param p
	 */
	public void executeExitActions(Player p) {

		((OlympaPlayerCreatif)AccountProvider.get(p.getUniqueId())).setCurrentPlot(null);
		
		removePlayerInPlot(p);
		plugin.getCommandBlocksManager().excecuteQuitActions(this, p);

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

		LocalSession weSession = plugin.getWorldEditManager().getSession(p);
		
		if (weSession != null) {
			//clear clipboard si le joueur n'en est pas le proprio
			if (members.getPlayerRank(p) != PlotRank.OWNER)
				weSession.setClipboard(null);
			
			World world = weSession.getSelectionWorld();
			
			//reset positions worldedit
			if (world != null && weSession.getRegionSelector(world) != null)
				weSession.getRegionSelector(world).clear();	
		}
	}
	
	
}
