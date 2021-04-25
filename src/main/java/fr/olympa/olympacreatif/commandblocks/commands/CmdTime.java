package fr.olympa.olympacreatif.commandblocks.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotParamType;

public class CmdTime extends CbCommand {

	public CmdTime(CommandSender sender, Location sendingLoc, OlympaCreatifMain plugin, Plot plot, String[] commandString) {
		super(CommandType.time, sender, sendingLoc, plugin, plot, commandString);
		
	}

	@Override
	public int execute() {
		if (args.length <= 1)
			return 0;
		
		Integer time = null;
		
		switch(args[0]) {
		case "add":
			time = parseTime(args[1]);
			break;

		case "set":
			time = parseTime(args[1]);
			break;
			
		case "query":
			return plot.getParameters().getParameter(PlotParamType.PLOT_TIME);
			
		default:
			return 0;
		}
		
		if (time != null) {
			time += 6000;
			
			PlotParamType.PLOT_TIME.setValue(plot, time);
			for (Player p : plot.getPlayers())
				p.setPlayerTime(time, false);	
		}
		
		return 1;
	}
	
	private Integer parseTime(String s) {
		if (StringUtils.isNumeric(s))
			return Integer.valueOf(s);
		else
			return null;
	}
}
