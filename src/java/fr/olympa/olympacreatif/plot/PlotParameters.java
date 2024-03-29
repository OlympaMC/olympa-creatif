package fr.olympa.olympacreatif.plot;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.gson.Gson;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Position;

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
			setParameter(PlotParamType.SPAWN_LOC, new Position(id.getLocation().clone().add(0.5, 3, 0.5)));
	}

	/**
	 * For internal use only, NEVER use this method!
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
	
	/*
	public synchronized Location getSpawnLoc() {
		return new Location(plugin.getWorldManager().getWorld(), 
				getParameter(PlotParamType.SPAWN_LOC_X), 
				getParameter(PlotParamType.SPAWN_LOC_Y), 
				getParameter(PlotParamType.SPAWN_LOC_Z));
	}
	
	
	public synchronized boolean setSpawnLoc(Location loc) {
		if (id.isInPlot(loc)) {
			setParameter(PlotParamType.SPAWN_LOC_X, loc.getBlockX());
			setParameter(PlotParamType.SPAWN_LOC_Y, loc.getBlockY());
			setParameter(PlotParamType.SPAWN_LOC_Z, loc.getBlockZ());
			return true;
		} else
			return false;
	}*/
	
	
	@SuppressWarnings("unchecked")
	public synchronized String toJson() {
		JSONObject json = new JSONObject();
		Gson gson = new Gson();
		
		for (PlotParamType<?> param : getParameters())
			json.put(param.getId(), gson.toJson(parameters.get(param)));

		/*if (!id.isInPlot(getSpawnLoc()))
			System.out.println("§cERREUR SAVE SPAWN PLOT " + id + " : " + getSpawnLoc() + " IS OUT OF PLOT AREA");*/
		
		return json.toString();
	}
	
	public synchronized static PlotParameters fromJson(OlympaCreatifMain plugin, PlotId plotId, String jsonString) {

		PlotParameters params = new PlotParameters(plugin, plotId);
		
		try {
			JSONObject json = (JSONObject) new JSONParser().parse(jsonString);
			Gson gson = new Gson();
			
			for (PlotParamType<?> param : params.getParameters())
				if (json.containsKey(param.getId()))
					params.setParameter(param, gson.fromJson((String) json.get(param.getId()), param.getType()));
			
			return params;
		} catch (Exception e) {
			e.printStackTrace();
			return params;
		}
	}
}
