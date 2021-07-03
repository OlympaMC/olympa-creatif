package fr.olympa.olympacreatif.worldedit;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.fastasyncworldedit.bukkit.regions.BukkitMaskManager;
import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.FaweAPI;
import com.fastasyncworldedit.core.regions.FaweMask;
import com.fastasyncworldedit.core.regions.general.CuboidRegionFilter;
import com.fastasyncworldedit.core.regions.general.RegionFilter;
import com.fastasyncworldedit.core.util.EditSessionBuilder;
import com.fastasyncworldedit.core.util.TaskManager;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.util.eventbus.EventHandler;
import com.sk89q.worldedit.util.eventbus.EventHandler.Priority;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;

import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OCparam;
import fr.olympa.olympacreatif.data.OcPermissions;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.data.PermissionsManager.ComponentCreatif;
import fr.olympa.olympacreatif.perks.KitsManager.KitType;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotPerm;
import fr.olympa.olympacreatif.utils.OtherUtils;
import fr.olympa.olympacreatif.world.WorldManager;

public class OcFastAsyncWorldEdit extends AWorldEditManager {

	public OcFastAsyncWorldEdit(OlympaCreatifMain pl) {
		super(pl);

		FaweAPI.addMaskManager(new OlympaCreatifMask());

		/*LocalConfiguration config = WorldEdit.getInstance().getConfiguration();
		for (Material mat : Material.values())
			if (plugin.getPerksManager().getKitsManager().getKitOf(mat) != null)
				config.disallowedBlocks.add("minecraft:" + mat.toString().toLowerCase());*/

		registerAntiCommandblockEditSession();

		plugin.getLogger().info("§dLoaded FastAsyncWorldEdit.");
	}

	@Override
	public boolean resetPlot(OlympaPlayerCreatif requester, final Plot plot) {
		resetingPlots.put(plot.getId(), 0);
		plugin.getTask().runTaskAsynchronously(() -> {

			int xMin = plot.getId().getLocation().getBlockX();
			int zMin = plot.getId().getLocation().getBlockZ();
			int xMax = xMin + OCparam.PLOT_SIZE.get() - 1;
			int zMax = zMin + OCparam.PLOT_SIZE.get() - 1;

			try (EditSession session = new EditSession(new EditSessionBuilder(BukkitAdapter.adapt(plugin.getWorldManager().getWorld())))) {
				OCmsg.PLOT_RESET_START.send(requester, plot);

				for (int x = xMin; x <= xMax; x++)
					for (int z = zMin; z <= zMax; z++)
						session.setBlock(x, 0, z, BlockTypes.BEDROCK);

				for (int x = xMin; x <= xMax; x++)
					for (int z = zMin; z <= zMax; z++)
						for (int y = 1; y < WorldManager.worldLevel; y++)
							session.setBlock(x, y, z, BlockTypes.DIRT);

				for (int x = xMin; x <= xMax; x++)
					for (int z = zMin; z <= zMax; z++)
						session.setBlock(x, WorldManager.worldLevel, z, BlockTypes.GRASS_BLOCK);

				for (int x = xMin; x <= xMax; x++)
					for (int z = zMin; z <= zMax; z++)
						for (int y = WorldManager.worldLevel + 1; y < 256; y++)
							session.setBlock(x, y, z, BlockTypes.AIR);

				session.flushQueue();
				session.close();
			}

			//Prefix.DEFAULT.sendMessage(requester, "§dLa réinitialisation de la parcelle %s est terminée !", plot);

			plot.getCbData().reloadAllCommandBlocks(true);
			OCmsg.PLOT_RESET_END.send(requester, plot);
			plugin.getTask().runTaskLater(() -> resetingPlots.remove(plot.getId()), 20 * 60 * 60);
		});
		return true;
	}

	@Override
	public void clearClipboard(Plot plot, Player p) {
		OlympaPlayerCreatif pc = AccountProviderAPI.getter().get(p.getUniqueId());
		if (pc.hasStaffPerm(StaffPerm.WORLDEDIT))
			return;

		LocalSession weSession = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(p));

		if (weSession != null) {
			//clear clipboard si le joueur n'en est pas le proprio
			if (!PlotPerm.BYPASS_EXIT_CLIPBOARD_CLEAR.has(plot, pc))
				weSession.setClipboard(null);

			World world = weSession.getSelectionWorld();

			//reset worldedit positions
			if (world != null && weSession.getRegionSelector(world) != null)
				weSession.getRegionSelector(world).clear();
		}
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
		public FaweMask getMask(final com.sk89q.worldedit.entity.Player wePlayerSuper, MaskType typeSuper) {

			final OlympaPlayerCreatif p = AccountProviderAPI.getter().get(BukkitAdapter.adapt(wePlayerSuper).getUniqueId());
			final Plot plot = plugin.getPlotsManager().getPlot(((Player) p.getPlayer()).getLocation());

			if (plot == null || p == null || !PlotPerm.USE_WE.has(plot, p) || !ComponentCreatif.WORLDEDIT.isActivated() || isReseting(plot)) {
				OCmsg.WE_ERR_INSUFFICIENT_PERMISSION.send(p);
				return null;
			}

			return new FaweMask(getPlotRegion(plot)) {

				@Override
				public boolean isValid(com.sk89q.worldedit.entity.Player wePlayer, MaskType type) {
					/*if (wePlayer.getSession() != null) {
						System.out.println("DETECTED MASK WITH " + wePlayer.getSession().getSelection(wePlayer.getSession().getSelectionWorld()).getMinimumPoint() + " AND " + wePlayer.getSession().getSelection(wePlayer.getSession().getSelectionWorld()));

						BlockVector3 min = wePlayer.getSession().getSelection(wePlayer.getSession().getSelectionWorld()).getMinimumPoint();
						BlockVector3 max = wePlayer.getSession().getSelection(wePlayer.getSession().getSelectionWorld()).getMaximumPoint();

						for (int x = min.getX() ; x <= max.getX() ; x++)
							for (int y = min.getY() ; y <= max.getY() ; y++)
					    		for (int z = min.getZ() ; z <= max.getZ() ; z++)
					    			System.out.println(plugin.getWorldManager().getWorld().getBlockAt(x, y, z));
					}*/

					return PlotPerm.USE_WE.has(plot, AccountProviderAPI.getter().get(BukkitAdapter.adapt(wePlayer).getUniqueId())) && !isReseting(plot);
				}
			};
		}
	}

	private CuboidRegion getPlotRegion(Plot plot) {
		BlockVector3 v1 = BlockVector3.at(plot.getId().getLocation().getBlockX(), 0,
				plot.getId().getLocation().getBlockZ());
		BlockVector3 v2 = BlockVector3.at(plot.getId().getLocation().getBlockX() + OCparam.PLOT_SIZE.get() - 1, 256,
				plot.getId().getLocation().getBlockZ() + OCparam.PLOT_SIZE.get() - 1);

		return new CuboidRegion(v1, v2);
	}

	public class OlympaCreatifRegionFilter extends CuboidRegionFilter {
		Collection<Plot> plots;

		public OlympaCreatifRegionFilter() {
			plots = TaskManager.IMP.sync(() -> new ArrayDeque<>(plugin.getPlotsManager().getPlots()));
		}

		@Override
		public void calculateRegions() {
			for (Plot plot : plots)
				add(BlockVector2.at(plot.getId().getLocation().getBlockX(), plot.getId().getLocation().getBlockZ()),
						BlockVector2.at(plot.getId().getLocation().getBlockX() + OCparam.PLOT_SIZE.get() - 1,
								plot.getId().getLocation().getBlockZ() + OCparam.PLOT_SIZE.get() - 1));
		}
	}

	private void registerAntiCommandblockEditSession() {
		Fawe.get().getWorldEdit().getEventBus().register(new EventHandler(Priority.EARLY) {

			/*
			@Subscribe
			public void onCommand(CommandEvent e) {
				System.out.println("Command executed : " + e.getArguments() + " BY " + e.getActor());
				LocalSession session = e.getActor().getSession();
			
				if (session == null)
					return;
			
				if (!session.isSelectionDefined(session.getSelectionWorld()))
					System.out.println("Selection not defined!");
				else
					System.out.println("Selection : " + session.getSelection(session.getSelectionWorld()).getMinimumPoint() +  " TO " + session.getSelection(session.getSelectionWorld()).getMaximumPoint());
			}*/

			@Subscribe
			public void onEditSession(EditSessionEvent e) {

				//e.setExtent(new AbstractDelegateExtent(null));

				if (e.getActor() == null || e.getActor().getUniqueId() == null)
					return;

				OlympaPlayerCreatif p = AccountProviderAPI.getter().get(e.getActor().getUniqueId());

				if (p.hasStaffPerm(StaffPerm.WORLDEDIT))
					return;

				e.setExtent(new OcExtent(e.getExtent(), p, plugin.getPlotsManager().getPlot(((Player) p.getPlayer()).getLocation())));
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
			public void dispatch(Object arg0) throws Exception {}

			class OcExtent extends AbstractDelegateExtent {

				private OlympaPlayerCreatif pc;
				private Plot plot;
				private Map<Long, Integer> commandBlocksCount = new HashMap<>();
				private boolean pasteCommandBlocks = true;

				private int tilesCount = 0;

				public OcExtent(Extent extent, OlympaPlayerCreatif pc, Plot plot) {
					super(extent);
					this.pc = pc;
					this.plot = plot;

					if (plot != null)
						tilesCount = plot.getTilesCount();
				}

				@Override
				public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 pos, T block) throws WorldEditException {
					//System.out.println("SET BLOCK POS " + block.getNbtData().toString());
					/*if (block.hasNbtData())
						System.out.println(block.getNbtData().toString());*/

					return isBlockAllowed(pos.getX(), pos.getY(), pos.getZ(), block) ? super.setBlock(pos, block) : false;
				}

				@Override
				public <T extends BlockStateHolder<T>> boolean setBlock(int x, int y, int z, T block) {
					//System.out.println("SET BLOCK LOCATIONS " + block.getNbtData().toString());
					/*if (block.hasNbtData())
						System.out.println(block.getNbtData().toString());*/

					return isBlockAllowed(x, y, z, block) ? super.setBlock(x, y, z, block) : false;
				}

				@Override
				public boolean setTile(int x, int y, int z, CompoundTag tile) {
					//System.out.println("Set tile with FAWE : " + tile.getValue() + " (cancelled)");
					return false;//super.setTile(x, y, z, tile);
				}

				private <T extends BlockStateHolder<T>> boolean isBlockAllowed(int x, int y, int z, T block) {
					//System.out.println("Can place block at " + x + ", " + y + ", " + z + " : " + !(plot == null || !PlotPerm.USE_WE.has(plot, pc)));
					if (plot == null || !PlotPerm.USE_WE.has(plot, pc)) {
						OCmsg.NULL_CURRENT_PLOT.send(pc);
						return false;
					}

					if (block.hasNbtData() && block.getNbtData().toString().length() > OCparam.WE_MAX_NBT_SIZE.get()) {
						OCmsg.WE_TOO_LONG_NBT.send(pc);
						return false;
					}

					if (block.hasNbtData() && tilesCount++ > OCparam.MAX_TILE_PER_PLOT.get()) {
						OCmsg.WE_TOO_MUCH_TILES.send(pc, OCparam.MAX_TILE_PER_PLOT.get() + "");
						return false;
					}

					Material mat = BukkitAdapter.adapt(block.getBlockType());
					KitType kit = plugin.getPerksManager().getKitsManager().getKitOf(mat);

					if (kit != null && !kit.hasKit(pc)) {
						OCmsg.INSUFFICIENT_KIT_PERMISSION.send(pc, kit);
						return false;
					}

					if (kit == KitType.COMMANDBLOCK)
						if (pasteCommandBlocks && plugin.getWorldManager().getWorld().isChunkLoaded(Math.floorDiv(x, 16), Math.floorDiv(z, 16))) {
							Chunk chFull;

							ChunkSnapshot ch = (chFull = plugin.getWorldManager().getWorld()
									.getChunkAt(Math.floorDiv(x, 16), Math.floorDiv(z, 16))).getChunkSnapshot();

							//System.out.println("CHUNK : " + ch.getX() + ", " + ch.getZ());

							Integer currentCbCount = commandBlocksCount.get(chFull.getChunkKey());
							if (currentCbCount == null) {
								int cbCount = 0;

								for (int xCh = 0; xCh < 16; xCh++)
									for (int yCh = 0; yCh < 256; yCh++)
										for (int zCh = 0; zCh < 16; zCh++)
											if (OtherUtils.isCommandBlock(ch.getBlockType(xCh, yCh, zCh)))
												cbCount++;

								currentCbCount = cbCount + 1;
							} else
								currentCbCount++;

							commandBlocksCount.put(chFull.getChunkKey(), currentCbCount);

							if (currentCbCount > OCparam.MAX_CB_PER_CHUNK.get()) {
								OCmsg.PLOT_LOAD_TOO_MUCH_CB_CHUNK.send(pc, plot, ch.getX() + ", " + ch.getZ());
								return false;
							}
						} else
							return false;

					return true;
				}
			}
		});
	}
}
