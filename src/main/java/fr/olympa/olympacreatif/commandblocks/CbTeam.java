package fr.olympa.olympacreatif.commandblocks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.olympacreatif.OlympaCreatifMain;
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
	private String name = "";
	
	private List<net.minecraft.server.v1_15_R1.Entity> teamNameHolders = new ArrayList<net.minecraft.server.v1_15_R1.Entity>();
	
	public CbTeam(OlympaCreatifMain plugin, Plot plot, String id, String name) {
		this.plugin = plugin;
		this.plot = plot;
		this.id = id;
		setName(ChatColor.translateAlternateColorCodes('&',name));
	}
	
	public String getName() {
		return name;
	}
	
	public String getId() {
		return id;
	}
	
	public void setName(String n) {
		name = ChatColor.translateAlternateColorCodes('&',n);
		
		if (name.equals(""))
			removeTeamNameForAll();
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
		if (e instanceof Player) {
			addMember(((Player) e).getName());
			showTeamName((Player) e);	
		}else
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
		if (e instanceof Player) {
			removeMember(((Player) e).getName());
			removeTeamName((Player) e);
		}else
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
	
	public void showTeamName(Player p) {//summon des entités pour faire apparaître le nom de la team au dessus du pseudo du joueur
		
		removeTeamName(p);
		
		if (name.equals(""))
			return;
		
		EntitySlime eSlime = new EntitySlime(EntityTypes.SLIME, plugin.getWorldManager().getNmsWorld());
		eSlime.setSize(1, true);
		eSlime.updateSize();
		eSlime.setInvisible(true);
		eSlime.setInvulnerable(true);
		eSlime.setCustomNameVisible(true);
		eSlime.setCustomName(new ChatMessage(color + name));
		eSlime.setNoAI(true);
		
		
		
		EntityBee eBee = new EntityBee(EntityTypes.BEE, plugin.getWorldManager().getNmsWorld());
		eBee.updateSize();
		eBee.setInvisible(true);
		eBee.setInvulnerable(true);
		eBee.setCustomNameVisible(true);
		eBee.setCustomName(new ChatMessage(p.getDisplayName()));
		eBee.setNoAI(true);
		
		eSlime.spawnIn(plugin.getWorldManager().getNmsWorld());
		eBee.spawnIn(plugin.getWorldManager().getNmsWorld());

		eSlime.startRiding(((CraftPlayer)p).getHandle());
		eBee.startRiding(((CraftPlayer)p).getHandle());
		

		teamNameHolders.add(eSlime);
		teamNameHolders.add(eBee);
	}
	
	public void removeTeamName(Player p) {
		for (Entity e : p.getPassengers())
			e.remove();
	}
	
	public void removeTeamNameForAll() {//tue les entités chevauchant les joueurs de cette équipe
		for (net.minecraft.server.v1_15_R1.Entity e : teamNameHolders)
			e.getBukkitEntity().remove();
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
