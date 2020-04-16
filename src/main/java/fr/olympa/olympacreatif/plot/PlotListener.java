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
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;

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
		if (e.isCancelled() || !plot.getId().isInPlot(e.getBlockPlaced().getLocation()))
			return;
		
		if (plot.getMembers().getPlayerRank(e.getPlayer()) == PlotRank.VISITOR) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.PLOT_CANT_BUILD.getValue());
		}
	}
	
	@EventHandler //test break block
	public void onBreakBlockEvent(BlockBreakEvent e) {
		if (e.isCancelled() || !plot.getId().isInPlot(e.getBlock().getLocation()))
			return;
		
		if (plot.getMembers().getPlayerRank(e.getPlayer()) == PlotRank.VISITOR) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.PLOT_CANT_BUILD.getValue());
		}
	}
	
	@SuppressWarnings("unchecked")
	@EventHandler //test interract block
	public void onInterractEvent(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null)
			return;
		if (!plot.getId().isInPlot(e.getClickedBlock().getLocation()))
			return;
		if (!PlotParameters.getAllPossibleAllowedBlocks().contains(e.getClickedBlock().getType()))
			return;
		
		if (plot.getMembers().getPlayerRank(e.getPlayer()) == PlotRank.VISITOR && 
				!((ArrayList<Material>) plot.getParameters().getParameter(PlotParamType.LIST_ALLOWED_INTERRACTION)).contains(e.getClickedBlock().getType())) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.PLOT_CANT_INTERRACT.getValue());
		}
	}
	
	@EventHandler //test print TNT
	public void onPrintTnt(BlockIgniteEvent e) {
		if (!plot.getId().isInPlot(e.getBlock().getLocation()))
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
		//détection entrée dans plot
		if (!plot.getId().isInPlot(e.getFrom()) && plot.getId().isInPlot(e.getTo())) {

			//expulse les joueurs bannis
			if (((List<Long>) plot.getParameters().getParameter(PlotParamType.BANNED_PLAYERS)).contains(AccountProvider.get(e.getPlayer().getUniqueId()).getId())) {
				e.setCancelled(true);
				e.getPlayer().setVelocity(e.getPlayer().getVelocity().multiply(-2));
				e.getPlayer().sendMessage(Message.PLOT_CANT_ENTER_BANNED.getValue());
				return;
			}
			
			executeEntryActions(e.getPlayer());
			
		//détection sortie du plot
		}else if (plot.getId().isInPlot(e.getFrom()) && !plot.getId().isInPlot(e.getTo())) {
			
			plot.removePlayer(e.getPlayer());
			
			//rendu inventaire avant entrée dans zone
			if (inventoryStorage.containsKey(e.getPlayer())) {
				e.getPlayer().getInventory().clear();
				e.getPlayer().getInventory().addItem(inventoryStorage.get(e.getPlayer()));
				inventoryStorage.remove(e.getPlayer());
			}
			
			//gamemode 1
			e.getPlayer().setGameMode(GameMode.CREATIVE);			
			e.getPlayer().setAllowFlight(true);
			//réinitialisation heure joueur
			e.getPlayer().resetPlayerTime();
		}
	}

	//actions à exécuter en entrée du plot (séparé du PlayerMoveEvent pour pouvoir être exécuté après le chargement du plot)
	@SuppressWarnings("unchecked")
	void executeEntryActions(Player p) {
		
		//si le joueur est banni, téléportation en dehors du plot
		if (((List<Long>) plot.getParameters().getParameter(PlotParamType.BANNED_PLAYERS)).contains(AccountProvider.get(p.getUniqueId()).getId())) {
			p.sendMessage(Message.PLOT_CANT_ENTER_BANNED.getValue());
			plot.teleportOut(p);
			return;
		}
		
		plot.addPlayerInPlot(p);
		
		//les actions suivantes ne sont effectuées que si le joueur n'appartient pas au plot
		if (plot.getMembers().getPlayerRank(p) != PlotRank.VISITOR)
			return;
		
		//clear les visiteurs en entrée
		if ((boolean)plot.getParameters().getParameter(PlotParamType.CLEAR_INCOMING_PLAYERS)) {
			inventoryStorage.put(p, p.getInventory().getContents().clone());
			p.getInventory().clear();	
		}
		
		//tp au spawn de la zone
		if ((boolean)plot.getParameters().getParameter(PlotParamType.FORCE_SPAWN_LOC)) {
			p.teleport((Location) plot.getParameters().getParameter(PlotParamType.SPAWN_LOC));
			p.sendMessage(Message.TELEPORTED_TO_PLOT_SPAWN.getValue());
		}
		
		//définition de l'heure du joueur
		p.setPlayerTime((int) plot.getParameters().getParameter(PlotParamType.PLOT_TIME), true);
		
		//définition du gamemode
		p.setGameMode((GameMode) plot.getParameters().getParameter(PlotParamType.GAMEMODE_INCOMING_PLAYERS));
		
		//définition du flymode
		p.setAllowFlight((boolean) plot.getParameters().getParameter(PlotParamType.ALLOW_FLY_INCOMING_PLAYERS));
	}
	
	@EventHandler //rendu inventaire en cas de déconnexion & tp au spawn
	public void onQuitEvent(PlayerQuitEvent e) {
		if (!plot.getId().isInPlot(e.getPlayer().getLocation()))
			return;

		plot.removePlayer(e.getPlayer());
		
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







