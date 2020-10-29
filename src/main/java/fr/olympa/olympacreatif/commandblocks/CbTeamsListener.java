package fr.olympa.olympacreatif.commandblocks;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotId;

public class CbTeamsListener implements Listener {

	private OlympaCreatifMain plugin;
	
	public CbTeamsListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		
		//update nametags des joueurs 
		OlympaCore.getInstance().getNameTagApi().addNametagHandler(EventPriority.LOW, (nametag, player, to) -> {
			//si les deux joueurs sont sur le même plot
			if (PlotId.fromLoc(plugin, player.getPlayer().getLocation()).equals(PlotId.fromLoc(plugin, to.getPlayer().getLocation()))) {
				//récupération du plot concerné
				Plot plot = plugin.getPlotsManager().getPlot(player.getPlayer().getLocation());
				
				if (plot != null) {
					CbTeam team = plot.getCbData().getTeamOf(player.getPlayer());
					
					//ajout du nom de la team au joueur concerné
					if (team != null && team.getName() != "")
						nametag.appendSuffix("§7(§r" + team.getName() + "§r§7)");
				}
			}
		});
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onDamage(EntityDamageByEntityEvent e) {
		if (e.isCancelled())
			return;

		Plot p1 = plugin.getPlotsManager().getPlot(e.getEntity().getLocation());
		Plot p2 = plugin.getPlotsManager().getPlot(e.getDamager().getLocation());
		
		if (p1 == null || !p1.equals(p2))
			return;
		
		CbTeam t1 = p1.getCbData().getTeamOf(e.getEntity());
		CbTeam t2 = p1.getCbData().getTeamOf( e.getDamager());
		
		if (t1 != null && t1.equals(t2) && !t1.hasFriendlyFire())
			e.setCancelled(true);
		
	}

}
