package fr.olympa.olympacreatif.worldedit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.fastasyncworldedit.bukkit.regions.BukkitMaskManager;
import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.FaweAPI;
import com.fastasyncworldedit.core.regions.FaweMask;
import com.fastasyncworldedit.core.regions.general.CuboidRegionFilter;
import com.fastasyncworldedit.core.regions.general.RegionFilter;
import com.fastasyncworldedit.core.util.EditSessionBuilder;
import com.fastasyncworldedit.core.util.TaskManager;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import fr.olympa.olympacreatif.data.*;
import org.bukkit.Bukkit;
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
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.data.PermissionsManager.ComponentCreatif;
import fr.olympa.olympacreatif.perks.KitsManager.KitType;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotPerm;
import fr.olympa.olympacreatif.utils.OtherUtils;
import fr.olympa.olympacreatif.world.WorldManager;

public class OcFastAsyncWorldEdit extends AWorldEdit {

	private Set<BlockType> blocksWithNbtNature = Set.of(
			BlockTypes.HOPPER,
			BlockTypes.CHEST,
			BlockTypes.TRAPPED_CHEST,
			BlockTypes.DISPENSER,
			BlockTypes.DROPPER,
			BlockTypes.END_PORTAL,
			BlockTypes.NETHER_PORTAL,
			BlockTypes.END_PORTAL_FRAME
			);

	private Cache<String, Clipboard> plotSchemsCache = CacheBuilder.newBuilder().concurrencyLevel(10).expireAfterAccess(5, TimeUnit.MINUTES).build();

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
				private boolean hasSentErrorMessage = false;

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
						if (!hasSentErrorMessage)
							OCmsg.NULL_CURRENT_PLOT.send(pc);

						hasSentErrorMessage = true;
						return false;
					}

					if (block.hasNbtData() && block.getNbtData().toString().length() > OCparam.WE_MAX_NBT_SIZE.get()) {
						if (!hasSentErrorMessage)
							OCmsg.WE_TOO_LONG_NBT.send(pc);

						hasSentErrorMessage = true;
						return false;
					}

					if ((blocksWithNbtNature.contains(block.getBlockType()) || block.hasNbtData()) && tilesCount++ > OCparam.MAX_TILE_PER_PLOT.get()) {
						if (!hasSentErrorMessage)
							OCmsg.WE_TOO_MUCH_TILES.send(pc, OCparam.MAX_TILE_PER_PLOT.get() + "");

						hasSentErrorMessage = true;
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
								if (!hasSentErrorMessage)
									OCmsg.PLOT_LOAD_TOO_MUCH_CB_CHUNK.send(pc, plot, ch.getX() + ", " + ch.getZ());

								hasSentErrorMessage = true;
								return false;
							}
						} else
							return false;

					return true;
				}
			}
		});
	}

	@Override
	public void savePlotSchem(OlympaPlayerCreatif pc, String schemName, Plot plot, Position pos1, Position pos2, final Runnable successCallback) {
		plugin.getTask().runTaskAsynchronously(() -> {

			File dir = new File(plugin.getDataFolder() + "/plot_schematics/plot_" + plot);
			dir.mkdirs();

			if (dir.list().length >= OCparam.PLOT_MAX_SCHEMS.get()) {
				OCmsg.PLOT_SCHEMS_MAX_COUNT_REACHED.send(pc, plot, OCparam.PLOT_MAX_SCHEMS);
				return;
			}

			if (getVolume(pos1, pos2) > OCparam.PLOT_MAX_SCHEM_BLOCKS.get()) {
				OCmsg.PLOT_SCHEMS_TOO_MANY_BLOCKS.send(pc, plot, getVolume(pos1, pos2) + "", OCparam.PLOT_MAX_SCHEM_BLOCKS);
				return;
			}

			if (!plot.getId().isInPlot(pos1) || !plot.getId().isInPlot(pos2)) {
				OCmsg.PLOT_SCHEMS_OPERATION_OUT_OF_PLOT.send(pc, plot, schemName);
				return;
			}

			File file = new File(dir.getAbsolutePath(), schemName + ".schem");

			BlockVector3 vec1 = BlockVector3.at(pos1.getX(), pos1.getY(), pos1.getZ());
			BlockVector3 vec2 = BlockVector3.at(pos2.getX(), pos2.getY(), pos2.getZ());

			EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(plugin.getWorldManager().getWorld()), -1);

			CuboidRegion region = new CuboidRegion(BukkitAdapter.adapt(plugin.getWorldManager().getWorld()), vec1, vec2);
			BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

			ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(session, region, clipboard, region.getMinimumPoint());
			forwardExtentCopy.setCopyingEntities(true);

			try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(file))) {
				Operations.complete(forwardExtentCopy);
				writer.write(clipboard);

				if (successCallback != null)
					successCallback.run();

				OCmsg.PLOT_SCHEMS_SAVED.send(pc, plot, schemName);

			} catch (IOException | WorldEditException e) {
				e.printStackTrace();
				OCmsg.PLOT_SCHEMS_ERROR.send(pc, plot);
			}
		});
	}

	@Override
	public void deletePlotSchem(OlympaPlayerCreatif pc, String schemName, Plot plot, Runnable successCallback) {
		File file = new File(plugin.getDataFolder() + "/plot_schematics/plot_" + plot + "/" + schemName + ".schem");

		if (!file.delete())
			OCmsg.PLOT_SCHEMS_UNKNOWN_FILE.send(pc, plot, schemName);
		else {
			OCmsg.PLOT_SCHEMS_FILE_DELETED.send(pc, plot, schemName);
			successCallback.run();
		}
	}

	@Override
	public void pastePlotSchem(OlympaPlayerCreatif pc, String schemName, Plot plot, Position pos) {
		plugin.getTask().runTaskAsynchronously(() -> {
			File dir = new File(plugin.getDataFolder() + "/plot_schematics/plot_" + plot);
			File file = new File(dir.getAbsolutePath(), schemName + ".schem");

			if (!file.exists()) {
				OCmsg.PLOT_SCHEMS_UNKNOWN_FILE.send(pc, plot, schemName);
				return;
			}

			try (ClipboardReader reader = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getReader(new FileInputStream(file))) {
				Clipboard clipboard = plotSchemsCache.get(schemName, () -> reader.read());

				BlockVector3 origin = BlockVector3.at(pos.getX(), pos.getY(), pos.getZ());

				if (!plot.getId().isInPlot(pos) ||
						!plot.getId().isInPlot((int) pos.getX() + clipboard.getDimensions().getBlockX(), (int) pos.getZ() + clipboard.getDimensions().getBlockZ(), 0) ){
					OCmsg.PLOT_SCHEMS_OPERATION_OUT_OF_PLOT.send(pc, plot, schemName);
					return;
				}

				clipboard.getDimensions().getBlockX();

				try (EditSession editSession = new EditSessionBuilder(BukkitAdapter.adapt(plugin.getWorldManager().getWorld()))
						.allowedRegionsEverywhere().limitUnlimited().build()) {
					Operation operation = new ClipboardHolder(clipboard)
							.createPaste(editSession)
							.to(origin)
							.ignoreAirBlocks(false)
							.build();

					Operations.complete(operation);
				}
				//plot.getCbData().reloadAllCommandBlocks(true);
				OCmsg.PLOT_SCHEMS_PASTED.send(pc, plot, schemName);

			} catch (IOException | ExecutionException e) {
				OCmsg.PLOT_SCHEMS_ERROR.send(pc, plot, schemName);
				e.printStackTrace();
			}

		});
	}

	@Override
	public boolean setPlotFloor(OlympaPlayerCreatif pc, Plot plot, Set<Material> mat, int matY) {
		/*if (plot == null){
			OCmsg.NULL_CURRENT_PLOT.send(pc);
			return false;

		} else if (!PlotPerm.RESET_PLOT.has(plot, pc)){
			OCmsg.INSUFFICIENT_PLOT_PERMISSION.send(pc, PlotPerm.RESET_PLOT);
			return false;

		}else*/
		if (mat.stream().anyMatch(m -> !m.isBlock())) {
			OCmsg.WE_NOT_BLOCK_BLOCK.send(pc, mat.toString().toLowerCase());
			return false;

		}

		List<BlockState> states = mat.stream().map(block -> BukkitAdapter.adapt(Bukkit.createBlockData(block))).collect(Collectors.toList());

		if (mat.stream().anyMatch(m -> !plugin.getPerksManager().getKitsManager().hasPlayerPermissionFor(pc, m))) {
			OCmsg.INSUFFICIENT_KIT_PERMISSION.send(pc, plugin.getPerksManager().getKitsManager()
					.getKitOf(mat.stream().filter(m -> !plugin.getPerksManager().getKitsManager()
							.hasPlayerPermissionFor(pc, m)).findAny().get()));
			return false;

		}else if (blocksWithNbtNature.stream().anyMatch(block -> states.contains(block.getDefaultState()))) {
			OCmsg.INSUFFICIENT_KIT_PERMISSION.send(pc, KitType.ADMIN);
			return false;

		}else if (matY < 1 || matY > 250) {
			OCmsg.WE_SETFLOOR_INVALID_Y.send(pc, "1 à 250");
			return false;
		}

		plugin.getTask().runTaskAsynchronously(() -> {
			int xMin = plot.getId().getLocation().getBlockX();
			int zMin = plot.getId().getLocation().getBlockZ();
			int xMax = xMin + OCparam.PLOT_SIZE.get() - 1;
			int zMax = zMin + OCparam.PLOT_SIZE.get() - 1;

			try (EditSession session = new EditSession(new EditSessionBuilder(BukkitAdapter.adapt(plugin.getWorldManager().getWorld())))) {
				OCmsg.PLOT_RESET_START.send(pc, plot);

				for (int x = xMin; x <= xMax; x++)
					for (int z = zMin; z <= zMax; z++)
						session.setBlock(x, 0, z, BlockTypes.BEDROCK);

				for (int x = xMin; x <= xMax; x++)
					for (int z = zMin; z <= zMax; z++)
						for (int y = 1; y <= matY; y++)
							session.setBlock(x, y, z, states.get(ThreadLocalRandom.current().nextInt(states.size())));

				for (int x = xMin; x <= xMax; x++)
					for (int z = zMin; z <= zMax; z++)
						for (int y = matY + 1; y < 256; y++)
							session.setBlock(x, y, z, BlockTypes.AIR);

				session.flushQueue();
				session.close();

				OCmsg.PLOT_RESET_END.send(pc);
			}
		});

		return true;
	}

	@Override
	public Position[] convertSelectionToPositions(OlympaPlayerCreatif pc) {
		LocalSession weSession = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt((Player)pc.getPlayer()));
		World world = weSession.getSelectionWorld();

		//reset worldedit positions
		if (world == null || weSession.getRegionSelector(world) == null)
			return null;
		else{
			BlockVector3 vec1 = weSession.getRegionSelector(world).getRegion().getMinimumPoint();
			BlockVector3 vec2 = weSession.getRegionSelector(world).getRegion().getMaximumPoint();

			return new Position[]{new Position(vec1.getBlockX(), vec1.getBlockY(), vec1.getBlockZ(), 0, 0),
					new Position(vec2.getBlockX(), vec2.getBlockY(), vec2.getBlockZ(), 0, 0)};
		}
	}

	@Override
	public boolean exportPlot(final Plot plot, final OlympaPlayerCreatif p) {
		if (!ComponentCreatif.WORLDEDIT.isActivated()) {
			OCmsg.WE_DISABLED.send(p);
			return false;
		}

		OCmsg.WE_START_GENERATING_PLOT_SCHEM.send(p, plot);

		//création fichier & dir si existants
		//create the Clipboard to copy
		//Generates the .schematic file from the clipboard
		plugin.getTask().runTaskAsynchronously(() -> {

			//création fichier & dir si existants
			File dir = new File(plugin.getDataFolder() + "/schematics");
			File schemFile = new File(dir.getAbsolutePath(), plot.getMembers().getOwner().getName() + "_" + plot.getId() + ".schem");
			plugin.getDataFolder().mkdir();
			dir.mkdir();
			try {
				schemFile.delete();
				schemFile.createNewFile();
				schemFile.deleteOnExit();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			//create the Clipboard to copy
			BlockVector3 v1 = BlockVector3.at(plot.getId().getLocation().getBlockX(), 0, plot.getId().getLocation().getBlockZ());
			BlockVector3 v2 = BlockVector3.at(plot.getId().getLocation().getBlockX() + OCparam.PLOT_SIZE.get() - 1, 255, plot.getId().getLocation().getBlockZ() + OCparam.PLOT_SIZE.get() - 1);

			CuboidRegion region = new CuboidRegion(BukkitAdapter.adapt(plugin.getWorldManager().getWorld()), v1, v2);
			BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

			EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(plugin.getWorldManager().getWorld()), -1);

			ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(session, region, clipboard, region.getMinimumPoint());
			forwardExtentCopy.setCopyingEntities(true);


			//Generates the .schematic file from the clipboard
			try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(schemFile))) {
				Operations.complete(forwardExtentCopy);
				writer.write(clipboard);

			} catch (IOException | WorldEditException e) {
				e.printStackTrace();
			}

			plugin.getDataManager().saveSchemToDb(p, plot, schemFile);
			OCmsg.WE_COMPLETE_GENERATING_PLOT_SCHEM.send(p, plot);
		});

		return true;
		//return "§4La fonctionnalité d'export de la parcelle est indisponible pendant la bêta, désolé ¯\\_༼ ಥ ‿ ಥ ༽_/¯";
	}

	@Override
	public boolean restorePlot(final Plot plot, final OlympaPlayerCreatif p) {
		if (!ComponentCreatif.WORLDEDIT.isActivated()) {
			OCmsg.WE_DISABLED.send(p);
			return false;
		}

		OCmsg.WE_START_RESTORING_PLOT.send(p, plot);

		plugin.getTask().runTaskAsynchronously(() -> {
			Blob blob = plugin.getDataManager().loadSchemFromDb(p, plot);

			if (blob == null) {
				OCmsg.WE_NO_PLOT_SCHEM_FOUND.send(p);
				return;
			}

			try (ClipboardReader reader = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getReader(blob.getBinaryStream())) {
				Clipboard clipboard = reader.read();
				BlockVector3 origin = BlockVector3.at(plot.getId().getLocation().getBlockX(), 0, plot.getId().getLocation().getBlockZ());

				try (EditSession editSession = new EditSessionBuilder(BukkitAdapter.adapt(plugin.getWorldManager().getWorld()))
						.allowedRegionsEverywhere().limitUnlimited().build()) {
					Operation operation = new ClipboardHolder(clipboard)
							.createPaste(editSession)
							.to(origin)
							.ignoreAirBlocks(false)
							.build();

					Operations.complete(operation);
				}
				plot.getCbData().reloadAllCommandBlocks(true);
				OCmsg.WE_COMPLETE_RESTORING_PLOT.send(p, plot);

			} catch (IOException | SQLException e) {
				OCmsg.WE_FAIL_RESTORING_PLOT.send(p, plot);
				e.printStackTrace();
			}
		});

		return true;
	}
}
