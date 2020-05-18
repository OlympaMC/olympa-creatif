package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand.CommandType;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdClear extends CbCommand {

	private Material matToRemove = null;
	int removedItemLimit = 1000000;
	
	public CmdClear(CommandType type, CommandSender sender, Location loc, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(type, sender, loc, plugin, plot, args);
		
		targetEntities = parseSelector(args[0], true);
		
		switch (args.length) {
		case 2:
			if (args[1].split(":").length == 2)
				matToRemove = Material.getMaterial(args[1].split(":")[1].toUpperCase());
			break;
		case 3:
			if (args[1].split(":").length == 2)
				matToRemove = Material.getMaterial(args[1].split(":")[1].toUpperCase());
			
			if (StringUtils.isNumeric(args[2]))
				removedItemLimit = (int) (double) Double.valueOf(args[2]);
			break;
		}
	}
	
	@Override
	public int execute() {
		
		int totalRemovedItems = 0;
		int playerItemsToRemove = 0;
		
		for (Entity e : targetEntities) {
			
			playerItemsToRemove = removedItemLimit;
			
			for (ItemStack it : ((Player) e).getInventory().getContents())
				if (playerItemsToRemove > 0 && it != null && (matToRemove == null || it.getType() == matToRemove)) {
					if (it.getAmount() < playerItemsToRemove) {
						
						playerItemsToRemove -= it.getAmount();
						((Player) e).getInventory().remove(it);
						
					}else {
						it.setAmount(it.getAmount() - playerItemsToRemove);
						playerItemsToRemove = 0;
					}
				}
			totalRemovedItems += removedItemLimit - playerItemsToRemove;
		}
		return totalRemovedItems;
	}

}
