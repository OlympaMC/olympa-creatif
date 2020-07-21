package fr.olympa.olympacreatif.data;

import java.util.HashMap;
import java.util.Map;

public enum Message {

	COMMAND_HELP,
	COMMAND_BASIC,

	PLOT_CREATED,
	PLOT_RANK_LOW,
	PLOT_RANK_MEDIUM,
	PLOT_RANK_HIGH,
	PLOT_RANK_OWNER,

	PLOT_CANT_BUILD,
	PLOT_CANT_WORLDEDIT,
	PLOT_CANT_INTERRACT,
	PLOT_CANT_PRINT_TNT,
	PLOT_CANT_ENTER_BANNED,
	PLOT_IMPOSSIBLE_TO_KICK_PLAYER,
	PLOT_HAVE_BEEN_BANNED,
	PLOT_BAN_PLAYER,
	PLOT_IMPOSSIBLE_TO_BAN_PLAYER,
	PLOT_UNBAN_PLAYER,
	PLOT_CANT_UNBAN_PLAYER,
	PLOT_NEW_CLAIM,
	MAX_PLOT_COUNT_REACHED,
	TELEPORT_PLOT_CENTER,

	PLOT_KICK_PLAYER,
	PLOT_HAVE_BEEN_KICKED,

	PROHIBITED_BLOCK_PLACED,
	TELEPORTED_TO_PLOT_SPAWN,
	TELEPORT_IN_PROGRESS,
	TELEPORT_TO_RANDOM_PLOT,
	INVALID_PLOT_ID,

	WE_ACTION_ENDED,
	WE_ACTION_QUEUED,
	WE_ANOTHER_ACTION_ALREADY_QUEUED,

	PARAM_MAX_ENTITIES_PER_TYPE_PER_PLOT,
	PARAM_WORLDEDIT_BPS,
	PARAM_WORLD_NAME,
	PARAM_WE_MAX_QUEUED_ACTIONS_PER_PLAYER,

	PLOT_RECIEVE_INVITATION,
	PLOT_INVITATION_TARGET_ALREADY_MEMBER,
	PLOT_SEND_INVITATION,
	PLAYER_TARGET_OFFLINE,
	INSUFFICIENT_PLOT_PERMISSION,
	PLOT_NULL_PLOT,
	PLOT_ACCEPTED_INVITATION,
	PLOT_NO_PENDING_INVITATION,

	WE_POS_SET,
	WE_INSUFFICIENT_PLOT_PERMISSION,
	WE_INSUFFICIENT_PERMISSION,

	WE_CMD_HELP,
	PARAM_WE_MAX_BLOCKS_PER_CMD,
	WE_ACTION_TOO_BIG,
	WE_CMD_COPY_SUCCESS,
	WE_UNDO_SUCCESS,
	WE_CMD_PASTE_SUCCESS,  
	WE_CUT_SUCCESS,
	WE_CMD_MIROR_SUCCESS,
	WE_CMD_ROTATE_SUCCESS,
	WE_CMD_SET_SUCCESS,
	WE_CMD_PASTE_ERROR,
	WE_CMD_SELECTION_CROSSPLOT,
	WE_TOO_MANY_ACTIONS,
	WE_NO_UNDO_AVAILABLE,

	INSUFFICIENT_KIT_PERMISSION,
	INSUFFICIENT_GROUP_PERMISSION,
	ITEM_INSUFFICIENT_PERMISSION,
	ITEM_PROHIBITED,

	WE_ERR_COPY_INCORRECT_DEGREES,
	WE_ERR_NULL_PLAN,
	WE_ERR_NOT_OWNER_OF_2_PLOTS,
	WE_ERR_SELECTION_TOO_BIG,
	WE_ERR_NULL_CLIPBOARD,
	WE_ERR_PASTE_PART_ON_NULL_TARGET, 
	WE_ERR_SET_INVALID_BLOCKDATA, 
	
	OCO_COMMAND_HELP,
	OCO_BLOCK_GIVED,
	OCO_UNKNOWN_MB, 
	
	OCO_EXPORT_SUCCESS, 
	OCO_EXPORT_FAILED, 
	OCO_HAT_SUCCESS, 
	
	PARAM_MAX_TOTAL_ENTITIES_PER_PLOT, 
	
	MAX_PLOT_COUNT_OWNER_REACHED, 
	PLOT_DENY_ITEM_DROP, 
	WE_ERR_PROTECTED_ZONE_ALREADY_SAVED, 
	WE_ERR_PROTECTED_ZONE_NOT_DEFINED, 
	WE_ERR_PROTECTED_ZONE_EMPTY, 
	WE_CMD_PROTECTED_AREA_CREATION_SUCCESS, 
	PLOT_PROTECTED_ZONE_SAVED, 
	PLOT_PROTECTED_ZONE_RESTORED, 
	OCO_GIVE_INDISPONIBLE_BLOCK, 
	OCO_GIVE_SUCCESSFUL, 
	OCO_INVALID_NBT_DATA, 
	
	WE_ERR_POS_NOT_DEFINED, 
	PLOT_SPAWN_LOC_SET, 
	
	PARAM_CB_MAX_CMDS_LEFT, 
	PARAM_CB_PER_TICK_ADDED_CMDS, 
	
	CB_NO_COMMANDS_LEFT, 
	
	CB_RESULT_SUCCESS, 
	CB_RESULT_FAILED, 
	
	UNKNOWN_MESSAGE, 
	
	PARAM_CB_MAX_TEAMS_PER_PLOT, 
	PARAM_CB_MAX_OBJECTIVES_PER_PLOT, 
	PARAM_CB_MIN_TICKS_BETWEEN_EACH_CB_EXECUTION, 
	PARAM_CB_COMMAND_TICKETS_CONSUMED_BY_SETBLOCK, 
	
	OCO_SET_FLY_SPEED, 
	ERROR_PLOT_NOT_LOADED,
	;
	
	private static Map<Message, String> messagesList = new HashMap<Message, String>();
	
	public String getValue() {
		if (messagesList.containsKey(this))
			return messagesList.get(this).replace("&", "§");
		
		return "§cMissing message : " + this.toString().toLowerCase();
	}
	
	public static void initialize() {
		messagesList.put(PARAM_WORLDEDIT_BPS, "1000");
		messagesList.put(PARAM_WORLD_NAME, "plots");
		messagesList.put(PARAM_WE_MAX_BLOCKS_PER_CMD, "10000");
		messagesList.put(PARAM_WE_MAX_QUEUED_ACTIONS_PER_PLAYER, "4");
		messagesList.put(PARAM_MAX_ENTITIES_PER_TYPE_PER_PLOT, "5");
		messagesList.put(PARAM_MAX_TOTAL_ENTITIES_PER_PLOT, "10");
		
		messagesList.put(PARAM_CB_MAX_CMDS_LEFT, "400");
		messagesList.put(PARAM_CB_PER_TICK_ADDED_CMDS, "10");

		messagesList.put(Message.PARAM_CB_MAX_TEAMS_PER_PLOT, "20");
		messagesList.put(Message.PARAM_CB_MAX_OBJECTIVES_PER_PLOT, "20");
		messagesList.put(Message.PARAM_CB_MIN_TICKS_BETWEEN_EACH_CB_EXECUTION, "5");
		
		messagesList.put(Message.PARAM_CB_COMMAND_TICKETS_CONSUMED_BY_SETBLOCK, "5");
	}
	
	public void setValue(Object s) {
		messagesList.put(this, s.toString());
	}
	
}
