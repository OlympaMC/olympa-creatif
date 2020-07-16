package fr.olympa.olympacreatif.plot;

import fr.olympa.olympacreatif.OlympaCreatifMain;

public class AsyncPlot {

	private OlympaCreatifMain plugin;
	private PlotId id;
	private PlotMembers members;
	private PlotParameters params;
	
	public AsyncPlot(OlympaCreatifMain plugin, PlotId plotLoc, PlotMembers plotMembers, PlotParameters plotParams) {
		this.plugin = plugin;
		this.id = plotLoc;
		this.members = plotMembers;
		this.params = plotParams;
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
}
