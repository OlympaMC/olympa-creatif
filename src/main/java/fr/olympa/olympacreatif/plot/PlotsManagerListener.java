package fr.olympa.olympacreatif.plot;

import java.util.ArrayList;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.olympacreatif.OlympaCreatifMain;

public class PlotsManagerListener implements Listener {

	private OlympaCreatifMain plugin;
	
	PlotsManagerListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;
	}	
	
	@EventHandler //chargement du plot sur le chunk tout juste chargé 
	public void onChunkLoad(ChunkLoadEvent e) {
		//on teste seulement le point d'origine du chunk pour générer moins de calculs
		plugin.getPlotsManager().registerPlot(plugin.getPlotsManager().getPlotId(e.getChunk().getX()*16, e.getChunk().getZ()*16));
	}
	
	@EventHandler 
	public void onJoinEvent(OlympaPlayerLoadEvent e) {
		plugin.getPlotsManager().loadPlotsFor(e.getOlympaPlayer());
	}
	
	@EventHandler 
	public void onLeftEvent(PlayerQuitEvent e){
		plugin.getPlotsManager().removeLoadedPlayer(e.getPlayer());
	}
}
