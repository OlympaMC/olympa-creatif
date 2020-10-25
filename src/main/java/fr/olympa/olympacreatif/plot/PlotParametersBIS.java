package fr.olympa.olympacreatif.plot;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import fr.olympa.olympacreatif.OlympaCreatifMain;

public class PlotParametersBIS {


	OlympaCreatifMain plugin;
	private Map <PlotParamTypeBIS<?>, Object> parameters = new HashMap<PlotParamTypeBIS<?>, Object>();
	private PlotId id;
	
	public PlotParametersBIS(OlympaCreatifMain plugin, PlotId id) {
		this.plugin = plugin;
		this.id = id;
		
		Field[] fields = PlotParamTypeBIS.class.getDeclaredFields();
		for (Field field : fields)
			if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()))
				try {
					field.setAccessible(true);
					PlotParamTypeBIS<?> param = (PlotParamTypeBIS<?>) field.get(null);
					parameters.put(param, param.getDefaultValue());
				} catch (IllegalArgumentException | IllegalAccessException e) {
					plugin.getLogger().log(Level.SEVERE, "Unable to use reflection on plot parameter " + field.getName() + " for plot " + id);
					e.printStackTrace();
				}
		
		if (id != null)
			setSpawnLoc(id.getLocation().clone().add(0.5, 3, 0.5));
	}

	/**
	 * For internal use only, NEVER use this method!
	 */
	public synchronized void setParameter(PlotParamTypeBIS<?> param, Object value) {
		parameters.put(param, value);
	}
	
	@SuppressWarnings("unchecked")
	public synchronized <T> T getParameter(PlotParamTypeBIS<T> param) {
		return (T) parameters.get(param);
	}
	
	public synchronized Set<PlotParamTypeBIS<?>> getParameters(){
		return Collections.unmodifiableSet(parameters.keySet());
	}
	
	public synchronized Location getSpawnLoc() {
		return new Location(plugin.getWorldManager().getWorld(), 
				getParameter(PlotParamTypeBIS.SPAWN_LOC_X), 
				getParameter(PlotParamTypeBIS.SPAWN_LOC_Y), 
				getParameter(PlotParamTypeBIS.SPAWN_LOC_Z));
	}
	
	
	public synchronized void setSpawnLoc(Location loc) {
		if (id.isInPlot(loc)) {
			setParameter(PlotParamTypeBIS.SPAWN_LOC_X, loc.getBlockX());
			setParameter(PlotParamTypeBIS.SPAWN_LOC_Y, loc.getBlockY());
			setParameter(PlotParamTypeBIS.SPAWN_LOC_Z, loc.getBlockZ());
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public synchronized String toJson() {
		JSONObject json = new JSONObject();
		
		for (PlotParamTypeBIS<?> param : getParameters())
			json.put(param.getId(), parameters.get(param).toString());
		
		return json.toString();
	}
	
	public synchronized static PlotParametersBIS fromJson(OlympaCreatifMain plugin, PlotId plotId, String jsonString) {

		PlotParametersBIS params = new PlotParametersBIS(plugin, plotId);
		
		try {
			JSONObject json = (JSONObject) new JSONParser().parse(jsonString);
			Gson gson = new Gson();
			
			for (PlotParamTypeBIS<?> param : params.getParameters())
				if (json.containsKey(param.getId()))
					
					if (param.getDefaultValue() instanceof Integer) {
						params.setParameter(param, gson.fromJson((String) json.get(param.getId()), Integer.class));
						
					}else if (param.getDefaultValue() instanceof Boolean) {
						params.setParameter(param, gson.fromJson((String) json.get(param.getId()), Boolean.class));
						
					}else if (param.getDefaultValue() instanceof WeatherType) {
						params.setParameter(param, gson.fromJson((String) json.get(param.getId()), WeatherType.class));
						
					}else if (param.getDefaultValue() instanceof GameMode) {
						params.setParameter(param, gson.fromJson((String) json.get(param.getId()), GameMode.class));
						
					}else if (param.getDefaultValue() instanceof List) {
						if (param.equals(PlotParamTypeBIS.BANNED_PLAYERS))
							params.setParameter(param, gson.fromJson((String) json.get(param.getId()), new TypeToken<ArrayList<Long>>(){}.getType()));
						else if (param.equals(PlotParamTypeBIS.LIST_ALLOWED_INTERRACTION))
							params.setParameter(param, gson.fromJson((String) json.get(param.getId()), new TypeToken<ArrayList<Material>>(){}.getType()));
						
					}
			
			return params;
		} catch (ParseException e) {
			e.printStackTrace();
			return params;
		}
	}
}
