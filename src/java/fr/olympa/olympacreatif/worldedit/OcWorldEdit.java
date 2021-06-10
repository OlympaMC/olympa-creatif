package fr.olympa.olympacreatif.worldedit;

import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.progressDisplay.IProgressDisplay;

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.eventbus.EventHandler;
import com.sk89q.worldedit.util.eventbus.EventHandler.Priority;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import fr.olympa.api.common.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.data.PermissionsManager.ComponentCreatif;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotPerm;

public class OcWorldEdit extends AWorldEditManager {

	private WorldEdit we;
	private IAsyncWorldEdit awe;
	
	public OcWorldEdit(OlympaCreatifMain plugin) {
		super(plugin);

		we = ((WorldEditPlugin) plugin.getServer().getPluginManager().getPlugin("WorldEdit")).getWorldEdit();
		registerEventHandler();
		
		awe = ((IAsyncWorldEdit) plugin.getServer().getPluginManager().getPlugin("AsyncWorldEdit"));
		awe.getProgressDisplayManager().registerProgressDisplay(new AWEProgressBar());
		
		LocalConfiguration config = we.getConfiguration();
		for (Material mat : Material.values())
			if (plugin.getPerksManager().getKitsManager().getKitOf(mat) != null)
				config.disallowedBlocks.add("minecraft:" + mat.toString().toLowerCase());
		
		//désactive goBrush qui ne fonctionne pas avec AWE
		if (plugin.getServer().getPluginManager().getPlugin("goBrush") != null)
			plugin.getServer().getPluginManager().disablePlugin(plugin.getServer().getPluginManager().getPlugin("goBrush"));
		
		plugin.getLogger().info("§dLoaded WorldEdit + AsyncWorldEdit.");
	}
	
	public void clearClipboard(Plot plot, Player p) {
		LocalSession weSession = we.getSessionManager().get(BukkitAdapter.adapt(p));
		
		if (weSession != null) {
			//clear clipboard si le joueur n'en est pas le proprio
			if (PlotPerm.BYPASS_EXIT_CLIPBOARD_CLEAR.has(plot, AccountProvider.getter().get(p.getUniqueId())))
				weSession.setClipboard(null);
			
			World world = weSession.getSelectionWorld();
			
			//reset worldedit positions
			if (world != null && weSession.getRegionSelector(world) != null)
				weSession.getRegionSelector(world).clear();	
		}
	}

	/*
	@Override
	public void resetPlot(Player requester, Plot plot) {
		throw new UnsupportedOperationException("Impossible de régénérer une parcelle avec AsyncWorldEdit.");
	}*/

	/*
	@Override
	public void resetPlot(Player requester, Plot plot) {
		requester.sendMessage("§dLa réinitialisation de la parcelle " + plot + " a commencé. Vous ne serez pas averti de la fin de l'opération.");

		int maxX = OCparam.PLOT_SIZE.get() / 16;
		int maxZ = OCparam.PLOT_SIZE.get() / 16;
		
		plugin.getWorldManager().getWorld().getChunkAtAsync(plot.getPlotId().getLocation(), true, 
				ch -> plugin.getTask().runTaskAsynchronously(() -> resetChunk(plot.getPlotId(), ch)));
	}
	
	private void resetChunk(PlotId plot, Chunk toReset) {
		IThreadSafeEditSession es = ((IAsyncEditSessionFactory) we.getEditSessionFactory())
				.getThreadSafeEditSession(getWeWorld(), -1);
		es.setFastMode(true);
		
		es.getBlockPlacer().addListener(new IBlockPlacerListener() {
			
			private IJobEntry job = null;
			
			@Override
			public void jobRemoved(IJobEntry j) {
				//if (job != null && job.getJobId() == j.getJobId())
					System.out.println("FIN DU SET DU CHUNK " + job);
			}
			
			@Override
			public void jobAdded(IJobEntry j) {
				System.out.println("AJOUT DU JOB " + job);
				job = job == null ?  j : job;
			}
		});
		
		es.setMask(new Mask() {
			
			@Override
			public boolean test(BlockVector3 arg0) {
				return !es.getBlock(arg0).getBlockType().equals(BlockTypes.AIR);
			}
			
			@Override
			public Mask copy() {return null;}
		});
		
		//es.regenerateChunk(toReset.getX(), toReset.getZ(), BiomeTypes.PLAINS, plugin.getWorldManager().getWorld().getSeed());

		
		for (int x = toReset.getX() * 16 ; x < (toReset.getX() + 1) * 16 ; x++)
			for (int z = toReset.getZ() * 16 ; z < (toReset.getZ() + 1) * 16 ; z++) {
				es.smartSetBlock(BlockVector3.at(x, 0, z), BlockTypes.BEDROCK.getDefaultState());
				es.smartSetBlock(BlockVector3.at(x, WorldManager.worldLevel, z), BlockTypes.GRASS_BLOCK.getDefaultState());
				for (int y = 1 ; y < WorldManager.worldLevel ; y++) 
					es.smartSetBlock(BlockVector3.at(x, y, z), BlockTypes.DIRT.getDefaultState());
				for (int y = WorldManager.worldLevel + 1 ; y <= 256 ; y++) 
					es.smartSetBlock(BlockVector3.at(x, y, z), BlockTypes.AIR.getDefaultState());
			}
	}*/
	
	/*
	@SuppressWarnings("deprecation")
	private void setLayer(final CuboidRegion reg, final BlockType block) {		
		try {
			IThreadSafeEditSession es = ((IAsyncEditSessionFactory) we.getEditSessionFactory())
					.getThreadSafeEditSession(getWeWorld(), -1);
			es.setFastMode(true);
			
			es.setMask(new Mask() {
				@Override
				public boolean test(BlockVector3 vector) {
					return !es.getBlock(vector).getBlockType().getMaterial().equals(block.getMaterial());					
				}
				@Override
				public Mask2D toMask2D() {return null;}
				@Override
				public Mask copy() {
					return this;
				}
			});
			
			reg.forEach(loc -> es.setBlock(loc, block.getDefaultState()));
			es.flushSession();
			es.close();
		} catch (WorldEditException e1) {
			e1.printStackTrace();
		}
	}
	
	private World getWeWorld() {
		return BukkitAdapter.adapt(plugin.getWorldManager().getWorld());
	}*/
	
	private class AWEProgressBar implements IProgressDisplay {

		@Override
		public void disableMessage(IPlayerEntry arg0) {
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public void setMessage(IPlayerEntry player, int jobsCount, int queuedBlocks, int maxQueuedBlocks, double timeLeft, double placingSpeed, double percentage) {
			final Player p = Bukkit.getPlayer(player.getUUID());
			
			if (p == null)
				return;
			//p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§6Progression WorldEdit : §e" + new DecimalFormat("#.##").format(percentage) + "%"));
			p.sendActionBar("§6Progression WorldEdit : §e" + new DecimalFormat("#.##").format(percentage) + "%");
		}
	}

	
	
	private void registerEventHandler() {
		we.getEventBus().register(new EventHandler(Priority.NORMAL) {
			
			@Subscribe //cancel actions we's actions if performed out of plot
			public void onEditSession(EditSessionEvent e) {	
				if (e.getActor() == null || e.getActor().getUniqueId() == null)
					return;
				
				OlympaPlayerCreatif p = AccountProvider.getter().get(e.getActor().getUniqueId());
				
				Plot plot = plugin.getPlotsManager().getPlot(p.getPlayer().getLocation());

				
				if (!p.hasStaffPerm(StaffPerm.WORLDEDIT) && (plot == null || !PlotPerm.USE_WE.has(plot, p))) {
					e.setExtent(new AbstractDelegateExtent(e.getExtent()) {
				        @Override
				        public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 pos, T block) throws WorldEditException {
				        	return false;
				        }
				    });
					OCmsg.WE_ERR_INSUFFICIENT_PERMISSION.send(p);
				}
				else
					e.setExtent(new AbstractDelegateExtent(e.getExtent()) {
				        @Override
				        public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 pos, T block) throws WorldEditException {
				    		//plugin.getPerksManager().getKitsManager().getKitOf(BukkitAdapter.adapt(block.getBlockType()));
				        	return ComponentCreatif.WORLDEDIT.isActivated() && 
				        			plot.getId().isInPlot(pos.getBlockX(), pos.getBlockZ()) ? super.setBlock(pos, block) : false;
				        }
				    });		
			}
			
			
			@Override
			public int hashCode() {
				return 1;
			}
			
			@Override
			public boolean equals(Object arg0) {
				return this == arg0;
			}
			
			@Override
			public void dispatch(Object arg0) throws Exception {
			}
		});
	}
}











