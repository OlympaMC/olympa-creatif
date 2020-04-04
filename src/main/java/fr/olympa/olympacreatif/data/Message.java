package fr.olympa.olympacreatif.data;

import java.util.HashMap;
import java.util.Map;

public enum Message {

	PLUGIN_PREFIX("plugin_prefix"),
	
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
	
	PLOT_KICK_PLAYER("plot_kick_player"),
	PLOT_HAVE_BEEN_KICKED("plot_have_been_kicked"),
	
	PROHIBITED_BLOCK_PLACED("prohibited_block_placed"), 
	TELEPORTED_TO_PLOT_SPAWN("teleport_to_plot_spawn"),
	
	;
	
	private String id;
	private static Map<String, String> messagesList = new HashMap<String, String>();
	
	private Message(String id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return id;
	}
	
	public String getMessage() {
		return messagesList.get(toString());
	}
	
	public static void initialize() {
		//TODO
	}
	
}
