package fr.olympa.olympacreatif.plot;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import fr.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;

public class PlotsManagerListener implements Listener {

	private OlympaCreatifMain plugin;
	
	PlotsManagerListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler //charge les plots non encore chargés sur lesquels les joueurs se rendent
	public void onMoveEvent(PlayerMoveEvent e) {
		//si pas de mouvment de block suivant X ou Z, return
		if (e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockZ() == e.getTo().getBlockZ())
			return;
		
		//si l'ancien/nouveau plot sont égaux, return, sinon chargement du plot
		UnaffectedPlotId oldId = plugin.getPlotsManager().getPlotLoc(e.getFrom());
		UnaffectedPlotId newId = plugin.getPlotsManager().getPlotLoc(e.getTo());
		
		if (newId != null && !newId.equals(oldId))
			plugin.getPlotsManager().registerPlot(newId);
	}
}
