package fr.olympa.olympacreatif.worldedit_legacy;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.worldedit_legacy.WorldEditManager.WorldEditError;

public class WorldEditListener_legacy implements Listener{

	private OlympaCreatifMain plugin;
	
	public WorldEditListener_legacy(OlympaCreatifMain plugin) {
		this.plugin = plugin;
	}
	
	/*
	@EventHandler //sélection de la zone
	public void onInterract(PlayerInteractEvent e) {
		if (e.getPlayer().getInventory().getItemInMainHand() == null || e.getClickedBlock() == null || e.getClickedBlock().getType() == Material.AIR)
			return;

		if (e.getPlayer().getInventory().getItemInMainHand().getType() != Material.WOODEN_AXE || e.getHand() != EquipmentSlot.HAND)
			return;
		
		e.setCancelled(true);
		
		//définition des pos
		if (e.getAction() == Action.LEFT_CLICK_BLOCK)
			if (plugin.getWorldEditManager().getPlayerInstance(e.getPlayer()).setPos1(e.getClickedBlock().getLocation()) == WorldEditError.NO_ERROR)
				e.getPlayer().sendMessage(Message.WE_POS_SET.getValue().replace("%pos%", "1"));
			else
				e.getPlayer().sendMessage(Message.WE_INSUFFICIENT_PLOT_PERMISSION.getValue());
		else if (e.getAction() == Action.RIGHT_CLICK_BLOCK)
			if (plugin.getWorldEditManager().getPlayerInstance(e.getPlayer()).setPos2(e.getClickedBlock().getLocation()) == WorldEditError.NO_ERROR)
				e.getPlayer().sendMessage(Message.WE_POS_SET.getValue().replace("%pos%", "2"));
			else
				e.getPlayer().sendMessage(Message.WE_INSUFFICIENT_PLOT_PERMISSION.getValue());
	}
	
	
	@EventHandler //ajout de l'instance pour chaque joueur qui rejoint le serveur
	public void onJoin(PlayerJoinEvent e) {
		plugin.getWorldEditManager().addPlayer(e.getPlayer());
	}
	
	@EventHandler //supresssion de l'instance pour chaque joueur qui quitte le serveur
	public void onQuit(PlayerQuitEvent e) {
		plugin.getWorldEditManager().removePlayer(e.getPlayer());
	}
	*/
}
