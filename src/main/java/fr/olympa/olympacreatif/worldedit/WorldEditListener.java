package fr.olympa.olympacreatif.worldedit;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;

public class WorldEditListener implements Listener{

	private OlympaCreatifMain plugin;
	
	public WorldEditListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onInterract(PlayerInteractEvent e) {
		if (e.getPlayer().getInventory().getItemInMainHand() == null || e.getClickedBlock() == null || e.getClickedBlock().getType() == Material.AIR)
			return;

		//définition des pos
		if (e.getAction() == Action.LEFT_CLICK_BLOCK)
			if (plugin.getWorldEditManager().getPlayerInstance(e.getPlayer()).setPos1(e.getClickedBlock().getLocation()))
				e.getPlayer().sendMessage(Message.WE_POS_SET.getValue().replace("%pos%", "1"));
			else
				e.getPlayer().sendMessage(Message.WE_INSUFFICIENT_PLOT_PERMISSION.getValue());
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK)
			if (plugin.getWorldEditManager().getPlayerInstance(e.getPlayer()).setPos2(e.getClickedBlock().getLocation()))
				e.getPlayer().sendMessage(Message.WE_POS_SET.getValue().replace("%pos%", "2"));
			else
				e.getPlayer().sendMessage(Message.WE_INSUFFICIENT_PLOT_PERMISSION.getValue());
	}
}
