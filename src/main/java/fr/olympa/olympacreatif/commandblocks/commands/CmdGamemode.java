package fr.olympa.olympacreatif.commandblocks.commands;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdGamemode extends CbCommand {

	public CmdGamemode(CommandType cmdType, CommandSender sender, Location sendingLoc, OlympaCreatifMain plugin,
			Plot plot, String[] commandString) {
		super(cmdType, sender, sendingLoc, plugin, plot, commandString);
		
		neededPlotLevelToExecute = 1;
	}

	@Override
	public int execute() {
		if (args.length == 0)
			return 0;
		
		GameMode gm = null;
		
		switch(args[0]) {
		case "creative":
		case "1":
			gm = GameMode.CREATIVE;
			break;
			
		case "survival":
		case "0":
			gm = GameMode.SURVIVAL;
			break;
			
		case "2":
		case "adventure":
			gm = GameMode.ADVENTURE;
			break;
			
		case "spectator":
		case "3":
			gm = GameMode.SPECTATOR;
			break;
		}
		
		if (gm == null)
			return 0;
		
		if (args.length == 2)
			targetEntities = parseSelector(args[1], true);
		else
			if (sender instanceof Player)
				targetEntities.add((Player) sender);
		
		for (Entity e : targetEntities)
			((Player) e).setGameMode(gm);
		
		return targetEntities.size();
	}
}
