package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdClear extends CbCommand {

	private Material matToRemove = null;
	int limit = 1000000;
	
	public CmdClear(CommandSender sender, OlympaCreatifMain plugin, Plot plot, String[] commandString) {
		super(sender, plugin, plot, commandString);
		
		targetEntities = parseSelector(plot, args[0], true);
		
		switch (args.length) {
		case 1:
			break;
		case 2:
			matToRemove = Material.getMaterial(args[1]);
			break;
		case 3:
			matToRemove = Material.getMaterial(args[1]);
			if (StringUtils.isNumeric(args[2]))
				limit = (int) (double) Double.valueOf(args[2]);
			break;
		}
	}
	
	@Override
	public int execute() {
		List<Entity> concernedPlayers = new ArrayList<Entity>();
		
		for (Entity e : targetEntities)
			for (ItemStack it : ((Player) e).getInventory().getContents())
				if (it != null && (matToRemove == null || it.getType() == matToRemove)) {
					if (it.getAmount() > limit) {
						limit -= it.getAmount();
						((Player) e).getInventory().remove(it);
						
						if (concernedPlayers.contains(e))
							concernedPlayers.add(e);
						
					}else {
						it.setAmount(it.getAmount() - limit);
						limit = 0;
						break;
					}
				}
		return concernedPlayers.size();
	}

}