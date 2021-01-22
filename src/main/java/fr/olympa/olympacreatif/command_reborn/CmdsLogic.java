package fr.olympa.olympacreatif.command_reborn;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import fr.olympa.api.chat.TxtComponentBuilder;
import fr.olympa.api.command.IOlympaCommand;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
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
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class CmdsLogic {

	private OlympaCreatifMain plugin;
	private Map<PlotId, Entry<OlympaPlayerCreatif, Player>> invitations = new HashMap<PlotId, Entry<OlympaPlayerCreatif, Player>>();
	
	public CmdsLogic (OlympaCreatifMain plugin) {
		this.plugin = plugin;
	}
	
	public void claimNewPlot(OlympaPlayerCreatif pc) {//teste si le joueur a encore des plots dispo
		if (pc.getPlotsSlots(true) - pc.getPlots(true).size() > 0) {
			if (pc.getPlotsSlots(false) - pc.getPlots(false).size() > 0) {
				
				Plot plot = plugin.getPlotsManager().createNewPlot(pc);
				pc.getPlayer().teleport(plot.getPlotId().getLocation());

				//TODO vérifier si le getCurrentPlot est bien MAJ
				OCmsg.PLOT_NEW_CLAIM.send(pc);
			}else
				OCmsg.MAX_PLOT_COUNT_REACHED.send(pc);
		}else
			OCmsg.MAX_PLOT_COUNT_OWNER_REACHED.send(pc);
	}
	
	public void invitePlayer(OlympaPlayerCreatif pc, Player target) {

		Plot plot = pc.getCurrentPlot();
		
		if (plot == null) {
			OCmsg.INVALID_PLOT_ID.send(pc);
			return;
		}

		if (!PlotPerm.INVITE_MEMBER.has(plot, pc)) {
			OCmsg.INSUFFICIENT_PLOT_PERMISSION.send(pc, PlotPerm.INVITE_MEMBER.getRank().getRankName());
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
		
		invitations.put(plot.getPlotId(), new AbstractMap.SimpleEntry<OlympaPlayerCreatif, Player>(pc, target));
		OCmsg.PLOT_RECIEVE_INVITATION.send(target, plot);
		OCmsg.PLOT_SEND_INVITATION.send(pc, target.getName());
	}

	public void acceptInvitation(OlympaPlayerCreatif pc, int plotId) {
		
		Plot plot = plugin.getPlotsManager().getPlot(PlotId.fromId(plugin, plotId));
		
		String plotIdStr = plotId + "";
		
		if (plot == null)
			OCmsg.PLOT_UNLOADED.send(pc, plotIdStr);
		
		else if (pc.getPlotsSlots(false) - pc.getPlots(false).size() <= 0)
			OCmsg.MAX_PLOT_COUNT_REACHED.send(pc, plot);
		
		else if (!invitations.containsKey(plot.getPlotId()) || !invitations.get(plot.getPlotId()).getValue().equals(pc.getPlayer()))
			OCmsg.PLOT_NO_PENDING_INVITATION.send(pc, plot);
		
		else if (!invitations.get(plot.getPlotId()).getKey().getPlayer().isOnline())
			OCmsg.PLOT_JOIN_ERR_SENDER_OFFLINE.send(pc, plot, invitations.get(plot.getPlotId()).getKey().getName());
		
		else if (plot.getMembers().getMembers().size() >= plot.getMembers().getMaxMembers())
			OCmsg.PLOT_JOIN_ERR_NOT_ENOUGH_SLOTS.send(pc, plot);
		
		else {
			OCmsg.PLOT_PLAYER_JOIN.send(invitations.get(plot.getPlotId()).getKey(), pc.getName());
			OCmsg.PLOT_ACCEPTED_INVITATION.send(pc, plotId);
			plot.getMembers().set(invitations.remove(plot.getPlotId()).getValue(), PlotRank.MEMBER);	
		}
	}

	
	
	public void kickPlayerFromPlot(OlympaPlayerCreatif pc, Player target) {
		Plot plot = pc.getCurrentPlot();
		
		if (plot == null) 
			OCmsg.INVALID_PLOT_ID.send(pc);
		
		else if (!PlotPerm.KICK_VISITOR.has(plot, pc))
			OCmsg.INSUFFICIENT_PLOT_PERMISSION.send(pc);
		
		else if (plot.getMembers().getPlayerRank(target) != PlotRank.VISITOR || !plot.getPlayers().contains(target))
			OCmsg.PLOT_IMPOSSIBLE_TO_KICK_PLAYER.send(pc, target.getName());
		
		else if (((OlympaPlayerCreatif) AccountProvider.get(target.getUniqueId())).hasStaffPerm(StaffPerm.BYPASS_KICK_AND_BAN))
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
			OCmsg.INSUFFICIENT_PLOT_PERMISSION.send(pc);
		
		else if (!plot.getPlayers().contains(target) || plot.getMembers().getPlayerRank(target) != PlotRank.VISITOR)
			OCmsg.PLOT_IMPOSSIBLE_TO_BAN_PLAYER.send(pc, target.getName());
		
		else if (((OlympaPlayerCreatif) AccountProvider.get(target.getUniqueId())).hasStaffPerm(StaffPerm.BYPASS_KICK_AND_BAN))
			OCmsg.PLOT_IMPOSSIBLE_TO_BAN_PLAYER.send(pc, target.getName());
		
		else {
			//exécution du ban
			plot.getParameters().getParameter(PlotParamType.BANNED_PLAYERS).add(AccountProvider.get(target.getUniqueId()).getId());
			
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
			OCmsg.INSUFFICIENT_PLOT_PERMISSION.send(pc);
		
		else if (plot.getParameters().getParameter(PlotParamType.BANNED_PLAYERS).remove(AccountProvider.get(target.getUniqueId()).getId()))
			OCmsg.PLOT_UNBAN_PLAYER.send(pc, target.getName());
		else
			OCmsg.PLOT_CANT_UNBAN_PLAYER.send(pc, target.getName());
	}
	
	
	public void sendBanList(OlympaPlayerCreatif pc) {
		Plot plot = pc.getCurrentPlot();
		
		List<Long> list = plot.getParameters().getParameter(PlotParamType.BANNED_PLAYERS);
		ComponentBuilder component = new ComponentBuilder(Prefix.DEFAULT.toString() + "§e>> Joueurs bannis de la parcelle " + plot + " : ");
		
		for (int i = 0 ; i < list.size() ; i++) {
			String target = AccountProvider.getPlayerInformations(list.get(i)).getName();
			component.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/oc unban " + target));
			component.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7Débannir " + target)));
			component.append(target);
			
			if (i < list.size() - 1)
				component.append(", ");
		}
		
		if (list.size() == 0)
			component.append("§7aucun");
		
		pc.getPlayer().sendMessage(component.create());
	}
	

	public void visitPlot(OlympaPlayerCreatif pc, PlotId id) {
		Plot plot = plugin.getPlotsManager().getPlot(id);
		
		if (id == null)
			OCmsg.INVALID_PLOT_ID.send(pc.getPlayer());
		
		else {
			if (plot == null)
				pc.getPlayer().teleport(id.getLocation());
			else
				pc.getPlayer().teleport(plot.getParameters().getSpawnLoc());
			OCmsg.TELEPORT_IN_PROGRESS.send(pc);
		}
	}

	public void visitPlotFrom(OlympaPlayerCreatif pc, Player target, int id) {
		List<Plot> plots = ((OlympaPlayerCreatif)AccountProvider.get(target.getUniqueId())).getPlots(true);
		
		id -= 1;
		
		if (id >= 0 && id < plots.size()) {
			pc.getPlayer().teleport(plots.get(id).getParameters().getSpawnLoc());
			OCmsg.TELEPORT_IN_PROGRESS.send(pc, plots.get(id));
		}else
			OCmsg.INVALID_PLOT_ID.send(pc);
	}
	
	
	public void sendPlotsList(OlympaPlayerCreatif pc, Player target) {
		String lp = "";
		List<Plot> plots = new ArrayList<Plot>();
		
		if (target == null)
			plots.addAll(plugin.getPlotsManager().getPlots());
		else
			plots.addAll(((OlympaPlayerCreatif) AccountProvider.get(target.getUniqueId())).getPlots(true));
	
		
		Collections.sort(plots, new Comparator<Plot>() {
			@Override
			public int compare(Plot o1, Plot o2) {
				return o1.getPlotId().getId() - o2.getPlotId().getId(); 
			}
		});
		
		for (Plot plot : plots)
			lp += plot + " ";
		
		if (target == null)
			pc.getPlayer().sendMessage("§aListe des " + plots.size() + " parcelles actuellement chargées : " + lp);
		else if (plots.size() > 0)
			pc.getPlayer().sendMessage("§aListe des parcelles possédées par " + target.getName() + " : " + lp);
		else
			pc.getPlayer().sendMessage("§aListe des parcelles possédées par " + target.getName() + " : §cjoueur hors ligne ou ne possédant aucune parcelle");
	}
}










