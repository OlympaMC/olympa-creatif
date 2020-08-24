package fr.olympa.olympacreatif.utils;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;

import fr.olympa.olympacreatif.OlympaCreatifMain;

public class EntityRemoveListener implements Listener {

	private OlympaCreatifMain plugin;
	
	public EntityRemoveListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onHangingDestroy(HangingBreakEvent e) {
		if (!e.isCancelled())
			Bukkit.getPluginManager().callEvent(new EntityRemoveEvent(plugin, e.getEntity()));
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onItemDespawn(ItemDespawnEvent e) {
		if (!e.isCancelled())
			Bukkit.getPluginManager().callEvent(new EntityRemoveEvent(plugin, e.getEntity()));
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onItemPickup(EntityPickupItemEvent e) {
		if (!e.isCancelled())
			Bukkit.getPluginManager().callEvent(new EntityRemoveEvent(plugin, e.getItem()));
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onArrowPickup(PlayerPickupArrowEvent e) {
		if (!e.isCancelled())
			Bukkit.getPluginManager().callEvent(new EntityRemoveEvent(plugin, e.getArrow()));
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onEntityDeath(EntityDeathEvent e) {
		if (!e.isCancelled())
			Bukkit.getPluginManager().callEvent(new EntityRemoveEvent(plugin, e.getEntity()));
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onEntityDeath(EntityCombustEvent e) {
		if (!e.isCancelled())
			plugin.getTask().runTaskLater(() -> Bukkit.getPluginManager().callEvent(new EntityRemoveEvent(plugin, e.getEntity())), 30);
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent e) {
		plugin.getTask().runTaskLater(() -> Bukkit.getPluginManager().callEvent(new EntityRemoveEvent(plugin, e.getEntity())), 60);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onItemMerge(ItemMergeEvent e) {
		if (!e.isCancelled())
			Bukkit.getPluginManager().callEvent(new EntityRemoveEvent(plugin, e.getEntity()));
	}
}






