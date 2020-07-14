package fr.olympa.olympacreatif.world;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.sk89q.jnbt.NBTInputStream;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.utils.NBTcontrollerUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_15_R1.ItemStack;
import net.minecraft.server.v1_15_R1.PacketPlayInFlying.PacketPlayInLook;
import net.minecraft.server.v1_15_R1.PacketPlayInFlying.PacketPlayInPosition;
import net.minecraft.server.v1_15_R1.PacketPlayInSetCreativeSlot;
import net.minecraft.server.v1_15_R1.PacketPlayOutPosition;
import net.minecraft.server.v1_15_R1.PacketPlayOutPosition.EnumPlayerTeleportFlags;
import net.minecraft.server.v1_15_R1.PacketPlayOutSetSlot;

public class PacketListener implements Listener {

	
	OlympaCreatifMain plugin;
	
	public PacketListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		injectPlayer(e.getPlayer());
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		removePlayer(e.getPlayer());
	}
	

    private void removePlayer(Player player) {
        Channel channel = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(player.getName());
            return null;
        });
    }

    private void injectPlayer(Player player) {
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {

            @Override
            public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
                Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "PACKET READ: " + ChatColor.RED + packet.toString());
                
                boolean cancelPacket = false;
                
            	if (packet instanceof PacketPlayInSetCreativeSlot) 
            		((PacketPlayInSetCreativeSlot) packet).getItemStack().setTag(NBTcontrollerUtil.getValidTags(((PacketPlayInSetCreativeSlot) packet).getItemStack().getTag()));	
            	
            	/*
            	if (packet instanceof PacketPlayOutSetSlot) {
                    Field field = PacketPlayOutSetSlot.class.getDeclaredField("c");
                    field.setAccessible(true);
                    ItemStack item = (ItemStack) field.get(packet);
                    
                    if (item != null) {
                        item.setTag(NBTcontrollerUtil.getValidTags(item.getTag()));
                        field.set(packet, item);	
                    }
            	}
            	*/
            	
            	if (!cancelPacket)
            		super.channelRead(channelHandlerContext, packet);
            }

            @Override
            public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise) throws Exception {
                //Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "PACKET WRITE: " + ChatColor.GREEN + packet.toString());

                super.write(channelHandlerContext, packet, channelPromise);
            }


        };

        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline();
        pipeline.addBefore("packet_handler", player.getName(), channelDuplexHandler);

    }
    
    //return false si l'item
    private boolean checkPacketPlayInSetCreativeSlot(Player p, PacketPlayInSetCreativeSlot packet) {
    	
    	NBTcontrollerUtil.getValidTags(packet.getItemStack().getTag());
    	
    	return true;
    }
}
