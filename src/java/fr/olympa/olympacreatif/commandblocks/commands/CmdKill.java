package fr.olympa.olympacreatif.commandblocks.commands;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.FakePlayerDeathEvent;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdKill extends CbCommand {

	public CmdKill(Entity sender, Location loc, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(CommandType.kill, sender, loc, plugin, plot, args);
	}
	
	@Override
	public int execute() {
		if (args.length != 1)
			return 0;
		
		targetEntities = parseSelector(args[0], false);
		
		for (Entity e : targetEntities)
			if (e instanceof Player)
				FakePlayerDeathEvent.fireFakeDeath(plugin, (Player) e, null, 999999999, plot);
			else 
				plot.removeEntityInPlot(e, true);
			
		return targetEntities.size();
	}

}
