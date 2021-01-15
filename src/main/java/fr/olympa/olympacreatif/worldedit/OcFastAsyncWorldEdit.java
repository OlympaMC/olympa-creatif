package fr.olympa.olympacreatif.worldedit;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.bukkit.regions.BukkitMaskManager;
import com.boydti.fawe.regions.FaweMask;
import com.boydti.fawe.regions.FaweMaskManager.MaskType;
import com.boydti.fawe.regions.general.CuboidRegionFilter;
import com.boydti.fawe.regions.general.RegionFilter;
import com.boydti.fawe.util.EditSessionBuilder;
import com.boydti.fawe.util.TaskManager;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockTypes;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OCparam;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotId;
import fr.olympa.olympacreatif.plot.PlotPerm;
import fr.olympa.olympacreatif.world.WorldManager;

public class OcFastAsyncWorldEdit implements IWorldEditManager {

	private boolean isEnabled = true;
	private OlympaCreatifMain plugin;
	private Set<PlotId> resetingPlots = new HashSet<PlotId>();
	
	public OcFastAsyncWorldEdit(OlympaCreatifMain pl) {
		this.plugin = pl;
		
		FaweAPI.addMaskManager(new OlympaCreatifMask());
		
		LocalConfiguration config = WorldEdit.getInstance().getConfiguration();
		for (Material mat : Material.values())
			if (plugin.getPerksManager().getKitsManager().getKitOf(mat) != null)
				config.disallowedBlocks.add("minecraft:" + mat.toString().toLowerCase());
		
		plugin.getLogger().info("§dLoaded FastAsyncWorldEdit.");
	}

	@Override
	public boolean isWeEnabled() {
		return isEnabled;
	}

	@Override
	public void setWeActivationState(boolean b) {
		isEnabled = b;
	}

	@Override
	public void resetPlot(Player requester, Plot plot) {
		if (resetingPlots.contains(plot.getPlotId()))
			return;
		
		resetingPlots.add(plot.getPlotId());
		plugin.getTask().runTaskAsynchronously(() -> {
			
			int xMin = plot.getPlotId().getLocation().getBlockX();
			int zMin = plot.getPlotId().getLocation().getBlockZ();
			int xMax = xMin + OCparam.PLOT_SIZE.get() - 1;
			int zMax = zMin + OCparam.PLOT_SIZE.get() - 1;

			try (EditSession session = new EditSession(new EditSessionBuilder(BukkitAdapter.adapt(plugin.getWorldManager().getWorld())))) {
				requester.sendMessage("§dLa réinitialisation de la parcelle " + plot + " a commencé.");
				for (int x = xMin ; x <= xMax ; x++)
					for (int z = zMin ; z <= zMax ; z++)
						session.setBlock(x, 0, z, BlockTypes.BEDROCK);

				for (int x = xMin ; x <= xMax ; x++)
					for (int z = zMin ; z <= zMax ; z++)
						for (int y = 1 ; y < WorldManager.worldLevel ; y++)
							session.setBlock(x, y, z, BlockTypes.DIRT);

				for (int x = xMin ; x <= xMax ; x++)
					for (int z = zMin ; z <= zMax ; z++)
						session.setBlock(x, WorldManager.worldLevel, z, BlockTypes.GRASS_BLOCK);

				for (int x = xMin ; x <= xMax ; x++)
					for (int z = zMin ; z <= zMax ; z++)
						for (int y = WorldManager.worldLevel + 1 ; y < 256 ; y++)
							session.setBlock(x, y, z, BlockTypes.AIR);							
			}
			
			requester.sendMessage("§dLa réinitialisation de la parcelle " + plot + " est terminée !");
			
			resetingPlots.remove(plot.getPlotId());
		});
	}

	@Override
	public void clearClipboard(Plot plot, Player p) {
		LocalSession weSession = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(p));
		
		if (weSession != null) {
			//clear clipboard si le joueur n'en est pas le proprio
			if (PlotPerm.BYPASS_EXIT_CLIPBOARD_CLEAR.has(plot, AccountProvider.get(p.getUniqueId())))
				weSession.setClipboard(null);
			
			World world = weSession.getSelectionWorld();
			
			//reset worldedit positions
			if (world != null && weSession.getRegionSelector(world) != null)
				weSession.getRegionSelector(world).clear();	
		}
	}

	private BlockVector3 getBV3(Location loc) {
		return BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}
	
	private World getWeWorld() {
		return BukkitAdapter.adapt(plugin.getWorldManager().getWorld());
	}
	private class OlympaCreatifMask extends BukkitMaskManager {

		public OlympaCreatifMask() {
			super(plugin.getName());
		}
		
	    @Override
	    public RegionFilter getFilter(String w) {
	    	return new OlympaCreatifRegionFilter();
	    }
		
	    @Override
	    public FaweMask getMask(final com.sk89q.worldedit.entity.Player wePlayer, MaskType type) {
	    	
	    	final OlympaPlayerCreatif p = AccountProvider.get(BukkitAdapter.adapt(wePlayer).getUniqueId());
	    	final Plot plot = plugin.getPlotsManager().getPlot(p.getPlayer().getLocation());
	    	
	    	if (plot == null || p == null || !PlotPerm.USE_WE.has(plot, p)) {
	    		p.getPlayer().sendMessage(OCmsg.WE_ERR_INSUFFICIENT_PERMISSION.getValue(plot));
	    		return null;
	    	}

	    	BlockVector3 v1 = BlockVector3.at(plot.getPlotId().getLocation().getBlockX(), 0, 
	    			plot.getPlotId().getLocation().getBlockZ());
	    	BlockVector3 v2 = BlockVector3.at(plot.getPlotId().getLocation().getBlockX() + OCparam.PLOT_SIZE.get() - 1, 256, 
	    			plot.getPlotId().getLocation().getBlockZ() + OCparam.PLOT_SIZE.get() - 1);
	    	
            return new FaweMask(new CuboidRegion(v1, v2)) {
            	
                @Override
                public boolean isValid(com.sk89q.worldedit.entity.Player wePlayer, MaskType type) {
                	return isWeEnabled() ? PlotPerm.USE_WE.has(plot, p) : false;
                }
            };
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
						BlockVector2.at(plot.getPlotId().getLocation().getBlockX() + OCparam.PLOT_SIZE.get() - 1, 
						plot.getPlotId().getLocation().getBlockZ() + OCparam.PLOT_SIZE.get() - 1));
		}
	}
}
