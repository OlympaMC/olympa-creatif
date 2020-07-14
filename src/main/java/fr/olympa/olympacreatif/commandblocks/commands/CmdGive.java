package fr.olympa.olympacreatif.commandblocks.commands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdGive extends CbCommand {

	private ItemStack item = null;
	
	public CmdGive(CommandType type, CommandSender sender, Location loc, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(type, sender, loc, plugin, plot, args);
	
		targetEntities = parseSelector(args[0], true);
		
		if (args.length < 2)
			return;
		
		item = getItemFromString(args[1]);
		
		if (item == null)
			return;
		
		int amount = 1;
		
		if (args.length == 3) {
			Double[] range = getDoubleRange(args[2]);
			if (range != null)
				amount = (int)(double)range[1];
		}
		
		if (item == null || amount < 0 || amount > 2500)
			return;
		
		item.setAmount(amount);
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
