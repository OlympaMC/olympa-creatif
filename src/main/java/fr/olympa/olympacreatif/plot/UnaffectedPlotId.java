package fr.olympa.olympacreatif.plot;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;

public class UnaffectedPlotId {
	
	OlympaCreatifMain plugin;
	
	protected int indexX;
	protected int indexZ;
	
	protected UnaffectedPlotId() {
	}
	
	public UnaffectedPlotId(OlympaCreatifMain plugin, int x, int z) {
		this.plugin = plugin;
		indexX = x;
		indexZ = z;
	}

	public int getX() {
		return indexX;
	}
	
	public int getZ() {
		return indexZ;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof UnaffectedPlotId && indexX == ((UnaffectedPlotId)obj).getX() && indexZ == ((UnaffectedPlotId)obj).getZ();	
	}

}
