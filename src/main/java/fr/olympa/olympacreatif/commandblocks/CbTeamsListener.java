package fr.olympa.olympacreatif.commandblocks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;

public class CbTeamsListener implements Listener {

	private OlympaCreatifMain plugin;
	
	public CbTeamsListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onDamage(EntityDamageByEntityEvent e) {
		if (e.isCancelled())
			return;

		Plot p1 = plugin.getPlotsManager().getPlot(e.getEntity().getLocation());
		Plot p2 = plugin.getPlotsManager().getPlot(e.getDamager().getLocation());
		
		if (p1 == null || p2 == null || !p1.equals(p2))
			return;
		
		CbTeam t1 = plugin.getCommandBlocksManager().getTeamOf(p1, e.getEntity());
		CbTeam t2 = plugin.getCommandBlocksManager().getTeamOf(p1, e.getDamager());
		
		if (t1 != null && t2 != null && t1.equals(t2) && !t1.hasFriendlyFire())
			e.setCancelled(true);
		
	}

}
