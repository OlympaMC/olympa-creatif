package fr.olympa.olympacreatif.commandblocks.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand.CommandType;
import fr.olympa.olympacreatif.perks.NbtParserUtil;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.world.WorldEventsListener;
import net.minecraft.server.v1_15_R1.MojangsonParser;
import net.minecraft.server.v1_15_R1.NBTTagCompound;

public class CmdGive extends CbCommand {

	private ItemStack item = null;
	private int count = 1;
	
	public CmdGive(CommandType type, CommandSender sender, Location loc, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(type, sender, loc, plugin, plot, args);
	
		targetEntities = parseSelector(args[0], true);

		Material mat = null;
		NBTTagCompound tag = null;
		
		if (args.length < 2)
			return;
		
		if (args.length >= 2 && args[1].split(":").length == 2) {
			//get material
			mat = Material.getMaterial(args[1].split(":")[1].split("\\{")[0].toUpperCase());
			
			//get tag
			if (args[1].contains("\\{")) {
				try {
					tag = MojangsonParser.parse(args[1].substring(args[1].indexOf("\\{"), args[1].length()));
				} catch (CommandSyntaxException e) {
				}
			}
		}
		
		if (args.length  == 3 && StringUtils.isNumeric(args[2]))
			count = Math.max(Math.min((int)(double)Double.valueOf(args[2]), 64), 1);
		
		if (mat != null) {
			item = new ItemStack(mat, count);
			
			if (tag != null) {				
				net.minecraft.server.v1_15_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
				
				nmsItem.setTag(NbtParserUtil.getValidItem(tag));
				
				item = CraftItemStack.asBukkitCopy(nmsItem);
			}		
		}
	}
	
	@Override
	public int execute() {
		if (item == null)
			return 0;
		
		for (Entity e : targetEntities)
			if (plugin.getWorldManager().hasPlayerPermissionFor(AccountProvider.get(e.getUniqueId()), item.getType(), false))
				((Player) e).getInventory().addItem(item);
		
		
		return targetEntities.size();
	}

}
