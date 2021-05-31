package fr.olympa.olympacreatif.commandblocks.commands;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdSpreadplayers extends CbCommand {

	public CmdSpreadplayers(Entity sender, Location sendingLoc, OlympaCreatifMain plugin, Plot plot, String[] commandString) {
		super(CommandType.spreadplayers, sender, sendingLoc, plugin, plot, commandString);
		
	}

	@Override
	public int execute() {
		sender.sendMessage("§cLe /spreadplayers n'est pas encore prêt.");
		
		return 0;
	}
	
}
