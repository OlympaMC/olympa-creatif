package fr.olympa.olympacreatif.commandblocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.CbTeam.ColorType;
import fr.olympa.olympacreatif.commandblocks.commands.CmdTellraw;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.utils.JSONtextUtil;

public class CbObjective {

	private OlympaCreatifMain plugin;
	private Plot plot;
	
	private ObjType type = null;
	private Object typeParam =null; //contient éventuellement le second paramètre de l'objectif (par exemple, killed_by:minecraft.zombie
	
	private DisplaySlot displaySlot = null;
	
	//private Map<String, Integer> values = new HashMap<String, Integer>();

	private Map<Entity, Integer> entityHolders = new HashMap<Entity, Integer>();
	private Map<String, Integer> stringHolders = new HashMap<String, Integer>();
	
	private String objId = "";
	private String objName = "";
	
	//liste des joueurs autorisés à utiliser le /trigger (si l'objectif est de type trigger)
	private List<Entity> allowedEntitiesTrigger = new ArrayList<Entity>();
	
	public CbObjective(OlympaCreatifMain plugin, Plot plot, String objType, String id, String objName) {
		this.plugin = plugin;
		this.plot = plot;
		this.objId = id.substring(0, Math.min(15, id.length()));

		setName(objName);
		
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
			if (objType.split("\\.").length == 2)
				if (typeParamString == null)
					return;
			
			typeParam = ColorType.getColor(objType.split("\\.")[1]);
			
			break;
		case teamkill:
			if (objType.split("\\.").length == 2)
				if (typeParamString == null)
					return;
			
			typeParam = ColorType.getColor(objType.split("\\.")[1]);
			
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

	public List<Entity> getTriggerAllowedEntities(){
		return allowedEntitiesTrigger;
	}
	
	public void setName(String newObjName) {
		if (newObjName == null)
			newObjName = objId;
		else		
			newObjName = JSONtextUtil.getJsonText(newObjName).toLegacyText();
		
		if (!newObjName.equals(objName)) {
			if (displaySlot == DisplaySlot.BELOW_NAME)
				plot.getCbData().getObjectiveBelowName().setDisplayName(newObjName);
			
			if (displaySlot == DisplaySlot.SIDEBAR)
				for (Player p : plot.getPlayers())
					((OlympaPlayerCreatif)AccountProvider.get(p.getUniqueId())).setCustomScoreboardTitle(newObjName);
		}
		
			
		objName = newObjName;
	}
	
	public Object getParamType() {
		return typeParam;
	}
	
	public Map<String, Integer> getValues(boolean sortValues){
		Map<String, Integer> values = new HashMap<String, Integer>();
		
		//copie valeurs strings
		for (Entry<String, Integer> e : stringHolders.entrySet())
			values.put(ChatColor.translateAlternateColorCodes('&', e.getKey().replace("_", " ")), e.getValue());
			//values.put(e.getKey(), e.getValue());
		
		//copie valeurs entités
		for (Entry<Entity, Integer> e : entityHolders.entrySet())
			//si joueur, copie nom avec grade
			if (e.getKey().getType() == EntityType.PLAYER)
				values.put((AccountProvider.get(e.getKey().getUniqueId())).getGroupNameColored() + " " + e.getKey().getName(), e.getValue());
			else
				//si entité, copie custom name ou type entité par défaut
				if (e.getKey().getCustomName() != null)
					values.put(e.getKey().getCustomName(), e.getValue());
				else
					values.put(e.getKey().getName(), e.getValue());
		
		if (!sortValues)
			return values;
	   
		List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(values.entrySet());
	   
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {

			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return - o1.getValue().compareTo(o2.getValue());
			}
	   });
	
		Map<String, Integer> result = new LinkedHashMap<String, Integer>();
	   for (Entry<String, Integer> e : list) {
		   result.put(e.getKey(), e.getValue());
	   }
	   
	   return result;
	}
	
	//ADD et SET POUR STRINGS
	
	public void add(String name, int value) {
		if (!stringHolders.containsKey(name))
			set(name, value);
		else
			set(name, stringHolders.get(name) + value);
	}
	
	//gestion sidebar/belowname ici
	public void set(String name, Integer value) {
		
		//name = ChatColor.translateAlternateColorCodes('&', name.replace("_", " "));
		
		if (value == null)
			stringHolders.remove(name);
		else
			stringHolders.put(name, value);
		
		//affichage scoreboard sidebar
		if (displaySlot == DisplaySlot.SIDEBAR) {
			Map<String, Integer> values = getValues(true);
			
			for (Player p : plot.getPlayers()) 
				((OlympaPlayerCreatif) AccountProvider.get(p.getUniqueId())).setCustomScoreboardLines(values);
			
			//Bukkit.broadcastMessage("SCORES " + objId + " : " + values);	
		}
	}

	public int get(String name) {
		if (stringHolders.containsKey(name))
			return stringHolders.get(name);
		else
			return 0;
	}
	
	//ADD et SET POUR ENTITIES
	
	public void add(Entity e, int value) {
		set(e, value + get(e));
	}
	
	@SuppressWarnings("deprecation")
	public void set(Entity e, Integer value) {
		
		//définition string portant le score		
		if (displaySlot == DisplaySlot.BELOW_NAME && e.getType() == EntityType.PLAYER) {
			Objective obj = plot.getCbData().getObjectiveBelowName();
			
			if (value == null)
				obj.getScore((Player) e).setScore(0);
			else
				obj.getScore((Player) e).setScore(value);
		}
		
		if (value == null)
			entityHolders.remove(e);
		else
			entityHolders.put(e, value);
		
		//affichage scoreboard sidebar
		if (displaySlot == DisplaySlot.SIDEBAR) {
			Map<String, Integer> values = getValues(true);
			
			for (Player p : plot.getPlayers()) 
				((OlympaPlayerCreatif) AccountProvider.get(p.getUniqueId())).setCustomScoreboardLines(values);	
		}
	}
	
	public int get(Entity e) {
		if (entityHolders.containsKey(e))
			return entityHolders.get(e);
		else
			return 0;
	}
	
	public int setDisplaySlot(DisplaySlot newDisplaySlot) {
		if (newDisplaySlot == displaySlot)
			return 0;
		
		for (CbObjective obj : plot.getCbData().getObjectives())
			if (obj.getDisplaySlot() == newDisplaySlot)
				obj.clearDisplaySlot();

		displaySlot = newDisplaySlot;
		
		if (displaySlot == DisplaySlot.BELOW_NAME) 
			plot.getCbData().getObjectiveBelowName().setDisplayName(objName);
				
		if (displaySlot == DisplaySlot.SIDEBAR) {
			Map<String, Integer> scores = getValues(true);
			
			for (Player p : plot.getPlayers()) {
				OlympaPlayerCreatif pc = AccountProvider.get(p.getUniqueId());
				
				pc.setCustomScoreboardTitle(getName());
				pc.setCustomScoreboardLines(scores);
			}	
		}
		return 1;
	}
	
	public void clearDisplaySlot() {
		if (displaySlot == DisplaySlot.SIDEBAR)
			for (Player p : plot.getPlayers())
				((OlympaPlayerCreatif)AccountProvider.get(p.getUniqueId())).clearCustomScoreboard();

		if (displaySlot == DisplaySlot.BELOW_NAME)
			plot.getCbData().clearBelowName();
		
		displaySlot = null;
	}

	public DisplaySlot getDisplaySlot() {
		return displaySlot;
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
		trigger,
		
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
