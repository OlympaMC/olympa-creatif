package fr.olympa.olympacreatif.plot;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.DatabaseSerializable;

public class PlotId implements DatabaseSerializable {

	String id;
	
	public PlotId(OlympaCreatifMain plugin, PlotArea area) {
		this.id = ((int) area.getFirstCorner().getBlockX() / (plugin.plotXwidth+plugin.roadWidth)) +  "," +
				((int) area.getFirstCorner().getBlockZ() / (plugin.plotZwidth+plugin.roadWidth));
	}
	
	public PlotId(OlympaCreatifMain plugin, String plotId) {
		this.id = plotId;
	}
	
	public String getId() {
		return id;
	}

	@Override
	public String toDbFormat() {
		return id;
	}
	
	public static PlotId fromDbFormat(OlympaCreatifMain plugin, String data) {
		return new PlotId(plugin, data);
	}
}
