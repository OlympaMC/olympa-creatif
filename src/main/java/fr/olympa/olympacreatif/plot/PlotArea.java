package fr.olympa.olympacreatif.plot;

import org.bukkit.Location;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.DatabaseSerializable;

public class PlotArea implements DatabaseSerializable{

	private OlympaCreatifMain plugin;
	private int x1;
	private int x2;
	private int z1;
	private int z2;
	
	public PlotArea(OlympaCreatifMain plugin, int x1, int z1, int x2, int z2) {
		this.plugin = plugin;
		this.x1 = x1;
		this.x2 = x2;
		this.z1 = z1;
		this.z2 = z2;
	}
	
	//cherche une nouvelle zone libre opur un plot
	public PlotArea(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		
		//recherche du premier cercle de plots non plein (le plot central étant compté comme plein)
		int circleIndex = 0;
		boolean plotFound = false;
		int iFinal = 0;
		int jFinal = 0;
		
		//tant qu'un plot libre n'a pas été trouvé
		while (!plotFound) {
			//pour chaque cercle (on reteste aussi les cercles intérieurs déjà testés)
			for (int i = -circleIndex ; i <= circleIndex ; i++) {
				for (int j = -circleIndex ; j <= circleIndex ; j++) {
					//pour chaque area potentielle, on regarde si elle est déjà occupée
					if (!plotFound) {
						boolean validPlot = true;
						for (Plot plot : plugin.getPlots()) {
							if (plot.getArea().isInPlot(i * (plugin.plotXwidth + plugin.roadWidth), j * (plugin.plotXwidth + plugin.roadWidth))) {
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

		//attribution des coordonnées définitives du plot
		x1 = iFinal * (plugin.plotXwidth + plugin.roadWidth);
		z1 = jFinal * (plugin.plotZwidth + plugin.roadWidth);
		x2 = x1 + plugin.plotXwidth;
		z2 = z1 + plugin.plotZwidth;
		
	}
	
	public Location getFirstCorner(){
		return new Location(plugin.getWorldManager().getWorld(), x1+0.5, plugin.worldLevel + 1, z1+0.5);
	}
	
	public Location getSecondCorner() {
		return new Location(plugin.getWorldManager().getWorld(), x2-0.5, plugin.worldLevel + 1, z2-0.5);
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

	@Override
	public String toDbFormat() {
		return x1 + "," + z1 + " " + x2 + "," + z2;
	}
	
	public static PlotArea fromDbFormat(OlympaCreatifMain plugin, String data) {
		return new PlotArea(plugin, Integer.valueOf(data.split(" ")[0].split(",")[0]), Integer.valueOf(data.split(" ")[0].split(",")[1])
				, Integer.valueOf(data.split(" ")[1].split(",")[0]), Integer.valueOf(data.split(" ")[1].split(",")[1]));
	}
}
