package fr.olympa.olympacreatif.worldedit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
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
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;

public class WorldEditListener extends EventHandler implements Listener {

	private OlympaCreatifMain plugin;
	
	public WorldEditListener(OlympaCreatifMain plugin) {
		super(Priority.NORMAL);
		
		this.plugin = plugin;
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@org.bukkit.event.EventHandler //cancel copy si joueur essaie de copier dans un plot qui n'est pas à lui
	public void onCopyCmd(PlayerCommandPreprocessEvent e) {		
		if (!e.getMessage().contains("/copy") || plugin.getWorldEditManager() == null)
			return;
		
		OlympaPlayerCreatif p = ((OlympaPlayerCreatif)AccountProvider.get(e.getPlayer().getUniqueId()));
		
		LocalSession session = plugin.getWorldEditManager().getSession(e.getPlayer());
		
		try {
			Region region = session.getSelection(session.getSelectionWorld());
			long size = region.getHeight() * region.getLength() * region.getWidth();
			
			//return si la sélection est trop grosse
			if (size <= 0 || size > session.getBlockChangeLimit()){
				e.setCancelled(true);
				e.getPlayer().sendMessage(Message.WE_ERR_SELECTION_TOO_BIG.getValue());
				return;
			}

			Plot plot1 = plugin.getPlotsManager().getPlot(blockVectorToLocation(region.getMinimumPoint()));
			Plot plot2 = plugin.getPlotsManager().getPlot(blockVectorToLocation(region.getMaximumPoint()));
			
			//return si le joueur n'a pas la perm de copy sur ce plot
			if (!p.hasStaffPerm(StaffPerm.BYPASS_WORLDEDIT))
				if (plot1 == null || plot2 == null || !plot1.equals(plot2) || 
						!plot1.equals(plugin.getPlotsManager().getPlot(p.getPlayer().getLocation())) || 
						plot1.getMembers().getPlayerRank(p) == PlotRank.VISITOR) {
					
					e.setCancelled(true);
					e.getPlayer().sendMessage(Message.WE_ERR_NULL_PLOT.getValue());
				}
		} catch (IncompleteRegionException e1) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.WE_ERR_SELECTION_TOO_BIG.getValue());
		}
	}

	private Location blockVectorToLocation(BlockVector3 vector) {
		return new Location(plugin.getWorldManager().getWorld(), vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
	}
	
	@Subscribe //handle WE place block event
	public void onEditSessionEvent(EditSessionEvent e) {
		e.setExtent(new AbstractDelegateExtent(e.getExtent()) {

	        @Override
	        public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 pos, T block) throws WorldEditException {
	        	if (!plugin.isWeEnabled())
	        		return false;
	        	
				Actor actor = e.getActor();
				if (actor == null || !actor.isPlayer())
					return false;
				
				OlympaPlayerCreatif p = AccountProvider.get(actor.getUniqueId());
				
				//Bukkit.broadcastMessage("actor : " + p);
				
				if (p == null)
					return false;
				
				//lastPasteTick.put(p.getPlayer(), MinecraftServer.currentTick);
				
				//place block si le joueur a la perm bypass worldedit
				if (p.hasStaffPerm(StaffPerm.BYPASS_WORLDEDIT))
					return getExtent().setBlock(pos, block);
				
				
				Plot plot = plugin.getPlotsManager().getPlot(getLoc(pos));
				
				//si le joueur n'a pas la perm de paste dans le plot cible, cancel paste
				if (plot == null || plot.getMembers().getPlayerLevel(p) < 2) 
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
