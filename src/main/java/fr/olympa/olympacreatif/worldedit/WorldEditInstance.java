package fr.olympa.olympacreatif.worldedit;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.*;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;
import fr.olympa.olympacreatif.worldedit.*;
import fr.olympa.olympacreatif.worldedit.ClipboardEdition.SymmetryPlan;
import fr.olympa.olympacreatif.worldedit.WorldEditManager.WorldEditError;
import io.netty.util.internal.StringUtil;

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
	public WorldEditError setPos1(Location loc) {
		if (plugin.getPlotsManager().getPlot(loc) != null)
			if (plugin.getPlotsManager().getPlot(loc).getMembers().getPlayerLevel(p) > 1) {
				this.pos1 = loc;
				return WorldEditError.NO_ERROR;
			}
		return WorldEditError.ERR_INSUFFICIENT_PLOT_PERMISSION;
	}
	
	//définit la position 2 de copie si elle est dans la même zone que l'autre (retourne vrai si le joueur a la perm worldedit, false sinon)
	public WorldEditError setPos2(Location loc) {
		if (plugin.getPlotsManager().getPlot(loc) != null)
			if (plugin.getPlotsManager().getPlot(loc).getMembers().getPlayerLevel(p) > 1) {
				this.pos2 = loc;
				return WorldEditError.NO_ERROR;
			}
		return WorldEditError.ERR_INSUFFICIENT_PLOT_PERMISSION;
	}
	
	
	//copie les blocs de la sélection dans la mémoire (ATTENTION coordonnées relatives par rapport à la position actuelle du joueur)
	public WorldEditError copySelection() {
		//cancel si zone trop grande
		if ((Math.abs(pos1.getBlockX()-pos2.getBlockX())+1) * (Math.abs(pos1.getBlockY()-pos2.getBlockY())+1) * (Math.abs(pos1.getBlockZ()-pos2.getBlockZ())+1) 
				> Integer.valueOf(Message.PARAM_WE_MAX_BLOCKS_PER_CMD.getValue()))
			return WorldEditError.ERR_COPY_TOO_BIG;
		
		WorldEditError err = isSelectionValid();
		if (err != WorldEditError.NO_ERROR)
			return err;
		
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
		
		return WorldEditError.NO_ERROR;
	}
	
	//vérifie la validité de la sélection
	public WorldEditError isSelectionValid() {
		//vérification que les deux points sont bien dans le même plot
		if (pos1 != null && pos2 != null)
			if (plugin.getPlotsManager().getPlot(pos1) != null && plugin.getPlotsManager().getPlot(pos2) != null)
				if (plugin.getPlotsManager().getPlot(pos1).equals(plugin.getPlotsManager().getPlot(pos2)))
					return WorldEditError.ERR_OPERATION_CROSS_PLOT;
		
		return WorldEditError.NO_ERROR;
	}
	
	//rotation de la sélection
	public WorldEditError rotateSelection(String planS, String degrees) {
		int rotX = 0;
		int rotY = 0;
		int rotZ = 0;
		
		SymmetryPlan plan = SymmetryPlan.getPlan(planS);
		if (plan != null) {
			if (StringUtils.isNumeric(degrees) && Integer.valueOf(degrees) % 90 == 0) {
				switch (plan) {
				case AXE_X:
					rotX = Integer.valueOf(degrees);
					break;
				case AXE_Y:
					rotY = Integer.valueOf(degrees);
					break;
				case AXE_Z:
					rotZ = Integer.valueOf(degrees);
					break;
				}
				clipboard = ClipboardEdition.rotateSelection(clipboard, Math.floorMod(rotX, 360), Math.floorMod(rotY, 360), Math.floorMod(rotZ, 360));
				return WorldEditError.NO_ERROR;	
			}
			else
				return WorldEditError.ERR_COPY_INCORRECT_DEGREES;
		}else
			return WorldEditError.ERR_NULL_PLAN;
	}
	
	//symétrie de la sélection
	public WorldEditError symetricSelection(String planS) {
		SymmetryPlan plan = SymmetryPlan.getPlan(planS);
		if (plan != null) {
			clipboard = ClipboardEdition.symmetrySelection(clipboard, plan);
			return WorldEditError.NO_ERROR;
		}else
			return WorldEditError.ERR_NULL_PLAN;
	}
	
	//effectuer un undo (retourne true si un undo a été effectué, false sinon)
	public WorldEditError executeUndo() {
		if (undoList.size() == 0)
			return WorldEditError.ERR_UNDO_LIST_EMPTY;

		WorldEditError err = plugin.getWorldEditManager().addToBuildingList(p, undoList.get(undoList.size()-1).getUndoData());
		
		undoList.remove(undoList.size()-1);
		return err;
	}
	
	public WorldEditError setRandomBlocks(String listBlocks) {
		WorldEditError err = WorldEditError.NO_ERROR;
		
		//sortie si la sélection n'est pas valide
		err = isSelectionValid();
		if (err != WorldEditError.NO_ERROR)
			return err;

		Map<BlockData, Integer> probaList = new HashMap<BlockData, Integer>();
		Map<Location, BlockData> toBuild = new HashMap<Location, BlockData>();
		BlockData data = null;
		int totalProba = 0; 
		
		//définition de tous les blockdata possibles
		for (String s : listBlocks.replace(" ", "").split(",")) {
			try{
				data = Bukkit.createBlockData(s.split("x")[0]);
				if (data != null)
					//définition de la proba du bloc (1 par défaut)
					if (s.split("x").length == 2 && StringUtils.isNumeric(s.split("x")[1])) {
						totalProba += Integer.valueOf(s.split("x")[1]);
						probaList.put(data, totalProba);
					}
					else {
						totalProba += 1;
						probaList.put(data, totalProba);
					}
			}catch(IllegalArgumentException e) {
				return WorldEditError.ERR_SET_INVALID_BLOCKDATA;
			}
		}
		
		//définition des blocksdata de manière random
		for (int x = Math.min(pos1.getBlockX(), pos2.getBlockX()) ; x <= Math.max(pos1.getBlockX(), pos2.getBlockX()) ; x++)
			for (int y = Math.min(pos1.getBlockY(), pos2.getBlockY()) ; y <= Math.max(pos1.getBlockY(), pos2.getBlockY()) ; y++)
				for (int z = Math.min(pos1.getBlockZ(), pos2.getBlockZ()) ; z <= Math.max(pos1.getBlockZ(), pos2.getBlockZ()) ; z++) {
					
					//recherche du bloc aléatoire à placer
					int proba = plugin.random.nextInt(totalProba) + 1;
					for (Entry<BlockData, Integer> e : probaList.entrySet())
						if (e.getValue() >= proba) {
							data = e.getKey();
							break;
						}
					//ajout du bloc à la file d'attente
					toBuild.put(new Location(plugin.getWorldManager().getWorld(), x, y, z), data);
				}
		
		return buildBlocks(toBuild);
	}
	
	//remplace les blocs par de l'air dans la zone sélectionnée
	public WorldEditError cutBlocks() {
		WorldEditError err = isSelectionValid();
		if (err != WorldEditError.NO_ERROR)
			return err;

		Map<Location, BlockData> toBuild = new HashMap<Location, BlockData>();
		
		for (int x = Math.min(pos1.getBlockX(), pos2.getBlockX()) ; x <= Math.max(pos1.getBlockX(), pos2.getBlockX()) ; x++)
			for (int y = Math.min(pos1.getBlockY(), pos2.getBlockY()) ; y <= Math.max(pos1.getBlockY(), pos2.getBlockY()) ; y++)
				for (int z = Math.min(pos1.getBlockZ(), pos2.getBlockZ()) ; z <= Math.max(pos1.getBlockZ(), pos2.getBlockZ()) ; z++) 
					toBuild.put(new Location(plugin.getWorldManager().getWorld(), x, y, z), Bukkit.createBlockData(Material.AIR));
		
		return buildBlocks(toBuild);
	}
	
	public WorldEditError pasteSelection() {
		return buildBlocks(clipboard);
		
	}
	
	//colle la sélection à l'endroit souhaité (erreur si paste en dehors du plot ou si le joeur n'est pas proprio des 2 plots)
	private WorldEditError buildBlocks(Map<Location, BlockData> blocksList) {
		Undo undo = new Undo(plugin);
		Plot targetPlot = null;
		List<SimpleEntry<Location, BlockData>> toBuild = new ArrayList<SimpleEntry<Location,BlockData>>();
		
		WorldEditError err = WorldEditError.NO_ERROR;
		
		if (clipboardPlot == null)
			return WorldEditError.ERR_PASTE_NULL_CLIPBOARD;
		
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
						return WorldEditError.ERR_PASTE_NOT_OWNER_OF_2_PLOTS;
					}
				}
			}else {//si le plot cible est nul
				return WorldEditError.ERR_PASTE_PART_ON_NULL_TARGET;
			}
		}
		err = plugin.getWorldEditManager().addToBuildingList(p, toBuild);
		if (err == WorldEditError.NO_ERROR)
		if (undo.getUndoData().size() > 0)
			undoList.add(undo);
		
		return err;
	}
}




