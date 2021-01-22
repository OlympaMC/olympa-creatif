package fr.olympa.olympacreatif.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableList;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.CommandBlocksManager;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OCparam;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.gui.MainGui;
import fr.olympa.olympacreatif.gui.ShopGui;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotPerm;
import fr.olympa.olympacreatif.world.WorldManager;

public class OcoCommand extends OlympaCommand {

	public static final List<String> subArgsList = ImmutableList.<String>builder()
			.add("help")
			.add("skull")
			.add("hat")
			.add("mb")
			.add("export")
			.add("speed")
			.add("debug")
			.add("debugentities")
			.add("shop")
			.build();
	
	private OlympaCreatifMain plugin;
	
	public OcoCommand(OlympaCreatifMain plugin, String cmd, String[] aliases) {
		super(plugin, cmd, aliases);
		this.plugin = plugin;
	}
/*
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return false;
		
		OlympaPlayerCreatif p = AccountProvider.get(((Player)sender).getUniqueId());
		Plot plot = plugin.getPlotsManager().getPlot(p.getPlayer().getLocation());
		
		args = OcCommand.updatedArgs(label, "oco", args);			
		
		switch (args.length) {
		case 1:
			switch (args[0]) {
			case "hat":
				/*if (!PermissionsList.USE_HAT_COMMAND.hasPermission(p)) {
					p.getPlayer().sendMessage(OCmsg.INSUFFICIENT_GROUP_PERMISSION.getValue(PermissionsList.USE_HAT_COMMAND.getMinGroup().getName(p.getGender())));
					return false;
				}
				p.getPlayer().sendMessage(OCmsg.OCO_HAT_SUCCESS.getValue());
				p.getPlayer().getInventory().setHelmet(new ItemStack(p.getPlayer().getInventory().getItemInMainHand().getType()));
				break;
				
				
			case "mb":
				if (!PermissionsList.USE_MICRO_BLOCKS.hasPermission(p)) {
					p.getPlayer().sendMessage(OCmsg.INSUFFICIENT_GROUP_PERMISSION.getValue(PermissionsList.USE_MICRO_BLOCKS.getMinGroup().getName(p.getGender())));
					return false;
				}
				plugin.getPerksManager().getMicroBlocks().openGui(p.getPlayer());
				break;
				
				
			case "export":
				if (!PermissionsList.USE_PLOT_EXPORTATION.hasPermission(p)) {
					p.getPlayer().sendMessage(OCmsg.INSUFFICIENT_GROUP_PERMISSION.getValue(PermissionsList.USE_PLOT_EXPORTATION.getMinGroup().getName(p.getGender())));
					return false;
				}
				if (plot != null && PlotPerm.EXPORT_PLOT.has(plot, p))
					plugin.getPerksManager().getSchematicCreator().export(plot, p);
				else
					p.getPlayer().sendMessage(OCmsg.OCO_EXPORT_FAILED.getValue(plot));					
					
				break;
				
				
			case "debugentities":
				if (plot == null) {
					sender.sendMessage(OCmsg.INVALID_PLOT_ID.getValue());
					break;
				}
				List<Entity> entList = plot.getEntities();
				Collections.sort(entList, new Comparator<Entity>() {
					@Override
					public int compare(Entity o1, Entity o2) {
						return o1.getType().toString().compareTo(o2.getType().toString());
					}
				});
				
				String deb = "\n   §6>>> Débug entités parcelle " + plot.getPlotId() + " :";
				for (Entity e : entList)
					deb += "\n   §e> " + e.getType().toString().toLowerCase() + "§7(" + (e.getCustomName() == null ? "" : e.getCustomName()) + "§7), " + 
							e.getLocation().getBlockX() + " " + e.getLocation().getBlockY() + " " + e.getLocation().getBlockZ() + " : " + 
							(!e.isDead() ? "§avivante" : "§cmorte §(contactez un staff)");
				
				sender.sendMessage(deb);
				break;
			
			case "debug":
				if (plot == null) {
					sender.sendMessage(OCmsg.INVALID_PLOT_ID.getValue());
					break;
				}
				
				String debug = "\n   §6>>> Débug parcelle " + plot.getPlotId() + " :";
				debug += "\n   §e> Joueurs : §a" + plot.getPlayers().size();
				debug += "\n   §e> Entités : §a" + plot.getEntities().size() + "/" + OCparam.MAX_TOTAL_ENTITIES_PER_PLOT.get() + " (max " + OCparam.MAX_ENTITIES_PER_TYPE_PER_PLOT.get() + " de chaque type) §7(détails avec /debugentities)";
				debug += "\n   §e> Equipes : §a" + plot.getCbData().getTeams().size() + "/" + OCparam.CB_MAX_TEAMS_PER_PLOT.get();
				debug += "\n   §e> Objectifs : §a" + plot.getCbData().getObjectives().size() + "/" + OCparam.CB_MAX_OBJECTIVES_PER_PLOT.get();
				debug += "\n   §e> Tickets commandblocks : §a" + plot.getCbData().getCommandsTicketsLeft() + "/" +
						OCparam.CB_MAX_CMDS_LEFT.get() + " (+" + plot.getCbData().getCpt() * 20 + "/s)";
				
				sender.sendMessage(debug);
				break;
				
			case "shop":
				new ShopGui(MainGui.getMainGui(p)).create(player);
				break;
				
			default:
				sender.sendMessage(OCmsg.OCO_COMMAND_HELP.getValue());
				break;
			}
			break;
		case 2:
			switch(args[0]) {
			case "mb":
				if (!PermissionsList.USE_MICRO_BLOCKS.hasPermission(p)) {
					p.getPlayer().sendMessage(OCmsg.INSUFFICIENT_GROUP_PERMISSION.getValue(PermissionsList.USE_MICRO_BLOCKS.getMinGroup().getName(p.getGender())));
					return false;
				}
				ItemStack item = plugin.getPerksManager().getMicroBlocks().getMb(args[1]);
				if (item != null) {
					p.getPlayer().getInventory().addItem(item);
					p.getPlayer().sendMessage(OCmsg.OCO_HEAD_GIVED.getValue());
				}else
					p.getPlayer().sendMessage(OCmsg.OCO_UNKNOWN_MB.getValue());
				break;
				
			case "skull":
				Consumer<ItemStack> consumer = sk -> p.getPlayer().getInventory().addItem(sk);
				ItemUtils.skull(consumer, "§6Tête de " + args[1], args[1]);
				p.getPlayer().sendMessage(OCmsg.OCO_HEAD_GIVED.getValue());
				break;
				
			case "speed":
				if (plot != null && !PlotPerm.DEFINE_OWN_FLY_SPEED.has(plot, p)) {
					p.getPlayer().sendMessage(OCmsg.INSUFFICIENT_PLOT_PERMISSION.getValue());
					return false;
				}
				
				float level = 0.1f;
				
				try {
					level = Math.min(Math.max(Float.valueOf(args[1])/18f, 0.1f), 1f);
				}catch(NumberFormatException e) {
				}
				
				p.getPlayer().setFlySpeed(level);
				p.getPlayer().sendMessage(OCmsg.OCO_SET_FLY_SPEED.getValue(args[1]));
				
				break;
				
			default:
				sender.sendMessage(OCmsg.OCO_COMMAND_HELP.getValue());
				break;
			}
			break;
		default:
			sender.sendMessage(OCmsg.OCO_COMMAND_HELP.getValue());
			break;
		}
		
		return false;
	}
*/
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		
		args = OcCommand.updatedArgs(label, "oco", args);
		
		List<String> list = new ArrayList<String>();
		List<String> response = new ArrayList<String>();
		
		if (args.length == 1)
			list.addAll(subArgsList);
		else if (args.length == 2 && args[0].equals("mb")) {
			for (Entry<String, ItemStack> e : plugin.getPerksManager().getMicroBlocks().getAllMbs().entrySet())
				list.add(e.getKey());
		}else
			for (Player p : Bukkit.getOnlinePlayers())
				list.add(p.getName());

		for (String s : list)
			if (s.startsWith(args[args.length-1]))
				response.add(s);
		
		return response;
	}
@Override
public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	// TODO Auto-generated method stub
	return false;
}
}
