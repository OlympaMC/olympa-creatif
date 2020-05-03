package fr.olympa.olympacreatif.commandblocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;

public class CbCommand {

	protected OlympaCreatifMain plugin;
	protected Plot plot;
	protected List<Entity> targetEntities;
	protected String[] args;
	
	public CbCommand(OlympaCreatifMain plugin, Plot plot, String[] commandString) {
		this.plugin = plugin;
		this.plot = plot;
	}
	
	protected List<Entity> parseSelector(String s, boolean limitToPlayers){
		List<Entity> list = new ArrayList<Entity>();
//TODO
		return list;
	}
	
	//récupère une localisation dans le plot depuis 3 strings
	protected Location getLocation(String x, String y, String z) {
		Location loc = null;
		
		if (StringUtils.isNumeric(x) && StringUtils.isNumeric(y) && StringUtils.isNumeric(z)){
			loc = new Location(plugin.getWorldManager().getWorld(), Double.valueOf(x), Double.valueOf(y), Double.valueOf(z));
			if (plot.getId().isInPlot(loc))
				return loc;
		}
		
		return null;
	}
	
	public static CbCommand getCommand(OlympaCreatifMain plugin, Plot plot, String fullCommand) {
		String[] args = fullCommand.split(" ");
		CbCommand cmd = new CbCommand(plugin, plot, args);
		
		if (args.length < 3)
			return cmd;
		
		CommandType type = CommandType.get(args[0]);
		
		List list = new ArrayList<String>(Arrays.asList(args));
		list.remove(0);
		args = (String[]) list.toArray();
		
		switch (type) {
		case BOSSBAR:
			cmd = new CmdBossBar(plugin, args);
			break;
		case CLEAR:
			cmd = new CmdClear(plugin, plot, args);
			break;
		case ENCHANT:
			cmd = new CmdEnchant(plugin, plot, args);
			break;
		case EXECUTE:
			cmd = new CmdExecute(plugin, args);
			break;
		case EXPERIENCE:
			cmd = new CmdExperience(plugin, plot, args);
			break;
		case GIVE:
			cmd = new CmdGive(plugin, plot, args);
			break;
		case MSG:
			cmd = new CmdMsg(plugin, plot, args);
			break;
		case SCOREBOARD:
			cmd = new CmdScoreboard(plugin, args);
			break;
		case TEAM:
			cmd = new CmdTeam(plugin, plot, args);
			break;
		case TELEPORT:
			cmd = new CmdTeleport(plugin, plot, args);
			break;
		default:
			break;
		}		
		
		return cmd;
	}
	
	public enum CommandType{
		TELEPORT,
		MSG,
		EXECUTE,
		TEAM,
		SCOREBOARD,
		BOSSBAR,
		CLEAR,
		GIVE,
		ENCHANT,
		EXPERIENCE,
		;
		
		public static CommandType get(String s) {
			for (CommandType cmd : CommandType.values())
				if (cmd.toString().equalsIgnoreCase(s))
					return cmd;
			return null;
		}
	}
	
	public int execute() {
		return 0;
	}
	
}
