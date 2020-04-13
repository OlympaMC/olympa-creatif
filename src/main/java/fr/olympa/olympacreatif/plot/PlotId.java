package fr.olympa.olympacreatif.plot;

import org.bukkit.Location;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;

public class PlotId{

	int indexX;
	int indexZ;
	private OlympaCreatifMain plugin;
	
	public PlotId(OlympaCreatifMain plugin) {
		this.plugin = plugin;

		double circleIndex = 1;
		double plotIndex = 1;
		double lineIndex = 0;
		double plotLineIndex = 0;

		plugin.getPlotsManager().incrementTotalPlotCount();
		
		//recherche du premier cercle de plots non plein (plot central = indice 1)
		while (plugin.getPlotsManager().getTotalPlotCount() > Math.pow(circleIndex*2-1, 2))
			circleIndex++;		
		
		//index du plot sur le cercle en cours de remplissage (commence à 0)
		plotIndex = (int) (Math.pow(circleIndex*2-1, 2) - plugin.getPlotsManager().getTotalPlotCount());
		
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
	
	public PlotId(OlympaCreatifMain plugin, int x, int z) {
		this.plugin = plugin;
		indexX = x;
		indexZ = z;
	}
	
	//toujours appeler isIdValid avant !
	public PlotId(OlympaCreatifMain plugin, String idAsString){
		this.plugin = plugin;
		indexX = Integer.valueOf(idAsString.split(".")[0]);
		indexZ = Integer.valueOf(idAsString.split(".")[1]);
	}
	
	public String getAsString() {
		return indexX + "." + indexZ;
	}
	
	public Location getLocation() {
		return new Location(plugin.getWorldManager().getWorld(), indexX * (Integer.valueOf(Message.PARAM_PLOT_X_SIZE.getValue()) + Integer.valueOf(Message.PARAM_ROAD_SIZE.getValue())) + 0.5, Integer.valueOf(Message.PARAM_WORLD_LEVEL.getValue()) + 1, indexZ * (Integer.valueOf(Message.PARAM_PLOT_Z_SIZE.getValue()) + Integer.valueOf(Message.PARAM_ROAD_SIZE.getValue())) + 0.5);
	}
	
	public boolean isInPlot(Location loc) {
		return isInPlot(loc.getBlockX(), loc.getBlockZ());
	}
	
	public boolean isInPlot(int x, int z) {
		int plotX = Integer.valueOf(Message.PARAM_PLOT_X_SIZE.getValue());
		int plotZ = Integer.valueOf(Message.PARAM_PLOT_Z_SIZE.getValue());
		int plotRoad = Integer.valueOf(Message.PARAM_ROAD_SIZE.getValue());
		
		return x >= indexX * (plotX + plotRoad) && 
				x < indexX * (plotX + plotRoad) + plotX && 
				z >= indexZ * (plotZ + plotRoad) && 
				z < indexZ * (plotZ + plotRoad) + plotZ; 
	}
	
	public static boolean isIdValid(String id) {
		if (id.split(".").length == 2)
			try {
				Integer.valueOf(id.split(".")[0]);
				Integer.valueOf(id.split(".")[1]);	
			}catch(NumberFormatException e) {
				return false;
			}
		return true;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof PlotId && ((PlotId) obj).getAsString().equals(getAsString());	
	}
}
