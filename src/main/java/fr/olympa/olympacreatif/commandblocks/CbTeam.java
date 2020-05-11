package fr.olympa.olympacreatif.commandblocks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;

public class CbTeam {

	private OlympaCreatifMain plugin;
	private Plot plot;
	private List<String> members = new ArrayList<String>();
	private boolean allowFriendlyFire = false;
	private String color = "";

	private String id;
	private String name;
	
	public CbTeam(OlympaCreatifMain plugin, Plot plot, String id, String name) {
		this.plugin = plugin;
		this.plot = plot;
		this.id = id;
		this.name = ChatColor.translateAlternateColorCodes('&',name);
	}
	
	public String getName() {
		return name;
	}
	
	public String getId() {
		return id;
	}
	
	public void setName(String n) {
		name = ChatColor.translateAlternateColorCodes('&',n);
	}
	
	public Plot getPlot() {
		return plot;
	}
	
	public String getDisplayName() {
		if (color.equals(""))
			return name;
		else
			return color + name;
	}
	
	public void addMember(Entity e) {
		if (e instanceof Player)
			addMember(((Player) e).getDisplayName());
		else
			addMember(e.getCustomName());
	}
	
	public void addMember(String s) {
		if (members.contains(s))
			return;
		
		for (CbTeam t : plugin.getCommandBlocksManager().getTeams(plot))
			if (t.getMembers().contains(s))
				t.removeMember(s);
		
		members.add(ChatColor.translateAlternateColorCodes('&', s));
	}
	
	public void removeMember(Entity e) {
		if (e instanceof Player)
			removeMember(((Player) e).getDisplayName());
		else
			removeMember(e.getCustomName());
	}
	
	public void removeMember(String s) {
		members.remove(ChatColor.translateAlternateColorCodes('&', s));
	}

	public List<String> getMembers(){
		return members;
	}
	
	public boolean isMember(Entity e) {
		if (e instanceof Player)
			return isMember(((Player) e).getDisplayName());
		else
			return isMember(e.getCustomName());
	}
	
	public boolean isMember(String s) {
		return members.contains(s);
	}
	
	public void setFriendlyFire(boolean b) {
		allowFriendlyFire = b;
	}
	
	public boolean hasFriendlyFire() {
		return allowFriendlyFire;
	}
	
	public String getColor() {
		return color;
	}
	
	public void setColor(String colorAsString) {
		color = ColorType.getColor(colorAsString);
	}
	
	public enum ColorType{
		dark_red("§4"),
		red("§c"),
		gold("§6"),
		yellow("§e"),
		dark_green("§2"),
		green("§a"),
		aqua("§b"),
		dark_aqua("§3"),
		dark_blue("§1"),
		blue("§9"),
		light_purple("§d"),
		dark_purple("§5"),
		white("§f"),
		gray("§7"),
		dark_gray("§8"),
		black("§0"),
		;
		
		String color;
		
		ColorType(String s){
			color = s;
		}
		
		public String getColor() {
			return color;
		}
		public static String getColor(String colorAsString) {
			for (ColorType c : ColorType.values())
				return c.getColor();
			
			return "";
		}
	}
	
}
