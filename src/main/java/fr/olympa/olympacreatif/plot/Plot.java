package fr.olympa.olympacreatif.plot;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.CbBossBar;
import fr.olympa.olympacreatif.commandblocks.CbTeam;
import fr.olympa.olympacreatif.commandblocks.PlotCbData;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;
import fr.olympa.olympacreatif.world.WorldManager;
import net.minecraft.server.v1_15_R1.TileEntity;

public class Plot {

	private OlympaCreatifMain plugin;
	
	private PlotMembers members;
	private PlotParameters parameters;
	private PlotId plotId;
	
	private PlotCbData cbData;

	private List<Player> playersInPlot = new ArrayList<Player>();
	private List<Entity> entitiesInPlot = new ArrayList<Entity>();
	
	private Map<Location, SimpleEntry<BlockData, TileEntity>> protectedZoneData = new HashMap<Location, SimpleEntry<BlockData,TileEntity>>();
	
	//constructeur pour un plot n'existant pas encore
	public Plot(OlympaCreatifMain plugin, OlympaPlayerInformations p) {
		this.plugin = plugin;
		
		plotId = PlotId.createNew(plugin);
		
		parameters = new PlotParameters(plugin, plotId);
		members = new PlotMembers(plugin, plotId);
		
		members.set(p, PlotRank.OWNER);
		
		cbData = plugin.getCommandBlocksManager().createPlotCbData();
		
		//plugin.getCommandBlocksManager().registerPlot(this);
		
		//exécution des actions d'entrée pour tous les joueurs sur le plot au moment du chargement
		for (Player player : Bukkit.getOnlinePlayers())
			if (plotId.isInPlot(player.getLocation()))
				PlotsInstancesListener.executeEntryActions(plugin, player, this);
	}
	
	//chargement d'un plot déjà existant
	public Plot(AsyncPlot ap) {
		this.plugin = ap.getPlugin();
		this.parameters = ap.getParameters();
		this.members = ap.getMembers();
		this.plotId = ap.getId();
		
		cbData = plugin.getCommandBlocksManager().createPlotCbData();

		//plugin.getCommandBlocksManager().registerPlot(this);
		
		//exécution des actions d'entrée pour les joueurs étant arrivés sur le plot avant chargement des données du plot
		for (Player p : Bukkit.getOnlinePlayers())
			if (plotId.isInPlot(p.getLocation()))
				PlotsInstancesListener.executeEntryActions(plugin, p, this);
	}
	
	public PlotParameters getParameters() {
		return parameters;
	}
	
	public PlotMembers getMembers(){
		return members;
	}
	
	public PlotId getLoc() {
		return plotId;
	}
	
	public PlotCbData getCbData() {
		return cbData;
	}
	
	public void addPlayerInPlot(Player p) {
		if (!playersInPlot.contains(p))
			playersInPlot.add(p);
	}
	
	public void removePlayerInPlot(Player p) {
		playersInPlot.remove(p);
	}
	
	public void addEntityInPlot(Entity e) {
		entitiesInPlot.add(e);
	}
	
	public void clearEntitiesInPlot() {
		entitiesInPlot.clear();
	}
	
	public List<Player> getPlayers(){
		return Collections.unmodifiableList(playersInPlot);
	}
	
	public List<Entity> getEntities(){
		return Collections.unmodifiableList(entitiesInPlot);
	}
	
	public void teleportOut(Player p) {
		p.teleport(getOutLoc());
	}
	
	public Location getOutLoc() {
		Location loc = plotId.getLocation().add(-3, 0, -3);
		loc.setY(WorldManager.worldLevel + 1);
		return loc;
	}

	public Map<Location, SimpleEntry<BlockData, TileEntity>> getProtectedZoneData() {
		return protectedZoneData;
	}

	public void unload() {
		cbData.unload();
	}

	public PlotId getPlotId() {
		return plotId;
	}
}
