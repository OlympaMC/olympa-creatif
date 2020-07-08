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
import org.bukkit.craftbukkit.v1_15_R1.command.CraftBlockCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.sk89q.jnbt.NBTUtils;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.CbObjective;
import fr.olympa.olympacreatif.commandblocks.CbTeam;
import fr.olympa.olympacreatif.commandblocks.PlotCbData;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.perks.NbtParserUtil;
import fr.olympa.olympacreatif.plot.Plot;
import net.minecraft.server.v1_15_R1.NBTTagCompound;

public abstract class CbCommand {

	protected OlympaCreatifMain plugin;
	protected Plot plot;
	protected PlotCbData plotCbData;
	protected List<Entity> targetEntities = new ArrayList<Entity>();
	protected String[] args;
	protected CommandSender sender;
	
	protected Location sendingLoc;
	protected CommandType cmdType;
	
	//la commande comprend un commandsender, une localisation (imposée par le execute at), le plugin, le plot à la commande est exécutée et les arguments de la commande
	public CbCommand(CommandType cmdType, CommandSender sender, Location sendingLoc, OlympaCreatifMain plugin, Plot plot, String[] commandString) {
		this.plugin = plugin;
		this.plot = plot;
		plotCbData = plot.getCbData();
		this.sender = sender;
		this.args = commandString;
		this.sendingLoc = sendingLoc;
		this.cmdType = cmdType;
	}
	
	//parse le selecteur et ses paramètres : x, y, z, dx, dy, dz, distance, name, team, scores, level, type
	@SuppressWarnings("deprecation")
	protected List<Entity> parseSelector(String s, boolean onlyPlayers){
		String selector = "";
		Map<String, String> params = new HashMap<String, String>(); 
		
		List<Entity> list = new ArrayList<Entity>();
		
		if (s.length() < 2)
			return list;
		
		//si le sélecteur ne contient qu'un pseudo
		if (!s.startsWith("@")){
			Player p = Bukkit.getPlayer(s);
			if (p == null)
				return new ArrayList<Entity>();
			
			list.add(p);
			return list;
		}
		
		//ajout des entités à la liste avant épuration
		list.addAll(plot.getPlayers());
		
		if (!onlyPlayers)
			list.addAll(plot.getEntities());
		
		Bukkit.broadcastMessage("Parse '" + s + " : " + list.toString());
		
		//définition du sélecteur de base et des arguments
		selector = s.substring(0, 2);
		s = s.substring(2);
		
		if (s.length() > 2) {
			s = s.substring(1);
			s = s.substring(0, s.length() - 1) + ",";	
		}
		
		//détecte des params sous forme de NBT pour ne pas en séparer le compound (grâce aux {...})
		boolean isScoreTag = false;
		String subParam = "";
		
		for (char c : s.toCharArray()) {
			
			//détection formatage NBTTagCompound pour ne pas les séparer même s'il y a des virgules
			if (c == '{') 
				isScoreTag = true;
			else if (c == '}')
				isScoreTag = false;
			
			//concat subArg
			if (c != ',' || isScoreTag)
				subParam += c;
			
			//extraction du paramètre et de sa valeur
			else {
				
				if (!subParam.contains("="))
					return list; 
				
				String id = subParam.substring(0, subParam.indexOf("="));
				
				//s'il n'y a rien après le =
				int indexOfEqualSymbol = subParam.indexOf("=");
				if (subParam.length() <= indexOfEqualSymbol + 1)
					return new ArrayList<Entity>();
				
				//regroupement des possibles valeurs pour ce paramètre
				if (!params.containsKey(id))
					params.put(id, subParam.substring(indexOfEqualSymbol + 1));
				else
					params.put(id, params.get(id) + "," + subParam.substring(indexOfEqualSymbol + 1));
				
				subParam = "";
			}
		}
		
		//ajout du tag de tri (si non existant) pour @p et @r
		if (!params.containsKey("sort"))
			if (selector.equals("@p"))
				params.put("sort", "nearest");
			else if (selector.equals("@r"))
				params.put("sort", "random");
		
		//ajout du tag de limite (si non existant) pour @p et @r 
		if (!params.containsKey("limit"))
			if (selector.equals("@p"))
				params.put("limit", "1");
			else if (selector.equals("@r"))
				params.put("limit", "1");
		
		//--------------------------//
		//APPLICATION DES PARAMETRES//
		//--------------------------//
		
		
		//définition de la localisation
		
		if (params.containsKey("x")) {
			Integer[] i = getIntRange(params.get("x"));
			
			if (i != null)
				sendingLoc.setX(i[0]);
		}
		if (params.containsKey("y")) {
			Integer[] i = getIntRange(params.get("y"));
			
			if (i != null)
				sendingLoc.setY(i[0]);
		}
		if (params.containsKey("z")) {
			Integer[] i = getIntRange(params.get("z"));
			
			if (i != null)
				sendingLoc.setZ(i[0]);
		}
		
		if (!plot.getId().isInPlot(sendingLoc))
			return new ArrayList<Entity>();
		
		//épuration selon distance
		
		if (params.containsKey("distance")) {
			Integer[] i = getIntRange(params.get("distance"));
			
			if (i == null)
				return new ArrayList<Entity>();
			
			for (Entity e : new ArrayList<Entity>(list)) {
				double distance = e.getLocation().distance(sendingLoc);
				
				if (distance < i[0] || distance > i[1])
					list.remove(e);
			}
		}
		
		if (params.containsKey("dx")) {
			Integer[] i = getIntRange(params.get("dx"));
			
			if (i == null)
				return new ArrayList<Entity>();
			
			for (Entity e : new ArrayList<Entity>(list)) {
				double distance = e.getLocation().getX() - sendingLoc.getX();
				
				if (i[0] > 0)
					if (distance > i[0])
						list.remove(e);
				if (i[0] < 0)
					if (distance < i[0])
						list.remove(e);
			}
		}
		
		if (params.containsKey("dy")) {
			Integer[] i = getIntRange(params.get("dy"));
			
			if (i == null)
				return new ArrayList<Entity>();
			
			for (Entity e : new ArrayList<Entity>(list)) {
				double distance = e.getLocation().getY() - sendingLoc.getY();
				
				if (i[0] > 0)
					if (distance > i[0])
						list.remove(e);
				if (i[0] < 0)
					if (distance < i[0])
						list.remove(e);
			}
		}
		
		if (params.containsKey("dz")) {
			Integer[] i = getIntRange(params.get("dz"));
			
			if (i == null)
				return new ArrayList<Entity>();
			
			for (Entity e : new ArrayList<Entity>(list)) {
				double distance = e.getLocation().getZ() - sendingLoc.getZ();
				
				if (i[0] > 0)
					if (distance > i[0])
						list.remove(e);
				if (i[0] < 0)
					if (distance < i[0])
						list.remove(e);
			}
		}
		
		//épuration selon niveau d'expérience
		
		if (params.containsKey("level")) {
			Integer[] i = getIntRange(params.get("level"));
			
			if (i == null)
				return new ArrayList<Entity>();

			for (Entity e : new ArrayList<Entity>(list)) {
				if (e.getType() != EntityType.PLAYER)
					list.remove(e);
				
				if (((Player)e).getLevel() < i[0] || ((Player)e).getLevel() > i[1])
					list.remove(e);
			}			
		}
		
		//épuration selon type d'entité
		
		if (params.containsKey("type")) {			
			for (String type : params.get("type").split(",")) {
				String nonType = getNonString(type);
				
				//si on ne veut pas ce cette entité
				if (nonType != null) {
					EntityType entType = EntityType.fromName(getUndomainedString(nonType));
					
					for (Entity e : new ArrayList<Entity>(list)) 
						if (e.getType() == entType)
							list.remove(e);
					
				//si on ne veut que cette entité
				}else {
					EntityType entType = EntityType.fromName(getUndomainedString(type));
					
					for (Entity e : new ArrayList<Entity>(list)) 
						if (e.getType() != entType)
							list.remove(e);
				}
			}
		}
		
		//épuration selon nom entité
		
		if (params.containsKey("name")) {
			for (String name : params.get("name").split(",")) {
				name = ChatColor.translateAlternateColorCodes('&', name);
				String nonName = getNonString(name);
				
				//si on ne veut pas ce ce nom
				if (nonName != null) {
					for (Entity e : new ArrayList<Entity>(list)) 
						
						if (e.getType() == EntityType.PLAYER) {
							if (nonName.equals(((Player)e).getName()))
								list.remove(e);	
						}else
							if (nonName.equals(e.getCustomName()))
								list.remove(e);
					
				//si on ne veut que ce nom d'entité	
				}else 
					for (Entity e : new ArrayList<Entity>(list)) 
						
						if (e.getType() == EntityType.PLAYER) {
							if (!name.equals(((Player)e).getName()))
								list.remove(e);	
						}else
							if (!name.equals(e.getCustomName()))
								list.remove(e);				
			}			
		}
		
		//épuration selon scores
		
		if (params.containsKey("scores")) {
			NBTTagCompound tag = NbtParserUtil.getTagFromString(params.get(params.get("scores")));
			
			for (String key : tag.getKeys()) {
				CbObjective obj = plot.getCbData().getObjective(key);
				Integer[] i = getIntRange(tag.getString(key));
				
				if (obj == null || i == null)
					return new ArrayList<Entity>();
				
				for (Entity e : new ArrayList<Entity>(list))
					if (obj.get(e) < i[0] || obj.get(e) > i[1])
						list.remove(e);
			}
		}
		
		if (params.containsKey("team")) {
			for (String team : params.get("team").split(",")) {
				String nonTeam = getNonString(team);
				
				//si on ne veut pas cette équipe
				if (nonTeam != null) {
					
					CbTeam nonTeamCb = plot.getCbData().getTeamById(nonTeam);
					
					//return si la team n'existe pas ou si le joueur n'as pas entré "team=!"
					if (nonTeamCb == null && !nonTeam.equals(""))
						return new ArrayList<Entity>();
					
					for (Entity e : new ArrayList<Entity>(list)) 
						//remove entité si appartenant à une équipe
						if (nonTeamCb == null) {
							if (plot.getCbData().getTeamOf(e) != null)
								list.remove(e);
						//remove entité si appartenant à l'équipe cible
						}else {
							if (nonTeamCb.isMember(e))
								list.remove(e);
						}					
					
				//si on ne veut que ce nom d'équipe
				}else {
					CbTeam teamCb = plot.getCbData().getTeamById(team);
					if (team == null)
						return new ArrayList<Entity>();
					
					for (Entity e : new ArrayList<Entity>(list)) 
						if (!teamCb.isMember(e))
							list.remove(e);
				}		
			}
		}
		
		//tri des résultats et limitation du nombre de sorties
		
		if (params.containsKey("sort")) {
			switch(params.get("sort")) {
			case "random":
				list.sort(new Comparator<Entity>() {
					@Override
					public int compare(Entity o1, Entity o2) {
						if (plugin.random.nextBoolean())
							return 1;
						else
							return -1;
					}
				});
				break;
			case "nearest":
				list.sort(new Comparator<Entity>() {
					@Override
					public int compare(Entity o1, Entity o2) {
						return (int) (o1.getLocation().distance(sendingLoc) - o2.getLocation().distance(sendingLoc)); 
					}
				});
				break;
			case "furthest":
				list.sort(new Comparator<Entity>() {
					@Override
					public int compare(Entity o1, Entity o2) {
						return (int) (o2.getLocation().distance(sendingLoc) - o1.getLocation().distance(sendingLoc)); 
					}
				});
				break;
			default:
				return new ArrayList<Entity>();
			}
		}
		
		if (params.containsKey("limit")) {
			Integer[] i = getIntRange(params.get("limit"));
			
			if (i == null)
				return new ArrayList<Entity>();
			
			list = list.subList(0, i[0]);
		}
		
		return list;
	}
	
	//renvoie deux entiers resprésentant les bornes du string (qui doit être sur le modèle 4..7)
	public Integer[] getIntRange(String s) {
		
		try {
			Integer[] response = new Integer[2];
			
			String[] ss = s.split("..");
			
			if (ss.length == 1) 
				if (s.startsWith("..")) {
					response[0] = -100000000;
					response[1] = Math.abs((int)(double)Double.valueOf(ss[0]));
				}else if (s.endsWith("..")) {
					response[0] = Math.abs((int)(double)Double.valueOf(ss[0]));
					response[1] = 100000000;
				}else {
					response[0] = Math.abs((int)(double)Double.valueOf(s));
					response[1] = response[0]; 
				}
			else {
				response[0] = Math.abs((int)(double)Double.valueOf(ss[0]));
				response[1] = Math.abs((int)(double)Double.valueOf(ss[1]));
			}
			
			return response;
		}catch(NumberFormatException e) {
			return null;
		}
	}
	
	//retourne le paramètre string moins le '!' s'il en contenait un, null sinon
	public static String getNonString(String value) {
		if (value.startsWith("!"))
			return value.substring(1);
		else
			return null;
	}
	
	
	//retourne un string en majuscules sans le "minecraft:"
	public String getUndomainedString(String s) {
		if (s.contains("minecraft:"))
			return s.substring(9).toUpperCase();
		else
			return s.toUpperCase();
	}
	
	
	//renvoie une localisation absolue ou relative complète (null si err de syntaxe ou si hors du plot)
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
	
	//renvoie la coordonnée x, y ou z à partir du string (en coordonnée absolue ou relative)
	private Double getUnverifiedPoint(String s, double potentialVectorValueToAdd) {
		
		try{
			return Double.valueOf(s);
		}catch(NumberFormatException e) {
		}
		
		if (s.contains("~"))
			if (s.length() >= 2)
				try{
					return Double.valueOf(s.replaceFirst("~", "")) + potentialVectorValueToAdd;	
				}catch(NumberFormatException e) {
					return null;
				}
			else
				return potentialVectorValueToAdd;
				
		return null;
	}
	
	
	public Plot getPlot() {
		return plot;
	}
	
	public CommandType getType() {
		return cmdType;
	}
	
	public static CommandType getCommandType(String cmd) {
		return CommandType.get(cmd.split(" ")[0].replaceFirst("/", ""));
	}
	
	public static CbCommand getCommand(OlympaCreatifMain plugin, CommandSender sender, Location loc, String fullCommand) {
		Plot plot = null;
		String[] args = fullCommand.split(" ");

		if (sender instanceof Entity) {
			plot = plugin.getPlotsManager().getPlot(((Entity) sender).getLocation());	
		}
		if (((sender instanceof CraftBlockCommandSender) && ((CraftBlockCommandSender) sender).getBlock().getState() instanceof CommandBlock)) {
			plot = plugin.getPlotsManager().getPlot(((CraftBlockCommandSender) sender).getBlock().getState().getLocation());	
		}
		
		if (args.length < 2 || plot == null)
			return null;
		
		CbCommand cmd = null;
		
		CommandType type = getCommandType(fullCommand);
		
		List<String> list = new ArrayList<String>(Arrays.asList(args));
		list.remove(0);
		args = list.toArray(new String[list.size()]);
		
		if (type == null)
			return null;
		
		switch (type) {
		case gamemode:
			cmd = new CmdGamemode(type, sender, loc, plugin, plot, args);
			break;
		case gm:
			cmd = new CmdGamemode(type, sender, loc, plugin, plot, args);
			break;
		case bossbar:
			cmd = new CmdBossBar(type, sender, loc, plugin, plot, args);
			break;
		case setblock:
			cmd = new CmdSetblock(type, sender, loc, plugin, plot, args);
			break;
		case clear:
			cmd = new CmdClear(type, sender, loc, plugin, plot, args);
			break;
		case enchant:
			cmd = new CmdEnchant(type, sender, loc, plugin, plot, args);
			break;
		case execute:
			cmd = new CmdExecute(type, sender, loc, plugin, plot, args);
			break;
		case experience:
			cmd = new CmdExperience(type, sender, loc, plugin, plot, args);
			break;
		case give:
			cmd = new CmdGive(type, sender, loc, plugin, plot, args);
			break;
		case tellraw:
			cmd = new CmdTellraw(type, sender, loc, plugin, plot, args);
			break;
		case scoreboard:
			cmd = new CmdScoreboard(type, sender, loc, plugin, plot, args);
			break;
		case team:
			cmd = new CmdTeam(type, sender, loc, plugin, plot, args);
			break;
		case teleport:
			cmd = new CmdTeleport(type, sender, loc, plugin, plot, args);
			break;
		case tp:
			cmd = new CmdTeleport(type, sender, loc, plugin, plot, args);
			break;
		case effect:
			cmd = new CmdEffect(type, sender, loc, plugin, plot, args);
			break;
		case summon:
			cmd = new CmdSummon(type, sender, loc, plugin, plot, args);
			break;
		case kill:
			cmd = new CmdKill(type, sender, loc, plugin, plot, args);
			break;
		case say:
			cmd = new CmdSay(type, sender, loc, plugin, plot, args);
			break;
		default:
			return null;
		}		
		
		return cmd;
	}
	
	public enum CommandType{
		teleport,
		tp,
		tellraw,
		execute,
		team,
		scoreboard,
		bossbar,
		clear,
		give,
		enchant,
		experience, 
		effect, 
		summon, 
		kill,
		say, 
		setblock, 
		gamemode, 
		gm,
		;
		
		public static CommandType get(String s) {
			for (CommandType cmd : CommandType.values())
				if (cmd.toString().equals(s))
					return cmd;
			return null;
		}
	}
	
	public int execute() {
		return 0;
	}
	
}
