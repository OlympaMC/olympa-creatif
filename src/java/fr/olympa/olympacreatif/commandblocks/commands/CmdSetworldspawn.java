package fr.olympa.olympacreatif.commandblocks.commands;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Position;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotParamType;

public class CmdSetworldspawn extends CbCommand {

	private Location loc = null;
	
	public CmdSetworldspawn(Entity sender, Location sendingLoc, OlympaCreatifMain plugin, Plot plot, String[] commandString) {
		super(CommandType.setworldspawn, sender, sendingLoc, plugin, plot, commandString);

		if (args.length != 3)
			return;
		
		loc = parseLocation(args[0], args[1], args[2]);
		
	}

	@Override
	public int execute() {
		
		if (loc == null)
			return 0;
		
		plot.getParameters().setParameter(PlotParamType.SPAWN_LOC, new Position(loc));
		
		return 1;
	}
	
}
