package fr.olympa.olympacreatif.commandblocks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
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
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.google.common.util.concurrent.SettableFuture;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.scoreboard.sign.ScoreboardManager;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.commands.CmdTellraw;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.utils.JSONtextUtil;
import fr.olympa.olympacreatif.utils.NbtParserUtil;
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
		
		//for (Entity e : members)
			//if (e.getType() == EntityType.PLAYER)
				//if (teamName.equals(""))
					//TODO//OlympaCore.getInstance().getNameTagApi().setSuffix(((Player)e).getName(), "");
				//else
					//TODO//OlympaCore.getInstance().getNameTagApi().setSuffix(((Player)e).getName(), " §7(" + getName() + "§7)");
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
		
		//if (e.getType() == EntityType.PLAYER && !teamName.equals("")) 
			//TODO//OlympaCore.getInstance().getNameTagApi().setSuffix(((Player)e).getName(), " §7(" + getName() + "§7)");
		
		
		return true;
	}
	
	public void removeMember(Entity e) {
		members.remove(e);
		
		//if (e.getType() == EntityType.PLAYER)
			//TODO//OlympaCore.getInstance().getNameTagApi().setSuffix(((Player)e).getName(), "");
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
		//for (Entity e : members)
			//if (e.getType() == EntityType.PLAYER)
				//TODO//OlympaCore.getInstance().getNameTagApi().setSuffix(((Player)e).getName(), "");
	}
	
}
