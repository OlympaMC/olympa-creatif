package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.CbObjective;
import fr.olympa.olympacreatif.commandblocks.CbTeam;
import fr.olympa.olympacreatif.plot.Plot;

public abstract class CbCommand {

	protected OlympaCreatifMain plugin;
	protected Plot plot;
	protected List<Entity> targetEntities;
	protected String[] args;
	protected CommandSender sender;
	
	protected Location sendingLoc;
	
	//la commande comprend un commandsender, une localisation (imposée par le execute at), le plugin, le plot à la commande est exécutée et les arguments de la commande
	public CbCommand(CommandSender sender, Location sendingLoc, OlympaCreatifMain plugin, Plot plot, String[] commandString) {
		this.plugin = plugin;
		this.plot = plot;
		this.sender = sender;
		this.args = commandString;
		this.sendingLoc = sendingLoc;
	}
	
	//parse le selecteur et ses paramètres : x, y, z, dx, dy, dz, distance, name, team, scores, level, type
	protected List<Entity> parseSelector(String s, boolean limitToPlayers){
		List<Entity> list = new ArrayList<Entity>();
		Map<String, String> parameters = new HashMap<String, String>();
		Location selectorLoc = sendingLoc.clone();

		//variable générale pour les intervalles d'entiers
		Integer[] range = null;
		
		if (s.length() < 2)
			return list;
		
		//cas où un pseudo est passé en paramètre
		if (!s.contains("@")) {
			Player p = Bukkit.getPlayer(s);
			if (p != null)
				list.add(p);
			return list;
		}
		
		//ajout des entités concernées par le test (joueurs (et) entités)
		if (s.startsWith("@s")) //ajout de l'entité exécutant la commande
			if (sender instanceof Entity)
				list.add((Entity) sender);
		else
			list = new ArrayList<Entity>(plot.getPlayers());
		
		if (s.startsWith("@e") && !limitToPlayers)
			list.addAll(plot.getEntities());
		

		//récupération des scores en paramètre
		if (s.contains(",scores={")) {
			if (s.split(",scores={").length != 2)
				return list;
			
			//pour chaque score renseigné, on teste les entités
			for (String ss : s.substring(s.indexOf(",scores={")+9, s.indexOf("}")).split(",")){
				String[] sss = ss.split("=");
				if (sss.length != 2)
					return list;
				
				CbObjective obj = plugin.getCommandBlocksManager().getObjective(plot, sss[0]);
				range = getIntRange(sss[3]);
				
				if (obj != null && range != null) {
					for (Entity e : new ArrayList<Entity>(list))
						if (obj.get(e) < range[0] || obj.get(e) > range[2])
							list.remove(e);
				}
				
			}
		}
			
		
		//extraction des paramètres
		for (String ss : s.replace("[", "").replace("]", "").split(",")) {
			String[] sss = ss.split("="); 
			if (sss.length == 2) {
				
				//regroupe les types d'entités & teams tolérées dans une seule liste du type "!pig,!cow,creeper" ou "equipebleue,!equipemechants"
				if (sss[0].equals("type"))
					if (parameters.containsKey("type"))
						parameters.put("type", parameters.get("type") + "," + sss[1]);
					else
						parameters.put("type", sss[1]);
				
				else if (sss[0].equals("team"))
					if (parameters.containsKey("team"))
						parameters.put("team", parameters.get("team") + "," + sss[1]);
					else
						parameters.put("team", sss[1]);
				
				else
					parameters.put(sss[0], sss[1]);	
			}	
		}

		
		//définition du point d'exécution de la commande
		if (parameters.containsValue("x")) {
			range = getIntRange(parameters.get("x"));
					if (range != null)
						selectorLoc.setX(range[0]);
		}
		
		if (parameters.containsValue("y")) {
			range = getIntRange(parameters.get("y"));
					if (range != null)
						selectorLoc.setX(range[0]);
		}
		
		if (parameters.containsValue("z")) {
			range = getIntRange(parameters.get("z"));
					if (range != null)
						selectorLoc.setX(range[0]);
		}
		
		//exclusion des entités n'étant pas dans la zone de recherche
		if (parameters.containsKey("distance")) {
			range = getIntRange(parameters.get("distance"));
			
			if (range == null)
				return list;
			
			for (Entity ent : new ArrayList<Entity>(list))
				if (ent.getLocation().distance(selectorLoc) < range[0] || ent.getLocation().distance(selectorLoc) > range[1])
					list.remove(ent);
		}else {
			
			//répétition pour x, y et z (test de si l'entité a la composante x, y ou z respectant le dx/y/z)
			if (parameters.containsKey("dx")) {
				range = getIntRange(parameters.get("dx"));
				
				if (range == null)
					return list;

				range[0] = selectorLoc.getBlockX();
				range[1] = selectorLoc.getBlockX() + range[1];
				
				for (Entity e : new ArrayList<Entity>(list))
					if (e.getLocation().getX() < range[0] || e.getLocation().getX() > range[1])
						list.remove(e);	
			}
			

			if (parameters.containsKey("dy")) {
				range = getIntRange(parameters.get("dy"));
				
				if (range == null)
					return list;

				range[0] = selectorLoc.getBlockY();
				range[1] = selectorLoc.getBlockY() + range[1];
				
				for (Entity e : new ArrayList<Entity>(list))
					if (e.getLocation().getY() < range[0] || e.getLocation().getY() > range[1])
						list.remove(e);	
			}
			

			if (parameters.containsKey("dz")) {
				range = getIntRange(parameters.get("dz"));
				
				if (range == null)
					return list;

				range[0] = selectorLoc.getBlockZ();
				range[1] = selectorLoc.getBlockZ() + range[1];
				
				for (Entity e : new ArrayList<Entity>(list))
					if (e.getLocation().getZ() < range[0] || e.getLocation().getZ() > range[1])
						list.remove(e);	
			}
							
		}
		
		//parcours tous les paramètres intermédiaires
		for (Entry<String, String> e : parameters.entrySet())
			switch(e.getKey()) {
			
			case "level":
				range = getIntRange(e.getValue());
				
				if (range == null)
					return list;
				
				for (Entity ent : new ArrayList<Entity>(list))
					
					if (ent instanceof Player) {
						
						if (((Player)ent).getLevel() < range[0] || ((Player)ent).getLevel() > range[1]) {
							list.remove(ent);	
						}	
					}else
						list.remove(ent);
						
				break;
				
			case "name":
				String entityName = "";
				String selectorName = "";
				boolean compareAsInequal = false;
				
				if (e.getValue().contains("!")) {
					compareAsInequal = true;
					selectorName = ChatColor.translateAlternateColorCodes('&', e.getValue().replace("!", ""));
				}else
					selectorName = ChatColor.translateAlternateColorCodes('&', e.getValue().replace("!", ""));
					
				for (Entity ent : new ArrayList<Entity>(list))
					if ((ent.getCustomName().equals(selectorName) && compareAsInequal) || (!ent.getCustomName().equals(selectorName) && !compareAsInequal))
						list.remove(ent);
				
				break;
				
			case "type":
				
				for (String ss : e.getValue().split(",")) {
					
					boolean isTestInequality = false;
					EntityType type = null;
					
					//recherche du type de l'entité
					if (ss.contains("!")) {
						type = EntityType.fromName(e.getValue().replace("!", ""));
						isTestInequality = true;
					}else
						type = EntityType.fromName(e.getValue());						
						
					//test des noms différents entre minecraft et spigot
					if (type == null) {
						switch (e.getValue()) {
						case "mooshroom":
							type = EntityType.MUSHROOM_COW;
							break;
						case "zombie_pigman":
							type = EntityType.PIG_ZOMBIE;
							break;
						default:
							return list;
						}
					}
					
					for (Entity ent : new ArrayList<Entity>(list))
						if ((isTestInequality && ent.getType() == type) || (!isTestInequality && ent.getType() != type))
							list.remove(ent);
							
				}
				break;
				
			case "team":
				
				for (String ss : e.getValue().split(",")) {
				
				boolean isTestInequality = false;
				CbTeam team = null;
				
				//recherche du type de l'entité
				if (ss.contains("!")) {
					team = plugin.getCommandBlocksManager().getTeam(plot, e.getValue().replace("!", ""));
					isTestInequality = true;
				}else
					team = plugin.getCommandBlocksManager().getTeam(plot, e.getValue());
				
				if (team == null)
					return list;
				
				for (Entity ent : new ArrayList<Entity>(list))
					if ((isTestInequality && team.isMember(ent)) || (!isTestInequality && !team.isMember(ent)))
						list.remove(ent);
						
			}
			break;
				
			}
		
		//trie le résultat
		
		//définition des comparateurs
		Comparator<Entity> sortByNearest = new Comparator<Entity>() {
			@Override
			public int compare(Entity o1, Entity o2) {
				return (int) (o1.getLocation().distance(selectorLoc) -  o2.getLocation().distance(selectorLoc));
			}
		};
		
		if (!parameters.containsKey("sort")) {
			if (s.startsWith("@p"))
				parameters.put("sort", "nearest");
			if (s.startsWith("@r"))
				parameters.put("sort", "random");
		}
		
		if (parameters.containsKey("sort"))
			switch(parameters.get("sort")) {
			case "nearest":
				list.sort(sortByNearest);
				break;
			case "furthest":
				list.sort(sortByNearest);
				Collections.reverse(list);
				break;
			case "random":
				List<Entity> listBis = new ArrayList<Entity>();
				
				while (list.size() > 0) 
					listBis.add(list.get(plugin.random.nextInt(list.size())));
				
				list = listBis;
				break;
			}
		
		//limite le nombre de sorties au paramètre "limit" si fourni, ou à 1 en cas de @s ou @p
		if (parameters.containsKey("limit")) {
			range = getIntRange(parameters.get("limit"));
			
			if (range == null)
				return list;
			
			List<Entity> listBis = new ArrayList<Entity>(list);
			Collections.reverse(listBis);
			
			for (Entity e : listBis)
				if (list.size() > range[0])
					list.remove(e);
		}
		
		return list;
	}
	
	//renvoie deux entiers resprésentant les bornes du string (qui doit être sur le modèle 4..7)
	private Integer[] getIntRange(String s) {
		Integer[] response = new Integer[2];
		
		
		String[] ss = s.split("..");
		
		//plage avec min <> max
		if (ss.length == 2) {
			if (StringUtils.isNumeric(ss[0]))
				response[0] = Math.abs((int)(double)Double.valueOf(ss[0]));
			else
				return null;
			
			if (StringUtils.isNumeric(ss[1]))				
				response[1] = Math.abs((int)(double)Double.valueOf(ss[1]));
			else
				return null;
			
		}else
			if (StringUtils.isNumeric(ss[0])) {
				response[0] = Math.abs((int)(double)Double.valueOf(ss[0]));
				response[1] = response[0];
			}else
				return null;
		
		return response;
	}
	
	protected Location getLocation (String x, String y, String z) {
		
		Double xF = getUnverifiedPoint(x, sendingLoc.getX());
		Double yF = getUnverifiedPoint(y, sendingLoc.getY());
		Double zF = getUnverifiedPoint(z, sendingLoc.getZ());		
		 
		if (xF != null && yF != null && zF != null) {
			Location loc = new Location(plugin.getWorldManager().getWorld(), xF, yF, zF); 
			if (plot.getId().isInPlot(loc))
				return loc;
			else
				return null;
		}else
			return null;
	}
	
	//renvoie la coordonnée x, y ou z à partir du string
	private Double getUnverifiedPoint(String s, double potentialVectorValueToAdd) {
		
		if (StringUtils.isNumeric(s))
			return Double.valueOf(s);
		
		if (StringUtils.isNumeric(s.replaceFirst("~", "")))
			return Double.valueOf(s.replaceFirst("~", "")) + potentialVectorValueToAdd;
		
		return null;
	}
	
	public static CbCommand getCommand(OlympaCreatifMain plugin, CommandSender sender, Location loc, String fullCommand) {
		Plot plot = null;
		String[] args = fullCommand.split(" ");

		if (sender instanceof Player) {
			plot = plugin.getPlotsManager().getPlot(((Player) sender).getLocation());	
		}
		if (sender instanceof CommandBlock) {
			plot = plugin.getPlotsManager().getPlot(((CommandBlock) sender).getLocation());	
		}
		
		if (args.length < 3 && plot == null)
			return null;
		
		CbCommand cmd = null;
		
		CommandType type = CommandType.get(args[0]);
		
		List<String> list = new ArrayList<String>(Arrays.asList(args));
		list.remove(0);
		args = (String[]) list.toArray();
		
		switch (type) {
		case BOSSBAR:
			cmd = new CmdBossBar(sender, loc, plugin, plot, args);
			break;
		case CLEAR:
			cmd = new CmdClear(sender, loc, plugin, plot, args);
			break;
		case ENCHANT:
			cmd = new CmdEnchant(sender, loc, plugin, plot, args);
			break;
		case EXECUTE:
			cmd = new CmdExecute(sender, loc, plugin, plot, args);
			break;
		case EXPERIENCE:
			cmd = new CmdExperience(sender, loc, plugin, plot, args);
			break;
		case GIVE:
			cmd = new CmdGive(sender, loc, plugin, plot, args);
			break;
		case MSG:
			cmd = new CmdTellraw(sender, loc, plugin, plot, args);
			break;
		case SCOREBOARD:
			cmd = new CmdScoreboard(sender, loc, plugin, plot, args);
			break;
		case TEAM:
			cmd = new CmdTeam(sender, loc, plugin, plot, args);
			break;
		case TELEPORT:
			cmd = new CmdTeleport(sender, loc, plugin, plot, args);
			break;
		case EFFECT:
			cmd = new CmdEffect(sender, loc, plugin, plot, args);
			break;
		case SUMMON:
			cmd = new CmdSummon(sender, loc, plugin, plot, args);
			break;
		case KILL:
			cmd = new CmdKill(sender, loc, plugin, plot, args);
			break;
		case SAY:
			cmd = new CmdSay(sender, loc, plugin, plot, args);
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
		EFFECT, 
		SUMMON, 
		KILL,
		SAY,
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
