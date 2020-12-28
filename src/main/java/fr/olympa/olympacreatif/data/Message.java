package fr.olympa.olympacreatif.data;

import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public enum Message {

	CB_INVALID_CMD,
	CB_NO_COMMANDS_LEFT,
	CB_RESULT_FAILED,
	CB_RESULT_SUCCESS,
	COMMAND_HELP,
	INSUFFICIENT_PLOT_PERMISSION,
	INVALID_PLOT_ID,
	MAX_PLOT_COUNT_OWNER_REACHED,
	MAX_PLOT_COUNT_REACHED,
	OCO_COMMAND_HELP,
	OCO_EXPORT_FAILED,
	OCO_EXPORT_SUCCESS,
	OCO_HAT_SUCCESS,
	OCO_HEAD_GIVED,
	OCO_SET_FLY_SPEED,
	OCO_UNKNOWN_MB,
	PARAM_CB_COMMAND_TICKETS_CONSUMED_BY_SETBLOCK,
	PARAM_CB_MAX_CMDS_LEFT,
	PARAM_CB_MAX_OBJECTIVES_PER_PLOT,
	PARAM_CB_MAX_TEAMS_PER_PLOT,
	PARAM_CB_MIN_TICKS_BETWEEN_EACH_CB_EXECUTION,
	PARAM_MAX_ENTITIES_PER_TYPE_PER_PLOT,
	PARAM_MAX_TOTAL_ENTITIES_PER_PLOT,
	PARAM_WORLD_NAME,
	PARAM_HOLO_HELP_LOC_1,
	PARAM_HOLO_HELP_LOC_2,
	PARAM_SPAWN_LOC,
	PLAYER_TARGET_OFFLINE,
	PLOT_ACCEPTED_INVITATION,
	PLOT_BAN_PLAYER,
	PLOT_CANT_BUILD,
	PLOT_CANT_ENTER_BANNED,
	PLOT_CANT_INTERRACT,
	PLOT_CANT_INTERRACT_NULL_PLOT,
	PLOT_CANT_PRINT_TNT,
	PLOT_CANT_UNBAN_PLAYER,
	PLOT_CANT_WORLDEDIT,
	PLOT_DENY_ITEM_DROP,
	PLOT_HAVE_BEEN_BANNED,
	PLOT_HAVE_BEEN_KICKED,
	PLOT_IMPOSSIBLE_TO_BAN_PLAYER,
	PLOT_IMPOSSIBLE_TO_KICK_PLAYER,
	PLOT_INSUFFICIENT_MEMBERS_SIZE,
	PLOT_INVITATION_TARGET_ALREADY_MEMBER,
	PLOT_ITEM_PROHIBITED_USED,
	PLOT_JOIN_ERR_NOT_ENOUGH_SLOTS,
	PLOT_KICK_PLAYER,
	PLOT_NEW_CLAIM,
	PLOT_NO_PENDING_INVITATION,
	PLOT_RECIEVE_INVITATION,
	PLOT_SEND_INVITATION,
	PLOT_SPAWN_LOC_SET,
	PLOT_UNBAN_PLAYER,
	PROHIBITED_BLOCK_PLACED,
	SHOP_BUY_SUCCESS,
	TELEPORT_IN_PROGRESS,
	TELEPORT_PLOT_CENTER,
	TELEPORT_TO_RANDOM_PLOT,
	TELEPORTED_TO_PLOT_SPAWN,
	WE_ERR_INSUFFICIENT_PERMISSION, 
	PLOT_PLAYER_JOIN, 
	PLOT_STOPLAG_FIRED, 
	PLOT_FORCED_STOPLAG_FIRED, 
	
	INSUFFICIENT_GROUP_PERMISSION, 
	WE_ERR_SELECTION_TOO_BIG, 
	
	PERIODIC_INCOME_RECEIVED, 
	PARAM_INCOME_NOT_AFK, 
	PARAM_INCOME_AFK, 
	TELEPORTED_TO_WORLD_SPAWN, 

	WE_START_GENERATING_PLOT_SCHEM,
	WE_COMPLETE_GENERATING_PLOT_SCHEM, 
	WE_DISABLED, 
	WE_ERR_SCHEM_CMD_DISABLED, 
	
	WE_ERR_INSUFFICENT_PERMISSION, 
	
	PARAM_TUTO_HOLO_LOC, 
	PARAM_TUTO_HOLO_LINES,
	;
	
	private String message = "";
	
	public String getValue(Object...args) {
		if (message.equals(""))
			return "§cMissing message : " + this.toString().toLowerCase();
		
		String mess = message;
		for (int i = 0 ; i < args.length ; i++)
			mess = mess.replace("&" + (i + 1), args[i].toString());
		
		return mess;
	}
	
	public void setValue(String s) {
		message = StringEscapeUtils.unescapeJava(s);
	}

	public static Location getLocFromMessage(Message msg) {
		try{
			return new Location(Bukkit.getWorld(Message.PARAM_WORLD_NAME.getValue()), 
					Double.valueOf(msg.getValue().split(" ")[0]), 
					Double.valueOf(msg.getValue().split(" ")[1]), 
					Double.valueOf(msg.getValue().split(" ")[2]), 
					Integer.valueOf(msg.getValue().split(" ")[3]), 
					Integer.valueOf(msg.getValue().split(" ")[4]));
		}catch(NumberFormatException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
