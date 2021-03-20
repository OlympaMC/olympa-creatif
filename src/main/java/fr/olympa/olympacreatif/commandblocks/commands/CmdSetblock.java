package fr.olympa.olympacreatif.commandblocks.commands;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.TileEntity;

public class CmdSetblock extends CbCommand {

	public CmdSetblock(CommandType cmdType, CommandSender sender, Location sendingLoc, OlympaCreatifMain plugin,
			Plot plot, String[] commandString) {
		super(cmdType, sender, sendingLoc, plugin, plot, commandString);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int execute() {
		if (args.length < 4)
			return 0;
		
		Location placingLoc = parseLocation(args[0], args[1], args[2]);
		ItemStack item = getItemFromString(args[3]);
		
		if (placingLoc == null || item == null)
			return 0;
		
		//return si le proprio n'a pas débloqué les spawners
		if (item.getType() == Material.SPAWNER && !plotCbData.hasUnlockedSpawnerSetblock() || plugin.getWEManager().isReseting(getPlot()))
			return 0;
		
		plugin.getWorldManager().getWorld().getBlockAt(placingLoc).setType(item.getType());
		
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
