package fr.olympa.olympacreatif.plot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.DatabaseSerializable;

public class PlotParameters implements DatabaseSerializable{

	private OlympaCreatifMain plugin;
	private Map <PlotParamType, Object> parameters = new HashMap<PlotParamType, Object>();
	
	@SuppressWarnings("unchecked")
	public PlotParameters(OlympaCreatifMain plugin, PlotArea area, boolean useDefaultProhibitedBlocks) {
		this.plugin = plugin;
		for (PlotParamType param : PlotParamType.values())
			switch (param) {
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
				if (area != null)
					parameters.put(param, area.getFirstCorner());
				else
					parameters.put(param, new Location(plugin.getWorldManager().getWorld(), 0, 100, 0));
				break;
			case LIST_PROHIBITED_INTERRACTION:
				parameters.put(param, new ArrayList<Material>());
				if (useDefaultProhibitedBlocks)
					((ArrayList<Material>) parameters.get(param)).addAll(getAllPossibleProhibitedBlocks());
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
	
	@SuppressWarnings("unchecked")
	@Override
	public String toDbFormat() {
		String s = "";
		for (Entry<PlotParamType, Object> e : parameters.entrySet())
			switch (e.getKey()) {
			case ALLOW_FLY_INCOMING_PLAYERS:
				s += e.getKey().getId() + "=" + e.getValue().toString() + " ";
				break;
			case ALLOW_PRINT_TNT:
				s += e.getKey().getId() + "=" + e.getValue().toString() + " ";
				break;
			case BANNED_PLAYERS:
				s += e.getKey().getId() + "=" ;
				for (int i = 0 ; i < ((ArrayList<Long>) e.getValue()).size()-1 ;i++)
					s += ((ArrayList<Long>) e.getValue()).get(i).toString() + ",";
					
				if (((ArrayList<Long>) e.getValue()).size() > 0)
					s += ((ArrayList<Long>) e.getValue()).get(((ArrayList<Long>) e.getValue()).size()-1);
				s += " ";
				break;
			case CLEAR_INCOMING_PLAYERS:
				s += e.getKey().getId() + "=" + e.getValue().toString() + " ";
				break;
			case FORCE_SPAWN_LOC:
				s += e.getKey().getId() + "=" + e.getValue().toString() + " ";
				break;
			case GAMEMODE_INCOMING_PLAYERS:
				s += e.getKey().getId() + "=" + e.getValue().toString() + " ";
				break;
			case LIST_PROHIBITED_INTERRACTION:
				s += e.getKey().getId() + "=" ;
				for (int i = 0 ; i < ((ArrayList<Material>) e.getValue()).size()-1 ;i++)
					s += ((ArrayList<Material>) e.getValue()).get(i).toString() + ",";
					
				if (((ArrayList<Material>) e.getValue()).size() > 0)
					s += ((ArrayList<Material>) e.getValue()).get(((ArrayList<Material>) e.getValue()).size()-1);
				s += " ";
				break;
			case PLOT_TIME:
				s += e.getKey().getId() + "=" + e.getValue().toString() + " ";
				break;
			case SPAWN_LOC:
				s += e.getKey().getId() + "=" + ((Location) e.getValue()).getX() + "," + ((Location) e.getValue()).getY() + "," + ((Location) e.getValue()).getZ() + " ";
				break;
			default:
				break;
			
			}
		
		return s;
	}

	@SuppressWarnings("unchecked")
	public static PlotParameters fromDbFormat(OlympaCreatifMain plugin, String data) {
		PlotParameters paramsSet = new PlotParameters(plugin, null, false);
		
		for (String s : data.split(" "))
			if (data.contains("=")) {
				PlotParamType param = PlotParamType.getFromString(s.split("=")[0]);
				String value = s.split("=")[1];
				
				switch (param) {
				case ALLOW_FLY_INCOMING_PLAYERS:
					paramsSet.setParameter(param, Boolean.valueOf(value));
					break;
				case ALLOW_PRINT_TNT:
					paramsSet.setParameter(param, Boolean.valueOf(value));
					break;
				case BANNED_PLAYERS:
					for (String st : value.split(","))
						((ArrayList<Long>) paramsSet.getParameter(param)).add(Long.valueOf(st));
					break;
				case CLEAR_INCOMING_PLAYERS:
					paramsSet.setParameter(param, Boolean.valueOf(value));
					break;
				case FORCE_SPAWN_LOC:
					paramsSet.setParameter(param, Boolean.valueOf(value));
					break;
				case GAMEMODE_INCOMING_PLAYERS:
					paramsSet.setParameter(param, GameMode.valueOf(value));
					break;
				case LIST_PROHIBITED_INTERRACTION:
					for (String st : value.split(","))
						((ArrayList<Material>) paramsSet.getParameter(param)).add(Material.valueOf(st));
					break;
				case PLOT_TIME:
					paramsSet.setParameter(param, Long.valueOf(value));
					break;
				case SPAWN_LOC:
					paramsSet.setParameter(param, new Location(plugin.getWorldManager().getWorld(),
							Double.valueOf(value.split(",")[0]), Double.valueOf(value.split(",")[1]), Double.valueOf(value.split(",")[2])));
					break;
				default:
					break;
				}
			}

		return paramsSet;
	}
	
	public static ArrayList<Material> getAllPossibleProhibitedBlocks(){
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
		
		list.add(Material.FURNACE);
		list.add(Material.CRAFTING_TABLE);
		list.add(Material.BREWING_STAND);
		list.add(Material.ENCHANTING_TABLE);
		list.add(Material.ANVIL);
		
		return list;
	}

}
