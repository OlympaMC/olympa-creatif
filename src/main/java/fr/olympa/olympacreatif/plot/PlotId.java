package fr.olympa.olympacreatif.plot;

import org.bukkit.Location;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.world.WorldManager;

public class PlotId extends UnaffectedPlotId{

	private OlympaCreatifMain plugin;
	
	private final int plotId;
	
	public PlotId(OlympaCreatifMain plugin, int plotId) {
		this.plugin = plugin;

		this.plotId = plotId;
		
		double circleIndex = 1;
		double plotIndex = 1;
		double lineIndex = 0;
		double plotLineIndex = 0;
		
		//recherche du premier cercle de plots non plein (plot central = indice 1)
		while (plotId > Math.pow(circleIndex*2-1, 2))
			circleIndex++;		
		
		//index du plot sur le cercle en cours de remplissage (commence à 0)
		plotIndex = (int) (Math.pow(circleIndex*2-1, 2) - plotId);
		
		//index de la ligne sur laquelle sera placée le plot (entre 0 et 3 en commençant par le côté gauche puis sens horaire)
		lineIndex = (int) (plotIndex / ((circleIndex-1)*2));
		
		//index du plot sur la ligne (commence à 0)
		plotLineIndex = plotIndex % ((circleIndex-1)*2);

		//sens : x++ (top to bottom)
		switch ((int)lineIndex) {
		case 0: //x++, -z
			indexX = (int) (plotLineIndex - circleIndex + 1);
			indexZ = (int) (-circleIndex + 1);
			break;
		case 1: //+x, z++
			indexX = (int) (circleIndex - 1);
			indexZ = (int) (plotLineIndex - circleIndex + 1);
			break;
		case 2: //x--, +z
			indexX = (int) (-plotLineIndex + circleIndex - 1);
			indexZ = (int) (circleIndex - 1);
			break;
		case 3: //-x, z--
			indexX = (int) (-circleIndex + 1);
			indexZ = (int) (-plotLineIndex + circleIndex - 1);
			break;
		}
	}
	
	/*
	public PlotLoc(OlympaCreatifMain plugin, int x, int z) {
		this.plugin = plugin;
		indexX = x;
		indexZ = z;
	}
	*/
	
	
	/*
	
	public PlotLoc(OlympaCreatifMain plugin, String idAsString){
		this.plugin = plugin;
		indexX = Integer.valueOf(idAsString.split(".")[0]);
		indexZ = Integer.valueOf(idAsString.split(".")[1]);
	}
	
	public static PlotLoc fromString(OlympaCreatifMain plugin, String id) {
		if (id.split("\\.").length == 2)
			try {
				return new PlotLoc(plugin, Integer.valueOf(id.split("\\.")[0]), Integer.valueOf(id.split("\\.")[1]));
			}catch(NumberFormatException e) {
				return null;
			}
		return null;
	}
	*/
	
	
	public Object getId(boolean useBase36) {
		if (useBase36) 
			return PlotsManager.getPlotIdAsString(plotId);
		else
			return plotId;
	}
	
	
	public Location getLocation() {
		return new Location(plugin.getWorldManager().getWorld(), 
				indexX * (WorldManager.plotSize + WorldManager.roadSize) + 0.5, 
				WorldManager.worldLevel + 1, 
				indexZ * (WorldManager.plotSize + WorldManager.roadSize) + 0.5);
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

	//retourne null si le plot associé à cet id n'existe pas encore ou si l'id est malformé
	public static PlotId fromString(OlympaCreatifMain plugin, String locAsString) {
		
		try {
			Integer id = PlotsManager.getPlotIdFromString(locAsString);
			
			if (id > plugin.getPlotsManager().getTotalPlotCount())
				return null;
			
			return new PlotId(plugin, id);
		}catch(NumberFormatException e) {
			return null;
		}
	}
}
