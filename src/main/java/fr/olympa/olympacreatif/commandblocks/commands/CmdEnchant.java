package fr.olympa.olympacreatif.commandblocks.commands;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.StringUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand.CommandType;
import fr.olympa.olympacreatif.plot.Plot;

@SuppressWarnings("unused")
public class CmdEnchant extends CbCommand {

	private Enchantment ench = null;
	private int level = 1;
	
	public CmdEnchant(CommandSender sender, Location loc, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(CommandType.enchant, sender, loc, plugin, plot, args);
	
		if (args.length >= 2) {
			ench = args[1].startsWith("minecraft:") ? 
					Enchantment.getByKey(NamespacedKey.fromString(args[1])) : Enchantment.getByKey(NamespacedKey.minecraft(args[1]));
		}
		
		if (args.length == 3)
			if (StringUtils.isNumeric(args[2]))
				level = (int)(double) Double.valueOf(args[2]);
	}

	@Override
	public int execute() {
		if (ench == null)
			return 0;
		
		if (args.length >= 2)
			targetEntities = parseSelector(args[0], true);
		
		int i = 0;
		
		for (Entity e : targetEntities)
			if (((Player) e).getInventory().getItemInMainHand() != null && ((Player) e).getInventory().getItemInMainHand().getType() != Material.AIR) {
				((Player) e).getInventory().setItemInMainHand(ItemUtils.addEnchant(((Player) e).getInventory().getItemInMainHand(), ench, level > 9 ? 9 : level));
				i++;
			}
		
		return i;
	}
}
