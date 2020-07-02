package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdExecuteBis extends CbCommand {

	public CmdExecuteBis(CommandType cmdType, CommandSender sender, Location loc, OlympaCreatifMain plugin,	Plot plot, String[] args) {
		super(cmdType, sender, loc, plugin, plot, args);
	}

	@Override
	public int execute() {
		Map<ExecuteType, List<String>> subCommands = new LinkedHashMap<ExecuteType, List<String>>();
		
		//liste utilisée exclusivement pour la génération de la map subCommands
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
		
		List<CommandSender> listSenders = new ArrayList<CommandSender>();
		List<Location> listLocations = new ArrayList<Location>();
		
		listSenders.add(sender);
		listLocations.add(sendingLoc);
		
		for (Entry<ExecuteType, List<String>> subCmd : subCommands.entrySet()) 
			switch (subCmd.getKey()) {
			
			case cmd_as:
				if (subCmd.getValue().size() != 1)
					return 0;
				
				listSenders.clear();
				
				//ajout de toutes les entités respectant le sélecteur à la liste des commandsenders
				for (Location loc : listLocations) {
					sendingLoc = loc;
					for (Entity e : parseSelector(subCmd.getValue().get(0), false))
						if (!listSenders.contains(e))
							listSenders.add(e);
				}
				break;
				
			case cmd_at:
				if (subCmd.getValue().size() != 1)
					return 0;
				
				listLocations.clear();
				
				//ajout de toutes les positions des entités du sélecteur à la liste des sending loc
				for (CommandSender s : listSenders) {
					
					if (s instanceof Entity)
						sendingLoc = ((Entity) s).getLocation();
					else if (s instanceof BlockCommandSender)
						sendingLoc = ((BlockCommandSender) s).getBlock().getLocation();
					
					for (Entity e : parseSelector(subCmd.getValue().get(0), false))
						if (!listLocations.contains(e.getLocation()))
							listLocations.add(e.getLocation());
				}

				break;
				
			case cmd_positioned:
				if (subCmd.getValue().size() != 3)
					return 0;
				
				List<Location> newLocs = new ArrayList<Location>();
				
				for (Location loc : listLocations) {
					sendingLoc = loc;
					Location newLoc = getLocation(subCmd.getValue().get(0), subCmd.getValue().get(1), subCmd.getValue().get(2));
					
					//si la loc est non nulle (si bien dans le plot)
					if (newLoc != null)
						newLocs.add(newLoc);
				}
				
				listLocations = newLocs;
				
				break;
				
			case cmd_if:
				break;
			case cmd_unless:
				break;
			case cmd_run:
				break;
			case cmd_store:
				break;
			default:
				return 0;
			}
		
		return 0;
	}
	
	private Boolean executeIfUnlessTest(List<String> args) {
		if (args.size() == 0)
			return null;
		
		switch (args.get(0)) {
		
		case "block":

			if (args.size() != 4)
				return null;
			
			//définition de la loc à tester
			Location loc = getLocation(args.get(1), args.get(2), args.get(3));
			if (loc == null)
				return false;

			//définition du type cible
			Material mat = null;
			
			if (args.get(4).split(".").length == 1)
				mat = Material.getMaterial(args.get(4).split(".")[0]);
			else
				mat = Material.getMaterial(args.get(4).split(".")[1]);
			
			if (mat == null)
				return null;
			
			//teste si le bloc aux coordonnées spécifiées est bien du type demandé
			if (loc.getBlock().getType().equals(mat))
				return true;
			else
				return false;
		}
		return null;
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
