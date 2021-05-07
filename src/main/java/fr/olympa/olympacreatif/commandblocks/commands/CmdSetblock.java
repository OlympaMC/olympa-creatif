package fr.olympa.olympacreatif.commandblocks.commands;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand.CommandType;
import fr.olympa.olympacreatif.plot.Plot;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.TileEntity;

public class CmdSetblock extends CbCommand {

	Location placingLoc;
	ItemStack item;
	
	public CmdSetblock(Entity sender, Location sendingLoc, OlympaCreatifMain plugin,
			Plot plot, String[] commandString) {
		super(CommandType.setblock, sender, sendingLoc, plugin, plot, commandString);
		

		if (args.length < 4)
			return;
		
		placingLoc = parseLocation(args[0], args[1], args[2]);
		item = getItemFromString(args[3]);
		
	}

	@Override
	public int execute() {
		
		if (placingLoc == null || item == null)
			return 0;
		
		//return si le proprio n'a pas débloqué les spawners
		if (item.getType() == Material.SPAWNER && !plotCbData.hasUnlockedSpawnerSetblock() || plugin.getWEManager().isReseting(getPlot()))
			return 0;
		
		Block block = plugin.getWorldManager().getWorld().getBlockAt(placingLoc);
		block.setType(item.getType());
		plot.getCbData().removeCommandBlock(block);
		
		net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
		
		if (nmsItem != null && nmsItem.getTag() != null) {
			TileEntity tile = plugin.getWorldManager().getNmsWorld().getTileEntity(new BlockPosition(placingLoc.getBlockX(), placingLoc.getBlockY(), placingLoc.getBlockZ()));
			
			NBTTagCompound tag = nmsItem.getTag();
			tag.setInt("x", placingLoc.getBlockX());
			tag.setInt("y", placingLoc.getBlockY());
			tag.setInt("z", placingLoc.getBlockZ());
			
			tile.load(null, tag);	
		}
		return 1;
	}
}
