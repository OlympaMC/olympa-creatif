package fr.olympa.olympacreatif.plot;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.DatabaseSerializable;
import fr.olympa.olympacreatif.data.Message;

public class PlotId implements DatabaseSerializable {

	String id;
	
	public PlotId(OlympaCreatifMain plugin, PlotArea area) {
		this.id = ((int) area.getFirstCorner().getBlockX() / (Integer.parseInt(Message.PARAM_PLOT_X_SIZE.getValue()) + Integer.parseInt(Message.PARAM_ROAD_SIZE.getValue()))) +  "," +
				((int) area.getFirstCorner().getBlockZ() / (Integer.parseInt(Message.PARAM_PLOT_Z_SIZE.getValue()) + Integer.parseInt(Message.PARAM_ROAD_SIZE.getValue())));
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
