package fr.olympa.olympacreatif.commands;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commands.CmdsLogic.OCtimerCommand;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OCparam;
import fr.olympa.olympacreatif.data.OcPermissions;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotParamType;
import fr.olympa.olympacreatif.plot.PlotPerm;

public class OcoCmd extends AbstractCmd {
	
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
		
		String debug = "\n   §6>>> Débug parcelle " + plot.getId() + " :";
		debug += "\n   §e> Joueurs : §a" + plot.getPlayers().size();
		debug += "\n   §e> Entités : §a" + plot.getEntities().size() + "/" + OCparam.MAX_TOTAL_ENTITIES_PER_PLOT.get() + " (max " + OCparam.MAX_ENTITIES_PER_TYPE_PER_PLOT.get() + "/type) §7(/oco debugentities)";
		debug += "\n   §e> Tile entities : §a" + plot.getTilesCount();
		debug += "\n   §e> Equipes : §a" + plot.getCbData().getTeams().size() + "/" + OCparam.CB_MAX_TEAMS_PER_PLOT.get();
		debug += "\n   §e> Objectifs : §a" + plot.getCbData().getObjectives().size() + "/" + OCparam.CB_MAX_OBJECTIVES_PER_PLOT.get();
		debug += "\n   §e> Stoplag : §a" + (!plot.hasStoplag() ? "§ainactif" : plot.getParameters().getParameter(PlotParamType.STOPLAG_STATUS) == 1 ? "§cactif" : "§4forcé §8contactez un membre du staff") + " §7(/stoplag info)";
		debug += "\n   §e> Tickets commandblocks : §a" + plot.getCbData().getCommandTicketsLeft() + "/" +
				OCparam.CB_MAX_CMDS_LEFT.get() + " (+" + plot.getCbData().getCommandsPerSecond() + "/s)";
		
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
		
		String debug = "\n   §6>>> Débug entités parcelle " + plot.getId() + " :";
		for (Entity e : entList)
			debug += "\n§7" + (entList.indexOf(e) + 1) + ".   §e> " + e.getType().toString().toLowerCase() + (e.getCustomName() == null ? "" : " §7(" + e.getCustomName() + "§7)") + ", " + 
					e.getLocation().getBlockX() + " " + e.getLocation().getBlockY() + " " + e.getLocation().getBlockZ() +  
					(!e.isDead() ? "" : " §c!! §4CONTACTEZ UN STAFF §c!!");
		
		getPlayer().sendMessage(debug);
	}
	
	@Cmd(player = true, syntax = "Exporter sa parcelle en .schematic")
	public void export(CommandContext cmd) {
		if (!OcPermissions.USE_PLOT_EXPORTATION.hasPermissionWithMsg(getOlympaPlayer()))
			return;
		
		Plot plot = ((OlympaPlayerCreatif) getOlympaPlayer()).getCurrentPlot();
		
		if (plot == null) {
			//OCmsg.WE_PLOT_EXPORT_FAILED.send(getPlayer(), plot);
			OCmsg.NULL_CURRENT_PLOT.send(getPlayer());
			return;
		}else if (!PlotPerm.EXPORT_PLOT.has((OlympaPlayerCreatif) getOlympaPlayer())) {
			OCmsg.INSUFFICIENT_PLOT_PERMISSION.send(getPlayer(), PlotPerm.EXPORT_PLOT);
			return;
		}

		if (!OCtimerCommand.OCO_EXPORT.canExecute(getOlympaPlayer()))
			return;

		plugin.getPerksManager().getSchematicCreator().export(plot, getOlympaPlayer());
	}
	
	
	@Cmd(player = true, syntax = "Restaurer sa parcelle vers la dernière version sauvegardée")
	public void restore(CommandContext cmd) {
		if (!OcPermissions.USE_PLOT_EXPORTATION.hasPermissionWithMsg(getOlympaPlayer()))
			return;
		
		Plot plot = ((OlympaPlayerCreatif) getOlympaPlayer()).getCurrentPlot();
		
		if (plot == null) {
			//OCmsg.WE_PLOT_RESTAURATION_FAILED.send(getPlayer(), plot);
			OCmsg.NULL_CURRENT_PLOT.send(getPlayer());
			return;
		}else if (!PlotPerm.EXPORT_PLOT.has((OlympaPlayerCreatif) getOlympaPlayer())) {
			OCmsg.INSUFFICIENT_PLOT_PERMISSION.send(getPlayer(), PlotPerm.EXPORT_PLOT);
			return;
		}

		if (!OCtimerCommand.OCO_RESTORE.canExecute(getOlympaPlayer()))
			return;
		
		plugin.getPerksManager().getSchematicCreator().restore(plot, getOlympaPlayer());
	}
	
	
	@Cmd(player = true, syntax = "Réinitialiser une parcelle. §cATTENTION : §4ACTION IRREVERSIBLE !!", args = {"INTEGER", "code"}/*, description = "/oca resetplot [plot] [confirmationCode]"*/)
	public void reset(CommandContext cmd) {
		if (!OCtimerCommand.OCO_RESET.canExecute(getOlympaPlayer()))
			return;
		
		plugin.getCmdLogic().resetPlot(getOlympaPlayer(), (cmd.getArgumentsLength() > 0 ? (Integer) cmd.getArgument(0) : null), (cmd.getArgumentsLength() > 1 ? (String) cmd.getArgument(1) : null));
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

	@Cmd(player = true, syntax = "Définir la vitesse de tick des commandblocks (entre 1 et 20)", args = {"INTEGER"}, min = 1/*, description = "/oca resetplot [plot] [confirmationCode]"*/)
	public void settickspeed(CommandContext cmd) {
		Plot plot = ((OlympaPlayerCreatif)getOlympaPlayer()).getCurrentPlot();
		
		if (plot == null) {
			OCmsg.NULL_CURRENT_PLOT.send(getPlayer());
			return;
		}else if (!PlotPerm.COMMAND_BLOCK.has(plot, getOlympaPlayer())) {
			OCmsg.INSUFFICIENT_PLOT_PERMISSION.send((OlympaPlayerCreatif) getOlympaPlayer(), PlotPerm.COMMAND_BLOCK);
			return;
		}
		
		int tickSpeed = cmd.getArgument(0); 
		
		if (tickSpeed > 0 && tickSpeed <= 20) {
			plot.getParameters().setParameter(PlotParamType.TICK_SPEED, tickSpeed);
			plot.getCbData().setTickSpeed(tickSpeed);
			OCmsg.CB_SET_TICK_SPEED.send(getPlayer(), tickSpeed + "", plot);
		}else
			sendIncorrectSyntax();
	}

	
	@Cmd(player = true, syntax = "Recharger tous les commandblocks de la parcelle"/*, description = "/oca resetplot [plot] [confirmationCode]"*/)
	public void reloadcommandblocks(CommandContext cmd) {
		if (!OCtimerCommand.OCO_RELOAD_COMMANDBLOCKS.canExecute(getOlympaPlayer()))
			return;
		
		Plot plot = ((OlympaPlayerCreatif)getOlympaPlayer()).getCurrentPlot();
		
		if (plot == null) {
			OCmsg.NULL_CURRENT_PLOT.send(getPlayer());
			return;
		}else if (!PlotPerm.COMMAND_BLOCK.has(plot, getOlympaPlayer())) {
			OCmsg.INSUFFICIENT_PLOT_PERMISSION.send((OlympaPlayerCreatif) getOlympaPlayer(), PlotPerm.COMMAND_BLOCK);
			return;
		}
		
		plot.getCbData().reloadAllCommandBlocks(true);
		OCmsg.PLOT_COMMANDBLOCKS_WILL_RELOAD.send(getPlayer());
	}


	@Cmd(player = true, syntax = "Ouvrir l'inventaire de manipulation des portes-armures"/*, description = "/oca resetplot [plot] [confirmationCode]"*/)
	public void armorstandeditor(CommandContext cmd) {
		if (!OcPermissions.USE_ARMORSTAND_EDITOR.hasPermission((OlympaPlayerCreatif) getOlympaPlayer())) {
			OCmsg.INSUFFICIENT_GROUP_PERMISSION.send((OlympaPlayerCreatif) getOlympaPlayer(), OcPermissions.USE_ARMORSTAND_EDITOR.getMinGroup().getName(getOlympaPlayer().getGender()));
			return;
		}
		
		if (!PlotPerm.USE_ARMORSTAND_EDITOR.has((OlympaPlayerCreatif)getOlympaPlayer())) {
			OCmsg.INSUFFICIENT_PLOT_PERMISSION.send((OlympaPlayerCreatif)getOlympaPlayer(), PlotPerm.USE_ARMORSTAND_EDITOR);
			return;
		}
			
		plugin.getPerksManager().getArmorStandManager().listeningFor(getPlayer());
		OCmsg.ARMORSTAND_EDITOR_SELECT_ARMORSTAND.send((OlympaPlayerCreatif) getOlympaPlayer());
	}
	
	
}






