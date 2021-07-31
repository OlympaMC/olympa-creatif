package fr.olympa.olympacreatif.commands;

import java.util.Arrays;
import java.util.List;

import fr.olympa.olympacreatif.plot.PlotParamType;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotPerm;

public class SpeedCommand extends OlympaCommand {

	public SpeedCommand(OlympaCreatifMain plugin) {
		super(plugin, "speed", "Définir ta vitesse de vol", null, new String[] {});
		allowConsole = false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length != 1 || !StringUtils.isNumeric(args[0])) {
			sendIncorrectSyntax("/speed 1|2|...|9");
			return false;
		}
		Plot plot = ((OlympaPlayerCreatif) getOlympaPlayer()).getCurrentPlot();
		
		if (plot != null && (
				!PlotPerm.DEFINE_OWN_FLY_SPEED.has(plot, getOlympaPlayer()) ||
				!plot.getParameters().getParameter(PlotParamType.ALLOW_FLY_INCOMING_PLAYERS))) {

			OCmsg.INSUFFICIENT_PLOT_PERMISSION.send(getPlayer(), PlotPerm.DEFINE_OWN_FLY_SPEED);
			return false;
		}
		
		float level = 0.1f;

		level = Math.min(Math.max(Float.valueOf(args[0])/18f, 0.1f), 1f);
		
		getPlayer().setFlySpeed(level);
		OCmsg.OCO_SET_FLY_SPEED.send(getPlayer(), args[0]);
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9");
	}

}
