package fr.olympa.olympacreatif.data;

import java.util.HashMap;
import java.util.Map;

public enum Message {

	PLUGIN_PREFIX("plugin_prefix"),
	
	PLOT_CREATED("plot_created"),
	PLOT_RANK_LOW("plot_rank_low"),
	PLOT_RANK_MEDIUM("plot_rank_medium"),
	PLOT_RANK_HIGH("plot_rank_high"),
	PLOT_RANK_OWNER("plot_rank_owner");
	
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
