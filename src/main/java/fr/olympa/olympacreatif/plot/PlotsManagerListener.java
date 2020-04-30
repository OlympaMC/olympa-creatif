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
	
	/*
	@EventHandler //chargement du plot sur le chunk tout juste chargé 
	public void onChunkLoad(ChunkLoadEvent e) {
		//on teste seulement le point d'origine du chunk pour générer moins de calculs
		plugin.getPlotsManager().registerPlot(plugin.getPlotsManager().getPlotId(e.getChunk().getX()*16, e.getChunk().getZ()*16));
	}
	*/
	
	@EventHandler //charge tous les plots du joueur
	public void onJoinEvent(PlayerJoinEvent e) {
		plugin.getPlotsManager().loadPlotsFor(AccountProvider.get(e.getPlayer().getUniqueId()));
	}
	
	@EventHandler //retire le joueur de la liste des joueurs chargés
	public void onLeftEvent(PlayerQuitEvent e){
		plugin.getPlotsManager().removeLoadedPlayer(e.getPlayer());
		plugin.getPlotsManager().removeAdminPlayer(AccountProvider.get(e.getPlayer().getUniqueId()));
	}
	
	@EventHandler //charge les plots non encore chargés sur lesquels les joueurs se rendent
	public void onMoveEvent(PlayerMoveEvent e) {
		//si pas de mouvment de block suivant X ou Z, return
		if (e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockZ() == e.getTo().getBlockZ())
			return;
		
		//si l'ancien/nouveau plot sont égaux, return, sinon chargement du plot
		PlotId oldId = plugin.getPlotsManager().getPlotId(e.getFrom());
		PlotId newId = plugin.getPlotsManager().getPlotId(e.getTo());
		
		if (oldId == null)
			if (newId == null)
				return;
			else
				plugin.getPlotsManager().registerPlot(newId);
		else
			if (!oldId.equals(newId))
				plugin.getPlotsManager().registerPlot(newId);
	}
}
