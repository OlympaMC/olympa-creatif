package fr.olympa.olympacreatif.commandblocks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.utils.JSONtextUtil;

public class CbTeam {

	private OlympaCreatifMain plugin;
	private Plot plot;
	private List<Entity> members = new ArrayList<Entity>();
	private boolean allowFriendlyFire = false;
	private ColorType color = null;

	private String teamId;
	private String teamName;
	
	public CbTeam(OlympaCreatifMain plugin, Plot plot, String id, String name) {
		this.plugin = plugin;
		this.plot = plot;
		this.teamId = id;
		setName(name);
	}
	
	public String getName() {
		return teamName;
	}
	
	public String getId() {
		return teamId;
	}
	
	public void setName(String newTeamName) {
		if (newTeamName == null)
			teamName = "";
		else
			teamName = JSONtextUtil.getJsonText(newTeamName).toLegacyText();
		
		List<OlympaPlayer> plotPlayers = new ArrayList<OlympaPlayer>();
		plot.getPlayers().forEach(p -> plotPlayers.add(AccountProvider.get(p.getUniqueId())));
		
		for (Entity e : members)
			if (plotPlayers.stream().filter(p -> p.getUniqueId().equals(e.getUniqueId())).count() > 1)
				OlympaCore.getInstance().getNameTagApi().callNametagUpdate(AccountProvider.get(e.getUniqueId()), plotPlayers);
	}
	
	public Plot getPlot() {
		return plot;
	}
	
	public boolean addMember(Entity e) {
		if (members.contains(e))
			return false;
		
		CbTeam quittedTeam = plot.getCbData().getTeamOf(e);
		if (quittedTeam != null)
			quittedTeam.removeMember(e);
		
		members.add(e);
		
		List<OlympaPlayer> plotPlayers = new ArrayList<OlympaPlayer>();
		plot.getPlayers().forEach(p -> plotPlayers.add(AccountProvider.get(p.getUniqueId())));
		
		if (e.getType() == EntityType.PLAYER)
			OlympaCore.getInstance().getNameTagApi().callNametagUpdate(AccountProvider.get(e.getUniqueId()), plotPlayers);
		
		
		return true;
	}
	
	public void removeMember(Entity e) {
		members.remove(e);
		
		List<OlympaPlayer> plotPlayers = new ArrayList<OlympaPlayer>();
		plot.getPlayers().forEach(p -> plotPlayers.add(AccountProvider.get(p.getUniqueId())));
		
		if (e.getType() == EntityType.PLAYER)
			OlympaCore.getInstance().getNameTagApi().callNametagUpdate(AccountProvider.get(e.getUniqueId()), plotPlayers);
	}

	public List<Entity> getMembers(){
		return members;
	}
	
	public boolean isMember(Entity e) {
		return members.contains(e);
	}
	
	public void setFriendlyFire(boolean b) {
		allowFriendlyFire = b;
	}
	
	public boolean hasFriendlyFire() {
		return allowFriendlyFire;
	}
	
	public ColorType getColor() {
		return color;
	}
	
	public boolean setColor(String colorAsString) {
		if (colorAsString == null) {
			color = null;
			return true;
		}
			
		ColorType newColor = ColorType.getColor(colorAsString);
		
		if (newColor == null)
			return false;
		
		for (CbTeam t : plot.getCbData().getTeams())
			if (t.getColor() == newColor)
				return false;
		
		color = newColor;
		return true;
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
		
		public String getColorCode() {
			return color;
		}
		public static ColorType getColor(String colorAsString) {
			for (ColorType c : ColorType.values())
				if (c.toString().equals(colorAsString))
					return c;
			return null;
		}
	}
	
	
	public void executeDeletionActions() {
		List<OlympaPlayer> plotPlayers = new ArrayList<OlympaPlayer>();
		plot.getPlayers().forEach(p -> plotPlayers.add(AccountProvider.get(p.getUniqueId())));
		
		for (Entity e : members)
			if (e.getType() == EntityType.PLAYER)		
				if (e.getType() == EntityType.PLAYER)
					OlympaCore.getInstance().getNameTagApi().callNametagUpdate(AccountProvider.get(e.getUniqueId()), plotPlayers);
	}	
}
