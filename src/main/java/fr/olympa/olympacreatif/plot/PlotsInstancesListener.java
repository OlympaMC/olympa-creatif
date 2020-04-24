package fr.olympa.olympacreatif.plot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;

public class PlotsInstancesListener implements Listener{

	private OlympaCreatifMain plugin;
	private static Map<Plot, Map<Player, List<ItemStack>>> inventoryStorage = new HashMap<Plot, Map<Player, List<ItemStack>>>();
	private Plot plot;
	
	public PlotsInstancesListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;
	}


	@EventHandler //test place block
	public void onPlaceBlockEvent(BlockPlaceEvent e) {
		plot = plugin.getPlotsManager().getPlot(e.getBlockPlaced().getLocation());
		if (e.isCancelled() || plot == null)
			return;
		
		if (plot.getMembers().getPlayerRank(e.getPlayer()) == PlotRank.VISITOR) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.PLOT_CANT_BUILD.getValue());
		}
	}
	
	@EventHandler //test break block
	public void onBreakBlockEvent(BlockBreakEvent e) {
		plot = plugin.getPlotsManager().getPlot(e.getBlock().getLocation());
		if (e.isCancelled() || plot == null)
			return;
		
		if (plot.getMembers().getPlayerRank(e.getPlayer()) == PlotRank.VISITOR) {
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
	@EventHandler //test interract block
	public void onInterractEvent(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null)
			return;

		plot = plugin.getPlotsManager().getPlot(e.getClickedBlock().getLocation());
		
		if (plot == null)
			return;
		
		if (!PlotParamType.getAllPossibleAllowedBlocks().contains(e.getClickedBlock().getType()))
			return;
		
		if (plot.getMembers().getPlayerRank(e.getPlayer()) == PlotRank.VISITOR && 
				!((ArrayList<Material>) plot.getParameters().getParameter(PlotParamType.LIST_ALLOWED_INTERRACTION)).contains(e.getClickedBlock().getType())) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.PLOT_CANT_INTERRACT.getValue());
		}
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
			executeEntryActions(e.getPlayer(), plotTo);	
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
	public static void executeEntryActions(Player p, Plot plotTo) {
		
		//si le joueur est banni, téléportation en dehors du plot
		if (((List<Long>) plotTo.getParameters().getParameter(PlotParamType.BANNED_PLAYERS)).contains(AccountProvider.get(p.getUniqueId()).getId())) {
			p.sendMessage(Message.PLOT_CANT_ENTER_BANNED.getValue());
			plotTo.teleportOut(p);
			return;
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
		p.setPlayerTime((int) plotTo.getParameters().getParameter(PlotParamType.PLOT_TIME), true);
		
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

	@EventHandler //gestion autorisation pvp
	public void onDamage(EntityDamageByEntityEvent e) {
		plot = plugin.getPlotsManager().getPlot(e.getEntity().getLocation());
		if (plot == null)
			return;
		
		if (!(boolean) plot.getParameters().getParameter(PlotParamType.ALLOW_PVP))
			e.setCancelled(true);			
	}
	
	
	@EventHandler //gestion autorisation pvp
	public void onDamage(EntityDamageByBlockEvent e) {
		plot = plugin.getPlotsManager().getPlot(e.getEntity().getLocation());
		if (plot == null)
			return;
		
		if (!(boolean) plot.getParameters().getParameter(PlotParamType.ALLOW_ENVIRONMENT_DAMAGE))
			e.setCancelled(true);			
	}
}
