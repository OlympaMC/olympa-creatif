package fr.olympa.olympacreatif.world;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
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
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
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
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.potion.PotionEffect;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.gui.MainGui;
import fr.olympa.olympacreatif.plot.Plot;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.MinecraftServer;
import net.minecraft.server.v1_15_R1.PacketPlayInChat;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityStatus;

public class WorldEventsListener implements Listener{

	OlympaCreatifMain plugin;
	Map<String, Long> sneakHistory = new HashMap<String, Long>();
	
	List<Entity> entities = new ArrayList<Entity>(); 
	List<Entity> entitiesToRemove = new ArrayList<Entity>();

	Map<Plot, Integer> spawnEntities = new HashMap<Plot, Integer>();
	
	int maxEntitiesPerTypePerPlot = 0;
	int maxTotalEntitiesPerPlot = 0;
	
	public WorldEventsListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;

		maxEntitiesPerTypePerPlot = Integer.valueOf(Message.PARAM_MAX_ENTITIES_PER_TYPE_PER_PLOT.getValue());
		maxTotalEntitiesPerPlot = Integer.valueOf(Message.PARAM_MAX_TOTAL_ENTITIES_PER_PLOT.getValue());
		
		//gestion des entités (remove si plot null ou si nb par plot > 100) et update de la liste des entités dans chaque plot (amélioration res performances du sélecteur @ dans les commandes)
		new BukkitRunnable() {
			
			
			Thread asyncEntityCheckup = null;
			
			int getTotalEntities(Map<EntityType, Integer> map) {
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
						
						for (Plot plot : plugin.getPlotsManager().getPlots())
							plot.clearEntitiesInPlot();
						
						//parcours de la liste en sens inverse pour supprimer les plus anciennes entités
						while (iterator.hasPrevious()) {
							entity = iterator.previous();
							
							Plot plot = plugin.getPlotsManager().getPlot(entity.getLocation());
							
							if (entity.getType() == EntityType.PLAYER)
								continue;
							
							//supprime l'entité si en dehors d'un plot (sauf si armorstand, peinture ou cadre) ou si le nombre d'entités dans le plot dépasse la valeur en paramètre
							if (plot == null) {
								if (entity.getType() != EntityType.ARMOR_STAND && entity.getType() != EntityType.PAINTING && entity.getType() != EntityType.ITEM_FRAME) {
									entitiesToRemove.add(entity);	
								}	
							}else {
								//création de la liste pour le plot si elle n'existe pas encore
								if (!entitiesPerPlot.containsKey(plot))
									entitiesPerPlot.put(plot, new HashMap<EntityType, Integer>());
								
								Map<EntityType, Integer> plotEntities = entitiesPerPlot.get(plot);
								
								//création de l'entrée pour le type d'entitité d'entity si n'existe pas encore
								if (!plotEntities.containsKey(entity.getType()))
									plotEntities.put(entity.getType(), 0);

								//supression de l'entité OU ajout à la liste des entités du plot
								if (plotEntities.get(entity.getType()) >= maxEntitiesPerTypePerPlot || getTotalEntities(plotEntities) >= maxTotalEntitiesPerPlot) {
									entitiesToRemove.add(entity);
								}else {
									plotEntities.put(entity.getType(), plotEntities.get(entity.getType()) + 1);
									plot.addEntityInPlot(entity);
								}
							}
						}
					}
				});
				asyncEntityCheckup.start();
			}
		}.runTaskTimer(plugin, 0, 30);
		
		
		//vide l'historique des entités spawnées
		new BukkitRunnable() {
			
			@Override
			public void run() {
				spawnEntities.clear();
			}
		}.runTaskTimer(plugin, 0, 20);
	}
	
	@EventHandler //n'autorise que certaines sources de spawn de créatures 
	public void onCreatureSpawn(CreatureSpawnEvent e) {
		if (e.isCancelled())
			return;
		
		if (plugin.getPlotsManager().getPlot(e.getLocation()) == null) {
			e.setCancelled(true);
			return;
		}
		
		/*
		if (e.getEntityType() == EntityType.ARMOR_STAND)
			return;*/
		
		if (e.getSpawnReason() != SpawnReason.ENDER_PEARL && 
				e.getSpawnReason() != SpawnReason.CUSTOM && e.getSpawnReason() != SpawnReason.ENDER_PEARL && 
				e.getSpawnReason() != SpawnReason.SPAWNER && e.getSpawnReason() != SpawnReason.SPAWNER_EGG)
			e.setCancelled(true);
	}
	
	@EventHandler //cancel évent si trop grand nombre spawnées simultanément dans un plot donné
	public void onEntitySpawn(EntitySpawnEvent e) {
		if (e.isCancelled() || e.getEntityType() == EntityType.PLAYER)
			return;
		
		Plot plot = plugin.getPlotsManager().getPlot(e.getLocation());
		
		if (plot == null) {
			e.setCancelled(true);
			return;
		}
		
		if (spawnEntities.containsKey(plot)) {
			spawnEntities.put(plot, spawnEntities.get(plot)+1);
			if (spawnEntities.get(plot) > maxEntitiesPerTypePerPlot)
				e.setCancelled(true);
		}else
			spawnEntities.put(plot, 1);
		
	}

	@EventHandler
	public void onFireSpread(BlockSpreadEvent e) {
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
		
		if (!PermissionsList.USE_COLORED_TEXT.hasPermission(e.getWhoClicked().getUniqueId()))
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
			if (!plugin.getWorldManager().hasPlayerPermissionFor(p, e.getCurrentItem().getType(), false)){
				e.setCancelled(true);
			}
	}
	
	
	@EventHandler //cancel pickup item restreint
	public void onPickup(EntityPickupItemEvent e) {
		if (e.getEntityType() != EntityType.PLAYER)
			return;

		if (!plugin.getWorldManager().hasPlayerPermissionFor(AccountProvider.get(e.getEntity().getUniqueId()), e.getItem().getItemStack().getType(), false))
			e.setCancelled(true);
	}
	
	@EventHandler //cancel interact item restreint
	public void onInterract(PlayerInteractEvent e) {
		if (e.getItem() == null)
			return;
		
		if (!plugin.getWorldManager().hasPlayerPermissionFor(AccountProvider.get(e.getPlayer().getUniqueId()), e.getItem().getType(), true)){
			e.setCancelled(true);
			e.getItem().setType(Material.STONE);
		}
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
						new MainGui(plugin, e.getPlayer(), plot, "Menu").create(e.getPlayer());
					else
						new MainGui(plugin, e.getPlayer(), plot, "Menu >> " + plot.getLoc().getId(true)).create(e.getPlayer());	
				}else
					sneakHistory.put(e.getPlayer().getName(), System.currentTimeMillis());
			else
				sneakHistory.put(e.getPlayer().getName(), System.currentTimeMillis());
	}
	
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
		if (!PermissionsList.USE_COLORED_TEXT.hasPermission(e.getPlayer().getUniqueId()))
			return;
		
		int i = 0;
		for (String s : e.getLines()) {
			e.setLine(i, ChatColor.translateAlternateColorCodes('&', s));
			i++;
		}
	}

	
	@EventHandler //clear l'historique de sneak de ce joueur
	public void onPlayerQuit(PlayerQuitEvent e) {
		sneakHistory.remove(e.getPlayer().getName());
		e.getPlayer().teleport(plugin.getWorldManager().getWorld().getSpawnLocation());

		((OlympaPlayerCreatif)AccountProvider.get(e.getPlayer().getUniqueId())).removeBukkitPermissions();
	}
	
	PermissionAttachment perm;
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		e.getPlayer().teleport(plugin.getWorldManager().getWorld().getSpawnLocation());
		
		//fait croire au client qu'il est op (pour ouvrir l'interface des commandblocks)
		EntityPlayer nmsPlayer = ((CraftPlayer) e.getPlayer()).getHandle();
		nmsPlayer.playerConnection.sendPacket(new PacketPlayOutEntityStatus(nmsPlayer, (byte) 28));

		((OlympaPlayerCreatif)AccountProvider.get(e.getPlayer().getUniqueId())).addBukkitPermissions();
	}
}
