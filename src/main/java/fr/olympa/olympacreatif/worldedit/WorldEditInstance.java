package fr.olympa.olympacreatif.worldedit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.objects.Plot;
import fr.olympa.olympacreatif.objects.PlotRank;
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

	//définit la position 2 de copie si elle est dans la même zone que l'autre
	public boolean setPos1(Location loc) {
		if (plugin.getPlot(loc).getMembers().containsKey(p))
			if (plugin.getPlot(loc).getMembers().get(p).getLevel() > 1) {
				this.pos1 = loc;
				return true;
			}
		return false;
	}
	
	//définit la position 2 de copie si elle est dans la même zone que l'autre
	public boolean setPos2(Location loc) {
		if (plugin.getPlot(loc).getMembers().containsKey(p))
			if (plugin.getPlot(loc).getMembers().get(p).getLevel() > 1) {
				this.pos2 = loc;
				return true;
			}
		return false;
	}
	
	
	//copie les blocs de la sélection dans la mémoire (ATTENTION coordonnées relatives par rapport à la position actuelle du joueur)
	public boolean copySelection() {
		//cancel si zone trop grande
		if ((Math.abs(pos1.getBlockX()-pos2.getBlockX())+1) * (Math.abs(pos1.getBlockY()-pos2.getBlockY())+1) * (Math.abs(pos1.getBlockZ()-pos2.getBlockZ())+1) > plugin.maxWorldEditBlocks)
			return false;
		
		clipboardPlot = plugin.getPlot(pos1);
		
		for (int x = Math.min(pos1.getBlockX(), pos2.getBlockX()) ; x >= Math.max(pos1.getBlockX(), pos2.getBlockX()) ; x++)
			for (int y = Math.min(pos1.getBlockY(), pos2.getBlockY()) ; y >= Math.max(pos1.getBlockY(), pos2.getBlockY()) ; y++)
				for (int z = Math.min(pos1.getBlockZ(), pos2.getBlockZ()) ; z >= Math.max(pos1.getBlockZ(), pos2.getBlockZ()) ; z++) {
					Location loc = new Location(plugin.getWorldManager().getWorld(), z, y, z);
					clipboard.put(p.getLocation().clone().subtract(loc), plugin.getWorldManager().getWorld().getBlockAt(loc).getBlockData());
				}
		
		return true;
	}
	
	//rotation de la sélection
	public void rotateSelection(int rotX, int rotY, int rotZ) {
		ClipboardEdition.rotateSelection(clipboard, rotX, rotY, rotZ);
	}
	
	//symétrie de la sélection
	public void symetricSelection(SymmetryPlan plan) {
		ClipboardEdition.symmetrySelection(clipboard, plan);
	}
	
	//effectuer un undo (retourne true si un undo a été effectué, false sinon)
	public boolean executeUndo() {
		if (undoList.size() == 0)
			return false;
		for (Entry<Location, BlockData> e : undoList.get(undoList.size()-1).getUndoData().entrySet()) {
			plugin.getWorldManager().addToBuildWaitingList(e.getKey(), e.getValue());
		}
		
		undoList.remove(undoList.size()-1);
		return true;
	}
	
	//retourne vrai si les deux points de la sélection sont dans le même plot, faux sinon (à appeler avant copySelection() )
	public boolean isSelectionValid() {
		if (pos1 != null && pos2 != null)
			if (plugin.getPlot(pos1) != null && plugin.getPlot(pos2) != null)
				if (plugin.getPlot(pos1).equals(plugin.getPlot(pos2)))
					return true;
		
		return false;
	}
	
	//colle la sélection à l'endroit souhaité (true si paste complètement effectué, false sinon)
	public boolean pasteSelection() {
		Undo undo = new Undo(plugin);
		Plot targetPlot = null;
		
		for (Entry<Location, BlockData> entry : clipboard.entrySet()) {
			targetPlot = plugin.getPlot(entry.getKey());
			
			//si le plot cible n'est pas nul
			if (targetPlot != null) {
				//si le plot cible est égal à celui de départ
				if (clipboardPlot.equals(targetPlot)) {
					
					//paste du block
					Location loc = entry.getKey().clone().add(p.getLocation());
					undo.addBlock(loc, loc.getBlock().getBlockData().clone());
					plugin.getWorldManager().addToBuildWaitingList(loc, entry.getValue());
					
				}else {//si le plot cible est pas égal à celui de départ
					//si le propriétaire est le même dans les 2 plots
					if (targetPlot.getPlayerRank(p)  == PlotRank.PERMISSIONS_OWNER && clipboardPlot.getPlayerRank(p)  == PlotRank.PERMISSIONS_OWNER ) {
						
						//paste du block
						Location loc = entry.getKey().clone().add(p.getLocation());
						undo.addBlock(loc, loc.getBlock().getBlockData().clone());
						plugin.getWorldManager().addToBuildWaitingList(loc, entry.getValue());
						
					}else {//si le joueur n'est pas propriétaire des 2 plots
						undoList.add(undo);
						return false;	
					}
				}
			}else {//si le plot cible est nul
				undoList.add(undo);
				return false;
			}
		}
		
		undoList.add(undo);
		return true;
	}
}




