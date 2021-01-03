package fr.olympa.olympacreatif.data;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

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
		plugin.getDataManager().setServerIndex(serverIndex);
		plugin.getLogger().log(Level.INFO, "§aINDEX DU SERVEUR CREATIF : " + serverIndex);
		
		WorldManager.plotSize = Integer.valueOf(Message.valueOf("PARAM_PLOT_SIZE_" + serverName.toUpperCase()).getValue());
		
		int plotsCount = plugin.getDataManager().getPlotsCount();
		if (plotsCount != -1)
			plugin.getLogger().log(Level.INFO, "Intégrité de la table creatif_plotsdata validée : MAX(plot_id) = COUNT(*). Nombre de parcelles détectées : " + plotsCount);
		else
			plugin.getLogger().log(Level.SEVERE, "§4ATTENTION problème dans la table creatif_plotsdata : nombre d'entrées différent de l'indice du plot maximal !!");
			
		plugin.getPlotsManager().setTotalPlotCount(plotsCount);
		plugin.getWorldManager().updateWorldBorder();

		plugin.getLogger().log(Level.INFO, "Taille parcelles définie à " + WorldManager.plotSize + "*" + WorldManager.plotSize);
		plugin.getServer().getPluginManager().callEvent(new PlotSizeRecievedEvent(WorldManager.plotSize));
	}
	
	public static class PlotSizeRecievedEvent extends Event {
	    private static final HandlerList HANDLERS_LIST = new HandlerList();

	    private int plotSize;
	    
	    private PlotSizeRecievedEvent(int plotSize) {
	    	super(true);
	    	this.plotSize = plotSize;
	    }
	    
	    public int getServerPlotSize() {
	    	return plotSize;
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
