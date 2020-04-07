package fr.olympa.olympacreatif.world;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.datas.Message;

public class WorldEventsListener implements Listener{

	OlympaCreatifMain plugin;
	
	public WorldEventsListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;		
	}
	
	@EventHandler //cancel spawn créatures, sauf si spawn par un plugin 
	public void onCreatureSpawn(CreatureSpawnEvent e) {
		if (!(e.getEntityType() == EntityType.PLAYER) && !(e.getSpawnReason() == SpawnReason.CUSTOM))
			return;
		
		e.setCancelled(true);
	}

	@EventHandler //cancel lava/water flow en dehors du plot. Cancel aussi toute téléportation d'un oeuf de dragon
	public void onLiquidFlow(BlockFromToEvent e) {
		if (e.getBlock() != null && e.getBlock().getType() == Material.DRAGON_EGG)
			e.setCancelled(true);
		
		if (!plugin.getPlotsManager().getPlot(e.getBlock().getLocation()).equals(plugin.getPlotsManager().getPlot(e.getToBlock().getLocation())))
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
		if (plugin.getPlotsManager().getPlot(e.getBlockPlaced().getLocation()) == null)
			e.setCancelled(true);
	}
	
	@EventHandler //cancel pose block si route & annule tout loot d'item possible
	public void onBreakBlockEvent(BlockBreakEvent e) {
		if (plugin.getPlotsManager().getPlot(e.getBlock().getLocation()) == null)
			e.setCancelled(true);
		
		e.setDropItems(false);
	}
	
	@EventHandler //détruit tous les items 5s après leur spawn
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
}
