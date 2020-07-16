package fr.olympa.olympacreatif.plot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;
import fr.olympa.olympacreatif.world.WorldManager;

public class PlotsManager {

	private OlympaCreatifMain plugin;
	private List<Plot> loadedPlots = new ArrayList<Plot>();
	
	private List<AsyncPlot> asyncPlots = new ArrayList<AsyncPlot>();
	
	private int plotCount;
	
	public PlotsManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;

		plugin.getServer().getPluginManager().registerEvents(new PlotsManagerListener(plugin), plugin);
		plugin.getServer().getPluginManager().registerEvents(new PlotsInstancesListener(plugin), plugin);
		
		plotCount = plugin.getDataManager().getPlotsCount();
		//plotCount = plugin.getDataManager().getTotalPlotsCount();
		
		//construit les objets Plot chargés depuis la bdd de manière synchrone avec le serveur
		new BukkitRunnable() {
			
			@Override
			public void run() {
				synchronized (asyncPlots) {
					for (AsyncPlot ap : asyncPlots)
						loadedPlots.add(new Plot(ap));
					
					asyncPlots.clear();	
				}
			}
		}.runTaskTimer(plugin, 20, 1);
		
		//libère la RAM des plots non utilisés (càd aucun membre connecté et aucun joueur dessus)
		new BukkitRunnable() {
			
			@Override
			public void run() {
				//pour tous les plots
				synchronized (loadedPlots) {
					for (Plot plot : new ArrayList<Plot>(loadedPlots)) {
						boolean hasMemberOnline = false;
						
						//s'il n'y a aucun joueur sur la parcelle
						if (plot.getPlayers().size() == 0) {
							//si aucun membre n'est en ligne, unload de la parcelle
							for (Player p : Bukkit.getOnlinePlayers()) {
								if (plot.getMembers().getPlayerRank(p) != PlotRank.VISITOR) {
									hasMemberOnline = true;
								}
							}
							if (!hasMemberOnline) {
								plot.unload();
								loadedPlots.remove(plot);
							}
						}	
					}
				}
			}
		}.runTaskTimer(plugin, 20, 20*60);
	}

	public void registerPlot(PlotId newId) {
		if (newId == null)
			return;
		
		for (Plot plot : loadedPlots)
			if (plot.getLoc().equals(newId))
				return;
		
		//si le plot existe mais n'est pas encore chargé
		plugin.getDataManager().loadPlot(newId);
	}	
	
	public Plot createPlot(Player p) {
		Plot plot = new Plot(plugin, AccountProvider.get(p.getUniqueId()).getInformation());
				
		loadedPlots.add(plot);
		return plot;
	}
	
	public List<Plot> getPlots(){
		return Collections.unmodifiableList(loadedPlots);
	}

	public Plot getPlot(Location loc) {
		return getPlot(loc.getBlockX(), loc.getBlockZ());
	}
	
	public Plot getPlot(int x, int z) {
		for (Plot plot : getPlots())
			if (plot.getLoc().isInPlot(x, z))
				return plot;
		return null;
	}

	public List<Plot> getPlotsOf(Player p, boolean onlyOwnedPlots) {
		List<Plot> list = new ArrayList<Plot>();
		
		for (Plot plot : loadedPlots) {
			PlotRank rank = plot.getMembers().getPlayerRank(p);
			if (onlyOwnedPlots) {
				if (rank == PlotRank.OWNER)
					list.add(plot);	
			}else
				if (rank != PlotRank.VISITOR)
					list.add(plot);
		}
		
		return list;
	}
	
	//retourne le plotid de la localisation correspondante
	
	/*
	public PlotLoc getPlotLoc(Location loc) {
		return getPlotLoc(loc.getBlockX(), loc.getBlockZ());
	}
	
	
	public PlotLoc getPlotLoc(int x, int z) {
		
		int xb = Math.floorMod(x, WorldManager.plotSize + WorldManager.roadSize);
		int zb = Math.floorMod(z, WorldManager.plotSize + WorldManager.roadSize);
		
		if (xb < WorldManager.plotSize && zb < WorldManager.plotSize)
			return new PlotLoc(plugin, Math.floorDiv(x, WorldManager.plotSize + WorldManager.roadSize), 
					Math.floorDiv(z, WorldManager.plotSize + WorldManager.roadSize));
		else
			return null;
	}
	*/
	
	
	public void incrementTotalPlotCount() {
		plotCount++;
	}
	
	public int getTotalPlotCount() {
		return plotCount;
	}
	
	public void addAsyncPlot(AsyncPlot plot, PlotId plotId) {
		if (plot != null)
			synchronized (asyncPlots) {
				asyncPlots.add(plot);
			}
	}
	
	public static Integer getPlotIdFromString(String id) {
		try {
			return Integer.valueOf(id, 36);
		}catch(NumberFormatException e) {
			return null;
		}
	}
	
	public static String getPlotIdAsString(int id) {
		return Integer.toString(id, 36).toUpperCase();
	}
}
