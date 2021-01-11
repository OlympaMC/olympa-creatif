package fr.olympa.olympacreatif.worldedit;

import java.util.function.BiFunction;
import java.util.logging.Level;

import org.bukkit.Material;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.eventbus.EventHandler;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotPerm;

public class WorldEditManager extends EventHandler {

	private OlympaCreatifMain plugin;
	private boolean isWePresent = false;
	private boolean isWeEnabled = false;
	public WorldEditManager(OlympaCreatifMain plugin) {
		super(Priority.NORMAL);
		
		this.plugin = plugin;
		
		//plugin.getServer().getPluginManager().registerEvents(this, plugin);

		WorldEdit.getInstance().getEventBus().register(this);
		//FaweAPI.addMaskManager(new OlympaCreatifMask());
		//Impossible de récupérer la liste des blocs dans un EditSession, impossible d'utiliser l'évent pour détecter les blocs par type... 
		IAsyncWorldEdit awe = (IAsyncWorldEdit) plugin.getServer().getPluginManager().getPlugin("AsyncWorldEdit");
		awe.getProgressDisplayManager().registerProgressDisplay(new AWEProgressBar());
		
		getWe().getEventBus().register(this);		
		
		LocalConfiguration config = WorldEdit.getInstance().getConfiguration();
		for (Material mat : Material.values())
			if (plugin.getPerksManager().getKitsManager().getKitOf(mat) != null)
				config.disallowedBlocks.add("minecraft:" + mat.toString().toLowerCase());
		plugin.getLogger().log(Level.INFO, "§dWE EventHandler registered. All kits blocks added to blacklist.");

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
		return WorldEdit.getInstance();
	}

	@Subscribe
	public void onEditSession(EditSessionEvent e) {	
		if (e.getActor() == null || e.getActor().getUniqueId() == null)
			return;
		
		OlympaPlayerCreatif p = AccountProvider.get(e.getActor().getUniqueId());
		
		Plot plot = plugin.getPlotsManager().getPlot(p.getPlayer().getLocation());

		/*Material mat = BukkitAdapter.adapt(e.getExtent().getBlock(BlockVector3.at(x, y, z)).getBlockType());
		KitType kit = plugin.getPerksManager().getKitsManager().getKitOf(mat);*/
		
		if (!p.hasStaffPerm(StaffPerm.BYPASS_WORLDEDIT) && (plot == null || !PlotPerm.USE_WE.has(plot, p))) {
			e.setExtent(new AbstractDelegateExtent(e.getExtent()) {
		        @Override
		        public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 pos, T block) throws WorldEditException {
		        	return false;
		        }
		    });
			
			p.getPlayer().sendMessage(OCmsg.WE_ERR_INSUFFICIENT_PERMISSION.getValue(plot));
		}
		else
			e.setExtent(new AbstractDelegateExtent(e.getExtent()) {
		        @Override
		        public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 pos, T block) throws WorldEditException {
		        	return plot.getPlotId().isInPlot(pos.getBlockX(), pos.getBlockZ()) ? super.setBlock(pos, block) : false;
		        }
		    });		
	}

	@Override
	public void dispatch(Object event) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int hashCode() {
		return 1;
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}
	
	@SuppressWarnings({ "unused", "rawtypes" })
	private AbstractDelegateExtent getExtent(Plot plot, Extent extent, BiFunction<BlockVector3, BlockStateHolder, Boolean> function) {
		return new AbstractDelegateExtent(extent) {
	        @Override
	        public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 pos, T block) throws WorldEditException {
	        	return plot.getPlotId().isInPlot(pos.getBlockX(), pos.getBlockZ()) ? super.setBlock(pos, block) : false;
	        };
		};
	}
	
	/*
	//class model: https://github.com/IntellectualSites/FastAsyncWorldEdit/blob/d4c0ab37909f1b473feeb726c0d158f83da86a5a/worldedit-bukkit/src/main/java/com/boydti/fawe/bukkit/regions/GriefPreventionFeature.java#L43
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
                	//return PlotPerm.USE_WE.has(plot, p);
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
	}*/
	
	/*@org.bukkit.event.EventHandler //cancel copy si joueur essaie de copier dans un plot qui n'est pas à lui
	public void onCopyCmd(PlayerCommandPreprocessEvent e) {
		
		OlympaPlayerCreatif p = ((OlympaPlayerCreatif)AccountProvider.get(e.getPlayer().getUniqueId()));
		
		if (p == null || !p.hasStaffPerm(StaffPerm.BYPASS_WORLDEDIT) && e.getMessage().contains("/schem")) {
			p.getPlayer().sendMessage(Message.WE_ERR_SCHEM_CMD_DISABLED.getValue());
			e.setCancelled(true);
			return;
		}
	}*/
		
					
		/*Bukkit.broadcastMessage("TRY TO SET EXTENT");
		Extent extent = e.getExtent();
		Region region = new CuboidRegion(e.getExtent().getMinimumPoint(), e.getExtent().getMaximumPoint());
		//Filter filter = new BlockMaskBuilder().add(BlockTypes.STONE).build(extent).toFilter(new OcFilter());
		//EditSession session = (EditSession) extent;
		//session.setMask(new BlockMaskBuilder().add(BlockTypes.STONE).build(extent));
		//e.setExtent(session.getExtent());
		//extent.
		extent.apply(region, new OcFilter(), true);
		e.setExtent(extent);
		Bukkit.broadcastMessage("EXTENT SET");*/
	
	/*
	private class OcFilter implements Filter {

		public final void applyBlock(FilterBlock block) {
			if (block.getBlock().equals(BlockTypes.STONE.getDefaultState()))
				block.setBlock(BlockTypes.AIR.getDefaultState());
		}
	}
	
	private class OlympaCreatifFaweExtent extends AbstractDelegateExtent {
		private OlympaPlayerCreatif p;
		
		public OlympaCreatifFaweExtent(Extent ex, Actor act) {
			super(ex);
			Bukkit.broadcastMessage("CREATION OF EXTENT");
			p = (BukkitAdapter.adapt(act) instanceof Player) ? AccountProvider.get(((Player) BukkitAdapter.adapt(act)).getUniqueId()) : null;
		}

        @Override
        public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 pos, T block) throws WorldEditException {
        	System.out.println("Get perm for block " + block + ", " + BukkitAdapter.adapt(block) + ", " + BukkitAdapter.adapt(block).getMaterial());
        	
            if (p == null || !p.hasKit(plugin.getPerksManager().getKitsManager().getKitOf(BukkitAdapter.adapt(block).getMaterial())))
            	return false;
            
            return extent.setBlock(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), block);
        }
	}*/
}

