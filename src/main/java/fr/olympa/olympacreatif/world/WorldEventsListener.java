package fr.olympa.olympacreatif.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import fr.olympa.api.command.essentials.AfkCommand;
import fr.olympa.api.customevents.AsyncPlayerAfkEvent;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OCparam;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.PlayerParamType;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.data.PermissionsManager.ComponentCreatif;
import fr.olympa.olympacreatif.gui.MainGui;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotId;

public class WorldEventsListener implements Listener{

	OlympaCreatifMain plugin;
	Map<String, Long> sneakHistory = new HashMap<String, Long>();

	Map<Plot, Integer> spawnEntities = new HashMap<Plot, Integer>();

	List<Entity> entities = new ArrayList<Entity>(); 
	List<Entity> entitiesToRemove = new ArrayList<Entity>();
	
	public WorldEventsListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onAfk(AsyncPlayerAfkEvent e) {
		if (e.isAfk())
			plugin.getTask().runTask(() -> {
				ThreadLocalRandom r = ThreadLocalRandom.current();
				if (r.nextBoolean())
					e.getPlayer().teleport(OCparam.SPAWN_LOC.get().toLoc().add(r.nextDouble(), 0, r.nextDouble()));
				else
					e.getPlayer().teleport(OCparam.SPAWN_LOC.get().toLoc().subtract(r.nextDouble(), 0, r.nextDouble()));
			});
	}
	
	@EventHandler //n'autorise que certaines sources de spawn de créatures 
	public void onCreatureSpawn(CreatureSpawnEvent e) {
		if (e.isCancelled())
			return;
		
		if (plugin.getPlotsManager().getPlot(e.getLocation()) == null) {
			e.setCancelled(true);
			return;
		}
		
		if (e.getSpawnReason() != SpawnReason.ENDER_PEARL && 
				e.getSpawnReason() != SpawnReason.CUSTOM && e.getSpawnReason() != SpawnReason.BREEDING && 
				e.getSpawnReason() != SpawnReason.SPAWNER && e.getSpawnReason() != SpawnReason.SPAWNER_EGG)
			e.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOW) //cancel spawn if outside of the creative world
	public void onEntitySpawn(EntitySpawnEvent e) {
		if (!ComponentCreatif.ENTITIES.isActivated() || !e.getLocation().getWorld().getUID().equals(plugin.getWorldManager().getWorld().getUID()))
			e.setCancelled(true);
	}

	@EventHandler
	public void onBlockSpread(BlockSpreadEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	public void onBlockSpread(BlockFormEvent e) {
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
		
		if (e.getCurrentItem() != null && e.getCurrentItem().getItemMeta() != null && e.getCurrentItem().getItemMeta().hasDisplayName())
			e.setCurrentItem(ItemUtils.name(e.getCurrentItem(), ChatColor.translateAlternateColorCodes('&',	e.getCurrentItem().getItemMeta().getDisplayName())));
	}
	
	@EventHandler //cancel potions jetables si effet > 10
	public void onSplashPotionEvent(PotionSplashEvent e) {
		for (PotionEffect effect : e.getPotion().getEffects())
			if (effect.getAmplifier() >= 10)
				effect.withAmplifier(9);
	}
	
	@EventHandler //cancel potion avec effet >5
	public void onPotionConsume(PlayerItemConsumeEvent e) {
		if (e.getItem().getType() != Material.POTION)
			return;
			
		for (PotionEffect effect : ((PotionMeta) e.getItem().getItemMeta()).getCustomEffects())
			if (effect.getAmplifier() >= 10)
				effect.withAmplifier(9);
	}
	
	@EventHandler //ouvre le menu si joueur a sneak deux fois rapidement (délai : 0.2s)
	public void onOpenMenu(PlayerToggleSneakEvent e) {
		if (e.isSneaking())
			if (sneakHistory.keySet().contains(e.getPlayer().getName()))
				if (sneakHistory.get(e.getPlayer().getName()) + 200 > System.currentTimeMillis()) {
					if (((OlympaPlayerCreatif)AccountProvider.get(e.getPlayer().getUniqueId())).hasPlayerParam(PlayerParamType.OPEN_GUI_ON_SNEAK))
						MainGui.getMainGui(AccountProvider.get(e.getPlayer().getUniqueId())).create(e.getPlayer());	
				}

				else
					sneakHistory.put(e.getPlayer().getName(), System.currentTimeMillis());
			else
				sneakHistory.put(e.getPlayer().getName(), System.currentTimeMillis());
	}
	
	@EventHandler //cancel téléportation par portail de l'end ou du nether ou si le monde de destination n'est pas le monde creative
	public void onChangeWorld(PlayerTeleportEvent e) {
		if (e.getCause() == TeleportCause.END_PORTAL || e.getCause() == TeleportCause.NETHER_PORTAL || 
				e.getCause() == TeleportCause.END_GATEWAY || e.getCause() == TeleportCause.SPECTATE || 
				!e.getTo().getWorld().equals(plugin.getWorldManager().getWorld()) ||
				!plugin.getWorldManager().getWorld().getWorldBorder().isInside(e.getTo()))
			e.setCancelled(true);
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) //chat de plot
	public void onChat(PlayerChatEvent e) {		
		Plot plot = plugin.getPlotsManager().getPlot(e.getPlayer().getLocation());
		OlympaPlayerCreatif p = AccountProvider.get(e.getPlayer().getUniqueId());
		
		if (PermissionsList.USE_COLORED_TEXT.hasPermission(p))
			e.setMessage(ChatColor.translateAlternateColorCodes('&', e.getMessage()));
		
		boolean invertTarget = false;
		if (e.getMessage().startsWith("@")) {
			e.setMessage(e.getMessage().replaceFirst("@", ""));
			invertTarget = true;
		}
		
		if (plot == null)
			return;
		 
		if ((p.hasPlayerParam(PlayerParamType.DEFAULT_PLOT_CHAT) && !invertTarget) ||
				(!p.hasPlayerParam(PlayerParamType.DEFAULT_PLOT_CHAT) && invertTarget)) {
			e.getRecipients().clear();
			plot.sendMessage(p, e.getMessage());
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

	
	@EventHandler //clear l'historique de sneak de ce joueur, exécute les actions de sortie de plot
	public void onPlayerQuit(PlayerQuitEvent e) {
		sneakHistory.remove(e.getPlayer().getName());
		OCparam.SPAWN_LOC.get().teleport(e.getPlayer());
		
		Plot plot = plugin.getPlotsManager().getPlot(e.getPlayer().getLocation());
		
		if (plot != null)
			plot.executeExitActions(e.getPlayer());
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		
		//fait croire au client qu'il est op (pour ouvrir l'interface des commandblocks)
		plugin.getCommandBlocksManager().setFakeOp(e.getPlayer(), true);
		
		Plot plot = plugin.getPlotsManager().getPlot(OCparam.SPAWN_LOC.get().toLoc());
		if (plot != null)
			plot.executeEntryActions(e.getPlayer(), false);

		OCparam.SPAWN_LOC.get().teleport(e.getPlayer());
		
		//set 1.8 attackspeed
		e.getPlayer().getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(16d);
	}
	
	/*
	@EventHandler //add perms worldedit si achat du grade correspondant
	public void onPlayerChangeGroupEvent(AsyncOlympaPlayerChangeGroupEvent e) {
		if (PermissionsList.USE_WORLD_EDIT.hasPermission(e.getOlympaPlayer()))
			setWorldEditPerms(AccountProvider.get(e.getPlayer().getUniqueId()), true);
	}*/
	
	//GESTION DES KITS
	@EventHandler(priority = EventPriority.LOWEST) //gestion des kits
	public void onBlockPlace(BlockPlaceEvent e) {
		
		OlympaPlayerCreatif p = AccountProvider.get(e.getPlayer().getUniqueId());
		
		//retrictions dues aux kits
		if (!plugin.getPerksManager().getKitsManager().hasPlayerPermissionFor(p, e.getBlock().getType())) {
			e.setCancelled(true);
			e.getPlayer().getInventory().setItem(e.getHand(), plugin.getPerksManager().getKitsManager().getNoKitPermItem(e.getBlock().getType()));
			e.getPlayer().updateInventory();
			return;
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST) //gestion des kits et des zones non claim
	public void onInterract(PlayerInteractEvent e) {
		
		OlympaPlayerCreatif p = AccountProvider.get(e.getPlayer().getUniqueId());
		
		//retrictions dues aux kits
		if (!plugin.getPerksManager().getKitsManager().hasPlayerPermissionFor(p, e.getMaterial())) {
			e.setCancelled(true);
			e.getPlayer().getInventory().setItem(e.getHand(), plugin.getPerksManager().getKitsManager().getNoKitPermItem(e.getMaterial()));
			return;
		}
		
		/*if (PlotId.fromLoc(plugin, e.getPlayer().getLocation()) == null && !p.hasStaffPerm(StaffPerm.BUILD_ROADS)) {
			e.setCancelled(true);
			return;	
		}*/
	}
	
	@EventHandler(priority = EventPriority.LOWEST) //gestion des kits
	public void onItemPickup(EntityPickupItemEvent e) {
		if (e.getEntityType() != EntityType.PLAYER)
			return;
		
		OlympaPlayerCreatif p = AccountProvider.get(e.getEntity().getUniqueId());
		
		//retrictions dues aux kits
		if (!plugin.getPerksManager().getKitsManager().hasPlayerPermissionFor(p, e.getItem().getItemStack().getType())) {
			e.getItem().setItemStack(plugin.getPerksManager().getKitsManager().getNoKitPermItem(e.getItem().getItemStack().getType()));
			return;
		}
	}
	
	@EventHandler //cancel join if plot size hos not been set yet, or cancel 1st join to avoid errors
	public void onPrePlayerJoin(AsyncPlayerPreLoginEvent e) {
		if (OCparam.PLOT_SIZE.get() == -1) {
			e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "§cImpossible de se connecter, le générateur de monde n'a pas été chargé correctement. Veuillez réessayer dans quelques instants.");
			return;
		}
		
		if (Bukkit.getOnlinePlayers().size() == 0)
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "§cUne erreur interne est survenue, merci de réessayer dans quelques instants.");
				plugin.getLogger().log(Level.WARNING, "Failed to delay first player join");
				e1.printStackTrace();
			}
	}

	@EventHandler
	public void onLeavesDisappear(LeavesDecayEvent e) {
		e.setCancelled(true);
	}
}



