package fr.olympa.olympacreatif.worldedit_legacy;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_15_R1.TileEntity;

public abstract class ClipboardEdition {

	public static Map<Location, SimpleEntry<BlockData, TileEntity>> rotateSelection(Map<Location, SimpleEntry<BlockData, TileEntity>> clipboard, int rotX, int rotY, int rotZ) {
		rotX = rotX % 360;
		rotY = rotY % 360;
		rotZ = rotZ % 360;

		Map<Location, SimpleEntry<BlockData, TileEntity>> newClipboard = new HashMap<Location, SimpleEntry<BlockData,TileEntity>>();

		//rotation sur Y
		for (int i = 1 ; i <= (int) (rotY+1)/90 ; i++) {
			newClipboard.clear();
			for (Entry<Location, SimpleEntry<BlockData, TileEntity>> entry : clipboard.entrySet()) {
				
				//détermination de la nouvelle position relative du bloc
				double newX = - entry.getKey().getZ();
				double newZ = entry.getKey().getX();
				
				BlockData data = entry.getValue().getKey().clone();
				rotateOnY(data);
				
				newClipboard.put(new Location(entry.getKey().getWorld(), newX, entry.getKey().getY(), newZ), new SimpleEntry<BlockData, TileEntity>(data, entry.getValue().getValue()));
			}
			clipboard = new HashMap<Location, SimpleEntry<BlockData, TileEntity>> (newClipboard);
		}
		
		//rotation sur X
		for (int i = 1 ; i <= (int) (rotX+1)/90 ; i++) {
			newClipboard.clear();
			for (Entry<Location, SimpleEntry<BlockData, TileEntity>> entry : clipboard.entrySet()) {
				
				//détermination de la nouvelle position relative du bloc
				double newZ = - entry.getKey().getY();
				double newY = entry.getKey().getZ();

				BlockData data = entry.getValue().getKey().clone();
				rotateOnX(data);
				
				newClipboard.put(new Location(entry.getKey().getWorld(), entry.getKey().getX(), newY, newZ), new SimpleEntry<BlockData, TileEntity>(data, entry.getValue().getValue()));
			}
			clipboard = new HashMap<Location, SimpleEntry<BlockData, TileEntity>> (newClipboard);
		}
		
		//rotation sur Z
		for (int i = 1 ; i <= (int) (rotZ+1)/90 ; i++) {
			newClipboard.clear();
			for (Entry<Location, SimpleEntry<BlockData, TileEntity>> entry : clipboard.entrySet()) {
				
				//détermination de la nouvelle position relative du bloc
				double newY = - entry.getKey().getX();
				double newX = entry.getKey().getY();

				BlockData data = entry.getValue().getKey().clone();
				rotateOnZ(data);
				
				newClipboard.put(new Location(entry.getKey().getWorld(), newX, newY, entry.getKey().getZ()), new SimpleEntry<BlockData, TileEntity>(data, entry.getValue().getValue()));
			}
			clipboard = new HashMap<Location, SimpleEntry<BlockData, TileEntity>> (newClipboard);
		}
		return newClipboard;
	}
	

	//tourne le bloc de 90° suivant Y sans le sens horaire
	public static void rotateOnY(BlockData data) {
		if (!(data instanceof Directional))
			return;
		
		switch (((Directional) data).getFacing()) {
		case EAST:
			if (((Directional) data).getFaces().contains(BlockFace.SOUTH))
				((Directional) data).setFacing(BlockFace.SOUTH);
			break;
		case EAST_NORTH_EAST:
			if (((Directional) data).getFaces().contains(BlockFace.SOUTH_SOUTH_EAST))
				((Directional) data).setFacing(BlockFace.SOUTH_SOUTH_EAST);
			break;
		case EAST_SOUTH_EAST:
			if (((Directional) data).getFaces().contains(BlockFace.SOUTH_SOUTH_WEST))
				((Directional) data).setFacing(BlockFace.SOUTH_SOUTH_WEST);
			break;
		case NORTH:
			if (((Directional) data).getFaces().contains(BlockFace.EAST))
				((Directional) data).setFacing(BlockFace.EAST);
			break;
		case NORTH_EAST:
			if (((Directional) data).getFaces().contains(BlockFace.SOUTH_EAST))
				((Directional) data).setFacing(BlockFace.SOUTH_EAST);
			break;
		case NORTH_NORTH_EAST:
			if (((Directional) data).getFaces().contains(BlockFace.EAST_SOUTH_EAST))
				((Directional) data).setFacing(BlockFace.EAST_SOUTH_EAST);
			break;
		case NORTH_NORTH_WEST:
			if (((Directional) data).getFaces().contains(BlockFace.EAST_NORTH_EAST))
				((Directional) data).setFacing(BlockFace.EAST_NORTH_EAST);
			break;
		case NORTH_WEST:
			if (((Directional) data).getFaces().contains(BlockFace.NORTH_EAST))
				((Directional) data).setFacing(BlockFace.NORTH_EAST);
			break;
		case SOUTH:
			if (((Directional) data).getFaces().contains(BlockFace.WEST))
				((Directional) data).setFacing(BlockFace.WEST);
			break;
		case SOUTH_EAST:
			if (((Directional) data).getFaces().contains(BlockFace.SOUTH_WEST))
				((Directional) data).setFacing(BlockFace.SOUTH_WEST);
			break;
		case SOUTH_SOUTH_EAST:
			if (((Directional) data).getFaces().contains(BlockFace.WEST_SOUTH_WEST))
				((Directional) data).setFacing(BlockFace.WEST_SOUTH_WEST);
			break;
		case SOUTH_SOUTH_WEST:
			if (((Directional) data).getFaces().contains(BlockFace.WEST_NORTH_WEST))
				((Directional) data).setFacing(BlockFace.WEST_NORTH_WEST);
			break;
		case SOUTH_WEST:
			if (((Directional) data).getFaces().contains(BlockFace.NORTH_WEST))
				((Directional) data).setFacing(BlockFace.NORTH_WEST);
			break;
		case WEST:
			if (((Directional) data).getFaces().contains(BlockFace.NORTH))
				((Directional) data).setFacing(BlockFace.NORTH);
			break;
		case WEST_NORTH_WEST:
			if (((Directional) data).getFaces().contains(BlockFace.NORTH_NORTH_EAST))
				((Directional) data).setFacing(BlockFace.NORTH_NORTH_EAST);
			break;
		case WEST_SOUTH_WEST:
			if (((Directional) data).getFaces().contains(BlockFace.NORTH_NORTH_WEST))
				((Directional) data).setFacing(BlockFace.NORTH_NORTH_WEST);
			break;
		default:
			break;
		}
	
	}

	//tourne le bloc de 90° suivant X sans le sens horaire
	private static void rotateOnX(BlockData data) {
		if (!(data instanceof Directional))
			return;
		
		switch (((Directional) data).getFacing()) {
		case DOWN:
			if (((Directional) data).getFaces().contains(BlockFace.EAST))
				((Directional) data).setFacing(BlockFace.EAST);
			break;
		case EAST:
			if (((Directional) data).getFaces().contains(BlockFace.UP))
				((Directional) data).setFacing(BlockFace.UP);
			break;
		case WEST:
			if (((Directional) data).getFaces().contains(BlockFace.DOWN))
				((Directional) data).setFacing(BlockFace.DOWN);
			break;
		case UP:
			if (((Directional) data).getFaces().contains(BlockFace.WEST))
				((Directional) data).setFacing(BlockFace.WEST);
			break;
		default:
			break;
		
		}
	}
	
	//tourne le bloc de 90° suivant X sans le sens horaire
	private static void rotateOnZ(BlockData data) {
		if (!(data instanceof Directional))
			return;
		
		switch (((Directional) data).getFacing()) {
		case DOWN:
			if (((Directional) data).getFaces().contains(BlockFace.SOUTH))
					((Directional) data).setFacing(BlockFace.SOUTH);
			break;
		case NORTH:
			if (((Directional) data).getFaces().contains(BlockFace.DOWN))
				((Directional) data).setFacing(BlockFace.DOWN);
			break;
		case SOUTH:
			if (((Directional) data).getFaces().contains(BlockFace.UP))
				((Directional) data).setFacing(BlockFace.UP);
			break;
		case UP:
			if (((Directional) data).getFaces().contains(BlockFace.NORTH))
				((Directional) data).setFacing(BlockFace.NORTH);
			break;
		default:
			break;
		
		}
	}
	
	public static Map<Location, SimpleEntry<BlockData, TileEntity>> symmetrySelection(Map<Location, SimpleEntry<BlockData, TileEntity>> clipboard, SymmetryPlan plan) {
		Map<Location, SimpleEntry<BlockData, TileEntity>> newClipboard = new HashMap<Location, SimpleEntry<BlockData,TileEntity>>();

			for (Entry<Location, SimpleEntry<BlockData, TileEntity>> entry : clipboard.entrySet()) {
				BlockData data = entry.getValue().getKey();
				
				//changement de l'orientation du bloc
				if (data instanceof Directional)
					if (((Directional) data).getFaces().contains(((Directional) data).getFacing().getOppositeFace()))
						((Directional) data).setFacing(((Directional) data).getFacing().getOppositeFace());
				
				//changement de la localisation du bloc dans le clipboard selon l'axe choisi
				switch(plan) {
				case AXE_X:
					newClipboard.put(new Location(entry.getKey().getWorld(), -entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ()), new SimpleEntry<BlockData, TileEntity>(data, entry.getValue().getValue()));
					break;
				case AXE_Y:
					newClipboard.put(new Location(entry.getKey().getWorld(), entry.getKey().getX(), -entry.getKey().getY(), entry.getKey().getZ()), new SimpleEntry<BlockData, TileEntity>(data, entry.getValue().getValue()));
					break;
				case AXE_Z:
					newClipboard.put(new Location(entry.getKey().getWorld(), entry.getKey().getX(), entry.getKey().getY(), -entry.getKey().getZ()), new SimpleEntry<BlockData, TileEntity>(data, entry.getValue().getValue()));
					break;
				default:
					break;
				
				}
			}
		return newClipboard;
	}
	
	public enum SymmetryPlan{
		AXE_X("X"),
		AXE_Y("Y"),
		AXE_Z("Z");
		
		String id;
		
		SymmetryPlan(String s){
			id = s;
		}
		
		public String getId() {
			return id;
		}
		
		public static SymmetryPlan getPlan(String s) {
			for (SymmetryPlan plan : SymmetryPlan.values())
				if (plan.getId().equals(s.toUpperCase()))
					return plan;
			return null;
		}
	}

}