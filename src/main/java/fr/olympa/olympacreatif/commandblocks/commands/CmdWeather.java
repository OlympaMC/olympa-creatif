package fr.olympa.olympacreatif.commandblocks.commands;

import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotParamType;

public class CmdWeather extends CbCommand {

	public CmdWeather(Entity sender, Location sendingLoc, OlympaCreatifMain plugin, Plot plot, String[] commandString) {
		super(CommandType.weather, sender, sendingLoc, plugin, plot, commandString);
		
	}

	@Override
	public int execute() {
		if (args.length == 0)
			return 0;
		
		WeatherType weather = null;

		switch(args[0]) {
		case "clear":
			weather = WeatherType.CLEAR;
			break;
			
		case "rain":
			weather = WeatherType.DOWNFALL;
			break;

		case "thunder":
			weather = WeatherType.DOWNFALL;
			break;
			
		default:
			return 0;
		}
		
		PlotParamType.PLOT_WEATHER.setValue(plot, weather);
		for (Player p : plot.getPlayers())
			p.setPlayerWeather(weather);
		
		return 1;
	}
	
}
