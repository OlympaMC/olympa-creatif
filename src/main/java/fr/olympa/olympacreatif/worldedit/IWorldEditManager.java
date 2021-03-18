package fr.olympa.olympacreatif.worldedit;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCparam;
import fr.olympa.olympacreatif.plot.Plot;
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

public abstract class IWorldEditManager {
	
	public abstract void clearClipboard(Plot plot, Player p);
	
	public void resetPlot(Player requester, Plot plot) {
		plot.getEntities().forEach(e -> e.remove());
		
		org.bukkit.Chunk originChunk = plot.getPlotId().getLocation().getChunk();
		
		for (int x = originChunk.getX() ; x < originChunk.getX() + OCparam.PLOT_SIZE.get() / 16 ; x++)
			for (int z = originChunk.getZ() ; z < originChunk.getZ() + OCparam.PLOT_SIZE.get() / 16 ; z++)
				OlympaCreatifMain.getInstance().getWorldManager().getWorld()
				.getChunkAtAsync(new Location(OlympaCreatifMain.getInstance().getWorldManager().getWorld(), x * 16, 0, z * 16),
						new Consumer<org.bukkit.Chunk>() {
							
							@Override
							public void accept(org.bukkit.Chunk c) {
								resetChunk(((CraftChunk)c).getHandle());
							}
						});		
	}
	
	public void resetChunk(org.bukkit.Chunk ch) {
		resetChunk(((CraftChunk)ch).getHandle());
	}
	
	public void resetChunk(Chunk ch) {
		
		OlympaCreatifMain.getInstance().getTask().runTaskAsynchronously(() -> {
			int yTotal = -1;
			ChunkSection[] chunkSections = new ChunkSection[16];
			
			for (int iChunkSection = 0 ; iChunkSection < chunkSections.length ; iChunkSection ++) {
				ChunkSection cs = new ChunkSection(iChunkSection / 16);

				for (int y = 0 ; y < 16 ; y++) {
					yTotal++;
					IBlockData block = yTotal == 0 ? Blocks.BEDROCK.getBlockData() : 
						yTotal < WorldManager.worldLevel ? Blocks.DIRT.getBlockData() : 
							yTotal == WorldManager.worldLevel ? Blocks.GRASS_BLOCK.getBlockData() : Blocks.AIR.getBlockData();
							
					for (int x = 0 ; x < 16 ; x++) 
						for (int z = 0 ; z < 16 ; z++)
							cs.setType(x, y, z, block);	
				}
				
				chunkSections[iChunkSection] = cs;
			}
			
			OlympaCreatifMain.getInstance().getTask().runTask(() -> {
				ch.getTileEntities().keySet().forEach(pos -> ch.getWorld().removeTileEntity(pos));
				for (int i = 0 ; i < 16 ; i++)
					ch.getSections()[i] = chunkSections[i];

				LightEngineLayerEventListener listener = ch.getWorld().getChunkProvider().getLightEngine().a(EnumSkyBlock.BLOCK);
				if (listener instanceof LightEngineBlock)
					updateLighting(ch, 0, (LightEngineBlock) listener);
			});
		});
	}
	
	private void updateLighting(Chunk ch, int y, LightEngineBlock lightEngine) {
		if (y <= WorldManager.worldLevel) {
			for (int x = ch.getPos().x * 16 ; x < ch.getPos().x * 16 + 16 ; x++)
				for (int z = ch.getPos().z * 16 ; z < ch.getPos().z * 16 + 16 ; z++) {
					BlockPosition pos = new BlockPosition(x, y, z);
					if (lightEngine.a(SectionPosition.a(pos)) != null)
						lightEngine.a(pos, 15);
				}
		
		OlympaCreatifMain.getInstance().getTask().runTaskLater(() -> updateLighting(ch, y + 1, lightEngine), 3);
		}else
			OlympaCreatifMain.getInstance().getTask().runTaskAsynchronously(() -> {
				PacketPlayOutMapChunk packet = new PacketPlayOutMapChunk(ch, 65535);
				Bukkit.getOnlinePlayers().forEach(p -> ((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet));
			});
	}
	
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
