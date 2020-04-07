package fr.olympa.olympacreatif.plot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import fr.olympa.api.objects.OlympaPlayerInformations;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.datas.Message;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;

public class Plot {

	private OlympaCreatifMain plugin;
	
	private PlotMembers members;
	private PlotArea area;
	private PlotParameters parameters;
	private PlotId plotId;
	
	private PlotListener listener;

	private List<Player> playersInPlot = new ArrayList<Player>();
	
	private boolean isActive = true; //sert à détecter quand un chunk est inactif
	
	//constructeur pour un plot n'existant pas encore
	Plot(OlympaCreatifMain plugin, OlympaPlayerInformations p) {
		this.plugin = plugin;
		area = new PlotArea(plugin);
		parameters = new PlotParameters(plugin, area, true);
		plotId = new PlotId(plugin, area);
		members = new PlotMembers(plugin, plotId);
		listener = new PlotListener(plugin, this);

		members.set(p, PlotRank.OWNER);
		plugin.getServer().getPluginManager().registerEvents(listener, plugin);
	}
	
	public Plot(AsyncPlot ap) {
		this.plugin = ap.getPlugin();
		this.area = ap.getArea();
		this.parameters = ap.getParameters();
		this.members = ap.getMembers();
		this.plotId = ap.getId();
		
		this.listener = new PlotListener(plugin, this);
		
		plugin.getServer().getPluginManager().registerEvents(listener, plugin);

		//exécution des actions d'entrée pour les joueurs étant arrivés sur le plot avant chargement des données du plot
		for (Player p : Bukkit.getOnlinePlayers())
			if (area.isInPlot(p.getLocation()))
				listener.executeEntryActions(p);
	}

	public PlotArea getArea() {
		return area;
	}
	
	public PlotParameters getParameters() {
		return parameters;
	}
	
	public PlotMembers getMembers(){
		return members;
	}
	
	public PlotId getPlotId() {
		return plotId;
	}
	public void unregisterListener() {
		HandlerList.unregisterAll(listener);
	}
	
	public boolean isActive() {
		return isActive;
	}
	
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	public void addPlayerInPlot(Player p) {
		playersInPlot.add(p);
	}
	
	public void removePlayer(Player p) {
		playersInPlot.remove(p);
	}
	
	public List<Player> getPlayers(){
		return Collections.unmodifiableList(playersInPlot);
	}
	
	public void teleportOut(Player p) {
		p.teleport(area.getFirstCorner().clone().add(-3, Integer.valueOf(Message.PARAM_WORLD_LEVEL.getValue()), -3));
	}
}
