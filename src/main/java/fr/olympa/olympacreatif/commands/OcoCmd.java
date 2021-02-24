package fr.olympa.olympacreatif.commands;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Entity;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.utils.Prefix;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OCparam;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotId;
import fr.olympa.olympacreatif.plot.PlotPerm;

public class OcoCmd extends AbstractCmd {

	private Map<PlotId, String> plotsResetVerifCode = new HashMap<PlotId, String>();
	
	public OcoCmd(OlympaCreatifMain plugin) {
		super(plugin, "oco", null, "Commandes spécialisées du Créatif");
	}

	@Cmd(player = true, syntax = "Afficher les informations de débug de la parcelle")
	public void debug(CommandContext cmd) {
		Plot plot = ((OlympaPlayerCreatif) getOlympaPlayer()).getCurrentPlot();
		
		if (plot == null) {
			OCmsg.INVALID_PLOT_ID.send(getPlayer());
			return;
		}
		
		String debug = "\n   §6>>> Débug parcelle " + plot.getPlotId() + " :";
		debug += "\n   §e> Joueurs : §a" + plot.getPlayers().size();
		debug += "\n   §e> Entités : §a" + plot.getEntities().size() + "/" + OCparam.MAX_TOTAL_ENTITIES_PER_PLOT.get() + " (max " + OCparam.MAX_ENTITIES_PER_TYPE_PER_PLOT.get() + " de chaque type) §7(détails avec /debugentities)";
		debug += "\n   §e> Equipes : §a" + plot.getCbData().getTeams().size() + "/" + OCparam.CB_MAX_TEAMS_PER_PLOT.get();
		debug += "\n   §e> Objectifs : §a" + plot.getCbData().getObjectives().size() + "/" + OCparam.CB_MAX_OBJECTIVES_PER_PLOT.get();
		debug += "\n   §e> Tickets commandblocks : §a" + plot.getCbData().getCommandsTicketsLeft() + "/" +
				OCparam.CB_MAX_CMDS_LEFT.get() + " (+" + plot.getCbData().getCpt() * 20 + "/s)";
		
		sender.sendMessage(debug);
	}

	@Cmd(player = true, syntax = "Afficher les informations détaillées des entités de la parcelle")
	public void debugentities(CommandContext cmd) {
		Plot plot = ((OlympaPlayerCreatif) getOlympaPlayer()).getCurrentPlot();
		
		if (plot == null) {
			OCmsg.INVALID_PLOT_ID.send(getPlayer());
			return;
		}
		
		List<Entity> entList = plot.getEntities();
		Collections.sort(entList, new Comparator<Entity>() {
			@Override
			public int compare(Entity o1, Entity o2) {
				return o1.getType().toString().compareTo(o2.getType().toString());
			}
		});
		
		String debug = "\n   §6>>> Débug entités parcelle " + plot.getPlotId() + " :";
		for (Entity e : entList)
			debug += "\n§7" + (entList.indexOf(e) + 1) + ".   §e> " + e.getType().toString().toLowerCase() + (e.getCustomName() == null ? "" : " §7(" + e.getCustomName() + "§7)") + ", " + 
					e.getLocation().getBlockX() + " " + e.getLocation().getBlockY() + " " + e.getLocation().getBlockZ() +  
					(!e.isDead() ? "" : " §c!! §4CONTACTEZ UN STAFF §c!!");
		
		getPlayer().sendMessage(debug);
	}
	
	@Cmd(player = true, syntax = "Exporter sa parcelle en .schematic")
	public void export(CommandContext cmd) {
		if (!PermissionsList.USE_PLOT_EXPORTATION.hasPermissionWithMsg(getOlympaPlayer()))
			return;
		
		Plot plot = ((OlympaPlayerCreatif) getOlympaPlayer()).getCurrentPlot();
		
		if (plot == null || !PlotPerm.EXPORT_PLOT.has((OlympaPlayerCreatif) getOlympaPlayer())) {
			OCmsg.WE_PLOT_EXPORT_FAILED.send(getPlayer(), plot);
			return;
		}

		plugin.getPerksManager().getSchematicCreator().export(plot, getOlympaPlayer());
	}
	
	@Cmd(player = true, syntax = "Réinitialiser une parcelle", description = "/oca resetplot <plot> [confirmationCode]")
	public void resetplot(CommandContext cmd) {
		if (!PermissionsList.STAFF_RESET_PLOT.hasPermissionWithMsg(getOlympaPlayer()))
			return;
		
		Plot plot = cmd.getArgumentsLength() == 0 ? ((OlympaPlayerCreatif)getOlympaPlayer()).getCurrentPlot() : plugin.getPlotsManager().getPlot(PlotId.fromString(plugin, cmd.getArgument(0)));
		
		if (plot == null) {
			OCmsg.NULL_CURRENT_PLOT.send(getPlayer());
			return;
		}
		
		if (!plotsResetVerifCode.containsKey(plot.getPlotId())) {
			String check = "";
			for (int i = 0 ; i < 5 ; i++) check += (char) plugin.random.nextInt(26) + 'a';
			
			plotsResetVerifCode.put(plot.getPlotId(), check);
			
			Prefix.DEFAULT.sendMessage(getPlayer(), "§dVeuillez saisir la commande /oca resetplot %s %s pour réinitialiser la parcelle %s (%s). \n§cAttention cette action est irréversible !!", plot.getPlotId(), check, plot.getPlotId(), plot.getMembers().getOwner().getName());
			
			plugin.getTask().runTaskLater(() -> plotsResetVerifCode.remove(plot.getPlotId()), 400);
			
		} else if (cmd.getArgumentsLength() != 2) {
			Prefix.DEFAULT.sendMessage(getPlayer(), "§dVeuillez saisir la commande /oca resetplot %s %s pour réinitialiser la parcelle %s (%s). \n§cAttention cette action est irréversible !!", plot.getPlotId(), plotsResetVerifCode.get(plot.getPlotId()), plot.getPlotId(), plot.getMembers().getOwner().getName());
			
		}else {			
			if (!plotsResetVerifCode.containsKey(plot.getPlotId()) || !plotsResetVerifCode.get(plot.getPlotId()).equals(cmd.getArgument(1))) {
				Prefix.DEFAULT.sendMessage(getPlayer(), "§dLe code renseigné n'est pas valide.");
				return;
			}
			
			plugin.getWEManager().resetPlot(getPlayer(), plot);
			plotsResetVerifCode.remove(plot.getPlotId());
			//Prefix.DEFAULT.sendMessage(getPlayer(), "§dLa parcelle %s (%s) va se réinitialiser.", plot.getPlotId(), plot.getMembers().getOwner().getName());
		}
	}

	/*
	@Cmd(player = true, syntax = "Définir votre vitesse de vol", args = "INTEGER", min = 1)
	public void speed(CommandContext cmd) {
		Plot plot = ((OlympaPlayerCreatif) getOlympaPlayer()).getCurrentPlot();
		
		if (plot != null && !PlotPerm.DEFINE_OWN_FLY_SPEED.has(plot, getOlympaPlayer())) {
			OCmsg.INSUFFICIENT_PLOT_PERMISSION.send(getPlayer());
			return;
		}
		
		float level = 0.1f;

		level = Math.min(Math.max(Float.valueOf(cmd.getArgument(0).toString())/18f, 0.1f), 1f);
		
		getPlayer().setFlySpeed(level);
		OCmsg.OCO_SET_FLY_SPEED.send(getPlayer(), cmd.getArgument(0).toString());
	}*/
}
