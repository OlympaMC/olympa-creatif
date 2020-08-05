package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.CbObjective;
import fr.olympa.olympacreatif.commandblocks.CbTeam;
import fr.olympa.olympacreatif.commandblocks.PlotCbData;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand.CommandType;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;

public abstract class CbCommandI {

	protected OlympaCreatifMain plugin;
	protected Plot plot;
	protected PlotCbData plotCbData;
	protected List<Entity> targetEntities = new ArrayList<Entity>();
	protected String[] args;
	protected CommandSender sender;
	
	protected Location sendingLoc;
	protected CommandType cmdType;

	protected int neededPlotLevelToExecute = PlotRank.CO_OWNER.getLevel();
	
	public CbCommandI(CommandType cmdType, CommandSender sender, Location sendingLoc, OlympaCreatifMain plugin, Plot plot, String[] commandString) {
		this.plugin = plugin;
		this.plot = plot;
		plotCbData = plot.getCbData();
		this.sender = sender;
		this.args = commandString;
		this.sendingLoc = sendingLoc;
		this.cmdType = cmdType;
	}
	
	public int getMinPlotLevelToExecute() {
		return neededPlotLevelToExecute;
	}

	//parse le selecteur et ses paramètres : x, y, z, dx, dy, dz, distance, name, team, scores, level, type
	@SuppressWarnings("deprecation")
	public List<Entity> parseSelector(String s, boolean onlyPlayers){
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
		
		//définition du sélecteur de base
		selector = s.substring(0, 2);
		s = s.substring(2);
		
		//ajout des entités à la liste avant épuration
		if (!selector.equals("@s")) {
			list.addAll(plot.getPlayers());
			
			if (!onlyPlayers && selector.equals("@e"))
				list.addAll(plot.getEntities());	
		}else
			if (sender instanceof Entity)
				list.add((Entity) sender);
		
		//définition des arguments de la commande
		
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
		
		//Bukkit.broadcastMessage("type sélecteur : " + selector + " - params : " + params.toString());
		
		//--------------------------//
		//APPLICATION DES PARAMETRES//
		//--------------------------//
		
		
		//définition de la localisation
		
		if (params.containsKey("x")) {
			Double[] i = getDoubleRange(params.get("x"));
			
			if (i != null)
				sendingLoc.setX(i[0]);
		}
		if (params.containsKey("y")) {
			Double[] i = getDoubleRange(params.get("y"));
			
			if (i != null)
				sendingLoc.setY(i[0]);
		}
		if (params.containsKey("z")) {
			Double[] i = getDoubleRange(params.get("z"));
			
			if (i != null)
				sendingLoc.setZ(i[0]);
		}
		
		if (!plot.getLoc().isInPlot(sendingLoc))
			return new ArrayList<Entity>();
		
		//épuration selon distance
		
		if (params.containsKey("distance")) {
			Double[] i = getDoubleRange(params.get("distance"));
			
			if (i == null)
				return new ArrayList<Entity>();
			
			for (Entity e : new ArrayList<Entity>(list)) {
				double distance = e.getLocation().distance(sendingLoc);
				
				if (distance < i[0] || distance > i[1])
					list.remove(e);
			}
		}
		
		if (params.containsKey("dx")) {
			Double[] i = getDoubleRange(params.get("dx"));
			
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
			Double[] i = getDoubleRange(params.get("dy"));
			
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
			Double[] i = getDoubleRange(params.get("dz"));
			
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
			Double[] i = getDoubleRange(params.get("level"));
			
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
			JsonObject json;

			try {
				json = new JsonParser().parse(params.get("scores")).getAsJsonObject();	
			}catch(Exception e) {
				json = new JsonObject();
			}
			//Bukkit.broadcastMessage("TAG : " + json);
			
			for (Entry<String, JsonElement> elt : json.entrySet()) {
				CbObjective obj = plot.getCbData().getObjective(elt.getKey());
				Double[] i = null;
				if (elt.getValue().isJsonPrimitive())
					i = getDoubleRange(elt.getValue().getAsString());
				
				//Bukkit.broadcastMessage(obj + " " + i);
				//Bukkit.broadcastMessage("list : " + list);
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
					if (teamCb == null)
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
			Double[] i = getDoubleRange(params.get("limit"));
			
			if (i == null)
				return new ArrayList<Entity>();
			
			list = list.subList(0, Math.min((int)(double)i[1], list.size()));
		}
		
		return list;
	}
	
	//renvoie deux entiers resprésentant les bornes du string (qui doit être sur le modèle 4..7)
	public Double[] getDoubleRange(String s) {
		
		if (s == null)
			return null;
		
		try {
			Double[] response = new Double[2];
			
			if (s.contains("..")) {
				if (s.startsWith(".."))
					s = "-10000000" + s;
				if (s.endsWith(".."))
					s = s +"10000000";
				
				String[] ss = s.split("\\.\\.");

				response[0] = Double.valueOf(ss[0]);
				response[1] = Double.valueOf(ss[1]);
			}else {
				response[0] = Double.valueOf(s);
				response[1] = response[0];	
			}

			response[0] = response[0] - 0.5;
			response[1] = response[1] + 0.5;
			
			return response;
		}catch(NumberFormatException e) {
			return null;
		}
	}
	
	
	//retourne le paramètre string moins le '!' s'il en contenait un, null sinon
	public String getNonString(String value) {
		if (value.startsWith("!"))
			return value.substring(1);
		else
			return null;
	}
	
	//retourne un string en majuscules, sans le tag et sans le "minecraft:"
	public static String getUndomainedString(String s) {
		s = s.toUpperCase();
		
		if (s.startsWith("MINECRAFT:"))
			s = s.substring(10);

		//get material
		return s.split("\\{")[0];
	}
}
