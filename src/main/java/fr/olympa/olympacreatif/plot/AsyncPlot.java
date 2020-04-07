package fr.olympa.olympacreatif.plot;

import fr.olympa.olympacreatif.OlympaCreatifMain;

public class AsyncPlot {

	private OlympaCreatifMain plugin;
	private PlotId id;
	private PlotArea area;
	private PlotMembers members;
	private PlotParameters params;
	
	public AsyncPlot(OlympaCreatifMain plugin, PlotId plotId, PlotArea plotArea, PlotMembers plotMembers, PlotParameters plotParams) {
		this.plugin = plugin;
		this.id = plotId;
		this.members = plotMembers;
		this.area = plotArea;
		this.params = plotParams;
	}
	
	public OlympaCreatifMain getPlugin() {
		return plugin;
	}
	
	public PlotId getId() {
		return id;
	}
	
	public PlotArea getArea() {
		return area;
	}
	
	public PlotMembers getMembers() {
		return members;
	}
	
	public PlotParameters getParameters() {
		return params;
	}
}
