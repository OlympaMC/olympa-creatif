package fr.olympa.olympacreatif.commands;

import java.text.DecimalFormat;
import java.util.function.Function;

import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
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

	private Function<Double, String> stoplagFormatter = d -> ((int) (d * 100)) + "%";
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

		getSender().sendMessage(Prefix.DEFAULT_GOOD.toString() + "§7Etat du stoplag de la parcelle " + plot + " : " + (plot.hasStoplag() ? "§cactif" : "§ainactif") + 
				" §7(entités : " + stoplagFormatter.apply(plot.getStoplagChecker().getScore(StopLagDetect.ENTITY)) + ", " + 
				"redstone : " + stoplagFormatter.apply(plot.getStoplagChecker().getScore(StopLagDetect.WIRE)) + ", " + 
				"lampes : " + stoplagFormatter.apply(plot.getStoplagChecker().getScore(StopLagDetect.LAMP)) + ", " + 
				"pistons : " + stoplagFormatter.apply(plot.getStoplagChecker().getScore(StopLagDetect.PISTON)) + ", " + 
				"liquides : " + stoplagFormatter.apply(plot.getStoplagChecker().getScore(StopLagDetect.LIQUID)) + ")");
	}

	@Cmd(player = true, args = "INTEGER", description = "Activer le stoplag sur parcelle")
	public void force(CommandContext cmd) {
		OlympaPlayerCreatif pc = getOlympaPlayer();

		if (!pc.hasStaffPerm(StaffPerm.OWNER_EVERYWHERE)) {
			sendIncorrectSyntax("Cette commande est réservée au staff.");
			return;
		}

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

		if (!PlotPerm.USE_STOPLAG.has(plot, (OlympaPlayerCreatif) getOlympaPlayer()) || (plot.getParameters().getParameter(PlotParamType.STOPLAG_STATUS) > 1 && !pc.hasStaffPerm(StaffPerm.OWNER_EVERYWHERE))) {
			OCmsg.PLOT_FORCED_STOPLAG_FIRED.send(pc, StopLagDetect.UNKNOWN);
			return;
		}

		PlotParamType.STOPLAG_STATUS.setValue(plot, level);
		OCmsg.PLOT_STOPLAG_FIRED_CMD.send(pc, level == 0 ? "§adésactivé" : level == 1 ? "§cactivé" : "§4forcé", plot);
	}
}
