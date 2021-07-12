package fr.olympa.olympacreatif.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.bukkit.event.block.BlockExplodeEvent;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import com.google.common.collect.ImmutableSet;

import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.spigot.customevents.AsyncPlayerAfkEvent;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCparam;
import fr.olympa.olympacreatif.data.OcPermissions;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.PlayerParamType;
import fr.olympa.olympacreatif.data.PermissionsManager.ComponentCreatif;
import fr.olympa.olympacreatif.gui.MainGui;
import fr.olympa.olympacreatif.plot.Plot;

public class WorldEventsListener implements Listener {

	OlympaCreatifMain plugin;
	Map<String, Long> sneakHistory = new HashMap<>();

	Map<Plot, Integer> spawnEntities = new HashMap<>();

	List<Entity> entities = new ArrayList<>();
	List<Entity> entitiesToRemove = new ArrayList<>();

	private Set<SpawnReason> allowedSpawnReasons = ImmutableSet.<SpawnReason>builder()
			.add(SpawnReason.ENDER_PEARL)
			.add(SpawnReason.BEEHIVE)
			.add(SpawnReason.CUSTOM)
			.add(SpawnReason.SPAWNER)
			.add(SpawnReason.BREEDING)
			.add(SpawnReason.SPAWNER_EGG)
			.add(SpawnReason.DISPENSE_EGG)
			.add(SpawnReason.DROWNED)
			.add(SpawnReason.BUILD_IRONGOLEM)
			.add(SpawnReason.BUILD_SNOWMAN)
			.add(SpawnReason.OCELOT_BABY)
			.add(SpawnReason.SLIME_SPLIT)
			.build();

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

	@EventHandler(priority = EventPriority.LOW) //n'autorise que certaines sources de spawn de créatures
	public void onCreatureSpawn(CreatureSpawnEvent e) {
		//System.out.println("Tryng to spawn " + e.getEntity() + " with reason " + e.getSpawnReason() + " is allowed : " + allowedSpawnReasons.contains(e.getSpawnReason()));

		//System.out.println("Creature Spawn Event : " + e.getEntityType() + " because " + e.getSpawnReason());

		if (plugin.getPlotsManager().getPlot(e.getLocation()) == null) {
			e.setCancelled(true);
			return;
		}

		if (!allowedSpawnReasons.contains(e.getSpawnReason()) && e.getEntityType() != EntityType.ARMOR_STAND)
			//System.out.println("DEBUG : cancelled " + e.getEntityType() + " spawn with reason " + e.getSpawnReason());
			e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST) //cancel spawn if outside of the creative world
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

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true) //cancel explosion TNT
	public void onEntityExplodeEvent(EntityExplodeEvent e) {
		e.blockList().clear();
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockExplodeEvent(BlockExplodeEvent e) {
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

		if (!OcPermissions.USE_COLORED_TEXT.hasPermission(e.getWhoClicked().getUniqueId()))
			return;

		if (e.getCurrentItem() != null && e.getCurrentItem().getItemMeta() != null && e.getCurrentItem().getItemMeta().hasDisplayName())
			e.setCurrentItem(ItemUtils.name(e.getCurrentItem(), ChatColor.translateAlternateColorCodes('&', e.getCurrentItem().getItemMeta().getDisplayName())));
	}

	@EventHandler //cancel potions jetables si effet > 10
	public void onSplashPotionEvent(PotionSplashEvent e) {
		for (PotionEffect effect : e.getPotion().getEffects())
			if (effect.getAmplifier() >= 50)
				effect.withAmplifier(49);
	}

	@EventHandler //cancel potion avec effet > 10
	public void onPotionConsume(PlayerItemConsumeEvent e) {
		if (e.getItem().getType() != Material.POTION)
			return;

		for (PotionEffect effect : ((PotionMeta) e.getItem().getItemMeta()).getCustomEffects())
			if (effect.getAmplifier() >= 50)
				effect.withAmplifier(49);
	}

	@EventHandler //ouvre le menu si joueur a sneak deux fois rapidement (délai : 0.2s)
	public void onOpenMenu(PlayerToggleSneakEvent e) {
		if (e.isSneaking())
			if (sneakHistory.keySet().contains(e.getPlayer().getName()))
				if (sneakHistory.get(e.getPlayer().getName()) + 200 > System.currentTimeMillis()) {
					if (((OlympaPlayerCreatif) AccountProviderAPI.getter().get(e.getPlayer().getUniqueId())).hasPlayerParam(PlayerParamType.OPEN_GUI_ON_SNEAK))
						MainGui.getMainGui(AccountProviderAPI.getter().get(e.getPlayer().getUniqueId())).create(e.getPlayer());
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
	public void onChat(AsyncPlayerChatEvent e) {
		Plot plot = plugin.getPlotsManager().getPlot(e.getPlayer().getLocation());
		OlympaPlayerCreatif p = AccountProviderAPI.getter().get(e.getPlayer().getUniqueId());

		boolean invertTarget = false;
		if (e.getMessage().charAt(0) == '@') {
			e.setMessage(e.getMessage().substring(1));
			invertTarget = true;
		}

		if (plot == null)
			return;

		if (p.hasPlayerParam(PlayerParamType.DEFAULT_PLOT_CHAT) && !invertTarget ||
				!p.hasPlayerParam(PlayerParamType.DEFAULT_PLOT_CHAT) && invertTarget) {
			e.setCancelled(true);
			if (OcPermissions.USE_COLORED_TEXT.hasPermission(p))
				e.setMessage(ColorUtils.color(e.getMessage()));
			plot.sendMessage(p, e.getMessage());

		} else if (OcPermissions.USE_COLORED_TEXT.hasPermission(p))
			e.setMessage(ColorUtils.colorSoft(e.getMessage()));
	}

	@EventHandler //color sur pancartes
	public void onSignColor(SignChangeEvent e) {
		if (!OcPermissions.USE_COLORED_TEXT.hasPermission(e.getPlayer().getUniqueId()))
			return;

		int i = 0;
		for (String s : e.getLines()) {
			e.setLine(i, ColorUtils.color(s));
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
		//plugin.getCommandBlocksManager().setFakeOp(e.getPlayer(), true);
		e.getPlayer().sendOpLevel((byte) 4);

		Plot plot = plugin.getPlotsManager().getPlot(OCparam.SPAWN_LOC.get().toLoc());
		if (plot != null)
			plot.executeEntryActions(e.getPlayer(), e.getPlayer().getLocation());

		OCparam.SPAWN_LOC.get().teleport(e.getPlayer());

		//set 1.8 attackspeed
		e.getPlayer().getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(16d);
	}

	/*
	@EventHandler //add perms worldedit si achat du grade correspondant
	public void onPlayerChangeGroupEvent(AsyncOlympaPlayerChangeGroupEvent e) {
		if (PermissionsList.USE_WORLD_EDIT.hasPermission(e.getOlympaPlayer()))
			setWorldEditPerms(AccountProviderAPI.getter().get(e.getPlayer().getUniqueId()), true);
	}*/

	//GESTION DES KITS
	@EventHandler(priority = EventPriority.LOWEST) //gestion des kits
	public void onBlockPlace(BlockPlaceEvent e) {

		OlympaPlayerCreatif p = AccountProviderAPI.getter().get(e.getPlayer().getUniqueId());

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

		OlympaPlayerCreatif p = AccountProviderAPI.getter().get(e.getPlayer().getUniqueId());

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

		OlympaPlayerCreatif p = AccountProviderAPI.getter().get(e.getEntity().getUniqueId());

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
