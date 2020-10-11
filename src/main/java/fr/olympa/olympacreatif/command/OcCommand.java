package fr.olympa.olympacreatif.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.gui.IGui;
import fr.olympa.olympacreatif.gui.MainGui;
import fr.olympa.olympacreatif.gui.MembersGui;
import fr.olympa.olympacreatif.gui.PlayerPlotsGui;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotId;
import fr.olympa.olympacreatif.plot.PlotParamType;
import fr.olympa.olympacreatif.plot.PlotsInstancesListener;
import fr.olympa.olympacreatif.plot.PlotsManager;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;
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
			.build();

	private OlympaCreatifMain plugin;
	private Map<Player, Plot> pendingInvitations = new HashMap<Player, Plot>();
	
	public OcCommand(OlympaCreatifMain plugin, String command, String[] alias) {
		super(plugin, command, alias);
		this.plugin = plugin;
	}

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
				p.sendMessage(Message.INVALID_PLOT_ID.getValue());
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
				sender.sendMessage(Message.COMMAND_HELP.getValue());
				break;
				
			case "find":
				
				//teste si le joueur a encore des plots dispo
				if (pc.getPlotsSlots(true) - pc.getPlots(true).size() > 0) {
					if (pc.getPlotsSlots(false) - pc.getPlots(false).size() > 0) {
						
						plot = plugin.getPlotsManager().createPlot(p);
						p.teleport(plot.getPlotId().getLocation());
						//PlotsInstancesListener.executeEntryActions(plugin, p, plot);
						sender.sendMessage(Message.PLOT_NEW_CLAIM.getValue(plot));	
						
					}else
						sender.sendMessage(Message.MAX_PLOT_COUNT_REACHED.getValue());
				}else
					sender.sendMessage(Message.MAX_PLOT_COUNT_OWNER_REACHED.getValue());
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
							sender.sendMessage(Message.PLOT_ACCEPTED_INVITATION.getValue(pendingInvitations.get(sender)));
							
							pendingInvitations.get(sender).getPlayers().forEach(pp -> {
								if (pendingInvitations.get(sender).getMembers().getPlayerLevel(pp) >= 3)
									pp.sendMessage(Message.PLOT_PLAYER_JOIN.getValue(pp.getName()));
							});
							
							pendingInvitations.remove(sender).getMembers().set(p, PlotRank.MEMBER);	
						}else
							sender.sendMessage(Message.PLOT_JOIN_ERR_NOT_ENOUGH_SLOTS.getValue(pendingInvitations.get(sender)));
						
					}else {
						sender.sendMessage(Message.MAX_PLOT_COUNT_REACHED.getValue());
					}
				}else
					sender.sendMessage(Message.PLOT_NO_PENDING_INVITATION.getValue());
				break;
				
			case "center":
				plot = plugin.getPlotsManager().getPlot(p.getLocation());
				if (plot == null || plot.getMembers().getPlayerLevel(p) == 0)
					p.sendMessage(Message.INSUFFICIENT_PLOT_PERMISSION.getValue());
				else {
					p.sendMessage(Message.TELEPORT_PLOT_CENTER.getValue());
					double x = plot.getPlotId().getLocation().getX() + (double)WorldManager.plotSize/2.0;
					double z = plot.getPlotId().getLocation().getZ() + (double)WorldManager.plotSize/2.0;
					
					p.teleport(new Location(plugin.getWorldManager().getWorld(), 
							x, plugin.getWorldManager().getWorld().getHighestBlockYAt((int)x, (int)z) + 1, z));
				}
				break;
				
			case "setspawn":
				plot = plugin.getPlotsManager().getPlot(p.getLocation());
				if (plot == null || plot.getMembers().getPlayerLevel(p) < 3)
					p.sendMessage(Message.INSUFFICIENT_PLOT_PERMISSION.getValue());
				else {
					plot.getParameters().setSpawnLoc(p.getLocation());
					p.sendMessage(Message.PLOT_SPAWN_LOC_SET.getValue(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ()));
				}
				
			break;
			
			case "members":
				IGui main = MainGui.getMainGui(pc);
				if (main.getPlot() != null)
					new MembersGui(main).create(p);
				else
					p.sendMessage(Message.INVALID_PLOT_ID.getValue());
			break;
			
			case "plots":
				new PlayerPlotsGui(MainGui.getMainGui(pc)).create(p);
			break;
			
			case "banlist":
				plugin.getTask().runTaskAsynchronously(() -> {
					Plot plot2 = pc.getCurrentPlot();
					String msg = "§a >> Joueurs bannis de la parcelle " + plot2 + " : ";
					
					List<Long> list = new ArrayList<Long>(((HashSet<Long>) plot2.getParameters().getParameter(PlotParamType.BANNED_PLAYERS)));
					
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
				sender.sendMessage(Message.COMMAND_HELP.getValue());
				break;
			}
			break;
		case 2:
			switch(args[0]) {
			/*
			case "protectedarea":
				plot = plugin.getPlotsManager().getPlot(p.getLocation());
				if (plot == null) {
					p.sendMessage(Message.PLOT_NULL_PLOT.getValue());
					return false;
				}
				if (plot.getMembers().getPlayerRank(p) != PlotRank.OWNER) {
					p.sendMessage(Message.INSUFFICIENT_PLOT_PERMISSION.getValue());
					return false;
				}
				
				switch (args[1]) {
				case "create":
					err = plugin.getWorldEditManager().getPlayerInstance(p).isSelectionValid();
					if (err == WorldEditError.NO_ERROR) {
						p.sendMessage(Message.WE_CMD_PROTECTED_AREA_CREATION_SUCCESS.getValue());
						plot.getParameters().setParameter(PlotParamType.PROTECTED_ZONE_POS1, plugin.getWorldEditManager().getPlayerInstance(p).getPos1().clone());
						plot.getParameters().setParameter(PlotParamType.PROTECTED_ZONE_POS2, plugin.getWorldEditManager().getPlayerInstance(p).getPos2().clone());
					}else
						p.sendMessage(err.getErrorMessage().getValue());
					break;
					
				case "save":
					err = plugin.getWorldEditManager().getPlayerInstance(p).saveProtectedZone(plot);
					if (err == WorldEditError.NO_ERROR)
						p.sendMessage(Message.PLOT_PROTECTED_ZONE_SAVED.getValue());
					else
						p.sendMessage(err.getErrorMessage().getValue());
					break;
					
				case "restore":
					err = plugin.getWorldEditManager().getPlayerInstance(p).restaureProtectedZone(plot);
					if (err == WorldEditError.NO_ERROR)
						p.sendMessage(Message.PLOT_PROTECTED_ZONE_RESTORED.getValue());
					else
						p.sendMessage(err.getErrorMessage().getValue());
					break;

				default:
					sender.sendMessage(Message.COMMAND_HELP.getValue());
					break;
				}
				break;
				*/
			
		case "menu":
			if (sender instanceof Player) 
				MainGui.getMainGui(pc, args[1]).create((Player)sender);
			break;
				
			case "visit":
				PlotId id = PlotId.fromString(plugin, args[1]);
				if (id != null) {
					p.teleport(id.getLocation());
					sender.sendMessage(Message.TELEPORT_IN_PROGRESS.getValue(id));
				}else {
					sender.sendMessage(Message.INVALID_PLOT_ID.getValue());
				}
				break;
				
			case "invite":
				
				plot = plugin.getPlotsManager().getPlot((p).getLocation());
				target = Bukkit.getPlayer(args[1]);
				
				if (plot != null)
					if (plot.getMembers().getPlayerLevel(p) >= 3)
						if (target != null)
							if (plot.getMembers().getPlayerRank(target) == PlotRank.VISITOR) {
								if (plot.getMembers().getMaxMembers() > plot.getMembers().getCount()) {
									pendingInvitations.put(target, plot);
									target.sendMessage(Message.PLOT_RECIEVE_INVITATION.getValue(plot, sender.getName()));
									sender.sendMessage(Message.PLOT_SEND_INVITATION.getValue(target.getName()));	
								}else
									sender.sendMessage(Message.PLOT_INSUFFICIENT_MEMBERS_SIZE.getValue());
							}else
								sender.sendMessage(Message.PLOT_INVITATION_TARGET_ALREADY_MEMBER.getValue(target.getName()));
						else
							sender.sendMessage(Message.PLAYER_TARGET_OFFLINE.getValue());
					else
						sender.sendMessage(Message.INSUFFICIENT_PLOT_PERMISSION.getValue());
				else
					sender.sendMessage(Message.INVALID_PLOT_ID.getValue());
				break;
				
			case "kick":

				plot = plugin.getPlotsManager().getPlot((p).getLocation());
				target = Bukkit.getPlayer(args[1]);
				
				//vérifications avant kick
				if (plot == null)
					p.sendMessage(Message.INVALID_PLOT_ID.getValue());
				
				else if (plot.getMembers().getPlayerLevel(pc) < 3)
					p.sendMessage(Message.INSUFFICIENT_PLOT_PERMISSION.getValue());
				
				else if (plot.getMembers().getPlayerRank(target) != PlotRank.VISITOR || !plot.getPlayers().contains(target))
					p.sendMessage(Message.PLOT_IMPOSSIBLE_TO_KICK_PLAYER.getValue());
				
				else if (((OlympaPlayerCreatif) AccountProvider.get(target.getUniqueId())).hasStaffPerm(StaffPerm.BYPASS_KICK_AND_BAN))
					p.sendMessage(Message.PLOT_IMPOSSIBLE_TO_KICK_PLAYER.getValue());
				
				else {
					//exécution du kick					
					plot.teleportOut(target);
					target.sendMessage(Message.PLOT_HAVE_BEEN_KICKED.getValue());
					sender.sendMessage(Message.PLOT_KICK_PLAYER.getValue(target.getName()));
					return false;	
				}
				
				
				break;
				
			case "ban":

				plot = plugin.getPlotsManager().getPlot((p).getLocation());
				target = Bukkit.getPlayer(args[1]);
				
				//vérifications avant ban
				if (plot == null)
					p.sendMessage(Message.INVALID_PLOT_ID.getValue());
				
				else if (plot.getMembers().getPlayerLevel(pc) < 3)
					p.sendMessage(Message.INSUFFICIENT_PLOT_PERMISSION.getValue());
				
				else if (!plot.getPlayers().contains(target) || plot.getMembers().getPlayerRank(target) == PlotRank.OWNER)
					p.sendMessage(Message.PLOT_IMPOSSIBLE_TO_BAN_PLAYER.getValue(target.getName()));
				
				else if (((OlympaPlayerCreatif) AccountProvider.get(target.getUniqueId())).hasStaffPerm(StaffPerm.BYPASS_KICK_AND_BAN))
					p.sendMessage(Message.PLOT_IMPOSSIBLE_TO_BAN_PLAYER.getValue(target.getName()));
				
				else {
					//exécution du ban
					((HashSet<Long>) plot.getParameters().getParameter(PlotParamType.BANNED_PLAYERS)).add(AccountProvider.get(target.getUniqueId()).getId());
					
					plot.teleportOut(target);
					plot.getMembers().set(target, PlotRank.VISITOR);
					
					target.sendMessage(Message.PLOT_HAVE_BEEN_BANNED.getValue(plot, sender.getName()));
					sender.sendMessage(Message.PLOT_BAN_PLAYER.getValue(target.getName()));
					return false;	
				}

				break;
				
			case "unban":

				plot = plugin.getPlotsManager().getPlot((p).getLocation());
				target = Bukkit.getPlayer(args[1]);

				//vérifications avant unban
				if (plot == null) 
					p.sendMessage(Message.INVALID_PLOT_ID.getValue());	
				
				else if (target == null)
					p.sendMessage(Message.PLAYER_TARGET_OFFLINE.getValue());
				
				else if (plot.getMembers().getPlayerLevel(pc) < 3)
					p.sendMessage(Message.INSUFFICIENT_PLOT_PERMISSION.getValue());
				
				else if (((HashSet<Long>) plot.getParameters().getParameter(PlotParamType.BANNED_PLAYERS)).remove(AccountProvider.get(target.getUniqueId()).getId()))
					sender.sendMessage(Message.PLOT_UNBAN_PLAYER.getValue(target.getName()));
				else
					sender.sendMessage(Message.PLOT_CANT_UNBAN_PLAYER.getValue(target.getName()));
				
				break;
			default:
				sender.sendMessage(Message.COMMAND_HELP.getValue());
				break;
			}
			break;
			
		case 3:
			switch(args[0]) {
			case "visit":
				if (args.length < 3) {
					sender.sendMessage(Message.COMMAND_HELP.getValue());
					break;
				}
					
				try {
					Player plotOwner = Bukkit.getPlayer(args[1]);
					
					if (plotOwner == null) {
						sender.sendMessage(Message.PLAYER_TARGET_OFFLINE.getValue());
						break;
					}
					
					List<Plot> plots = plugin.getPlotsManager().getPlotsOf(plotOwner, true);
					
					int index = Integer.valueOf(args[2]) - 1;
					
					if (index < plots.size()) {
						p.teleport(plots.get(index).getParameters().getSpawnLoc());
						p.sendMessage(Message.TELEPORT_IN_PROGRESS.getValue(plots.get(index)));
					}else
						sender.sendMessage(Message.INVALID_PLOT_ID.getValue());						
						
				}catch(NumberFormatException e) {
					sender.sendMessage(Message.INVALID_PLOT_ID.getValue());
				}
				
				break;
				
			default:
				sender.sendMessage(Message.COMMAND_HELP.getValue());
				break;
			}
			break;
			
		default:
			sender.sendMessage(Message.COMMAND_HELP.getValue());
			break;
		}
		
		return false;
	}

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
}
