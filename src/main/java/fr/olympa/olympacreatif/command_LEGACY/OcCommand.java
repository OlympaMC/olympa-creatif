package fr.olympa.olympacreatif.command_LEGACY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OCparam;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.gui.IGui;
import fr.olympa.olympacreatif.gui.MainGui;
import fr.olympa.olympacreatif.gui.MembersGui;
import fr.olympa.olympacreatif.gui.PlayerPlotsGui;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotId;
import fr.olympa.olympacreatif.plot.PlotParamType;
import fr.olympa.olympacreatif.plot.PlotPerm;
import fr.olympa.olympacreatif.plot.PlotPerm.PlotRank;
import fr.olympa.olympacreatif.world.WorldManager;

public class OcCommand extends OlympaCommand {

	public static final List<String> subArgsList = ImmutableList.<String>builder()
			.add("help")
			.add("menu")
			.add("find")
			.add("invite")
			.add("accept")
			.add("chat")
			.add("kick")
			.add("ban")
			.add("unban")
			.add("banlist")
			.add("visit")
			.add("plots")
			.add("members")
			.add("setspawn")
			.add("center")
			.add("spawn")
			.add("pspawn")
			.build();

	private OlympaCreatifMain plugin;
	private Map<Player, Plot> pendingInvitations = new HashMap<Player, Plot>();
	
	public OcCommand(OlympaCreatifMain plugin, String command, String[] alias) {
		super(plugin, command, alias);
		this.plugin = plugin;
	}
	
	/*
	@SuppressWarnings("unchecked")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return false;
		
		args = updatedArgs(label, "oc", args);
		
		Player p = (Player) sender;
		OlympaPlayerCreatif pc = AccountProvider.get(player.getUniqueId());
		
		Player target = null;
		Plot plot;
		
		if (args.length > 0 && args[0].equals("chat")) {
			plot = plugin.getPlotsManager().getPlot(p.getLocation());
			if (plot == null)
				p.sendMessage(OCmsg.INVALID_PLOT_ID.getValue());
			else {
				String concat = "";
				List<String> argsMsg = new ArrayList<String>(Arrays.asList(args));
				argsMsg.remove(0);
				
				for (String s : argsMsg)
					concat += s + " ";
				
				plot.sendMessage(pc, concat);
			}
			return false;
		}
		
		switch (args.length) {
		case 1:
			switch(args[0]) {
			case "help":
				sender.sendMessage(OCmsg.COMMAND_HELP.getValue());
				break;
				
			case "spawn":
				OCparam.SPAWN_LOC.get().teleport(p);
				sender.sendMessage(OCmsg.TELEPORTED_TO_WORLD_SPAWN.getValue());
				break;
				
			case "pspawn":
				plot = plugin.getPlotsManager().getPlot(p.getLocation());
				if (plot == null) {
					p.sendMessage(OCmsg.INVALID_PLOT_ID.getValue());
					break;
				}
				p.teleport(plot.getParameters().getSpawnLoc());
				sender.sendMessage(OCmsg.TELEPORTED_TO_PLOT_SPAWN.getValue(plot));
				break;
				
			case "find":
				
				//teste si le joueur a encore des plots dispo
				if (pc.getPlotsSlots(true) - pc.getPlots(true).size() > 0) {
					if (pc.getPlotsSlots(false) - pc.getPlots(false).size() > 0) {
						
						plot = plugin.getPlotsManager().createNewPlot(pc);
						p.teleport(plot.getPlotId().getLocation());
						//PlotsInstancesListener.executeEntryActions(plugin, p, plot);
						sender.sendMessage(OCmsg.PLOT_NEW_CLAIM.getValue(plot));	
						
					}else
						sender.sendMessage(OCmsg.MAX_PLOT_COUNT_REACHED.getValue());
				}else
					sender.sendMessage(OCmsg.MAX_PLOT_COUNT_OWNER_REACHED.getValue());
				break;
				
			case "menu":
				if (sender instanceof Player) 
					MainGui.getMainGui(pc).create(p);
				break;
				
			case "accept":
				if (pendingInvitations.containsKey(sender)) {
					
					//si le joueur a assez de slots pour rejoindre le plot
					if (pc.getPlotsSlots(false) - pc.getPlots(false).size() > 0) {
						//si le plot a assez de slots pour accueillir un nouveau membre
						if (pendingInvitations.get(sender).getMembers().getMaxMembers() > pendingInvitations.get(sender).getMembers().getCount()) {
							sender.sendMessage(OCmsg.PLOT_ACCEPTED_INVITATION.getValue(pendingInvitations.get(sender)));
							
							pendingInvitations.get(sender).getPlayers().forEach(pp -> {
								if (PlotPerm.INVITE_MEMBER.has(pc))
									pp.sendMessage(OCmsg.PLOT_PLAYER_JOIN.getValue(sender.getName()));
							});
							
							pendingInvitations.remove(sender).getMembers().set(p, PlotRank.MEMBER);	
						}else
							sender.sendMessage(OCmsg.PLOT_JOIN_ERR_NOT_ENOUGH_SLOTS.getValue(pendingInvitations.get(sender)));
						
					}else {
						sender.sendMessage(OCmsg.MAX_PLOT_COUNT_REACHED.getValue());
					}
				}else
					sender.sendMessage(OCmsg.PLOT_NO_PENDING_INVITATION.getValue());
				break;
				
			case "center":
				plot = plugin.getPlotsManager().getPlot(p.getLocation());
				if (plot == null || PlotPerm.BUILD.has(plot, pc))
					p.sendMessage(OCmsg.INSUFFICIENT_PLOT_PERMISSION.getValue());
				else {
					p.sendMessage(OCmsg.TELEPORT_PLOT_CENTER.getValue());
					double x = plot.getPlotId().getLocation().getX() + (double)OCparam.PLOT_SIZE.get()/2.0;
					double z = plot.getPlotId().getLocation().getZ() + (double)OCparam.PLOT_SIZE.get()/2.0;
					
					p.teleport(new Location(plugin.getWorldManager().getWorld(), 
							x, plugin.getWorldManager().getWorld().getHighestBlockYAt((int)x, (int)z) + 1, z));
				}
				break;
				
			case "setspawn":
				plot = plugin.getPlotsManager().getPlot(p.getLocation());
				if (plot == null || !PlotPerm.SET_PLOT_SPAWN.has(plot, pc))
					p.sendMessage(OCmsg.INSUFFICIENT_PLOT_PERMISSION.getValue());
				else {
					plot.getParameters().setSpawnLoc(p.getLocation());
					p.sendMessage(OCmsg.PLOT_SPAWN_LOC_SET.getValue(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ()));
				}
				
			break;
			
			case "members":
				IGui main = MainGui.getMainGui(pc);
				if (main.getPlot() != null)
					new MembersGui(main).create(p);
				else
					p.sendMessage(OCmsg.INVALID_PLOT_ID.getValue());
			break;
			
			case "plots":
				new PlayerPlotsGui(MainGui.getMainGui(pc)).create(p);
			break;
			
			case "banlist":
				plugin.getTask().runTaskAsynchronously(() -> {
					Plot plot2 = pc.getCurrentPlot();
					String msg = "§e >> Joueurs bannis de la parcelle " + plot2 + " : ";
					
					List<Long> list = plot2.getParameters().getParameter(PlotParamType.BANNED_PLAYERS);
					
					for (int i = 0 ; i < list.size() ; i++) {
						msg += "§e" + AccountProvider.getPlayerInformations(list.get(i)).getName();
						if (i < list.size() - 1)
							msg += ", ";
					}
					
					if (list.size() == 0)
						msg += "§7aucun";
					
					p.sendMessage(msg);
				});
				
					
				break;
			
			default:
				sender.sendMessage(OCmsg.COMMAND_HELP.getValue());
				break;
			}
			break;
		case 2:
			switch(args[0]) {
			
			case "menu":
				if (sender instanceof Player) 
					MainGui.getMainGui(pc, args[1]).create((Player)sender);
			break;
				
			case "visit":
				if (!StringUtils.isNumeric(args[1])) {
					Bukkit.dispatchCommand(sender, "oc visit " + args[1] + " 1");
					break;
				}
				
				PlotId id = PlotId.fromString(plugin, args[1]);
				if (id != null) {
					plot = plugin.getPlotsManager().getPlot(id);
					if (plot == null)
						p.teleport(id.getLocation());
					else
						p.teleport(plot.getParameters().getSpawnLoc());
					
					sender.sendMessage(OCmsg.TELEPORT_IN_PROGRESS.getValue(id));
				}else {
					sender.sendMessage(OCmsg.INVALID_PLOT_ID.getValue());
				}
			break;
				
			case "invite":
				
				plot = plugin.getPlotsManager().getPlot((p).getLocation());
				target = Bukkit.getPlayer(args[1]);
				
				if (plot != null)
					if (PlotPerm.INVITE_MEMBER.has(plot, pc))
						if (target != null)
							if (plot.getMembers().getPlayerRank(target) == PlotRank.VISITOR) {
								if (plot.getMembers().getMaxMembers() > plot.getMembers().getCount()) {
									pendingInvitations.put(target, plot);
									target.sendMessage(OCmsg.PLOT_RECIEVE_INVITATION.getValue(plot, sender.getName()));
									sender.sendMessage(OCmsg.PLOT_SEND_INVITATION.getValue(target.getName()));	
								}else
									sender.sendMessage(OCmsg.PLOT_INSUFFICIENT_MEMBERS_SIZE.getValue());
							}else
								sender.sendMessage(OCmsg.PLOT_INVITATION_TARGET_ALREADY_MEMBER.getValue(target.getName()));
						else
							sender.sendMessage(OCmsg.PLAYER_TARGET_OFFLINE.getValue());
					else
						sender.sendMessage(OCmsg.INSUFFICIENT_PLOT_PERMISSION.getValue());
				else
					sender.sendMessage(OCmsg.INVALID_PLOT_ID.getValue());
				break;
				
			case "kick":

				plot = plugin.getPlotsManager().getPlot((p).getLocation());
				target = Bukkit.getPlayer(args[1]);
				
				//vérifications avant kick
				if (plot == null)
					p.sendMessage(OCmsg.INVALID_PLOT_ID.getValue());
				
				else if (!PlotPerm.KICK_VISITOR.has(plot, pc))
					p.sendMessage(OCmsg.INSUFFICIENT_PLOT_PERMISSION.getValue());
				
				else if (plot.getMembers().getPlayerRank(target) != PlotRank.VISITOR || !plot.getPlayers().contains(target))
					p.sendMessage(OCmsg.PLOT_IMPOSSIBLE_TO_KICK_PLAYER.getValue());
				
				else if (((OlympaPlayerCreatif) AccountProvider.get(target.getUniqueId())).hasStaffPerm(StaffPerm.BYPASS_KICK_AND_BAN))
					p.sendMessage(OCmsg.PLOT_IMPOSSIBLE_TO_KICK_PLAYER.getValue());
				
				else {
					//exécution du kick					
					plot.teleportOut(target);
					target.sendMessage(OCmsg.PLOT_HAVE_BEEN_KICKED.getValue());
					sender.sendMessage(OCmsg.PLOT_KICK_PLAYER.getValue(target.getName()));
					return false;	
				}
				
				
				break;
				
			case "ban":

				plot = plugin.getPlotsManager().getPlot((p).getLocation());
				target = Bukkit.getPlayer(args[1]);
				
				//vérifications avant ban
				if (plot == null)
					p.sendMessage(OCmsg.INVALID_PLOT_ID.getValue());
				
				else if (!PlotPerm.BAN_VISITOR.has(plot, pc))
					p.sendMessage(OCmsg.INSUFFICIENT_PLOT_PERMISSION.getValue());
				
				else if (!plot.getPlayers().contains(target) || plot.getMembers().getPlayerRank(target) == PlotRank.OWNER)
					p.sendMessage(OCmsg.PLOT_IMPOSSIBLE_TO_BAN_PLAYER.getValue(target.getName()));
				
				else if (((OlympaPlayerCreatif) AccountProvider.get(target.getUniqueId())).hasStaffPerm(StaffPerm.BYPASS_KICK_AND_BAN))
					p.sendMessage(OCmsg.PLOT_IMPOSSIBLE_TO_BAN_PLAYER.getValue(target.getName()));
				
				else {
					//exécution du ban
					plot.getParameters().getParameter(PlotParamType.BANNED_PLAYERS).add(AccountProvider.get(target.getUniqueId()).getId());
					
					plot.teleportOut(target);
					plot.getMembers().set(target, PlotRank.VISITOR);
					
					target.sendMessage(OCmsg.PLOT_HAVE_BEEN_BANNED.getValue(plot, sender.getName()));
					sender.sendMessage(OCmsg.PLOT_BAN_PLAYER.getValue(target.getName()));
					return false;	
				}

				break;
				
			case "unban":

				plot = plugin.getPlotsManager().getPlot((p).getLocation());
				target = Bukkit.getPlayer(args[1]);

				//vérifications avant unban
				if (plot == null) 
					p.sendMessage(OCmsg.INVALID_PLOT_ID.getValue());	
				
				else if (target == null)
					p.sendMessage(OCmsg.PLAYER_TARGET_OFFLINE.getValue());
				
				else if (PlotPerm.BAN_VISITOR.has(plot, pc))
					p.sendMessage(OCmsg.INSUFFICIENT_PLOT_PERMISSION.getValue());
				
				else if (plot.getParameters().getParameter(PlotParamType.BANNED_PLAYERS).remove(AccountProvider.get(target.getUniqueId()).getId()))
					sender.sendMessage(OCmsg.PLOT_UNBAN_PLAYER.getValue(target.getName()));
				else
					sender.sendMessage(OCmsg.PLOT_CANT_UNBAN_PLAYER.getValue(target.getName()));
				
				break;
			default:
				sender.sendMessage(OCmsg.COMMAND_HELP.getValue());
				break;
			}
			break;
			
		case 3:
			switch(args[0]) {
			case "visit":					
				try {
					Player plotOwner = Bukkit.getPlayer(args[1]);
					
					if (plotOwner == null) {
						sender.sendMessage(OCmsg.PLAYER_TARGET_OFFLINE.getValue());
						break;
					}
					
					List<Plot> plots = ((OlympaPlayerCreatif)AccountProvider.get(plotOwner.getUniqueId())).getPlots(true);
					
					int index = Integer.valueOf(args[2]) - 1;
					
					if (index >= 0 && index < plots.size()) {
						p.teleport(plots.get(index).getParameters().getSpawnLoc());
						p.sendMessage(OCmsg.TELEPORT_IN_PROGRESS.getValue(plots.get(index)));
					}else
						sender.sendMessage(OCmsg.INVALID_PLOT_ID.getValue());						
						
				}catch(NumberFormatException e) {
					sender.sendMessage(OCmsg.INVALID_PLOT_ID.getValue());
				}
				
				break;
				
			default:
				sender.sendMessage(OCmsg.COMMAND_HELP.getValue());
				break;
			}
			break;
			
		default:
			sender.sendMessage(OCmsg.COMMAND_HELP.getValue());
			break;
		}
		
		return false;
	}*/

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {

		args = OcCommand.updatedArgs(label, "oc", args);
		
		List<String> list = new ArrayList<String>();
		List<String> response = new ArrayList<String>();
		
		if (args.length == 1)
			list.addAll(subArgsList);
		if (args.length == 2) {
			switch(args[0]) {
			case "ban":
				for (Player p : Bukkit.getOnlinePlayers())
					list.add(p.getName());
				break;
				
			case "unban":
				for (Player p : Bukkit.getOnlinePlayers())
					list.add(p.getName());
				break;
				
			case "kick":
				for (Player p : Bukkit.getOnlinePlayers())
					list.add(p.getName());
				break;
	
			case "invite":
				for (Player p : Bukkit.getOnlinePlayers())
					list.add(p.getName());
				break;
			}
		}

		for (String s : list)
			if (s.startsWith(args[args.length-1]))
				response.add(s);
		
		return response;
	}
	
	public static String[] updatedArgs(String label, String wantedCmd, String[] args) {
		if (label.equals(wantedCmd))
			return args;
		
		List<String> list = new ArrayList<String>(Arrays.asList(args));
		list.add(0, label);
		return (String[]) list.toArray(new String[list.size()]);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		return false;
	}
}
