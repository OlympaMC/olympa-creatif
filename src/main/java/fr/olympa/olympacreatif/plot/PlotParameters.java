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
	
	@SuppressWarnings("unchecked")
	public static PlotParameters fromJson(PlotId plotId, String jsonString) {

		PlotParameters params = new PlotParameters(plotId);
		
		try {
			JSONObject json = (JSONObject) new JSONParser().parse(jsonString);
			
			for (Object key : json.keySet())
				if (EnumUtils.isValidEnum(PlotParamType.class, (String)key)) {
					
					PlotParamType type = PlotParamType.valueOf((String)key);
					
					//types booléens
					if (type.getType().equals(Boolean.class))
						params.setParameter(type, json.get(key));
					
					//gestion integers
					else if (type.getType().equals(Integer.class))
						try {
							params.setParameter(type, Integer.valueOf((String) json.get(key)));
						}catch(NumberFormatException e) {
						}
					
					//gestion gamemode
					else if (type == PlotParamType.GAMEMODE_INCOMING_PLAYERS)
						if (EnumUtils.isValidEnum(GameMode.class, (String)json.get(key)))
							params.setParameter(type, GameMode.valueOf((String)json.get(key)));
						else
							params.setParameter(type, GameMode.CREATIVE);
					
					//gestion météo
					else if (type == PlotParamType.PLOT_WEATHER)
						if (EnumUtils.isValidEnum(WeatherType.class, (String)json.get(key)))
							params.setParameter(type, WeatherType.valueOf((String)json.get(key)));
						else
							params.setParameter(type, WeatherType.CLEAR);
					
					//gestion listes
					else if (type.getType().equals(List.class)) {
						String[] args = ((String)json.get(key)).substring(1, ((String)json.get(key)).length() - 1).split(",");
						
						for (int i = 0 ; i < args.length ; i++) {
							//gestion joueurs bannis du plot
							if (type == PlotParamType.BANNED_PLAYERS)
								try {
									((List<Long>)params.getParameter(PlotParamType.BANNED_PLAYERS)).add(Long.valueOf(args[i]));
								}catch(NumberFormatException e) {
								}	
							
							//gestion interractions autorisées
							else if (type == PlotParamType.LIST_ALLOWED_INTERRACTION)
								if (Material.getMaterial(args[i]) != null)
									((List<Material>)params.getParameter(PlotParamType.BANNED_PLAYERS)).add(Material.getMaterial(args[i]));
						}
					}
						
				}
			
			return params;
		} catch (ParseException e) {
			e.printStackTrace();
			return params;
		}
	}
}
