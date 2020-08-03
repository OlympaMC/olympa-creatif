package fr.olympa.olympacreatif.world;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.KitsManager.KitType;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.utils.NBTcontrollerUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_15_R1.ItemStack;
import net.minecraft.server.v1_15_R1.PacketPlayInBlockPlace;
import net.minecraft.server.v1_15_R1.PacketPlayInPickItem;
import net.minecraft.server.v1_15_R1.PacketPlayInSetCreativeSlot;
import net.minecraft.server.v1_15_R1.PacketPlayInUseItem;
import net.minecraft.server.v1_15_R1.PacketPlayOutBlockChange;
import net.minecraft.server.v1_15_R1.PacketPlayOutMultiBlockChange;
import net.minecraft.server.v1_15_R1.PacketPlayOutSetSlot;

public class PacketListener implements Listener {

	OlympaCreatifMain plugin;
	
	public PacketListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		handlePlayerPackets(e.getPlayer());
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		unhandlePlayerPacket(e.getPlayer());
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
            	//Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "PACKET READ: " + ChatColor.RED + handledPacket.toString());
                
            	if (handledPacket instanceof PacketPlayInSetCreativeSlot) {
            		PacketPlayInSetCreativeSlot packet = ((PacketPlayInSetCreativeSlot) handledPacket);
            		
            		if (packet.getItemStack() != null){
                		
                		if (!plugin.getPerksManager().getKitsManager().
                				hasPlayerPermissionFor(p, CraftItemStack.asBukkitCopy(packet.getItemStack()).getType()))
                			return;
                		
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
