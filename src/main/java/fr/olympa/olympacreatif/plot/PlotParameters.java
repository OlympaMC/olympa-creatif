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

public class PlotParameters {


	OlympaCreatifMain plugin;
	private Map <PlotParamType<?>, Object> parameters = new HashMap<PlotParamType<?>, Object>();
	private PlotId id;
	
	public PlotParameters(OlympaCreatifMain plugin, PlotId id) {
		this.plugin = plugin;
		this.id = id;
		
		Field[] fields = PlotParamType.class.getDeclaredFields();
		for (Field field : fields)
			if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()))
				try {
					field.setAccessible(true);
					PlotParamType<?> param = (PlotParamType<?>) field.get(null);
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
	 * To set the spawn location, use the dedicated method setSpawnLoc
	 */
	public synchronized void setParameter(PlotParamType<?> param, Object value) {
		//if (!param.equals(PlotParamType.SPAWN_LOC_X) && !param.equals(PlotParamType.SPAWN_LOC_Y) && !param.equals(PlotParamType.SPAWN_LOC_Z))
		parameters.put(param, value);
	}
	
	@SuppressWarnings("unchecked")
	public synchronized <T> T getParameter(PlotParamType<T> param) {
		return (T) parameters.get(param);
	}
	
	public synchronized Set<PlotParamType<?>> getParameters(){
		return Collections.unmodifiableSet(parameters.keySet());
	}
	
	public synchronized Location getSpawnLoc() {
		return new Location(plugin.getWorldManager().getWorld(), 
				getParameter(PlotParamType.SPAWN_LOC_X), 
				getParameter(PlotParamType.SPAWN_LOC_Y), 
				getParameter(PlotParamType.SPAWN_LOC_Z));
	}
	
	
	public synchronized void setSpawnLoc(Location loc) {
		if (id.isInPlot(loc)) {
			setParameter(PlotParamType.SPAWN_LOC_X, loc.getBlockX());
			setParameter(PlotParamType.SPAWN_LOC_Y, loc.getBlockY());
			setParameter(PlotParamType.SPAWN_LOC_Z, loc.getBlockZ());
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public synchronized String toJson() {
		JSONObject json = new JSONObject();
		
		for (PlotParamType<?> param : getParameters())
			json.put(param.getId(), parameters.get(param).toString());

		if (!id.isInPlot(getSpawnLoc()))
			System.out.println("§cERREUR SAVE SPAWN PLOT " + id + " : " + getSpawnLoc() + " IS OUT OF PLOT AREA");
		
		return json.toString();
	}
	
	public synchronized static PlotParameters fromJson(OlympaCreatifMain plugin, PlotId plotId, String jsonString) {

		PlotParameters params = new PlotParameters(plugin, plotId);
		
		try {
			JSONObject json = (JSONObject) new JSONParser().parse(jsonString);
			Gson gson = new Gson();
			
			for (PlotParamType<?> param : params.getParameters())
				if (json.containsKey(param.getId()))

					if (param.getDefaultValue() instanceof Integer) {
						params.setParameter(param, gson.fromJson((String) json.get(param.getId()), Integer.class));
						
					}else if (param.getDefaultValue() instanceof String) {
						params.setParameter(param, (String) json.get(param.getId()));
						
					}else if (param.getDefaultValue() instanceof Boolean) {
						params.setParameter(param, gson.fromJson((String) json.get(param.getId()), Boolean.class));
						
					}else if (param.getDefaultValue() instanceof WeatherType) {
						params.setParameter(param, gson.fromJson((String) json.get(param.getId()), WeatherType.class));
						
					}else if (param.getDefaultValue() instanceof GameMode) {
						params.setParameter(param, gson.fromJson((String) json.get(param.getId()), GameMode.class));
						
					}else if (param.getDefaultValue() instanceof List) {
						if (param.equals(PlotParamType.BANNED_PLAYERS))
							params.setParameter(param, gson.fromJson((String) json.get(param.getId()), new TypeToken<ArrayList<Long>>(){}.getType()));
						else if (param.equals(PlotParamType.LIST_ALLOWED_INTERRACTION))
							params.setParameter(param, gson.fromJson((String) json.get(param.getId()), new TypeToken<ArrayList<Material>>(){}.getType()));
						
					}
			
			if (!plotId.isInPlot(params.getSpawnLoc()))
				System.out.println("§4ERREUR LOAD SPAWN PLOT " + plotId + " : " + params.getSpawnLoc() + " IS OUT OF PLOT AREA");
			
			return params;
		} catch (ParseException e) {
			e.printStackTrace();
			return params;
		}
	}
}
