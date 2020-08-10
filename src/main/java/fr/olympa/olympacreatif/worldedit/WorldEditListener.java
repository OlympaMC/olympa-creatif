package fr.olympa.olympacreatif.worldedit;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.eventbus.EventHandler;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.Node;
import net.minecraft.server.v1_15_R1.MinecraftServer;

public class WorldEditListener extends EventHandler implements Listener {

	private OlympaCreatifMain plugin;
	
	//contient la dernière fois qu'un joueur a exécuté un /copy.
	//cancel le /copy si un placement de blocs a été effectué trop récemment.
	//évite un usebeug possible permettant de /copy d'un plot à l'autre sans être le propriétaire du plot source :
	//si un joueur fais un /paste puis un /copy dans le même plot juste après, le système de vérif peut être abusé et autoriser le paste alors que le
	//joueur n'est pas proprio du plot source
	private Map<Player, Integer> lastPasteTick = new HashMap<Player, Integer>(); 
	
	//private Map<BlockVector3, Plot> storedLocs = new HashMap<BlockVector3, Plot>();
	
	public WorldEditListener(OlympaCreatifMain plugin) {
		super(Priority.NORMAL);
		
		this.plugin = plugin;
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
		/*new BukkitRunnable() {
			
			@Override
			public void run() {
				storedLocs.clear();
			}
		}.runTaskTimer(plugin, 0, 5);*/
	}

	@org.bukkit.event.EventHandler //cancel copy si le joueur n'a pas la permission de copier certains des blocs de sa sélection
	public void onCopySelection(PlayerCommandPreprocessEvent e) {
		if (!e.getMessage().contains("/copy"))
			return;
		
		if (lastPasteTick.get(e.getPlayer()) + 40 > MinecraftServer.currentTick) {
			e.getPlayer().sendMessage(Message.WE_ACTION_TOO_RAPID.getValue());
			e.setCancelled(true);
			return;
		}
		
		OlympaPlayerCreatif p = AccountProvider.get(e.getPlayer().getUniqueId());
		
		try {
			LocalSession session = plugin.getWorldEditManager().getSession(e.getPlayer());
			Region selection = session.getSelection(session.getSelectionWorld());
			
			Plot plot1 = plugin.getPlotsManager().getPlot(getLoc(selection.getMinimumPoint()));
			Plot plot2 = plugin.getPlotsManager().getPlot(getLoc(selection.getMaximumPoint()));
			
			//si le joueur a bien la permission de copy dans ce plot
			if (plot1 == null || !plot1.equals(plot2) || plot1.getMembers().getPlayerLevel(p) > 2) {
				
				if (session.getBlockChangeLimit() < selection.getWidth() * selection.getLength() * selection.getHeight())		
					p.setWEclipboardPlot(plot1);
				
				else {
					p.getPlayer().sendMessage(Message.WE_ACTION_TOO_BIG.getValue());
					p.setWEclipboardPlot(null);
					e.setCancelled(true);
				}
			}else {
				p.getPlayer().sendMessage(Message.INSUFFICIENT_PLOT_PERMISSION.getValue());
				p.setWEclipboardPlot(null);
				e.setCancelled(true);
			}
			
		} catch (IncompleteRegionException e1) {
			p.setWEclipboardPlot(null);
			return;
		}
		
		//if (plugin.getWorldEditManager().getSession(e.getPlayer()).getSelection((World) plugin.getWorldManager().getWorld()) != null)
	}
	
	@org.bukkit.event.EventHandler
	public void onJoin(PlayerJoinEvent e) {
		lastPasteTick.put(e.getPlayer(), MinecraftServer.currentTick);	
	}
	
	@org.bukkit.event.EventHandler
	public void onQuit(PlayerQuitEvent e) {
		lastPasteTick.remove(e.getPlayer());
		
	}
	
	@Subscribe //handle WE place block event
	public void onEditSession(EditSessionEvent e) {
		e.setExtent(new AbstractDelegateExtent(e.getExtent()) {

	        @Override
	        public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 pos, T block) throws WorldEditException {
	        	
				Actor actor = e.getActor();
				if (actor == null || !actor.isPlayer())
					return false;
				
				OlympaPlayerCreatif p = AccountProvider.get(actor.getUniqueId());
				
				//Bukkit.broadcastMessage("actor : " + p);
				
				if (p == null)
					return false;
				
				if (p.getWEclipboardPlot() == null)
					return false;
				
				lastPasteTick.put(p.getPlayer(), MinecraftServer.currentTick);
				
				//Bukkit.broadcastMessage("\nmin point" + getExtent().getMinimumPoint());
				//Bukkit.broadcastMessage("max point" + getExtent().getMaximumPoint());
				
				Plot plot = plugin.getPlotsManager().getPlot(getLoc(pos));
				
				//storedLocs.put(pos, plugin.getPlotsManager().getPlot(getLoc(pos)));

				//Bukkit.broadcastMessage("plot min : " + plotMin);
				//Bukkit.broadcastMessage("plot max : " + plotMax);
				
				//si le joueur n'a pas la perm de paste dans le plot cible ou si sa sélection a été faite dans un plot dont il n'est pas le proprio, cancel
				if (plot == null || plot.getMembers().getPlayerLevel(p) < 2 ||
						(p.getWEclipboardPlot().getMembers().getPlayerRank(p) != PlotRank.OWNER && !plot.equals(p.getWEclipboardPlot()))) 
					return false;
				
				//cancel si le joueur n'a pas la permission pour le bloc en cours de copie
				String blockName = CbCommand.getUndomainedString(block.getAsString());
				int splitIndex = blockName.indexOf("[");
				if (splitIndex >= 0)
					blockName = blockName.substring(0, splitIndex);
				
				if (plugin.getPerksManager().getKitsManager().hasPlayerPermissionFor(p, Material.getMaterial(blockName)))
					return getExtent().setBlock(pos, block);
				else
					return false;
	        }
		});
	}
	
	private Location getLoc(BlockVector3 pos) {
		return new Location(plugin.getWorldManager().getWorld(), pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
	}
	
	@Override
	public void dispatch(Object event) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return false;
	}
}
