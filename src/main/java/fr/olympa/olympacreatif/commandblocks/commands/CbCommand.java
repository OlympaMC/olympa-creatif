package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_15_R1.command.CraftBlockCommandSender;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.jnbt.NBTUtils;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.CbObjective;
import fr.olympa.olympacreatif.commandblocks.CbTeam;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotCbData;
import fr.olympa.olympacreatif.utils.NBTcontrollerUtil;
import fr.olympa.olympacreatif.utils.NbtParserUtil;
import net.minecraft.server.v1_15_R1.MojangsonParser;
import net.minecraft.server.v1_15_R1.NBTTagCompound;

public abstract class CbCommand extends CbCommandI {
	
	//la commande comprend un commandsender, une localisation (imposée par le execute at), le plugin, le plot à la commande est exécutée et les arguments de la commande
	public CbCommand(CommandType cmdType, CommandSender sender, Location sendingLoc, OlympaCreatifMain plugin, Plot plot, String[] commandString) {
		super(cmdType, sender, sendingLoc, plugin, plot, commandString);
	}
	
	//get item from string, prend en compte material et tags
	public static ItemStack getItemFromString(String s) {
		if (s == null)
			return null;
		
		String matStr = s.replace("minecraft:", "");

		Material mat = Material.getMaterial(matStr.split("\\{")[0].toUpperCase());
		
		if (mat == null)
			return null;
		
		ItemStack item = new ItemStack(mat);
		
		if (!s.contains("{"))  
			return item;
		
		try {			
			NBTTagCompound tag = NBTcontrollerUtil.getValidTags(MojangsonParser.parse(s.substring(s.indexOf("{"))));
			net.minecraft.server.v1_15_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
			nmsItem.setTag(tag);
			item = CraftItemStack.asBukkitCopy(nmsItem);
		} catch (CommandSyntaxException e) {
			return null;
		}
		
		return item;
	}
	
	//renvoie une localisation absolue ou relative complète (null si err de syntaxe ou si hors du plot)
	protected Location parseLocation (String x, String y, String z) {
		
		Double xF = getUnverifiedPoint(x, sendingLoc.getX());
		Double yF = getUnverifiedPoint(y, sendingLoc.getY());
		Double zF = getUnverifiedPoint(z, sendingLoc.getZ());		
		 
		if (xF != null && yF != null && zF != null) {
			Location loc = new Location(plugin.getWorldManager().getWorld(), xF, yF, zF); 
			if (plot.getPlotId().isInPlot(loc))
				return loc;
			else
				return null;
		}else
			return null;
	}
	
	//renvoie la coordonnée x, y ou z à partir du string (en coordonnée absolue ou relative)
	private Double getUnverifiedPoint(String s, double potentialVectorValueToAdd) {
		
		try{
			return Double.valueOf(s);
		}catch(NumberFormatException e) {
		}
		
		if (s.contains("~"))
			if (s.length() >= 2)
				try{
					return Double.valueOf(s.replaceFirst("~", "")) + potentialVectorValueToAdd;	
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
		String s = cmd.split(" ")[0].replaceFirst("/", "");
		return CommandType.get(s.split(":")[s.split(":").length - 1]);
	}
	
	public static CbCommand getCommand(OlympaCreatifMain plugin, CommandSender sender, Location loc, String fullCommand) {
		Plot plot = null;

		if (sender instanceof Entity) {
			plot = plugin.getPlotsManager().getPlot(((Entity) sender).getLocation());	
		}
		if (((sender instanceof CraftBlockCommandSender) && ((CraftBlockCommandSender) sender).getBlock().getState() instanceof CommandBlock)) {
			plot = plugin.getPlotsManager().getPlot(((CraftBlockCommandSender) sender).getBlock().getState().getLocation());	
		}
		
		if (plot == null)
			return null;
		
		CommandType type = getCommandType(fullCommand);
		
		if (type == null)
			return null;

		//extraction des arguments de la commande
		String[] args = fullCommand.split(" ");
		
		List<String> list = new ArrayList<String>(Arrays.asList(args));
		List<String> concatList = new ArrayList<String>();
		
		//conctat les tags en plusieurs morceaux
		int accoladesCount = 0;
		int crochetsCount = 0;
		String concat = "";
		
		for (String s : list) {
			
			if (concat.equals(""))
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
		
		switch (type) {
		case gamemode:
			return new CmdGamemode(type, sender, loc, plugin, plot, args);
		case gm:
			return new CmdGamemode(type, sender, loc, plugin, plot, args);
		case bossbar:
			return new CmdBossBar(type, sender, loc, plugin, plot, args);
		case setblock:
			return new CmdSetblock(type, sender, loc, plugin, plot, args);
		case clear:
			return new CmdClear(type, sender, loc, plugin, plot, args);
		case enchant:
			return new CmdEnchant(type, sender, loc, plugin, plot, args);
		case execute:
			return new CmdExecute(type, sender, loc, plugin, plot, args);
		case experience:
			return new CmdExperience(type, sender, loc, plugin, plot, args);
		case give:
			return new CmdGive(type, sender, loc, plugin, plot, args);
		case tellraw:
			return new CmdTellraw(type, sender, loc, plugin, plot, args);
		case scoreboard:
			return new CmdScoreboard(type, sender, loc, plugin, plot, args);
		case team:
			return new CmdTeam(type, sender, loc, plugin, plot, args);
		case teleport:
			return new CmdTeleport(type, sender, loc, plugin, plot, args);
		case tp:
			return new CmdTeleport(type, sender, loc, plugin, plot, args);
		case effect:
			return new CmdEffect(type, sender, loc, plugin, plot, args);
		case summon:
			return new CmdSummon(type, sender, loc, plugin, plot, args);
		case kill:
			return new CmdKill(type, sender, loc, plugin, plot, args);
		case say:
			return new CmdSay(type, sender, loc, plugin, plot, args);
		case me:
			return new CmdSay(type, sender, loc, plugin, plot, args);
		case trigger:
			return new CmdTrigger(type, sender, loc, plugin, plot, args);
		case replaceitem:
			return new CmdReplaceitem(type, sender, loc, plugin, plot, args);
		//easter egg
		case op:
			return new CmdOp(type, sender, loc, plugin, plot, args);
		default:
			return null;
		}
	}
	
	public enum CommandType{
		teleport,
		tp,
		tellraw,
		execute,
		team,
		scoreboard,
		bossbar,
		clear,
		give,
		enchant,
		experience, 
		effect, 
		summon, 
		kill,
		say, 
		me, 
		setblock, 
		gamemode, 
		gm, 
		op,
		trigger,
		replaceitem,
		;
		
		public static CommandType get(String s) {
			for (CommandType cmd : CommandType.values())
				if (cmd.toString().equals(s))
					return cmd;
			return null;
		}
	}
	
	public int execute() {
		return 0;
	}
	
}
