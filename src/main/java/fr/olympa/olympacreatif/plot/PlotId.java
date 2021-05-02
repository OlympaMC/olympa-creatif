package fr.olympa.olympacreatif.plot;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OCparam;
import fr.olympa.olympacreatif.data.Position;
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
		int plotIndex = 0;
		int lineIndex = 0;
		int plotLineIndex = 0;
		
		//recherche du premier cercle de plots non plein (plot central = circleIndex 1)
		while (plotId > Math.pow(circleIndex*2-1, 2))
			circleIndex++;
		
		//nombre de plots par ligne de tour (4 par cercle)
		lineSize = circleIndex * 2 - 2;//(int) Math.pow(circleIndex - 1, 2);
		
		//index du plot sur le cercle en cours de remplissage (commence à 0)
		if (circleIndex > 1)
			plotIndex = (int) (plotId - Math.pow((circleIndex - 1) * 2 - 1, 2)) - 1;//(int) (Math.pow(circleIndex*2-1, 2) - plotId);
		
		if (lineSize > 0)
			while (plotIndex >= lineSize * (lineIndex + 1))
				lineIndex++;
		
		plotLineIndex = plotIndex - lineIndex * lineSize;
		
		//Bukkit.broadcastMessage("lineSize = " + lineSize + " - plotIndex = " + plotIndex + " - lineIndex = " + lineIndex + " - plotLineIndex = " + plotLineIndex);
		
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
				indexX * (OCparam.PLOT_SIZE.get() + WorldManager.roadSize) + 0.5, 
				WorldManager.worldLevel + 1, 
				indexZ * (OCparam.PLOT_SIZE.get() + WorldManager.roadSize) + 0.5);
	}
	
	private PlotId(OlympaCreatifMain plugin, int id, int x, int z) {
		this.plugin = plugin;
		plotId = id;
		indexX = x;
		indexZ = z;
		
		loc = new Location(plugin.getWorldManager().getWorld(), 
				indexX * (OCparam.PLOT_SIZE.get() + WorldManager.roadSize) + 0.5, 
				WorldManager.worldLevel + 1, 
				indexZ * (OCparam.PLOT_SIZE.get() + WorldManager.roadSize) + 0.5);
	}
	
	/**
	 * Crée un nouveau plotId (EXCLUSIVEMENT pour un nouveau plot)
	 * @param plugin
	 * @return
	 */
	public static PlotId createNew(OlympaCreatifMain plugin) {
		plugin.getPlotsManager().incrementTotalPlotCount();
		plugin.getWorldManager().updateWorldBorder();
		
		return new PlotId(plugin, plugin.getPlotsManager().getTotalPlotCount());
	}

	/**
	 * Retourne un nouveau plotId avec l'id en paramètre
	 * @param plugin
	 * @param id
	 * @return
	 */
	public static PlotId fromId(OlympaCreatifMain plugin, Integer id) {
		if (id == null)
			return null;
		
		if (plugin.getPlotsManager() == null)
			return new PlotId(plugin, id);
		else if (id <= plugin.getPlotsManager().getTotalPlotCount())
			return new PlotId(plugin, id);
		else
			return null;
	}

	/**
	 * Retourne un PlotId si un plot est affecté à cet id (chargé ou non)
	 * @param plugin
	 * @param idAsString
	 * @return
	 */
	/*
	public static PlotId fromString(OlympaCreatifMain plugin, String idAsString) {

		Integer id = PlotsManager.getPlotIdFromString(idAsString);
		
		if (id != null && id <= plugin.getPlotsManager().getTotalPlotCount())			
			return new PlotId(plugin, id);
		
		return null;
	}*/
	public static PlotId fromString(OlympaCreatifMain plugin, String idAsString) {
		try {
			int id = Integer.valueOf(idAsString);
			if (id > 0 && id <= plugin.getPlotsManager().getTotalPlotCount())			
				return new PlotId(plugin, id);
			else
				return null;
		}catch(NumberFormatException e) {
			return null;
		}
	}
	
	//retourne un PlotId si la localisation est sur un plot (chargé ou non)
	public static PlotId fromLoc(OlympaCreatifMain plugin, Location loc) {
		int x = Math.floorMod(loc.getBlockX(), OCparam.PLOT_SIZE.get() + WorldManager.roadSize);
		int z = Math.floorMod(loc.getBlockZ(), OCparam.PLOT_SIZE.get() + WorldManager.roadSize);
		
		//return null si route
		if (x >= OCparam.PLOT_SIZE.get() || z >= OCparam.PLOT_SIZE.get())
			return null;
		
		//Bukkit.broadcastMessage("x = " + x + " - z = " + z);

		//recherche de l'id du plot selon ses coords
		int plotId = 0;
		
		int plotX = Math.floorDiv(loc.getBlockX(), OCparam.PLOT_SIZE.get() + WorldManager.roadSize);
		int plotZ = Math.floorDiv(loc.getBlockZ(), OCparam.PLOT_SIZE.get() + WorldManager.roadSize);
		
		int circleIndex = Math.max(Math.abs(plotX), Math.abs(plotZ)) + 1;
		plotId += Math.pow((circleIndex - 1) * 2 - 1, 2);
		
		int plotsPerLine = circleIndex * 2 - 2;
		
		//Bukkit.broadcastMessage("circleIndex = " + circleIndex + " - plotsPerLine = " + plotsPerLine);
		
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
		
		//Bukkit.broadcastMessage("lineIndex = " + lineIndex);
		
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
		
		//Bukkit.broadcastMessage("plotId = " + plotId);
		
		if (plotId <= plugin.getPlotsManager().getTotalPlotCount())
			return new PlotId(plugin, plotId, plotX, plotZ);
		else
			return null;
	}
	
	

	public int getIndexX() {
		return indexX;
	}
	
	public int getIndexZ() {
		return indexZ;
	}
	
	public int getId() {
		return plotId;
	}
	
	@Override
	public String toString() {
		return "" + plotId;
	}
	
	public Location getLocation() {
		return loc;
	}
	
	public void teleport(Player p) {
		Plot plot = plugin.getPlotsManager().getPlot(this);
		if (plot != null)
			plot.getParameters().getParameter(PlotParamType.SPAWN_LOC).teleport(p);
		else
			p.teleport(getLocation());
	}
	
	public boolean isInPlot(Position loc) {
		return isInPlot((int) loc.getX(), (int) loc.getZ());
	}
	
	public boolean isInPlot(Location loc) {
		return isInPlot(loc, 0);
	}
	
	public boolean isInPlot(int x, int z) {
		return isInPlot(x, z, 0);
		/*return x >= indexX * (OCparam.PLOT_SIZE.get() + WorldManager.roadSize) && 
				x < indexX * (OCparam.PLOT_SIZE.get() + WorldManager.roadSize) + OCparam.PLOT_SIZE.get() && 
				z >= indexZ * (OCparam.PLOT_SIZE.get() + WorldManager.roadSize) && 
				z < indexZ * (OCparam.PLOT_SIZE.get() + WorldManager.roadSize) + OCparam.PLOT_SIZE.get();*/ 
	}
	
	public boolean isInPlot(Location loc, int radius) {
		return isInPlot(loc.getBlockX(), loc.getBlockZ(), radius);
	}
	
	public boolean isInPlot(int x, int z, int radius) {
		return x >= indexX * (OCparam.PLOT_SIZE.get() + WorldManager.roadSize) + radius && 
				x < indexX * (OCparam.PLOT_SIZE.get() + WorldManager.roadSize) + OCparam.PLOT_SIZE.get() - radius && 
				z >= indexZ * (OCparam.PLOT_SIZE.get() + WorldManager.roadSize) + radius && 
				z < indexZ * (OCparam.PLOT_SIZE.get() + WorldManager.roadSize) + OCparam.PLOT_SIZE.get() - radius; 
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof PlotId && plotId == ((PlotId)obj).getId();	
	}
}
