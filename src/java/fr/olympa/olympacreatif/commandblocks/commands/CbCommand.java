package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.utils.NBTcontrollerUtil;
import net.minecraft.server.v1_16_R3.NBTTagCompound;

public abstract class CbCommand extends CbCommandSelectorParser {
	
	//la commande comprend un commandsender, une localisation (imposée par le execute at), le plugin, le plot à la commande est exécutée et les arguments de la commande
	public CbCommand(CommandType cmdType, Entity sender, Location sendingLoc, OlympaCreatifMain plugin, Plot plot, String[] commandString) {
		super(cmdType, sender, sendingLoc, plugin, plot, commandString);
	}
	
	//get item from string, prend en compte material et tags
	public static ItemStack getItemFromString(String s, Player requester) {
		if (s == null)
			return null;
		
		String matStr = s.startsWith("minecraft:") ? s.substring(10, s.length()) : s;

		Material mat = Material.getMaterial(matStr.split("\\{")[0].toUpperCase());
		
		if (mat == null)
			return null;
		
		ItemStack item = new ItemStack(mat);
		
		int indexOfNbt = s.indexOf("{");
		if (s.indexOf("{") == -1)  
			return item;

		NBTTagCompound tag = NBTcontrollerUtil.getValidTags(s.substring(indexOfNbt), requester);
		net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
		nmsItem.setTag(tag);
		item = CraftItemStack.asBukkitCopy(nmsItem);
		
		return item;
	}
	
	//renvoie une localisation absolue ou relative complète (null si err de syntaxe ou si hors du plot)
	protected Location parseLocation(String x, String y, String z) {
		return parseLocation(x, y, z, null, null);
	}

	protected Location parseLocation(String x, String y, String z, String yaw, String pitch) {
		return parseLocation(plot, sendingLoc, x, y, z, yaw, pitch);
	}

	public static Location parseLocation(Plot plot, Location sendingLoc, String x, String y, String z) {
		return parseLocation(plot, sendingLoc, x, y, z, "0", "0");
	}

	public static Location parseLocation(Plot plot, Location sendingLoc, String x, String y, String z, String yaw, String pitch) {
		if (plot == null || sendingLoc == null)
			return null;

		Double xF;
		Double yF;
		Double zF;
		float yawF = 0;
		float pitchF = 0; 
		
		try {
			xF = getUnverifiedPoint(x, sendingLoc.getX()) + 0.5;
			yF = getUnverifiedPoint(y, sendingLoc.getY());
			zF = getUnverifiedPoint(z, sendingLoc.getZ()) + 0.5;
			yawF = Float.parseFloat(yaw);
			pitchF = Float.parseFloat(pitch);
		}catch(Exception ex) {
			return null;
		}
		
		if (xF != null && yF != null && zF != null) {
			Location loc = new Location(OlympaCreatifMain.getInstance().getWorldManager().getWorld(), xF, yF, zF, yawF, pitchF);
			if (plot.getId().isInPlot(loc))
				return loc;
			else
				return null;
		}else
			return null;
	}
	
	//renvoie la coordonnée x, y ou z à partir du string (en coordonnée absolue ou relative)
	private static Double getUnverifiedPoint(String s, double potentialVectorValueToAdd) {
		
		try{
			return Double.valueOf(s);
		}catch(NumberFormatException e) {
		}
		
		if (s.startsWith("~"))
			if (s.length() >= 2)
				try{
					return Double.valueOf(s.substring(1, s.length())) + potentialVectorValueToAdd;	
				}catch(NumberFormatException e) {
					return null;
				}
			else
				return potentialVectorValueToAdd;
				
		return null;
	}
	
	
	public Plot getPlot() {
		return plot;
	}
	
	public CommandType getType() {
		return cmdType;
	}
	
	public static CommandType getCommandType(String cmd) {
		String s = cmd.startsWith("/") ? cmd.substring(1, cmd.length()) : cmd;
		s = cmd.startsWith("minecraft:") ? cmd.substring(10, cmd.length()) : s;
		return CommandType.get(s);
		//return CommandType.get(s.split(":")[s.split(":").length - 1]);
	}
	
	public static CbCommand getCommand(CommandType type, OlympaCreatifMain plugin, Entity sender, Location loc, String fullCommand) {
		if (type == null)
			return null;
		
		Plot plot = plugin.getPlotsManager().getPlot(loc);
		
		if (plot == null)
			return null;

		//extraction des arguments de la commande
		String[] args = fullCommand.replace("minecraft:", "").split(" ");
		
		//List<String> list = new ArrayList<String>();
		List<String> concatList = new ArrayList<String>();
		
		//conctat les tags en plusieurs morceaux
		int accoladesCount = 0;
		int crochetsCount = 0;
		String concat = "";
		
		for (String s : args) {
			
			if (concat.length() == 0)
				concat += s;
			else
				concat += " " + s;
			
			accoladesCount += StringUtils.countMatches(s, "{");
			crochetsCount += StringUtils.countMatches(s, "[");
			accoladesCount -= StringUtils.countMatches(s, "}");
			crochetsCount -= StringUtils.countMatches(s, "]");
			
			if (accoladesCount == 0 && crochetsCount == 0) {
				concatList.add(concat);
				concat = "";
			}
		}
		
		concatList.remove(0);
		args = concatList.toArray(new String[concatList.size()]);
		
		return type == null ? null : type.getBuilder().getCmd(sender, loc, plugin, plot, args);
	}
	
	public static CbCommand getCommand(OlympaCreatifMain plugin, Entity sender, Location loc, String fullCommand) {
		return getCommand(getCommandType(fullCommand), plugin, sender, loc, fullCommand);
	}
	
	public enum CommandType{
		teleport(CmdOp::new),
		tp(CmdTeleport::new),
		tellraw(CmdTellraw::new),
		execute(CmdExecute::new, 2),
		team(CmdTeam::new),
		scoreboard(CmdScoreboard::new),
		bossbar(CmdBossBar::new),
		clear(CmdClear::new, 2),
		give(CmdGive::new, 2),
		enchant(CmdEnchant::new),
		experience(CmdExperience::new), 
		effect(CmdEffect::new), 
		summon(CmdSummon::new, 2), 
		kill(CmdKill::new),
		say(CmdSay::new), 
		me(CmdSay::new), 
		setblock(CmdSetblock::new, 3), 
		gamemode(CmdGamemode::new), 
		gm(CmdGamemode::new), 
		op(CmdOp::new),
		trigger(CmdTrigger::new),
		replaceitem(CmdReplaceitem::new),
		weather(CmdWeather::new), 
		time(CmdTime::new), 
		setworldspawn(CmdSetworldspawn::new), 
		spreadplayers(CmdSpreadplayers::new),
		structure(CmdStructure::new, 50, false)
		;
		
		private final CommandBuilder builder;
		private final int requiredCmdTickets;
		public final boolean cancelPlayerExecution;
		
		CommandType(CommandBuilder builder) {
			this(builder, 1);
		}

		CommandType(CommandBuilder builder, int i) {
			this(builder, i, true);
		}

		CommandType(CommandBuilder builder, int i, boolean cancelPlayerExecution) {
			this.builder = builder;
			requiredCmdTickets = i;
			this.cancelPlayerExecution = cancelPlayerExecution;
		}
		
		public int getRequiredCbTickets() {
			return requiredCmdTickets;
		}
		
		public CommandBuilder getBuilder() {
			return builder;
		}
		
		public static CommandType get(String s) {
			s = s.toLowerCase().split(" ")[0];
			for (CommandType cmd : CommandType.values())
				if (s.equals(cmd.toString()))
					return cmd;
			return null;
		}
	}
	
	public int execute() {
		return 0;
	}
	
	@FunctionalInterface
	static interface CommandBuilder {
		CbCommand getCmd(Entity sender, Location loc, OlympaCreatifMain plugin, Plot plot, String[] args);
	}
	
}
