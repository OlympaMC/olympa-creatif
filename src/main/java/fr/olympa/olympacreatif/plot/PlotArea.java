package fr.olympa.olympacreatif.plot;

import org.bukkit.Location;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;

public class PlotArea {

	private OlympaCreatifMain plugin;
	private int x1;
	private int x2;
	private int z1;
	private int z2;
	
	PlotArea(OlympaCreatifMain plugin, int x1, int z1, int x2, int z2) {
		this.plugin = plugin;
		this.x1 = x1;
		this.x2 = x2;
		this.z1 = z1;
		this.z2 = z2;
	}
	
	//cherche une nouvelle zone libre opur un plot
	PlotArea(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		
		//recherche du premier cercle de plots non plein (le plot central étant compté comme plein)
		int circleIndex = 1;
		int plotIndex = 1;
		int rowIndex = 0;
		int plotRowIndex = 0;
		
		while (plugin.getPlotsManager().getTotalPlotCount() > Math.pow(circleIndex*2-1, 2)) {
			circleIndex++;
		}

		
		plotIndex = (int) (Math.pow(circleIndex*2-1, 2) - plugin.getPlotsManager().getTotalPlotCount());
		rowIndex = plotIndex / ((int) (Math.pow(circleIndex, 2) - Math.pow(circleIndex-1, 2)) / 4);
		plotRowIndex = (int) (plotIndex - rowIndex * ((Math.pow(circleIndex, 2) - Math.pow(circleIndex-1, 2)) / 4));
		//sens : x++ (top to bottom)
		switch (rowIndex) {
		case 0: //x++, -z
			x1 = plotRowIndex - circleIndex + 1;
			z1 = -circleIndex + 1;
			break;
		case 1: //+x, z++
			x1 = circleIndex - 1;
			z1 = plotRowIndex - circleIndex + 1;
			break;
		case 2: //x--, +z
			x1 = -plotRowIndex + circleIndex - 1;
			z1 = circleIndex - 1;
			break;
		case 3: //-x, z--
			x1 = -circleIndex + 1;
			z1 = -plotRowIndex + circleIndex - 1;
			break;
		}

		plugin.getPlotsManager().incrementTotalPlotsCount();
		
		x1 = x1 * (Integer.valueOf(Message.PARAM_PLOT_X_SIZE.getValue()) + Integer.valueOf(Message.PARAM_ROAD_SIZE.getValue()));
		z1 = z1 * (Integer.valueOf(Message.PARAM_PLOT_Z_SIZE.getValue()) + Integer.valueOf(Message.PARAM_ROAD_SIZE.getValue()));
		x2 = x1 + Integer.valueOf(Message.PARAM_PLOT_X_SIZE.getValue());
		z2 = z1 + Integer.valueOf(Message.PARAM_PLOT_Z_SIZE.getValue());
			
		//tant qu'un plot libre n'a pas été trouvé
		/*
		while (!plotFound) {
			//pour chaque cercle (on reteste aussi les cercles intérieurs déjà testés)
			for (int i = -circleIndex ; i <= circleIndex ; i++) {
				for (int j = -circleIndex ; j <= circleIndex ; j++) {
					//pour chaque area potentielle, on regarde si elle est déjà occupée
					if (!plotFound) {
						boolean validPlot = true;
						for (Plot plot : plugin.getPlotsManager().getPlots()) {
							if (plot.getArea().isInPlot(i * (Integer.valueOf(Message.PARAM_PLOT_X_SIZE.getValue()) + Integer.valueOf(Message.PARAM_ROAD_SIZE.getValue())), 
									j * (Integer.valueOf(Message.PARAM_PLOT_Z_SIZE.getValue()) + Integer.valueOf(Message.PARAM_ROAD_SIZE.getValue())))) {
								validPlot = false;
							}
						}
						
						//si le plot est valide, enregistrement des indexs correspondants
						if (!plotFound && validPlot) {
							plotFound = true;
							iFinal = i;
							jFinal = j;
						}
					}
				}
			}
			circleIndex++;
		}

		*/
		
	}
	
	public Location getFirstCorner(){
		return new Location(plugin.getWorldManager().getWorld(), x1+0.5, Integer.parseInt(Message.PARAM_WORLD_LEVEL.getValue()) + 1, z1+0.5);
	}
	
	public Location getSecondCorner() {
		return new Location(plugin.getWorldManager().getWorld(), x2-0.5, Integer.parseInt(Message.PARAM_WORLD_LEVEL.getValue()) + 1, z2-0.5);
	}
	
	public boolean isInPlot(Location loc) {
		return isInPlot(loc.getBlockX(), loc.getBlockZ());
	}
	
	public boolean isInPlot(int x, int z) {
		if (x >= x1 && x <= x2 && z >= z1 && z <= z2)
			return true;
		else
			return false;
	}
}
