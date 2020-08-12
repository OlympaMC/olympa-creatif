package fr.olympa.olympacreatif.plot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.EnumUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import fr.olympa.olympacreatif.OlympaCreatifMain;

public class PlotParameters {

	private Map <PlotParamType, Object> parameters = new HashMap<PlotParamType, Object>();
	
	public PlotParameters(PlotId id) {
		for (PlotParamType param : PlotParamType.values())
			switch (param) {
			case SPAWN_LOC_X:
				if (id != null)
					parameters.put(param, id.getLocation().getBlockX());
				else
					parameters.put(param, 0);
				break;
				
			case SPAWN_LOC_Y:
				if (id != null)
					parameters.put(param, id.getLocation().getBlockY());
				else
					parameters.put(param, 100);
				break;
				
			case SPAWN_LOC_Z:
				if (id != null)
					parameters.put(param, id.getLocation().getBlockZ());
				else
					parameters.put(param, 0);
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
	
	public Location getSpawnLoc(OlympaCreatifMain plugin) {
		return new Location(plugin.getWorldManager().getWorld(), 
				(int) parameters.get(PlotParamType.SPAWN_LOC_X), 
				(int)parameters.get(PlotParamType.SPAWN_LOC_Y), 
				(int)parameters.get(PlotParamType.SPAWN_LOC_Z));
	}
	
	public void setSpawnLoc(Location loc) {
		parameters.put(PlotParamType.SPAWN_LOC_X, loc.getBlockX());
		parameters.put(PlotParamType.SPAWN_LOC_Y, loc.getBlockY());
		parameters.put(PlotParamType.SPAWN_LOC_Z, loc.getBlockZ());
	}
	
	@SuppressWarnings("unchecked")
	public String toJson() {
		JSONObject json = new JSONObject();
		
		for (PlotParamType param : PlotParamType.values())
			if (parameters.containsKey(param))
				json.put(param.toString(), parameters.toString());
		
		return json.toString();
	}
	
	public static PlotParameters fromJson(PlotId plotId, String jsonString) {

		PlotParameters params = new PlotParameters(plotId);
		
		try {
			JSONObject json = (JSONObject) new JSONParser().parse(jsonString);
			
			for (Object key : json.keySet())
				if (EnumUtils.isValidEnum(PlotParamType.class, (String)key)) {
					
					PlotParamType type = PlotParamType.valueOf((String)json.get(key));
					if (type.getType().equals(Boolean.class) || type.getType().equals(Integer.class) || type.getType().equals(List.class))
						params.setParameter(type, json.get(key));
					
					else if (type.getType().equals(GameMode.class))
						if (EnumUtils.isValidEnum(GameMode.class, (String)json.get(key)))
							params.setParameter(type, GameMode.valueOf((String)json.get(key)));
						else
							params.setParameter(type, GameMode.CREATIVE);
					
					else if (type.getType().equals(WeatherType.class))
						if (EnumUtils.isValidEnum(WeatherType.class, (String)json.get(key)))
							params.setParameter(type, WeatherType.valueOf((String)json.get(key)));
						else
							params.setParameter(type, WeatherType.CLEAR);
				}
			
			return params;
		} catch (ParseException e) {
			e.printStackTrace();
			return params;
		}
	}
}
