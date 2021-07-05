package fr.olympa.olympacreatif.commands;

import fr.olympa.api.common.command.complex.ArgumentParser;
import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.common.permission.OlympaSpigotPermission;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.common.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand;
import fr.olympa.olympacreatif.data.*;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotPerm;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StructureCommand extends AbstractCmd {

    private static Function<Integer, File> getPlotSchemsFolder = id -> new File(OlympaCreatifMain.getInstance().getDataFolder() + "/plot_schematics/plot_" + id);
    private static Map<Integer, Set<String>> knownSchems = new HashMap<Integer, Set<String>>();

    public StructureCommand(OlympaCreatifMain plugin) {
        super(plugin, "structure", OcPermissions.STRUCTURE_COMMAND, "Gérer les structure des parcelles");

        addArgumentParser("SCHEMS_NAMES", (sender, arg) -> {
            OlympaPlayerCreatif pc = AccountProvider.getter().get(((Player)sender).getUniqueId());
            if (pc == null || pc.getCurrentPlot() == null)
                return new ArrayList<String>();

            return getSchemsOf(pc.getCurrentPlot().getId().getId());

        }, arg -> {
            try {
                int plotId = Integer.parseInt(arg.substring(arg.lastIndexOf("_") + 1, arg.length()));

                /*System.out.println("arg : " + arg);
                System.out.println("plot id : " + plotId);
                System.out.println("knownSchems : " + knownSchems);
                System.out.println("condition : " + (!knownSchems.containsKey(plotId) || !knownSchems.get(plotId).contains(arg)));*/

                if (!knownSchems.containsKey(plotId) || !knownSchems.get(plotId).contains(arg))
                    return null;

                File file = new File(getPlotSchemsFolder.apply(plotId), arg + ".schem");
                //System.out.println("file : " + file);

                return file.exists() ? arg : null;
            }catch(Exception ex) {
                ex.printStackTrace();
                return null;
            }

        }, s -> "Nom de structure non reconnu.");
    }

    @Cmd(player = false, syntax = "Lister les structures enregistrées de la parcelle")
    public void list(CommandContext cmd) {
        if (getOlympaPlayer() == null)
            return;
        if (!PlotPerm.COMMAND_BLOCK.has((OlympaPlayerCreatif) getOlympaPlayer())){
            OCmsg.INSUFFICIENT_PLOT_PERMISSION.send((OlympaPlayerCreatif) getOlympaPlayer(), PlotPerm.COMMAND_BLOCK, ((OlympaPlayerCreatif)getOlympaPlayer()).getCurrentPlot());
            return;
        }

        Plot plot = ((OlympaPlayerCreatif)getOlympaPlayer()).getCurrentPlot();

        list(getOlympaPlayer(), plot);
    }

    @Cmd(player = false, syntax = "Sauvegarder une structure", min = 1, args = {"structure_name", "~", "~", "~", "~", "~", "~"})
    public void save(CommandContext cmd) {
        if (getOlympaPlayer() == null)
            return;
        if (!PlotPerm.COMMAND_BLOCK.has((OlympaPlayerCreatif) getOlympaPlayer())){
            OCmsg.INSUFFICIENT_PLOT_PERMISSION.send((OlympaPlayerCreatif) getOlympaPlayer(), PlotPerm.COMMAND_BLOCK, ((OlympaPlayerCreatif)getOlympaPlayer()).getCurrentPlot());
            return;
        }

        Plot plot = ((OlympaPlayerCreatif)getOlympaPlayer()).getCurrentPlot();

        if (plot == null){
            OCmsg.NULL_CURRENT_PLOT.send((OlympaPlayerCreatif) getOlympaPlayer());
            return;
        }

        Position[] pos;

        if (cmd.getArgumentsLength() == 7) {
            pos = new Position[2];
            pos[0] = new Position(CbCommand.parseLocation(plot, getPlayer().getLocation(), cmd.getArgument(1), cmd.getArgument(2),cmd.getArgument(3)));
            pos[1] = new Position(CbCommand.parseLocation(plot, getPlayer().getLocation(), cmd.getArgument(4), cmd.getArgument(5),cmd.getArgument(6)));

            if (pos[0] == null || pos[1] == null)
                pos = null;
        }else
            pos = plugin.getWEManager().convertSelectionToPositions(getOlympaPlayer());

        if (pos == null) {
            OCmsg.PLOT_SCHEMS_INVALID_SELECTION.send((OlympaPlayerCreatif) getOlympaPlayer());
            return;
        }

        save(getOlympaPlayer(),cmd.getArgument(0), plot, pos[0], pos[1]);
    }

    @Cmd(player = false, syntax = "Coller une structure", min = 4, args = {"SCHEMS_NAMES", "~", "~", "~"})
    public void paste(CommandContext cmd) {
        if (getOlympaPlayer() == null)
            return;
        if (!PlotPerm.COMMAND_BLOCK.has((OlympaPlayerCreatif) getOlympaPlayer())){
            OCmsg.INSUFFICIENT_PLOT_PERMISSION.send((OlympaPlayerCreatif) getOlympaPlayer(), PlotPerm.COMMAND_BLOCK, ((OlympaPlayerCreatif)getOlympaPlayer()).getCurrentPlot());
            return;
        }

        Plot plot = ((OlympaPlayerCreatif)getOlympaPlayer()).getCurrentPlot();

        if (plot == null) {
            OCmsg.NULL_CURRENT_PLOT.send((OlympaPlayerCreatif) getOlympaPlayer());
            return;
        }

        Location origin = CbCommand.parseLocation(plot, getPlayer().getLocation(), cmd.getArgument(1),  cmd.getArgument(2),  cmd.getArgument(3));

        if (origin == null) {
            OCmsg.PLOT_SCHEMS_OPERATION_OUT_OF_PLOT.send((OlympaPlayerCreatif) getOlympaPlayer());
            return;
        }

        paste(getOlympaPlayer(),cmd.getArgument(0), plot, new Position(origin));
    }

    @Cmd(player = false, syntax = "Supprimer une structure", min = 1, args = {"SCHEMS_NAMES"})
    public void delete(CommandContext cmd) {
        if (getOlympaPlayer() == null)
            return;
        if (!PlotPerm.COMMAND_BLOCK.has((OlympaPlayerCreatif) getOlympaPlayer())){
            OCmsg.INSUFFICIENT_PLOT_PERMISSION.send((OlympaPlayerCreatif) getOlympaPlayer(), PlotPerm.COMMAND_BLOCK, ((OlympaPlayerCreatif)getOlympaPlayer()).getCurrentPlot());
            return;
        }

        delete(getOlympaPlayer(), cmd.getArgument(0), ((OlympaPlayerCreatif)getOlympaPlayer()).getCurrentPlot());
    }



    public static void list(OlympaPlayerCreatif pc, Plot plot) {
        if (pc == null)
            return;

        if (plot == null) {
            OCmsg.NULL_CURRENT_PLOT.send(pc);
            return;
        }

        Prefix.INFO.sendMessage((CommandSender) pc.getPlayer(), "§6Structures de la parcelle " + plot +
                " (" + getSchemsOf(plot.getId().getId()).size() + "/" + OCparam.PLOT_MAX_SCHEMS.get() + ") :");

        for (String s : getSchemsOf(plot.getId().getId()))
            Prefix.INFO.sendMessage((CommandSender) pc.getPlayer(), "§e" + s);

        return;
    }

    public static void save(OlympaPlayerCreatif pc, String schemName, Plot plot, Position pos1, Position pos2) {
        OlympaCreatifMain.getInstance().getWEManager().savePlotSchem(pc, schemName + "_" + plot, plot, pos1, pos2,
                () -> getSchemsOf(plot.getId().getId()).add(schemName + "_" + plot));
    }

    public static void delete(OlympaPlayerCreatif pc, String schemName, Plot plot) {
        OlympaCreatifMain.getInstance().getWEManager().deletePlotSchem(pc, schemName, plot,
                () -> getSchemsOf(plot.getId().getId()).remove(schemName));
    }

    public static void paste(OlympaPlayerCreatif pc, String schemName, Plot plot, Position origin) {
        OlympaCreatifMain.getInstance().getWEManager().pastePlotSchem(pc, schemName, plot, origin);
    }




    private static Set<String> getSchemsOf(int plotId) {
        if (knownSchems.containsKey(plotId))
            return knownSchems.get(plotId);

        if (!getPlotSchemsFolder.apply(plotId).exists())
            knownSchems.put(plotId, ConcurrentHashMap.newKeySet());
        else
            knownSchems.put(plotId, Stream.of(getPlotSchemsFolder.apply(plotId).list()).map(s ->
                    s.replace(".schem","")).collect(Collectors.toSet()));

        return knownSchems.get(plotId);
    }

}
