package fr.olympa.olympacreatif.commandblocks.commands;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.CbObjective;
import fr.olympa.olympacreatif.commandblocks.CbObjective.ObjType;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotPerm.PlotRank;

public class CmdTrigger extends CbCommand {

	public CmdTrigger(Entity sender, Location sendingLoc, OlympaCreatifMain plugin,
			Plot plot, String[] commandString) {
		super(CommandType.trigger, sender, sendingLoc, plugin, plot, commandString);
		
		neededPlotRankToExecute = PlotRank.VISITOR;
		needCbKitToExecute = false;
	}

	@Override
	public int execute() {
		if (args.length != 3 || !(sender instanceof Entity))
			return 0;
		
		CbObjective obj = plotCbData.getObjective(args[0]);
		
		if (obj == null || obj.getType() != ObjType.trigger || !obj.getTriggerAllowedEntities().contains(sender))
			return 0;
		
		Double[] values = getDoubleRange(args[2]);
		
		if (values == null)
			return 0;
		
		switch(args[1]) {
		case "set":
			obj.set((Entity) sender, (int)(double)values[1]);
			break;
			
		case "add":
			obj.add((Entity) sender, (int)(double)values[1]);
			break;
			
		default:
			return 0;
		}
		
		obj.getTriggerAllowedEntities().remove(sender);
		return 1;
	}
}
