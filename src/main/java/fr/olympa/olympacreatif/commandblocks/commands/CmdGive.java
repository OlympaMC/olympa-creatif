package fr.olympa.olympacreatif.commandblocks.commands;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdGive extends CbCommand {

	private ItemStack item = null;
	
	public CmdGive(Entity sender, Location loc, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(CommandType.give, sender, loc, plugin, plot, args);
		
		if (args.length >= 2)
			item = getItemFromString(args[1], sender instanceof Player ? (Player) sender : null);
	}
	
	@Override
	public int execute() {
		
		if (args.length < 2)
			return 0;
		
		if (item == null)
			return 0;
	
		targetEntities = parseSelector(args[0], true);
		
		if (targetEntities.size() == 0)
			return 0;
		
		int amount = 1;
		
		//Bukkit.broadcastMessage("args cmd : " + new ArrayList<String>(Arrays.asList(args)).toString());
		
		if (args.length == 3) {
			Double[] range = getDoubleRange(args[2]);
			
			//Bukkit.broadcastMessage("arg 2 : " + args[2]);
			//Bukkit.broadcastMessage("range : " + range[0] + " " + range[1]);
			
			if (range != null)
				amount = (int)(double)range[1];
		}
		
		if (amount < 0 || amount > 3000)
			return 0;
		
		item.setAmount(amount);
		
		for (Entity e : targetEntities)
			if (plugin.getPerksManager().getKitsManager().hasPlayerPermissionFor(AccountProvider.get(e.getUniqueId()), item.getType()))
				((Player) e).getInventory().addItem(item);
			else
				((Player) e).getInventory().addItem(plugin.getPerksManager().getKitsManager().getNoKitPermItem(item.getType()));
		
		return targetEntities.size();
	}

}
