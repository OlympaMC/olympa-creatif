package fr.olympa.olympacreatif.plot;

import fr.olympa.olympacreatif.OlympaCreatifMain;

public class AsyncPlot {

	private OlympaCreatifMain plugin;
	private PlotId id;
	private PlotMembers members;
	private PlotParameters params;
	
	private int plotId;
	
	public AsyncPlot(OlympaCreatifMain plugin, int plotId, PlotId plotLoc, PlotMembers plotMembers, PlotParameters plotParams) {
		this.plugin = plugin;
		this.id = plotLoc;
		this.members = plotMembers;
		this.params = plotParams;
		
		this.plotId = plotId;
	}
	
	public OlympaCreatifMain getPlugin() {
		return plugin;
	}
	
	public PlotId getLoc() {
		return id;
	}
	
	public PlotMembers getMembers() {
		return members;
	}
	
	public PlotParameters getParameters() {
		return params;
	}
	
	public int getId() {
		return plotId;
	}
}
