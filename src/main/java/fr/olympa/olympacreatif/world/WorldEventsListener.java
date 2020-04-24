package fr.olympa.olympacreatif.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.gui.MainGui;
import fr.olympa.olympacreatif.plot.Plot;

public class WorldEventsListener implements Listener{

	OlympaCreatifMain plugin;
	Map<String, Long> sneakHistory = new HashMap<String, Long>(); 
	List<Entity> entities = new ArrayList<Entity>(); 
	List<Entity> entitiesToRemove = new ArrayList<Entity>();
	
	public WorldEventsListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		
		//gestion des entités (remove si plot null ou si nb par plot > 100)
		new BukkitRunnable() {
			
			Thread asyncEntityCheckup = null;
			int maxEntitiesPerPlot = Integer.valueOf(Message.PARAM_MAX_ENTITIES_PER_PLOT.getValue());
			
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

					Map<Plot, Map<EntityType, Integer>> entitiesPerPlot = new HashMap<Plot, Map<EntityType,Integer>>(); 
					
					@Override
					public void run() {
						for (Entity entity : entities) {
							Plot plot = plugin.getPlotsManager().getPlot(entity.getLocation());
							
							if (entity.getType() == EntityType.PLAYER)
								continue;
							
							//supprime l'entité si en dehors d'un plot ou si le nombre d'entités dans le plot dépasse la valeur en paramètre
							if (plot == null)
								entitiesToRemove.add(entity);
							else
								if (entitiesPerPlot.keySet().contains(plot))
									if (entitiesPerPlot.get(plot).containsKey(entity.getType())) {
										entitiesPerPlot.get(plot).put(entity.getType(), entitiesPerPlot.get(plot).get(entity.getType()) + 1);
										if (entitiesPerPlot.get(plot).get(entity.getType()) > maxEntitiesPerPlot)
											entitiesToRemove.add(entity);
									}else
										entitiesPerPlot.get(plot).put(entity.getType(), 1);
								else
									entitiesPerPlot.put(plot, new HashMap<EntityType, Integer>());
						}
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
		
		
		if (e.getSpawnReason() != SpawnReason.EGG && e.getSpawnReason() != SpawnReason.DISPENSE_EGG && 
				e.getSpawnReason() != SpawnReason.CUSTOM && e.getSpawnReason() != SpawnReason.ENDER_PEARL && 
				e.getSpawnReason() != SpawnReason.SPAWNER && e.getSpawnReason() != SpawnReason.SPAWNER_EGG)
			e.setCancelled(true);
	}

	@EventHandler //cancel lava/water flow en dehors du plot. Cancel aussi toute téléportation d'un oeuf de dragon
	public void onLiquidFlow(BlockFromToEvent e) {
		if (e.getBlock() != null && (e.getBlock().getType() == Material.DRAGON_EGG || e.getBlock().getType() == Material.WATER || e.getBlock().getType() == Material.LAVA)) {
			e.setCancelled(true);
			return;
		}
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
	public void onEntityExplodeEvent(EntityExplodeEvent e) {
		e.blockList().clear();
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
	
	//Gestion renommage en couleur des items
	@EventHandler
	public void onItemRename(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player))
			return;
		
		if (e.getClickedInventory() == null)
			return;
		
		if (e.getCurrentItem() == null)
			return;
		
		if (!AccountProvider.get(e.getWhoClicked().getUniqueId()).hasPermission(PermissionsList.USE_COLORED_TEXT))
			return;
		
		e.setCurrentItem(ItemUtils.name(e.getCurrentItem(), ChatColor.translateAlternateColorCodes('&',	e.getCurrentItem().getItemMeta().getDisplayName())));
	}
	
	//Gestion des items restreints
	@EventHandler //test dans inventaires
	public void onLimitedItemInventory(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player))
			return;
		
		OlympaPlayer p = AccountProvider.get(e.getWhoClicked().getUniqueId());
		
		if (e.getCursor() != null)
			if (!hasPlayerPermissionFor(p, e.getCursor().getType(), true)) {
				e.setCancelled(true);
				e.getCursor().setType(Material.STONE);
			}
		
		if (e.getCurrentItem() != null)
			if (!hasPlayerPermissionFor(p, e.getCurrentItem().getType(), true)){
				e.setCancelled(true);
				e.getCursor().setType(Material.STONE);
			}
	}
	
	@EventHandler //cancel pickup item restreint
	public void onPickup(EntityPickupItemEvent e) {
		if (e.getEntityType() != EntityType.PLAYER)
			return;

		if (!hasPlayerPermissionFor(AccountProvider.get(e.getEntity().getUniqueId()), e.getItem().getItemStack().getType(), false))
			e.setCancelled(true);
	}
	
	@EventHandler //cancel interact item restreint
	public void onInterract(PlayerInteractEvent e) {
		if (e.getItem() == null)
			return;
		
		if (!hasPlayerPermissionFor(AccountProvider.get(e.getPlayer().getUniqueId()), e.getItem().getType(), true)){
			e.setCancelled(true);
			e.getItem().setType(Material.STONE);
		}
	}

	//true si le joueur a la permission d'utiliser l'objet désigné
	public boolean hasPlayerPermissionFor(OlympaPlayer p, Material mat, boolean sendMessage) {
		if (plugin.getWorldManager().getRestrictedItems().keySet().contains(mat))
			if (!p.hasPermission(plugin.getWorldManager().getRestrictedItems().get(mat))) {
				if (sendMessage)
					p.getPlayer().sendMessage(Message.INSUFFICIENT_KIT_PERMISSION.getValue().replace("%kit%", plugin.getWorldManager().getRestrictedItems().get(mat).toString().toLowerCase().replace("_", " ")));
				return false;
			}
		return true;
	}
	
	
	
	@EventHandler //cancel potions jetables si effet >5
	public void onSplashPotionEvent(PotionSplashEvent e) {
		for (PotionEffect effect : e.getPotion().getEffects())
			if (effect.getAmplifier() >= 5)
				e.setCancelled(true);
	}
	@EventHandler //cancel potions persistantes si effet >5
	public void onLingeringPotionEvent(LingeringPotionSplashEvent e) {
		for (PotionEffect effect : e.getAreaEffectCloud().getCustomEffects())
			if (effect.getAmplifier() >= 5)
				e.setCancelled(true);
	}
	
	@EventHandler //cancel potion avec effet >5
	public void onPotionConsume(PlayerItemConsumeEvent e) {
		if (e.getItem().getType() != Material.POTION)
			return;
			
		PotionMeta im = (PotionMeta) e.getItem().getItemMeta();
		for (PotionEffect effect : im.getCustomEffects())
			if (effect.getAmplifier() >= 5)
				e.setCancelled(true);
	}
	
	@EventHandler //ouvre le menu si joueur a sneak deux fois rapidement (délai : 0.2s)
	public void onOpenMenu(PlayerToggleSneakEvent e) {
		if (e.isSneaking())
			if (sneakHistory.keySet().contains(e.getPlayer().getName()))
				if (sneakHistory.get(e.getPlayer().getName()) + 200 > System.currentTimeMillis()) {
					Plot plot = plugin.getPlotsManager().getPlot(e.getPlayer().getLocation());
					if (plot == null)
						new MainGui(plugin, e.getPlayer(), plot, "§9Menu").create(e.getPlayer());
					else
						new MainGui(plugin, e.getPlayer(), plot, "§9Menu >> " + plot.getId().getAsString()).create(e.getPlayer());	
				}else
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
			((Dispenser) e.getBlock().getState()).getInventory().addItem(e.getItem().clone());
		}
		if (e.getBlock().getState() instanceof Dropper) {
			((Dropper) e.getBlock().getState()).getInventory().addItem(e.getItem().clone());
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
	
	@EventHandler //color sur pancartes
	public void onSignColor(SignChangeEvent e) {
		if (!AccountProvider.get(e.getPlayer().getUniqueId()).hasPermission(PermissionsList.USE_COLORED_TEXT))
			return;
		
		int i = 0;
		for (String s : e.getLines()) {
			e.setLine(i, ChatColor.translateAlternateColorCodes('&', s));
			i++;
		}
	}
}
