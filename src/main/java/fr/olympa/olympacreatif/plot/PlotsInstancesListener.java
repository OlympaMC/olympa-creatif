package fr.olympa.olympacreatif.plot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;
import net.minecraft.server.v1_15_R1.Entity;
import net.minecraft.server.v1_15_R1.NBTTagCompound;

public class PlotsInstancesListener implements Listener{

	private OlympaCreatifMain plugin;
	private static Map<Plot, Map<Player, List<ItemStack>>> inventoryStorage = new HashMap<Plot, Map<Player, List<ItemStack>>>();
	private Plot plot;
	
	public PlotsInstancesListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;
	}


	@EventHandler //test place block (autorisé uniquement pour les membres et pour la zone protégeé)
	public void onPlaceBlockEvent(BlockPlaceEvent e) {
		plot = plugin.getPlotsManager().getPlot(e.getBlockPlaced().getLocation());
		if (plot == null) {
			e.getPlayer().sendMessage(Message.PLOT_CANT_BUILD.getValue());
			e.setCancelled(true);
			return;	
		}
		
		if (plot.getMembers().getPlayerRank(e.getPlayer()) == PlotRank.VISITOR && !plot.getProtectedZoneData().keySet().contains(e.getBlock().getLocation())) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.PLOT_CANT_BUILD.getValue());
		}
	}
	
	@EventHandler //test break block (autorisé uniquement pour les membres et pour la zone protégeé)
	public void onBreakBlockEvent(BlockBreakEvent e) {
		plot = plugin.getPlotsManager().getPlot(e.getBlock().getLocation());
		if (plot == null) {
			e.getPlayer().sendMessage(Message.PLOT_CANT_BUILD.getValue());
			e.setCancelled(true);
			return;	
		}
		
		if (plot.getMembers().getPlayerRank(e.getPlayer()) == PlotRank.VISITOR && !plot.getProtectedZoneData().keySet().contains(e.getBlock().getLocation())) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.PLOT_CANT_BUILD.getValue());
		}
	}
	
	@EventHandler //cancel splash potion sin interdites dans le plot
	public void onPotionThrows(PotionSplashEvent e) {
		Location loc = null;
		if (e.getHitBlock() == null)
			loc = e.getHitEntity().getLocation();
		else
			loc = e.getHitBlock().getLocation();

		plot = plugin.getPlotsManager().getPlot(loc);
		
		if (e.isCancelled() || plot == null)
			return;
		
		if(!(boolean)plot.getParameters().getParameter(PlotParamType.ALLOW_SPLASH_POTIONS))
			e.setCancelled(true);
	}
	
	@SuppressWarnings("unchecked")
	@EventHandler //test interract block (cancel si pas la permission d'interagir avec le bloc) & test placement liquide
	public void onInterractEvent(PlayerInteractEvent e) {
		if (e.getClickedBlock() == null)
			return;
		
		plot = plugin.getPlotsManager().getPlot(e.getClickedBlock().getLocation());

		if (plot == null) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.PLOT_CANT_INTERRACT.getValue());
			return;
		}
		
		PlotRank playerRank = plot.getMembers().getPlayerRank(e.getPlayer());
		
		//test si permission d'interagir avec le bloc donné
		if (PlotParamType.getAllPossibleBlocksWithInteractions().contains(e.getClickedBlock().getType()))		
			if (playerRank == PlotRank.VISITOR && 
					!((ArrayList<Material>) plot.getParameters().getParameter(PlotParamType.LIST_ALLOWED_INTERRACTION)).contains(e.getClickedBlock().getType()) &&
					!plot.getProtectedZoneData().keySet().contains(e.getClickedBlock().getLocation())) {
				e.setCancelled(true);
				e.getPlayer().sendMessage(Message.PLOT_CANT_INTERRACT.getValue());
				
				return;
			}
		
		//cancel si usage d'autre chose qu'un oeuf, un arc, une arbalète ou une boule de neige
		if (e.getItem() == null)
			return;
		
		Material mat = e.getItem().getType(); 
		
		if (playerRank == PlotRank.VISITOR)
			if (mat != Material.BOW && mat != Material.SPLASH_POTION && mat != Material.SNOWBALL && mat != Material.CROSSBOW && mat != Material.FLINT_AND_STEEL)
				e.setUseItemInHand(Result.DENY);
	}
	
	
	@EventHandler //cancel interraction avec un itemframe
	public void onInterractEntityEvent(PlayerInteractEntityEvent e) {
		plot = plugin.getPlotsManager().getPlot(e.getRightClicked().getLocation());
		if (plot == null)
			return;

		if (plot.getMembers().getPlayerRank(e.getPlayer()) == PlotRank.VISITOR && e.getRightClicked().getType() == EntityType.ITEM_FRAME) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.PLOT_CANT_INTERRACT.getValue());
		}
		
		if ((e.getPlayer().getInventory().getItemInMainHand().getType() == Material.WOODEN_HOE || 
				e.getPlayer().getInventory().getItemInOffHand().getType() == Material.WOODEN_HOE) &&
				plot.getMembers().getPlayerRank(e.getPlayer()) != PlotRank.VISITOR && !(e.getRightClicked() instanceof Player))
			e.getRightClicked().remove();
			
	}
	
	@EventHandler //test print TNT
	public void onPrintTnt(BlockIgniteEvent e) {
		plot = plugin.getPlotsManager().getPlot(e.getBlock().getLocation());
		
		if (plot == null)
			return;
		if (e.getPlayer() == null || (e.getCause() != IgniteCause.ARROW && e.getCause() != IgniteCause.FLINT_AND_STEEL)) {
			e.setCancelled(true);
			return;
		}
		if (e.getBlock().getType() != Material.TNT && plot.getMembers().getPlayerRank(e.getPlayer()) == PlotRank.VISITOR) {
			e.setCancelled(true);
			return;
		}
		if (!(boolean)plot.getParameters().getParameter(PlotParamType.ALLOW_PRINT_TNT) && plot.getMembers().getPlayerRank(e.getPlayer()) == PlotRank.VISITOR) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.PLOT_CANT_PRINT_TNT.getValue());
		}
	}
	
	@SuppressWarnings("unchecked")
	@EventHandler //actions à effectuer lors de la sortie/entrée d'un joueur
	public void onPlayerMove(PlayerMoveEvent e) {
		if (e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockZ() == e.getTo().getBlockZ())
			return;
		
		Plot plotTo = plugin.getPlotsManager().getPlot(e.getTo());
		Plot plotFrom = plugin.getPlotsManager().getPlot(e.getFrom());
		//sortie de l'évent si pas de changement de plot
		if (plotTo == plotFrom)
			return;
		
		//actions d'entrée de plot

		//expulse les joueurs bannis
		if (plotTo != null) {
			if (((List<Long>) plotTo.getParameters().getParameter(PlotParamType.BANNED_PLAYERS)).contains(AccountProvider.get(e.getPlayer().getUniqueId()).getId())) {
				e.setCancelled(true);
				e.getPlayer().setVelocity(e.getPlayer().getVelocity().multiply(-2));
				e.getPlayer().sendMessage(Message.PLOT_CANT_ENTER_BANNED.getValue());
				return;
			}
			executeEntryActions(plugin, e.getPlayer(), plotTo);	
		}
		
		//actions de sortie de plot
		if (plotFrom != null) {
			plotFrom.removePlayer(e.getPlayer());
			
			//rendu inventaire si stocké
			if (inventoryStorage.containsKey(plotFrom) && inventoryStorage.get(plotFrom).containsKey(e.getPlayer())) {
				e.getPlayer().getInventory().clear();
				for (ItemStack it : inventoryStorage.get(plotFrom).get(e.getPlayer()))
					e.getPlayer().getInventory().addItem(it);
				inventoryStorage.get(plotFrom).remove(e.getPlayer());
			}
			
			//gamemode 1
			e.getPlayer().setGameMode(GameMode.CREATIVE);			
			e.getPlayer().setAllowFlight(true);
			//réinitialisation heure joueur
			e.getPlayer().resetPlayerTime();
			e.getPlayer().resetPlayerWeather();
		}
	}

	//actions à exécuter en entrée du plot (séparé du PlayerMoveEvent pour pouvoir être exécuté après le chargement du plot)
	@SuppressWarnings("unchecked")
	public static void executeEntryActions(OlympaCreatifMain plugin, Player p, Plot plotTo) {
		
		if (plugin.getPlotsManager().isAdmin(p))
			return;
		
		//si le joueur est banni, téléportation en dehors du plot
		if (((List<Long>) plotTo.getParameters().getParameter(PlotParamType.BANNED_PLAYERS)).contains(AccountProvider.get(p.getUniqueId()).getId())) {
			if (!AccountProvider.get(p.getUniqueId()).hasPermission(PermissionsList.STAFF_BYPASS_PLOT_BAN)) {
				p.sendMessage(Message.PLOT_CANT_ENTER_BANNED.getValue());
				plotTo.teleportOut(p);
				return;	
			}
		}

		plotTo.addPlayerInPlot(p);
		
		//les actions suivantes ne sont effectuées que si le joueur n'appartient pas au plot
		if (plotTo.getMembers().getPlayerRank(p) != PlotRank.VISITOR)
			return;
		
		//clear les visiteurs en entrée & stockage de leur inventaire
		if ((boolean)plotTo.getParameters().getParameter(PlotParamType.CLEAR_INCOMING_PLAYERS)) {
			List<ItemStack> list = new ArrayList<ItemStack>();
			for (ItemStack it : p.getInventory().getContents()) {
				if (it != null && it.getType() != Material.AIR)
				list.add(it);
			}
			
			if (!inventoryStorage.containsKey(plotTo))
				inventoryStorage.put(plotTo, new HashMap<Player, List<ItemStack>>());
			
			inventoryStorage.get(plotTo).put(p, list);
			p.getInventory().clear();
			
			for (PotionEffect effect : p.getActivePotionEffects())
				p.removePotionEffect(effect.getType());
		}
		
		//tp au spawn de la zone
		if ((boolean)plotTo.getParameters().getParameter(PlotParamType.FORCE_SPAWN_LOC)) {
			p.teleport((Location) plotTo.getParameters().getParameter(PlotParamType.SPAWN_LOC));
			p.sendMessage(Message.TELEPORTED_TO_PLOT_SPAWN.getValue());
		}
		
		//définition de l'heure du joueur
		p.setPlayerTime((int) plotTo.getParameters().getParameter(PlotParamType.PLOT_TIME), false);
		
		//définition du gamemode
		p.setGameMode((GameMode) plotTo.getParameters().getParameter(PlotParamType.GAMEMODE_INCOMING_PLAYERS));
		
		//définition du flymode
		p.setAllowFlight((boolean) plotTo.getParameters().getParameter(PlotParamType.ALLOW_FLY_INCOMING_PLAYERS));
		
		//définition de la météo
		p.setPlayerWeather((WeatherType) plotTo.getParameters().getParameter(PlotParamType.PLOT_WEATHER));
	}
	
	
	@EventHandler //rendu inventaire en cas de déconnexion & tp au spawn
	public void onQuitEvent(PlayerQuitEvent e) {
		plot = plugin.getPlotsManager().getPlot(e.getPlayer().getLocation());
		if (plot == null)
			return;

		plot.removePlayer(e.getPlayer());

		//rendu inventaire si stocké
		if (inventoryStorage.containsKey(plot) && inventoryStorage.get(plot).containsKey(e.getPlayer())) {
			e.getPlayer().getInventory().clear();
			for (ItemStack it : inventoryStorage.get(plot).get(e.getPlayer()))
				e.getPlayer().getInventory().addItem(it);
			inventoryStorage.get(plot).remove(e.getPlayer());
		}
		
		e.getPlayer().setGameMode(GameMode.CREATIVE);
		e.getPlayer().setAllowFlight(true);
		e.getPlayer().teleport(plugin.getWorldManager().getWorld().getSpawnLocation());
		e.getPlayer().resetPlayerTime();
		e.getPlayer().resetPlayerWeather();
	}

	@EventHandler //cancel remove paintings et itemsframes
	public void onItemFrameDestroy(HangingBreakByEntityEvent e) {
		plot = plugin.getPlotsManager().getPlot(e.getEntity().getLocation());
		if (plot == null)
			return;
		
		if (e.getRemover().getType() != EntityType.PLAYER && e.getEntity().getType() == EntityType.PAINTING && e.getEntity().getType() == EntityType.ITEM_FRAME) {
			e.setCancelled(true);
			return;
		}
		
		if (e.getRemover().getType() == EntityType.PLAYER && plot.getMembers().getPlayerRank((Player) e.getRemover()) == PlotRank.VISITOR) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler //gestion autorisation pvp
	public void onDamageByEntity(EntityDamageByEntityEvent e) {
		plot = plugin.getPlotsManager().getPlot(e.getEntity().getLocation());
		if (plot == null)
			return;
		
		if (!(boolean) plot.getParameters().getParameter(PlotParamType.ALLOW_PVP) && e.getEntityType() == EntityType.PLAYER && e.getDamager().getType() == EntityType.PLAYER) {
			e.setCancelled(true);
			return;
		}
		
		if (!(boolean) plot.getParameters().getParameter(PlotParamType.ALLOW_PVE) && (e.getEntityType() != EntityType.PLAYER || e.getDamager().getType() != EntityType.PLAYER)) {
			e.setCancelled(true);
			return;
		}
		
		NBTTagCompound tag = new NBTTagCompound();
		((CraftEntity)e.getEntity()).getHandle().c(tag);
		Bukkit.broadcastMessage(tag.asString());
		
		if (tag.hasKey("EntityTag"))
			if (tag.getCompound("EntityTag").hasKey("Invulnerable"))
				e.setCancelled(true);
		
		tpPlayerToPlotSpawnOnDeath(e, plot);
	}
	
	
	@EventHandler //gestion autorisation pvp
	public void onDamageByBlock(EntityDamageByBlockEvent e) {
		if (e.getEntityType() != EntityType.PLAYER)
			return;
		
		plot = plugin.getPlotsManager().getPlot(e.getEntity().getLocation());
		if (plot == null)
			return;
		
		if (!(boolean) plot.getParameters().getParameter(PlotParamType.ALLOW_ENVIRONMENT_DAMAGE)) {
			e.setCancelled(true);
			return;
		}
		
		tpPlayerToPlotSpawnOnDeath(e, plot);
	}
	
	
	private void tpPlayerToPlotSpawnOnDeath(EntityDamageEvent e, Plot plot) {
		Player p =  (Player) e.getEntity();
		if (((Player)e.getEntity()).getHealth() - e.getDamage() <= 0) {
			e.getEntity().teleport((Location) plot.getParameters().getParameter(PlotParamType.SPAWN_LOC));
			e.setCancelled(true);
			p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
			p.setFoodLevel(20);
		}
	}
	
	
	@EventHandler //empêche le placement de liquides en dehors du plot
	public void onPlaceLiquid(PlayerInteractEvent e){		
	}
	
	
	@EventHandler //empêche le drop d'items si interdit sur le plot (et cancel si route)
	public void onDropItem(PlayerDropItemEvent e) {
		plot = plugin.getPlotsManager().getPlot(e.getPlayer().getLocation());
		
		if (plot == null) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.PLOT_DENY_ITEM_DROP.getValue());
			return;	
		}
		
		if (plot.getMembers().getPlayerRank(e.getPlayer()) == PlotRank.VISITOR && !((boolean) plot.getParameters().getParameter(PlotParamType.ALLOW_DROP_ITEMS))) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.PLOT_DENY_ITEM_DROP.getValue());
			e.getPlayer().sendMessage("ICI");
		}		
	}
	
	@EventHandler //empêche la nourriture de descendre si paramètre du plot défini comme tel
	public void onFoodChange(FoodLevelChangeEvent e) {
		if (e.getEntityType() != EntityType.PLAYER)
			return;
		
		plot = plugin.getPlotsManager().getPlot(e.getEntity().getLocation());
		if (plot == null)
			return;
		
		if ((boolean) plot.getParameters().getParameter(PlotParamType.KEEP_MAX_FOOD_LEVEL))
			e.setCancelled(true);
	}
}
