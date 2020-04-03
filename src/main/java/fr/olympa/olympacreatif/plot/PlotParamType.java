package fr.olympa.olympacreatif.plot;

import java.util.HashMap;
import java.util.Map;

public enum PlotParamType {

	PLOT_TIME("plot_time"),
	
	CUSTOM_NAME("custom_name"),
	
	BANNED_PLAYERS("banned_players"),
	
	SPAWN_LOC("spawn_location"),
	FORCE_SPAWN_LOC("force_spawn_location"),
	
	CLEAR_INCOMING_PLAYERS("clear_incoming_players"),
	GAMEMODE_INCOMING_PLAYERS("gamemode_incomings_players"),
	ALLOW_FLY_INCOMING_PLAYERS("flymode_incomings_players"),
	
	INTERRACT_SPRUCE_DOOR("interract_spruce_door"),
	INTERRACT_BIRCH_DOOR("interract_birch_door"),
	INTERRACT_JUNGLE_DOOR("interract_jungle_door"),
	INTERRACT_ACACIA_DOOR("interract_acacia_door"),
	INTERRACT_DARKOAK_DOOR("interract_darkoak_door"),
	INTERRACT_OAK_DOOR("interract_oak_door"),
	INTERRACT_TRAPDOOR("interract_trapdoor"),

	INTERRACT_SPRUCE_FENCE("interract_spruce_fence"),
	INTERRACT_BIRCH_FENCE("interract_birch_fence"),
	INTERRACT_JUNGLE_FENCE("interract_jungle_fence"),
	INTERRACT_ACACIA_FENCE("interract_acacia_fence"),
	INTERRACT_DARKOAK_FENCE("interract_darkoak_fence"),
	INTERRACT_OAK_FENCE("interract_oak_fence"),

	INTERRACT_CHEST("interract_chest"),
	INTERRACT_TRAPPED_CHEST("interract_trapped_chest"),
	INTERRACT_ENDER_CHEST("interract_ender_chest"),
	INTERRACT_DISPENSER("interract_dispenser"),
	INTERRACT_DROPPER("interract_dropper"),

	INTERRACT_FURNACE("interract_furnace"),
	INTERRACT_CRAFTING_TABLE("interract_crafting_table"),
	INTERRACT_ANVIL("interract_anvil"),
	INTERRACT_ENCHANTMENT_TABLE("interract_enchantment_table"),
	INTERRACT_BREWING_STAND("interract_brewing_stand"),

	INTERRACT_REDSTONE_COMPARATOR("interract_redstone_comparator"),
	INTERRACT_REDSTONE_REPEATER("interract_redstone_repeator"),
	INTERRACT_WOOD_BUTTON("interract_wood_button"),
	INTERRACT_STONE_BUTTON("interract_stone_button"),
	INTERRACT_LEVER("interract_lever"),
	
	INTERRACT_TNT("interract_tnt")
	
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
