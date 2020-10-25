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

public class PlotParamTypeBIS<T extends Object> implements Cloneable {
	public static PlotParamTypeBIS<Boolean> FORCE_SPAWN_LOC = new PlotParamTypeBIS<Boolean>("FORCE_SPAWN_LOC", false);
	public static PlotParamTypeBIS<Boolean> CLEAR_INCOMING_PLAYERS = new PlotParamTypeBIS<Boolean>("CLEAR_INCOMING_PLAYERS", false);
	public static PlotParamTypeBIS<Boolean> ALLOW_FLY_INCOMING_PLAYERS = new PlotParamTypeBIS<Boolean>("ALLOW_FLY_INCOMING_PLAYERS", true);
	public static PlotParamTypeBIS<Boolean> ALLOW_SPLASH_POTIONS = new PlotParamTypeBIS<Boolean>("ALLOW_SPLASH_POTIONS", false);
	public static PlotParamTypeBIS<Boolean> ALLOW_PRINT_TNT = new PlotParamTypeBIS<Boolean>("ALLOW_PRINT_TNT", false);
	public static PlotParamTypeBIS<Boolean> ALLOW_PVP = new PlotParamTypeBIS<Boolean>("ALLOW_PVP", false);
	public static PlotParamTypeBIS<Boolean> ALLOW_PVE = new PlotParamTypeBIS<Boolean>("ALLOW_PVE", false);
	public static PlotParamTypeBIS<Boolean> ALLOW_ENVIRONMENT_DAMAGE = new PlotParamTypeBIS<Boolean>("ALLOW_ENVIRONMENT_DAMAGE", false);
	public static PlotParamTypeBIS<Boolean> ALLOW_DROP_ITEMS = new PlotParamTypeBIS<Boolean>("ALLOW_DROP_ITEMS", false);
	public static PlotParamTypeBIS<Boolean> ALLOW_LAUNCH_PROJECTILES = new PlotParamTypeBIS<Boolean>("ALLOW_LAUNCH_PROJECTILES", false);
	public static PlotParamTypeBIS<Boolean> KEEP_MAX_FOOD_LEVEL = new PlotParamTypeBIS<Boolean>("KEEP_MAX_FOOD_LEVEL", true);
	public static PlotParamTypeBIS<Boolean> KEEP_INVENTORY_ON_DEATH = new PlotParamTypeBIS<Boolean>("KEEP_INVENTORY_ON_DEATH", false);
	
	public static PlotParamTypeBIS<Integer> SPAWN_LOC_X = new PlotParamTypeBIS<Integer>("SPAWN_LOC_X", 0);
	public static PlotParamTypeBIS<Integer> SPAWN_LOC_Y = new PlotParamTypeBIS<Integer>("SPAWN_LOC_Y", 0);
	public static PlotParamTypeBIS<Integer> SPAWN_LOC_Z = new PlotParamTypeBIS<Integer>("SPAWN_LOC_Z", 0);
	
	public static PlotParamTypeBIS<Integer> PLOT_TIME = new PlotParamTypeBIS<Integer>("PLOT_TIME", 6000);
	
	//niveaux de stoplag : 0 aucun; 1 activé; 2 activé et bloqué jusqu'à vérif par un staff
	public static PlotParamTypeBIS<Integer> STOPLAG_STATUS = new PlotParamTypeBIS<Integer>("STOPLAG_STATUS", 0);
	
	public static PlotParamTypeBIS<WeatherType> PLOT_WEATHER = new PlotParamTypeBIS<WeatherType>("PLOT_WEATHER", WeatherType.CLEAR);
	public static PlotParamTypeBIS<GameMode> GAMEMODE_INCOMING_PLAYERS = new PlotParamTypeBIS<GameMode>("GAMEMODE_INCOMING_PLAYERS", GameMode.CREATIVE);
	
	public static PlotParamTypeBIS<List<Material>> LIST_ALLOWED_INTERRACTION = new PlotParamTypeBIS<List<Material>>("LIST_ALLOWED_INTERRACTION", new ArrayList<Material>());
	public static PlotParamTypeBIS<List<Long>> BANNED_PLAYERS = new PlotParamTypeBIS<List<Long>>("BANNED_PLAYERS", new ArrayList<Long>());
	
	private static ArrayList<Material> blocksWithInteractionsList = new ArrayList<Material>();
	
	private String id;
	private T defaultValue;
	
	private PlotParamTypeBIS(String id, T defaultValue) {
		this.id = id;
		this.defaultValue = defaultValue;
	}
	
	public String getId() {
		return id;
	}
	
	public T getDefaultValue() {
		return defaultValue;
	}
	
	public void setValue(Plot plot, T val) {
		plot.getParameters().setParameter(this, val);
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof PlotParamTypeBIS && ((PlotParamTypeBIS<?>)obj).getId().equals(id);
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

	public static List<Biome> getAllPossibleBiomes() {
		List<Biome> list = new ArrayList<Biome>();

		list.add(Biome.PLAINS);
		list.add(Biome.NETHER);
		list.add(Biome.THE_END);
		list.add(Biome.MUSHROOM_FIELDS);
		
		return list;
	}
}
