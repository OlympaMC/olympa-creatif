package fr.olympa.olympacreatif.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.gui.MainGui;
import fr.olympa.olympacreatif.plot.Plot;

public class WorldEventsListener implements Listener{

	OlympaCreatifMain plugin;
	Map<String, Long> sneakHistory = new HashMap<String, Long>(); 
	List<Entity> entities = new ArrayList<Entity>(); 
	List<Entity> entitiesToRemove = new ArrayList<Entity>();
	
	public WorldEventsListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		
		//gestion des entités (remove si plot null)
		new BukkitRunnable() {
			
			Thread asyncEntityCheckup = null;
			
			@Override
			public void run() {
				if (asyncEntityCheckup != null)
					asyncEntityCheckup.interrupt();
				
				//supression des entitées marquées
				for (Entity e : entitiesToRemove)
					e.remove();
				
				//ajout des nouvelles entités
				entitiesToRemove.clear();
				entities = new ArrayList<Entity>(plugin.getWorldManager().getWorld().getEntities());
				
				//lancement du thread de test
				asyncEntityCheckup = new Thread(new Runnable() {

					@Override
					public void run() {
						for (Entity e : entities)
							if (plugin.getPlotsManager().getPlot(e.getLocation()) == null)
								entitiesToRemove.add(e);
					}
				});
				asyncEntityCheckup.start();
			}
		}.runTaskTimer(plugin, 0, 40);
	}
	
	@EventHandler //n'autorise que les spawn à partir d'oeufs 
	public void onCreatureSpawn(CreatureSpawnEvent e) {
		if (plugin.getPlotsManager().getPlot(e.getLocation()) == null)
			e.setCancelled(true);
		
		if (e.getSpawnReason() != SpawnReason.EGG && e.getSpawnReason() != SpawnReason.DISPENSE_EGG)
			e.setCancelled(true);
	}

	@EventHandler //cancel lava/water flow en dehors du plot. Cancel aussi toute téléportation d'un oeuf de dragon
	public void onLiquidFlow(BlockFromToEvent e) {
		if (e.getBlock() != null && e.getBlock().getType() == Material.DRAGON_EGG || e.getBlock().getType() == Material.WATER || e.getBlock().getType() == Material.LAVA) {
			e.setCancelled(true);
			return;
		}
		
		if (plugin.getPlotsManager().getPlot(e.getToBlock().getLocation())  == null)
			e.setCancelled(true);
	}
	
	@EventHandler //cancel rétractation piston si un bloc affecté se trouve sur une route
	public void onPistonRetractEvent(BlockPistonRetractEvent e) {
		for (Block block : e.getBlocks())
			if (plugin.getPlotsManager().getPlot(block.getLocation()) == null)
				e.setCancelled(true);
	}
	
	@EventHandler //cancel poussée piston si un bloc affecté se trouve sur une route
	public void onPistonPushEvent(BlockPistonExtendEvent e) {
		for (Block block : e.getBlocks())
			if (plugin.getPlotsManager().getPlot(block.getLocation()) == null)
				e.setCancelled(true);
	}
	
	@EventHandler //cancel explosion TNT
	public void onTntExplodeEvent(ExplosionPrimeEvent e) {
		e.setCancelled(true);
	}

	@EventHandler //cancel pose block si route ou plot non défini
	public void onPlaceBlockEvent(BlockPlaceEvent e) {
		if (plugin.getPlotsManager().getPlot(e.getBlockPlaced().getLocation()) == null) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.PLOT_CANT_BUILD.getValue());
		}
	}
	
	@EventHandler //cancel pose block si route & annule tout loot d'item possible
	public void onBreakBlockEvent(BlockBreakEvent e) {
		if (plugin.getPlotsManager().getPlot(e.getBlock().getLocation()) == null) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.PLOT_CANT_BUILD.getValue());
		}
		
		e.setDropItems(false);
	}
	
	//@EventHandler //détruit tous les items 5s après leur spawn
	public void onSpawnItem(final ItemSpawnEvent e) {
		
		new BukkitRunnable() {
			
			public void run() {
				if (!e.getEntity().isDead())
					e.getEntity().remove();
			}
		}.runTaskLater(plugin, 100);
	}
	
	//Gestion des items restreints
	@EventHandler //test dans inventaires
	public void onProhibitedItemInventory(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player))
			return;
		
		OlympaPlayer p = AccountProvider.get(e.getWhoClicked().getUniqueId());

		if (e.getCurrentItem() != null && plugin.getWorldManager().getRestrictedItems().keySet().contains(e.getCurrentItem().getType()))
			if (!p.hasPermission(plugin.getWorldManager().getRestrictedItems().get(e.getCurrentItem().getType()))) {
				e.setCancelled(true);
				p.getPlayer().sendMessage(Message.INSUFFICENT_PERMISSION_NEW.getValue().replace("%kit%", plugin.getWorldManager().getRestrictedItems().get(e.getCurrentItem().getType()).toString().toLowerCase().replace("_", " ")));
			}
		
		if (e.getCursor() != null && plugin.getWorldManager().getRestrictedItems().keySet().contains(e.getCursor().getType()))
			if (!p.hasPermission(plugin.getWorldManager().getRestrictedItems().get(e.getCursor().getType()))) {
				e.setCancelled(true);
				p.getPlayer().sendMessage(Message.INSUFFICENT_PERMISSION_NEW.getValue().replace("%kit%", plugin.getWorldManager().getRestrictedItems().get(e.getCursor().getType()).toString().toLowerCase().replace("_", " ")));
			}
	}
	
	@EventHandler //cancel pickup item restreint
	public void onPickup(EntityPickupItemEvent e) {
		if (e.getEntityType() != EntityType.PLAYER)
			return;

		OlympaPlayer p = AccountProvider.get(e.getEntity().getUniqueId());

		if (e.getItem().getItemStack() != null && plugin.getWorldManager().getRestrictedItems().keySet().contains(e.getItem().getItemStack().getType()))
			if (!p.hasPermission(plugin.getWorldManager().getRestrictedItems().get(e.getItem().getItemStack().getType()))) {
				e.setCancelled(true);
				p.getPlayer().sendMessage(Message.INSUFFICENT_PERMISSION_NEW.getValue().replace("%kit%", plugin.getWorldManager().getRestrictedItems().get(e.getItem().getItemStack().getType()).toString().toLowerCase().replace("_", " ")));
			}
	}
	
	@EventHandler //cancel interact si objet restreint
	public void onInterract(PlayerInteractEvent e) {
		OlympaPlayer p = AccountProvider.get(e.getPlayer().getUniqueId());

		if (e.getItem() != null && plugin.getWorldManager().getRestrictedItems().keySet().contains(e.getItem().getType()))
			if (!p.hasPermission(plugin.getWorldManager().getRestrictedItems().get(e.getItem().getType()))) {
				e.setCancelled(true);
				p.getPlayer().sendMessage(Message.INSUFFICENT_PERMISSION_NEW.getValue().replace("%kit%", plugin.getWorldManager().getRestrictedItems().get(e.getItem().getType()).toString().toLowerCase().replace("_", " ")));
			}	
	}
	
	
	
	@EventHandler //cancel potions persistantes
	public void onLingeringPotion(LingeringPotionSplashEvent e) {
		e.setCancelled(true);
	}
	
	@EventHandler //ouvre le menu si joueur a sneak deux fois rapidement (délai : 0.5s)
	public void onOpenMenu(PlayerToggleSneakEvent e) {
		if (e.isSneaking())
			if (sneakHistory.keySet().contains(e.getPlayer().getName()))
				if (sneakHistory.get(e.getPlayer().getName()) + 200 > System.currentTimeMillis())
					new MainGui(plugin, e.getPlayer()).create(e.getPlayer());
				else
					sneakHistory.put(e.getPlayer().getName(), System.currentTimeMillis());
			else
				sneakHistory.put(e.getPlayer().getName(), System.currentTimeMillis());
	}
	
	@EventHandler //clear l'historique de sneak de ce joueur
	public void onPlayerQuit(PlayerQuitEvent e) {
		sneakHistory.remove(e.getPlayer().getName());
		e.getPlayer().teleport(plugin.getWorldManager().getWorld().getSpawnLocation());
	}
	
	@EventHandler //remplir le dispenser au fur et à mesure qu'il se vide (pour toujours garder les mêmes objets à l'intérieur)
	public void onDispense(BlockDispenseEvent e) {
		if (e.getItem() == null) 
			return;
		
		if (e.getBlock().getState() instanceof Dispenser) {
			((Dispenser) e.getBlock().getState()).getInventory().addItem(e.getItem());
		}
		if (e.getBlock().getState() instanceof Dropper) {
			((Dropper) e.getBlock().getState()).getInventory().addItem(e.getItem());
		}
		
	}
	
	@EventHandler //chat de plot
	public void inChat(AsyncPlayerChatEvent e) {
		if (e.isCancelled())
			return;
		
		Plot plot = plugin.getPlotsManager().getPlot(e.getPlayer().getLocation());
		
		if (e.getMessage().startsWith("@") || plot == null)
			e.setMessage(e.getMessage().replaceFirst("@", ""));
		else {
			e.getRecipients().clear();
			e.setFormat("§7[Plot] §r" + e.getPlayer().getDisplayName() + " : " + e.getMessage());
			for (Player p : plot.getPlayers())
				e.getRecipients().add(p);
		}
	}
}
