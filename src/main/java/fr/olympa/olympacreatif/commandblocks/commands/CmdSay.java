package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand.CommandType;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdSay extends CbCommand {

	public CmdSay(CommandType type, CommandSender sender, Location loc, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(type, sender, loc, plugin, plot, args);
	}
	
	@Override
	public int execute() {
		targetEntities = new ArrayList<Entity>(plot.getPlayers());
		
		String message = "§7[CB] §r";

		for (String s : args) 
			message += s + " ";
		
		
		for (Entity e : targetEntities)
			((Player)e).sendMessage(ChatColor.translateAlternateColorCodes('&', message));
		
		return 1;
	}

}
