package fr.olympa.olympacreatif.commandblocks.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand.CommandType;
import fr.olympa.olympacreatif.plot.Plot;
import net.minecraft.server.v1_16_R3.NBTTagCompound;

public class CmdClear extends CbCommand {

	private Material matToRemove = null;
	private NBTTagCompound tagToRemove = null;
	int removedItemLimit = 1000000;
	
	public CmdClear(CommandSender sender, Location loc, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(CommandType.clear, sender, loc, plugin, plot, args);

		if (args.length >=2) {
			ItemStack item = getItemFromString(args[1]);
			
			matToRemove = item.getType();
			tagToRemove = CraftItemStack.asNMSCopy(item).getTag();
		}
		
		if (args.length >=3)
			if (StringUtils.isNumeric(args[2]))
				removedItemLimit = (int) (double) Double.valueOf(args[2]);
	}
	
	@Override
	public int execute() {
		
		if (args.length > 0)
			targetEntities = parseSelector(args[0], true);
		else
			if (sender instanceof Player)
				targetEntities.add((Player) sender);
		
		int totalRemovedItems = 0;
		int playerItemsToRemove = 0;
		
		for (Entity e : targetEntities) {
			
			playerItemsToRemove = removedItemLimit;
			
			for (ItemStack it : ((Player) e).getInventory().getContents()) {
				
				if (it == null)
					continue;
				
				if (playerItemsToRemove > 0 && it != null && (matToRemove == null ||
				//si le tag est nul et que les material correspondent
				(it.getType() == matToRemove && tagToRemove == null) ||
				//si le tag n'est pas nul et que les matériaux correspondent
				(it.getType() == matToRemove && tagToRemove.equals(CraftItemStack.asNMSCopy(it).getTag())))) {
					
					if (it.getAmount() < playerItemsToRemove) {
						
						playerItemsToRemove -= it.getAmount();
						it.setAmount(0);
						
					}else {
						it.setAmount(it.getAmount() - playerItemsToRemove);
						playerItemsToRemove = 0;
					}
				}
			}
			
			totalRemovedItems += removedItemLimit - playerItemsToRemove;
		}
		return totalRemovedItems;
	}

}
