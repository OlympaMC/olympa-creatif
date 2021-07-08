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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OcweCmd extends AbstractCmd {
    public OcweCmd(OlympaCreatifMain plugin) {
        super(plugin, "ocwe", null, "Commandes de manipulation de la parcelle");
        addArgumentParser("FLOOR_LEVEL",
                (sender, str) -> List.of("1 à 250"),str -> {
                    try{
                        int val = Integer.parseInt(str);
                        return val < 1 || val > 250 ? null : val;
                    }catch(Exception ex) {
                        return null;
                    }
                },
                p -> "Le niveau renseigné doit être compris entre 1 et 250 (par défaut " + WorldManager.worldLevel + ")");
        addArgumentParser("MATERIAL", Material.class);
    }

    @Cmd(player = true, syntax = "Modifier la hauteur et le matériau du sol de la parcelle", min = 2, args = {"FLOOR_LEVEL",
            "MATERIAL", "MATERIAL", "MATERIAL", "MATERIAL", "MATERIAL",
            "MATERIAL", "MATERIAL", "MATERIAL", "MATERIAL", "MATERIAL"})
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

        //int y = cmd.getArgumentsLength() >= 2 ? cmd.getArgument(1) : WorldManager.worldLevel;
        Set<Material> mats = new HashSet<>();
        for (int i = 1 ; i < cmd.getArgumentsLength() ; i++)
            mats.add(cmd.getArgument(i));

        if (plugin.getWEManager().setPlotFloor(getOlympaPlayer(), plot, mats, cmd.getArgument(0)))
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
        }else if (!PlotPerm.EXPORT_PLOT.has(plot, getOlympaPlayer())) {
            OCmsg.INSUFFICIENT_PLOT_PERMISSION.send(getPlayer(), PlotPerm.EXPORT_PLOT);
            return;
        }

        if (plugin.getWEManager().exportPlot(plot, getOlympaPlayer()))
            CmdsLogic.OCtimerCommand.EXPORT.delay(getOlympaPlayer());
    }


    @Cmd(player = true, syntax = "Réinitialiser la parcelle"/*, description = "/oca resetplot [plot] [confirmationCode]"*/)
    public void reset(CommandContext cmd) {
        if (!CmdsLogic.OCtimerCommand.RESET.canExecute2(getOlympaPlayer()))
            return;

        Plot plot = ((OlympaPlayerCreatif)getOlympaPlayer()).getCurrentPlot();

        if (plugin.getCmdLogic().resetPlot(getOlympaPlayer(), plot == null ? null : plot.getId().getId()))
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
