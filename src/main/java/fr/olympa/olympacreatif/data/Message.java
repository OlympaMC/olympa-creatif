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
	
	PLOT_NEW_CLAIM("plot_new_claim"),
	MAX_PLOT_COUNT_REACHED("plot_max_count_reached"),
	
	PLOT_KICK_PLAYER("plot_kick_player"),
	PLOT_HAVE_BEEN_KICKED("plot_have_been_kicked"),
	
	PROHIBITED_BLOCK_PLACED("prohibited_block_placed"), 
	TELEPORTED_TO_PLOT_SPAWN("teleport_to_plot_spawn"),
	TELEPORT_IN_PROGRESS("teleport_in_progress"),
	INVALID_PLID_ID("invalid_plot_id"),
	
	WE_ACTION_ENDED("world_edit_action_terminated"),
	WE_ACTION_QUEUED("worldedit_action_queued"),
	WE_ANOTHER_ACTION_ALREADY_QUEUED("worldedit_another_action_already_queued"),
	WE_NOTHING_TO_DO("worldedit_invalid_selection"),
	
	PARAM_WORLDEDIT_BPS("parameter_worldedit_blocks_per_second"),
	PARAM_WORLD_NAME("parameter_world_name"),
	PARAM_WORLD_LEVEL("parameter_world_height"),
	PARAM_PLOT_X_SIZE("parameter_x_plot_size"),
	PARAM_PLOT_Z_SIZE("parameter_z_plot_size"),
	PARAM_ROAD_SIZE("parameter_road_size"),
	PARAM_PREFIX("parameter_chat_prefix"), 
	
	GUI_MAIN_NAME("main_gui_name"),
	GUI_MAIN_MEMBERS_LIST("main_gui_members"), 
	GUI_MAIN_MEMBERS_LIST_LORE("main_gui_members_lore"),
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
			return messagesList.get(getKey());
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
	}
	
	public void setValue(Object s) {
		messagesList.put(id, s.toString());
	}
	
}
