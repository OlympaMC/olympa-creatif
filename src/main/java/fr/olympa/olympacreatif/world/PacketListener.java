package fr.olympa.olympacreatif.world;

import java.util.HashMap;
import java.util.Map;

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
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.utils.NBTcontrollerUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_15_R1.ItemStack;
import net.minecraft.server.v1_15_R1.PacketPlayInSetCreativeSlot;

public class PacketListener implements Listener {

	OlympaCreatifMain plugin;
	private Map<Material, OlympaPermission> restrictedItems = new HashMap<Material, OlympaPermission>();
	
	public PacketListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		buildRestrictedItems();
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
            	//Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "PACKET READ: " + ChatColor.RED + packet.toString());
                
            	if (handledPacket instanceof PacketPlayInSetCreativeSlot) {
            		
            		PacketPlayInSetCreativeSlot packet = ((PacketPlayInSetCreativeSlot) handledPacket);
            		
            		if (packet.getItemStack() != null){
                		
                		if (!hasPermissionFor(p, packet.getItemStack()))
                			return;
                		
                		if (packet.getItemStack().getTag() != null)
                    		packet.getItemStack().setTag(NBTcontrollerUtil.getValidTags(packet.getItemStack().getTag()));
            		}
            	}
            	
            	
            	super.channelRead(channelHandlerContext, handledPacket);
            }

            @Override
            public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise) throws Exception {
                super.write(channelHandlerContext, packet, channelPromise);
            }
        };

        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline();
        pipeline.addBefore("packet_handler", player.getName(), channelDuplexHandler);

    }
    
    private boolean hasPermissionFor(OlympaPlayerCreatif p, ItemStack item) {
    	CraftItemStack.asBukkitCopy(item);
    	
    	return true;
    }
    
    private void buildRestrictedItems() {
		restrictedItems.put(Material.LINGERING_POTION, PermissionsList.KIT_ADMIN);
		restrictedItems.put(Material.DEBUG_STICK, PermissionsList.KIT_ADMIN);
		restrictedItems.put(Material.STRUCTURE_BLOCK, PermissionsList.KIT_ADMIN);
		restrictedItems.put(Material.STRUCTURE_VOID, PermissionsList.KIT_ADMIN);
		restrictedItems.put(Material.REPEATING_COMMAND_BLOCK, PermissionsList.KIT_ADMIN);
		
		restrictedItems.put(Material.BEE_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.CAT_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.CHICKEN_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.COD_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.COW_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.DOLPHIN_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.DONKEY_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.FOX_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.HORSE_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.LLAMA_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.MAGMA_CUBE_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.MOOSHROOM_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.MULE_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.OCELOT_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.PANDA_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.PARROT_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.PIG_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.PUFFERFISH_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.RABBIT_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.SALMON_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.SHEEP_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.TRADER_LLAMA_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.TROPICAL_FISH_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.TURTLE_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.VILLAGER_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.WANDERING_TRADER_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.WOLF_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		
		restrictedItems.put(Material.BAT_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.BLAZE_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.CAVE_SPIDER_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.CREEPER_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.DROWNED_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.ELDER_GUARDIAN_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.ENDERMAN_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.ENDERMITE_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.EVOKER_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.GHAST_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.GUARDIAN_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.HUSK_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.PHANTOM_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.PILLAGER_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.POLAR_BEAR_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.RAVAGER_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.SHULKER_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.SILVERFISH_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.SKELETON_HORSE_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.SKELETON_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.SLIME_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.SPIDER_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.SQUID_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.STRAY_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.VEX_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.VINDICATOR_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.WITCH_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.WITHER_SKELETON_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.ZOMBIE_HORSE_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.ZOMBIE_PIGMAN_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.ZOMBIE_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.ZOMBIE_VILLAGER_SPAWN_EGG, PermissionsList.KIT_MOBS);
		
		restrictedItems.put(Material.SPAWNER, PermissionsList.KIT_MOBS);
		
		restrictedItems.put(Material.DROPPER, PermissionsList.KIT_REDSTONE);
		restrictedItems.put(Material.DISPENSER, PermissionsList.KIT_REDSTONE);
		restrictedItems.put(Material.REPEATER, PermissionsList.KIT_REDSTONE);
		restrictedItems.put(Material.REDSTONE_TORCH, PermissionsList.KIT_REDSTONE);
		restrictedItems.put(Material.PISTON, PermissionsList.KIT_REDSTONE);
		restrictedItems.put(Material.STICKY_PISTON, PermissionsList.KIT_REDSTONE);
		restrictedItems.put(Material.COMPARATOR, PermissionsList.KIT_REDSTONE);
		restrictedItems.put(Material.DAYLIGHT_DETECTOR, PermissionsList.KIT_REDSTONE);
		
		restrictedItems.put(Material.LAVA, PermissionsList.KIT_LAVA);
		restrictedItems.put(Material.LAVA_BUCKET, PermissionsList.KIT_LAVA);

		restrictedItems.put(Material.COMMAND_BLOCK, PermissionsList.KIT_COMMAND_BLOCKS);
		restrictedItems.put(Material.CHAIN_COMMAND_BLOCK, PermissionsList.KIT_COMMAND_BLOCKS);
		restrictedItems.put(Material.REPEATING_COMMAND_BLOCK, PermissionsList.KIT_COMMAND_BLOCKS);
    }
}
