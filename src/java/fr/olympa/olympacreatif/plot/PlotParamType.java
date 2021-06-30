package fr.olympa.olympacreatif.plot;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.WeatherType;

import com.google.gson.reflect.TypeToken;

import fr.olympa.olympacreatif.data.Position;

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
	public static PlotParamType<Boolean> ALLOW_VISITORS = new PlotParamType<Boolean>("ALLOW_VISITORS", true);
	
	public static PlotParamType<Position> SPAWN_LOC = new PlotParamType<Position>("SPAWN_LOC", new Position());
	
	public static PlotParamType<Integer> PLOT_TIME = new PlotParamType<Integer>("PLOT_TIME", 6000);

	public static PlotParamType<String> SONG = new PlotParamType<String>("SONG", "");
	
	//niveaux de stoplag : 0 aucun; 1 activé; 2 activé et bloqué jusqu'à vérif par un staff
	public static PlotParamType<Integer> STOPLAG_STATUS = new PlotParamType<Integer>("STOPLAG_STATUS", 0);
	
	public static PlotParamType<WeatherType> PLOT_WEATHER = new PlotParamType<WeatherType>("PLOT_WEATHER", WeatherType.CLEAR);
	public static PlotParamType<GameMode> GAMEMODE_INCOMING_PLAYERS = new PlotParamType<GameMode>("GAMEMODE_INCOMING_PLAYERS", GameMode.CREATIVE);
	
	public static PlotParamType<List<Material>> LIST_ALLOWED_INTERRACTION = new PlotParamType<List<Material>>("LIST_ALLOWED_INTERRACTION", new ArrayList<Material>(), new TypeToken<ArrayList<Material>>(){}.getType());
	public static PlotParamType<List<Long>> BANNED_PLAYERS = new PlotParamType<List<Long>>("BANNED_PLAYERS", new ArrayList<Long>(), new TypeToken<ArrayList<Long>>(){}.getType());

	public static PlotParamType<Boolean> RESET_VISITOR_FLY_SPEED = new PlotParamType<Boolean>("RESET_VISITOR_FLY_SPEED", false);

	public static PlotParamType<Integer> TICK_SPEED = new PlotParamType<Integer>("TICK_SPEED", 4);

	public static PlotParamType<Map<Integer, HologramData>> HOLOS_DATAS = new PlotParamType<Map<Integer,HologramData>>("HOLOS_DATAS", new HashMap<Integer, HologramData>(), new TypeToken<HashMap<Integer, HologramData>>(){}.getType());
	
	
	private static ArrayList<Material> blocksWithInteractionsList = (ArrayList<Material>) Arrays.asList(new Material[] {
			Material.LEVER,

			Material.OAK_FENCE_GATE,
			Material.ACACIA_FENCE_GATE,
			Material.BIRCH_FENCE_GATE,
			Material.DARK_OAK_FENCE_GATE,
			Material.JUNGLE_FENCE_GATE,
			Material.SPRUCE_FENCE_GATE,
			Material.WARPED_FENCE_GATE,
			Material.CRIMSON_FENCE_GATE,

			Material.REPEATER,

			Material.OAK_DOOR,
			Material.ACACIA_DOOR,
			Material.BIRCH_DOOR,
			Material.DARK_OAK_DOOR,
			Material.JUNGLE_DOOR,
			Material.SPRUCE_DOOR,
			Material.WARPED_DOOR,
			Material.CRIMSON_DOOR,

			Material.COMPARATOR,

			Material.OAK_TRAPDOOR,
			Material.ACACIA_TRAPDOOR,
			Material.BIRCH_TRAPDOOR,
			Material.DARK_OAK_TRAPDOOR,
			Material.JUNGLE_TRAPDOOR,
			Material.SPRUCE_TRAPDOOR,
			Material.WARPED_TRAPDOOR,
			Material.CRIMSON_TRAPDOOR,

			Material.STONE_BUTTON,
			Material.POLISHED_BLACKSTONE_BUTTON,
			Material.OAK_BUTTON,
			Material.ACACIA_BUTTON,
			Material.BIRCH_BUTTON,
			Material.DARK_OAK_BUTTON,
			Material.JUNGLE_BUTTON,
			Material.SPRUCE_BUTTON,
			Material.WARPED_BUTTON,
			Material.CRIMSON_BUTTON,

			Material.CHEST,
			Material.TRAPPED_CHEST,
			Material.ENDER_CHEST,
			Material.BARREL,
			Material.DISPENSER,
			Material.DROPPER,
			Material.HOPPER,
			
			Material.BREWING_STAND,
			Material.FURNACE,
			Material.ENCHANTING_TABLE,
			Material.ANVIL,
			Material.CRAFTING_TABLE,
			Material.BELL,
			Material.LECTERN,
	});
	
	private String id;
	private T defaultValue;
	private Type type;

	private PlotParamType(String id, T defaultValue) {
		this(id, defaultValue, defaultValue.getClass());
	}
	
	private PlotParamType(String id, T defaultValue, Type type) {
		this.id = id;
		this.defaultValue = defaultValue;
		this.type = type;
	}
	
	public String getId() {
		return id;
	}
	
	public T getDefaultValue() {
		return defaultValue;
	}
	
	public Type getType() {
		return type;
	}
	
	public synchronized void setValue(Plot plot, T val) {
		plot.getParameters().setParameter(this, val);
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof PlotParamType && ((PlotParamType<?>)obj).getId().equals(id);
	}
	
	public static List<Material> getAllPossibleIntaractibleBlocks() {
		return blocksWithInteractionsList;
		/*if (blocksWithInteractionsList.size() > 0)
			return blocksWithInteractionsList;
		
		blocksWithInteractionsList.add(Material.LEVER);

		blocksWithInteractionsList.add(Material.OAK_FENCE_GATE);
		blocksWithInteractionsList.add(Material.ACACIA_FENCE_GATE);
		blocksWithInteractionsList.add(Material.BIRCH_FENCE_GATE);
		blocksWithInteractionsList.add(Material.DARK_OAK_FENCE_GATE);
		blocksWithInteractionsList.add(Material.JUNGLE_FENCE_GATE);
		blocksWithInteractionsList.add(Material.SPRUCE_FENCE_GATE);
		blocksWithInteractionsList.add(Material.WARPED_FENCE_GATE);
		blocksWithInteractionsList.add(Material.CRIMSON_FENCE_GATE);

		blocksWithInteractionsList.add(Material.REPEATER);

		blocksWithInteractionsList.add(Material.OAK_DOOR);
		blocksWithInteractionsList.add(Material.ACACIA_DOOR);
		blocksWithInteractionsList.add(Material.BIRCH_DOOR);
		blocksWithInteractionsList.add(Material.DARK_OAK_DOOR);
		blocksWithInteractionsList.add(Material.JUNGLE_DOOR);
		blocksWithInteractionsList.add(Material.SPRUCE_DOOR);
		blocksWithInteractionsList.add(Material.WARPED_DOOR);
		blocksWithInteractionsList.add(Material.CRIMSON_DOOR);

		blocksWithInteractionsList.add(Material.COMPARATOR);

		blocksWithInteractionsList.add(Material.OAK_TRAPDOOR);
		blocksWithInteractionsList.add(Material.ACACIA_TRAPDOOR);
		blocksWithInteractionsList.add(Material.BIRCH_TRAPDOOR);
		blocksWithInteractionsList.add(Material.DARK_OAK_TRAPDOOR);
		blocksWithInteractionsList.add(Material.JUNGLE_TRAPDOOR);
		blocksWithInteractionsList.add(Material.SPRUCE_TRAPDOOR);
		blocksWithInteractionsList.add(Material.WARPED_TRAPDOOR);
		blocksWithInteractionsList.add(Material.CRIMSON_TRAPDOOR);

		blocksWithInteractionsList.add(Material.STONE_BUTTON);
		blocksWithInteractionsList.add(Material.POLISHED_BLACKSTONE_BUTTON);
		blocksWithInteractionsList.add(Material.OAK_BUTTON);
		blocksWithInteractionsList.add(Material.ACACIA_BUTTON);
		blocksWithInteractionsList.add(Material.BIRCH_BUTTON);
		blocksWithInteractionsList.add(Material.DARK_OAK_BUTTON);
		blocksWithInteractionsList.add(Material.JUNGLE_BUTTON);
		blocksWithInteractionsList.add(Material.SPRUCE_BUTTON);
		blocksWithInteractionsList.add(Material.WARPED_BUTTON);
		blocksWithInteractionsList.add(Material.CRIMSON_BUTTON);

		blocksWithInteractionsList.add(Material.CHEST);
		blocksWithInteractionsList.add(Material.TRAPPED_CHEST);
		blocksWithInteractionsList.add(Material.ENDER_CHEST);
		blocksWithInteractionsList.add(Material.BARREL);
		blocksWithInteractionsList.add(Material.DISPENSER);
		blocksWithInteractionsList.add(Material.DROPPER);
		blocksWithInteractionsList.add(Material.HOPPER);
		
		blocksWithInteractionsList.add(Material.BREWING_STAND);
		blocksWithInteractionsList.add(Material.FURNACE);
		blocksWithInteractionsList.add(Material.ENCHANTING_TABLE);
		blocksWithInteractionsList.add(Material.ANVIL);
		blocksWithInteractionsList.add(Material.CRAFTING_TABLE);
		blocksWithInteractionsList.add(Material.BELL);
		blocksWithInteractionsList.add(Material.LECTERN);
		
		//blocksWithInteractionsList.add(Material.CARTOGRAPHY_TABLE);
		//blocksWithInteractionsList.add(Material.SMOKER);
		//blocksWithInteractionsList.add(Material.BLAST_FURNACE);
		//
		
		
		return blocksWithInteractionsList;*/
	}
	
	/**
	 * Class used to save holos datas in database
	 *
	 */
	public static class HologramData {
		private int id;
		private List<String> lines;
		private Position bottom;
		
		public HologramData(int id, List<String> lines, Position bottom) {
			this.id = id;
			this.lines = lines;
			this.bottom = bottom;
		}
		public int getId() {
			return id;
		}
		/*public void setId(int id) {
			this.id = id;
		}*/
		public List<String> getLines() {
			return lines;
		}
		public Position getBottom() {
			return bottom;
		}
	}
}
