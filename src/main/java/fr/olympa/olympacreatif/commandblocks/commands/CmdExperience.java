package fr.olympa.olympacreatif.commandblocks.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand.CommandType;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdExperience extends CbCommand {

	private Type type = null;
	private Experience expType = Experience.LEVELS;
	private int definedAmount = 0;
	
	public CmdExperience(CommandSender sender, Location loc, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(CommandType.experience, sender, loc, plugin, plot, args);
		
		if (args.length == 0)
			return;
		
		switch(args[0]) {
		case "add":
			if (args.length >= 3) {
				targetEntities = parseSelector(args[1], true);
				type = Type.ADD;

				if (StringUtils.isNumeric(args[2]))
					definedAmount = (int)(double)Double.valueOf(args[2]);
				
				if (args.length == 4)
					expType = Experience.getExperienceType(args[3]);
				else
					expType = Experience.POINTS;
			} 
			break;
			
			
		case "query":
			if (args.length == 3) {
				targetEntities = parseSelector(args[1], true);
				type = Type.QUERY;
				expType = Experience.getExperienceType(args[2]);
			}
			break;
			
			
		case "set":
			if (args.length >= 3) {
				targetEntities = parseSelector(args[1], true);
				type = Type.SET;
				
				if (StringUtils.isNumeric(args[2]))
					definedAmount = (int)(double)Double.valueOf(args[2]);
				
				if (args.length == 4)
					expType = Experience.getExperienceType(args[3]);
				else
					expType = Experience.POINTS;
			}
			break;
		}
	}

	private enum Type{
		ADD,
		SET,
		QUERY;
	}
	
	private enum Experience{
		POINTS,
		LEVELS;
		
		public static Experience getExperienceType(String s) {
			for (Experience t : Experience.values())
				if (t.toString().equalsIgnoreCase(s))
					return t;
			return LEVELS;
		}
	}
	
	@Override
	public int execute() {
		if (type == null)
			return 0;
		
		switch (type) {
		case ADD:
			for (Entity p : targetEntities)
				if (expType == Experience.LEVELS)
					((Player) p).setLevel(((Player) p).getLevel() + definedAmount);
				else
					((Player) p).setExp(Math.min(((Player) p).getExp() + definedAmount, 1f));
			
			return targetEntities.size();
			
		case QUERY:
			if (targetEntities.size() != 1)
				return 0;
			
			if (expType == Experience.LEVELS)
				return ((Player) targetEntities.get(0)).getLevel();
			else
				return (int) ((Player) targetEntities.get(0)).getExp();
			
		case SET:
			for (Entity p : targetEntities)
				if (expType == Experience.LEVELS)
					((Player) p).setLevel(definedAmount);
				else
					((Player) p).setExp(definedAmount);
			
			return targetEntities.size();
		}
		
		return 0;
	}
}
