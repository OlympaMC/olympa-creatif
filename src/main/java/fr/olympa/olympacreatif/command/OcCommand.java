package fr.olympa.olympacreatif.command;

import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.plot.Plot;

public class OcCommand extends OlympaCommand {

	private OlympaCreatifMain plugin;
	
	public OcCommand(OlympaCreatifMain plugin, String command, String[] alias) {
		super(plugin, command, alias);
		this.plugin = plugin;
	}


	@SuppressWarnings("unused")
	@Cmd(player = true, min=1, syntax = "Syntaxe : /oc find|info")
	public void plotClaim(CommandContext cmd) {
		if (cmd.args[0].equals("find")) {
			int plotCount = 0;
			for (Plot p : plugin.getPlotsManager().getPlots()) {
				if (p.getMembers().getPlayerLevel(cmd.player) > 0)
					plotCount++;
			}
			
			//TODO limite d'obtention de plots
			if (true) {
				cmd.player.teleport(plugin.getPlotsManager().createPlot(cmd.player).getArea().getFirstCorner());
				cmd.player.sendMessage(Message.PLOT_NEW_CLAIM.getValue());
			}else {
				cmd.player.sendMessage(Message.MAX_PLOT_COUNT_REACHED.getValue());
			}
		}
	}



	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		switch (args.length) {
		case 0:
			sender.sendMessage(Message.COMMAND_BASIC.getValue());
			break;
		case 1:
			switch(args[0]) {
			case "help": 
				sender.sendMessage(Message.COMMAND_HELP.getValue());
				break;
			case "find":
				if (!(sender instanceof Player))
					break;
				Plot plot = plugin.getPlotsManager().createPlot((Player) sender);
				((Player) sender).teleport(plot.getArea().getFirstCorner());
			}
			break;
		case 2:
			break;
		}
		return false;
	}



	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}
}
