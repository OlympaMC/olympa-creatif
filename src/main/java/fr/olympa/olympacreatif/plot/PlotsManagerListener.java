package fr.olympa.olympacreatif.plot;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import fr.olympa.olympacreatif.OlympaCreatifMain;

public class PlotsManagerListener implements Listener {

	private OlympaCreatifMain plugin;
	
	PlotsManagerListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler //charge les plots non encore chargés sur lesquels les joueurs se rendent
	public void onMoveEvent(PlayerMoveEvent e) {
		//si pas de mouvment de block suivant X ou Z, return
		if (e.getFrom().getChunk() == e.getTo().getChunk())
			return;
		
		tryToRegisterPlot(e.getFrom(), e.getTo());
	}
	
	@EventHandler 
	public void onTpEvent(PlayerTeleportEvent e){
		tryToRegisterPlot(e.getFrom(), e.getTo());
	}
	
	private void tryToRegisterPlot(Location oldLoc, Location newLoc) {
		
		//si l'ancien/nouveau plot sont égaux, return, sinon chargement du plot
		PlotId oldId = PlotId.fromLoc(plugin, oldLoc);
		PlotId newId = PlotId.fromLoc(plugin, newLoc);
		
		//Bukkit.broadcastMessage("TRY TO LOAD PLOT "  + newId);
		
		if (newId != null && !newId.equals(oldId))
			plugin.getPlotsManager().loadExistingPlot(newId);
	}
}
