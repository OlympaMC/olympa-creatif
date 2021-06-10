package fr.olympa.olympacreatif.commandblocks.commands;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.common.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.perks.KitsManager.KitType;
import fr.olympa.olympacreatif.plot.Plot;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.TileEntity;

public class CmdSetblock extends CbCommand {

	Location placingLoc;
	ItemStack item;
	OlympaPlayerCreatif pc = null;
	KitType kit  = null;
	
	public CmdSetblock(Entity sender, Location sendingLoc, OlympaCreatifMain plugin,
			Plot plot, String[] commandString) {
		super(CommandType.setblock, sender, sendingLoc, plugin, plot, commandString);
		

		if (args.length < 4)
			return;
		
		if (sender instanceof Player)
			pc = AccountProvider.getter().get(sender.getUniqueId());
		
		placingLoc = parseLocation(args[0], args[1], args[2]);
		item = getItemFromString(args[3], sender instanceof Player ? (Player) sender : null);

		if (item != null)
			kit = plugin.getPerksManager().getKitsManager().getKitOf(item.getType());
	}

	@Override
	public int execute() {
		
		if (placingLoc == null || item == null || !placingLoc.isChunkLoaded())
			return 0;
		
		//return si le proprio n'a pas débloqué les spawners
		if (item.getType() == Material.SPAWNER && !plotCbData.hasUnlockedSpawnerSetblock() || plugin.getWEManager().isReseting(getPlot()))
			return 0;
		
		/*if (OtherUtils.isCommandBlock(item) && OtherUtils.getCbCount(placingLoc.getChunk()) > OCparam.MAX_CB_PER_CHUNK.get())
			return 0;*/
		
		if (kit == KitType.ADMIN || kit == KitType.COMMANDBLOCK) {
			return 0;
		}
		
		if (pc != null && kit != null && !pc.hasKit(kit)) {
			OCmsg.INSUFFICIENT_KIT_PERMISSION.send(pc, kit);
			return 0;
		}
		
		Block block = plugin.getWorldManager().getWorld().getBlockAt(placingLoc);
		plot.getCbData().removeCommandBlock(block);
		
		block.setType(item.getType());
		
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
