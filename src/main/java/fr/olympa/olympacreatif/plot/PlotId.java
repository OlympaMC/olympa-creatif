package fr.olympa.olympacreatif.plot;

import org.bukkit.Location;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;

public class PlotId{

	String id;
	private OlympaCreatifMain plugin;
	
	public PlotId(OlympaCreatifMain plugin, PlotArea area) {
		this.plugin = plugin;
		this.id = ((int) area.getFirstCorner().getBlockX() / (Integer.parseInt(Message.PARAM_PLOT_X_SIZE.getValue()) + Integer.parseInt(Message.PARAM_ROAD_SIZE.getValue()))) +  "," +
				((int) area.getFirstCorner().getBlockZ() / (Integer.parseInt(Message.PARAM_PLOT_Z_SIZE.getValue()) + Integer.parseInt(Message.PARAM_ROAD_SIZE.getValue())));
	}
	
	public PlotId(OlympaCreatifMain plugin, String plotId) {
		this.plugin = plugin;
		this.id = plotId;
	}
	
	public PlotId(OlympaCreatifMain plugin, int x, int z) {
		this.plugin = plugin;
		this.id = x + "," + z; 
	}
	
	public String getId() {
		return id;
	}
	
	public Location getLocation() {
		return new Location(plugin.getWorldManager().getWorld(), Integer.valueOf(id.split(",")[0]) * Integer.valueOf(Message.PARAM_PLOT_X_SIZE.getValue()) + 0.5, Integer.valueOf(Message.PARAM_WORLD_LEVEL.getValue()), Integer.valueOf(id.split(",")[1]) * Integer.valueOf(Message.PARAM_PLOT_Z_SIZE.getValue()) + 0.5);
	}
	
	public static PlotId fromDbFormat(OlympaCreatifMain plugin, String data) {
		return new PlotId(plugin, data);
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof PlotId && ((PlotId) obj).getId().equals(id);
		
	}
}
