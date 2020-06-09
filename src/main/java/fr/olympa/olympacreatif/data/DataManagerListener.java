package fr.olympa.olympacreatif.data;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.olympa.olympacreatif.OlympaCreatifMain;

public class DataManagerListener implements Listener {

	private OlympaCreatifMain plugin;
	
	public DataManagerListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onJoin(OlympaPlayerLoadEvent e) {
		plugin.getDataManager().loadPlayerPlots(e.getOlympaPlayer());
	}
}
