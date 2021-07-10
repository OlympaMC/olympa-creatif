package fr.olympa.olympacreatif.commands;

import java.util.function.Function;

import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.spigot.command.ComplexCommand;
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
import fr.olympa.olympacreatif.plot.PlotStoplagChecker.StopLagDetect;

public class StoplagCommand extends ComplexCommand {

	private Function<Integer, String> stoplagName = i -> i == 0 ? "§ainactif" : i == 1 ? "§cactif" : i == 2 ? "§4forcé §7(contactez un staff)" : "§9inconnu";
	
	private Function<Double, String> stoplagFormatter = d -> ((int) (d * 100)) + "%%";
	private OlympaCreatifMain plugin;

	public StoplagCommand(OlympaCreatifMain plugin) {
		super(plugin, "stoplag", "Définir le statut de stoplag de la parcelle", null, new String[0]);
		this.plugin = plugin;
		allowConsole = false;
	}

	@Cmd(description = "Afficher l'état de stoplag de la parcelle", args = "INTEGER")
	public void info(CommandContext cmd) {
		OlympaPlayerCreatif pc = getOlympaPlayer();
		Plot plot = cmd.getArgumentsLength() == 0 ? 
				pc != null ? pc.getCurrentPlot() : 
					null : 
				plugin.getPlotsManager().getPlot(PlotId.fromId(plugin, cmd.getArgument(0)));
		
		if (plot == null) {
			OCmsg.NULL_CURRENT_PLOT.send(pc);
			return;
		}

		sendMessage(Prefix.DEFAULT_GOOD, "§7Etat du stoplag de la parcelle " + plot + " : " + 
				(plot.hasStoplag() ? plot.getParameters().getParameter(PlotParamType.STOPLAG_STATUS) == 1 ? "§cactif" : "§4forcé §7(contactez un staff)" : "§ainactif") + 
				" §7(entités : " + stoplagFormatter.apply(plot.getStoplagChecker().getScore(StopLagDetect.ENTITY)) + ", " + 
				"redstone : " + stoplagFormatter.apply(plot.getStoplagChecker().getScore(StopLagDetect.WIRE)) + ", " + 
				"lampes : " + stoplagFormatter.apply(plot.getStoplagChecker().getScore(StopLagDetect.LAMP)) + ", " + 
				"pistons : " + stoplagFormatter.apply(plot.getStoplagChecker().getScore(StopLagDetect.PISTON)) + ", " + 
				"liquides : " + stoplagFormatter.apply(plot.getStoplagChecker().getScore(StopLagDetect.LIQUID)) + ")");
	}

	@Cmd(player = true, args = "INTEGER", description = "Activer le stoplag sur parcelle")
	public void force(CommandContext cmd) {
		OlympaPlayerCreatif pc = getOlympaPlayer();

		Plot plot = cmd.getArgumentsLength() == 0 ? pc.getCurrentPlot() : plugin.getPlotsManager().getPlot(PlotId.fromId(plugin, cmd.getArgument(0)));

		manageStoplag(plot, pc, 2);
	}

	@Cmd(player = true, args = "INTEGER", description = "Activer le stoplag sur parcelle")
	public void activate(CommandContext cmd) {
		OlympaPlayerCreatif pc = getOlympaPlayer();

		Plot plot = cmd.getArgumentsLength() == 0 ? pc.getCurrentPlot() : plugin.getPlotsManager().getPlot(PlotId.fromId(plugin, cmd.getArgument(0)));

		manageStoplag(plot, pc, 1);
	}

	@Cmd(player = true, args = {"INTEGER", "INTEGER"}, description = "Désactiver le stoplag sur parcelle")
	public void deactivate(CommandContext cmd) {
		OlympaPlayerCreatif pc = getOlympaPlayer();

		Plot plot = cmd.getArgumentsLength() == 0 ? pc.getCurrentPlot() : plugin.getPlotsManager().getPlot(PlotId.fromId(plugin, cmd.getArgument(0)));

		manageStoplag(plot, pc, 0);
	}

	private void manageStoplag(Plot plot, OlympaPlayerCreatif pc, int level) {
		if (level < 0 || level > 2)
			throw new UnsupportedOperationException("Stoplag level mus be between 0 and 2.");
		
		if (plot == null) {
			OCmsg.INVALID_PLOT_ID.send(pc);
			return;
		}
		
		if (level == 2 && !OcPermissions.STAFF_STOPLAG_MANAGEMENT.hasPermissionWithMsg(pc))
			return;
		
		if (!PlotPerm.USE_STOPLAG.has(plot, (OlympaPlayerCreatif) getOlympaPlayer())) {
			OCmsg.INSUFFICIENT_PLOT_PERMISSION.send(pc, PlotPerm.USE_STOPLAG);
			return;
		}

		if (plot.getParameters().getParameter(PlotParamType.STOPLAG_STATUS) > 1 && !OcPermissions.STAFF_STOPLAG_MANAGEMENT.hasPermission(pc)) {
			OCmsg.PLOT_FORCED_STOPLAG_FIRED.send(pc, StopLagDetect.UNKNOWN, plot);
			return;
		}
		
		OCmsg.PLOT_STOPLAG_FIRED_CMD.send(pc, stoplagName.apply(level) + 
				" §7(anciennement : " + stoplagName.apply(plot.getParameters().getParameter(PlotParamType.STOPLAG_STATUS)) + "§7)§a");

		PlotParamType.STOPLAG_STATUS.setValue(plot, level);
	}
}
