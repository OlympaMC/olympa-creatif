package fr.olympa.olympacreatif.commands;

import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotPerm;
import fr.olympa.olympacreatif.world.WorldManager;
import org.bukkit.Material;

public class OcweCmd extends AbstractCmd {
    public OcweCmd(OlympaCreatifMain plugin) {
        super(plugin, "ocwe", null, "Commandes de manipulation de la parcelle");
        addArgumentParser("MATERIAL", Material.class);
    }

    @Cmd(player = true, syntax = "Modifier le sol et la hauteur du sol de la parcelle", min = 1, args = {"MATERIAL", "INTEGER"})
    public void setfloor(CommandContext cmd) {
        if (!CmdsLogic.OCtimerCommand.SETFLOOR.canExecute2(getOlympaPlayer()))
            return;

        Plot plot = ((OlympaPlayerCreatif) getOlympaPlayer()).getCurrentPlot();

        if (plot == null) {
            //OCmsg.WE_PLOT_RESTAURATION_FAILED.send(getPlayer(), plot);
            OCmsg.NULL_CURRENT_PLOT.send(getPlayer());
            return;

        }else if (!PlotPerm.RESET_PLOT.has((OlympaPlayerCreatif) getOlympaPlayer())) {
            OCmsg.INSUFFICIENT_PLOT_PERMISSION.send(getPlayer(), PlotPerm.RESET_PLOT);
            return;
        }

        if (plot == null) {
            OCmsg.NULL_CURRENT_PLOT.send(getPlayer());
            return;
        }

        int y = cmd.getArgumentsLength() >= 2 ? cmd.getArgument(1) : WorldManager.worldLevel;

        if (plugin.getWEManager().setPlotFloor(getOlympaPlayer(), plot, cmd.getArgument(0), y))
            CmdsLogic.OCtimerCommand.SETFLOOR.delay(getOlympaPlayer());
    }



    @Cmd(player = true, syntax = "Exporter sla parcelle en .schematic")
    public void export(CommandContext cmd) {
        if (!CmdsLogic.OCtimerCommand.EXPORT.canExecute2(getOlympaPlayer()))
            return;

        Plot plot = ((OlympaPlayerCreatif) getOlympaPlayer()).getCurrentPlot();

        if (plot == null) {
            OCmsg.NULL_CURRENT_PLOT.send(getPlayer());
            return;
        }else if (!PlotPerm.EXPORT_PLOT.has(plot, (OlympaPlayerCreatif) getOlympaPlayer())) {
            OCmsg.INSUFFICIENT_PLOT_PERMISSION.send(getPlayer(), PlotPerm.EXPORT_PLOT);
            return;
        }

        if (plugin.getWEManager().exportPlot(plot, getOlympaPlayer()))
            CmdsLogic.OCtimerCommand.EXPORT.delay(getOlympaPlayer());
    }


    @Cmd(player = true, syntax = "Réinitialiser une parcelle", args = {"INTEGER"}/*, description = "/oca resetplot [plot] [confirmationCode]"*/)
    public void reset(CommandContext cmd) {
        if (!CmdsLogic.OCtimerCommand.RESET.canExecute2(getOlympaPlayer()))
            return;

        if (plugin.getCmdLogic().resetPlot(getOlympaPlayer(),
                (cmd.getArgumentsLength() > 0 ? (Integer) cmd.getArgument(0) : null)))
            CmdsLogic.OCtimerCommand.RESET.delay(getOlympaPlayer());
    }



    @Cmd(player = true, syntax = "Restaurer sa parcelle vers la dernière version sauvegardée")
    public void restore(CommandContext cmd) {
        if (!CmdsLogic.OCtimerCommand.RESTORE.canExecute2(getOlympaPlayer()))
            return;

        Plot plot = ((OlympaPlayerCreatif) getOlympaPlayer()).getCurrentPlot();

        if (plot == null) {
            //OCmsg.WE_PLOT_RESTAURATION_FAILED.send(getPlayer(), plot);
            OCmsg.NULL_CURRENT_PLOT.send(getPlayer());
            return;
        }else if (!PlotPerm.RESET_PLOT.has((OlympaPlayerCreatif) getOlympaPlayer())) {
            OCmsg.INSUFFICIENT_PLOT_PERMISSION.send(getPlayer(), PlotPerm.RESET_PLOT);
            return;
        }

        if (plugin.getWEManager().restorePlot(plot, getOlympaPlayer()))
            CmdsLogic.OCtimerCommand.RESTORE.delay(getOlympaPlayer());
    }

}
