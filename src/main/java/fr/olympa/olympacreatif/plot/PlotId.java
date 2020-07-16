package fr.olympa.olympacreatif.plot;

import org.bukkit.Location;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.world.WorldManager;

public class PlotId {

	private OlympaCreatifMain plugin;
	
	private final int plotId;
	private final Location loc;
	
	protected int indexX;
	protected int indexZ;
	
	private PlotId(OlympaCreatifMain plugin, int plotId) {
		this.plugin = plugin;

		this.plotId = plotId;
		
		int circleIndex = 1;
		int lineSize = 0;
		int plotIndex = 1;
		int lineIndex = 0;
		int plotLineIndex = 0;
		
		//recherche du premier cercle de plots non plein (plot central = circleIndex 1)
		while (plotId > Math.pow(circleIndex*2-1, 2))
			circleIndex++;		
		
		if (circleIndex == 1) {
			indexX = 0;
			indexZ = 0;
			loc = new Location(plugin.getWorldManager().getWorld(), 0, WorldManager.worldLevel, 0);
			return;	
		}
		
		//nombre de plots par ligne de tour (4 par cercle)
		lineSize = (int) Math.pow(circleIndex - 1, 2);
		
		//index du plot sur le cercle en cours de remplissage (commence à 0)
		plotIndex = plotId - lineSize - 1;//(int) (Math.pow(circleIndex*2-1, 2) - plotId);
		
		//index de la ligne sur laquelle sera placée le plot (entre 0 et 3 en commençant par le côté en haut à gauche puis sens horaire)
		lineIndex = (int) (plotIndex / ((circleIndex * 2) - 2));//(int) (plotIndex / ((circleIndex-1)*2));
		
		plotLineIndex = plotIndex - lineIndex * lineSize;
		
		switch(lineIndex) {
		case 0:
			indexX = -circleIndex + 1 + plotLineIndex;
			indexZ = circleIndex - 1;
			break;
		case 1:
			indexX = circleIndex - 1;
			indexZ = circleIndex - 1 - plotLineIndex;
			break;
		case 2:
			indexX = circleIndex - 1 - plotLineIndex;
			indexZ = - circleIndex + 1;
			break;
		case 3:
			indexX = - circleIndex + 1;
			indexZ = - circleIndex + 1 + plotLineIndex;
			break;
		}
		
		loc = new Location(plugin.getWorldManager().getWorld(), 
				indexX * (WorldManager.plotSize + WorldManager.roadSize) + 0.5, 
				WorldManager.worldLevel + 1, 
				indexZ * (WorldManager.plotSize + WorldManager.roadSize) + 0.5);
	}
	
	//crée un nouveau plotId (EXCLUSIVEMENT pour un nouveau plot)
	public static PlotId createNew(OlympaCreatifMain plugin) {
		plugin.getPlotsManager().incrementTotalPlotCount();
		return new PlotId(plugin, plugin.getPlotsManager().getTotalPlotCount());
	}

	//retourne un nouveau plotId avec l'id en paramètre
	public static PlotId fromId(OlympaCreatifMain plugin, int id) {
		if (id <= plugin.getPlotsManager().getTotalPlotCount())
			return new PlotId(plugin, id);
		else
			return null;
	}

	//retourne un PlotId si un plot est affecté à cet id (chargé ou non)
	public static PlotId fromString(OlympaCreatifMain plugin, String idAsString) {
		
		try {
			Integer id = PlotsManager.getPlotIdFromString(idAsString);
			
			if (id <= plugin.getPlotsManager().getTotalPlotCount())			
				return new PlotId(plugin, id);
			
		}catch(NumberFormatException e) {
		}
		return null;
	}
	
	//retourne un PlotId si la localisation est sur un plot (chargé ou non)
	public static PlotId fromLoc(OlympaCreatifMain plugin, Location loc) {
		int x = Math.floorMod(loc.getBlockX(), WorldManager.plotSize + WorldManager.roadSize);
		int z = Math.floorMod(loc.getBlockZ(), WorldManager.plotSize + WorldManager.roadSize);
		
		//return null si route
		if (x >= WorldManager.plotSize || z >= WorldManager.plotSize)
			return null;

		//recherche de l'id du plot selon ses coords
		int plotId = 1;
		
		int plotX = Math.floorDiv(loc.getBlockX(), WorldManager.plotSize + WorldManager.roadSize);
		int plotZ = Math.floorDiv(loc.getBlockZ(), WorldManager.plotSize + WorldManager.roadSize);
		
		int circleIndex = Math.max(Math.abs(plotX), Math.abs(plotZ)) + 1;
		plotId += Math.pow(circleIndex - 1, 2);
		
		int plotsPerLine = (circleIndex - 1) * 2;
		
		int lineIndex = -1;
		//recherche de l'indice de la rangée du plot (entre 0 et 3)
		if (plotX >= -circleIndex + 1 && plotX <= circleIndex - 2 && plotZ == circleIndex - 1)
			lineIndex = 0;
		else if (plotZ >= -circleIndex + 2 && plotZ <= circleIndex - 1 && plotX == circleIndex - 1)
			lineIndex = 1;
		else if (plotX >= -circleIndex + 2 && plotX <= circleIndex - 1 && plotZ == -circleIndex + 1)
			lineIndex = 2;
		else if (plotZ >= -circleIndex + 1 && plotZ <= circleIndex - 2 && plotX == -circleIndex + 1)
			lineIndex = 3;
		
		plotId += lineIndex * plotsPerLine;
		
		switch(lineIndex) {
		case 0:
			plotId += circleIndex + plotX;
			break;
		case 1:
			plotId += circleIndex - plotZ;
			break;
		case 2:
			plotId += circleIndex - plotX;
			break;
		case 3:
			plotId += circleIndex + plotZ;
			break;
		}
		
		if (plotId <= plugin.getPlotsManager().getTotalPlotCount())
			return new PlotId(plugin, plotId);
		else
			return null;
	}
	
	

	public int getX() {
		return indexX;
	}
	
	public int getZ() {
		return indexZ;
	}
	
	public Object getId(boolean useBase36) {
		if (useBase36) 
			return PlotsManager.getPlotIdAsString(plotId);
		else
			return plotId;
	}
	
	public Location getLocation() {
		return loc;
	}
	
	public boolean isInPlot(Location loc) {
		return isInPlot(loc.getBlockX(), loc.getBlockZ());
	}
	
	public boolean isInPlot(int x, int z) {
		
		return x >= indexX * (WorldManager.plotSize + WorldManager.roadSize) && 
				x < indexX * (WorldManager.plotSize + WorldManager.roadSize) + WorldManager.plotSize && 
				z >= indexZ * (WorldManager.plotSize + WorldManager.roadSize) && 
				z < indexZ * (WorldManager.plotSize + WorldManager.roadSize) + WorldManager.plotSize; 
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof PlotId && indexX == ((PlotId)obj).getX() && indexZ == ((PlotId)obj).getZ();	
	}
}
