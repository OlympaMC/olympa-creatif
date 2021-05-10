package fr.olympa.olympacreatif.utils;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

public abstract class OtherUtils {

	public static Location getFacingLoc(Location l, BlockFace face) {
		Location loc = l.clone();
		
		switch (face) {
		case UP:
			return loc.add(0, 1, 0);
		case DOWN:
			return loc.add(0, -1, 0);
			
		case SOUTH:
			return loc.add(0, 0, 1);
		case NORTH:
			return loc.add(0, 0, -1);
			
		case WEST:
			return loc.add(-1, 0, 0);
		case EAST:
			return loc.add(1, 0, 0);
			
		default:
			return loc;
		}
	}
	
}
