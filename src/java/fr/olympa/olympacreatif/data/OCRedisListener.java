package fr.olympa.olympacreatif.data;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import redis.clients.jedis.JedisPubSub;

public class OCRedisListener extends JedisPubSub {

	private OlympaCreatifMain plugin;
	
	public OCRedisListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		plugin.getLogger().log(Level.INFO, "Redis listener chargé.");
	}
	
	@Override
	public void onMessage(String channel, String message) {
		super.onMessage(channel, message);
		String[] info = message.split(";");
		
		if (info.length < 3)
			return;
		
		String serverName = info[2];
		
		if (plugin.getDataManager().getServerIndex() != -1 || !serverName.startsWith("creatif"))
			return;
		
		int serverIndex = Integer.valueOf(serverName.substring(serverName.length() - 1));
		
		plugin.getDataManager().updateWithServerIndex(serverIndex);
		
		int plotsCount = plugin.getDataManager().getPlotsCount();
		if (plotsCount == -1) {
			plugin.getLogger().log(Level.SEVERE, "§4ATTENTION problème dans la table creatif_plotsdata : nombre d'entrées différent de l'indice du plot maximal !! Arrêt du serveur.");
			Bukkit.getServer().shutdown();
			return;
		}
			
		plugin.getPlotsManager().setTotalPlotCount(plotsCount);
		
		
		new BukkitRunnable() {
			@Override
			public void run() {
				plugin.getWorldManager().defineWorldParams();	
				plugin.getWorldManager().loadCustomWorldGenerator();
				plugin.getPlotsManager().loadHelpHolos();
			}
		}.runTask(plugin);

		//create default scoreboard
		plugin.createScoreboard(serverIndex);
		
		plugin.getLogger().info("§aINDEX DU SERVEUR CREATIF : " + serverIndex + "§7 - Nombre de parcelles : " + plotsCount + " - Taille parcelles : " + OCparam.PLOT_SIZE.get() + "*" + OCparam.PLOT_SIZE.get());
		
		//plugin.getLogger().log(Level.INFO, "Taille parcelles définie à " + OCparam.PLOT_SIZE.get() + "*" + OCparam.PLOT_SIZE.get());
		//plugin.getServer().getPluginManager().callEvent(new PlotSizeRecievedEvent(OCparam.PLOT_SIZE.get(), WorldManager.roadSize, WorldManager.worldLevel));
	}
}
