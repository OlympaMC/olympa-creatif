package fr.olympa.olympacreatif.commandblocks.commands;

import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Position;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotParamType;

public class CmdSpreadplayers extends CbCommand {

	public CmdSpreadplayers(CommandSender sender, Location sendingLoc, OlympaCreatifMain plugin, Plot plot, String[] commandString) {
		super(CommandType.spreadplayers, sender, sendingLoc, plugin, plot, commandString);
		
	}

	@Override
	public int execute() {
		sender.sendMessage("§cLe /spreadplayers n'est pas encore prêt.");
		
		return 0;
	}
	
}
