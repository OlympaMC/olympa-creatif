package fr.olympa.olympacreatif.plot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;

public class PlotsManager {

	private OlympaCreatifMain plugin;
	private Map<PlotId, Plot> loadedPlots = new HashMap<PlotId, Plot>();
	private List<PlotId> emptyPlots = new ArrayList<PlotId>();
	private List<AsyncPlot> asyncPlots = new ArrayList<AsyncPlot>();
	
	private List<Player> loadedPlayers = new ArrayList<Player>();

	private Map<Player, Integer> playerBonusPlots = new HashMap<Player, Integer>();
	private Map<Player, Integer> playerMoney = new HashMap<Player, Integer>();
	
	private int plotCount; 
	
	private List<Object> adminPlayers = new ArrayList<Object>();
	
	public PlotsManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;

		plugin.getServer().getPluginManager().registerEvents(new PlotsManagerListener(plugin), plugin);
		plugin.getServer().getPluginManager().registerEvents(new PlotsInstancesListener(plugin), plugin);
		
		plotCount = plugin.getDataManager().getTotalPlotsCount();
		
		//construit les objets Plot chargés depuis la bdd de manière synchrone avec le serveur
		new BukkitRunnable() {
			
			@Override
			public void run() {
				synchronized (asyncPlots) {
					for (AsyncPlot ap : asyncPlots) {
						Plot plot = new Plot(ap);
						loadedPlots.put(plot.getId(), plot);
						emptyPlots.remove(plot.getId());
					}
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
					for (Entry<PlotId, Plot> e : loadedPlots.entrySet()) {
						boolean hasMemberOnline = false;
						
						//s'il n'y a aucun joueur sur la parcelle
						if (e.getValue().getPlayers().size() == 0) {
							//si aucun membre n'est en ligne, unload de la parcelle
							for (Player p : Bukkit.getOnlinePlayers()) {
								if (e.getValue().getMembers().getPlayerRank(p) != PlotRank.VISITOR) {
									hasMemberOnline = true;
								}
							}
							if (!hasMemberOnline) {
								//e.getValue().unregisterListener();
								loadedPlots.remove(e.getKey());
								plugin.getCommandBlocksManager().unregisterPlot(e.getValue());
							}
						}	
					}
				}
			}
		}.runTaskTimer(plugin, 20, 20*60);
	}

	public void registerPlot(PlotId plotId) {
		if (plotId == null)
			return;
		
		//si le plot n'a pas encore été testé et déterminé non-existant et s'il n'est pas déjà chargé
		if (!emptyPlots.contains(plotId) && !loadedPlots.keySet().contains(plotId))
			plugin.getDataManager().loadPlot(plotId);
	}
	
	public Plot createPlot(Player p) {
		Plot plot = new Plot(plugin, AccountProvider.get(p.getUniqueId()).getInformation());
		
		emptyPlots.remove(plot.getId());		
		loadedPlots.put(plot.getId(), plot);
		return plot;
	}
	
	public Collection<Plot> getPlots(){
		return loadedPlots.values();
	}

	public Plot getPlot(Location loc) {
		return getPlot(loc.getBlockX(), loc.getBlockZ());
	}
	
	public Plot getPlot(int x, int z) {
		for (Plot plot : getPlots())
			if (plot.getId().isInPlot(x, z))
				return plot;
		return null;
	}
	
	//retourne le plotid de la localisation correspondante
	public PlotId getPlotId(Location loc) {
		return getPlotId(loc.getBlockX(), loc.getBlockZ());
	}
	
	public PlotId getPlotId(int x, int z) {
		int xb = Math.floorMod(x, Integer.valueOf(Message.PARAM_PLOT_X_SIZE.getValue()) + Integer.valueOf(Message.PARAM_ROAD_SIZE.getValue()));
		int zb = Math.floorMod(z, Integer.valueOf(Message.PARAM_PLOT_Z_SIZE.getValue()) + Integer.valueOf(Message.PARAM_ROAD_SIZE.getValue()));
		
		if (xb <  + Integer.valueOf(Message.PARAM_PLOT_X_SIZE.getValue()) && zb <  + Integer.valueOf(Message.PARAM_PLOT_Z_SIZE.getValue()))
			return new PlotId(plugin, xb, zb);
		

		int xId = x / (Integer.valueOf(Message.PARAM_PLOT_X_SIZE.getValue()) + Integer.valueOf(Message.PARAM_ROAD_SIZE.getValue()));
		int zId = x / (Integer.valueOf(Message.PARAM_PLOT_Z_SIZE.getValue()) + Integer.valueOf(Message.PARAM_ROAD_SIZE.getValue()));
		
		if (xId < Math.floorMod(x, Integer.valueOf(Message.PARAM_PLOT_X_SIZE.getValue())) && zId < Math.floorMod(z, Integer.valueOf(Message.PARAM_PLOT_Z_SIZE.getValue())))
			return new PlotId(plugin, xId, zId);

		return null;
	}
	
	public void incrementTotalPlotCount() {
		plotCount++;
	}
	
	public int getTotalPlotCount() {
		return plotCount;
	}
	
	public void addAsyncPlot(AsyncPlot plot, PlotId id) {
		if (plot != null)
			synchronized (asyncPlots) {
				asyncPlots.add(plot);
			}
		else
			emptyPlots.add(id);
	}
}
