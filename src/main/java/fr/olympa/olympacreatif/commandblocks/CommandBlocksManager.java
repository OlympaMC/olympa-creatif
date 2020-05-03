package fr.olympa.olympacreatif.commandblocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;

public class CommandBlocksManager {

	private OlympaCreatifMain plugin;
	private Map<Plot, List<CbCommand>> queuedCommands = new HashMap<Plot, List<CbCommand>>(); 
	private Map<Plot, List<PlotObjective>> plotObjectives = new HashMap<Plot, List<PlotObjective>>();
	
	public CommandBlocksManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;
	}
	
	public void registerPlot(Plot plot) {
		if (!queuedCommands.containsKey(plot))
			queuedCommands.put(plot, new ArrayList<CbCommand>());
	}
	
	public void unregisterPlot(Plot plot) {
		queuedCommands.remove(plot);
	}
	
	
}
