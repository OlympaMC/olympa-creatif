package fr.olympa.olympacreatif.world;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.gui.MainGui;
import fr.olympa.olympacreatif.plot.Plot;

public class WorldEventsListener implements Listener{

	OlympaCreatifMain plugin;
	Map<String, Long> sneakHistory = new HashMap<String, Long>(); 
	
	public WorldEventsListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;		
	}
	
	@EventHandler //cancel spawn créatures, sauf si spawn par un plugin 
	public void onCreatureSpawn(CreatureSpawnEvent e) {
		if (e.getEntityType() == EntityType.PLAYER)
			return;
		if (plugin.getPlotsManager().getPlot(e.getLocation()) != null)
			return;
		if (e.getSpawnReason() == SpawnReason.CUSTOM)
			return;
		
		e.setCancelled(true);
	}

	@EventHandler //cancel lava/water flow en dehors du plot. Cancel aussi toute téléportation d'un oeuf de dragon
	public void onLiquidFlow(BlockFromToEvent e) {
		if (e.getBlock() != null && e.getBlock().getType() == Material.DRAGON_EGG)
			e.setCancelled(true);
		
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
	
	@EventHandler //cancel pose d'un bloc s'il est interdit
	public void onPlaceProhibitedBlock(BlockPlaceEvent e) {
		if (plugin.getWorldManager().getProhibitedBlocks().contains(e.getBlockPlaced().getType())) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.PROHIBITED_BLOCK_PLACED.getValue());
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
				if (sneakHistory.get(e.getPlayer().getName()) + 500 > System.currentTimeMillis())
					new MainGui(plugin, e.getPlayer()).create(e.getPlayer());
				else
					sneakHistory.put(e.getPlayer().getName(), System.currentTimeMillis());
			else
				sneakHistory.put(e.getPlayer().getName(), System.currentTimeMillis());
	}
	
	@EventHandler //clear l'historique de sneak de ce joueur
	public void onPlayerQuit(PlayerQuitEvent e) {
		sneakHistory.remove(e.getPlayer().getName());
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
			e.getMessage().replaceFirst("@", "");
		else {
			e.getRecipients().clear();
			e.setFormat("§7[Plot] §r" + e.getPlayer().getDisplayName() + " : ");
			for (Player p : plot.getPlayers())
				e.getRecipients().add(p);
		}
	}
}
