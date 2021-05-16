package fr.olympa.olympacreatif.plot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.CommandBlock;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.entity.Entity;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import fr.olympa.api.holograms.Hologram;
import fr.olympa.api.holograms.Hologram.HologramLine;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.CbBossBar;
import fr.olympa.olympacreatif.commandblocks.CbObjective;
import fr.olympa.olympacreatif.commandblocks.CbTeam;
import fr.olympa.olympacreatif.commandblocks.CbTeam.ColorType;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OCparam;
import fr.olympa.olympacreatif.data.Position;
import fr.olympa.olympacreatif.plot.PlotParamType.HologramData;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.PacketPlayOutMapChunk;
import net.minecraft.server.v1_16_R3.TileEntity;
import net.minecraft.server.v1_16_R3.TileEntityCommand;

public class PlotCbData {
	
	private static final NamespacedKey cbAutoKey = NamespacedKey.minecraft("cb_is_auto");
	
	public static BiConsumer<org.bukkit.block.CommandBlock, Boolean> setCbAuto = (cb, isAuto) -> {
		cb.getPersistentDataContainer().set(cbAutoKey, PersistentDataType.BYTE, isAuto ? (byte) 1 : (byte) 0);
		cb.update();
	};
	public static Function<org.bukkit.block.CommandBlock, Boolean> isCbAuto = cb -> {
		
		//System.out.println("GET is auro custom tag : " + cb.getPersistentDataContainer().get(cbAutoKey, PersistentDataType.BYTE));
		
		if (cb.getPersistentDataContainer().getKeys().contains(cbAutoKey))
			return cb.getPersistentDataContainer().get(cbAutoKey, PersistentDataType.BYTE) == (byte) 1;
		else {
			setCbAuto.accept(cb, false);
			return false;
		}
	};
	
	private OlympaCreatifMain plugin;
	private Plot plot;
	
	private Set<Long> loadedChunks = new HashSet<Long>();
	private int nextChunkCbLoadTick = 0;
	
	private List<CbObjective> objectives = new ArrayList<CbObjective>();
	private List<CbTeam> teams = new ArrayList<CbTeam>();
	private Map<String, CbBossBar> bossbars = new HashMap<String, CbBossBar>();
	
	private double commandsLeft;
	private Scoreboard scb;
	
	private int addTicketsPerSecond; //commandes par tick ajoutées
	
	private boolean hasUnlockedSummonCmd;
	private boolean hasUnlockedSetblockSpawnerCmd;

	private Map<Location, OcCommandBlockData> orangeCommandblocks = new HashMap<Location, OcCommandBlockData>();
	private Map<Location, OcCommandBlockData> greenCommandblocks = new HashMap<Location, OcCommandBlockData>();
	private Map<Location, OcCommandBlockData> blueCommandblocks = new HashMap<Location, OcCommandBlockData>();
	
	private int cbTask = -1;
	
	//key : real holo ID for the core // value : custom holo id which will be used for saving
	private Map<Integer, Integer> holosIds = new HashMap<Integer, Integer>();


	//////////////////////////////////////////////////////////////////////////////////////
	//                                GENERAL MANAGEMENT                                //
	//////////////////////////////////////////////////////////////////////////////////////
	
	
	public PlotCbData(OlympaCreatifMain plugin, int cpt, boolean hasUnlockedSummonCmd, boolean hasUnlockedSetblockSpawnerCmd) {
		this.plugin = plugin;
		
		this.addTicketsPerSecond = cpt;
		this.hasUnlockedSetblockSpawnerCmd = hasUnlockedSummonCmd;
		this.hasUnlockedSummonCmd = hasUnlockedSummonCmd;
		
		commandsLeft = OCparam.CB_MAX_CMDS_LEFT.get();
	}

	@SuppressWarnings("deprecation")
	public void setPlot(Plot plot) {
		if (this.plot != null)
			throw new UnsupportedOperationException("Plot " + this.plot + " has already been defined for this plot cb data.");

		this.plot = plot;

		//load holos
		plot.getParameters().getParameter(PlotParamType.HOLOS_DATAS).forEach((id, data) -> {
			OlympaCore.getInstance().getHologramsManager().registerHologram(id, 
					new Hologram(id, data.getBottom().toLoc(), false, true, true, 
					data.getLines().stream().map(s -> new FixedLine<HologramLine>(s)).collect(Collectors.toList()).toArray(FixedLine[]::new)));
			
			holosIds.put(id, id);
		});
		//System.out.println("Plot cb data " + plot + " loaded. Set holos for " + plot.getPlayers());
		plot.getPlayers().forEach(p -> holosIds.keySet().forEach(holo -> OlympaCore.getInstance().getHologramsManager().getHologram(holo).show(p)));
		
		reloadAllCommandBlocks(false);
		setTickSpeed(plot.getParameters().getParameter(PlotParamType.TICK_SPEED));
		
		//délai nécessaire car sinon le plot est null et la commande ne peut pas se générer
		/*plugin.getTask().runTaskLater(() -> {
			reloadAllCommandBlocks(false);
			setTickSpeed(plot.getParameters().getParameter(PlotParamType.TICK_SPEED));
		}, 1);*/
	}
	
	public void unload() {
		if (cbTask > -1)
			plugin.getTask().cancelTaskById(cbTask);
		
		for (CbObjective o : objectives) 
			o.clearDisplaySlot();
			
		for (CbTeam t : teams)
			for (Entity e : t.getMembers())
				t.removeMember(e);
		
		for (Entry<String, CbBossBar> e : bossbars.entrySet())
			e.getValue().getBar().removeAll();

		if (objectives != null)
			objectives.clear();
		if (teams != null)
			teams.clear();
		if (bossbars != null)
			bossbars.clear();
		
		
		//MAJ du param holos puis supression
		
		PlotParamType.HOLOS_DATAS.setValue(plot, 
			holosIds.keySet().stream().map(id -> OlympaCore.getInstance().getHologramsManager().getHologram(id))
			.collect(Collectors.toMap(holo -> holosIds.get(holo.getID()), holo -> new HologramData(holosIds.get(holo.getID()), 
					holo.getLines().stream().map(line ->  line.getLine() instanceof FixedLine ? ((FixedLine<HologramLine>)line.getLine()).getValue(line) : "")
					.collect(Collectors.toList()), new Position(holo.getBottom())))));
		
		holosIds.keySet().forEach(id -> OlympaCore.getInstance().getHologramsManager().deleteHologram(id));
	}


	//////////////////////////////////////////////////////////////////////////////////////
	//                              HOLOGRAMS MANAGEMENT                                //
	//////////////////////////////////////////////////////////////////////////////////////


	public void addHolo(Hologram holo) {
		if (holo == null)
			return;

		holosIds.put(holo.getID(), getRealHoloIdFromHoloPlotId(holo.getID()));
		//holosLocs.put(holo.getID(), holo.getBottom());
		
		plot.getPlayers().forEach(p -> holo.show(p));
	}

	public boolean removeHolo(Hologram holo) {
		if (holo == null)
			return false;
		
		if (containsHolo(holo))
			if (plot == null)
				Bukkit.getOnlinePlayers().forEach(p -> holo.hide(p));
			else
				plot.getPlayers().forEach(p -> holo.hide(p));

		//holosLocs.remove(holo.getID());
		return holosIds.remove(holo.getID()) != null;
	}
	
	public boolean containsHolo(Hologram holo) {
		return holosIds.containsKey(holo.getID());
	}
	
	public Set<Integer> getHolos() {
		return holosIds.keySet();
	}
	
	public Set<Hologram> getHolosOf(int x, int z) {
		/*return holosLocs.entrySet().stream().filter(e -> e.getValue().getChunk().getChunkKey() == ch.getChunkKey())
				.map(e -> e.getKey()).collect(Collectors.toSet());*/
		return holosIds.keySet().stream().map(i -> OlympaCore.getInstance().getHologramsManager().getHologram(i))
				.filter(holo -> holo.getBottom().getChunk().getX() == x && holo.getBottom().getChunk().getZ() == z).collect(Collectors.toSet());
	}
	
	private int getRealHoloIdFromHoloPlotId(int i) {
		int id = plot.getId().getId() * 1000 + 1;
		while (holosIds.containsValue(id) && id < plot.getId().getId() * 1000 + OCparam.MAX_HOLOS_PER_PLOT.get())
			id++;
		
		return id;
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////
	//                      SCOREBOARDS AND OBJECTIVES MANAGEMENT                       //
	//////////////////////////////////////////////////////////////////////////////////////
	
	
	public Scoreboard getScoreboard() {
		if (scb == null)
			scb = Bukkit.getScoreboardManager().getNewScoreboard();
		
		return scb;
	}
	
	public List<CbObjective> getObjectives() {
		return objectives;
	}
	
	public CbObjective getObjective(String id) {
		for (CbObjective o : objectives)
			if (o.getId().equals(id))
				return o;
		return null;
	}
	
	public List<CbTeam> getTeams(){
		return teams;
	}
	//macimum 20 objectifs par plot
	public boolean registerObjective(CbObjective obj) {

		//n'enregistre pas le scoreboard si un autre avec le même nom existe déjà dans le plot
		for (CbObjective o : objectives)
			if (o.getId().equals(obj.getId()))
				return false;
		
		objectives.add(obj);
		if (objectives.size() > OCparam.CB_MAX_OBJECTIVES_PER_PLOT.get()) {
			objectives.remove(0).clearDisplaySlot();
		}
		
		return true;
	}
	
	public Objective getObjectiveBelowName() {
		if (scb.getObjective(DisplaySlot.BELOW_NAME) != null)
			return scb.getObjective(DisplaySlot.BELOW_NAME);
		else {
			Objective obj = scb.registerNewObjective("belowName", "dummy", "à spécifier");
			obj.setDisplaySlot(DisplaySlot.BELOW_NAME);
			
			return obj;
		}
	}
	
	public void clearBelowName() {
		if (scb.getObjective(DisplaySlot.BELOW_NAME) != null)
			scb.getObjective(DisplaySlot.BELOW_NAME).unregister();
	}
	

	//////////////////////////////////////////////////////////////////////////////////////
	//                          TEAMS AND BOSSBARS MANAGEMENT                           //
	//////////////////////////////////////////////////////////////////////////////////////
	

	//GESTION TEAMS
	
	//ajoute la team à la liste du plot. Max teams autorisées : maxTeamsPerPlot
	public boolean registerTeam(CbTeam team) {
		
		//si une team avec ce nom existe déjà, return
		for (CbTeam t : teams)
			if (t.getId().equals(team.getId()))
				return false;
		
		teams.add(team);
		
		if (teams.size() > OCparam.CB_MAX_TEAMS_PER_PLOT.get())
			teams.remove(0);
		
		return true;
	}
	
	public CbTeam getTeamOf(Entity e) {
		
		for (CbTeam t : teams)
			if (t.getMembers().contains(e)) 
				return t;			
				
		return null;
	}

	public CbTeam getTeamById(String teamId) {
		for (CbTeam t : teams)
			if (t.getId().equals(teamId)) 
				return t;			
				
		return null;
	}

	public CbTeam getTeamByColor(ColorType color) {
		for (CbTeam t : teams)
			if (t.getColor() == color) 
				return t;			
				
		return null;
	}
	
	public CbBossBar getBossBar(String id) {
		if (bossbars.containsKey(id))
			return bossbars.get(id);
		else
			return null;
	}

	//GESTION BOSSBAR
	public void addBossBar(String id, CbBossBar bar){
		if (!bossbars.containsKey(id)) 
			bossbars.put(id, bar);
	}
	
	public boolean removeBossBar(String id) {
		if (bossbars.containsKey(id)) {
			bossbars.get(id).getBar().removeAll();
			bossbars.remove(id);
			return true;
		}else
			return false;
			
	}
	
	public Map<String, CbBossBar> getBossBars() {
		return Collections.unmodifiableMap(bossbars);
	}
	

	//////////////////////////////////////////////////////////////////////////////////////
	//                         COMMANDBLOCKS TICKETS MANAGEMENT                         //
	//////////////////////////////////////////////////////////////////////////////////////

	public boolean removeCommandTickets(double tickets) {
		if (commandsLeft >= tickets) {
			commandsLeft -= tickets;
			return true;
		}else
			return false;
	}
	
	public int getCommandTicketsLeft() {
		return (int) commandsLeft;
	}
	
	public void addCommandTickets() {
		commandsLeft = Math.min(OCparam.CB_MAX_CMDS_LEFT.get(), commandsLeft + addTicketsPerSecond);
	}
	
	public boolean hasEmptyTickets() {
		return commandsLeft > 1;
	}
	
	public void setCommandsPerSecond(int newCpt) {
		addTicketsPerSecond = newCpt;
	}
	
	public int getCommandsPerSecond() {
		return addTicketsPerSecond;
	}


	//////////////////////////////////////////////////////////////////////////////////////
	//                                  GENERAL UTILS                                   //
	//////////////////////////////////////////////////////////////////////////////////////

	
	public boolean hasUnlockedSpawnerSetblock() {
		return hasUnlockedSetblockSpawnerCmd;
	}
	
	public boolean hasUnlockedSummon() {
		return hasUnlockedSummonCmd;
	}
	
	public void unlockSpawnerSetblock() {
		hasUnlockedSetblockSpawnerCmd = true;
	}
	
	public void unlockSummon() {
		hasUnlockedSummonCmd = true;
	}

	

	public void clearEntityDatas(Entity e) {
		for (CbObjective o : objectives) 
			o.set(e, null);
			
		for (CbTeam t : teams)
			if (t.isMember(e))
				t.removeMember(e);
	}

	
	//////////////////////////////////////////////////////////////////////////////////////
	//                       COMMANDBLOCKS PARAMETERS MANAGEMENT                        //
	//////////////////////////////////////////////////////////////////////////////////////
	
	
	public void handleSetCommandBlockPacket(org.bukkit.block.CommandBlock cb, Material oldMat, Material newMat) {
		Map<Location, OcCommandBlockData> oldMap = getCbMap(oldMat);
		Map<Location, OcCommandBlockData> newMap = getCbMap(newMat);
		
		if (oldMap != newMap)
			oldMap.remove(cb.getLocation());
		
		newMap.put(cb.getLocation(), new OcCommandBlockData(cb));
		/*if (newMap.size() <= OCparam.MAX_CB_PER_PLOT.get())
			newMap.put(cb.getLocation(), new OcCommandBlockData(cb));
		else
			plot.getMembers().getList().entrySet().stream().filter(e -> PlotPerm.COMMAND_BLOCK.has(e.getValue()))
			.map(e -> Bukkit.getPlayer(e.getKey().getUUID())).filter(p -> p != null).forEach(p -> OCmsg.PLOT_LOAD_TOO_MUCH_CB_PLOT.send(p, plot, newMat.toString().toLowerCase()));*/
			
	}

	public void removeCommandBlock(Block block) {
		getCbMap(block.getType()).remove(block.getLocation());
	}
	
	public void reloadAllCommandBlocks(final boolean resetChunks) {
		if (resetChunks) {
			orangeCommandblocks.clear();
			greenCommandblocks.clear();
			blueCommandblocks.clear();
			loadedChunks.clear();
		}
		
		nextChunkCbLoadTick = 1;
		plot.getLoadedChunks().forEach(ch -> addChunkToLoadQueue(ch));
	}
	
	public void addChunkToLoadQueue(final Chunk ch) {
		if (!loadedChunks.add(ch.getChunkKey()))
			return;
		
		nextChunkCbLoadTick++;
		
		plugin.getTask().runTaskLater(() -> {
			nextChunkCbLoadTick--;
			registerCommandBlocks(ch);
		}, nextChunkCbLoadTick);
	}
	
	private void registerCommandBlocks(final Chunk ch) {
		//System.out.println("[DEBUG] SET COMMANDS FOR CHUNK " + ch);
		/*if (!ch.isLoaded())
			throw new UnsupportedOperationException("Trying to load commandblock on unloaded chunk " + ch.getX() + ", " + ch.getZ() + "!");*/
		
		ch.getWorld().getChunkAtAsync(ch.getX(), ch.getZ(), new Consumer<Chunk>() {

			@Override
			public void accept(Chunk chunk) {
				
				Map<BlockPosition, TileEntity> tiles = new HashMap<BlockPosition, TileEntity>(((CraftChunk)chunk).getHandle().tileEntities);
				chunk.setForceLoaded(true);
				final World w = plugin.getWorldManager().getWorld();
				
				plugin.getTask().runTaskAsynchronously(() -> {
					Set<Location> locs = tiles.entrySet().stream().filter(e -> e.getValue() instanceof TileEntityCommand).map(e -> new Location(w, e.getKey().getX(), e.getKey().getY(), e.getKey().getZ())).collect(Collectors.toSet());
					
					plugin.getTask().runTask(() -> {
						if (locs.size() > OCparam.MAX_CB_PER_CHUNK.get()) {
							plot.getMembers().getList().entrySet().stream().filter(e -> PlotPerm.COMMAND_BLOCK.has(e.getValue()))
							.map(e -> Bukkit.getPlayer(e.getKey().getUUID())).filter(p -> p != null).forEach(p -> OCmsg.PLOT_LOAD_TOO_MUCH_CB_CHUNK.send(p, plot, "[" + chunk.getX() + "," + chunk.getZ() + "]"));
							
							//System.out.println("CANCELLED CB LOADING for " + ch);	
						}else 
							locs.forEach(loc -> {
								Map<Location, OcCommandBlockData> map = getCbMap(loc.getBlock().getType());
								map.put(loc, new OcCommandBlockData((org.bukkit.block.CommandBlock) loc.getBlock().getState()));
							});
						chunk.setForceLoaded(false);
					});
				});
			}
		});
	}


	//////////////////////////////////////////////////////////////////////////////////////
	//                        COMMANDBLOCKS EXECUTION MANAGEMENT                        //
	//////////////////////////////////////////////////////////////////////////////////////
	
	
	public void handleCommandBlockPowered(BlockRedstoneEvent e) {
		if ((plot == null) || (e.getNewCurrent() > 0 && e.getOldCurrent() > 0))
			return;
		
		OcCommandBlockData cbData = null;
		cbData = getCbData(e.getBlock().getType(), e.getBlock().getLocation());
		
		if (cbData != null) {
			if (cbData.cmd != null && e.getBlock().getType() == Material.COMMAND_BLOCK)
				if (!cbData.isAuto && !cbData.isPowered && e.getNewCurrent() > 0) 
					if (removeCommandTickets(cbData.cmd.getType().getRequiredCbTickets()))
						cbData.cmd.execute();/*((org.bukkit.block.CommandBlock)e.getBlock().getState()) */
					else
						plot.getPlayers().forEach(p -> OCmsg.CB_NO_COMMANDS_LEFT.send(p));

			cbData.isPowered = e.getNewCurrent() > 0;
		}
		
	}
	
	private void executeTickingCommandBlocks() {
		int lastResult = 0;
		
		for (Entry<Location, OcCommandBlockData> e : blueCommandblocks.entrySet()) {
			if (!e.getValue().isExecutablePowered() || e.getValue().conditionnal)
				continue;
			
			lastResult = executeCommandBlock(e.getValue(), lastResult);
			
			OcCommandBlockData greenData = greenCommandblocks.get(e.getValue().direction);
			
			while (greenData != null) {
				if (greenData.isExecutablePowered()) {
					lastResult = executeCommandBlock(greenData, lastResult);
					if (lastResult == -1)
						return;
				}else {
					lastResult = 0;
					if (!removeCommandTickets(0.5)) {
						plot.getPlayers().forEach(p -> OCmsg.CB_NO_COMMANDS_LEFT.send(p));
						return;
					}
				}
				
				greenData = greenCommandblocks.get(greenData.direction);
			}
		}
	}
	
	private int executeCommandBlock(OcCommandBlockData cb, int lastResult) {
		if (cb.cmd != null)
			if (removeCommandTickets(cb.cmd.getType().getRequiredCbTickets()))
				return cb.cmd.execute();
			else {
				plot.getPlayers().forEach(p -> OCmsg.CB_NO_COMMANDS_LEFT.send(p));
				return -1;
			}
		else
			return 0;
	}


	//////////////////////////////////////////////////////////////////////////////////////
	//                                COMMANDBLOCKS UTILS                               //
	//////////////////////////////////////////////////////////////////////////////////////
	
	public void setTickSpeed(int ticks) {
		if (cbTask > -1)
			plugin.getTask().cancelTaskById(cbTask);
		
		cbTask = plugin.getTask().scheduleSyncRepeatingTask(() -> {
			if (plot != null && !plot.hasStoplag())
				executeTickingCommandBlocks();
		}, ticks, ticks);
	}
	
	
	private Location getPointingLoc(CommandBlock cbData, Location blockLoc) {
		switch (cbData.getFacing()) {
		case UP:
			return blockLoc.getBlockY() < 255 ? blockLoc.clone().add(0, 1, 0) : null;
		case DOWN:
			return blockLoc.getBlockY() > 0 ? blockLoc.clone().add(0, -1, 0) : null;
		case EAST:
			return blockLoc.clone().add(1, 0, 0);
		case WEST:
			return blockLoc.clone().add(-1, 0, 0);
		case SOUTH:
			return blockLoc.clone().add(0, 0, 1);
		case NORTH:
			return blockLoc.clone().add(0, 0, -1);
		default:
			return blockLoc.clone();
		}
	}
	
	/*private Location getPointingLoc(org.bukkit.block.CommandBlock cb) {
		return getPointingLoc((CommandBlock) cb.getBlockData(), cb.getLocation());
	}*/
	
	private Map<Location, OcCommandBlockData> getCbMap(Material mat) {
		switch (mat) {
		case COMMAND_BLOCK:
			return orangeCommandblocks;
		case CHAIN_COMMAND_BLOCK:
			return greenCommandblocks;
		case REPEATING_COMMAND_BLOCK:
			return blueCommandblocks;
		default:
			return new HashMap<Location, OcCommandBlockData>();
		}
	}
	
	private OcCommandBlockData getCbData(Material mat, Location loc) {
		return getCbMap(mat).get(loc);
	}
	
	private class OcCommandBlockData {
		
		//private org.bukkit.block.CommandBlock cb;
		
		public CbCommand cmd;
		public boolean conditionnal;
		
		public boolean isAuto;
		public boolean isPowered = false;
		/**
		 * Contient la localisation du bloc vers lequel pointe ce commandblock
		 */
		public Location direction;
		
		private OcCommandBlockData(org.bukkit.block.CommandBlock cb) {
			//this.cb = cb;
			this.cmd = CbCommand.getCommand(plugin, null, cb.getLocation(), cb.getCommand());
			this.conditionnal = ((CommandBlock)cb.getBlockData()).isConditional();
			this.isAuto = isCbAuto.apply(cb);
			direction = getPointingLoc((CommandBlock) cb.getBlockData(), cb.getLocation());
			//this((CommandBlock) cb.getBlockData(), cb.getLocation(), cb.getCommand(), ((CommandBlock)cb).isConditional(), isCbAuto.apply(cb));
		}

		/*private OcCommandBlockData(CommandBlock cbData, Location loc, String cmd, boolean conditionnal, boolean needsRedstone, boolean isPowered) {
			this.cmd = CbCommand.getCommand(plugin, null, loc, cmd);
			this.conditionnal = conditionnal;
			this.isAuto = needsRedstone;
			this.isPowered = isPowered;
			this.direction = getPointingLoc(cbData, loc);
		}*/
		
		public boolean isExecutablePowered() {
			return isPowered || isAuto;
		}
	}
}




