package fr.olympa.olympacreatif.plot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;

public class PlotListener implements Listener {

	private OlympaCreatifMain plugin;
	private Plot plot;
	
	private Map<Player, ItemStack[]> inventoryStorage = new HashMap<Player, ItemStack[]>();
	
	public PlotListener(OlympaCreatifMain plugin, Plot plot) {
		this.plugin = plugin;
		this.plot = plot;
	}

	@EventHandler //test place block
	public void onPlaceBlockEvent(BlockPlaceEvent e) {
		if (e.isCancelled() || !plot.getArea().isInPlot(e.getBlockPlaced().getLocation()))
			return;
		
		if (plot.getMembers().getPlayerRank(e.getPlayer()) == PlotRank.VISITOR) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.PLOT_CANT_BUILD.getMessage());
		}
	}
	
	@EventHandler //test break block
	public void onBreakBlockEvent(BlockBreakEvent e) {
		if (e.isCancelled() || !plot.getArea().isInPlot(e.getBlock().getLocation()))
			return;
		
		if (plot.getMembers().getPlayerRank(e.getPlayer()) == PlotRank.VISITOR) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.PLOT_CANT_BUILD.getMessage());
		}
	}
	
	@SuppressWarnings("unchecked")
	@EventHandler //test interract block
	public void onInterractEvent(PlayerInteractEvent e) {
		if (!plot.getArea().isInPlot(e.getClickedBlock().getLocation()))
			return;
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null)
			return;
		
		if (plot.getMembers().getPlayerRank(e.getPlayer()) == PlotRank.VISITOR && 
				((ArrayList<Material>) plot.getParameters().getParameter(PlotParamType.LIST_PROHIBITED_INTERRACTION)).contains(e.getClickedBlock().getType())) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.PLOT_CANT_INTERRACT.getMessage());
		}
	}
	
	@EventHandler //test print TNT
	public void onPrintTnt(BlockIgniteEvent e) {
		if (!plot.getArea().isInPlot(e.getBlock().getLocation()))
			return;
		if (e.getPlayer() == null || (e.getCause() != IgniteCause.ARROW && e.getCause() != IgniteCause.FLINT_AND_STEEL)) {
			e.setCancelled(true);
			return;
		}
		if (!(boolean)plot.getParameters().getParameter(PlotParamType.ALLOW_PRINT_TNT) && plot.getMembers().getPlayerRank(e.getPlayer()) == PlotRank.VISITOR) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.PLOT_CANT_PRINT_TNT.getMessage());
		}
	}
	
	@SuppressWarnings("unchecked")
	@EventHandler //actions à effectuer lors de la sortie/entrée d'un joueur
	public void onPlayerMove(PlayerMoveEvent e) {
		//détection entrée dans plot
		if (!plot.getArea().isInPlot(e.getFrom()) && plot.getArea().isInPlot(e.getTo())) {
			if (plot.getMembers().getPlayerRank(e.getPlayer()) == PlotRank.VISITOR) {
				
				//expulse les joueurs bannis
				if (((List<Long>) plot.getParameters().getParameter(PlotParamType.BANNED_PLAYERS)).contains(AccountProvider.get(e.getPlayer().getUniqueId()).getId())) {
					e.setCancelled(true);
					e.getPlayer().setVelocity(e.getPlayer().getVelocity().multiply(-1));
					e.getPlayer().sendMessage(Message.PLOT_CANT_ENTER_BANNED.getMessage());
					return;
				}
				
				//clear les visiteurs en entrée
				if ((boolean)plot.getParameters().getParameter(PlotParamType.CLEAR_INCOMING_PLAYERS)) {
					inventoryStorage.put(e.getPlayer(), e.getPlayer().getInventory().getContents().clone());
					e.getPlayer().getInventory().clear();	
				}
				
				//tp au spawn de la zone
				if ((boolean)plot.getParameters().getParameter(PlotParamType.FORCE_SPAWN_LOC)) {
					e.getPlayer().teleport((Location) plot.getParameters().getParameter(PlotParamType.SPAWN_LOC));
					e.getPlayer().sendMessage(Message.TELEPORTED_TO_PLOT_SPAWN.getMessage());
				}
				
				//définition de l'heure du joueur
				if ((int) plot.getParameters().getParameter(PlotParamType.PLOT_TIME) != -1) {
					e.getPlayer().setPlayerTime((int) plot.getParameters().getParameter(PlotParamType.PLOT_TIME), false);
				}
				
				//définition du gamemode
				e.getPlayer().setGameMode((GameMode) plot.getParameters().getParameter(PlotParamType.GAMEMODE_INCOMING_PLAYERS));
				
				//définition du flymode
				e.getPlayer().setFlying((boolean) plot.getParameters().getParameter(PlotParamType.ALLOW_FLY_INCOMING_PLAYERS));
				
			}
		//détection sortie du plot
		}else if (plot.getArea().isInPlot(e.getFrom()) && !plot.getArea().isInPlot(e.getTo())) {
			
			//rendu inventaire avant entrée dans zone
			if (inventoryStorage.containsKey(e.getPlayer())) {
				e.getPlayer().getInventory().clear();
				e.getPlayer().getInventory().addItem(inventoryStorage.get(e.getPlayer()));
				inventoryStorage.remove(e.getPlayer());
			}
			
			//gamemode 1
			e.getPlayer().setGameMode(GameMode.CREATIVE);			
			
			//réinitialisation heure joueur
			e.getPlayer().resetPlayerTime();
		}
	}
	
	@EventHandler //rendu inventaire en cas de déconnexion & tp au spawn
	public void onQuitEvent(PlayerQuitEvent e) {
		if (!plot.getArea().isInPlot(e.getPlayer().getLocation()))
			return;
		
		if (inventoryStorage.containsKey(e.getPlayer())) {
			e.getPlayer().getInventory().clear();
			e.getPlayer().getInventory().addItem(inventoryStorage.get(e.getPlayer()));
			inventoryStorage.remove(e.getPlayer());
		}
		
		e.getPlayer().setGameMode(GameMode.CREATIVE);
		e.getPlayer().setFlying(true);
		e.getPlayer().teleport(plugin.getWorldManager().getWorld().getSpawnLocation());
		e.getPlayer().resetPlayerTime();
	}
}







