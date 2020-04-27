package fr.olympa.olympacreatif.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.objects.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.gui.MainGui;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotId;
import fr.olympa.olympacreatif.plot.PlotParamType;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;

public class OcCommand extends OlympaCommand {

	private OlympaCreatifMain plugin;
	private Map<Player, Plot> pendingInvitations = new HashMap<Player, Plot>();
	
	public OcCommand(OlympaCreatifMain plugin, String command, String[] alias) {
		super(plugin, command, alias);
		this.plugin = plugin;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player)
			if (!plugin.getPlotsManager().isPlayerLoaded((Player) sender)) {
				sender.sendMessage("§4Chargement des données en cours, commande annulée...");
				return false;	
			}
		
		Player p = (Player) sender;
		
		switch (args.length) {
		case 1:
			switch(args[0]) {
			case "help":
				sender.sendMessage(Message.COMMAND_HELP.getValue());
				break;
			case "find":
				if (!(sender instanceof Player))
					break;
				//teste si le joueur a encore des plots dispo
				if (plugin.getPlotsManager().getAvailablePlotSlotsLeftOwner(p) > 0) {
					if (plugin.getPlotsManager().getAvailablePlotSlotsLeftTotal(p) > 0) {
						
						Plot plot = plugin.getPlotsManager().createPlot(p);
						p.teleport(plot.getId().getLocation());
						sender.sendMessage(Message.PLOT_NEW_CLAIM.getValue());	
						
					}else
						sender.sendMessage(Message.MAX_PLOT_COUNT_REACHED.getValue());
				}else
					sender.sendMessage(Message.MAX_PLOT_COUNT_OWNER_REACHED.getValue());
				break;
			case "menu":
				if (sender instanceof Player) {
					Plot plot = plugin.getPlotsManager().getPlot(p.getLocation());
					if (plot == null)
						new MainGui(plugin, p, plot, "§9Menu").create(p);
					else
						new MainGui(plugin, p, plot, "§9Menu >> " + plot.getId().getAsString()).create(p);
				}
				break;
			case "accept":
				if (pendingInvitations.containsKey(sender)) {
					if (plugin.getPlotsManager().getAvailablePlotSlotsLeftTotal(p) > 0) {
						sender.sendMessage(Message.PLOT_ACCEPTED_INVITATION.getValue());
						pendingInvitations.get(sender).getMembers().set(p, PlotRank.MEMBER);
						pendingInvitations.remove(sender);
					}else {
						sender.sendMessage(Message.MAX_PLOT_COUNT_REACHED.getValue());
					}
				}else
					sender.sendMessage(Message.PLOT_NO_PENDING_INVITATION.getValue());
				break;
			case "center":
				Plot plot = plugin.getPlotsManager().getPlot(p.getLocation());
				if (plot == null || plot.getMembers().getPlayerLevel(p) > 0)
					p.sendMessage(Message.PLOT_INSUFFICIENT_PERMISSION.getValue());
				else {
					p.sendMessage(Message.TELEPORT_PLOT_CENTER.getValue());
					double x = plot.getId().getLocation().getX() + (double)Integer.valueOf(Message.PARAM_PLOT_X_SIZE.getValue())/2.0;
					double z = plot.getId().getLocation().getZ() + (double)Integer.valueOf(Message.PARAM_PLOT_Z_SIZE.getValue())/2.0;
					
					p.teleport(new Location(plugin.getWorldManager().getWorld(), 
							x, plugin.getWorldManager().getWorld().getHighestBlockYAt((int)x, (int)z), z));
				}
					
				break;
			default:
				sender.sendMessage(Message.COMMAND_HELP.getValue());
				break;
			}
			break;
		case 2:
			switch(args[0]) {
			case "tp":
				PlotId id = PlotId.fromString(plugin, args[1]);
				if (id != null) {
					(p).teleport(id.getLocation());
					sender.sendMessage(Message.TELEPORT_IN_PROGRESS.getValue());
				}else {
					sender.sendMessage(Message.INVALID_PLID_ID.getValue());
				}
				break;
			case "invite":
				if (!(sender instanceof Player))
					return false;
				Plot plot = plugin.getPlotsManager().getPlot((p).getLocation());
				Player target = Bukkit.getPlayer(args[1]);
				if (plot != null)
					if (plot.getMembers().getPlayerLevel(p) >= 3)
						if (target != null)
							if (plot.getMembers().getPlayerRank(target) == PlotRank.VISITOR) {
								pendingInvitations.put(target, plot);
								target.sendMessage(Message.PLOT_RECIEVE_INVITATION.getValue().replace("%player%", sender.getName().replace("%plot%", plot.getId().getAsString())));
								sender.sendMessage(Message.PLOT_SEND_INVITATION.getValue().replace("%player%", target.getName().replace("%plot%", plot.getId().getAsString())));
							}else
								sender.sendMessage(Message.PLOT_INVITATION_TARGET_ALREADY_MEMBER.getValue());
						else
							sender.sendMessage(Message.PLAYER_TARGET_OFFLINE.getValue());
					else
						sender.sendMessage(Message.PLOT_INSUFFICIENT_PERMISSION.getValue());
				else
					sender.sendMessage(Message.PLOT_NULL_PLOT.getValue());
				break;
			case "kick":
				if (!(sender instanceof Player))
					return false;
				Plot plot2 = plugin.getPlotsManager().getPlot((p).getLocation());
				Player target2 = Bukkit.getPlayer(args[1]);
				
				Bukkit.broadcastMessage(plot2.getId().getAsString() + " ; " + target2.toString());
				
				if (plot2 != null)
					if (plot2.getMembers().getPlayerLevel(p) >= 3)
						if (target2 != null)
							if (plot2.getMembers().getPlayerRank(target2) == PlotRank.VISITOR && plot2.getPlayers().contains(target2)) {
								plot2.teleportOut(target2);
								target2.sendMessage(Message.PLOT_HAVE_BEEN_KICKED.getValue());
								sender.sendMessage(Message.PLOT_KICK_PLAYER.getValue().replace("%player%", target2.getDisplayName()));
								return false;
							}
				sender.sendMessage(Message.PLOT_IMPOSSIBLE_TO_KICK_PLAYER.getValue().replace("%player%", target2.getDisplayName()));
				break;
			case "ban":
				if (!(sender instanceof Player))
					return false;
				Plot plot3 = plugin.getPlotsManager().getPlot((p).getLocation());
				Player target3 = Bukkit.getPlayer(args[1]);
				if (plot3 != null)
					if (plot3.getMembers().getPlayerLevel(p) >= 3) {
						if (target3 != null) {
							if (plot3.getMembers().getPlayerRank(target3) == PlotRank.VISITOR && plot3.getPlayers().contains(target3)) {
								((ArrayList<Long>) plot3.getParameters().getParameter(PlotParamType.BANNED_PLAYERS)).add(AccountProvider.get(target3.getUniqueId()).getId());
								plot3.teleportOut(target3);
								target3.sendMessage(Message.PLOT_HAVE_BEEN_BANNED.getValue());
								sender.sendMessage(Message.PLOT_BAN_PLAYER.getValue().replace("%player%", target3.getDisplayName()));
								return false;
							}
						}
					}else {
						sender.sendMessage(Message.PLOT_INSUFFICIENT_PERMISSION.getValue());
						return false;
					}
				sender.sendMessage(Message.PLOT_IMPOSSIBLE_TO_BAN_PLAYER.getValue().replace("%player%", target3.getDisplayName()));
				break;
			case "unban":
				if (!(sender instanceof Player))
					return false;
				Plot plot4 = plugin.getPlotsManager().getPlot((p).getLocation());
				Player target4 = Bukkit.getPlayer(args[1]);
				if (plot4 != null && target4 != null)
					if (plot4.getMembers().getPlayerLevel(p) >= 3)
						if (((ArrayList<Long>) plot4.getParameters().getParameter(PlotParamType.BANNED_PLAYERS)).remove(AccountProvider.get(target4.getUniqueId()).getId()))
							sender.sendMessage(Message.PLOT_UNBAN_PLAYER.getValue());
						else
							sender.sendMessage(Message.PLOT_CANT_UNBAN_PLAYER.getValue());
					else
						sender.sendMessage(Message.PLOT_INSUFFICIENT_PERMISSION.getValue());
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
		List<String> list = new ArrayList<String>();
		List<String> response = new ArrayList<String>();
		
		if (args.length == 1) {
			list.add("help");
			list.add("find");
			list.add("menu");
			list.add("center");
			list.add("invite");
			list.add("accept");
			list.add("tp");
			list.add("kick");
			list.add("ban");
			list.add("unban");
			list.add("skull");
			for (String s : list)
				if (s.startsWith(args[0]))
					response.add(s);
		}
		else
			for (Player p : Bukkit.getOnlinePlayers())
				response.add(p.getName());

		return response;
	}
}
