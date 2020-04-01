package fr.olympa.olympacreatif.objects;

import java.util.HashMap;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.messages.Message;

public class Plot {

	private OlympaCreatifMain plugin;
	private HashMap<OlympaPlayer, PlotRank> members = new HashMap<OlympaPlayer, PlotRank>();
	private PlotArea area;
	
	public Plot(OlympaCreatifMain plugin, OlympaPlayer p) {
		this.plugin = plugin;
		members.put(p, PlotRank.PERMISSIONS_OWNER);
		area = new PlotArea(plugin);

		Chunk previousLoadedChunk = null;
		//création des routes autour du plot
		for (int x = area.getFirstCorner().getBlockX()-1 ; x >= area.getFirstCorner().getBlockX()-plugin.roadWidth ; x--)
			for (int z = area.getFirstCorner().getBlockZ()-plugin.roadWidth ; z <= area.getSecondCorner().getBlockZ()-plugin.roadWidth ; z++) {
				//si le chunk n'a pas encore été chargé, on le charge
				if (new Location(plugin.getWorldManager().getWorld(), x, 1, z).getChunk().equals(previousLoadedChunk)) {
					previousLoadedChunk = new Location(plugin.getWorldManager().getWorld(), x, 1, z).getChunk();
					plugin.getWorldManager().getWorld().loadChunk(new Location(plugin.getWorldManager().getWorld(), x, 1, z).getChunk());	
				}
				//ajout du bloc de route
				plugin.getWorldManager().getWorld().getBlockAt(x, plugin.worldLevel, z).setType(Material.STONE);	
			}
		
		for (int x = area.getSecondCorner().getBlockX()+1 ; x <= area.getFirstCorner().getBlockX()+plugin.roadWidth ; x++)
			for (int z = area.getFirstCorner().getBlockZ()-plugin.roadWidth ; z <= area.getSecondCorner().getBlockZ()-plugin.roadWidth ; z++) {
				if (new Location(plugin.getWorldManager().getWorld(), x, 1, z).getChunk().equals(previousLoadedChunk)) {
					previousLoadedChunk = new Location(plugin.getWorldManager().getWorld(), x, 1, z).getChunk();
					plugin.getWorldManager().getWorld().loadChunk(new Location(plugin.getWorldManager().getWorld(), x, 1, z).getChunk());	
				}
				plugin.getWorldManager().getWorld().getBlockAt(x, plugin.worldLevel, z).setType(Material.STONE);	
			}

		for (int z = area.getFirstCorner().getBlockZ()-1 ; z >= area.getFirstCorner().getBlockZ()-plugin.roadWidth ; z--)
			for (int x = area.getFirstCorner().getBlockX() ; x <= area.getSecondCorner().getBlockX() ; x++) {
				if (new Location(plugin.getWorldManager().getWorld(), x, 1, z).getChunk().equals(previousLoadedChunk)) {
					previousLoadedChunk = new Location(plugin.getWorldManager().getWorld(), x, 1, z).getChunk();
					plugin.getWorldManager().getWorld().loadChunk(new Location(plugin.getWorldManager().getWorld(), x, 1, z).getChunk());	
				}
				plugin.getWorldManager().getWorld().getBlockAt(x, plugin.worldLevel, z).setType(Material.STONE);
			}
		
		for (int z = area.getSecondCorner().getBlockZ()+1 ; z <= area.getSecondCorner().getBlockZ()+plugin.roadWidth ; z++)
			for (int x = area.getFirstCorner().getBlockX() ; x <= area.getSecondCorner().getBlockX() ; x++) {
				if (new Location(plugin.getWorldManager().getWorld(), x, 1, z).getChunk().equals(previousLoadedChunk)) {
					previousLoadedChunk = new Location(plugin.getWorldManager().getWorld(), x, 1, z).getChunk();
					plugin.getWorldManager().getWorld().loadChunk(new Location(plugin.getWorldManager().getWorld(), x, 1, z).getChunk());	
				}
				plugin.getWorldManager().getWorld().getBlockAt(x, plugin.worldLevel, z).setType(Material.STONE);
			}
		
		//création des bordures autour du plot
		for (int x = area.getFirstCorner().getBlockX()-1 ; x <= area.getSecondCorner().getBlockX()+1 ; x++) {
			plugin.getWorldManager().getWorld().getBlockAt(x, plugin.worldLevel, area.getFirstCorner().getBlockZ()-1).setType(Material.STONE);
			plugin.getWorldManager().getWorld().getBlockAt(x, plugin.worldLevel, area.getSecondCorner().getBlockZ()+1).setType(Material.STONE);	
		}
		for (int z = area.getFirstCorner().getBlockZ() ; z <= area.getSecondCorner().getBlockZ() ; z++) {
			plugin.getWorldManager().getWorld().getBlockAt(area.getFirstCorner().getBlockX()-1, plugin.worldLevel, z).setType(Material.STONE);
			plugin.getWorldManager().getWorld().getBlockAt(area.getSecondCorner().getBlockX()+1, plugin.worldLevel, z).setType(Material.STONE);	
		}
	}
	
	public PlotArea getArea() {
		return area;
	}
	
	public HashMap<OlympaPlayer, PlotRank> getMembers(){
		return members;
	}
}
