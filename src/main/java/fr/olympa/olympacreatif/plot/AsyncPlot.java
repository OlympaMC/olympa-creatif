package fr.olympa.olympacreatif.plot;

import fr.olympa.olympacreatif.OlympaCreatifMain;

public class AsyncPlot {

	private OlympaCreatifMain plugin;
	private PlotId id;
	private PlotMembers members;
	private PlotParameters params;
	
	private boolean liquidFlow;
	
	public AsyncPlot(OlympaCreatifMain plugin, PlotId plotLoc, PlotMembers plotMembers, PlotParameters plotParams, boolean liquidFlow) {
		this.plugin = plugin;
		this.id = plotLoc;
		this.members = plotMembers;
		this.params = plotParams;
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
	
	public PlotParameters getParameters() {
		return params;
	}

	public boolean getAllowLiquidFlow() {
		return liquidFlow;
	}
}
