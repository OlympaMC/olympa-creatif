package fr.olympa.olympacreatif.commands;

import fr.olympa.api.common.command.complex.ArgumentParser;
import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.common.permission.OlympaSpigotPermission;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.common.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OcPermissions;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.plot.Plot;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StructureCommand extends AbstractCmd {

    private Function<Integer, File> getPlotSchemsFolder = id -> new File(OlympaCreatifMain.getInstance().getDataFolder() + "/plot_schematics/plot_" + id);
    private Map<Integer, Set<String>> knownSchems = new HashMap<Integer, Set<String>>();

    public StructureCommand(OlympaCreatifMain plugin, OlympaSpigotPermission permission, String desc) {
        super(plugin, "structure", OcPermissions.STRUCTURE_COMMAND, desc);

        addArgumentParser("SCHEMS_NAMES", (sender, arg) -> {
            OlympaPlayerCreatif pc = AccountProvider.getter().get(((Player)sender).getUniqueId());
            if (pc == null || pc.getCurrentPlot() == null)
                return null;

            if (knownSchems.containsKey(pc.getCurrentPlot().getId().getId()))
                return knownSchems.get(pc.getCurrentPlot().getId().getId());

            if (!getPlotSchemsFolder.apply(pc.getCurrentPlot().getId().getId()).exists())
                knownSchems.put(pc.getCurrentPlot().getId().getId(), new HashSet<String>());
            else
                knownSchems.put(pc.getCurrentPlot().getId().getId(),
                        Stream.of(getPlotSchemsFolder.apply(pc.getCurrentPlot().getId().getId()).list()).map(s ->
                        pc.getCurrentPlot() + "_" + s.replace(".schem","")).collect(Collectors.toSet()));

            return knownSchems.get(pc.getCurrentPlot().getId().getId());

        }, arg -> {
            int plotIndex = arg.indexOf("_");
            if (plotIndex == -1)
                return null;

            try {
                int plotId = Integer.parseInt(arg.substring(0, plotIndex));
                if (!knownSchems.containsKey(plotId) || !knownSchems.get(plotId).contains(arg))
                    return null;

                return new File(getPlotSchemsFolder.apply(plotId), arg.substring(plotIndex + 1, arg.length() - 1) + ".schem");
            }catch(Exception ex){
                return null;
            }

        }, s -> "Nom de structure non reconnu.", true);
    }

    @Cmd(player = false, syntax = "Lister les structures enregistrées de la parcelle")
    public void list(CommandContext cmd) {
        if (getOlympaPlayer() == null)
            return;

        Plot plot = ((OlympaPlayerCreatif)getOlympaPlayer()).getCurrentPlot();

    }

    public static void list(OlympaPlayerCreatif pc, Plot plot) {
        if (pc == null)
            return;

        if (plot == null){
            OCmsg.NULL_CURRENT_PLOT.send(pc);
            return;
        }

        Prefix.INFO.sendMessage((CommandSender) pc.getPlayer(), "Structures de la parcelle " + plot);
    }

}
