package fr.olympa.olympacreatif.worldedit;

import java.util.HashMap;
import java.util.Map;

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
	
	public Map<Location, BlockData> getUndoData(){
		return undoData;
	}
	
}
