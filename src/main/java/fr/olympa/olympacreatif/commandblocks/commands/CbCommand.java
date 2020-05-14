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

public abstract class CbCommand {

	protected OlympaCreatifMain plugin;
	protected Plot plot;
	protected List<Entity> targetEntities;
	protected String[] args;
	protected CommandSender sender;
	
	protected Location sendingLoc;
	
	//la commande comprend un commandsender, une localisation (imposée par le execute at), le plugin, le plot à la commande est exécutée et les arguments de la commande
	public CbCommand(CommandSender sender, Location sendingLoc, OlympaCreatifMain plugin, Plot plot, String[] commandString) {
		this.plugin = plugin;
		this.plot = plot;
		this.sender = sender;
		this.args = commandString;
		this.sendingLoc = sendingLoc;
	}
	
	protected List<Entity> parseSelector(Plot plot, String s, boolean limitToPlayers){
		List<Entity> list = new ArrayList<Entity>();
		

		//TODO
		//TODO
		
		
		return list;
	}
	
	protected Location getLocation (String x, String y, String z) {
		Location locFinal = null;
		Location locInit = sendingLoc;
		
		Double xF = getUnverifiedPoint(x, sendingLoc.getX());
		Double yF = getUnverifiedPoint(y, sendingLoc.getY());
		Double zF = getUnverifiedPoint(z, sendingLoc.getZ());		
		 
		if (xF != null && yF != null && zF != null) {
			Location loc = new Location(plugin.getWorldManager().getWorld(), xF, yF, zF); 
			if (plot.getId().isInPlot(loc))
				return loc;
			else
				return null;
		}else
			return null;
	}
	
	private Double getUnverifiedPoint(String s, double potentialVectorValueToAdd) {
		
		if (StringUtils.isNumeric(s))
			return Double.valueOf(s);
		
		if (StringUtils.isNumeric(s.replaceFirst("~", "")))
			return Double.valueOf(s.replaceFirst("~", "")) + potentialVectorValueToAdd;
		
		return null;
	}
	
	public static CbCommand getCommand(OlympaCreatifMain plugin, CommandSender sender, Location loc, String fullCommand) {
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
		
		List<String> list = new ArrayList<String>(Arrays.asList(args));
		list.remove(0);
		args = (String[]) list.toArray();
		
		switch (type) {
		case BOSSBAR:
			cmd = new CmdBossBar(sender, loc, plugin, plot, args);
			break;
		case CLEAR:
			cmd = new CmdClear(sender, loc, plugin, plot, args);
			break;
		case ENCHANT:
			cmd = new CmdEnchant(sender, loc, plugin, plot, args);
			break;
		case EXECUTE:
			cmd = new CmdExecute(sender, loc, plugin, plot, args);
			break;
		case EXPERIENCE:
			cmd = new CmdExperience(sender, loc, plugin, plot, args);
			break;
		case GIVE:
			cmd = new CmdGive(sender, loc, plugin, plot, args);
			break;
		case MSG:
			cmd = new CmdTellraw(sender, loc, plugin, plot, args);
			break;
		case SCOREBOARD:
			cmd = new CmdScoreboard(sender, loc, plugin, plot, args);
			break;
		case TEAM:
			cmd = new CmdTeam(sender, loc, plugin, plot, args);
			break;
		case TELEPORT:
			cmd = new CmdTeleport(sender, loc, plugin, plot, args);
			break;
		case EFFECT:
			cmd = new CmdEffect(sender, loc, plugin, plot, args);
			break;
		case SUMMON:
			cmd = new CmdSummon(sender, loc, plugin, plot, args);
			break;
		case KILL:
			cmd = new CmdKill(sender, loc, plugin, plot, args);
			break;
		case SAY:
			cmd = new CmdSay(sender, loc, plugin, plot, args);
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
		EFFECT, 
		SUMMON, 
		KILL,
		SAY,
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
