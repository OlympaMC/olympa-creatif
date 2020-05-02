package fr.olympa.olympacreatif.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.gui.MainGui;
import fr.olympa.olympacreatif.plot.Plot;
import net.minecraft.server.v1_15_R1.MinecraftServer;

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
			int maxEntitiesPerTypePerPlot = Integer.valueOf(Message.PARAM_MAX_ENTITIES_PER_TYPE_PER_PLOT.getValue());
			int maxTotalEntitiesPerPlot = Integer.valueOf(Message.PARAM_MAX_TOTAL_ENTITIES_PER_PLOT.getValue());
			
			private int getTotalEntities(Map<EntityType, Integer> map) {
				int i = 0;
				for (Entry<EntityType, Integer> e : map.entrySet())
					i += e.getValue();
					
				return i;
			}
			
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
						ListIterator<Entity> iterator = entities.listIterator(entities.size());
						Entity entity = null;
						
						//parcours de la liste en sens inverse pour supprimer les plus anciennes entités
						while (iterator.hasPrevious()) {
							entity = iterator.previous();
							
							Plot plot = plugin.getPlotsManager().getPlot(entity.getLocation());
							
							if (entity.getType() == EntityType.PLAYER)
								continue;
							
							//supprime l'entité si en dehors d'un plot (sauf si armorstand, peinture ou cadre) ou si le nombre d'entités dans le plot dépasse la valeur en paramètre
							if (plot == null && entity.getType() != EntityType.ARMOR_STAND && entity.getType() != EntityType.PAINTING && entity.getType() != EntityType.ITEM_FRAME)
								entitiesToRemove.add(entity);
							else //si le plot n'existe pas, on le crée
								if (entitiesPerPlot.keySet().contains(plot))
									//si l'entité n'est pas référencée, on le fait
									if (entitiesPerPlot.get(plot).containsKey(entity.getType())) {
										entitiesPerPlot.get(plot).put(entity.getType(), entitiesPerPlot.get(plot).get(entity.getType()) + 1);
										//si le nb d'entités pour ce type ou si le nb total d'entités est dépassé, on la supprime
										if (entitiesPerPlot.get(plot).get(entity.getType()) >= maxEntitiesPerTypePerPlot 
												|| getTotalEntities(entitiesPerPlot.get(plot)) >= maxTotalEntitiesPerPlot)
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
		}.runTaskTimer(plugin, 0, 30);
	}
	
	@EventHandler //n'autorise que les spawn à partir d'oeufs 
	public void onCreatureSpawn(CreatureSpawnEvent e) {
		if (plugin.getPlotsManager().getPlot(e.getLocation()) == null) {
			e.setCancelled(true);
			return;
		}
		
		if (e.getEntityType() == EntityType.ARMOR_STAND)
			return;
		
		if (e.getSpawnReason() != SpawnReason.DISPENSE_EGG && e.getSpawnReason() != SpawnReason.ENDER_PEARL &&
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
	
	//Gestion renommage en couleur des items
	@EventHandler
	public void onItemRename(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player))
			return;
		
		if (e.getClickedInventory() == null || e.getClickedInventory().getType() != InventoryType.ANVIL)
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
		
		if (e.getCurrentItem() != null)
			if (!hasPlayerPermissionFor(p, e.getCurrentItem().getType(), false)){
				e.setCancelled(true);
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
	public boolean hasPlayerPermissionFor(OlympaPlayer p, Material mat, boolean setStoneInMainHand) {
		if (plugin.getWorldManager().getRestrictedItems().keySet().contains(mat))
			if (!p.hasPermission(plugin.getWorldManager().getRestrictedItems().get(mat))) {
				if (setStoneInMainHand)
					if (p.getPlayer().getInventory().getItemInMainHand() != null)
						ItemUtils.name(p.getPlayer().getInventory().getItemInMainHand(), Message.INSUFFICIENT_KIT_PERMISSION.getValue().replace("%kit%", plugin.getWorldManager().getRestrictedItems().get(mat).toString().toLowerCase().replace("_", " ")));
				return true;
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
	
	/*
	@Deprecated
	@EventHandler //mise du GM 1
	public void onJoinEvent(PlayerJoinEvent e) {
		Bukkit.getServer().getScheduler().runTaskLater(plugin, () -> e.getPlayer().setGameMode(GameMode.CREATIVE), 1);
	}
	*/
	
	@EventHandler //cancel téléportation par portail de l'end ou du nether
	public void onChangeWorld(PlayerTeleportEvent e) {
		if (e.getCause() == TeleportCause.END_PORTAL || e.getCause() == TeleportCause.NETHER_PORTAL)
			e.setCancelled(true);
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
	public void onChat(AsyncPlayerChatEvent e) {
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
