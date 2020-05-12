package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdTeleport extends CbCommand {
	
	private List<Location> tpPoints = new ArrayList<Location>();
	
	public CmdTeleport(CommandSender sender, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(sender, plugin, plot, args);
		
		switch(args.length) {
		case 2:
			targetEntities = parseSelector(plot, args[0], false);
			for (Entity e : parseSelector(plot, args[1], false))
				tpPoints.add(e.getLocation());
			break;
			
		case 4:
			targetEntities = parseSelector(plot, args[0], false);
			Location loc = getLocation(sender, args[1], args[2], args[3]);
			if (loc != null)
				tpPoints.add(loc);
			break;
		}
	}
	
	@Override
	public int execute() {
		if (tpPoints.size() == 0)
			return 0;
		
		int i = 0;
		
		for (Entity e : targetEntities) {
			i++;
			int rnd = plugin.random.nextInt(tpPoints.size());
			e.teleport(tpPoints.get(rnd));
		}
		return i;
	}
}