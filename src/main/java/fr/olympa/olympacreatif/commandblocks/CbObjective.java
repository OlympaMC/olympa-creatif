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

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.plot.Plot;

public class CbObjective {

	private OlympaCreatifMain plugin;
	private Plot plot;
	
	private ObjType type = null;
	private Object typeParam =null; //contient éventuellement le second paramètre de l'objectif (par exemple, killed_by:minecraft.zombie
	
	private DisplaySlot displaySlot = null;
	
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
		if (!newObjName.equals(objName)) {
			if (displaySlot == DisplaySlot.BELOW_NAME)
				plugin.getCommandBlocksManager().getPlotScoreboard(plot).getObjective("belowName").setDisplayName(newObjName);
			
			if (displaySlot == DisplaySlot.SIDEBAR)
				for (Player p : plot.getPlayers())
					((OlympaPlayerCreatif)AccountProvider.get(p.getUniqueId())).setCustomScoreboardLine(0, newObjName);
		}
		
			
		objName = newObjName;
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
	public void set(String name, Integer value) {
		if (value == null)
			values.remove(name);
		else
			values.put(name, value);
		
		//affichage scoreboard sidebar
		if (displaySlot == DisplaySlot.SIDEBAR) {
			Map<String, Integer> scores = getValues(true);
			List<String> keys = new ArrayList<String>(scores.keySet());

			for (Player p : plot.getPlayers()) {
				OlympaPlayerCreatif pc = AccountProvider.get(p.getUniqueId());
				
				for (int i = 0 ; i < keys.size() ; i++) 
					pc.setCustomScoreboardLine(i, keys.get(i));				
			}	
		}
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
	
	@SuppressWarnings("deprecation")
	public void set(Entity e, Integer value) {
		
		//définition string portant le score
		String scoreHolder = "";
		
		if (e instanceof Player)
			scoreHolder = ((Player) e).getDisplayName();
		else
			scoreHolder = e.getCustomName();
		
		if (displaySlot == DisplaySlot.BELOW_NAME && e.getType() == EntityType.PLAYER) {
			Scoreboard scb = plugin.getCommandBlocksManager().getPlotScoreboard(plot);
			
			if (value == null)
				scb.resetScores((Player) e);
			else
				scb.getObjective("belowName").getScore((Player) e).setScore(value);
		}
		
		set(scoreHolder, value);
	}
	
	public int get(Entity e) {
		if (e instanceof Player)
			return get(((Player) e).getDisplayName());
		else
			return get(e.getCustomName());
	}

	
	public void setDisplaySlot(DisplaySlot newDisplaySlot) {
		if (newDisplaySlot == displaySlot)
			return;
		
		clearDisplaySlots();
		
		if (newDisplaySlot == DisplaySlot.BELOW_NAME) {
			Objective obj = plugin.getCommandBlocksManager().getPlotScoreboard(plot).getObjective("belowName");
			obj.setDisplaySlot(newDisplaySlot);
			obj.setDisplayName(getName());
		}
		
		if (newDisplaySlot == DisplaySlot.SIDEBAR) {
			Map<String, Integer> scores = getValues(true);
			List<String> keys = new ArrayList<String>(scores.keySet());

			for (Player p : plot.getPlayers()) {
				OlympaPlayerCreatif pc = AccountProvider.get(p.getUniqueId());
				
				for (int i = 0 ; i < keys.size() ; i++) 
					pc.setCustomScoreboardLine(i, keys.get(i));				
			}	
		}				
		
		displaySlot = newDisplaySlot;
	}
	
	
	public void clearDisplaySlots() {
		if (displaySlot == DisplaySlot.SIDEBAR)
			for (Player p : plot.getPlayers())
				((OlympaPlayerCreatif)AccountProvider.get(p.getUniqueId())).getCustomScoreboardLines().clear();;
		
		if (displaySlot == DisplaySlot.BELOW_NAME)
			plugin.getCommandBlocksManager().getPlotScoreboard(plot).clearSlot(displaySlot);
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
