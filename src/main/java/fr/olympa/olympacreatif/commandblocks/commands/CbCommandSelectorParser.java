package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.compress.archivers.EntryStreamOffsets;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.EnumUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.CbObjective;
import fr.olympa.olympacreatif.commandblocks.CbTeam;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand.CommandType;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotCbData;
import fr.olympa.olympacreatif.plot.PlotPerm;
import fr.olympa.olympacreatif.plot.PlotPerm.PlotRank;

public abstract class CbCommandSelectorParser {

	protected OlympaCreatifMain plugin;
	protected Plot plot;
	protected PlotCbData plotCbData;
	protected List<Entity> targetEntities = new ArrayList<Entity>();
	protected String[] args;
	protected CommandSender sender;
	
	protected Location sendingLoc;
	protected CommandType cmdType;

	protected PlotRank neededPlotRankToExecute = PlotPerm.EXECUTE_CB_CMD.getRank();
	protected boolean needCbKitToExecute = true;
	
	public CbCommandSelectorParser(CommandType cmdType, CommandSender sender, Location sendingLoc, OlympaCreatifMain plugin, Plot plot, String[] commandString) {
		this.plugin = plugin;
		this.plot = plot;
		plotCbData = plot.getCbData();
		this.sender = sender;
		this.args = commandString;
		this.sendingLoc = sendingLoc;
		this.cmdType = cmdType;
	}
	
	public PlotRank getMinRankToExecute() {
		return neededPlotRankToExecute;
	}
	
	public boolean needCbKitToExecute() {
		return needCbKitToExecute;
	}

	@SuppressWarnings("deprecation")
	private static final ImmutableSet<Entry<String, SelectorFunction>> selectorParametersFunctions = ImmutableMap.<String, SelectorFunction>builder()
			.put("gamemode", (plot, loc, stream, param) -> 
				EnumUtils.isValidEnum(GameMode.class, param.toUpperCase()) ? 
				stream.filter(e -> e.getType() == EntityType.PLAYER).filter(e -> ((Player)e).getGameMode() == GameMode.valueOf(param)) : 
				EnumUtils.isValidEnum(GameMode.class, getNonString(param.toUpperCase())) ? 
				stream.filter(e -> e.getType() == EntityType.PLAYER).filter(e -> ((Player)e).getGameMode() == GameMode.valueOf(getNonString(param.toUpperCase()))) : 
				stream)
			
			.put("sort", (plot, loc, stream, param) -> 
				param == "nearest" ? 
				stream.sorted(Comparator.comparingDouble(e -> e.getLocation().distance(loc))) : 
				param == "furthest" ? 
				stream.sorted(Comparator.comparingDouble(e -> -e.getLocation().distance(loc))) : 
				stream.sorted(Comparator.comparingInt(i -> ThreadLocalRandom.current().nextInt())))

			.put("x", (plot, loc, stream, param) -> {if (getInt(param) != null) loc.setX(getInt(param)); return stream;})
			.put("y", (plot, loc, stream, param) -> {if (getInt(param) != null) loc.setY(getInt(param)); return stream;})
			.put("z", (plot, loc, stream, param) -> {if (getInt(param) != null) loc.setZ(getInt(param)); return stream;})

			.put("dx", (plot, loc, stream, param) -> {
				Double[] range = getDoubleRange(param);
				if (range == null)
					return stream.limit(0);
				
				return stream.filter(e -> {
					double distance = e.getLocation().getX() - loc.getX();
					return distance > range[0] && distance < range[1];
				});
			})
			.put("dy", (plot, loc, stream, param) -> {
				Double[] range = getDoubleRange(param);
				if (range == null)
					return stream.limit(0);
				
				return stream.filter(e -> {
					double distance = e.getLocation().getY() - loc.getY();
					return distance > range[0] && distance < range[1];
				});
			})
			.put("dz", (plot, loc, stream, param) -> {
				Double[] range = getDoubleRange(param);
				if (range == null)
					return stream.limit(0);
				
				return stream.filter(e -> {
					double distance = e.getLocation().getZ() - loc.getZ();
					return distance > range[0] && distance < range[1];
				});
			})
			
			.put("distance", (plot, loc, stream, param) -> {
				Double[] limits = getDoubleRange(param);
				if (limits == null)
					return stream.limit(0);

				return stream.filter(e -> {
					double distance = e.getLocation().distance(loc);
					return distance >= limits[0] && distance <= limits[1];	
				});
			})
			
			.put("level", (plot, loc, stream, param) -> {
				Double[] range = getDoubleRange(param);
				if (range == null)
					return stream.limit(0);					
				return stream.filter(e -> e.getType() == EntityType.PLAYER).filter(e -> ((Player)e).getLevel() > range[0] && ((Player)e).getLevel() < range[1]); })
			
			.put("type", (plot, loc, stream, param) ->
				getNonString(param) == null ?
				stream.filter(e -> e.getType() == EntityType.fromName(param.toUpperCase())) :
				stream.filter(e -> e.getType() == EntityType.fromName(getNonString(param.toUpperCase()))))
			
			.put("name", (plot, loc, stream, param) -> 
				getNonString(param) == null ? 
				stream.filter(e -> e.getType() == EntityType.PLAYER ? param.equals(((Player)e).getName()) : param.equals(e.getCustomName())) :
				stream.filter(e -> e.getType() == EntityType.PLAYER ? !param.equals(((Player)e).getName()) : !param.equals(e.getCustomName())))
			
			.put("scores", (plot, loc, stream, param) -> {
				String[] parts = param.split(",");
				for (String part : parts) {
					String[] objParts = part.split("=");
					if (objParts.length != 2)
						continue;
					
					Double[] range = getDoubleRange(objParts[1]);
					if (range == null)
						continue;
					
					CbObjective obj = plot.getCbData().getObjective(objParts[0]);
					if (obj == null)
						continue;
					
					stream = stream.filter(e -> obj.get(e) >= range[0] && obj.get(e) <= range[1]);
				}
				
				return stream;
			})
			
			.put("team", (plot, loc, stream, param) -> {
				if (param == "!")
					return stream.filter(e -> plot.getCbData().getTeamOf(e) == null);
				else if (param == "")
					return stream.filter(e -> plot.getCbData().getTeamOf(e) != null);
				
				CbTeam team = getNonString(param) == null ? plot.getCbData().getTeamById(param) : plot.getCbData().getTeamById(getNonString(param));
				if (team == null)
					return stream.limit(0);
				return stream.filter(e -> getNonString(param) == null ? team.isMember(e) : !team.isMember(e));
			})
			
			.put("limit", (plot, loc, stream, param) -> getInt(param) == null ? stream.limit(0) : stream.limit(getInt(param)))
			
			//.put("", (plot, loc, stream, param) -> null)
			.build().entrySet();
	
	@FunctionalInterface
	private interface SelectorFunction {
		public Stream<Entity> apply(Plot plot, Location loc, Stream<Entity> stream, String paramValue);
	}
	
	public List<Entity> parseSelector(String s, boolean onlyPlayers) {
		if (s == null)
			return new ArrayList<Entity>();
		
		if (s.equals("@s"))
			return (sender instanceof Entity) ? Arrays.asList(new Entity[]{(Entity) sender})  : new ArrayList<Entity>();
		
		if (!s.startsWith("@") && plot.getPlayers().contains(Bukkit.getPlayer(s)))
			return new ArrayList<Entity>(Arrays.asList(Bukkit.getPlayer(s)));
		
		if (s.length() < 2)
			return new ArrayList<Entity>();
		
		Set<Entity> ents = (s.startsWith("@e") && !onlyPlayers) ? new HashSet<Entity>(plot.getEntities()) : new HashSet<Entity>(plot.getPlayers().size());
		ents.addAll(plot.getPlayers());
		
		HashMultimap<String, String> selectorParams = HashMultimap.create();
		
		String key = "";
		String value = null;
		int bracketsScore = 0;
		boolean isInBrackets = false;
		
		for (char c : s.substring(2).replace("[", "").replace("]", "").toCharArray())  {
			
			if (c == '{')
				bracketsScore++;
			else if (c == '}')
				bracketsScore--;
			else if (c == '"' && !isInBrackets)
				isInBrackets = true;
			else if (c == '"' && isInBrackets)
				isInBrackets = false;
			else if (c == '=' && bracketsScore == 0 && !isInBrackets)
				value = "";
			else if (c == ',' && bracketsScore == 0 && !isInBrackets) {
				selectorParams.put(key, value);
				key = "";
				value = null;
			}else
				if (value == null)
					key += c;
				else
					value += c;
		}
		
		if (value != null && bracketsScore == 0 && !isInBrackets)
			selectorParams.put(key, value);
		
		switch(s.substring(0, 2)) {
		case "@r":
			if (!selectorParams.containsKey("limit"))
				selectorParams.put("limit", "1");

			if (!selectorParams.containsKey("sort"))
				selectorParams.put("sort", "random");
			break;
			
		case "@p":
			if (!selectorParams.containsKey("limit"))
				selectorParams.put("limit", "1");

			if (!selectorParams.containsKey("sort"))
				selectorParams.put("sort", "nearest");
			break;
		}

		Stream<Entity> entitiesStream = ents.stream();
		
		int iterations = 0;
		for (Entry<String, SelectorFunction> e : selectorParametersFunctions)
			for (String paramValue : selectorParams.removeAll(e.getKey())) {
				entitiesStream = e.getValue().apply(plot, sendingLoc, entitiesStream, paramValue);

				if (iterations++ > 10)
					return entitiesStream.collect(Collectors.toList());
			}
					
					/*
			selectorParams.removeAll(e.getKey()).forEach(paramValue -> {
				entitiesStream = e.getValue().apply(plot, sendingLoc, entitiesStream, paramValue);
				iterations++;
				
				if (iterations > 10)
					break;
			});*/
				
		return entitiesStream.collect(Collectors.toList());
	}
	
	//renvoie deux entiers resprésentant les bornes du string (qui doit être sur le modèle 4..7)
	protected static Double[] getDoubleRange(String s) {
		
		if (s == null)
			return null;
		
		try {
			Double[] response = new Double[] {null, null};
			
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
	
	
	protected static Integer getInt(String value) {
		for (char c : value.toCharArray())
			if (c < '0' || c > '9')
				return null;
		
		return Integer.valueOf(value);
	}
	
	
	//retourne le paramètre string moins le '!' s'il en contenait un, null sinon
	protected static String getNonString(String value) {
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

		//return block name sans tags nbt
		int splitIndex = s.indexOf("{");
		if (splitIndex > 0)
			return s.substring(0, splitIndex);
		else
			return s;
	}
}
