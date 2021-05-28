package fr.olympa.olympacreatif.commands;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotId;
import fr.olympa.olympacreatif.plot.PlotParamType;
import fr.olympa.olympacreatif.plot.PlotPerm;

public class StoplagCommand extends ComplexCommand {

	private OlympaCreatifMain plugin;
	
	public StoplagCommand(OlympaCreatifMain plugin) {
		super(plugin, "stoplag", "Définir le statut de stoplag de la parcelle", null, new String[] {});
		this.plugin = plugin;
		allowConsole = false;
	}
	
	@Cmd(player = true, args = "INTEGER", description = "Activer le stoplag sur parcelle")
	public void activate(CommandContext cmd) {
		OlympaPlayerCreatif pc = getOlympaPlayer();
		
		Plot plot = cmd.getArgumentsLength() == 0 ? pc.getCurrentPlot() : plugin.getPlotsManager().getPlot(PlotId.fromId(plugin, cmd.getArgument(0)));
		
		manageStoplag(plot, pc, true);
	}
	
	@Cmd(player = true, args = "INTEGER", description = "Désactiver le stoplag sur parcelle")
	public void deactivate(CommandContext cmd) {
		OlympaPlayerCreatif pc = getOlympaPlayer();
		
		Plot plot = cmd.getArgumentsLength() == 0 ? pc.getCurrentPlot() : plugin.getPlotsManager().getPlot(PlotId.fromId(plugin, cmd.getArgument(0)));
		
		manageStoplag(plot, pc, false);
	}
	
	private void manageStoplag(Plot plot, OlympaPlayerCreatif pc, boolean enable) {
		if (plot == null) {
			OCmsg.INVALID_PLOT_ID.send(pc);
			return;
		}
		
		if (!PlotPerm.USE_STOPLAG.has((OlympaPlayerCreatif) getOlympaPlayer()) || plot.getParameters().getParameter(PlotParamType.STOPLAG_STATUS) > 1) {
			OCmsg.INSUFFICIENT_PLOT_PERMISSION.send((OlympaPlayerCreatif) getOlympaPlayer(), PlotPerm.USE_STOPLAG);
			return;
		}
		
		PlotParamType.STOPLAG_STATUS.setValue(plot, enable ? 1 : 0);
		OCmsg.PLOT_STOPLAG_FIRED_CMD.send(pc, enable ? "§aactivé" : "§sdésactivé", plot);
	}
}











