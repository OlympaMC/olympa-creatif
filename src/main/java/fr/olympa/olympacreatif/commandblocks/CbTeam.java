package fr.olympa.olympacreatif.commandblocks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.perks.NbtParserUtil;
import fr.olympa.olympacreatif.plot.Plot;
import net.minecraft.server.v1_15_R1.ChatMessage;
import net.minecraft.server.v1_15_R1.EntityBee;
import net.minecraft.server.v1_15_R1.EntitySlime;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import net.minecraft.server.v1_15_R1.ItemStack;
import net.minecraft.server.v1_15_R1.IChatBaseComponent.ChatSerializer;

public class CbTeam {

	private OlympaCreatifMain plugin;
	private Plot plot;
	private List<String> members = new ArrayList<String>();
	private boolean allowFriendlyFire = false;
	private String color = "";

	private String id;
	private String teamName = "";
	
	private List<Entity> teamNameHolders = new ArrayList<Entity>();
	
	public CbTeam(OlympaCreatifMain plugin, Plot plot, String id, String name) {
		this.plugin = plugin;
		this.plot = plot;
		this.id = id;
		setName(name);
	}
	
	public String getName() {
		return teamName;
	}
	
	public String getId() {
		return id;
	}
	
	public void setName(String n) {
		//teamName = NbtParserUtil.parseJsonText(n);
		
		if (teamName.equals(""))
			removeTeamNameForAll();
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
		boolean isAdded = false;
		
		if (e instanceof Player) {
			isAdded = addMember(((Player) e).getDisplayName());
		}else {
			isAdded = addMember(e.getCustomName());
		}

		if (isAdded) {
			showTeamName(e);

		}
		
		return isAdded;
	}
	
	public boolean addMember(String s) {
		if (members.contains(s))
			return false;
		
		for (CbTeam t : plugin.getCommandBlocksManager().getTeams(plot))
			if (t.getMembers().contains(s))
				t.removeMember(s);
		
		members.add(ChatColor.translateAlternateColorCodes('&', s));
		
		return true;
	}
	
	public void removeMember(Entity e) {
		if (e instanceof Player) {
			removeMember(((Player) e).getDisplayName());
			removeTeamName((Player) e);
		}else
			removeMember(e.getCustomName());
		
		removeTeamName(e);
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
	
	public void showTeamName(Entity entity) {//summon des entités pour faire apparaître le nom de la team au dessus du pseudo du joueur
		
		removeTeamName(entity);
		
		Bukkit.broadcastMessage("HERE");
		
		if (teamName.equals(""))
			return;

		
		Bukkit.broadcastMessage("HERE 2");
		
		//entité portant le nom ed l'équipe
		EntitySlime eSlime = new EntitySlime(EntityTypes.SLIME, plugin.getWorldManager().getNmsWorld());
		eSlime.setSize(1, true);
		eSlime.updateSize();
		eSlime.setInvisible(true);
		eSlime.setInvulnerable(true);
		eSlime.setCustomNameVisible(true);
		eSlime.setCustomName(new ChatMessage(color + getDisplayName()));
		eSlime.setNoAI(true);
		
		
		//entité portant le nom du joueur
		EntityBee eBee = new EntityBee(EntityTypes.BEE, plugin.getWorldManager().getNmsWorld());
		eBee.updateSize();
		eBee.setInvisible(true);
		eBee.setInvulnerable(true);
		eBee.setCustomNameVisible(true);
		if (entity.getType() == EntityType.PLAYER)
			eBee.setCustomName(new ChatMessage(((Player) entity).getDisplayName()));
		else
			eBee.setCustomName(new ChatMessage(entity.getCustomName()));
		
		eBee.setNoAI(true);
		
		eSlime.spawnIn(plugin.getWorldManager().getNmsWorld());
		eBee.spawnIn(plugin.getWorldManager().getNmsWorld());
		
		eSlime.startRiding(((CraftEntity)entity).getHandle());
		eBee.startRiding(((CraftEntity)entity).getHandle());
		

		teamNameHolders.add(eSlime.getBukkitEntity());
		teamNameHolders.add(eBee.getBukkitEntity());
	}
	
	public void removeTeamName(Entity entity) {
		for (Entity e : entity.getPassengers())
			if (teamNameHolders.contains(e))
				e.remove();
	}
	
	public void removeTeamNameForAll() {//tue les entités chevauchant les joueurs de cette équipe
		for (Entity e : teamNameHolders)
			e.remove();
	}
	
	public List<Entity> getTeamsNameHoldersEntityes(){
		return teamNameHolders;
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
