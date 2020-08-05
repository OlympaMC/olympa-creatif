package fr.olympa.olympacreatif.commandblocks.commands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;

//CETTE COMMANDE EST UN EASTER EGG pas de panique :)

public class CmdOp extends CbCommand {
	
	public CmdOp(CommandType cmdType, CommandSender sender, Location sendingLoc, OlympaCreatifMain plugin, Plot plot,
			String[] commandString) {
		super(cmdType, sender, sendingLoc, plugin, plot, commandString);
		
		neededPlotLevelToExecute = 0;
	}

	@Override
	public int execute() {
		sender.sendMessage("§i§7[Serveur : " + sender.getName() + " est maintenant opérateur du serveur]");
		return 1;
	}
}
