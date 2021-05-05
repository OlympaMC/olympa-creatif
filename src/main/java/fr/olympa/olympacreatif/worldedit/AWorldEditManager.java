package fr.olympa.olympacreatif.worldedit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R3.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OCparam;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotId;
import fr.olympa.olympacreatif.world.WorldManager;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.Blocks;
import net.minecraft.server.v1_16_R3.Chunk;
import net.minecraft.server.v1_16_R3.ChunkSection;
import net.minecraft.server.v1_16_R3.EnumSkyBlock;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.LightEngineBlock;
import net.minecraft.server.v1_16_R3.LightEngineLayerEventListener;
import net.minecraft.server.v1_16_R3.PacketPlayOutMapChunk;
import net.minecraft.server.v1_16_R3.SectionPosition;

public abstract class AWorldEditManager {
	
	protected OlympaCreatifMain plugin;
	
	private static Map<PlotId, Integer> resetingPlots = new HashMap<PlotId, Integer>();
	
	public AWorldEditManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;
	}
	
	public abstract void clearClipboard(Plot plot, Player p);
	
	//private static Set<PlotId> resetingPlots = new HashSet<PlotId>();
	
	public final boolean isReseting(Plot plot) {
		return resetingPlots.containsKey(plot.getId());
	}
	
	public boolean resetPlot(OlympaPlayerCreatif requester, Plot plot) {
		if (isReseting(plot))
			return false;
		
		resetingPlots.put(plot.getId(), 0);
		plugin.getTask().runTaskLater(() -> resetingPlots.remove(plot.getId()), 200);
		
		Arrays.asList(plot.getEntities().toArray(new Entity[plot.getEntities().size()])).forEach(e -> plot.removeEntityInPlot(e, true));
		
		org.bukkit.Chunk originChunk = plot.getId().getLocation().getChunk();
		
		for (int x = originChunk.getX() ; x < originChunk.getX() + OCparam.PLOT_SIZE.get() / 16 ; x++)
			for (int z = originChunk.getZ() ; z < originChunk.getZ() + OCparam.PLOT_SIZE.get() / 16 ; z++) {
				resetingPlots.put(plot.getId(), resetingPlots.get(plot.getId()) + 1);
				
				plugin.getWorldManager().getWorld()
				.getChunkAtAsync(new Location(plugin.getWorldManager().getWorld(), x * 16, 0, z * 16),
						new Consumer<org.bukkit.Chunk>() {
							
							@Override
							public void accept(org.bukkit.Chunk c) {
								resetChunk(requester, plot, ((CraftChunk)c).getHandle());
							}
						});	
			}		
		
		return true;
	}
	
	private void resetChunk(OlympaPlayerCreatif requester, Plot plot, Chunk ch) {
		resetTiles(requester, plot, ch, new ArrayList<BlockPosition>(ch.getTileEntities().keySet()));
	}
	
	private void resetTiles(OlympaPlayerCreatif requester, Plot plot, Chunk ch, List<BlockPosition> tiles) {
		try {
			for (int i = 0 ; i < (tiles.size() > 4 ? 4 : tiles.size()) ; i++)
				ch.getWorld().removeTileEntity(tiles.remove(i));
			
			if (tiles.size() > 0)
				plugin.getTask().runTaskLater(() -> resetTiles(requester, plot, ch, tiles), 1);
			else
				resetBlocks(requester, plot, ch);	
		}catch(Exception ex) {
			resetingPlots.remove(plot.getId());
			plugin.getLogger().warning("Error while trying to reset plot " + plot + " on chunk " + ch.getPos().x + "," + ch.getPos().z);
			OCmsg.PLOT_RESET_ERROR.send(requester, plot);
		}
	}
	
	private void resetBlocks(OlympaPlayerCreatif requester, Plot plot, Chunk ch) {
		ChunkSection[] sections = new ChunkSection[16];

		plugin.getTask().runTaskAsynchronously(() -> {
			int yTotal = -1;
		
			for (int iChunkSection = 0 ; iChunkSection < 16 ; iChunkSection ++) {
		        
				for (int y = 0 ; y < 16 ; y++) {
					yTotal++;
					
			        ChunkSection cs = sections[yTotal >> 4];
			        if (cs == null) {
			            cs = new ChunkSection(yTotal >> 4 << 4);
			            sections[yTotal >> 4] = cs;
			        }
			        
					IBlockData block = yTotal == 0 ? Blocks.BEDROCK.getBlockData() : 
						yTotal < WorldManager.worldLevel ? Blocks.DIRT.getBlockData() : 
							yTotal == WorldManager.worldLevel ? Blocks.GRASS_BLOCK.getBlockData() : Blocks.AIR.getBlockData();
							
					for (int x = 0 ; x < 16 ; x++)
						for (int z = 0 ; z < 16 ; z++) 
							//BlockPosition pos = new BlockPosition(ch.getPos().x + x, yTotal, ch.getPos().z + z);
							cs.setType(x, y, z, block);					
				}
			}
			
			plugin.getTask().runTask(() -> {
				for (int i = 0 ; i < 16 ; i++)
					ch.getSections()[i] = sections[i];
				
				LightEngineLayerEventListener listener = ch.getWorld().getChunkProvider().getLightEngine().a(EnumSkyBlock.BLOCK);
				if (listener instanceof LightEngineBlock)
					for (int x = ch.getPos().x * 16 ; x < ch.getPos().x * 16 + 16 ; x++)
						for (int z = ch.getPos().z * 16 ; z < ch.getPos().z * 16 + 16 ; z++) {
							BlockPosition pos = new BlockPosition(x, WorldManager.worldLevel, z);
							if (listener.a(SectionPosition.a(pos)) != null)
								((LightEngineBlock)listener).a(pos, 15);
						}
				
				ch.markDirty();
				ch.mustNotSave = false;
				
				if (resetingPlots.containsKey(plot.getId()))
					resetingPlots.put(plot.getId(), resetingPlots.get(plot.getId()) - 1);
				
				if (plot != null && resetingPlots.get(plot.getId()) <= 0) {
					resetingPlots.remove(plot.getId());
					OCmsg.PLOT_RESET_END.send(requester, plot);
				}
				
				plugin.getTask().runTaskAsynchronously(() -> {
					PacketPlayOutMapChunk packet = new PacketPlayOutMapChunk(ch, 65535);
					Bukkit.getOnlinePlayers().forEach(p -> ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet));
				});
			});
		});
		
	}
	
	/*
	public void resetChunk(Chunk ch) {

		int yTotal = -1;
		
		for (int iChunkSection = 0 ; iChunkSection < 16 ; iChunkSection ++) {
	        
			for (int y = 0 ; y < 16 ; y++) {
				yTotal++;
				
		        ChunkSection cs = ch.getSections()[yTotal >> 4];
		        if (cs == null) {
		            cs = new ChunkSection(yTotal >> 4 << 4);
		            ch.getSections()[yTotal >> 4] = cs;
		        }
		        
				IBlockData block = yTotal == 0 ? Blocks.BEDROCK.getBlockData() : 
					yTotal < WorldManager.worldLevel ? Blocks.DIRT.getBlockData() : 
						yTotal == WorldManager.worldLevel ? Blocks.GRASS_BLOCK.getBlockData() : Blocks.AIR.getBlockData();
						
				for (int x = 0 ; x < 16 ; x++)
					for (int z = 0 ; z < 16 ; z++) {
						BlockPosition pos = new BlockPosition(ch.getPos().x + x, yTotal, ch.getPos().z + z);
						
						if (ch.getTileEntity(pos) != null)
							ch.getWorld().setTypeAndData(pos, block, 1042);
						else
							cs.setType(x, y, z, block);
					}
			}
		}
		
		LightEngineLayerEventListener listener = ch.getWorld().getChunkProvider().getLightEngine().a(EnumSkyBlock.BLOCK);
		if (listener instanceof LightEngineBlock)
			for (int x = ch.getPos().x * 16 ; x < ch.getPos().x * 16 + 16 ; x++)
				for (int z = ch.getPos().z * 16 ; z < ch.getPos().z * 16 + 16 ; z++) {
					BlockPosition pos = new BlockPosition(x, WorldManager.worldLevel, z);
					if (listener.a(SectionPosition.a(pos)) != null)
						((LightEngineBlock)listener).a(pos, 15);
				}
		
		ch.markDirty();
		ch.mustNotSave = false;
		
		plugin.getTask().runTaskAsynchronously(() -> {
			PacketPlayOutMapChunk packet = new PacketPlayOutMapChunk(ch, 65535);
			Bukkit.getOnlinePlayers().forEach(p -> ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet));
		});
	}
/*
			LightEngineLayerEventListener listener = ch.getWorld().getChunkProvider().getLightEngine().a(EnumSkyBlock.BLOCK);
			if (listener instanceof LightEngineBlock)
				for (int x = ch.getPos().x * 16 ; x < ch.getPos().x * 16 + 16 ; x++)
					for (int z = ch.getPos().z * 16 ; z < ch.getPos().z * 16 + 16 ; z++) {
						BlockPosition pos = new BlockPosition(x, WorldManager.worldLevel, z);
						if (listener.a(SectionPosition.a(pos)) != null)
							((LightEngineBlock)listener).a(pos, 15);
					}
			
			ch.markDirty();
			ch.mustNotSave = false;
			
			PacketPlayOutMapChunk packet = new PacketPlayOutMapChunk(ch, 65535);
			Bukkit.getOnlinePlayers().forEach(p -> ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet));
			
			ch.getTileEntities().keySet().forEach(pos -> ch.getWorld().setTypeAndData(
					pos, ((CraftBlockData)Material.AIR.createBlockData()).getState(), 1042));*/		
		
	
	
	/*
	private void updateLighting(Chunk ch, int y, LightEngineBlock lightEngine) {
		if (y <= WorldManager.worldLevel) {
			for (int x = ch.getPos().x * 16 ; x < ch.getPos().x * 16 + 16 ; x++)
				for (int z = ch.getPos().z * 16 ; z < ch.getPos().z * 16 + 16 ; z++) {
					BlockPosition pos = new BlockPosition(x, y, z);
					if (lightEngine.a(SectionPosition.a(pos)) != null)
						lightEngine.a(pos, 15);
				}
		
		plugin.getTask().runTaskLater(() -> updateLighting(ch, y + 1, lightEngine), 3);
		}else {
			PacketPlayOutMapChunk packet = new PacketPlayOutMapChunk(ch, 65535);
			Bukkit.getOnlinePlayers().forEach(p -> ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet));
		}
	}*/
	
	/*Listener bukkitListener = new Listener() {
		
		@EventHandler
		public void onCommand(PlayerCommandPreprocessEvent e) {
			
			if (e.getMessage().contains("/schem")) {
				OlympaPlayerCreatif pc = AccountProvider.get(e.getPlayer().getUniqueId());
				
				if (!PermissionsList.STAFF_BYPASS_WORLDEDIT.hasPermissionWithMsg(pc) || !pc.hasStaffPerm(StaffPerm.WORLDEDIT_EVERYWHERE))
					e.setCancelled(true);
			}	
		}
	};*/
}
