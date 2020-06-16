package fr.olympa.olympacreatif.commandblocks.commands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand.CommandType;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdKill extends CbCommand {

	public CmdKill(CommandType type, CommandSender sender, Location loc, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(type, sender, loc, plugin, plot, args);
	}
	
	@Override
	public int execute() {
		if (args.length != 1)
			return 0;
		
		targetEntities = parseSelector(args[0], false);
		
		for (Entity e : targetEntities)
			if (e instanceof Player)
				((Player)e).setHealth(0d);
			else
				e.remove();
		
		return targetEntities.size();
	}

}