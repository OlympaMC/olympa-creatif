package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdExecute extends CbCommand {

	private List<CbCommand> commandsList = new ArrayList<CbCommand>();
	
	public CmdExecute(CommandSender sender, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(sender, plugin, plot, args);
		
	}

}
