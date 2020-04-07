package fr.olympa.olympacreatif.worldedit;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

import fr.olympa.olympacreatif.OlympaCreatifMain;

public class Undo {

	private OlympaCreatifMain plugin;
	private Map<Location, BlockData> undoData = new HashMap<Location, BlockData>();
	
	public Undo(OlympaCreatifMain plugin) {
		this.plugin = plugin;
	}
	
	public void addBlock(Location loc, BlockData data) {
		undoData.put(loc, data);
	}
	
	public List<SimpleEntry<Location, BlockData>> getUndoData(){
		List<SimpleEntry<Location, BlockData>> list = new ArrayList<SimpleEntry<Location,BlockData>>();
		for (Entry<Location, BlockData> e : undoData.entrySet())
			list.add(new SimpleEntry<Location, BlockData>(e));
		
		return list;
	}
	
}
