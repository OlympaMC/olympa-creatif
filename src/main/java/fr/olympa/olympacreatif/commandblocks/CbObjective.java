package fr.olympa.olympacreatif.commandblocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;

public class CbObjective {

	private OlympaCreatifMain plugin;
	private Plot plot;
	
	private ObjType type = null;
	private Object typeParam =null; //contient éventuellement le second paramètre de l'objectif (par exemple, killed_by:minecraft.zombie
	
	private DisplaySlot displayLoc = null;
	
	private Map<String, Integer> values = new HashMap<String, Integer>();
	
	private String objId = "";
	private String objName = "";
	
	public CbObjective(OlympaCreatifMain plugin, Plot plot, String objType, String id, String objName) {
		this.plugin = plugin;
		this.plot = plot;
		this.objId = id.substring(0, Math.min(15, id.length()));
		this.objName = objName;
		type = ObjType.get(objType);
		
		if (type == null)
			return;
		
		String typeParamString = null;
		
		if (objType.contains(":")) {
			String param = objType.split(":")[1];
			
			if (param.contains("."))
				typeParamString = param.substring(Math.min(10, param.length()));
			else
				typeParamString = param;
		}
		
		//récupération paramètre secondaire du type de l'obj
		switch (type) {
		case killedByTeam:
			if (objType.split(".").length == 2)
				//TODO
			break;
		case teamkill:
			if (objType.split(".").length == 2)
				//TODO
			break;
		case minecraft_broken:
			if (typeParamString == null)
				return;
			
			typeParam = Material.getMaterial(typeParamString.toUpperCase());
			break;
		case minecraft_crafted:
			if (typeParamString == null)
				return;
			
			typeParam = Material.getMaterial(typeParamString.toUpperCase());
			break;
		case minecraft_dropped:
			if (typeParamString == null)
				return;
			
			typeParam = Material.getMaterial(typeParamString.toUpperCase());
			break;
		case minecraft_killed:
			if (typeParamString == null)
				return;
			
			typeParam = EntityType.fromName(typeParamString.toUpperCase());
			break;
		case minecraft_killed_by:
			if (typeParamString == null)
				return;
			
			typeParam = EntityType.fromName(typeParamString.toUpperCase());
			break;
		case minecraft_mined:
			if (typeParamString == null)
				return;
			
			typeParam = Material.getMaterial(typeParamString.toUpperCase());
			break;
		case minecraft_picked_up:
			if (typeParamString == null)
				return;
			
			typeParam = Material.getMaterial(typeParamString.toUpperCase());
			break;
		case minecraft_used:
			if (typeParamString == null)
				return;
			
			typeParam = Material.getMaterial(typeParamString.toUpperCase());
			break;
		}
	}

	
	public Plot getPlot() {
		return plot;
	}
	
	public ObjType getType() {
		return type;
	}
	
	public String getId() {
		return objId;
	}
	
	public String getName() {
		return objName;
	}

	public void setName(String newObjName) {
		this.objName = newObjName;
	}
	
	public Object getParamType() {
		return typeParam;
	}
	
	public Map<String, Integer> getValues(boolean sortValues){
		if (!sortValues)
			return values;
	   
		List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(values.entrySet());
	   
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {

			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
	   });
	
	   Map<String, Integer> result = new LinkedHashMap<String, Integer>();
	   for (Entry<String, Integer> e : list) {
		   result.put(e.getKey(), e.getValue());
	   }
	   
	   return result;
	}
	
	public void add(String name, int value) {
		if (!values.containsKey(name))
			set(name, value);
		else
			set(name, values.get(name) + value);
	}
	
	//gestion sidebar/belowname ici
	public void set(String name, int value) {
		values.put(name, value);
		
		//affichage scoreboard sidebar
		if (displayLoc == DisplaySlot.SIDEBAR) {
			Objective obj = plugin.getCommandBlocksManager().getObjectiveOnSlot(plot, displayLoc);
			obj.getScore(name).setScore(value);
		}
	}
	
	public void reset(String name) {
		values.remove(name);
	}
	
	public int get(String name) {
		if (values.containsKey(name))
			return values.get(name);
		else
			return 0;
	}
	
	public void add(Entity e, int value) {
		set (e, value + get(e));
	}
	
	public void set(Entity e, int value) {
		
		//définition string portant le score
		String scoreHolder = "";
		
		if (e instanceof Player)
			scoreHolder = ((Player) e).getDisplayName();
		else
			scoreHolder = e.getCustomName();
		
		//affichage scoreboard belowName
		if (displayLoc == DisplaySlot.BELOW_NAME)
			if (plugin.getPerksManager().getLinesOnHeadUtil().getLinesCount(e) == 2)
				plugin.getPerksManager().getLinesOnHeadUtil().setLine(e, 1, value + " " + objName, true);
			else
				plugin.getPerksManager().getLinesOnHeadUtil().setLine(e, 2, value + " " + objName, true);
		
		set(scoreHolder, value);
	}
	
	public void reset(Entity e) {
		
		//utilisation du set pour réinitialiser l'affichage du scoreboard
		set(e, 0);
		
		if (e instanceof Player)
			values.remove(((Player) e).getDisplayName());
		else
			values.remove(e.getCustomName());
	}
	
	public int get(Entity e) {
		if (e instanceof Player)
			return get(((Player) e).getDisplayName());
		else
			return get(e.getCustomName());
	}
	
	//méthodes d'affichage en sidebar et belowname
	public void setDisplaySlot(DisplaySlot newDisplayLoc) {
		if (newDisplayLoc != null)
			for (CbObjective o : plugin.getCommandBlocksManager().getObjectives(plot))
				if (o.getDisplaySlot() == newDisplayLoc)
					o.setDisplaySlot(null);
		
		//clear l'emplacement si nécessaire
		if (displayLoc != null && newDisplayLoc == null)
			plugin.getCommandBlocksManager().clearScoreboardSlot(plot, displayLoc);
		
		//affichage du score sur la sidebar
		if (displayLoc != newDisplayLoc && newDisplayLoc == DisplaySlot.SIDEBAR) {
			Objective obj = plugin.getCommandBlocksManager().getObjectiveOnSlot(plot, newDisplayLoc);
			obj.setDisplayName(objId);
			
			for (Entry<String, Integer> e : values.entrySet()) 
				obj.getScore(e.getKey()).setScore(e.getValue());
		
		//affichage du score sur le belowName
		}else if (displayLoc != newDisplayLoc && newDisplayLoc == DisplaySlot.BELOW_NAME) {
			for (Player p : plot.getPlayers()) {
				//affichage nom entité
				plugin.getPerksManager().getLinesOnHeadUtil().setLine(p, 0, p.getDisplayName(), true);
				
				//affichage score entité
				if (plugin.getPerksManager().getLinesOnHeadUtil().getLinesCount(p) == 2)
					plugin.getPerksManager().getLinesOnHeadUtil().setLine(p, 1, get(p) + " " + getName(), false);
				else
					plugin.getPerksManager().getLinesOnHeadUtil().setLine(p, 2, get(p) + " " + getName(), false);
			}
			
			for (Entity e : plot.getEntities()) {
				plugin.getPerksManager().getLinesOnHeadUtil().setLine(e, 0, e.getCustomName(), true);
				
				if (plugin.getPerksManager().getLinesOnHeadUtil().getLinesCount(e) == 2)
					plugin.getPerksManager().getLinesOnHeadUtil().setLine(e, 2, get(e) + " " + getName(), false);
				else
					plugin.getPerksManager().getLinesOnHeadUtil().setLine(e, 1, get(e) + " " + getName(), false);
			}
		}
		
		this.displayLoc = newDisplayLoc;		
	}


	public DisplaySlot getDisplaySlot() {
		return displayLoc;
	}

	public enum ObjType{
		dummy,
		deathCount,
		playerKillCount,
		totalKillCount,
		health,
		xp,
		level,
		food,
		armor,
		
		minecraft_crafted,
		minecraft_used,
		minecraft_broken,
		minecraft_mined,
		minecraft_killed,
		minecraft_picked_up,
		minecraft_dropped,
		minecraft_killed_by,
		teamkill,
		killedByTeam;
		
		public static ObjType get(String s) {
			
			for (ObjType t : ObjType.values())
				if (t.toString().equals(s.replace(".", "_").split(":")[0]))
					return t;
			return null;
		}
	}
}
