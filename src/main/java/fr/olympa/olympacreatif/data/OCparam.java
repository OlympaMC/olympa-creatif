package fr.olympa.olympacreatif.data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.Location;
import org.json.simple.JSONObject;

import fr.olympa.olympacreatif.plot.PlotParamType;

public class OCparam<T> {
	public static final OCparam<Integer> CB_COMMAND_TICKETS_CONSUMED_BY_SETBLOCK = new OCparam<Integer>(4);
	public static final OCparam<Integer> CB_MAX_CMDS_LEFT = new OCparam<Integer>(0);
	public static final OCparam<Integer> CB_MAX_OBJECTIVES_PER_PLOT = new OCparam<Integer>(0);
	public static final OCparam<Integer> CB_MAX_TEAMS_PER_PLOT = new OCparam<Integer>(0);
	public static final OCparam<Integer> CB_MIN_TICKS_BETWEEN_EACH_CB_EXECUTION = new OCparam<Integer>(10000);
	
	public static final OCparam<Integer> MAX_ENTITIES_PER_TYPE_PER_PLOT = new OCparam<Integer>(0);
	public static final OCparam<Integer> MAX_TOTAL_ENTITIES_PER_PLOT = new OCparam<Integer>(0);
	
	public static final OCparam<String> WORLD_NAME = new OCparam<String>("");
	public static final OCparam<Location> SPAWN_LOC = new OCparam<Location>(new Location(null, 0, 0, 0));
	
	public static final OCparam<Location> HOLO_HELP_1_LOC = new OCparam<Location>(new Location(null, 0, 0, 0));
	public static final OCparam<Location> HOLO_HELP_2_LOC = new OCparam<Location>(new Location(null, 0, 0, 0));
	public static final OCparam<String> HOLO_HELP_1_TEXT = new OCparam<String>("");
	public static final OCparam<String> HOLO_HELP_2_TEXT = new OCparam<String>("");
	
	public static final OCparam<Integer> INCOME_NOT_AFK = new OCparam<Integer>(0); 
	public static final OCparam<Integer> INCOME_AFK = new OCparam<Integer>(0); 
	
	public static final OCparam<Integer> PLOT_SIZE = new OCparam<Integer>(-1);
	
	private T value;
	private Class<T> paramClass;
	
	@SuppressWarnings("unchecked")
	private OCparam(T value) {
		this.value = value;
		paramClass = (Class<T>) value.getClass();
	}
	
	public T getValue() {
		return value;
	}
	
	@SuppressWarnings("unchecked")
	public void setValueFromBdd(Object value) {
		this.value = (T) value;
	}
	
	public void setValue(T value) {
		this.value = value;
	}
	
	public Class<T> getParamClass(){
		return paramClass;
	}
	
	public static Map<String, OCparam<?>> getValues() {
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
	
	
	@SuppressWarnings("unchecked")
	public static String toJson() {
		JSONObject json = new JSONObject();
		
		for (Entry<String, OCparam<?>> param : getValues().entrySet())
			json.put(param.getKey(), param.getValue().getValue());

		return json.toString();
	}
}
