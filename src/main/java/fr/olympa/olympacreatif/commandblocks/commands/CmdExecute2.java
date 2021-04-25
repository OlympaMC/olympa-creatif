package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.CbBossBar;
import fr.olympa.olympacreatif.commandblocks.CbObjective;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdExecute2 extends CbCommand {

	public CmdExecute2(CommandSender sender, Location loc, OlympaCreatifMain plugin,	Plot plot, String[] args) {
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
		
		//Map<CommandSender, Location> commandSenders = new HashMap<CommandSender, Location>();
		//Set<Location> sendingLocations = new HashSet<Location>();
		
		Set<CbEntry> senders = new HashSet<CbEntry>();
		
		senders.add(new CbEntry(sender, sendingLoc));
		
		//résultats commande (nombre de résultats = locations * commandSender)
		List<Integer> cmdResults = new ArrayList<Integer>();
		
		//Bukkit.broadcastMessage("arguments : " + subCommands.toString());
		
		for (Entry<ExecuteType, List<String>> subCmd : subCommands.entrySet()) 
			switch (subCmd.getKey()) {
			
			case cmd_as:
				if (subCmd.getValue().size() != 1)
					return 0;

				Set<Location> locsSet = senders.stream().map(e -> e.getValue()).collect(Collectors.toSet());
				senders = new HashSet<CbEntry>();
				
				for (Location loc : locsSet) {
					sendingLoc = loc;
					for (Entity e : parseSelector(subCmd.getValue().get(0), false))
						senders.add(new CbEntry(e, loc));
				}
				
				//ajout de toutes les entités respectant le sélecteur à la liste des commandsenders
				//for (Location loc : sendingLocations) {
					//sendingLoc = loc;
				//}
				//Bukkit.broadcastMessage("Parsed senders : " + senders);
			break;
				
			case cmd_at:
				if (subCmd.getValue().size() != 1)
					return 0;

				Set<CommandSender> sendersSet = senders.stream().map(e -> e.getKey()).collect(Collectors.toSet());
				senders = new HashSet<CbEntry>();
				
				//ajout de toutes les positions des entités du sélecteur à la liste des sending loc
				for (CommandSender s : sendersSet) {
					sender = s;
					
					/*if (s instanceof Entity)
						sendingLoc = ((Entity) s).getLocation();
					else if (s instanceof BlockCommandSender)
						sendingLoc = ((BlockCommandSender) s).getBlock().getLocation();*/
					
					for (Entity e : parseSelector(subCmd.getValue().get(0), false))
						senders.add(new CbEntry(s, e.getLocation()));
				}

				break;
				
			case cmd_positioned:
				if (subCmd.getValue().size() == 2 && subCmd.getValue().get(0).equals("as")) {

					Set<CommandSender> sendersSet2 = senders.stream().map(e -> e.getKey()).collect(Collectors.toSet());
					senders = new HashSet<CbEntry>();

					for (CommandSender s : sendersSet2) {
						sender = s;
						for (Entity e : parseSelector(subCmd.getValue().get(1), false))
							senders.add(new CbEntry(s, e.getLocation()));
					}	
					
				}else if (subCmd.getValue().size() == 3) {
					
					Set<CommandSender> sendersSet2 = senders.stream().map(e -> e.getKey()).collect(Collectors.toSet());
					senders = new HashSet<CbEntry>();
					
					Location loc = parseLocation(subCmd.getValue().get(0), subCmd.getValue().get(1), subCmd.getValue().get(2));
					
					if (loc != null)
						for (CommandSender s : sendersSet2) 
							senders.add(new CbEntry(s, loc));
				}
				
				break;
				
			case cmd_if:
				cmdResults.clear();
				
				Iterator<CbEntry> iterator = senders.iterator();
				
				while (iterator.hasNext()) {
					CbEntry entry = iterator.next();
					
					sender = entry.getKey();
					sendingLoc = entry.getValue();
					
					Integer result = executeIfUnlessTest(subCmd.getValue());
					cmdResults.add(result == null ? 0 : result);
					
					if (result == null || result == 0)
						iterator.remove();
				}	
				break;
				
				
			case cmd_unless:
				cmdResults.clear();
				
				Iterator<CbEntry> iterator2 = senders.iterator();
				
				while (iterator2.hasNext()) {
					CbEntry entry = iterator2.next();
					
					sender = entry.getKey();
					sendingLoc = entry.getValue();
					
					Integer result = executeIfUnlessTest(subCmd.getValue());
					cmdResults.add(result == null ? 0 : result);
					
					if (result == null || result == 1)
						iterator2.remove();
				}			
				break;
				
				
			case cmd_run:
				
				cmdResults.clear();
				
				//Bukkit.broadcastMessage("RUN with : " + senders);
				
				//concat commande à partir de la liste
				String stringCmd = "";
				for (String s : subArgs)
					if (!s.equals("run"))
						stringCmd += s + " ";
				
				for (CbEntry e : senders) {
					sender = e.getKey();
					sendingLoc = e.getValue();
					
					CommandType type = CbCommand.getCommandType(stringCmd);
					if (plot.getCbData().getCommandsTicketsLeft() < type.getRequiredCbTickets())
						return -1;
					
					CbCommand runCmd = CbCommand.getCommand(type, plugin, sender, sendingLoc, stringCmd);
					
					if (runCmd == null)
						return 0;
					
					plot.getCbData().removeCommandTickets(runCmd.getType().getRequiredCbTickets());
					cmdResults.add(runCmd.execute());
				}
				break;
				
			case cmd_store:
				
				//Bukkit.broadcastMessage(cmdResults.size() + " = " + listSenders.size() + " * " + listLocations.size());
				
				//return si le nombre de résultats n'est pas suffisant
				if (cmdResults.size() != senders.size())
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
					
					for (CbEntry e : senders) {
						sender = e.getKey();
						sendingLoc = e.getValue();
						
						i++;
						//pour chaque entité du sélecteur, stockage du résultat précédent dans score obj
						for (Entity ent : parseSelector(subCmdArgs.get(2), false))
							obj.set(ent, cmdResults.size() > i ? cmdResults.get(i) : 0);
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
	
	private class CbEntry extends SimpleEntry<CommandSender, Location> {

		/**
		 * 
		 */
		private static final long serialVersionUID = -2308946095098425283L;

		public CbEntry(CommandSender key, Location value) {
			super(key, value);
		}
		
		@Override
		public boolean equals(Object o) {
			return (o instanceof CbEntry) && ((CbEntry)o).getKey().equals(getKey()) && ((CbEntry)o).getValue().equals(getValue());
		}
	}
}






