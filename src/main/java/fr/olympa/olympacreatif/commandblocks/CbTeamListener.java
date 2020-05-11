package fr.olympa.olympacreatif.commandblocks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;

public class CbTeamListener implements Listener {

	private OlympaCreatifMain plugin;
	
	public CbTeamListener(OlympaCreatifMain plugin) {
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

		String s1 = "";
		String s2 = "";

		if (e.getEntity() instanceof Player)
			s1 = ((Player) e.getEntity()).getDisplayName();
		else
			s2 = e.getEntity().getCustomName();
		
		if (e.getDamager() instanceof Player)
			s1 = ((Player) e.getDamager()).getDisplayName();
		else
			s2 = e.getDamager().getCustomName();

		
		CbTeam t1 = plugin.getCommandBlocksManager().getTeamOfPlayer(p1, s1);
		CbTeam t2 = plugin.getCommandBlocksManager().getTeamOfPlayer(p1, s2);
		
		if (t1 != null && t2 != null && t1.equals(t2) && !t1.hasFriendlyFire())
			e.setCancelled(true);
		
	}

}
