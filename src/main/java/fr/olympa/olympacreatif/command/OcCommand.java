package fr.olympa.olympacreatif.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import fr.olympa.api.command.OlympaCommand;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.gui.MainGui;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotId;

public class OcCommand extends OlympaCommand {

	private OlympaCreatifMain plugin;
	
	public OcCommand(OlympaCreatifMain plugin, String command, String[] alias) {
		super(plugin, command, alias);
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		switch (args.length) {
		case 1:
			switch(args[0]) {
			case "help":
				sender.sendMessage(Message.COMMAND_HELP.getValue());
				break;
			case "find":
				if (!(sender instanceof Player))
					break;
				Plot plot = plugin.getPlotsManager().createPlot((Player) sender);
				((Player) sender).teleport(plot.getId().getLocation());
				break;
			case "menu":
				if (sender instanceof Player)
					new MainGui(plugin, (Player) sender).create((Player) sender);
				else
					sender.sendMessage(Message.COMMAND_HELP.getValue());
			default:
				sender.sendMessage(Message.COMMAND_HELP.getValue());
				break;
			}
			break;
		case 2:
			switch(args[0]) {
			case "tp":
				if (PlotId.isIdValid(args[1]) && sender instanceof Player) {
					((Player) sender).teleport(new PlotId(plugin, args[1]).getLocation());
					sender.sendMessage(Message.TELEPORT_IN_PROGRESS.getValue());
				}else {
					sender.sendMessage(Message.INVALID_PLID_ID.getValue());
				}
				break;
			default:
				sender.sendMessage(Message.COMMAND_HELP.getValue());
				break;
			}
			break;
		default:
			sender.sendMessage(Message.COMMAND_HELP.getValue());
			break;
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> list = new ArrayList<String>();
		
		if (args.length == 1) {
			list.add("help");
			list.add("find");
			list.add("menu");
		}
		return list;
	}
}
