package fr.olympa.olympacreatif.worldedit_legacy;

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
import net.minecraft.server.v1_15_R1.TileEntity;

public class Undo {

	private OlympaCreatifMain plugin;
	private Map<Location, SimpleEntry<BlockData, TileEntity>> undoData = new HashMap<Location, SimpleEntry<BlockData,TileEntity>>();
	
	public Undo(OlympaCreatifMain plugin) {
		this.plugin = plugin;
	}
	
	public void addBlock(Location loc, SimpleEntry<BlockData, TileEntity> data) {
		undoData.put(loc, data);
	}
	
	public List<SimpleEntry<Location, SimpleEntry<BlockData, TileEntity>>> getUndoData(){
		List<SimpleEntry<Location, SimpleEntry<BlockData, TileEntity>>> list = new ArrayList<SimpleEntry<Location,SimpleEntry<BlockData,TileEntity>>>();
		for (Entry<Location, SimpleEntry<BlockData, TileEntity>> e : undoData.entrySet())
			list.add(new SimpleEntry<Location, SimpleEntry<BlockData, TileEntity>>(e));
		
		return list;
	}
	
}
