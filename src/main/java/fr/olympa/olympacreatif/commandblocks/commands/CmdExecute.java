package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import com.sk89q.worldedit.function.operation.RunContext;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.CbBossBar;
import fr.olympa.olympacreatif.commandblocks.CbObjective;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdExecute extends CbCommand {

	public CmdExecute(CommandSender sender, Location loc, OlympaCreatifMain plugin,	Plot plot, String[] args) {
		super(CommandType.execute, sender, loc, plugin, plot, args);
	}
	
	@Override
	public int execute() {
		LinkedHashMap<ExecuteType, List<String>> subCommands = new LinkedHashMap<ExecuteType, List<String>>();
		
		//liste utilisée exclusivement pour la génération de la map subCommands
		List<String> subArgs = new ArrayList<String>(10);
		
		ExecuteType currentType = null;
		
		//extraction de chaque subcommand
		for (String s : args) {
			ExecuteType newType = ExecuteType.getSubCommandType(s);
			
			if (newType == null || currentType == ExecuteType.cmd_run || (newType == ExecuteType.cmd_as && currentType == ExecuteType.cmd_positioned))
				subArgs.add(s);
			
			//si la commande n'est pas la dernière possible (à savoir run), ajout de la commande à la liste et reset de la liste d'arg pour la prochaine sub cmd
			else {
				if (currentType != null) 
					subCommands.put(currentType, subArgs);
				
				subArgs = new ArrayList<String>();
				currentType = newType;
			}
		}
		
		//copie de la dernière sous commande du /execute
		if (currentType != null)
			subCommands.put(currentType, subArgs);
		
		//passage de la sous commande store (s'il y en a une) tout à la fin de la liste
		if (subCommands.containsKey(ExecuteType.cmd_store))
			subCommands.put(ExecuteType.cmd_store, subCommands.remove(ExecuteType.cmd_store));
		
		Map<CommandSender, Location> commandSenders = new HashMap<CommandSender, Location>();
		Set<Location> sendingLocations = new HashSet<Location>();
		
		commandSenders.put(sender, null);
		sendingLocations.add(sendingLoc);
		
		//résultats commande (nombre de résultats = locations * commandSender)
		List<Integer> cmdResults = new ArrayList<Integer>();
		
		Bukkit.broadcastMessage("arguments : " + subCommands.toString());
		
		for (Entry<ExecuteType, List<String>> subCmd : subCommands.entrySet()) 
			switch (subCmd.getKey()) {
			
			case cmd_as:
				if (subCmd.getValue().size() != 1)
					return 0;
				
				commandSenders.clear();
				
				//ajout de toutes les entités respectant le sélecteur à la liste des commandsenders
				for (Location loc : sendingLocations) {
					sendingLoc = loc;
					for (Entity e : parseSelector(subCmd.getValue().get(0), false))
						if (!commandSenders.containsKey(e))
							commandSenders.put(e, null);
				}
				break;
				
			case cmd_at:
				if (subCmd.getValue().size() != 1)
					return 0;
				
				sendingLocations.clear();
				
				//ajout de toutes les positions des entités du sélecteur à la liste des sending loc
				for (CommandSender s : commandSenders.keySet()) {
					
					if (s instanceof Entity)
						sendingLoc = ((Entity) s).getLocation();
					else if (s instanceof BlockCommandSender)
						sendingLoc = ((BlockCommandSender) s).getBlock().getLocation();
					
					for (Entity e : parseSelector(subCmd.getValue().get(0), false))
						if (!sendingLocations.contains(e.getLocation()))
							sendingLocations.add(e.getLocation());
				}

				break;
				
			case cmd_positioned:
				if (subCmd.getValue().size() == 2 && subCmd.getValue().get(0).equals("as")) {
					
					for (CommandSender e : commandSenders.keySet()) {
						sender = e;
						
						List<Entity> list = parseSelector(subCmd.getValue().get(1), false);	
						
						if (list.size() > 0)
							commandSenders.put(e, list.get(0).getLocation());
					}
					
					
				}else if (subCmd.getValue().size() == 3) {
					
					for (CommandSender e : commandSenders.keySet()) {
						sender = e;
						
						Location newLoc = parseLocation(subCmd.getValue().get(0), subCmd.getValue().get(1), subCmd.getValue().get(2));
						
						if (newLoc != null)
							commandSenders.put(e, newLoc);
					}
					
					
				}
				
				break;
				
			case cmd_if:
				cmdResults.clear();
				
				for (Location loc : new ArrayList<Location>(sendingLocations)) {
					sendingLoc = loc;
					
					Iterator<Entry<CommandSender, Location>> iter = commandSenders.entrySet().iterator();
					int senderSize = commandSenders.size();
					
					while(iter.hasNext()) {
						
						Entry<CommandSender, Location> e = iter.next();
						
						sender = e.getKey();
						
						if (e.getValue() != null)
							sendingLoc = e.getValue();
						
						Integer result = executeIfUnlessTest(subCmd.getValue());
						
						if (result == null)
							return 0;
						
						if (result == 0)
							if (senderSize == 1)
								sendingLocations.remove(loc);
							else if (sendingLocations.size() == 1)
								iter.remove();
							else {
								sendingLocations.remove(loc);
								iter.remove();
							}
						
						cmdResults.add(result);	
					}
				}				
				break;
				
				
			case cmd_unless:
				cmdResults.clear();
				
				for (Location loc : new ArrayList<Location>(sendingLocations)) {
					sendingLoc = loc;
					
					Iterator<Entry<CommandSender, Location>> iter = commandSenders.entrySet().iterator();
					int senderSize = commandSenders.size();
					
					while(iter.hasNext()) {
						
						Entry<CommandSender, Location> e = iter.next();
						
						sender = e.getKey();
						
						if (e.getValue() != null)
							sendingLoc = e.getValue();
						
						Integer result = executeIfUnlessTest(subCmd.getValue());
						
						if (result == null)
							return 0;
						
						if (result > 0)
							if (senderSize == 1)
								sendingLocations.remove(loc);
							else if (sendingLocations.size() == 1)
								iter.remove();
							else {
								sendingLocations.remove(loc);
								iter.remove();
							}
						
						if (result == 0)
							cmdResults.add(1);
						else
							cmdResults.add(0);
					}
				}				
				break;
				
				
			case cmd_run:
				
				cmdResults.clear();
				
				//Bukkit.broadcastMessage("senders : " + commandSenders.toString() + " - locations : " + sendingLocations.toString());
				
				//concat commande à partir de la liste
				String stringCmd = "";
				for (String s : subArgs)
					if (!s.equals("run"))
						stringCmd += s + " ";
				
				for (Location loc : sendingLocations) {
					sendingLoc = loc;
					for (Entry<CommandSender, Location> e : commandSenders.entrySet()) {
						sender = e.getKey();
						
						if (e.getValue() != null)
							sendingLoc = e.getValue();
						
						if (plot.getCbData().getCommandsTicketsLeft() < CbCommand.getCommandType(stringCmd).getRequiredCbTickets())
							return -1;
						
						CbCommand runCmd = CbCommand.getCommand(plugin, sender, sendingLoc, stringCmd);
						
						if (runCmd == null)
							return 0;
						
						plot.getCbData().removeCommandTickets(runCmd.getType().getRequiredCbTickets());
						cmdResults.add(runCmd.execute());
					}
				}
				break;
				
			case cmd_store:
				
				//Bukkit.broadcastMessage(cmdResults.size() + " = " + listSenders.size() + " * " + listLocations.size());
				
				//return si le nombre de résultats n'est pas suffisant
				if (cmdResults.size() != commandSenders.size() * sendingLocations.size())
					return 0;
				
				List<String> subCmdArgs = subCommands.get(ExecuteType.cmd_store);
				
				if (subCmdArgs.size() == 0)
					return 0;
				
				//support de deux types de store : score et bossbar
				switch (subCmdArgs.get(1)) {
				case "score":
					
					if (subCmdArgs.size() != 4)
						return 0;
					
					CbObjective obj = plotCbData.getObjective(subCmdArgs.get(3));
					
					if (obj == null)
						return 0;
					
					int i = -1;
					
					for (Location loc : sendingLocations) {
						sendingLoc = loc;
						for (Entry<CommandSender, Location> e : commandSenders.entrySet()) {
							sender = e.getKey();
							
							if (e.getValue() != null)
								sendingLoc = e.getValue();
							
							i++;
							//pour chaque entité du sélecteur, stockage du résultat précédent dans score obj
							for (Entity ent : parseSelector(subCmdArgs.get(2), false))
								obj.set(ent, cmdResults.get(i));
						}
					}
					break;
					
					
				case "bossbar":
					
					if (subCmdArgs.size() != 4)
						return 0;
					
					CbBossBar bar = plot.getCbData().getBossBar(subCmdArgs.get(2));
					
					if (bar == null || cmdResults.size() == 0)
						return 0;
					
					switch (subCmdArgs.get(3)) {
					case "value":
						bar.setValue(cmdResults.get(0));
						break;
					case "max":
						bar.setMax(cmdResults.get(0));
					}	
				}
				
				break;
			default:
				return 0;
			}

		//return final
		if (cmdResults.size() == 0)
			return 0;
		else
			return cmdResults.get(cmdResults.size() - 1);
	}
	
	private Integer executeIfUnlessTest(List<String> args) {
		if (args.size() == 0)
			return null;
		
		switch (args.get(0)) {
		
		case "block":

			if (args.size() != 5)
				return null;
			
			//définition de la loc à tester
			Location loc = parseLocation(args.get(1), args.get(2), args.get(3));
			
			if (loc == null)
				return 0;
			
			//définition du type cible
			Material mat = null;
			
			if (args.get(4).split(":").length == 2)
				mat = Material.getMaterial(args.get(4).split(":")[1].toUpperCase());
			else
				mat = Material.getMaterial(args.get(4).toUpperCase());
			
			if (mat == null)
				return null;
			
			//teste si le bloc aux coordonnées spécifiées est bien du type demandé
			if (loc.getBlock().getType().equals(mat))
				return 1;
			else
				return 0;
			
			
		case "blocks":

			if (args.size() != 12)
				return null;
			
			List<BlockData> blocks = new ArrayList<BlockData>();
			
			//définition des 3 points servant de référence à la comparaiosn
			Location loc1 = parseLocation( args.get(2), args.get(3), args.get(4));
			if (loc1 == null)
				return null;
			
			Location loc2 = parseLocation(args.get(5), args.get(6), args.get(7));
			if (loc2 == null)
				return null;
			
			Location finalLoc3 = parseLocation(args.get(8), args.get(9), args.get(10));
			if (finalLoc3 == null)
				return null;

			Location finalLoc1 = new Location(plugin.getWorldManager().getWorld(), Math.min(loc1.getBlockX(), loc2.getBlockX()), Math.min(loc1.getBlockY(), loc2.getBlockY()), Math.min(loc1.getBlockZ(), loc2.getBlockZ()));
			Location finalLoc2 = new Location(plugin.getWorldManager().getWorld(), Math.max(loc1.getBlockX(), loc2.getBlockX()), Math.max(loc1.getBlockY(), loc2.getBlockY()), Math.max(loc1.getBlockZ(), loc2.getBlockZ()));
			Location finalLoc4 = finalLoc3.clone().add(finalLoc2.getBlockX() - finalLoc1.getBlockX(), finalLoc2.getBlockY() - finalLoc1.getBlockY(), finalLoc2.getBlockZ() - finalLoc1.getBlockZ());

			//enregistre les blocks à comparer dans une liste 
			for (int x = finalLoc1.getBlockX() ; x <= finalLoc2.getBlockX() ; x++)
				for (int y = finalLoc1.getBlockY() ; y <= finalLoc2.getBlockY() ; y++)
					for (int z = finalLoc1.getBlockZ() ; z <= finalLoc2.getBlockZ() ; z++)
						blocks.add(plugin.getWorldManager().getWorld().getBlockAt(x, y, z).getBlockData());
			
			int i = 0;
			
			//su un seul des blocks à tester est différent des ceux enregistrés, return false
			for (int x = finalLoc3.getBlockX() ; x <= finalLoc4.getBlockX() ; x++)
				for (int y = finalLoc3.getBlockY() ; y <= finalLoc4.getBlockY() ; y++)
					for (int z = finalLoc3.getBlockZ() ; z <= finalLoc4.getBlockZ() ; z++) { 
						if (!plugin.getWorldManager().getWorld().getBlockAt(x, y, z).getBlockData().equals(blocks.get(i))) {
							return 0;
						}
						i++;
					}
			return 1;
			
			
		case "entity":
			
			if (args.size() == 2)
				return parseSelector(args.get(1), false).size();
			
		case "score":
			
			//test score match
			if (args.size() == 5 && args.get(3).equals("match")) {
				Double[] range = getDoubleRange(args.get(4));
				
				CbObjective obj = plotCbData.getObjective(args.get(2));
				
				List<Entity> targets = parseSelector(args.get(1), false);
				
				if (range == null || obj == null || targets.size() != 1)
					return null;
				
				if (obj.get(targets.get(0)) >= range[0] && obj.get(targets.get(0)) <= range[1])
					return 1;
				else
					return 0;
			}
			
			if (args.size() == 6) {
				CbObjective obj1 = plotCbData.getObjective(args.get(2));
				CbObjective obj2 = plotCbData.getObjective(args.get(5));

				List<Entity> ent1 = parseSelector(args.get(1), false);
				List<Entity> ent2 = parseSelector(args.get(4), false);
				
				if (obj1 == null || obj2 == null || ent1.size() != 1 || ent2.size() != 1)
					return null;
				
				switch (args.get(3)) {
				case "<":
					if (obj1.get(ent1.get(0)) < obj2.get(ent2.get(0)))
						return 1;
					else
						return 0;
				case "<=":
					if (obj1.get(ent1.get(0)) <= obj2.get(ent2.get(0)))
						return 1;
					else
						return 0;
				case "=":
					if (obj1.get(ent1.get(0)) == obj2.get(ent2.get(0)))
						return 1;
					else
						return 0;
				case ">":
					if (obj1.get(ent1.get(0)) > obj2.get(ent2.get(0)))
						return 1;
					else
						return 0;
				case ">=":
					if (obj1.get(ent1.get(0)) >= obj2.get(ent2.get(0)))
						return 1;
					else
						return 0;
				default:
					return null;
				}
			}
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
