package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.perks.NbtEntityParser;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdBossBar extends CbCommand {
	
	public CmdBossBar(CommandSender sender, Location loc, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(sender, loc, plugin, plot, args);
	}
	
	@Override
	public int execute() {
		
		BossBar bar = null;
		
		switch (args[0]) {
		case "add":
			if (args.length != 3)
				return 0;
			
			plot.addBossBar(args[1], Bukkit.createBossBar(NbtEntityParser.parseJsonText(args[2]), BarColor.WHITE, BarStyle.SOLID));
			return 1;
			
		case "get":
			if (args.length != 3)
				return 0;
			
			bar = plot.getBossBar(args[1]);
			if (bar == null)
				return 0;
			
			switch (args[2]) {
			case "max":
				return 100;
			case "value":
				return (int) bar.getProgress();
			case "players":
				return bar.getPlayers().size();
			default:
				return 0;
			}
			
		case "list":
			sender.sendMessage("§6  >>>  Bossbars du plot " + plot.getId().getAsString() + " <<<");
			for (Entry<String, BossBar> e : plot.getBossBars().entrySet()) 
				sender.sendMessage("   §e> " + e.getKey() + " (" + e.getValue().getTitle() + "§r§e) : " + e.getValue().getPlayers().size() + " joueur(s)");
			
			return plot.getBossBars().size();
			
		case "remove":
			if (plot.removeBossBar(args[1]))
				return 1;
			else
				return 0;
			
		case "set":
			if (args.length != 4)
				return 0;
			
			bar = plot.getBossBar(args[1]);
			
			if (bar == null)
				return 0;
			
			switch(args[2]) {
			case "color":
				switch (args[3]) {
				case "white":
					bar.setColor(BarColor.WHITE);
					return 1;
				case "red":
					bar.setColor(BarColor.RED);
					return 1;
				case "blue":
					bar.setColor(BarColor.BLUE);
					return 1;
				case "green":
					bar.setColor(BarColor.GREEN);
					return 1;
				case "pink":
					bar.setColor(BarColor.PINK);
					return 1;
				case "purple":
					bar.setColor(BarColor.PURPLE);
					return 1;
				case "yellow":
					bar.setColor(BarColor.YELLOW);
					return 1;
				default:
					return 0;
				}
				
			case "name":
				bar.setTitle(NbtEntityParser.parseJsonText(args[3]));
				return 1;
				
			case "players":
				targetEntities = parseSelector(plot, args[3], true);
				for (Entity p : targetEntities)
					bar.addPlayer((Player) p);
				
				return targetEntities.size();
				
			case "style":
				if (args.length < 4)
					return 0;
				
				switch (args[3]) {
				case "progress":
					bar.setStyle(BarStyle.SOLID);
					return 1;
				case "notched_10":
					bar.setStyle(BarStyle.SEGMENTED_10);
					return 1;
				case "notched_12":
					bar.setStyle(BarStyle.SEGMENTED_12);
					return 1;
				case "notched_20":
					bar.setStyle(BarStyle.SEGMENTED_20);
					return 1;
				case "notched_6":
					bar.setStyle(BarStyle.SEGMENTED_6);
					return 1;
				default:
					return 0;
				}
				
			case "visible":
				switch(args[3]) {
				case "true":
					bar.setVisible(true);
					return 1;
				case "false":
					bar.setVisible(false);
					return 1;
				default:
				return 0;
				}
			default:
				return 0;
			}
			
		default:
			return 0;
		}
	}

}
