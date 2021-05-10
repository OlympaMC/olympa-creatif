package fr.olympa.olympacreatif.utils;

import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableSet;

public abstract class OtherUtils {
	
	public static final Set<Material> commandBlockTypes = ImmutableSet.<Material>builder()
			.add(Material.COMMAND_BLOCK)
			.add(Material.CHAIN_COMMAND_BLOCK)
			.add(Material.REPEATING_COMMAND_BLOCK)
			.build();

	public static boolean isCommandBlock(Material mat) {
		return commandBlockTypes.contains(mat);
	}
	public static boolean isCommandBlock(Block block) {
		return block != null ? isCommandBlock(block.getType()) : false;
	}
	public static boolean isCommandBlock(ItemStack item) {
		return item != null ? isCommandBlock(item.getType()) : false;
	}
	
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
	
	public static int getCbCount(Chunk ch) {
		return ch.getTileEntities((Block b) -> isCommandBlock(b.getType()), false).size();
	}
	
	@Deprecated
	/**
	 * Use the getCbCount(Chunk ch) instead if possible
	 * @param ch
	 * @return
	 */
	public static int getCbCount(ChunkSnapshot ch) {
		int cbCount = 0;

		for (int x = 0 ; x < 16 ; x++)
			for (int y = 0 ; y < 256 ; y++)
    			for (int z = 0 ; z < 16 ; z++)
    				if (isCommandBlock(ch.getBlockType(x, y, z)))
    					cbCount++;
		
		return cbCount;
	}
	
}
