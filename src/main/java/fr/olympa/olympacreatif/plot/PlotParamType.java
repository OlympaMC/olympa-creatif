package fr.olympa.olympacreatif.plot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.GameMode;
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
	
	PLOT_BIOME("plot_biome", Biome.PLAINS),
	PLOT_WEATHER("plot_weather", WeatherType.CLEAR),
	GAMEMODE_INCOMING_PLAYERS("gamemode_incomings_players", GameMode.CREATIVE),
	LIST_ALLOWED_INTERRACTION("allowed_block_interraction", new ArrayList<Material>()),
	BANNED_PLAYERS("banned_players", new ArrayList<Long>()),
	PLOT_TIME("plot_time", 6000),
		
	;
	
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
	

	
	public static ArrayList<Material> getAllPossibleAllowedBlocks(){
		ArrayList<Material> list = new ArrayList<Material>();
		list.add(Material.ACACIA_FENCE_GATE);
		list.add(Material.BIRCH_FENCE_GATE);
		list.add(Material.DARK_OAK_FENCE_GATE);
		list.add(Material.JUNGLE_FENCE_GATE);
		list.add(Material.SPRUCE_FENCE_GATE);
		list.add(Material.OAK_FENCE_GATE);
		
		list.add(Material.ACACIA_DOOR);
		list.add(Material.BIRCH_DOOR);
		list.add(Material.DARK_OAK_DOOR);
		list.add(Material.JUNGLE_DOOR);
		list.add(Material.SPRUCE_DOOR);

		list.add(Material.ACACIA_TRAPDOOR);
		list.add(Material.BIRCH_TRAPDOOR);
		list.add(Material.DARK_OAK_TRAPDOOR);
		list.add(Material.JUNGLE_TRAPDOOR);
		list.add(Material.SPRUCE_TRAPDOOR);

		list.add(Material.STONE_BUTTON);
		list.add(Material.ACACIA_BUTTON);
		list.add(Material.BIRCH_BUTTON);
		list.add(Material.DARK_OAK_BUTTON);
		list.add(Material.JUNGLE_BUTTON);
		list.add(Material.SPRUCE_BUTTON);

		list.add(Material.LEVER);
		list.add(Material.REPEATER);
		list.add(Material.COMPARATOR);

		list.add(Material.CHEST);
		list.add(Material.TRAPPED_CHEST);
		list.add(Material.ENDER_CHEST);
		list.add(Material.DISPENSER);
		list.add(Material.DROPPER);
		list.add(Material.HOPPER);
		
		list.add(Material.FURNACE);
		list.add(Material.BREWING_STAND);
		list.add(Material.ENCHANTING_TABLE);
		list.add(Material.ANVIL);
		list.add(Material.CRAFTING_TABLE);

		list.add(Material.CARTOGRAPHY_TABLE);
		list.add(Material.SMOKER);
		list.add(Material.BLAST_FURNACE);
		list.add(Material.BARREL);
		list.add(Material.LOOM);
		list.add(Material.GRINDSTONE);
		list.add(Material.LECTERN);
		list.add(Material.STONECUTTER);
		list.add(Material.BELL);
		
		
		return list;
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
