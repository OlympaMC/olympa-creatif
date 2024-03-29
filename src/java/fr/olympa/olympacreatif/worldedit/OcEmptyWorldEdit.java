package fr.olympa.olympacreatif.worldedit;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.Position;
import fr.olympa.olympacreatif.plot.Plot;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Set;

public class OcEmptyWorldEdit extends AWorldEdit {

    public OcEmptyWorldEdit(OlympaCreatifMain plugin) {
        super(plugin);
    }

    @Override
    public void clearClipboard(Plot plot, Player p) {
    }

    @Override
    public boolean resetPlot(OlympaPlayerCreatif requester, Plot plot) {
        OCmsg.WE_DISABLED.send(requester);
        return false;
    }

    @Override
    public Position[] convertSelectionToPositions(OlympaPlayerCreatif pc) {
        //OCmsg.WE_DISABLED.send(pc);
        return null;
    }

    @Override
    public void savePlotSchem(OlympaPlayerCreatif pc, String schemName, Plot plot, Position pos1, Position pos2, Runnable successCallback) {
        OCmsg.WE_DISABLED.send(pc);
    }

    @Override
    public void deletePlotSchem(OlympaPlayerCreatif pc, String schemName, Plot plot, Runnable successCallback) {
        OCmsg.WE_DISABLED.send(pc);
    }

    @Override
    public void pastePlotSchem(OlympaPlayerCreatif pc, String schemName, Plot plot, Position origin) {
        OCmsg.WE_DISABLED.send(pc);
    }

    @Override
    public boolean setPlotFloor(OlympaPlayerCreatif pc, Plot plot, Set<Material> mat, int matY) {
        OCmsg.WE_DISABLED.send(pc);
        return false;
    }

    @Override
    public boolean exportPlot(Plot plot, OlympaPlayerCreatif p) {
        OCmsg.WE_DISABLED.send(p);
        return false;
    }

    @Override
    public boolean restorePlot(Plot plot, OlympaPlayerCreatif p) {
        OCmsg.WE_DISABLED.send(p);
        return false;
    }
}
