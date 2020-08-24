package fr.olympa.olympacreatif.plot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.block.Biome;

import fr.olympa.olympacreatif.OlympaCreatifMain;

public enum PlotParamType {
	
	FORCE_SPAWN_LOC(Boolean.class, false),
	
	CLEAR_INCOMING_PLAYERS(Boolean.class, false),
	
	ALLOW_FLY_INCOMING_PLAYERS(Boolean.class, true),
	ALLOW_SPLASH_POTIONS(Boolean.class, false),
	ALLOW_PRINT_TNT(Boolean.class, false),
	ALLOW_PVP(Boolean.class, false),
	ALLOW_PVE(Boolean.class, false),
	ALLOW_ENVIRONMENT_DAMAGE(Boolean.class, false),
	ALLOW_DROP_ITEMS(Boolean.class, false),
	ALLOW_LAUNCH_PROJECTILES(Boolean.class, false),
	
	KEEP_MAX_FOOD_LEVEL(Boolean.class, true),
	KEEP_INVENTORY_ON_DEATH(Boolean.class, false),

	SPAWN_LOC_X(Integer.class, 0),
	SPAWN_LOC_Y(Integer.class, 0),
	SPAWN_LOC_Z(Integer.class, 0),
	
	PLOT_WEATHER(WeatherType.class, WeatherType.CLEAR),
	GAMEMODE_INCOMING_PLAYERS(GameMode.class, GameMode.CREATIVE),
	
	LIST_ALLOWED_INTERRACTION(Set.class, new HashSet<Material>()),
	BANNED_PLAYERS(Set.class, new HashSet<Long>()),
	
	PLOT_TIME(Integer.class, 6000),
	//niveaux de stoplag : 0 aucun, 1 activé, 2 activé et bloqué jusqu'à vérif par un staff
	STOPLAG_STATUS(Integer.class, 0),
	;
	
	private static ArrayList<Material> blocksWithInteractionsList = new ArrayList<Material>();
	
	private Class<?> paramType;
	private Object defaultValue;
	
	private PlotParamType(Class<?> type, Object defaultValue) {
		this.paramType = type;
		this.defaultValue = defaultValue;
	}
	
	public Class getType() {
		return paramType;
	}
	
	public Object getDefaultValue() {
		return defaultValue;
	}
	
	public static void load() {
		//TODO
	}
	
	/*
	public static PlotParamType getFromString(String s) {
		for (PlotParamType p : PlotParamType.values())
			if (p.getId().equals(s))
				return p;
		
		return null;
	}
	*/
	
	public static ArrayList<Material> getAllPossibleIntaractibleBlocks(){
		if (blocksWithInteractionsList.size() > 0)
			return blocksWithInteractionsList;

		blocksWithInteractionsList.add(Material.OAK_FENCE_GATE);
		blocksWithInteractionsList.add(Material.ACACIA_FENCE_GATE);
		blocksWithInteractionsList.add(Material.BIRCH_FENCE_GATE);
		blocksWithInteractionsList.add(Material.DARK_OAK_FENCE_GATE);
		blocksWithInteractionsList.add(Material.JUNGLE_FENCE_GATE);
		blocksWithInteractionsList.add(Material.SPRUCE_FENCE_GATE);

		blocksWithInteractionsList.add(Material.OAK_DOOR);
		blocksWithInteractionsList.add(Material.ACACIA_DOOR);
		blocksWithInteractionsList.add(Material.BIRCH_DOOR);
		blocksWithInteractionsList.add(Material.DARK_OAK_DOOR);
		blocksWithInteractionsList.add(Material.JUNGLE_DOOR);
		blocksWithInteractionsList.add(Material.SPRUCE_DOOR);

		blocksWithInteractionsList.add(Material.OAK_TRAPDOOR);
		blocksWithInteractionsList.add(Material.ACACIA_TRAPDOOR);
		blocksWithInteractionsList.add(Material.BIRCH_TRAPDOOR);
		blocksWithInteractionsList.add(Material.DARK_OAK_TRAPDOOR);
		blocksWithInteractionsList.add(Material.JUNGLE_TRAPDOOR);
		blocksWithInteractionsList.add(Material.SPRUCE_TRAPDOOR);

		blocksWithInteractionsList.add(Material.OAK_BUTTON);
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
		blocksWithInteractionsList.add(Material.LECTERN);
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
