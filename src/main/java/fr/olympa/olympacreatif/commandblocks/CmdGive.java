package fr.olympa.olympacreatif.commandblocks;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;
import net.minecraft.server.v1_15_R1.MojangsonParser;
import net.minecraft.server.v1_15_R1.NBTTagCompound;

public class CmdGive extends CbCommand {

	private ItemStack item = null;
	
	public CmdGive(OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(plugin, plot, args);
		
		targetEntities = parseSelector(args[0], true);
		
		Material mat;
		
		if (args.length >= 2)
			if (!args[1].contains("{")){
				mat = Material.getMaterial(args[1]);

				if (mat != null)
					item = new ItemStack(mat);
			}else {
				mat = Material.getMaterial(args[1]);

				if (mat != null) {
					item = new ItemStack(mat);
					args[1].replace(mat.toString(), "");

					//tentative récupération tag item
					try {
						NBTTagCompound tag = MojangsonParser.parse(args[1]);
						
						net.minecraft.server.v1_15_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
						
						nmsItem.setTag(plugin.getPerksManager().getNbtEntityParser().getValidItem(tag));
						
						item = CraftItemStack.asBukkitCopy(nmsItem);
					} catch (CommandSyntaxException e) {
					}
				}
			}
		
		
		if (args.length == 3 && item != null)
			if (StringUtils.isNumeric(args[2])) {
				int amount = (int)(double)Double.valueOf(args[2]);
				if (amount >= 1 && amount <= 64)
					item.setAmount(amount);
			}
	}
	
	@Override
	public int execute() {
		if (item == null)
			return 0;
		
		for (Entity e : targetEntities) 
			((Player) e).getInventory().addItem(item);
		
		
		return targetEntities.size();
	}

}
