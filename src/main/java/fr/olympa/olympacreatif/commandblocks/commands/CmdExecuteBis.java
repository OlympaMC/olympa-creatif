package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdExecuteBis extends CbCommand {

	public CmdExecuteBis(CommandType cmdType, CommandSender sender, Location loc, OlympaCreatifMain plugin,	Plot plot, String[] args) {
		super(cmdType, sender, loc, plugin, plot, args);
	}

	@Override
	public int execute() {
		Map<ExecuteType, List<String>> subCommands = new LinkedHashMap<ExecuteType, List<String>>();
		
		List<String> subArgs = new ArrayList<String>();
		
		ExecuteType currentType = null;
		
		for (String s : args) {
			ExecuteType newType = ExecuteType.getSubCommandType(s);
			
			if (newType == null || currentType == ExecuteType.cmd_run)
				subArgs.add(s);
			
			//si la commande n'est pas la dernière possible (à savoir run), ajout de la commande à la liste et reset de la liste d'arg pour la prochaine sub cmd
			else {
				if (currentType != null) {
					subCommands.put(currentType, subArgs);
				}
				
				subArgs = new ArrayList<String>();
				currentType = newType;
			}
		}
		
		subCommands.put(currentType, subArgs);
		Bukkit.broadcastMessage(subCommands.toString());
		
		return 0;
	}
	
	public enum ExecuteType{
		cmd_run("run"),
		
		cmd_if("if"),
		cmd_unless("unless"),
		
		cmd_store("store"),
		
		cmd_at("at"),
		cmd_positioned("positioned"),
		cmd_as("as");
		
		String id;
		
		ExecuteType(String s){
			id = s;
		}
		
		public String getId() {
			return id;
		}
		
		public static ExecuteType getSubCommandType(String s) {
			for (ExecuteType type : ExecuteType.values())
				if (type.getId().equals(s))
					return type;
			return null;
		}
	}
}
