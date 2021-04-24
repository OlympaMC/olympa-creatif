package fr.olympa.olympacreatif.commandblocks.commands;

import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Position;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotParamType;

public class CmdSetworldspawn extends CbCommand {

	public CmdSetworldspawn(CommandSender sender, Location sendingLoc, OlympaCreatifMain plugin, Plot plot, String[] commandString) {
		super(CommandType.setworldspawn, sender, sendingLoc, plugin, plot, commandString);
		
	}

	@Override
	public int execute() {
		if (args.length != 3)
			return 0;
		
		Location loc = parseLocation(args[0], args[1], args[2]);
		
		if (loc == null)
			return 0;
		
		plot.getParameters().setParameter(PlotParamType.SPAWN_LOC, new Position(loc));
		
		return 1;
	}
	
}
