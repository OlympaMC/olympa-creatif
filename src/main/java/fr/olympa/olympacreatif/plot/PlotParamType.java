package fr.olympa.olympacreatif.plot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.block.Biome;

public enum PlotParamType {
	
	SPAWN_LOC("spawn_location", null),
	FORCE_SPAWN_LOC("force_spawn_location", false),
	
	CLEAR_INCOMING_PLAYERS("clear_incoming_players", false),
	
	ALLOW_FLY_INCOMING_PLAYERS("flymode_incomings_players", true),
	ALLOW_SPLASH_POTIONS("allow_splash_potions", false),
	ALLOW_PRINT_TNT("allow_print_tnt", false),
	ALLOW_PVP("allow_pvp", false),
	ALLOW_ENVIRONMENT_DAMAGE("allow_environment_damage", false),
	ALLOW_DROP_ITEMS("allow_drop_items", false),
	KEEP_MAX_FOOD_LEVEL("keep_max_food_level", true),
	
	PLOT_BIOME("plot_biome", Biome.PLAINS),
	PLOT_WEATHER("plot_weather", WeatherType.CLEAR),
	GAMEMODE_INCOMING_PLAYERS("gamemode_incomings_players", GameMode.CREATIVE),
	LIST_ALLOWED_INTERRACTION("allowed_block_interraction", new ArrayList<Material>()),
	BANNED_PLAYERS("banned_players", new ArrayList<Long>()),
	PLOT_TIME("plot_time", 6000),

	PROTECTED_ZONE_POS1("protected_zone_pos1", null),
	PROTECTED_ZONE_POS2("protected_zone_pos2", null),
	;
	
	private static ArrayList<Material> blocksWithInteractionsList = new ArrayList<Material>();
	
	private String id;
	private Object defaultValue;
	
	private PlotParamType(String s, Object defaultValue) {
		this.id = s;
		this.defaultValue = defaultValue;
	}
	
	public String getId() {
		return id;
	}
	
	public Object getDefaultValue() {
		return defaultValue;
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
	
	public static ArrayList<Material> getAllPossibleBlocksWithInteractions(){
		if (blocksWithInteractionsList.size() > 0)
			return blocksWithInteractionsList;
		
		blocksWithInteractionsList.add(Material.ACACIA_FENCE_GATE);
		blocksWithInteractionsList.add(Material.BIRCH_FENCE_GATE);
		blocksWithInteractionsList.add(Material.DARK_OAK_FENCE_GATE);
		blocksWithInteractionsList.add(Material.JUNGLE_FENCE_GATE);
		blocksWithInteractionsList.add(Material.SPRUCE_FENCE_GATE);
		blocksWithInteractionsList.add(Material.OAK_FENCE_GATE);
		
		blocksWithInteractionsList.add(Material.ACACIA_DOOR);
		blocksWithInteractionsList.add(Material.BIRCH_DOOR);
		blocksWithInteractionsList.add(Material.DARK_OAK_DOOR);
		blocksWithInteractionsList.add(Material.JUNGLE_DOOR);
		blocksWithInteractionsList.add(Material.SPRUCE_DOOR);

		blocksWithInteractionsList.add(Material.ACACIA_TRAPDOOR);
		blocksWithInteractionsList.add(Material.BIRCH_TRAPDOOR);
		blocksWithInteractionsList.add(Material.DARK_OAK_TRAPDOOR);
		blocksWithInteractionsList.add(Material.JUNGLE_TRAPDOOR);
		blocksWithInteractionsList.add(Material.SPRUCE_TRAPDOOR);

		blocksWithInteractionsList.add(Material.STONE_BUTTON);
		blocksWithInteractionsList.add(Material.ACACIA_BUTTON);
		blocksWithInteractionsList.add(Material.BIRCH_BUTTON);
		blocksWithInteractionsList.add(Material.DARK_OAK_BUTTON);
		blocksWithInteractionsList.add(Material.JUNGLE_BUTTON);
		blocksWithInteractionsList.add(Material.SPRUCE_BUTTON);

		blocksWithInteractionsList.add(Material.LEVER);
		blocksWithInteractionsList.add(Material.REPEATER);
		blocksWithInteractionsList.add(Material.COMPARATOR);

		blocksWithInteractionsList.add(Material.CHEST);
		blocksWithInteractionsList.add(Material.TRAPPED_CHEST);
		blocksWithInteractionsList.add(Material.ENDER_CHEST);
		blocksWithInteractionsList.add(Material.DISPENSER);
		blocksWithInteractionsList.add(Material.DROPPER);
		blocksWithInteractionsList.add(Material.HOPPER);
		
		blocksWithInteractionsList.add(Material.FURNACE);
		blocksWithInteractionsList.add(Material.BREWING_STAND);
		blocksWithInteractionsList.add(Material.ENCHANTING_TABLE);
		blocksWithInteractionsList.add(Material.ANVIL);
		blocksWithInteractionsList.add(Material.CRAFTING_TABLE);

		blocksWithInteractionsList.add(Material.CARTOGRAPHY_TABLE);
		blocksWithInteractionsList.add(Material.SMOKER);
		blocksWithInteractionsList.add(Material.BLAST_FURNACE);
		blocksWithInteractionsList.add(Material.BARREL);
		blocksWithInteractionsList.add(Material.LOOM);
		blocksWithInteractionsList.add(Material.GRINDSTONE);
		blocksWithInteractionsList.add(Material.LECTERN);
		blocksWithInteractionsList.add(Material.STONECUTTER);
		blocksWithInteractionsList.add(Material.BELL);
		
		return blocksWithInteractionsList;
	}

	public static List<Biome> getAllPossibleBiomes() {
		List<Biome> list = new ArrayList<Biome>();

		list.add(Biome.PLAINS);
		list.add(Biome.NETHER);
		list.add(Biome.THE_END);
		list.add(Biome.MUSHROOM_FIELDS);
		
		return list;
	}
}
