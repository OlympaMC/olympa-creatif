package fr.olympa.olympacreatif.commandblocks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftBee;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftSlime;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.perks.NbtParserUtil;
import fr.olympa.olympacreatif.perks.PlayerMultilineUtil.LineDataWrapper;
import fr.olympa.olympacreatif.plot.Plot;
import net.minecraft.server.v1_15_R1.ChatMessage;
import net.minecraft.server.v1_15_R1.EntityArmorStand;
import net.minecraft.server.v1_15_R1.EntityBee;
import net.minecraft.server.v1_15_R1.EntitySlime;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import net.minecraft.server.v1_15_R1.ItemStack;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.IChatBaseComponent.ChatSerializer;

public class CbTeam {

	private OlympaCreatifMain plugin;
	private Plot plot;
	private List<Entity> members = new ArrayList<Entity>();
	private boolean allowFriendlyFire = false;
	private String color = "";

	private String teamId;
	private String teamName = "";
	
	public CbTeam(OlympaCreatifMain plugin, Plot plot, String id, String name) {
		this.plugin = plugin;
		this.plot = plot;
		this.teamId = id;
		this.teamName = name;
	}
	
	public String getName() {
		return teamName;
	}
	
	public String getId() {
		return teamId;
	}
	
	public void setName(String newTeamName) {
		
		if (newTeamName.equals(teamId))
			removeTeamNameForAll();
		else if (!teamName.equals(newTeamName)) {

			teamName = newTeamName;
			
			for (Player p : plot.getPlayers())
				if (isMember(p))
					showTeamName(p);	
		}
	}
	
	public Plot getPlot() {
		return plot;
	}
	
	public String getDisplayName() {
		if (color.equals(""))
			return teamName;
		else
			return color + teamName;
	}
	
	public boolean addMember(Entity e) {
		if (members.contains(e))
			return false;
		
		CbTeam quittedTeam = plugin.getCommandBlocksManager().getTeamOf(plot, e);
		if (quittedTeam != null)
			quittedTeam.removeMember(e);
		else
			members.add(e);

		if (e.getType() == EntityType.PLAYER && !getDisplayName().equals(getId())) 
			showTeamName((Player) e);
		
		return true;
	}
	
	public void removeMember(Entity e) {
		members.remove(e);
		
		if (e.getType() == EntityType.PLAYER)
			removeTeamName((Player) e);
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
	
	public String getColor() {
		return color;
	}
	
	public void setColor(String colorAsString) {
		color = ColorType.getColor(colorAsString);
	}
	
	//summon des entités pour faire apparaître le nom de la team au dessus du pseudo du joueur
	public void showTeamName(Player p) {

		LineDataWrapper data = plugin.getPerksManager().getLinesOnHeadUtil().getLineDataWrapper(p);
		
		data.addLine("playerName", p.getDisplayName());
		
		if (!data.editLine("team", teamName)) {
			data.addLine("team", teamName);
			data.moveLine("team", 1);
		}
		
		Bukkit.broadcastMessage(data.toString());
	}
	
	public void removeTeamName(Player p) {
		LineDataWrapper data = plugin.getPerksManager().getLinesOnHeadUtil().getLineDataWrapper(p);
		
		data.removeLine("team");
		if (data.getLinesCount() == 1)
			data.clearLines();
	}

	public void removeTeamNameForAll() {
		for (Entity e : members)
			if (e.getType() == EntityType.PLAYER)
				removeTeamName((Player) e);
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
				if (c.toString().equals(colorAsString))
					return c.getColor();
			
			return "";
		}
	}
	
}
