package fr.olympa.olympacreatif.worldedit;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.boydti.fawe.Fawe;
import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.bukkit.regions.BukkitMaskManager;
import com.boydti.fawe.regions.FaweMask;
import com.boydti.fawe.regions.general.CuboidRegionFilter;
import com.boydti.fawe.regions.general.RegionFilter;
import com.boydti.fawe.util.TaskManager;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotPerm;
import fr.olympa.olympacreatif.world.WorldManager;

public class WorldEditManager implements Listener {

	private OlympaCreatifMain plugin;
	private boolean isWePresent = false;
	private boolean isWeEnabled = false;
	public WorldEditManager(OlympaCreatifMain plugin) {
		//super(Priority.NORMAL);
		
		this.plugin = plugin;
		
		if (plugin.getServer().getPluginManager().getPlugin("FastAsyncWorldEdit") == null)
			return;
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
		FaweAPI.addMaskManager(new OlympaCreatifMask());
		
		plugin.getLogger().log(Level.INFO, "§aCustom FAWE Mask Manager has been loaded. Currently loaded: " + FaweAPI.getMaskManagers().size());

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
	private class OlympaCreatifMask extends BukkitMaskManager implements Listener {

		public OlympaCreatifMask() {
			super(plugin.getName());
			plugin.getLogger().log(Level.INFO, "Generating FAWE Mask...");
		}
		@Override
		public boolean isExclusive() {
			return true; 
		}
		
	    @Override
	    public FaweMask getMask(final com.sk89q.worldedit.entity.Player wePlayer, MaskType type) {OlympaPlayerCreatif p = AccountProvider.get(BukkitAdapter.adapt(wePlayer).getUniqueId());
	    	
	    	final Plot plot = plugin.getPlotsManager().getPlot(p.getPlayer().getLocation());
	    	
	    	if (plot == null || !PlotPerm.USE_WE.has(plot, p)) {
	    		p.getPlayer().sendMessage(Message.WE_ERR_INSUFFICIENT_PERMISSION.getValue(plot));
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

	    	BlockVector3 v1 = BlockVector3.at(plot.getPlotId().getLocation().getBlockX(), 0, 
	    			plot.getPlotId().getLocation().getBlockZ());
	    	BlockVector3 v2 = BlockVector3.at(plot.getPlotId().getLocation().getBlockX() + WorldManager.plotSize - 1, 256, 
	    			plot.getPlotId().getLocation().getBlockZ() + WorldManager.plotSize - 1);

	    	Bukkit.broadcastMessage("[DEBUG] MASK for " + wePlayer.getName() + " in " + new CuboidRegion(v1, v2) + " : " + PlotPerm.USE_WE.has(plot, p));
	    	
            return new FaweMask(new CuboidRegion(v1, v2)) {
            	
                @Override
                public boolean isValid(com.sk89q.worldedit.entity.Player wePlayer, MaskType type) {
                	return PlotPerm.USE_WE.has(plot, p);
                }
            };
	    }
	    
	    @Override
	    public RegionFilter getFilter(String w) {
	    	return new OlympaCreatifRegionFilter();
	    }
	}
	
	public class OlympaCreatifRegionFilter extends CuboidRegionFilter{
		Collection<Plot> plots;
		
		public OlympaCreatifRegionFilter() {
		    this.plots = (Collection<Plot>)TaskManager.IMP.sync(() -> 
	        new ArrayDeque<Plot>(plugin.getPlotsManager().getPlots()));
		}
		
		@Override
		public void calculateRegions() {			
			for (Plot plot : plots)
				add(BlockVector2.at(plot.getPlotId().getLocation().getBlockX(), plot.getPlotId().getLocation().getBlockZ()), 
						BlockVector2.at(plot.getPlotId().getLocation().getBlockX() + WorldManager.plotSize - 1, 
						plot.getPlotId().getLocation().getBlockZ() + WorldManager.plotSize - 1));
		}
	}
	
	@org.bukkit.event.EventHandler //cancel copy si joueur essaie de copier dans un plot qui n'est pas à lui
	public void onCopyCmd(PlayerCommandPreprocessEvent e) {
		
		OlympaPlayerCreatif p = ((OlympaPlayerCreatif)AccountProvider.get(e.getPlayer().getUniqueId()));
		
		if (p == null || !p.hasStaffPerm(StaffPerm.BYPASS_WORLDEDIT) && e.getMessage().contains("/schem")) {
			p.getPlayer().sendMessage(Message.WE_ERR_SCHEM_CMD_DISABLED.getValue());
			e.setCancelled(true);
			return;
		}
	}
}
