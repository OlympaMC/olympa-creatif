package fr.olympa.olympacreatif.plot;

import fr.olympa.olympacreatif.OlympaCreatifMain;

public class AsyncPlot {

	private OlympaCreatifMain plugin;
	private PlotId id;
	private PlotMembers members;
	private PlotParametersBIS params;
	private PlotCbData cbData;
	
	private boolean liquidFlow;
	
	public AsyncPlot(OlympaCreatifMain plugin, PlotId plotLoc, PlotMembers plotMembers, PlotParametersBIS plotParams, PlotCbData cbData, boolean liquidFlow) {
		this.plugin = plugin;
		this.id = plotLoc;
		this.members = plotMembers;
		this.params = plotParams;
		this.cbData = cbData;
		this.liquidFlow = liquidFlow;
	}
	
	public OlympaCreatifMain getPlugin() {
		return plugin;
	}
	
	public PlotId getId() {
		return id;
	}
	
	public PlotMembers getMembers() {
		return members;
	}
	
	public PlotParametersBIS getParameters() {
		return params;
	}

	public PlotCbData getCbData() {
		return cbData;
	}

	public boolean getAllowLiquidFlow() {
		return liquidFlow;
	}
}
