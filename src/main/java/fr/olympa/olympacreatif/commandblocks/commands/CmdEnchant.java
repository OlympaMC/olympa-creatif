package fr.olympa.olympacreatif.commandblocks.commands;

import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.StringUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdEnchant extends CbCommand {

	private Enchantment ench;
	private int level = 1;
	
	public CmdEnchant(CommandSender sender, OlympaCreatifMain plugin, Plot plot, String[] commandString) {
		super(sender, plugin, plot, commandString);
		
		if (args.length >= 2)
			targetEntities = parseSelector(plot, args[0], true);
		
		ench = Enchantment.getByKey(NamespacedKey.minecraft(args[1]));
		
		if (args.length == 3)
			if (StringUtils.isNumeric(args[2]))
				level = (int)(double) Double.valueOf(args[2]);
	}

	@Override
	public int execute() {
		if (ench == null)
			return 0;
		
		int i = 0;
		
		for (Entity e : targetEntities)
			if (((Player) e).getInventory().getItemInMainHand() != null) {
				((Player) e).getInventory().setItemInMainHand(ItemUtils.addEnchant(((Player) e).getInventory().getItemInMainHand(), ench, level));
				i++;
			}
		
		return i;
	}
}
