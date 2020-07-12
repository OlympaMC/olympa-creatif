package fr.olympa.olympacreatif.commandblocks.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
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
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.utils.NBTutil;
import fr.olympa.olympacreatif.utils.NbtParserUtil;
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
		
		//get material
		String withoutTag = args[1].split("\\{")[0].toUpperCase();
		if (withoutTag.contains(":") && withoutTag.length() > 10)
			mat = Material.getMaterial(withoutTag.substring(10));
		else
			mat = Material.getMaterial(withoutTag);
		
		//get tag
		if (args[1].contains("{")) {
			try {
				tag = MojangsonParser.parse(args[1].substring(args[1].indexOf("{")));
			} catch (CommandSyntaxException e) {
			}
		}
		
		//Bukkit.broadcastMessage("TAG vérifié new method : " + NBTutil.getValidTags(tag).asString());
		
		//get quantité
		if (args.length  == 3 && StringUtils.isNumeric(args[2]))
			count = Math.max(Math.min((int)(double)Double.valueOf(args[2]), 5184), 1);
		
		if (mat != null) {
			item = new ItemStack(mat, count);
			
			if (tag != null) {				
				net.minecraft.server.v1_15_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
				
				//Bukkit.broadcastMessage("tag avant check : " + tag.asString());
				//Bukkit.broadcastMessage("tag après check : " + NbtParserUtil.getValidItem(tag).asString());
				
				nmsItem.setTag(NBTutil.getValidTags(tag));
				
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
