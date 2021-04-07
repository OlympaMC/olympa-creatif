package fr.olympa.olympacreatif.world;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.utils.NBTcontrollerUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_16_R3.PacketPlayInJigsawGenerate;
import net.minecraft.server.v1_16_R3.PacketPlayInSetCreativeSlot;
import net.minecraft.server.v1_16_R3.PacketPlayInSetJigsaw;
import net.minecraft.server.v1_16_R3.PacketPlayInStruct;

public class PacketListener implements Listener {

	OlympaCreatifMain plugin;
	private Set<UUID> blockedPlayers = new HashSet<UUID>();
	
	private final int maxPacketsPerPeriod = 40;
	private Map<UUID, Long[]> packetsLimiter = new HashMap<UUID, Long[]>();
	
	public PacketListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		plugin.getTask().scheduleSyncRepeatingTask(() -> packetsLimiter.keySet().forEach(key -> packetsLimiter.get(key)[0] = 0l), 0, 250, TimeUnit.MILLISECONDS);
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		handlePlayerPackets(e.getPlayer());
		packetsLimiter.put(e.getPlayer().getUniqueId(), new Long[] {0l});
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		unhandlePlayerPacket(e.getPlayer());
		packetsLimiter.remove(e.getPlayer().getUniqueId());
	}
	

    private void unhandlePlayerPacket(Player player) {
        Channel channel = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(player.getName());
            return null;
        });
    }

    private void handlePlayerPackets(Player player) {
    	
    	OlympaPlayerCreatif p = AccountProvider.get(player.getUniqueId());
    	
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
        	
            @Override
            public void channelRead(ChannelHandlerContext channelHandlerContext, Object handledPacket) throws Exception {
            	
            	//cancel packets related to structure blocks
                if (handledPacket instanceof PacketPlayInJigsawGenerate || handledPacket instanceof PacketPlayInSetJigsaw || handledPacket instanceof PacketPlayInStruct)
                	return;
                
                if (packetsLimiter.get(player.getUniqueId())[0]++ > maxPacketsPerPeriod)
                	return;
                
            	if (player.isOp()) {
            		if (blockedPlayers.add(player.getUniqueId())) {
            			player.sendMessage("§2Very interesting!! §aHow did you get operator permissions? §bAnyway, you won't be able to do anything. §6Don't forget to have fun on Olympa!\n§7If you think that's an error (but I think it isn't), please contact a server administrator.\n§a");
            			plugin.getTask().runTaskLater(() -> blockedPlayers.remove(player.getUniqueId()), 20*30);
            		}
            		return;
            	}
                
            	if (handledPacket instanceof PacketPlayInSetCreativeSlot) {
            		PacketPlayInSetCreativeSlot packet = ((PacketPlayInSetCreativeSlot) handledPacket);
            		
            		if (packet.getItemStack() != null){
            			Material mat = CraftItemStack.asBukkitCopy(packet.getItemStack()).getType();
            			
                		if (!plugin.getPerksManager().getKitsManager().
                				hasPlayerPermissionFor(p, mat)) {
                			p.getPlayer().getInventory().setItemInMainHand(plugin.getPerksManager().getKitsManager().getNoKitPermItem(mat));
                			p.getPlayer().updateInventory();
                			return;	
                		}
                		
                		if (packet.getItemStack().getTag() != null)
                    		packet.getItemStack().setTag(NBTcontrollerUtil.getValidTags(packet.getItemStack().getTag()));
            		}
            	}            	
            	
            	super.channelRead(channelHandlerContext, handledPacket);
            }

            @Override
            public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise) throws Exception {
            	//Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "PACKET OUTPUT: " + ChatColor.RED + packet.toString());
            	
                super.write(channelHandlerContext, packet, channelPromise);
            }
        };

        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline();
        pipeline.addBefore("packet_handler", player.getName(), channelDuplexHandler);

    }
}
