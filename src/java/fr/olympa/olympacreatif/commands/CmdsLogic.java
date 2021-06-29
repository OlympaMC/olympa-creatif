package fr.olympa.olympacreatif.commands;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.utils.Prefix;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OcPermissions;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotId;
import fr.olympa.olympacreatif.plot.PlotParamType;
import fr.olympa.olympacreatif.plot.PlotPerm;
import fr.olympa.olympacreatif.plot.PlotPerm.PlotRank;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class CmdsLogic {

	private OlympaCreatifMain plugin;
	private Map<PlotId, Entry<OlympaPlayerCreatif, Player>> invitations = new HashMap<>();

	private Set<Player> delayRandomPlotVisit = new HashSet<>();

	public CmdsLogic(OlympaCreatifMain plugin) {
		this.plugin = plugin;
	}

	public void claimNewPlot(OlympaPlayerCreatif pc) {//teste si le joueur a encore des plots dispo
		if (pc.getPlotsSlots(true) - pc.getPlots(true).size() > 0) {
			if (pc.getPlotsSlots(false) - pc.getPlots(false).size() > 0) {

				Plot plot = plugin.getPlotsManager().createNewPlot(pc);
				plot.getId().teleport((Player) pc.getPlayer());

				//TODO vérifier si le getCurrentPlot est bien MAJ
				OCmsg.PLOT_NEW_CLAIM.send(pc);
			} else
				OCmsg.MAX_PLOT_COUNT_REACHED.send(pc);
		} else
			OCmsg.MAX_PLOT_COUNT_OWNER_REACHED.send(pc);
	}

	public void invitePlayer(OlympaPlayerCreatif pc, Player target) {

		Plot plot = pc.getCurrentPlot();

		if (plot == null) {
			OCmsg.INVALID_PLOT_ID.send(pc);
			return;
		}

		if (!PlotPerm.INVITE_MEMBER.has(plot, pc)) {
			OCmsg.INSUFFICIENT_PLOT_PERMISSION.send(pc, PlotPerm.INVITE_MEMBER);
			return;
		}

		if (target == null) {
			OCmsg.PLAYER_TARGET_OFFLINE.send(pc);
			return;
		}

		if (plot.getMembers().getPlayerRank(target) != PlotRank.VISITOR) {
			OCmsg.PLOT_INVITATION_TARGET_ALREADY_MEMBER.send(pc, target.getName());
			return;
		}

		if (plot.getMembers().getMaxMembers() <= plot.getMembers().getCount()) {
			OCmsg.PLOT_INSUFFICIENT_MEMBERS_SIZE.send(pc);
			return;
		}

		invitations.put(plot.getId(), new AbstractMap.SimpleEntry<>(pc, target));
		OCmsg.PLOT_RECIEVE_INVITATION.send(target, plot, pc.getName());
		OCmsg.PLOT_SEND_INVITATION.send(pc, target.getName());
	}

	public void acceptInvitation(OlympaPlayerCreatif pc, int plotId) {

		Plot plot = plugin.getPlotsManager().getPlot(PlotId.fromId(plugin, plotId));

		String plotIdStr = plotId + "";

		if (plot == null)
			OCmsg.PLOT_UNLOADED.send(pc, plotIdStr);

		else if (pc.getPlotsSlots(false) - pc.getPlots(false).size() <= 0)
			OCmsg.MAX_PLOT_COUNT_REACHED.send(pc, plot);

		else if (!invitations.containsKey(plot.getId()) || !invitations.get(plot.getId()).getValue().equals(pc.getPlayer()))
			OCmsg.PLOT_NO_PENDING_INVITATION.send(pc, plot);

		else if (!((OfflinePlayer) invitations.get(plot.getId()).getKey().getPlayer()).isOnline())
			OCmsg.PLOT_JOIN_ERR_SENDER_OFFLINE.send(pc, plot, invitations.get(plot.getId()).getKey().getName());

		else if (plot.getMembers().getMembers().size() >= plot.getMembers().getMaxMembers())
			OCmsg.PLOT_JOIN_ERR_NOT_ENOUGH_SLOTS.send(pc, plot);

		else {
			OCmsg.PLOT_PLAYER_JOIN.send(invitations.remove(plot.getId()).getKey(), pc.getName());
			OCmsg.PLOT_ACCEPTED_INVITATION.send(pc, plot);
			plot.getMembers().set(pc, PlotRank.MEMBER);
		}
	}

	public void kickPlayerFromPlot(OlympaPlayerCreatif pc, Player target) {
		Plot plot = pc.getCurrentPlot();

		if (plot == null)
			OCmsg.INVALID_PLOT_ID.send(pc);

		else if (!PlotPerm.KICK_VISITOR.has(plot, pc))
			OCmsg.INSUFFICIENT_PLOT_PERMISSION.send(pc, PlotPerm.KICK_VISITOR);

		else if (plot.getMembers().getPlayerRank(target) != PlotRank.VISITOR || !plot.getPlayers().contains(target))
			OCmsg.PLOT_IMPOSSIBLE_TO_KICK_PLAYER.send(pc, target.getName());

		else if (((OlympaPlayerCreatif) AccountProviderAPI.getter().get(target.getUniqueId())).hasStaffPerm(StaffPerm.BYPASS_KICK_BAN))
			OCmsg.PLOT_IMPOSSIBLE_TO_KICK_PLAYER.send(pc, target.getName());

		else {
			//exécution du kick
			plot.teleportOut(target);
			OCmsg.PLOT_HAVE_BEEN_KICKED.send(target);
			OCmsg.PLOT_KICK_PLAYER.send(pc, target.getName());
		}
	}

	public void banPlayerFromPlot(OlympaPlayerCreatif pc, Player target) {

		Plot plot = pc.getCurrentPlot();

		//vérifications avant ban
		if (plot == null)
			OCmsg.INVALID_PLOT_ID.send(pc);

		else if (target == null)
			OCmsg.PLAYER_TARGET_OFFLINE.send(pc);

		else if (!PlotPerm.BAN_VISITOR.has(plot, pc))
			OCmsg.INSUFFICIENT_PLOT_PERMISSION.send(pc, PlotPerm.BAN_VISITOR);

		else if (!plot.getPlayers().contains(target) || plot.getMembers().getPlayerRank(target) != PlotRank.VISITOR)
			OCmsg.PLOT_IMPOSSIBLE_TO_BAN_PLAYER.send(pc, target.getName());

		else if (((OlympaPlayerCreatif) AccountProviderAPI.getter().get(target.getUniqueId())).hasStaffPerm(StaffPerm.BYPASS_KICK_BAN))
			OCmsg.PLOT_IMPOSSIBLE_TO_BAN_PLAYER.send(pc, target.getName());

		else {
			//exécution du ban
			plot.getParameters().getParameter(PlotParamType.BANNED_PLAYERS).add(AccountProviderAPI.getter().get(target.getUniqueId()).getId());

			plot.teleportOut(target);

			OCmsg.PLOT_HAVE_BEEN_BANNED.send(target, pc.getName());
			OCmsg.PLOT_BAN_PLAYER.send(pc, target.getName());
		}
	}

	public void unbanPlayerFromPlot(OlympaPlayerCreatif pc, Player target) {

		Plot plot = pc.getCurrentPlot();

		//vérifications avant unban
		if (plot == null)
			OCmsg.INVALID_PLOT_ID.send(pc);

		else if (target == null)
			OCmsg.PLAYER_TARGET_OFFLINE.send(pc);

		else if (!PlotPerm.BAN_VISITOR.has(plot, pc))
			OCmsg.INSUFFICIENT_PLOT_PERMISSION.send(pc, PlotPerm.BAN_VISITOR);

		else if (plot.getParameters().getParameter(PlotParamType.BANNED_PLAYERS).remove(AccountProviderAPI.getter().get(target.getUniqueId()).getId()))
			OCmsg.PLOT_UNBAN_PLAYER.send(pc, target.getName());
		else
			OCmsg.PLOT_CANT_UNBAN_PLAYER.send(pc, target.getName());
	}

	public void sendBanList(OlympaPlayerCreatif pc) {
		Plot plot = pc.getCurrentPlot();

		List<Long> list = plot.getParameters().getParameter(PlotParamType.BANNED_PLAYERS);
		ComponentBuilder component = new ComponentBuilder(Prefix.DEFAULT.toString() + "§eJoueurs bannis de la parcelle " + plot + " : ");

		for (int i = 0; i < list.size(); i++) {
			String target = AccountProviderAPI.getter().getPlayerInformations(list.get(i)).getName();
			component.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/oc unban " + target));
			component.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7Débannir " + target)));
			component.append(target);

			if (i < list.size() - 1)
				component.append(", ");
		}

		if (list.size() == 0)
			component.append("§7aucun");

		((CommandSender) pc.getPlayer()).sendMessage(component.create());
	}

	public void visitPlot(OlympaPlayerCreatif pc, PlotId id) {
		Player player = (Player) pc.getPlayer();
		if (id == null)
			OCmsg.INVALID_PLOT_ID.send(player);
		else {
			id.teleport(player);
			OCmsg.TELEPORT_IN_PROGRESS.send(pc, id);
		}
	}

	public void visitPlotRandom(final OlympaPlayerCreatif pc) {
		if (delayRandomPlotVisit.contains(pc.getPlayer())) {
			OCmsg.WAIT_BEFORE_REEXECUTE_COMMAND.send(pc, "/oc visitrandom");
			return;
		}
		Player player = (Player) pc.getPlayer();
		delayRandomPlotVisit.add(player);
		plugin.getTask().runTaskLater(() -> delayRandomPlotVisit.remove(pc.getPlayer()), 30);

		List<Integer> set = new ArrayList<>();

		//on retire les plots du spawn (et le 2 mais flemme de le rajouter)
		for (int i = 6; i <= plugin.getDataManager().getPlotsCount(); i++)
			set.add(i);

		set.removeAll(pc.getPlots(false).stream().map(pl -> pl.getId().getId()).collect(Collectors.toList()));

		set.removeAll(plugin.getPlotsManager().getPlots().stream()
				.filter(pl -> pl.getMembers().getOwner().getName().equals("Spawn") || !pl.canEnter(pc))
				.map(pl -> pl.getId().getId()).collect(Collectors.toList()));

		if (set.size() == 0)
			return;

		PlotId id = PlotId.fromId(plugin, set.get(ThreadLocalRandom.current().nextInt(set.size())));
		id.teleport(player);
		OCmsg.TELEPORTED_TO_PLOT_SPAWN.send(pc, id);
	}

	public void visitPlotOf(OlympaPlayerCreatif pc, String p, int id) {
		Player target = Bukkit.getPlayerExact(p);
		Player player = (Player) pc.getPlayer();
		if (target != null) {
			List<Plot> plots = ((OlympaPlayerCreatif) AccountProviderAPI.getter().get(target.getUniqueId())).getPlots(true);

			id -= 1;

			if (id >= 0 && id < plots.size()) {
				plots.get(id).getParameters().getParameter(PlotParamType.SPAWN_LOC).teleport(player);
				OCmsg.TELEPORT_IN_PROGRESS.send(pc, plots.get(id).getId() + "");
			} else
				OCmsg.INVALID_PLOT_ID.send(pc);
		} else {
			OCmsg.PLOT_LOADING_IN_PROGRESS.send(pc);
			plugin.getDataManager().loadPlot(pc, p, id, plot -> {
				if (plot == null)
					OCmsg.INVALID_PLOT_ID.send(pc);
				else {
					plot.getParameters().getParameter(PlotParamType.SPAWN_LOC).teleport(player);
					OCmsg.TELEPORTED_TO_PLOT_SPAWN.send(pc, plot);
				}
			});
		}
	}

	public void sendPlotsList(CommandSender sender, Player target) {
		String lp = "";
		List<Plot> plots = new ArrayList<>();

		if (target == null)
			plots.addAll(plugin.getPlotsManager().getPlots());
		else
			plots.addAll(((OlympaPlayerCreatif) AccountProviderAPI.getter().get(target.getUniqueId())).getPlots(true));

		Collections.sort(plots, (o1, o2) -> o1.getId().getId() - o2.getId().getId());

		for (Plot plot : plots)
			lp += plot + " ";

		if (target == null)
			sender.sendMessage("§aListe des " + plots.size() + " parcelles actuellement chargées : " + lp);
		else if (plots.size() > 0)
			sender.sendMessage("§aListe des parcelles possédées par " + target.getName() + " : " + lp);
		else
			sender.sendMessage("§aListe des parcelles possédées par " + target.getName() + " : §cjoueur hors ligne ou ne possédant aucune parcelle");
	}

	private Map<PlotId, String> plotsResetVerifCode = new HashMap<>();

	public void resetPlot(OlympaPlayerCreatif pc, Integer plotId, String code) {
		Plot plot = plotId == null ? pc.getCurrentPlot() : plugin.getPlotsManager().getPlot(PlotId.fromId(plugin, plotId));

		if (plot == null) {
			OCmsg.NULL_CURRENT_PLOT.send(pc);
			return;
		}

		if (!PlotPerm.RESET_PLOT.has(plot, pc) && !OcPermissions.STAFF_RESET_PLOT.hasPermission(pc)) {
			OCmsg.INSUFFICIENT_PLOT_PERMISSION.send(pc, PlotPerm.RESET_PLOT);
			return;
		}

		if (!plotsResetVerifCode.containsKey(plot.getId())) {
			String check = "";
			for (int i = 0; i < 6; i++)
				check += (char) (plugin.random.nextInt(26) + 'a');

			plotsResetVerifCode.put(plot.getId(), check);
			OCmsg.PLOT_PRE_RESET.send(pc, plot, "/oco reset " + plot + " " + check);
			//Prefix.DEFAULT.sendMessage(pc.getPlayer(), "§dVeuillez saisir la commande /oca resetplot %s %s pour réinitialiser la parcelle %s (%s). \n§cAttention cette action est irréversible !!", plot.getPlotId(), check, plot.getPlotId(), plot.getMembers().getOwner().getName());

			plugin.getTask().runTaskLater(() -> plotsResetVerifCode.remove(plot.getId()), 600);

		} else if (code == null)
			OCmsg.PLOT_PRE_RESET.send(pc, plot, "/oco reset " + plot + " " + plotsResetVerifCode.get(plot.getId()));
		else {
			if (!plotsResetVerifCode.get(plot.getId()).equals(code)) {
				OCmsg.PLOT_PRE_RESET.send(pc, plot, "/oco reset " + plot + " " + plotsResetVerifCode.get(plot.getId()));
				return;
			}

			plotsResetVerifCode.remove(plot.getId());
			//OCmsg.PLOT_RESET_START.send(pc, plot);
			plugin.getWEManager().resetPlot(pc, plot);
			//Prefix.DEFAULT.sendMessage(pc.getPlayer(), "§dLa parcelle %s (%s) va se réinitialiser.", plot.getPlotId(), plot.getMembers().getOwner().getName());
		}
	}
}
