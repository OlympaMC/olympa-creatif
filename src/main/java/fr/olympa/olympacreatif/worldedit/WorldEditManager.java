package fr.olympa.olympacreatif.worldedit;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.boydti.fawe.Fawe;
import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.bukkit.regions.BukkitMaskManager;
import com.boydti.fawe.regions.FaweMask;
import com.boydti.fawe.regions.FaweMaskManager;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
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
import fr.olympa.olympacreatif.world.WorldManager;

public class WorldEditManager extends EventHandler implements Listener {

	private OlympaCreatifMain plugin;
	private boolean isWePresent = false;
	private boolean isWeEnabled = false;
	public WorldEditManager(OlympaCreatifMain plugin) {
		super(Priority.NORMAL);
		
		this.plugin = plugin;
		
		if (plugin.getServer().getPluginManager().getPlugin("FastAsyncWorldEdit") == null)
			return;
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
		FaweAPI.addMaskManager(new FaweMaskHook());
		plugin.getLogger().log(Level.INFO, "§aCustom FAWE Mask Manager has been loaded.");

		isWePresent = true;
		isWeEnabled = true;

	}
	
	public boolean isWeEnabled() {
		return isWePresent && isWeEnabled;
	}
	
	public void toggleWeActivation() {
		isWeEnabled = isWePresent && !isWeEnabled;
	}
	
	public WorldEdit getWe() {
		return Fawe.get().getWorldEdit();
	}
	
	//class model: https://github.com/IntellectualSites/FastAsyncWorldEdit/blob/d4c0ab37909f1b473feeb726c0d158f83da86a5a/worldedit-bukkit/src/main/java/com/boydti/fawe/bukkit/regions/GriefPreventionFeature.java#L43
	private class FaweMaskHook extends BukkitMaskManager implements Listener {

		public FaweMaskHook() {
			super(plugin.getName());
		}

	    @Override
	    public FaweMask getMask(final com.sk89q.worldedit.entity.Player wePlayer, MaskType type) {
	    	OlympaPlayerCreatif p = AccountProvider.get(BukkitAdapter.adapt(wePlayer).getUniqueId());
	    	
	    	Plot plot = plugin.getPlotsManager().getPlot(p.getPlayer().getLocation());
	    	
	    	if (plot == null || plot.getMembers().getPlayerLevel(p) < 3) {
	    		p.getPlayer().sendMessage(Message.WE_ERR_NULL_PLOT.getValue(plot));
	    		return null;	
	    	} else if (!isWeEnabled) {
	    		p.getPlayer().sendMessage("§dPour des raisons de sécurité, WorldEdit a été désactivé temporairement.");
	    		return null;
	    	}
	    		
	            /*return new FaweMask(new CuboidRegion(
	            		BlockVector3.at(p.getPlayer().getLocation().getBlockX(), p.getPlayer().getLocation().getBlockY(), p.getPlayer().getLocation().getBlockZ()), 
	            		BlockVector3.at(p.getPlayer().getLocation().getBlockX(), p.getPlayer().getLocation().getBlockY(), p.getPlayer().getLocation().getBlockZ()))) {

	                @Override
	                public boolean isValid(com.sk89q.worldedit.entity.Player wePlayer, MaskType type) {
	                	return false;
	                }
	    		};*/

	    	BlockVector3 v1 = BlockVector3.at(plot.getPlotId().getLocation().getBlockX(), 0, plot.getPlotId().getLocation().getBlockZ());
	    	BlockVector3 v2 = BlockVector3.at(plot.getPlotId().getLocation().getBlockX() + WorldManager.plotSize - 1, 255, 
	    			plot.getPlotId().getLocation().getBlockZ() + WorldManager.plotSize - 1);

            return new FaweMask(new CuboidRegion(v1, v2)) {

                @Override
                public boolean isValid(com.sk89q.worldedit.entity.Player wePlayer, MaskType type) {
                	//return plot.getMembers().getPlayerLevel(p) >= 3 /*|| p.hasStaffPerm(StaffPerm.BYPASS_WORLDEDIT)*/;
                	return true;
                }
            };
	    }
	}
	
	@org.bukkit.event.EventHandler //cancel copy si joueur essaie de copier dans un plot qui n'est pas à lui
	public void onCopyCmd(PlayerCommandPreprocessEvent e) {
		if (!e.getMessage().contains("/copy") || plugin.getWEManager() == null)
			return;
		
		OlympaPlayerCreatif p = ((OlympaPlayerCreatif)AccountProvider.get(e.getPlayer().getUniqueId()));
		
		LocalSession session = getWe().getSessionManager().get(BukkitAdapter.adapt(e.getPlayer()));
		
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
	
	/*
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
	}*/
	
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
