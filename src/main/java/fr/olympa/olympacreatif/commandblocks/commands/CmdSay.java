package fr.olympa.olympacreatif.commandblocks.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdSay extends CbCommand {

	private String message = "§7[CB] §r";
	
	public CmdSay(Entity sender, Location loc, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(CommandType.say, sender, loc, plugin, plot, args);

		for (String s : args) 
			message += s + " ";
	}
	
	@Override
	public int execute() {
		plot.getPlayers().forEach(p -> p.sendMessage(ChatColor.translateAlternateColorCodes('&', message)));
		return targetEntities.size();
	}

}
