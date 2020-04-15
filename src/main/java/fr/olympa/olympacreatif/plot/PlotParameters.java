package fr.olympa.olympacreatif.plot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

import fr.olympa.olympacreatif.OlympaCreatifMain;

public class PlotParameters {

	private OlympaCreatifMain plugin;
	private Map <PlotParamType, Object> parameters = new HashMap<PlotParamType, Object>();
	
	public PlotParameters(OlympaCreatifMain plugin, PlotId id) {
		this.plugin = plugin;
		for (PlotParamType param : PlotParamType.values())
			switch (param) {
			case PLOT_BIOME:
				parameters.put(param, Biome.PLAINS);
			case ALLOW_FLY_INCOMING_PLAYERS:
				parameters.put(param, true);
				break;
			case ALLOW_PRINT_TNT:
				parameters.put(param, false);
				break;
			case BANNED_PLAYERS:
				parameters.put(param, new ArrayList<Long>());
				break;
			case CLEAR_INCOMING_PLAYERS:
				parameters.put(param, false);
				break;
			case FORCE_SPAWN_LOC:
				parameters.put(param, false);
				break;
			case GAMEMODE_INCOMING_PLAYERS:
				parameters.put(param, GameMode.CREATIVE);
				break;
			case PLOT_TIME:
				parameters.put(param, -1);
				break;
			case SPAWN_LOC:
				if (id != null)
					parameters.put(param, id.getLocation());
				else
					parameters.put(param, new Location(plugin.getWorldManager().getWorld(), 0, 100, 0));
				break;
			case LIST_ALLOWED_INTERRACTION:
				parameters.put(param, new ArrayList<Material>());
				break;
			default:
				break;
			
			}
	}

	public void setParameter(PlotParamType param, Object value) {
		parameters.put(param, value);
	}
	
	public Object getParameter(PlotParamType param) {
		if (parameters.containsKey(param))
			return parameters.get(param);
		
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

		list.add(Material.ACACIA_BUTTON);
		list.add(Material.BIRCH_BUTTON);
		list.add(Material.DARK_OAK_BUTTON);
		list.add(Material.JUNGLE_BUTTON);
		list.add(Material.SPRUCE_BUTTON);

		list.add(Material.STONE_BUTTON);

		list.add(Material.REPEATER);
		list.add(Material.COMPARATOR);
		list.add(Material.LEVER);

		list.add(Material.ACACIA_TRAPDOOR);
		list.add(Material.BIRCH_TRAPDOOR);
		list.add(Material.DARK_OAK_TRAPDOOR);
		list.add(Material.JUNGLE_TRAPDOOR);
		list.add(Material.SPRUCE_TRAPDOOR);

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

		list.add(Material.CARTOGRAPHY_TABLE);
		list.add(Material.SMOKER);
		list.add(Material.BLAST_FURNACE);
		list.add(Material.BARREL);
		list.add(Material.LOOM);
		list.add(Material.GRINDSTONE);
		list.add(Material.LECTERN);
		list.add(Material.GRINDSTONE);
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
