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

public class PlotParamType<T extends Object> implements Cloneable {
	public static PlotParamType<Boolean> FORCE_SPAWN_LOC = new PlotParamType<Boolean>("FORCE_SPAWN_LOC", false);
	public static PlotParamType<Boolean> CLEAR_INCOMING_PLAYERS = new PlotParamType<Boolean>("CLEAR_INCOMING_PLAYERS", false);
	public static PlotParamType<Boolean> ALLOW_FLY_INCOMING_PLAYERS = new PlotParamType<Boolean>("ALLOW_FLY_INCOMING_PLAYERS", true);
	public static PlotParamType<Boolean> ALLOW_SPLASH_POTIONS = new PlotParamType<Boolean>("ALLOW_SPLASH_POTIONS", true);
	public static PlotParamType<Boolean> ALLOW_PRINT_TNT = new PlotParamType<Boolean>("ALLOW_PRINT_TNT", false);
	public static PlotParamType<Boolean> ALLOW_PVP = new PlotParamType<Boolean>("ALLOW_PVP", false);
	public static PlotParamType<Boolean> ALLOW_PVE = new PlotParamType<Boolean>("ALLOW_PVE", false);
	public static PlotParamType<Boolean> ALLOW_ENVIRONMENT_DAMAGE = new PlotParamType<Boolean>("ALLOW_ENVIRONMENT_DAMAGE", false);
	public static PlotParamType<Boolean> ALLOW_DROP_ITEMS = new PlotParamType<Boolean>("ALLOW_DROP_ITEMS", false);
	public static PlotParamType<Boolean> ALLOW_LAUNCH_PROJECTILES = new PlotParamType<Boolean>("ALLOW_LAUNCH_PROJECTILES", true);
	public static PlotParamType<Boolean> KEEP_MAX_FOOD_LEVEL = new PlotParamType<Boolean>("KEEP_MAX_FOOD_LEVEL", true);
	public static PlotParamType<Boolean> KEEP_INVENTORY_ON_DEATH = new PlotParamType<Boolean>("KEEP_INVENTORY_ON_DEATH", true);
	
	public static PlotParamType<Integer> SPAWN_LOC_X = new PlotParamType<Integer>("SPAWN_LOC_X", 0);
	public static PlotParamType<Integer> SPAWN_LOC_Y = new PlotParamType<Integer>("SPAWN_LOC_Y", 0);
	public static PlotParamType<Integer> SPAWN_LOC_Z = new PlotParamType<Integer>("SPAWN_LOC_Z", 0);
	
	public static PlotParamType<Integer> PLOT_TIME = new PlotParamType<Integer>("PLOT_TIME", 6000);

	public static PlotParamType<String> SONG = new PlotParamType<String>("SONG", "");
	
	//niveaux de stoplag : 0 aucun; 1 activé; 2 activé et bloqué jusqu'à vérif par un staff
	public static PlotParamType<Integer> STOPLAG_STATUS = new PlotParamType<Integer>("STOPLAG_STATUS", 0);
	
	public static PlotParamType<WeatherType> PLOT_WEATHER = new PlotParamType<WeatherType>("PLOT_WEATHER", WeatherType.CLEAR);
	public static PlotParamType<GameMode> GAMEMODE_INCOMING_PLAYERS = new PlotParamType<GameMode>("GAMEMODE_INCOMING_PLAYERS", GameMode.CREATIVE);
	
	public static PlotParamType<List<Material>> LIST_ALLOWED_INTERRACTION = new PlotParamType<List<Material>>("LIST_ALLOWED_INTERRACTION", new ArrayList<Material>());
	public static PlotParamType<List<Long>> BANNED_PLAYERS = new PlotParamType<List<Long>>("BANNED_PLAYERS", new ArrayList<Long>());
	
	
	private static ArrayList<Material> blocksWithInteractionsList = new ArrayList<Material>();
	
	private String id;
	private T defaultValue;
	
	private PlotParamType(String id, T defaultValue) {
		this.id = id;
		this.defaultValue = defaultValue;
	}
	
	public String getId() {
		return id;
	}
	
	public T getDefaultValue() {
		return defaultValue;
	}
	
	public synchronized void setValue(Plot plot, T val) {
		plot.getParameters().setParameter(this, val);
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof PlotParamType && ((PlotParamType<?>)obj).getId().equals(id);
	}
	
	public static List<Material> getAllPossibleIntaractibleBlocks(){
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

	/*public static List<Biome> getAllPossibleBiomes() {
		List<Biome> list = new ArrayList<Biome>();

		list.add(Biome.PLAINS);
		list.add(Biome.NETHER);
		list.add(Biome.THE_END);
		list.add(Biome.MUSHROOM_FIELDS);
		
		return list;
	}*/
}
