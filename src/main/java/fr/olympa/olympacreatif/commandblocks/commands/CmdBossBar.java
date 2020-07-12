package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.CbBossBar;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.utils.NbtParserUtil;

public class CmdBossBar extends CbCommand {
	
	public CmdBossBar(CommandType type, CommandSender sender, Location loc, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(type, sender, loc, plugin, plot, args);
	}
	
	@Override
	public int execute() {
		
		CbBossBar bar = null;
		
		switch (args[0]) {
		case "add":
			if (args.length != 3)
				return 0;
			
			BossBar bukkitBar = Bukkit.createBossBar(NbtParserUtil.parseJsonFromCompound(NbtParserUtil.getTagFromString(args[2])), BarColor.WHITE, BarStyle.SOLID);
			
			plotCbData.addBossBar(args[1], new CbBossBar(plugin, bukkitBar));
			return 1;
			
		case "get":
			if (args.length != 3)
				return 0;
			
			bar = plotCbData.getBossBar(args[1]);
			if (bar == null)
				return 0;
			
			switch (args[2]) {
			case "max":
				return bar.getMax();
			case "value":
				return bar.getValue();
			case "players":
				return bar.getBar().getPlayers().size();
			default:
				return 0;
			}
			
		case "list":
			sender.sendMessage("§6  >>>  Bossbars du plot " + plot.getLoc().getId(true) + " <<<");
			for (Entry<String, CbBossBar> e : plot.getCbData().getBossBars().entrySet()) 
				sender.sendMessage("   §e> " + e.getKey() + " (" + e.getValue().getBar().getTitle() + "§r§e) : " + e.getValue().getBar().getPlayers().size() + " joueur(s)");
			
			return 1;
			
		case "remove":
			if (plotCbData.removeBossBar(args[1]))
				return 1;
			else
				return 0;
			
		case "set":
			if (args.length != 4)
				return 0;
			
			bar = plotCbData.getBossBar(args[1]);
			
			if (bar == null)
				return 0;
			
			switch(args[2]) {
			case "color":
				switch (args[3]) {
				case "white":
					bar.getBar().setColor(BarColor.WHITE);
					return 1;
				case "red":
					bar.getBar().setColor(BarColor.RED);
					return 1;
				case "blue":
					bar.getBar().setColor(BarColor.BLUE);
					return 1;
				case "green":
					bar.getBar().setColor(BarColor.GREEN);
					return 1;
				case "pink":
					bar.getBar().setColor(BarColor.PINK);
					return 1;
				case "purple":
					bar.getBar().setColor(BarColor.PURPLE);
					return 1;
				case "yellow":
					bar.getBar().setColor(BarColor.YELLOW);
					return 1;
				default:
					return 0;
				}
				
			case "name":
				bar.getBar().setTitle(NbtParserUtil.parseJsonFromCompound(NbtParserUtil.getTagFromString(args[3])));
				return 1;
				
			case "value":
				int val = 0;
				
				if (StringUtils.isNumeric(args[3])) {
					val = (int) (double) Double.valueOf(args[3]);
					if (val < 0)
						return 0;
				}
				else
					return 0;
				
				bar.setValue(val);
				return 1;
				
			case "max":
				int max = 100;

				if (StringUtils.isNumeric(args[3])) {
					max = (int) (double) Double.valueOf(args[3]);
					if (max <= 0)
						return 0;
				}
				else
					return 0;
				
				bar.setMax(max);
				return 1;
				
				
			case "players":
				
				targetEntities = parseSelector(args[3], true);
				for (Entity p : targetEntities)
					bar.getBar().addPlayer((Player) p);
				
				return targetEntities.size();
				
			case "style":
				if (args.length < 4)
					return 0;
				
				switch (args[3]) {
				case "progress":
					bar.getBar().setStyle(BarStyle.SOLID);
					return 1;
				case "notched_10":
					bar.getBar().setStyle(BarStyle.SEGMENTED_10);
					return 1;
				case "notched_12":
					bar.getBar().setStyle(BarStyle.SEGMENTED_12);
					return 1;
				case "notched_20":
					bar.getBar().setStyle(BarStyle.SEGMENTED_20);
					return 1;
				case "notched_6":
					bar.getBar().setStyle(BarStyle.SEGMENTED_6);
					return 1;
				default:
					return 0;
				}
				
			case "visible":
				switch(args[3]) {
				case "true":
					bar.getBar().setVisible(true);
					return 1;
				case "false":
					bar.getBar().setVisible(false);
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
