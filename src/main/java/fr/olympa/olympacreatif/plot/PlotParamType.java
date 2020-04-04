package fr.olympa.olympacreatif.plot;

import java.util.HashMap;
import java.util.Map;

public enum PlotParamType {

	PLOT_TIME("plot_time"),
	
	BANNED_PLAYERS("banned_players"),
	
	SPAWN_LOC("spawn_location"),
	FORCE_SPAWN_LOC("force_spawn_location"),
	
	LIST_PROHIBITED_INTERRACTION("allowed_block_interraction"),
	ALLOW_PRINT_TNT("allow_print_tnt"),
	
	CLEAR_INCOMING_PLAYERS("clear_incoming_players"),
	GAMEMODE_INCOMING_PLAYERS("gamemode_incomings_players"),
	ALLOW_FLY_INCOMING_PLAYERS("flymode_incomings_players"),
	
	;
	
	private String id;
	
	
	private PlotParamType(String s) {
		this.id = s;
	}
	
	public String getId() {
		return id;
	}
	
	public static void load() {
		//TODO
	}
	
	public static PlotParamType getFromString(String s) {
		for (PlotParamType p : PlotParamType.values())
			if (p.getId().equals(s))
				return p;
		
		return null;
	}
}
