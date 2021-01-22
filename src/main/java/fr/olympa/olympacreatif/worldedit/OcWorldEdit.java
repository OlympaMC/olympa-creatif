package fr.olympa.olympacreatif.worldedit;

import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.progressDisplay.IProgressDisplay;
import org.primesoft.asyncworldedit.api.worldedit.IAsyncEditSessionFactory;
import org.primesoft.asyncworldedit.api.worldedit.IEditSession;
import org.primesoft.asyncworldedit.api.worldedit.IThreadSafeEditSession;

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.Mask2D;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.util.eventbus.EventHandler;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OCparam;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotPerm;
import fr.olympa.olympacreatif.world.WorldManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class OcWorldEdit extends EventHandler implements IWorldEditManager {

	private boolean weEnabled = true;
	private  OlympaCreatifMain plugin;
	private WorldEdit we;
	
	public OcWorldEdit(OlympaCreatifMain plugin) {
		super(Priority.NORMAL);
		this.plugin = plugin;

		we = ((WorldEditPlugin) plugin.getServer().getPluginManager().getPlugin("WorldEdit")).getWorldEdit();
		we.getEventBus().register(this);
		
		((IAsyncWorldEdit) plugin.getServer().getPluginManager().getPlugin("AsyncWorldEdit"))
				.getProgressDisplayManager().registerProgressDisplay(new AWEProgressBar());
		
		LocalConfiguration config = we.getConfiguration();
		for (Material mat : Material.values())
			if (plugin.getPerksManager().getKitsManager().getKitOf(mat) != null)
				config.disallowedBlocks.add("minecraft:" + mat.toString().toLowerCase());
		
		OcWorldEdit instance = this;
		
		/*new BukkitRunnable() {
			
			@Override
			public void run() {
				we.getEventBus().register(instance);
				
				LocalConfiguration config = we.getConfiguration();
				for (Material mat : Material.values())
					if (plugin.getPerksManager().getKitsManager().getKitOf(mat) != null)
						config.disallowedBlocks.add("minecraft:" + mat.toString().toLowerCase());
			}
		}.runTask(plugin);*/
		
		//désactive goBrush qui ne fonctionne pas avec AWE
		if (plugin.getServer().getPluginManager().getPlugin("goBrush") != null)
			plugin.getServer().getPluginManager().disablePlugin(plugin.getServer().getPluginManager().getPlugin("goBrush"));
		
		plugin.getLogger().info("§dLoaded WorldEdit + AsyncWorldEdit.");
	}
	
	public boolean isWeEnabled() {
		return weEnabled;
	}
	
	public void setWeActivationState(boolean b) {
		weEnabled = b;
	}
	
	public void clearClipboard(Plot plot, Player p) {
		LocalSession weSession = we.getSessionManager().get(BukkitAdapter.adapt(p));
		
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

	@Override
	public void resetPlot(Player requester, Plot plot) {
		requester.sendMessage("§dLa réinitialisation de la parcelle " + plot + " a commencé. Vous ne serez pas averti de la fin de l'opération.");
		
		BlockVector3 vec1 = getBV3(plot.getPlotId().getLocation()).withY(0);
		BlockVector3 vec2 = getBV3(plot.getPlotId().getLocation()).withY(0).add(OCparam.PLOT_SIZE.get() - 1, 0, OCparam.PLOT_SIZE.get() - 1);
		
		plugin.getTask().runTaskAsynchronously(() -> {
			setLayer(new CuboidRegion(getWeWorld(), vec1, vec2), BlockTypes.BEDROCK);
			setLayer(new CuboidRegion(getWeWorld(), vec1.withY(2), vec2.withY(WorldManager.worldLevel - 1)), BlockTypes.DIRT);
			setLayer(new CuboidRegion(getWeWorld(), vec1.withY(WorldManager.worldLevel), vec2.withY(WorldManager.worldLevel)), BlockTypes.GRASS_BLOCK);
			setLayer(new CuboidRegion(getWeWorld(), vec1.withY(WorldManager.worldLevel + 1), vec2.withY(255)), BlockTypes.AIR);
		});
	}
	
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
	
	@Subscribe //cancel actions we's actions if performed out of plot
	public void onEditSession(EditSessionEvent e) {	
		if (e.getActor() == null || e.getActor().getUniqueId() == null)
			return;
		
		OlympaPlayerCreatif p = AccountProvider.get(e.getActor().getUniqueId());
		
		Plot plot = plugin.getPlotsManager().getPlot(p.getPlayer().getLocation());

		
		if (!p.hasStaffPerm(StaffPerm.BYPASS_WORLDEDIT) && (plot == null || !PlotPerm.USE_WE.has(plot, p))) {
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
		        	return isWeEnabled() && plot.getPlotId().isInPlot(pos.getBlockX(), pos.getBlockZ()) ? super.setBlock(pos, block) : false;
		        }
		    });		
	}

	private BlockVector3 getBV3(Location loc) {
		return BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}
	
	private World getWeWorld() {
		return BukkitAdapter.adapt(plugin.getWorldManager().getWorld());
	}
	
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

	
	
	@Override
	public void dispatch(Object event) throws Exception {
	}

	@Override
	public int hashCode() {
		return 1;
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}
}











