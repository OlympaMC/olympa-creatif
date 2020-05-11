package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Color;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.entity.Entity.Spigot;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;

public class CbCommand {

	protected OlympaCreatifMain plugin;
	protected Plot plot;
	protected List<Entity> targetEntities;
	protected String[] args;
	protected CommandSender sender;
	
	public CbCommand(CommandSender sender, OlympaCreatifMain plugin, Plot plot, String[] commandString) {
		this.plugin = plugin;
		this.plot = plot;
		this.sender = sender;
	}
	
	protected List<Entity> parseSelector(Plot plot, String s, boolean limitToPlayers){
		List<Entity> list = new ArrayList<Entity>();
//TODO
		
		
		return list;
	}
	
	//récupère une localisation dans le plot depuis 3 strings
	protected Location getLocation(CommandSender sender, String x, String y, String z) {
		Location loc = null;
		Location locInit = null;
		
		if (sender instanceof CommandBlock)
			locInit = ((CommandBlock)sender).getLocation();
		else if (sender instanceof Player)
			locInit = ((Player)sender).getLocation();
			
		if (locInit == null)
			return null;
		
		if (StringUtils.isNumeric(x) && StringUtils.isNumeric(y) && StringUtils.isNumeric(z)){
			loc = new Location(plugin.getWorldManager().getWorld(), Double.valueOf(x), Double.valueOf(y), Double.valueOf(z));
			
			if (plot.getId().isInPlot(loc))
				return loc;
			
		}else if(StringUtils.isNumeric(x.replaceFirst("~", "")) && StringUtils.isNumeric(y.replaceFirst("~", "")) && StringUtils.isNumeric(z.replaceFirst("~", ""))){

			loc = new Location(plugin.getWorldManager().getWorld(), Double.valueOf(x.replaceFirst("~", "")), Double.valueOf(y.replaceFirst("~", "")), Double.valueOf(z.replaceFirst("~", "")));
			if (plot.getId().isInPlot(loc))
				return loc;
			
		}
		
		return null;
	}
	
	public static CbCommand getCommand(OlympaCreatifMain plugin, CommandSender sender, String fullCommand) {
		Plot plot = null;
		String[] args = fullCommand.split(" ");

		if (sender instanceof Player) {
			plot = plugin.getPlotsManager().getPlot(((Player) sender).getLocation());	
		}
		if (sender instanceof CommandBlock) {
			plot = plugin.getPlotsManager().getPlot(((CommandBlock) sender).getLocation());	
		}
		
		if (args.length < 3 && plot == null)
			return null;
		
		CbCommand cmd = null;
		
		CommandType type = CommandType.get(args[0]);
		
		List list = new ArrayList<String>(Arrays.asList(args));
		list.remove(0);
		args = (String[]) list.toArray();
		
		switch (type) {
		case BOSSBAR:
			cmd = new CmdBossBar(plugin, args);
			break;
		case CLEAR:
			cmd = new CmdClear(sender, plugin, plot, args);
			break;
		case ENCHANT:
			cmd = new CmdEnchant(sender, plugin, plot, args);
			break;
		case EXECUTE:
			cmd = new CmdExecute(plugin, args);
			break;
		case EXPERIENCE:
			cmd = new CmdExperience(sender, plugin, plot, args);
			break;
		case GIVE:
			cmd = new CmdGive(sender, plugin, plot, args);
			break;
		case MSG:
			cmd = new CmdTellraw(sender, plugin, plot, args);
			break;
		case SCOREBOARD:
			cmd = new CmdScoreboard(sender, plugin, plot, args);
			break;
		case TEAM:
			cmd = new CmdTeam(sender, plugin, plot, args);
			break;
		case TELEPORT:
			cmd = new CmdTeleport(sender, plugin, plot, args);
			break;
		default:
			break;
		}		
		
		return cmd;
	}
	
	public enum CommandType{
		TELEPORT,
		MSG,
		EXECUTE,
		TEAM,
		SCOREBOARD,
		BOSSBAR,
		CLEAR,
		GIVE,
		ENCHANT,
		EXPERIENCE,
		;
		
		public static CommandType get(String s) {
			for (CommandType cmd : CommandType.values())
				if (cmd.toString().equalsIgnoreCase(s))
					return cmd;
			return null;
		}
	}
	
	public int execute() {
		return 0;
	}
	
}
