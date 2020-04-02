package fr.olympa.olympacreatif.worldedit;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;

public abstract class ClipboardEdition {

	public static void rotateSelection(Map<Location, BlockData> clipboard, int rotX, int rotY, int rotZ) {
		rotX = rotX % 360;
		rotY = rotY % 360;
		rotZ = rotZ % 360;

		//rotation sur X
		for (int i = 1 ; i <= (int) (rotX+1)/90 ; i++) {
			Map<Location, BlockData> newClipboard = new HashMap<Location, BlockData>();
			for ( Entry<Location, BlockData> entry : clipboard.entrySet()) {
				
				//détermination de la nouvelle position relative du bloc
				double newX = entry.getKey().getZ();
				double newZ = - entry.getKey().getX();
				
				BlockData data = entry.getValue().clone();
				rotateOnX(data);
				
				newClipboard.put(new Location(entry.getKey().getWorld(), newX, entry.getKey().getY(), newZ), data);
			}
			
			clipboard = newClipboard;
		}
		
		//rotation sur Y
		for (int i = 1 ; i <= (int) (rotY+1)/90 ; i++) {
			Map<Location, BlockData> newClipboard = new HashMap<Location, BlockData>();
			for ( Entry<Location, BlockData> entry : clipboard.entrySet()) {
				
				//détermination de la nouvelle position relative du bloc
				double newZ = entry.getKey().getY();
				double newY = - entry.getKey().getZ();
				
				BlockData data = entry.getValue().clone();
				rotateOnY(data);
				
				newClipboard.put(new Location(entry.getKey().getWorld(), entry.getKey().getX(), newY, newZ), data);
			}
			
			clipboard = newClipboard;
		}
		
		//rotation sur Z
		for (int i = 1 ; i <= (int) (rotZ+1)/90 ; i++) {
			Map<Location, BlockData> newClipboard = new HashMap<Location, BlockData>();
			for ( Entry<Location, BlockData> entry : clipboard.entrySet()) {
				
				//détermination de la nouvelle position relative du bloc
				double newY = entry.getKey().getX();
				double newX = - entry.getKey().getY();
				
				BlockData data = entry.getValue().clone();
				rotateOnZ(data);
				
				newClipboard.put(new Location(entry.getKey().getWorld(), newX, newY, entry.getKey().getZ()), data);
			}
			
			clipboard = newClipboard;
		}
	}
	

	//tourne le bloc de 90° suivant Y sans le sens horaire
	private static void rotateOnY(BlockData data) {
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
	
	public static void symmetrySelection(Map<Location, BlockData> clipboard, SymmetryPlan plan) {
		Map<Location, BlockData> newClipboard = new HashMap<Location, BlockData>();

			for (Entry<Location, BlockData> entry : clipboard.entrySet()) {
				BlockData data = entry.getValue();
				
				//changement de l'orientation du bloc
				if (data instanceof Directional)
					if (((Directional) data).getFaces().contains(((Directional) data).getFacing().getOppositeFace()))
						((Directional) data).setFacing(((Directional) data).getFacing().getOppositeFace());
				
				//changement de la localisation du bloc dans le clipboard selon l'axe choisi
				switch(plan) {
				case AXE_X:
					newClipboard.put(new Location(entry.getKey().getWorld(), -entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ()), data);
					break;
				case AXE_Y:
					newClipboard.put(new Location(entry.getKey().getWorld(), entry.getKey().getX(), -entry.getKey().getY(), entry.getKey().getZ()), data);
					break;
				case AXE_Z:
					newClipboard.put(new Location(entry.getKey().getWorld(), entry.getKey().getX(), entry.getKey().getY(), -entry.getKey().getZ()), data);
					break;
				default:
					break;
				
				}
			}
		clipboard = newClipboard;
	}
	
	public enum SymmetryPlan{
		AXE_X("X"),
		AXE_Y("X"),
		AXE_Z("X");
		
		String id;
		
		SymmetryPlan(String s){
			id = s;
		}
		
		public static SymmetryPlan getPlan(String s) {
			
			return null;
		}
	}

}
