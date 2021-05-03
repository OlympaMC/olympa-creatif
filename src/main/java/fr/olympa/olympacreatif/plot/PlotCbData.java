package fr.olympa.olympacreatif.plot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.CommandBlock;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.entity.Entity;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.CbBossBar;
import fr.olympa.olympacreatif.commandblocks.CbObjective;
import fr.olympa.olympacreatif.commandblocks.CbTeam;
import fr.olympa.olympacreatif.commandblocks.CbTeam.ColorType;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OCparam;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.PacketPlayInSetCommandBlock;
import net.minecraft.server.v1_16_R3.TileEntity;
import net.minecraft.server.v1_16_R3.TileEntityCommand;
import net.minecraft.server.v1_16_R3.TileEntityTypes;

public class PlotCbData {

	/*
	private static final Set<Material> commandBlocksTypes = ImmutableSet.<Material>builder()
			.add(Material.COMMAND_BLOCK)
			.add(Material.CHAIN_COMMAND_BLOCK)
			.add(Material.REPEATING_COMMAND_BLOCK)
			.build();*/
	
	private OlympaCreatifMain plugin;
	private Plot plot;
	
	private List<CbObjective> objectives = new ArrayList<CbObjective>();
	private List<CbTeam> teams = new ArrayList<CbTeam>();
	private Map<String, CbBossBar> bossbars = new HashMap<String, CbBossBar>();
	
	private double commandsLeft;
	private Scoreboard scb;
	
	private int cpt; //commandes par tick ajoutées
	
	private boolean hasUnlockedSummonCmd;
	private boolean hasUnlockedSetblockSpawnerCmd;

	private Map<Location, OcCommandBlockData> orangeCommandblocks = new HashMap<Location, OcCommandBlockData>();
	private Map<Location, OcCommandBlockData> greenCommandblocks = new HashMap<Location, OcCommandBlockData>();
	private Map<Location, OcCommandBlockData> blueCommandblocks = new HashMap<Location, OcCommandBlockData>();
	
	public PlotCbData(OlympaCreatifMain plugin, Plot plot, int cpt, boolean hasUnlockedSummonCmd, boolean hasUnlockedSetblockSpawnerCmd) {
		this.plugin = plugin;
		this.plot = plot;
		
		this.cpt = cpt;
		this.hasUnlockedSetblockSpawnerCmd = hasUnlockedSummonCmd;
		this.hasUnlockedSummonCmd = hasUnlockedSummonCmd;
		
		commandsLeft = OCparam.CB_MAX_CMDS_LEFT.get();
	}

	public void setPlot(Plot plot) {
		if (this.plot != null)
			throw new UnsupportedOperationException("Plot " + this.plot + " has already been defined for this plot cb data.");
		this.plot = plot;
	}
	
	//GETTERS
	public Scoreboard getScoreboard() {
		//if (scb == null)
			//scb = plugin.getCommandBlocksManager().getScoreboardForPlotCbData();
		
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
		commandsLeft = Math.min(OCparam.CB_MAX_CMDS_LEFT.get(), commandsLeft + cpt);
	}
	
	public boolean hasEmptyTickets() {
		return commandsLeft > 1;
	}
	
	public void setCpt(int newCpt) {
		cpt = newCpt;
	}
	
	public int getCpt() {
		return cpt;
	}
	
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
	
	
	//GESTION CbOBJECTIVES

	
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
	
	public void unload() {
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
		
		/*
		if (scb != null) {
			if (scb.getObjective(DisplaySlot.BELOW_NAME) != null)
				scb.getObjective(DisplaySlot.BELOW_NAME).unregister();
			plugin.getCommandBlocksManager().addUnusedScoreboard(scb);
		}*/	
	}

	public void clearEntityDatas(Entity e) {
		for (CbObjective o : objectives) 
			o.set(e, null);
			
		for (CbTeam t : teams)
			if (t.isMember(e))
				t.removeMember(e);
	}
	
	public synchronized void handleSetCommandBlockPacket(PacketPlayInSetCommandBlock packet) {
		System.out.println("COMMANDBLOCK PACKET RECIEVED : \n"
				+ "position = " + packet.b() + 
				"\nc = " + packet.c() + 
				"\nd = " + packet.d() + 
				"\ne = " + packet.e() + 
				"\nf = " + packet.f() + 
				"\ng = " + packet.g() + "\n\n");
	}
	
	public void registerCommandBlocks(Chunk ch, boolean removeExisting) {
		Map<BlockPosition, TileEntity> tiles = new HashMap<BlockPosition, TileEntity>(((CraftChunk)ch).getHandle().tileEntities);
		final ChunkSnapshot chSnapshot = ch.getChunkSnapshot();

		//clear les commandblocks déjà enregistrés de ce chunk si demandé 
		if (removeExisting) {
			Iterator<Entry<Location, OcCommandBlockData>> iter = orangeCommandblocks.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<Location, OcCommandBlockData> entry = iter.next();
				if (entry.getKey().getChunk().equals(ch))
					iter.remove();
			}
			
			Iterator<Entry<Location, OcCommandBlockData>> iter2 = blueCommandblocks.entrySet().iterator();
			while (iter2.hasNext()) {
				Entry<Location, OcCommandBlockData> entry = iter2.next();
				if (entry.getKey().getChunk().equals(ch))
					iter2.remove();
			}
			
			Iterator<Entry<Location, OcCommandBlockData>> iter3 = greenCommandblocks.entrySet().iterator();
			while (iter3.hasNext()) {
				Entry<Location, OcCommandBlockData> entry = iter3.next();
				if (entry.getKey().getChunk().equals(ch))
					iter3.remove();
			}
		}
		
		//récupère tous les commandsblocks du chunk et les enregistre
		plugin.getTask().runTaskAsynchronously(() -> {

			Map<Location, OcCommandBlockData> orange = new HashMap<Location, OcCommandBlockData>();
			Map<Location, OcCommandBlockData> green = new HashMap<Location, OcCommandBlockData>();
			Map<Location, OcCommandBlockData> blue = new HashMap<Location, OcCommandBlockData>();
			
			tiles.forEach((pos, tile) -> {
				BlockData data = chSnapshot.getBlockData(Math.floorMod(pos.getX(), 16), pos.getY(), Math.floorMod(pos.getZ(), 16));
				
				if (data instanceof CommandBlock && tile.getTileType().equals(TileEntityTypes.COMMAND_BLOCK)) {
					CommandBlock cb = (CommandBlock) data;
					TileEntityCommand cbTile = (TileEntityCommand) tile;
					Location loc = new Location(plugin.getWorldManager().getWorld(), pos.getX(), pos.getY(), pos.getZ());
					
					CbCommand cmd = CbCommand.getCommand(plugin, cbTile.getCommandBlock().getBukkitSender(cbTile.getCommandBlock().getWrapper()), 
							loc, cbTile.getCommandBlock().getCommand());
					
					OcCommandBlockData cbDatas = new OcCommandBlockData(cmd, cb.isConditional(), cbTile.g(), false, getPointingLoc(cb, loc));
					
					if (cb.getMaterial() == Material.COMMAND_BLOCK) {
						orange.put(loc, cbDatas);
					}else if (cb.getMaterial() == Material.REPEATING_COMMAND_BLOCK) {
						blue.put(loc, cbDatas);
					}else if (cb.getMaterial() == Material.CHAIN_COMMAND_BLOCK) {
						green.put(loc, cbDatas);
					}
				}
			});
			
			plugin.getTask().runTask(() -> {
				orangeCommandblocks.putAll(orange);
				greenCommandblocks.putAll(green);
				blueCommandblocks.putAll(blue);
			});
		});
	}
	
	@SuppressWarnings("incomplete-switch")
	public void registerCommandBlock(Block block) {
		switch(block.getType()) {
		case COMMAND_BLOCK:
			orangeCommandblocks.put(block.getLocation(), 
					new OcCommandBlockData(
							CbCommand.getCommand(plugin, null, block.getLocation(), ((org.bukkit.block.CommandBlock)block).getCommand()), 
							false, true, block.isBlockPowered()
							, getPointingLoc((CommandBlock)block.getBlockData(), block.getLocation())));
			break;
		case CHAIN_COMMAND_BLOCK:
			greenCommandblocks.put(block.getLocation(), 
					new OcCommandBlockData(
							CbCommand.getCommand(plugin, null, block.getLocation(), ((org.bukkit.block.CommandBlock)block).getCommand()), 
							false, true, block.isBlockPowered()
							, getPointingLoc((CommandBlock)block.getBlockData(), block.getLocation())));
			break;
		case REPEATING_COMMAND_BLOCK:
			blueCommandblocks.put(block.getLocation(), 
					new OcCommandBlockData(
							CbCommand.getCommand(plugin, null, block.getLocation(), ((org.bukkit.block.CommandBlock)block).getCommand()), 
							false, true, block.isBlockPowered()
							, getPointingLoc((CommandBlock)block.getBlockData(), block.getLocation())));
			break;
		}
	}
	
	public void handleCommandBlockPowered(BlockRedstoneEvent e) {
		if (e.getNewCurrent() > 0 && e.getOldCurrent() > 0)
			return;
		
		OcCommandBlockData cbData = null;
		cbData = orangeCommandblocks.get(e.getBlock().getLocation());
		
		if (cbData != null && cbData.isPowered && e.getNewCurrent() > 0) {
			if (cbData.cmd != null)
				if (removeCommandTickets(cbData.cmd.getType().getRequiredCbTickets()))
					cbData.cmd.execute();
				else
					plot.getPlayers().forEach(p -> OCmsg.CB_NO_COMMANDS_LEFT.send(p));
		}else {
			cbData = greenCommandblocks.containsKey(e.getBlock().getLocation()) ?
						greenCommandblocks.get(e.getBlock().getLocation()) :
						blueCommandblocks.get(e.getBlock().getLocation());
						
			if (cbData != null)
				cbData.isPowered = e.getNewCurrent() > 0;
		}
	}
	
	public void executeTickingCommandBlocks() {
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
	
	
	
	private Location getPointingLoc(CommandBlock cb, Location initLoc) {
		switch (cb.getFacing()) {
		case UP:
			return initLoc.getBlockY() < 255 ? initLoc.clone().add(0, 1, 0) : null;
		case DOWN:
			return initLoc.getBlockY() > 0 ? initLoc.clone().add(0, -1, 0) : null;
		case EAST:
			return initLoc.clone().add(1, 0, 0);
		case WEST:
			return initLoc.clone().add(-1, 0, 0);
		case SOUTH:
			return initLoc.clone().add(0, 0, 1);
		case NORTH:
			return initLoc.clone().add(0, 0, -1);
		default:
			return initLoc.clone();
		}
	}
	
	
	private class OcCommandBlockData {
		
		public CbCommand cmd;
		public boolean conditionnal;
		public boolean needsRedstone;
		public boolean isPowered;
		/**
		 * Contient la localisation du bloc vers lequel pointe ce commandblock
		 */
		public Location direction;
		
		private OcCommandBlockData(CbCommand cmd, boolean conditionnal, boolean needsRedstone, boolean isPowered, Location direction) {
			this.cmd = cmd;
			this.conditionnal = conditionnal;
			this.needsRedstone = needsRedstone;
			this.isPowered = isPowered;
			this.direction = direction;
		}
		
		public boolean isExecutablePowered() {
			return isPowered || !needsRedstone;
		}
	}
}




