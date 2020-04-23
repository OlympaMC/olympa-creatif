package fr.olympa.olympacreatif.plot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

import fr.olympa.olympacreatif.OlympaCreatifMain;

public class PlotParameters {

	private OlympaCreatifMain plugin;
	private Map <PlotParamType, Object> parameters = new HashMap<PlotParamType, Object>();
	
	public PlotParameters(OlympaCreatifMain plugin, PlotId id) {
		this.plugin = plugin;
		for (PlotParamType param : PlotParamType.values())
			switch (param) {
			case SPAWN_LOC:
				if (id != null)
					parameters.put(param, id.getLocation());
				else
					parameters.put(param, new Location(plugin.getWorldManager().getWorld(), 0, 100, 0));
				break;
			default:
				parameters.put(param, param.getDefaultValue());
				break;
			
			}
	}

	public void setParameter(PlotParamType param, Object value) {
		parameters.put(param, value);
	}
	
	public Object getParameter(PlotParamType param) {
		if (parameters.containsKey(param))
			return parameters.get(param);
		
		return null;
	}

}
