package fr.olympa.olympacreatif.plot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.PermissionsList;
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
	
	//private Map<Location, SimpleEntry<BlockData, TileEntity>> protectedZoneData = new HashMap<Location, SimpleEntry<BlockData,TileEntity>>();
	
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
		
		//exécution des actions d'entrée pour tous les joueurs sur le plot au moment du chargement
		for (Player player : Bukkit.getOnlinePlayers())
			if (plotId.isInPlot(player.getLocation()))
				PlotsInstancesListener.executeEntryActions(plugin, player, this);
		
		loadInitialEntitiesOnChunks();
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
		
		//exécution des actions d'entrée pour les joueurs étant arrivés sur le plot avant chargement des données du plot
		for (Player p : Bukkit.getOnlinePlayers())
			if (plotId.isInPlot(p.getLocation()))
				PlotsInstancesListener.executeEntryActions(plugin, p, this);
		
		loadInitialEntitiesOnChunks();
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

	/*
	public Map<Location, SimpleEntry<BlockData, TileEntity>> getProtectedZoneData() {
		return protectedZoneData;
	}
	*/

	public void unload() {
		cbData.unload();
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
}
