package fr.olympa.olympacreatif.commandblocks.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdSay extends CbCommand {

	public CmdSay(CommandSender sender, Location loc, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(sender, loc, plugin, plot, args);
	}
	
	@Override
	public int execute() {
		targetEntities = parseSelector(plot, args[0], true);
		
		String message = "";
		int i = 0;
		for (String s : args) {
			if (i > 0)
				message += s;
			
			i++;
		}
		
		for (Entity e : targetEntities)
			((Player)e).sendMessage(ChatColor.translateAlternateColorCodes('&', message));
		
		return targetEntities.size();
	}

}
