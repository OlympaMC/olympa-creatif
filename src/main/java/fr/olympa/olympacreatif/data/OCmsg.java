package fr.olympa.olympacreatif.data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fr.olympa.olympacreatif.OlympaCreatifMain;

public class OCmsg {

	public static final OCmsg CB_INVALID_CMD = new OCmsg(null);
	public static final OCmsg CB_NO_COMMANDS_LEFT = new OCmsg(null);
	public static final OCmsg CB_RESULT_FAILED = new OCmsg(null);
	public static final OCmsg CB_RESULT_SUCCESS = new OCmsg(null);
	
	public static final OCmsg COMMAND_HELP = new OCmsg(null);
	public static final OCmsg INSUFFICIENT_PLOT_PERMISSION = new OCmsg(null);
	public static final OCmsg INVALID_PLOT_ID = new OCmsg(null);
	public static final OCmsg MAX_PLOT_COUNT_OWNER_REACHED = new OCmsg(null);
	public static final OCmsg MAX_PLOT_COUNT_REACHED = new OCmsg(null);
	
	public static final OCmsg OCO_COMMAND_HELP = new OCmsg(null);
	public static final OCmsg OCO_EXPORT_FAILED = new OCmsg(null);
	//public static final OCmsg OCO_EXPORT_SUCCESS = new OCmsg(null);
	public static final OCmsg OCO_HAT_SUCCESS = new OCmsg(null);
	public static final OCmsg OCO_HEAD_GIVED = new OCmsg(null);
	public static final OCmsg OCO_SET_FLY_SPEED = new OCmsg(null);
	public static final OCmsg OCO_UNKNOWN_MB = new OCmsg(null);
	
	public static final OCmsg PLAYER_TARGET_OFFLINE = new OCmsg(null);
	public static final OCmsg PLOT_ACCEPTED_INVITATION = new OCmsg(null);
	public static final OCmsg PLOT_BAN_PLAYER = new OCmsg(null);
	public static final OCmsg PLOT_CANT_BUILD = new OCmsg(null);
	public static final OCmsg PLOT_CANT_ENTER_BANNED = new OCmsg(null);
	public static final OCmsg PLOT_CANT_INTERRACT = new OCmsg(null);
	public static final OCmsg PLOT_CANT_INTERRACT_NULL_PLOT = new OCmsg(null);
	public static final OCmsg PLOT_CANT_PRINT_TNT = new OCmsg(null);
	public static final OCmsg PLOT_CANT_UNBAN_PLAYER = new OCmsg(null);
	public static final OCmsg PLOT_CANT_WORLDEDIT = new OCmsg(null);
	public static final OCmsg PLOT_DENY_ITEM_DROP = new OCmsg(null);
	public static final OCmsg PLOT_HAVE_BEEN_BANNED = new OCmsg(null);
	public static final OCmsg PLOT_HAVE_BEEN_KICKED = new OCmsg(null);
	public static final OCmsg PLOT_IMPOSSIBLE_TO_BAN_PLAYER = new OCmsg(null);
	public static final OCmsg PLOT_IMPOSSIBLE_TO_KICK_PLAYER = new OCmsg(null);
	public static final OCmsg PLOT_INSUFFICIENT_MEMBERS_SIZE = new OCmsg(null);
	public static final OCmsg PLOT_INVITATION_TARGET_ALREADY_MEMBER = new OCmsg(null);
	public static final OCmsg PLOT_ITEM_PROHIBITED_USED = new OCmsg(null);
	public static final OCmsg PLOT_JOIN_ERR_NOT_ENOUGH_SLOTS = new OCmsg(null);
	public static final OCmsg PLOT_KICK_PLAYER = new OCmsg(null);
	public static final OCmsg PLOT_NEW_CLAIM = new OCmsg(null);
	public static final OCmsg PLOT_NO_PENDING_INVITATION = new OCmsg(null);
	public static final OCmsg PLOT_RECIEVE_INVITATION = new OCmsg(null);
	public static final OCmsg PLOT_SEND_INVITATION = new OCmsg(null);
	public static final OCmsg PLOT_SPAWN_LOC_SET = new OCmsg(null);
	public static final OCmsg PLOT_UNBAN_PLAYER = new OCmsg(null);
	//public static final OCmsg PROHIBITED_BLOCK_PLACED = new OCmsg(null);
	public static final OCmsg SHOP_BUY_SUCCESS = new OCmsg(null);
	public static final OCmsg TELEPORT_IN_PROGRESS = new OCmsg(null);
	public static final OCmsg TELEPORT_PLOT_CENTER = new OCmsg(null);
	public static final OCmsg TELEPORT_TO_RANDOM_PLOT = new OCmsg(null);
	public static final OCmsg TELEPORTED_TO_PLOT_SPAWN = new OCmsg(null);
	public static final OCmsg WE_ERR_INSUFFICIENT_PERMISSION = new OCmsg(null); 
	public static final OCmsg PLOT_PLAYER_JOIN = new OCmsg(null); 
	public static final OCmsg PLOT_STOPLAG_FIRED = new OCmsg(null); 
	public static final OCmsg PLOT_FORCED_STOPLAG_FIRED = new OCmsg(null); 

	public static final OCmsg INSUFFICIENT_GROUP_PERMISSION = new OCmsg(null); 
	public static final OCmsg WE_ERR_SELECTION_TOO_BIG = new OCmsg(null); 

	public static final OCmsg PERIODIC_INCOME_RECEIVED = new OCmsg(null); 
	public static final OCmsg TELEPORTED_TO_WORLD_SPAWN = new OCmsg(null); 

	public static final OCmsg WE_START_GENERATING_PLOT_SCHEM = new OCmsg(null);
	public static final OCmsg WE_COMPLETE_GENERATING_PLOT_SCHEM = new OCmsg(null); 
	public static final OCmsg WE_DISABLED = new OCmsg(null); 
	public static final OCmsg WE_ERR_SCHEM_CMD_DISABLED = new OCmsg(null);
	public static final OCmsg WE_NO_KIT_FOR_MATERIAL = new OCmsg(null);
	public static final OCmsg WE_DEACTIVATED_FOR_SAFETY = new OCmsg(null); 
 
	//public static final OCmsg WE_ERR_INSUFFICENT_PERMISSION = new OCmsg(null); 

	
	private String message = null;
	
	private OCmsg(String s) {
		message = s;
	}
	
	public String getValue(Object...args) {
		if (message == null)
			return "§cMissing message : " + this.toString().toLowerCase();
		
		String mess = message;
		for (int i = 0 ; i < args.length ; i++)
			mess = mess.replace("&" + (i + 1), args[i].toString());
		
		return mess;
	}
	
	public void setValue(String s) {
		message = StringEscapeUtils.unescapeJava(s);
	}
	
	public OCmsg valueOf(String s) {		
		for (Entry<String, OCmsg> entry : values().entrySet())
			if (entry.getKey().equals(s))
				return entry.getValue();
		
		return null;
	}
	
	@Override
	public String toString() {
		return message;
	}
	
	/**
	 * Return all public static fields of the class
	 * @return
	 */
	public static Map<String, OCmsg> values() {
		Map<String, OCmsg> map = new HashMap<String, OCmsg>();
		
		Field[] fields = OCmsg.class.getDeclaredFields();
		for (Field field : fields)
			if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()))
				try {
					field.setAccessible(true);
					map.put(field.getName(), (OCmsg) field.get(null));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
		
		return map;
	}
}
