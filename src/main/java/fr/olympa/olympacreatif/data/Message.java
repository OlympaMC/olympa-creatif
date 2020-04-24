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
	INVALID_PLID_ID,

	WE_ACTION_ENDED,
	WE_ACTION_QUEUED,
	WE_ANOTHER_ACTION_ALREADY_QUEUED,

	PARAM_MAX_ENTITIES_PER_PLOT,
	PARAM_WORLDEDIT_BPS,
	PARAM_WORLD_NAME,
	PARAM_WORLD_LEVEL,
	PARAM_PLOT_X_SIZE,
	PARAM_PLOT_Z_SIZE,
	PARAM_ROAD_SIZE,
	PARAM_PREFIX,
	PARAM_WE_MAX_QUEUED_ACTIONS_PER_PLAYER,

	GUI_MAIN_MEMBERS_LIST,
	GUI_MAIN_MEMBERS_LIST_LORE,
	GUI_MAIN_PLOT_INFO,
	GUI_MAIN_PLOT_INFO_LORE,
	GUI_MAIN_TELEPORT_PLOT_SPAWN,
	GUI_MAIN_TELEPORT_PLOT_SPAWN_LORE,
	GUI_MAIN_TELEPORT_RANDOM_PLOT,
	GUI_MAIN_TELEPORT_RANDOM_PLOT_LORE,
	GUI_MAIN_INTERACTION_PARAMETERS,
	GUI_MAIN_INTERACTION_PARAMETERS_LORE,
	GUI_MAIN_PLOT_PARAMETERS,
	GUI_MAIN_PLOT_PARAMETERS_LORE,
	GUI_MAIN_PLOTS_LIST,
	GUI_MAIN_PLOTS_LIST_LORE,

	PLOT_RECIEVE_INVITATION,
	PLOT_INVITATION_TARGET_ALREADY_MEMBER,
	PLOT_SEND_INVITATION,
	PLAYER_TARGET_OFFLINE,
	PLOT_INSUFFICIENT_PERMISSION,
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
	WE_CMD_INVALID_SELECTION,
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
	;
	
	private static Map<Message, String> messagesList = new HashMap<Message, String>();
	
	public String getValue() {
		if (messagesList.containsKey(this))
			return messagesList.get(this).replace("&", "§");
		
		return "§cMissing message : " + this.toString().toLowerCase();
	}
	
	public static void initialize() {
		messagesList.put(PARAM_PLOT_X_SIZE, "10");
		messagesList.put(PARAM_PLOT_Z_SIZE, "15");
		messagesList.put(PARAM_ROAD_SIZE, "6");
		messagesList.put(PARAM_PREFIX, "[prefix] ");
		messagesList.put(PARAM_WORLDEDIT_BPS, "1000");
		messagesList.put(PARAM_WORLD_LEVEL, "60");
		messagesList.put(PARAM_WORLD_NAME, "CREATIF");
		messagesList.put(PARAM_WE_MAX_BLOCKS_PER_CMD, "10000");
		messagesList.put(PARAM_WE_MAX_QUEUED_ACTIONS_PER_PLAYER, "4");
		messagesList.put(PARAM_MAX_ENTITIES_PER_PLOT, "100");
	}
	
	public void setValue(Object s) {
		messagesList.put(this, s.toString());
	}
	
}
