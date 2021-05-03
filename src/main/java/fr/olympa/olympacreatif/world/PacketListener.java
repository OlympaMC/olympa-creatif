package fr.olympa.olympacreatif.world;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.CommandBlock;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftCommandBlock;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.perks.KitsManager;
import fr.olympa.olympacreatif.perks.KitsManager.KitType;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotId;
import fr.olympa.olympacreatif.utils.NBTcontrollerUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_16_R3.BlockCommand;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.Blocks;
import net.minecraft.server.v1_16_R3.CommandBlockListenerAbstract;
import net.minecraft.server.v1_16_R3.EnumDirection;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.IMaterial;
import net.minecraft.server.v1_16_R3.ItemStack;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.PacketPlayInJigsawGenerate;
import net.minecraft.server.v1_16_R3.PacketPlayInSetCommandBlock;
import net.minecraft.server.v1_16_R3.PacketPlayInSetCreativeSlot;
import net.minecraft.server.v1_16_R3.PacketPlayInSetJigsaw;
import net.minecraft.server.v1_16_R3.PacketPlayInStruct;
import net.minecraft.server.v1_16_R3.TileEntity;
import net.minecraft.server.v1_16_R3.TileEntityCommand;
import net.minecraft.server.v1_16_R3.TileEntityCommand.Type;
import net.minecraft.server.v1_16_R3.WorldServer;

public class PacketListener implements Listener {

	OlympaCreatifMain plugin;
	private Set<UUID> blockedPlayers = new HashSet<UUID>();
	
	private final int maxPacketsPerPeriod = 100;
	private Map<UUID, Long[]> packetsLimiter = new HashMap<UUID, Long[]>();

	private Field packetSetInSlotSlot;
	private Field packetSetInCreativeSlotItem;
	
	private static final ItemStack airItem = CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(Material.AIR)); 
	
	public PacketListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		try {
			packetSetInSlotSlot = PacketPlayInSetCreativeSlot.class.getDeclaredField("slot");
			packetSetInSlotSlot.setAccessible(true);
			packetSetInCreativeSlotItem = PacketPlayInSetCreativeSlot.class.getDeclaredField("b");
			packetSetInCreativeSlotItem.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			plugin.getLogger().warning("Failed to reflect class PacketPlayInSetCreativeSlot!");
			e.printStackTrace();
		}
		
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
                
                /*if (packetsLimiter.get(player.getUniqueId())[0]++ > maxPacketsPerPeriod)
                	return;*/
                
            	if (player.isOp() && !PermissionsList.STAFF_BYPASS_OP_CHECK.hasPermission(p)) {
            		if (blockedPlayers.add(player.getUniqueId())) {
            			player.sendMessage("§2Very interesting!! §aHow did you get operator permissions? §bAnyway, you won't be able to do anything. §6Don't forget to have fun on Olympa!\n§7If you think that's an error (but I think it isn't), please contact a server administrator.\n§a");
            			plugin.getTask().runTaskLater(() -> blockedPlayers.remove(player.getUniqueId()), 20*30);
            		}
            		return;
            	}
            	
            	if (handledPacket instanceof PacketPlayInSetCommandBlock) {
            		PacketPlayInSetCommandBlock packet = (PacketPlayInSetCommandBlock) handledPacket;
            			System.out.println("COMMANDBLOCK PACKET RECIEVED : \n"
            					+ "position = " + packet.b() + 
            					"\nc = " + packet.c() + 
            					"\nd = " + packet.d() + 
            					"\ne = " + packet.e() + 
            					"\nf = " + packet.f() + 
            					"\ng = " + packet.g() + "\n\n");
            			
               		 handleCbPacket(packet);
               		 
            		 /*Plot plot = plugin.getPlotsManager().getPlot(PlotId.fromPosition(plugin, pos.getX(), pos.getZ()));
            		 if (plot != null)
            			 plot.getCbData().handleSetCommandBlockPacket((PacketPlayInSetCommandBlock) handledPacket);*/
            		 
            		 return;
            	}
                
            	if (handledPacket instanceof PacketPlayInSetCreativeSlot) {
            		PacketPlayInSetCreativeSlot packet = ((PacketPlayInSetCreativeSlot) handledPacket);
            		
            		if (packet.getItemStack() != null){
            			Material mat = CraftItemStack.asBukkitCopy(packet.getItemStack()).getType();
            			/*System.out.println("Detected item " + mat + " for " + p.getName() + ". Have perm : " + plugin.getPerksManager().getKitsManager().
                				hasPlayerPermissionFor(p, mat));*/
            			
            			KitType kit = plugin.getPerksManager().getKitsManager().getKitOf(mat);
            			
                		if (kit != null && !p.hasKit(kit)) {
                			packetSetInCreativeSlotItem.set(packet, plugin.getPerksManager().getKitsManager().getNoKitPermItemNMS(mat));
                			p.getPlayer().updateInventory();
                			
                			NBTTagCompound tag = new NBTTagCompound();
                			plugin.getPerksManager().getKitsManager().getNoKitPermItemNMS(mat).save(tag);
                			
                			//System.out.println("TAG de la stone : " + tag.asString());
                			//OCmsg.INSUFFICIENT_KIT_PERMISSION.send(p, kit);
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
    
    public void handleCbPacket(PacketPlayInSetCommandBlock packet) {
    	plugin.getTask().runTask(() -> {
    		String cmd = packet.c();
        	boolean conditional = packet.e();
        	boolean needRedstone = !packet.f();
        	Material mat = packet.g() == Type.REDSTONE ?
        			Material.COMMAND_BLOCK : 
        				packet.g() == Type.SEQUENCE ?
        						Material.CHAIN_COMMAND_BLOCK :
        						Material.REPEATING_COMMAND_BLOCK;
        	
        	Location loc = new Location(plugin.getWorldManager().getWorld(), packet.b().getX(), packet.b().getY(), packet.b().getZ());
        	 if (!loc.isChunkLoaded())
        		 return;
        	 
        	 Block block = loc.getBlock();
        	 
        	 if (block.getBlockData() instanceof CommandBlock) {
        		 block.setType(mat);
        		 
        		 NBTTagCompound tag = new NBTTagCompound();
        		 TileEntity tile = plugin.getWorldManager().getNmsWorld().getTileEntity(packet.b());
        		 tile.save(tag);
        		 tag.setByte("auto", needRedstone ? (byte)0 : (byte)1);
        		 tag.setString("Command", cmd);
        		 tile.load(tile.getBlock(), tag);
        		 
        		//org.bukkit.block.CommandBlock cb = (org.bukkit.block.CommandBlock)block.getState();
        		 CommandBlock cbData = (CommandBlock) block.getBlockData();        		 
        		 cbData.setConditional(conditional);
        		 block.setBlockData(cbData, false);
        		 
        	 }else {
        		 plugin.getLogger().warning("§cTrying to set commandblock metadata at " + SpigotUtils.convertLocationToHumanString(loc) + ", but there is no commandblock at this location!");
        	 }
    	});
    }
    
    /*public void handleCbPacket(PacketPlayInSetCommandBlock packet) {
    	
    	System.out.println("Commandblock packed is being used to setup commandblock...");
    	
       WorldServer world = ((CraftWorld)plugin.getWorldManager().getWorld()).getHandle();
   	
       CommandBlockListenerAbstract commandblocklistenerabstract = null;
       TileEntityCommand tileentitycommand = null;
       BlockPosition blockposition = packet.b();
       TileEntity tileentity = world.getTileEntity(blockposition);
		
       if (tileentity instanceof TileEntityCommand) {
           tileentitycommand = (TileEntityCommand) tileentity;
           commandblocklistenerabstract = tileentitycommand.getCommandBlock();
       }
		
       String s = packet.c();
       boolean flag = packet.d();
		
       if (commandblocklistenerabstract != null) {
           IBlockData iblockdata;
           TileEntityCommand.Type tileentitycommand_type = tileentitycommand.m();
           EnumDirection enumdirection = (EnumDirection) world.getType(blockposition).get(BlockCommand.a);
			
           switch (packet.g()) {
               case SEQUENCE:
                   iblockdata = Blocks.CHAIN_COMMAND_BLOCK.getBlockData();
                   world.setTypeAndData(blockposition, (IBlockData)((IBlockData) iblockdata.set(BlockCommand.a, enumdirection)).set(BlockCommand.b, Boolean.valueOf(packet.e())), 2);
                   break;
               case AUTO:
                   iblockdata = Blocks.REPEATING_COMMAND_BLOCK.getBlockData();
                   world.setTypeAndData(blockposition, (IBlockData)((IBlockData) iblockdata.set(BlockCommand.a, enumdirection)).set(BlockCommand.b, Boolean.valueOf(packet.e())), 2);
                   break;
               default:
                   iblockdata = Blocks.COMMAND_BLOCK.getBlockData();
                   world.setTypeAndData(blockposition, (IBlockData)((IBlockData) iblockdata.set(BlockCommand.a, enumdirection)).set(BlockCommand.b, Boolean.valueOf(packet.e())), 2);
                   break;
           }
           tileentity.r();
           world.setTileEntity(blockposition, tileentity);
			
           commandblocklistenerabstract.setCommand(s);
           commandblocklistenerabstract.a(flag);
			
           //if (!flag) 
				//commandblocklistenerabstract.b(null);
				
           tileentitycommand.b(packet.f());
			
           if (tileentitycommand_type != packet.g()) 
				tileentitycommand.h();
				
           commandblocklistenerabstract.e();
			
           //if (!UtilColor.b(s)) this.player.sendMessage(new ChatMessage("advMode.setCommand.success", new Object[] {s}), SystemUtils.b);
       }
    }*/
}
