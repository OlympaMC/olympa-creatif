package fr.olympa.olympacreatif.data;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.world.WorldManager;
import redis.clients.jedis.JedisPubSub;

public class RedisListener extends JedisPubSub {

	private OlympaCreatifMain plugin;
	
	public RedisListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		plugin.getLogger().log(Level.INFO, "Redis listener chargé.");
	}
	
	@Override
	public void onMessage(String channel, String message) {
		super.onMessage(channel, message);
		String[] info = message.split(";");
		
		//String ip = info[0];
		//int port = Integer.valueOf(info[1]).intValue();
		String serverName = info[2];
		
		if (plugin.getDataManager().getServerIndex() != -1)
			return;
		
		int serverIndex = Integer.valueOf(serverName.substring(serverName.length() - 1));
		plugin.getLogger().info("§aINDEX DU SERVEUR CREATIF : " + serverIndex);
		
		plugin.getDataManager().updateWithServerIndex(serverIndex);
		
		int plotsCount = plugin.getDataManager().getPlotsCount();
		if (plotsCount != -1)
			plugin.getLogger().log(Level.INFO, "Nombre de parcelles détectées : " + plotsCount);
		else
			plugin.getLogger().log(Level.SEVERE, "§4ATTENTION problème dans la table creatif_plotsdata : nombre d'entrées différent de l'indice du plot maximal !!");
			
		plugin.getPlotsManager().setTotalPlotCount(plotsCount);
		new BukkitRunnable() {
			@Override
			public void run() {
				plugin.getWorldManager().defineWorldParams();	
			}
		}.runTask(plugin);

		//create default scoreboard
		plugin.createScoreboard(serverIndex);
		
		plugin.getLogger().log(Level.INFO, "Taille parcelles définie à " + OCparam.PLOT_SIZE.get() + "*" + OCparam.PLOT_SIZE.get());
		plugin.getServer().getPluginManager().callEvent(new PlotSizeRecievedEvent(OCparam.PLOT_SIZE.get(), WorldManager.roadSize, WorldManager.worldLevel));
	}
	
	public static class PlotSizeRecievedEvent extends Event {
	    private static final HandlerList HANDLERS_LIST = new HandlerList();

	    private int plotSize;
	    private int roadSize;
	    private int worldLevel;
	    
	    private PlotSizeRecievedEvent(int plotSize, int roadSize, int worldLevel) {
	    	super(true);
	    	this.plotSize = plotSize;
	    	this.roadSize = roadSize;
	    	this.worldLevel = worldLevel;
	    }
	    
	    public int getPlotSize() {
	    	return plotSize;
	    }
	    
	    public int getRoadSize() {
	    	return roadSize;
	    }
	    
	    public int getWorldLevel() {
	    	return worldLevel;
	    }
	    
	    @Override
	    public HandlerList getHandlers() {
	        return HANDLERS_LIST;
	    }

	    public static HandlerList getHandlerList() {
	        return HANDLERS_LIST;
	    }
	}
	
}
