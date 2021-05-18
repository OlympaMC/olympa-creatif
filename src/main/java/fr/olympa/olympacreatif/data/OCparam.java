package fr.olympa.olympacreatif.data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import fr.olympa.olympacreatif.OlympaCreatifMain;



public class OCparam<T> {
	public static final OCparam<Integer> CB_MAX_CMDS_LEFT = new OCparam<Integer>(0);
	public static final OCparam<Integer> CB_MAX_OBJECTIVES_PER_PLOT = new OCparam<Integer>(0);
	public static final OCparam<Integer> CB_MAX_TEAMS_PER_PLOT = new OCparam<Integer>(0);
	public static final OCparam<Integer> CB_MIN_TICKS_BETWEEN_EACH_CB_EXECUTION = new OCparam<Integer>(10000);
	
	public static final OCparam<Integer> MAX_ENTITIES_PER_TYPE_PER_PLOT = new OCparam<Integer>(50);
	public static final OCparam<Integer> MAX_TOTAL_ENTITIES_PER_PLOT = new OCparam<Integer>(60);
	//public static final OCparam<Integer> MAX_HANGINGS_PER_PLOT = new OCparam<Integer>(50);
	
	public static final OCparam<String> WORLD_NAME = new OCparam<String>("world");
	public static final OCparam<Position> SPAWN_LOC = new OCparam<Position>(new Position());
	
	public static final OCparam<Position> HOLO_HELP_1_LOC = new OCparam<Position>(new Position());
	public static final OCparam<Position> HOLO_HELP_2_LOC = new OCparam<Position>(new Position());
	public static final OCparam<List<String>> HOLO_HELP_1_TEXT = new OCparam<List<String>>(new ArrayList<String>(), new TypeToken<List<String>>() {}.getType());
	public static final OCparam<List<String>> HOLO_HELP_2_TEXT = new OCparam<List<String>>(new ArrayList<String>(), new TypeToken<List<String>>() {}.getType());
	
	public static final OCparam<Integer> INCOME_NOT_AFK = new OCparam<Integer>(0); 
	public static final OCparam<Integer> INCOME_AFK = new OCparam<Integer>(0); 
	
	public static final OCparam<Integer> PLOT_SIZE = new OCparam<Integer>(0);

	public static final OCparam<String> ROAD_SCHEM_NAME_X = new OCparam<String>("fileName");
	public static final OCparam<String> ROAD_SCHEM_NAME_Z = new OCparam<String>("fileName");

	public static final OCparam<Integer> MAX_CB_PER_CHUNK = new OCparam<Integer>(100);
	
	public static final OCparam<Integer> MAX_TILE_PER_PLOT = new OCparam<Integer>(6000);

	public static final OCparam<Integer> MAX_HOLOS_PER_PLOT = new OCparam<Integer>(60);
	public static final OCparam<Integer> MAX_LINES_PER_HOLO = new OCparam<Integer>(15);
	
	public static final OCparam<Integer> WE_MAX_NBT_SIZE = new OCparam<Integer>(10000);
	
	
	//public static final OCparam<Integer> MAX_CB_PER_PLOT = new OCparam<Integer>(1000);
	
	private T value;
	private Type type;

	private OCparam(T value) {
		this(value, value.getClass());
	}
	private OCparam(T value, Type type) {
		this.value = value;
		this.type = type;
	}
	
	public T get() {
		return value;
	}
	
	/*@SuppressWarnings("unchecked")
	public void setValueFromBdd(Object value) {
		//if (value.getClass().equals(paramClass))
		this.value = (T) value;
	}*/
	
	public void setValue(T value) {
		this.value = value;
	}
	
	private Type getType(){
		return type;
	}

	
	/**
	 * Return all public static fields of the class
	 * @return
	 */
	public static Map<String, OCparam<?>> values() {
		Map<String, OCparam<?>> map = new HashMap<String, OCparam<?>>();
		
		Field[] fields = OCparam.class.getDeclaredFields();
		for (Field field : fields)
			if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()))
				try {
					field.setAccessible(true);
					map.put(field.getName(), (OCparam<?>) field.get(null));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
		
		return map;
	}
	
	/**
	 * Set values of all public static fields according to provided json data
	 * @param jsonText
	 */
	public static void fromJson(String jsonText) {
		try {
			JSONObject json = (JSONObject) new JSONParser().parse(jsonText);
			Gson gson = new GsonBuilder().serializeNulls().create();
			
			for (Entry<String, OCparam<?>> e : values().entrySet()) {
				if (json.containsKey(e.getKey())) 
					e.getValue().setValue(gson.fromJson((String) json.get(e.getKey()), e.getValue().getType()));
					/*if (e.getValue().get() instanceof Integer)
						e.getValue().setValueFromBdd(gson.fromJson((String) json.get(e.getKey()), Integer.class));
					else if (e.getValue().get() instanceof String)
						e.getValue().setValueFromBdd(gson.fromJson((String) json.get(e.getKey()), String.class));
					else if (e.getValue().get() instanceof Position)
						e.getValue().setValueFromBdd(gson.fromJson((String) json.get(e.getKey()), Position.class));*/
				else
					OlympaCreatifMain.getInstance().getLogger().warning("§eLe paramètre " + e.getKey() + " n'existe pas en bdd ! Une valeur par défaut a été définie : " + e.getValue().get());
			}
			
			OlympaCreatifMain.getInstance().getLogger().info("§aParamètres du serveur correctement chargés.");	
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Return all public static fields of this class as a json string
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String toJson() {
		JSONObject json = new JSONObject();
		//Gson gson = new GsonBuilder().serializeNulls().create();
		
		for (Entry<String, OCparam<?>> param : values().entrySet())
			json.put(param.getKey(), new Gson().toJson(param.getValue().get(), param.getValue().getType()));
		
		return json.toString();
	}
}
