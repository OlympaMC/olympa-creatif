package fr.olympa.olympacreatif.plot;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.olympacreatif.OlympaCreatifMain;

public class Plot {

	private OlympaCreatifMain plugin;
	private HashMap<OlympaPlayer, PlotRank> members = new HashMap<OlympaPlayer, PlotRank>();
	private PlotArea area;
	private PlotParameters parameters;
	private PlotId id;
	
	//constructeur pour un plot n'existant pas encore
	public Plot(OlympaCreatifMain plugin, OlympaPlayer p) {
		this.plugin = plugin;
		members.put(p, PlotRank.PERMISSIONS_OWNER);
		area = new PlotArea(plugin);
		parameters = new PlotParameters(plugin);
		id = new PlotId(area);
		
		//création des routes autour du plot
		if (plugin.getPlot(area.getFirstCorner().clone().add(-plugin.plotXwidth-plugin.roadWidth, 0, 0)) == null)
			for (int x = area.getFirstCorner().getBlockX()-1 ; x >= area.getFirstCorner().getBlockX()-plugin.roadWidth ; x--)
				for (int z = area.getFirstCorner().getBlockZ()-plugin.roadWidth ; z <= area.getSecondCorner().getBlockZ()+plugin.roadWidth ; z++)
					plugin.getWorldManager().addToBuildWaitingList(new Location(plugin.getWorldManager().getWorld(), x, plugin.worldLevel, z), Bukkit.createBlockData(Material.STONE));

		if (plugin.getPlot(area.getFirstCorner().clone().add(plugin.plotXwidth+plugin.roadWidth, 0, 0)) == null)
			for (int x = area.getSecondCorner().getBlockX()+1 ; x <= area.getSecondCorner().getBlockX()+plugin.roadWidth ; x++)
				for (int z = area.getFirstCorner().getBlockZ()-plugin.roadWidth ; z <= area.getSecondCorner().getBlockZ()+plugin.roadWidth ; z++)
					plugin.getWorldManager().addToBuildWaitingList(new Location(plugin.getWorldManager().getWorld(), x, plugin.worldLevel, z), Bukkit.createBlockData(Material.STONE));

		if (plugin.getPlot(area.getFirstCorner().clone().add(0, 0, -plugin.plotZwidth-plugin.roadWidth)) == null)
			for (int z = area.getFirstCorner().getBlockZ()-1 ; z >= area.getFirstCorner().getBlockZ()-plugin.roadWidth ; z--)
				for (int x = area.getFirstCorner().getBlockX() ; x <= area.getSecondCorner().getBlockX() ; x++) 
					plugin.getWorldManager().addToBuildWaitingList(new Location(plugin.getWorldManager().getWorld(), x, plugin.worldLevel, z), Bukkit.createBlockData(Material.STONE));

		if (plugin.getPlot(area.getFirstCorner().clone().add(0, 0, plugin.plotZwidth+plugin.roadWidth)) == null)
			for (int z = area.getSecondCorner().getBlockZ()+1 ; z <= area.getSecondCorner().getBlockZ()+plugin.roadWidth ; z++)
				for (int x = area.getFirstCorner().getBlockX() ; x <= area.getSecondCorner().getBlockX() ; x++) 
					plugin.getWorldManager().addToBuildWaitingList(new Location(plugin.getWorldManager().getWorld(), x, plugin.worldLevel, z), Bukkit.createBlockData(Material.STONE));

		//création des bordures autour du plot
		
		for (int x = area.getFirstCorner().getBlockX()-1 ; x <= area.getSecondCorner().getBlockX()+1 ; x++) {
			plugin.getWorldManager().addToBuildWaitingList(new Location(plugin.getWorldManager().getWorld(), x, plugin.worldLevel+1, area.getFirstCorner().getBlockZ()-1), Bukkit.createBlockData(Material.GRANITE_SLAB));
			plugin.getWorldManager().addToBuildWaitingList(new Location(plugin.getWorldManager().getWorld(), x, plugin.worldLevel+1, area.getSecondCorner().getBlockZ()+1), Bukkit.createBlockData(Material.GRANITE_SLAB));	
		}
		for (int z = area.getFirstCorner().getBlockZ() ; z <= area.getSecondCorner().getBlockZ() ; z++) {
			plugin.getWorldManager().addToBuildWaitingList(new Location(plugin.getWorldManager().getWorld(), area.getFirstCorner().getX()-1, plugin.worldLevel+1, z), Bukkit.createBlockData(Material.GRANITE_SLAB));
			plugin.getWorldManager().addToBuildWaitingList(new Location(plugin.getWorldManager().getWorld(), area.getSecondCorner().getX()+1, plugin.worldLevel+1, z), Bukkit.createBlockData(Material.GRANITE_SLAB));
		}
	}
	
	//constructeur pour un plot déjà existant
	public Plot(OlympaCreatifMain plugin, PlotId id, PlotArea area, PlotParameters parameters, HashMap<OlympaPlayer, PlotRank> members) {
		this.plugin = plugin;
		this.id = id;
		this.area = area;
		this.parameters = parameters;
		this.members = members;
	}
	
	public PlotArea getArea() {
		return area;
	}
	
	public PlotId getId() {
		return id;
	}
	
	public PlotParameters getParameters() {
		return parameters;
	}
	
	public HashMap<OlympaPlayer, PlotRank> getMembers(){
		return members;
	}
	
	public PlotRank getPlayerRank(OlympaPlayer p) {
		if (members.containsKey(p))
			return members.get(p);
		else
			return PlotRank.PERMISSIONS_NULL;
		
	}
}
