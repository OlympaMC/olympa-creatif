package fr.olympa.olympacreatif.worldedit;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.*;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;
import fr.olympa.olympacreatif.worldedit.*;
import fr.olympa.olympacreatif.worldedit.ClipboardEdition.SymmetryPlan;

public class WorldEditInstance {

	private OlympaCreatifMain plugin;
	private Player p;
	
	private List<Undo> undoList = new ArrayList<Undo>();
	
	private Map<Location, BlockData> clipboard = new HashMap<Location, BlockData>();
	
	private Plot clipboardPlot;
	
	private Location pos1;
	private Location pos2;
	
	public WorldEditInstance(OlympaCreatifMain plugin, Player p) {
		this.plugin = plugin;
		this.p = p;
	}

	//définit la position 1 de copie si elle est dans la même zone que l'autre (retourne vrai si le joueur a la perm worldedit, false sinon)
	public boolean setPos1(Location loc) {
		if (plugin.getPlotsManager().getPlot(loc) != null)
			if (plugin.getPlotsManager().getPlot(loc).getMembers().getPlayerLevel(p) > 1) {
				this.pos1 = loc;
				return true;
			}
		return false;
	}
	
	//définit la position 2 de copie si elle est dans la même zone que l'autre (retourne vrai si le joueur a la perm worldedit, false sinon)
	public boolean setPos2(Location loc) {
		if (plugin.getPlotsManager().getPlot(loc) != null)
			if (plugin.getPlotsManager().getPlot(loc).getMembers().getPlayerLevel(p) > 1) {
				this.pos2 = loc;
				return true;
			}
		return false;
	}
	
	
	//copie les blocs de la sélection dans la mémoire (ATTENTION coordonnées relatives par rapport à la position actuelle du joueur)
	public boolean copySelection() {
		//cancel si zone trop grande
		if ((Math.abs(pos1.getBlockX()-pos2.getBlockX())+1) * (Math.abs(pos1.getBlockY()-pos2.getBlockY())+1) * (Math.abs(pos1.getBlockZ()-pos2.getBlockZ())+1) 
				> Integer.valueOf(Message.PARAM_WE_MAX_BLOCKS_PER_CMD.getValue()))
			return false;

		clipboardPlot = plugin.getPlotsManager().getPlot(pos1);
		clipboard.clear();
		
		//copie le bloc et sa position relative au joueur
		for (int x = Math.min(pos1.getBlockX(), pos2.getBlockX()) ; x <= Math.max(pos1.getBlockX(), pos2.getBlockX()) ; x++)
			for (int y = Math.min(pos1.getBlockY(), pos2.getBlockY()) ; y <= Math.max(pos1.getBlockY(), pos2.getBlockY()) ; y++)
				for (int z = Math.min(pos1.getBlockZ(), pos2.getBlockZ()) ; z <= Math.max(pos1.getBlockZ(), pos2.getBlockZ()) ; z++) {
					Location loc = new Location(plugin.getWorldManager().getWorld(), x, y, z);
					clipboard.put(loc.clone().subtract(p.getPlayer().getLocation().getBlockX(), p.getPlayer().getLocation().getBlockY(), p.getPlayer().getLocation().getBlockZ()), 
							plugin.getWorldManager().getWorld().getBlockAt(loc).getBlockData());
				}
		return true;
	}
	
	//rotation de la sélection
	public void rotateSelection(int rotX, int rotY, int rotZ) {
		clipboard = ClipboardEdition.rotateSelection(clipboard, Math.floorMod(rotX, 360), Math.floorMod(rotY, 360), Math.floorMod(rotZ, 360));
	}
	
	//symétrie de la sélection
	public void symetricSelection(SymmetryPlan plan) {
		clipboard = ClipboardEdition.symmetrySelection(clipboard, plan);
	}
	
	//effectuer un undo (retourne true si un undo a été effectué, false sinon)
	public boolean executeUndo() {
		if (undoList.size() == 0)
			return false;

		plugin.getWorldEditManager().addToBuildingList(p, undoList.get(undoList.size()-1).getUndoData());
		
		undoList.remove(undoList.size()-1);
		return true;
	}
	
	//retourne vrai si les deux points de la sélection sont dans le même plot, faux sinon (à appeler avant copySelection() )
	public boolean isSelectionValid() {
		if (pos1 != null && pos2 != null)
			if (plugin.getPlotsManager().getPlot(pos1) != null && plugin.getPlotsManager().getPlot(pos2) != null)
				if (plugin.getPlotsManager().getPlot(pos1).equals(plugin.getPlotsManager().getPlot(pos2)))
					return true;
		
		return false;
	}
	
	public boolean pasteSelection() {
		return buildBlocks(clipboard);
	}
	
	//colle la sélection à l'endroit souhaité (true si paste complètement effectué, false sinon)
	private boolean buildBlocks(Map<Location, BlockData> blocksList) {
		Undo undo = new Undo(plugin);
		Plot targetPlot = null;
		List<SimpleEntry<Location, BlockData>> toBuild = new ArrayList<SimpleEntry<Location,BlockData>>();
		
		if (clipboardPlot == null)
			return false;
		
		for (Entry<Location, BlockData> entry : blocksList.entrySet()) {
			targetPlot = plugin.getPlotsManager().getPlot(entry.getKey().clone().add(p.getLocation()));

			//si le plot cible n'est pas nul
			if (targetPlot != null) {
				//si le plot cible est égal à celui de départ
				if (clipboardPlot.equals(targetPlot)) {
					
					//paste du block
					Location loc = entry.getKey().clone().add(p.getLocation());
					undo.addBlock(loc, loc.getBlock().getBlockData().clone());
					toBuild.add(new SimpleEntry<Location, BlockData>(loc, entry.getValue()));
					
				}else {//si le plot cible est pas égal à celui de départ
					//si le propriétaire est le même dans les 2 plots
					if (targetPlot.getMembers().getPlayerRank(p)  == PlotRank.OWNER && clipboardPlot.getMembers().getPlayerRank(p)  == PlotRank.OWNER ) {
						
						//paste du block
						Location loc = entry.getKey().clone().add(p.getLocation());
						undo.addBlock(loc, loc.getBlock().getBlockData().clone());
						toBuild.add(new SimpleEntry<Location, BlockData>(loc, entry.getValue()));
						
					}else {//si le joueur n'est pas propriétaire des 2 plots
						plugin.getWorldEditManager().addToBuildingList(p, toBuild);
						if (undo.getUndoData().size() > 0)
							undoList.add(undo);
						return false;	
					}
				}
			}else {//si le plot cible est nul
				plugin.getWorldEditManager().addToBuildingList(p, toBuild);
				if (undo.getUndoData().size() > 0)
					undoList.add(undo);
				return false;
			}
		}
		plugin.getWorldEditManager().addToBuildingList(p, toBuild);
		if (undo.getUndoData().size() > 0)
			undoList.add(undo);
		return true;
	}
}




