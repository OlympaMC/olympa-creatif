package fr.olympa.olympacreatif.data;

import java.util.HashMap;
import java.util.Map;

public enum Message {

	COMMAND_HELP("command_help"),
	COMMAND_BASIC("command_basic"),
	
	PLOT_CREATED("plot_created"),
	PLOT_RANK_LOW("plot_rank_low"),
	PLOT_RANK_MEDIUM("plot_rank_medium"),
	PLOT_RANK_HIGH("plot_rank_high"),
	PLOT_RANK_OWNER("plot_rank_owner"),

	PLOT_CANT_BUILD("plot_cant_build"),
	PLOT_CANT_WORLDEDIT("plot_cant_worldedit"),
	PLOT_CANT_INTERRACT("plot_cant_interract"),
	PLOT_CANT_PRINT_TNT("plot_cant_print_tnt"),
	PLOT_CANT_ENTER_BANNED("plot_cant_enter_banned"),
	PLOT_IMPOSSIBLE_TO_KICK_PLAYER("plot_impossible_to_kick_player"),
	PLOT_HAVE_BEEN_BANNED("plot_have_been_banned"), 
	PLOT_BAN_PLAYER("plot_ban_player"), 
	PLOT_IMPOSSIBLE_TO_BAN_PLAYER("plot_impossible_to_ban_player"),
	PLOT_UNBAN_PLAYER("plot_unabn_player"), 
	PLOT_CANT_UNBAN_PLAYER("plot_cant_unban_player"),
	PLOT_NEW_CLAIM("plot_new_claim"),
	MAX_PLOT_COUNT_REACHED("plot_max_count_reached"),
	
	PLOT_KICK_PLAYER("plot_kick_player"),
	PLOT_HAVE_BEEN_KICKED("plot_have_been_kicked"),
	
	PROHIBITED_BLOCK_PLACED("prohibited_block_placed"), 
	TELEPORTED_TO_PLOT_SPAWN("teleport_to_plot_spawn"),
	TELEPORT_IN_PROGRESS("teleport_in_progress"),
	TELEPORT_TO_RANDOM_PLOT("teleport_to_random_plot"),
	INVALID_PLID_ID("invalid_plot_id"),
	
	WE_ACTION_ENDED("world_edit_action_terminated"),
	WE_ACTION_QUEUED("worldedit_action_queued"),
	WE_ANOTHER_ACTION_ALREADY_QUEUED("worldedit_another_action_already_queued"),
	
	PARAM_WORLDEDIT_BPS("parameter_worldedit_blocks_per_second"),
	PARAM_WORLD_NAME("parameter_world_name"),
	PARAM_WORLD_LEVEL("parameter_world_height"),
	PARAM_PLOT_X_SIZE("parameter_x_plot_size"),
	PARAM_PLOT_Z_SIZE("parameter_z_plot_size"),
	PARAM_ROAD_SIZE("parameter_road_size"),
	PARAM_PREFIX("parameter_chat_prefix"), 
	PARAM_WE_MAX_QUEUED_ACTIONS_PER_PLAYER("we_max_queued_actions_per_player"), 
	
	GUI_MAIN_MEMBERS_LIST("main_gui_members"), 
	GUI_MAIN_MEMBERS_LIST_LORE("main_gui_members_lore"),
	GUI_MAIN_PLOT_INFO("main_gui_plot_info"),
	GUI_MAIN_PLOT_INFO_LORE("main_gui_plot_info_lore"), 
	GUI_MAIN_TELEPORT_PLOT_SPAWN("main_gui_teleport_plot_zone"),
	GUI_MAIN_TELEPORT_PLOT_SPAWN_LORE("main_gui_teleport_plot_zone_lore"), 
	GUI_MAIN_TELEPORT_RANDOM_PLOT("main_gui_teleport_random_plot"), 
	GUI_MAIN_TELEPORT_RANDOM_PLOT_LORE("main_gui_teleport_random_plot_lore"), 
	GUI_MAIN_INTERACTION_PARAMETERS("main_gui_interaction_parameters"), 
	GUI_MAIN_INTERACTION_PARAMETERS_LORE("main_gui_interaction_parameters_lore"), 
	GUI_MAIN_PLOT_PARAMETERS("main_gui_plot_parameters"), 
	GUI_MAIN_PLOT_PARAMETERS_LORE("main_gui_plot_parameters_lore"),
	GUI_MAIN_PLOTS_LIST("main_gui_plots_list"), 
	GUI_MAIN_PLOTS_LIST_LORE("main_gui_plots_list_lore"), 

	PLOT_RECIEVE_INVITATION("plot_recieve_invitation"), 
	PLOT_INVITATION_TARGET_ALREADY_MEMBER("plot_invitation_target_already_member"), 
	PLOT_SEND_INVITATION("plot_send_invitation"), 
	PLAYER_TARGET_OFFLINE("plot_target_offline"), 
	PLOT_INSUFFICIENT_PERMISSION("plot_insufficient_permission"), 
	PLOT_NULL_PLOT("plot_null_plot"), 
	PLOT_ACCEPTED_INVITATION("plot_accept_invitation"), 
	PLOT_NO_PENDING_INVITATION("plot_no_pending_invitation"), 
	
	WE_POS_SET("we_pos_set"), 
	WE_INSUFFICIENT_PLOT_PERMISSION("we_insufficient_plot_permission"), 
	
	WE_CMD_HELP("we_cmd_help"), 
	PARAM_WE_MAX_BLOCKS_PER_CMD("we_max_blocks_per_cmd"), 
	WE_ACTION_TOO_BIG("we_cmd_action_too_big"), 
	WE_CMD_CLIPBOARD_COPIED("we_cmd_clipboard_copied"), 
	WE_CMD_PASTE_ERROR("we_cmd_paste_error"), 
	WE_CMD_CLIPBOARD_MIROR("we_cmd_clipboard_miror"), 
	WE_CMD_CLIPBOARD_ROTATE("we_cmd_clipboard_rotate"), 
	WE_CMD_INVALID_SELECTION("we_cmd_invalid_selection"), 
	WE_TOO_MANY_ACTIONS("we_too_many_queued_actions"), 
	WE_UNDO_QUEUED("we_undo_queued"), 
	WE_NO_UNDO_AVAILABLE("we_no_undo_available"), 
	
	INSUFFICENT_PERMISSION_NEW("insufficient_permission"),   
	ITEM_INSUFFICIENT_PERMISSION("item_insufficient_permission"),
	ITEM_PROHIBITED("otem_prohibited"),
	;
	
	private String id;
	private static Map<String, String> messagesList = new HashMap<String, String>();
	
	private Message(String id) {
		this.id = id;
	}
	
	public String getKey() {
		return id;
	}
	
	public String getValue() {
		if (messagesList.containsKey(getKey()))
			return messagesList.get(getKey()).replace("&", "§");
		
		return "§cMissing message : " + getKey();
	}
	
	public static void initialize() {
		messagesList.put(PARAM_PLOT_X_SIZE.getKey(), "10");
		messagesList.put(PARAM_PLOT_Z_SIZE.getKey(), "15");
		messagesList.put(PARAM_ROAD_SIZE.getKey(), "6");
		messagesList.put(PARAM_PREFIX.getKey(), "[prefix] ");
		messagesList.put(PARAM_WORLDEDIT_BPS.getKey(), "1000");
		messagesList.put(PARAM_WORLD_LEVEL.getKey(), "60");
		messagesList.put(PARAM_WORLD_NAME.getKey(), "CREATIF");
		messagesList.put(PARAM_WE_MAX_BLOCKS_PER_CMD.getKey(), "10000");
		messagesList.put(PARAM_WE_MAX_QUEUED_ACTIONS_PER_PLAYER.getKey(), "4");
	}
	
	public void setValue(Object s) {
		messagesList.put(id, s.toString());
	}
	
}
