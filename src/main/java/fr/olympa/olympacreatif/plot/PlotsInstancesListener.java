package fr.olympa.olympacreatif.plot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.CommandBlock;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityPathfindEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import com.google.common.collect.ImmutableSet;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.commands.CmdSummon;
import fr.olympa.olympacreatif.data.FakePlayerDeathEvent;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.data.PermissionsManager.ComponentCreatif;
import fr.olympa.olympacreatif.perks.KitsManager.KitType;
import fr.olympa.olympacreatif.plot.PlotStoplagChecker.StopLagDetect;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.PacketPlayOutTileEntityData;

public class PlotsInstancesListener implements Listener{

	private OlympaCreatifMain plugin;
	private Plot plot;
	
	//private Map<UUID, List<ItemStack>> itemsToKeepOnDeath = new HashMap<UUID, List<ItemStack>>();
	
	private Set<Material> commandBlockTypes = new HashSet<Material>(Arrays.asList(new Material[] {Material.COMMAND_BLOCK, Material.CHAIN_COMMAND_BLOCK, Material.REPEATING_COMMAND_BLOCK}));

	//gère le placement des commandblocks
	private List<Player> cbPlacementPlayer = new ArrayList<Player>();
	private List<Location> cbPlacementLocation = new ArrayList<Location>();
	private List<Material> cbPlacementTypeCb = new ArrayList<Material>();
	
	private Set<Material> interractProhibitedItems = ImmutableSet.<Material>builder()
			.add(Material.WATER_BUCKET)
			.add(Material.WATER)
			.add(Material.LAVA_BUCKET)
			.add(Material.LAVA)
			
			.add(Material.HOPPER_MINECART)
			.add(Material.FURNACE_MINECART)
			.add(Material.CHEST_MINECART)
			.add(Material.TNT_MINECART)
			.add(Material.MINECART)

			.add(Material.ACACIA_BOAT)
			.add(Material.JUNGLE_BOAT)
			.add(Material.DARK_OAK_BOAT)
			.add(Material.BIRCH_BOAT)
			.add(Material.OAK_BOAT)
			.add(Material.SPRUCE_BOAT)
			
			.add(Material.BONE_MEAL)
			
			.add(Material.ARMOR_STAND)

			.addAll(Stream.of(Material.values()).filter(mat -> mat.toString().contains("_EGG")).collect(Collectors.toSet()).iterator())
			.addAll(Stream.of(Material.values()).filter(mat -> mat.toString().contains("BUCKET")).collect(Collectors.toSet()).iterator())
			.build();
	
	public PlotsInstancesListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		
		//gère le remplacement des commandblock dans la main du joueur (après le remplacement initial pour permettre le placement du cb)
		new BukkitRunnable() {
			
			@Override
			public void run() {
				
				//set command block
				for (int i = 0 ; i < cbPlacementLocation.size() ; i++) {
					Player p = cbPlacementPlayer.get(i);
					Location loc = cbPlacementLocation.get(i);
					Material mat = cbPlacementTypeCb.get(i);
					
					if (!commandBlockTypes.contains(mat))
						continue;
					
					if (p.getInventory().getItemInMainHand().getType() != null)
						p.getInventory().getItemInMainHand().setType(mat);
					
					if (plugin.getWorldManager().getWorld().getBlockAt(loc).getBlockData() instanceof Dispenser) {
						Dispenser disp = (Dispenser) plugin.getWorldManager().getWorld().getBlockAt(loc).getBlockData();
						BlockFace face = disp.getFacing();
						
						Block targetBlock = plugin.getWorldManager().getWorld().getBlockAt(loc); 
						targetBlock.setType(mat);
						
						CommandBlock targetBlockData = (CommandBlock) targetBlock.getBlockData();
						targetBlockData.setFacing(face);
						targetBlock.setBlockData(targetBlockData);
					}
				}
				
				cbPlacementLocation.clear();
				cbPlacementPlayer.clear();
				cbPlacementTypeCb.clear();
			}
		}.runTaskTimer(plugin, 10, 1);
	}
	
	/*@EventHandler(priority = EventPriority.HIGHEST)
	public void onEvent(HangingEvent e) {
		if (e instanceof Cancellable)
			if (((Cancellable)e).isCancelled())
				Bukkit.broadcastMessage("§cCancelled event : "+ e);
			else
				Bukkit.broadcastMessage("§aNot cancelled event : "+ e);
		else
			Bukkit.broadcastMessage("§7Uncancellable event : "+ e);
	}*/

	////////////////////////////////////////////////////////////
	//                      BLOCKS EVENTS                     //
	////////////////////////////////////////////////////////////
	
	@EventHandler(ignoreCancelled = true) //test place block (autorisé uniquement pour les membres et pour la zone protégeé)
	public void onPlaceBlockEvent(BlockPlaceEvent e) {
		OlympaPlayerCreatif pc = AccountProvider.get(e.getPlayer().getUniqueId());
		if (pc.hasStaffPerm(StaffPerm.BUILD_ROADS))
			return;
		
		plot = plugin.getPlotsManager().getPlot(e.getBlockPlaced().getLocation());
		if (plot == null || plugin.getWEManager().isReseting(plot)) {
			OCmsg.PLOT_CANT_BUILD.send(pc);
			e.setCancelled(true);
			return;	
		}
		
		if (!PlotPerm.BUILD.has(plot, pc)) {
			e.setCancelled(true);
			OCmsg.PLOT_CANT_BUILD.send(pc);
			return;
		}
		
		//détection placement spawner
		/*
		if (e.getBlock().getType() == Material.SPAWNER) {
			TileEntity tile = plugin.getWorldManager().getNmsWorld().getTileEntity(new BlockPosition(e.getBlockPlaced().getLocation().getBlockX(), e.getBlockPlaced().getLocation().getBlockY(), e.getBlockPlaced().getLocation().getBlockZ()));
			
			if (tile != null) {
				net.minecraft.server.v1_16_R3.ItemStack item = CraftItemStack.asNMSCopy(e.getItemInHand());
				NBTTagCompound tag = new NBTTagCompound();
				
				if (item.hasTag())
					item.save(tag);
				
				if (tag.hasKey("tag"))
					tile.load(tag.getCompound("tag"));
			}
		}
		*/
	}
	
	@EventHandler(ignoreCancelled = true) //test break block (autorisé uniquement pour les membres et pour la zone protégeé)
	public void onBreakBlockEvent(BlockBreakEvent e) {
		OlympaPlayerCreatif pc = AccountProvider.get(e.getPlayer().getUniqueId());
		if (pc.hasStaffPerm(StaffPerm.BUILD_ROADS))
			return;
		
		plot = plugin.getPlotsManager().getPlot(e.getBlock().getLocation());
		if (plot == null || plugin.getWEManager().isReseting(plot)) {
			e.setCancelled(true);
			OCmsg.PLOT_CANT_BUILD.send(pc);
			return;	
		}

		if (!PlotPerm.BUILD.has(plot, pc)) {
			e.setCancelled(true);
			OCmsg.PLOT_CANT_BUILD.send(pc);
		}
	}
	
	@EventHandler//cancel redstone si stoplag, sinon enregistre l'évent dans le stoplag checker
	public void onRedstoneChange(BlockRedstoneEvent e) {
		plot = plugin.getPlotsManager().getPlot(e.getBlock().getLocation());

		if (plot == null || plot.hasStoplag() || !ComponentCreatif.REDSTONE.isActivated())
			e.setNewCurrent(0);
		else
			if (plot.hasStoplag())
				e.setNewCurrent(0);
			else if (e.getBlock().getType() == Material.REDSTONE_LAMP)
				plot.getStoplagChecker().addEvent(StopLagDetect.LAMP);
			else if (e.getBlock().getType() == Material.REDSTONE_WIRE)
				plot.getStoplagChecker().addEvent(StopLagDetect.WIRE);
		
			else if (commandBlockTypes.contains(e.getBlock().getType()))
				plot.getCbData().handleCommandBlockPowered(e);
		
		//Bukkit.broadcastMessage("REDSTONE EVENT plot : " + plot + ", new current : " + e.getNewCurrent());
	}
	
	@EventHandler //test print TNT
	public void onPrintTnt(BlockIgniteEvent e) {
		plot = plugin.getPlotsManager().getPlot(e.getBlock().getLocation());

		OlympaPlayerCreatif pc = AccountProvider.get(e.getPlayer().getUniqueId());
		
		if (plot == null || plot.hasStoplag()) {
			e.setCancelled(true);
			return;	
		}
		
		if (e.getPlayer() == null || (e.getCause() != IgniteCause.ARROW && e.getCause() != IgniteCause.FLINT_AND_STEEL)) {
			e.setCancelled(true);
			return;
		}
		if (e.getBlock().getType() != Material.TNT && !PlotPerm.BUILD.has(plot, pc)) {
			e.setCancelled(true);
			return;
		}
		if (!plot.getParameters().getParameter(PlotParamType.ALLOW_PRINT_TNT) && !PlotPerm.BUILD.has(plot, pc)) {
			e.setCancelled(true);
			OCmsg.PLOT_CANT_PRINT_TNT.send(pc);
		}
	}
	
	
	
	@EventHandler(priority = EventPriority.LOW)//cancel rétractation piston si un bloc affecté se trouve sur une route
	public void onPistonRetractEvent(BlockPistonRetractEvent e) {
		if (e.isCancelled())
			return;
		
		Plot plot = plugin.getPlotsManager().getPlot(e.getBlock().getLocation());
		
		if (plot == null || plot.hasStoplag()) {
			e.setCancelled(true);
			return;	
		}
		
		if (e.getBlocks().stream().anyMatch(block -> !plot.getId().equals(PlotId.fromLoc(plugin, block.getLocation()))))
			e.setCancelled(true);
		
		plot.getStoplagChecker().addEvent(StopLagDetect.PISTON);
	}
	
	@EventHandler(priority = EventPriority.LOW) //cancel poussée piston si un bloc affecté se trouve sur une route
	public void onPistonPushEvent(BlockPistonExtendEvent e) {
		if (e.isCancelled())
			return;
		
		Plot plot = plugin.getPlotsManager().getPlot(e.getBlock().getLocation());
		
		if (plot == null || plot.hasStoplag()) {
			e.setCancelled(true);
			return;	
		}
		
		if (e.getBlocks().stream().anyMatch(block -> (!plot.getId().equals(PlotId.fromLoc(plugin, block.getLocation())) || 
				!plot.getId().isInPlot(block.getLocation(), 1)) ))
			e.setCancelled(true);
		
		plot.getStoplagChecker().addEvent(StopLagDetect.PISTON);
	}
	
	@EventHandler //cancel lava/water flow en dehors du plot. Cancel aussi toute téléportation d'un oeuf de dragon
	public void onLiquidFlow(BlockFromToEvent e) {
		Plot plot = plugin.getPlotsManager().getPlot(e.getToBlock().getLocation());

		//Bukkit.broadcastMessage("FROM TO EVENT : " + plot + ", loc : " + e.getBlock().getLocation());
		
		if (plot == null || plot.hasStoplag() || !plot.hasLiquidFlow() || e.getBlock().getType() == Material.DRAGON_EGG) {
			e.setCancelled(true);
			return;
		}
		
		plot.getStoplagChecker().addEvent(StopLagDetect.LIQUID);
	}
	
	@EventHandler //cancel pousse d'arbres, etc en dehors d'un plot
	public void onGrowStructure(StructureGrowEvent e) {
		Plot plot = plugin.getPlotsManager().getPlot(e.getLocation());
		if (plot == null) {
			e.setCancelled(true);
			return;
		}
		for (BlockState b : e.getBlocks())
			if (!plot.equals(plugin.getPlotsManager().getPlot(b.getLocation()))) {
				e.setCancelled(true);
				return;
			}
	}
	
	@EventHandler //cancel pousse céréale, citrouille, ...
	public void onGrowBlock(BlockGrowEvent e) {
		Plot plot = plugin.getPlotsManager().getPlot(e.getBlock().getLocation());
		
		if (plot == null || !plot.getId().isInPlot(e.getBlock().getLocation()))
			e.setCancelled(true);
	}
	
	@EventHandler
	public void onDispense(BlockDispenseEvent e) {
		Plot plot = plugin.getPlotsManager().getPlot(e.getBlock().getLocation());
		
		if (plot == null || !plot.getId().isInPlot(e.getBlock().getLocation(), 1))
			e.setCancelled(true);
	}

	////////////////////////////////////////////////////////////
	//               POTIONS & INTERRACT EVENTS               //
	////////////////////////////////////////////////////////////
	
	@EventHandler //cancel splash potion sin interdites dans le plot
	public void onPotionThrows(PotionSplashEvent e) {
		if (e.getAffectedEntities().size() == 0)
			return;
		
		plot = plugin.getPlotsManager().getPlot(((Entity) e.getAffectedEntities().toArray()[0]).getLocation());
		
		if (e.isCancelled() || plot == null)
			return;
		
		if(!plot.getParameters().getParameter(PlotParamType.ALLOW_SPLASH_POTIONS) || plot.hasStoplag())
			e.setCancelled(true);
	}
	
	
	@EventHandler //test interract block (cancel si pas la permission d'interagir avec le bloc) & test placement liquide
	public void onInterractEvent(PlayerInteractEvent e) {
		OlympaPlayerCreatif pc = ((OlympaPlayerCreatif)AccountProvider.get(e.getPlayer().getUniqueId()));
		
		Block clickedBlock = e.getClickedBlock();
		
		//detect if clicked on water or on block
		if (e.getClickedBlock() == null) 
			for (Block b : e.getPlayer().getLineOfSight(null, 6))
				if (clickedBlock == null && b.getType() == Material.WATER)
					clickedBlock = b;
		
		if (clickedBlock == null)
			return;

		plot = plugin.getPlotsManager().getPlot(clickedBlock.getLocation());

		
		if (pc.hasStaffPerm(StaffPerm.BUILD_ROADS) && plot == null)
			return;

		if (plot == null) {
			if (!pc.hasStaffPerm(StaffPerm.WORLDEDIT)) {
				e.setCancelled(true);
				OCmsg.PLOT_CANT_INTERRACT_NULL_PLOT.send(pc);
			}
			return;
		}
		
		//test si permission d'interagir avec le bloc donné
		if (!PlotPerm.BUILD.has(plot, pc)) {
			if (PlotParamType.getAllPossibleIntaractibleBlocks().contains(clickedBlock.getType()) &&! plot.getParameters().getParameter(PlotParamType.LIST_ALLOWED_INTERRACTION).contains(clickedBlock.getType()) ) {
				e.setCancelled(true);
				OCmsg.PLOT_CANT_INTERRACT.send(pc);
				
			}else if (e.getItem() != null && interractProhibitedItems.contains(e.getItem().getType())) {
				OCmsg.PLOT_ITEM_PROHIBITED_USED.send(pc);
				e.setCancelled(true);
			}
			
			return;
		}
		
		//GESTION COMMANDBLOCKS
		if (commandBlockTypes.contains(clickedBlock.getType())) {
			if (!PlotPerm.COMMAND_BLOCK.has(plot, pc))
				OCmsg.INSUFFICIENT_PLOT_PERMISSION.send(pc, PlotPerm.COMMAND_BLOCK);
				
			else if (e.getAction() == Action.LEFT_CLICK_BLOCK && (e.getItem() == null || e.getItem().getType() != Material.WOODEN_AXE))
				clickedBlock.setType(Material.AIR);
			
			else if (!KitType.COMMANDBLOCK.hasKit(pc)) 
				OCmsg.INSUFFICIENT_KIT_PERMISSION.send(pc, KitType.COMMANDBLOCK);
				
			//TODO retirer ce truc moche !
			else if (!pc.getPlayer().isSneaking() && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				
				BlockPosition pos = new BlockPosition(clickedBlock.getLocation().getBlockX(), clickedBlock.getLocation().getBlockY(), clickedBlock.getLocation().getBlockZ());
				NBTTagCompound tag = new NBTTagCompound();
				
				plugin.getWorldManager().getNmsWorld().getTileEntity(pos).save(tag);
				
				PacketPlayOutTileEntityData packet = new PacketPlayOutTileEntityData(pos, 2, tag);
				
		        EntityPlayer nmsPlayer = ((CraftPlayer) e.getPlayer()).getHandle();
		        nmsPlayer.playerConnection.sendPacket(packet);
		        e.setUseItemInHand(Result.DENY);
		        
			}
		}
		
		if ((e.useItemInHand() != Result.DENY || e.getPlayer().isSneaking()) && e.getItem() != null && commandBlockTypes.contains(e.getItem().getType())) {
			
			if (!KitType.COMMANDBLOCK.hasKit(pc)) {
				OCmsg.INSUFFICIENT_KIT_PERMISSION.send(pc, KitType.COMMANDBLOCK);
				return;
			}
			
			if (plot.getCommandBlocksCount() > 5) {
				pc.getPlayer().sendMessage("[DEBUG] plus de 5 commandblocks ont été posés, pas bien !!");
				return;
			}
			
			//return si le Y est trop bas ou trop haut
			if (clickedBlock.getLocation().getBlockY() < 2 || clickedBlock.getLocation().getBlockY() > 254)
				return;
			
			Location loc = null;
			
			switch(e.getBlockFace()) {
			case DOWN:
				loc = clickedBlock.getLocation().clone().add(0, -1, 0);
				break;
			case EAST:
				loc = clickedBlock.getLocation().clone().add(1, 0, 0);
				break;
			case NORTH:
				loc = clickedBlock.getLocation().clone().add(0, 0, -1);
				break;
			case SOUTH:
				loc = clickedBlock.getLocation().clone().add(0, 0, 1);
				break;
			case UP:
				loc = clickedBlock.getLocation().clone().add(0, 1, 0);
				break;
			case WEST:
				loc = clickedBlock.getLocation().clone().add(-1, 0, 0);
				break;
			default:
				return;
			}
			
			cbPlacementLocation.add(loc);
			cbPlacementPlayer.add(e.getPlayer());
			cbPlacementTypeCb.add(e.getItem().getType());
			
			e.getItem().setType(Material.DISPENSER);
		}
		
		//GESTION COMMAND BLOCKS
		//si édition/placement du commandblock
		/*
		if (PlotPerm.COMMAND_BLOCK.has(plot, pc) && clickedBlock != null && 
				plugin.getPerksManager().getKitsManager().hasPlayerPermissionFor(pc, clickedBlock.getType())) {
			
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				ItemStack item = e.getItem();
				
				//si le block cliqué est un commandblock, ouverture interface
				if (commandBlockTypes.contains(clickedBlock.getType()) && !e.getPlayer().isSneaking()) {
					
					BlockPosition pos = new BlockPosition(clickedBlock.getLocation().getBlockX(), clickedBlock.getLocation().getBlockY(), clickedBlock.getLocation().getBlockZ());
					NBTTagCompound tag = new NBTTagCompound();
					
					plugin.getWorldManager().getNmsWorld().getTileEntity(pos).save(tag);
					
					PacketPlayOutTileEntityData packet = new PacketPlayOutTileEntityData(pos, 2, tag);
					
			        EntityPlayer nmsPlayer = ((CraftPlayer) e.getPlayer()).getHandle();
			        nmsPlayer.playerConnection.sendPacket(packet);
			        e.setCancelled(true);
			        
				//si l'item en main est un commandblock, placement de ce dernier
				}else if (item != null && commandBlockTypes.contains(item.getType())){
					
					//return si le Y est trop bas ou trop haut
					if (clickedBlock.getLocation().getBlockY() < 2 || clickedBlock.getLocation().getBlockY() > 254)
						return;
					
					Location loc = null;
					
					switch(e.getBlockFace()) {
					case DOWN:
						loc = clickedBlock.getLocation().add(0, -1, 0);
						break;
					case EAST:
						loc = clickedBlock.getLocation().add(1, 0, 0);
						break;
					case NORTH:
						loc = clickedBlock.getLocation().add(0, 0, -1);
						break;
					case SOUTH:
						loc = clickedBlock.getLocation().add(0, 0, 1);
						break;
					case UP:
						loc = clickedBlock.getLocation().add(0, 1, 0);
						break;
					case WEST:
						loc = clickedBlock.getLocation().add(-1, 0, 0);
						break;
					default:
						return;
					}
					
					cbPlacementLocation.add(loc);
					cbPlacementPlayer.add(e.getPlayer());
					cbPlacementTypeCb.add(e.getItem().getType());
					
					e.getItem().setType(Material.DISPENSER);
				}
			}else if (e.getAction() == Action.LEFT_CLICK_BLOCK && (e.getItem() == null || e.getItem().getType() != Material.WOODEN_AXE)) {
				if (commandBlockTypes.contains(clickedBlock.getType()))
					clickedBlock.setType(Material.AIR);
			}
		}*/
	}
	
	@EventHandler //cancel interraction avec un itemframe
	public void onInterractEntityEvent(PlayerInteractEntityEvent e) {
		plot = plugin.getPlotsManager().getPlot(e.getRightClicked().getLocation());
		if (plot == null)
			return;

		OlympaPlayerCreatif pc = AccountProvider.get(e.getPlayer().getUniqueId());
		
		if (!PlotPerm.BUILD.has(plot, pc) && !(e.getRightClicked() instanceof Vehicle)) {
			e.setCancelled(true);
			return;
		}
		
		//remove entity si clic droit dessus avec une houe
		if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.WOODEN_HOE && !(e.getRightClicked() instanceof Player) &&
				PlotPerm.BUILD.has(plot, pc)) 
			plot.removeEntityInPlot(e.getRightClicked(), true);
	}

	////////////////////////////////////////////////////////////
	//                       MOVE EVENTS                      //
	////////////////////////////////////////////////////////////
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true) //modifie la destination téléport si joueur banni du plot
	public void onTeleportEvent(PlayerTeleportEvent e) {
		
		Plot plotFrom = plugin.getPlotsManager().getPlot(e.getFrom());
		Plot plotTo = plugin.getPlotsManager().getPlot(e.getTo());
		
		//((OlympaPlayerCreatif)AccountProvider.get(e.getPlayer().getUniqueId())).setCurrentPlot(plotTo);
		
		if (plotFrom == plotTo)
			return;
		
		if (plotTo != null) {
			if (plotTo.canEnter(e.getPlayer())) {
				if (plotFrom != null)
					plotFrom.executeExitActions(e.getPlayer());
				
				plotTo.executeEntryActions(e.getPlayer(), e.getTo());	
			}else
				e.setCancelled(true);
			
		}else if (plotFrom != null) 
			plotFrom.executeExitActions(e.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.LOWEST) //actions à effectuer lors de la sortie/entrée d'un joueur
	public void onPlayerMove(PlayerMoveEvent e) {
		if (e.getFrom().getChunk() == e.getTo().getChunk())
			return;
		
		
		Plot plotTo = plugin.getPlotsManager().getPlot(e.getTo());
		Plot plotFrom = plugin.getPlotsManager().getPlot(e.getFrom());

		//Bukkit.broadcastMessage("DETECTED chunk SWITCH FOR " + e.getPlayer().getName() + " : " + plotFrom + " TO " + plotTo);
		
		//sortie de l'évent si pas de changement de plot
		if (plotTo == plotFrom)
			return;
		
		if (plotTo != null) {
			if (plotTo.canEnter(e.getPlayer())) {
				if (plotFrom != null)
					plotFrom.executeExitActions(e.getPlayer());
				
				plotTo.executeEntryActions(e.getPlayer(), e.getTo());	
			}else
				e.setCancelled(true);
			
		}else if (plotFrom != null) 
			plotFrom.executeExitActions(e.getPlayer());
	}

	////////////////////////////////////////////////////////////
	//                      DAMAGE EVENTS                     //
	////////////////////////////////////////////////////////////
	
	@EventHandler(priority = EventPriority.LOW) //gestion autorisation pvp & fake player death
	public void onDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.isCancelled())
			return;
		
		plot = plugin.getPlotsManager().getPlot(e.getEntity().getLocation());
		
		if (plot == null) {
			e.setCancelled(true);
			return;	
		}
		
		if (e.getDamager().getType() == EntityType.PLAYER && PlotPerm.BUILD.has(plot, (OlympaPlayerCreatif)AccountProvider.get(e.getDamager().getUniqueId())))
			return;
		
		if (!plot.getParameters().getParameter(PlotParamType.ALLOW_PVP) && e.getEntityType() == EntityType.PLAYER && e.getDamager().getType() == EntityType.PLAYER) {
			e.setCancelled(true);
			return;
		}
		
		if (!plot.getParameters().getParameter(PlotParamType.ALLOW_PVE) && (e.getEntityType() != EntityType.PLAYER || e.getDamager().getType() != EntityType.PLAYER)) {
			e.setCancelled(true);
			return;
		}
		
		if (e.getEntity().getType() == EntityType.PLAYER)
			if (FakePlayerDeathEvent.fireFakeDeath(plugin, (Player) e.getEntity(), e.getDamager(), e.getFinalDamage(), plot))
				e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOW) //gestion autorisation dégâts environementaux
	public void onGeneralDamage(EntityDamageEvent e) {
		if (e instanceof EntityDamageByEntityEvent || e.getEntityType() != EntityType.PLAYER)
			return;
		
		plot = plugin.getPlotsManager().getPlot(e.getEntity().getLocation());
		
		if (plot == null) {
			e.setCancelled(true);
			return;	
		}
		
		if (!plot.getParameters().getParameter(PlotParamType.ALLOW_ENVIRONMENT_DAMAGE)) {
			e.setCancelled(true);
			return;
		}
		
		if (e.getEntity().getType() == EntityType.PLAYER)
			if (FakePlayerDeathEvent.fireFakeDeath(plugin, (Player) e.getEntity(), null, e.getFinalDamage(), plot))
				e.setCancelled(true);
	}

	////////////////////////////////////////////////////////////
	//                      ENTITY EVENTS                     //
	////////////////////////////////////////////////////////////

	@EventHandler //cancel remove paintings et itemsframes
	public void onItemFrameDestroy(HangingBreakByEntityEvent e) {
		//Bukkit.broadcastMessage("Hanging break : " + e.getEntity().getLocation());
		
		if (e.getRemover().getType() == null)
			return;
		
		if (e.getRemover().getType() != EntityType.PLAYER) {
			e.setCancelled(true);
			return;
		}
		
		plot = plugin.getPlotsManager().getPlot(e.getEntity().getLocation());
		if (plot == null)
			return;
		
		if (!PlotPerm.BUILD.has(plot, AccountProvider.get(e.getRemover().getUniqueId()))) {
			e.setCancelled(true);	
			return;
		}
		
		plot.removeEntityInPlot(e.getEntity(), false);
	}

	@EventHandler //cancel place
	public void onItemFramePlace(HangingPlaceEvent e) {
		Plot plot = plugin.getPlotsManager().getPlot(e.getEntity().getLocation());
		
		if (plot == null)
			e.setCancelled(true);
		
		else if (!PlotPerm.BUILD.has(plot, AccountProvider.get(e.getPlayer().getUniqueId()))) {
			OCmsg.INSUFFICIENT_PLOT_PERMISSION.send(e.getPlayer(), PlotPerm.BUILD);
			e.setCancelled(true);
		}
	}
	
	@EventHandler //empêche le drop d'items si interdit sur le plot (et cancel si route)
	public void onDropItem(PlayerDropItemEvent e) {
		plot = plugin.getPlotsManager().getPlot(e.getPlayer().getLocation());
		
		if (plot == null) {
			e.setCancelled(true);
			OCmsg.PLOT_DENY_ITEM_DROP.send(e.getPlayer());
			return;	
		}
		
		if (!PlotPerm.DROP_ITEM.has(plot, AccountProvider.get(e.getPlayer().getUniqueId())) && !plot.getParameters().getParameter(PlotParamType.ALLOW_DROP_ITEMS)) {
			e.setCancelled(true);
			OCmsg.PLOT_DENY_ITEM_DROP.send(e.getPlayer());
		}		
	}
	
	@EventHandler//cancel spawn entité si paramètre du plot l'interdit
	public void onProjectileSpawn(ProjectileLaunchEvent e) {
		plot = plugin.getPlotsManager().getPlot(e.getLocation());

		if (plot == null || !plot.getParameters().getParameter(PlotParamType.ALLOW_LAUNCH_PROJECTILES)) {
			e.setCancelled(true);
			return;
		}			
	}
	
	@EventHandler(priority = EventPriority.HIGH) //décide si l'entité aura le droit de spawn
	public void onEntitySpawn(EntitySpawnEvent e) {		
		
		if (e.isCancelled())
			return;
		
		Plot plot = plugin.getPlotsManager().getPlot(e.getLocation());
		if (plot == null || plot.hasStoplag()) {
			e.setCancelled(true);
			return;
		}
	}
	
	@EventHandler //set birth plot of new entities
	public void onEntitySpawn(EntityAddToWorldEvent e) {
		if (e.getEntityType() == EntityType.PLAYER)
			return;

		//Bukkit.broadcastMessage("add entity : " + e.getEntity());
		
		PlotId id = PlotId.fromLoc(plugin, e.getEntity().getLocation());
		
		if (!CmdSummon.allowedEntities.contains(e.getEntityType()) || id == null) {
			e.getEntity().remove();
			return;
		}
			
		Plot plot = plugin.getPlotsManager().getPlot(id);
		if (plot == null)
			return;
		
		plugin.getPlotsManager().setBirthPlot(plot.getId(), e.getEntity());
		
		plot.addEntityInPlot(e.getEntity());
		plot.getStoplagChecker().addEvent(StopLagDetect.ENTITY);
	}
	
	@EventHandler //remove entities from plot entities list
	public void onEntityDespawn(EntityRemoveFromWorldEvent e) {
		if (e.getEntityType() == EntityType.PLAYER)
			return;
		
		plot = plugin.getPlotsManager().getPlot(
				plugin.getPlotsManager().getBirthPlot(
						e.getEntity()));
		
		/*try {
			throw new UnsupportedOperationException("§4[DEBUG] Entity " + e.getEntity() + " removed from " + plot);
		}catch (Exception ex) {
			ex.printStackTrace();
		}*/
		
		if (plot != null)
			plugin.getLogger().info("§aEntity " + e.getEntity() + " removed from plot " + plot);
		else
			plugin.getLogger().info("§cEntity " + e.getEntity() + " removed without birth plot!");
		
		if (plot != null)
			plot.removeEntityInPlot(e.getEntity(), false);
		/*else
			e.getEntity().remove();*/
	}
	
	@EventHandler //cancel entity pathfind of entity try to go outside of the plot
	public void onEntityPathFind(EntityPathfindEvent e) {
		if (e.getEntityType() == EntityType.PLAYER)
			return;
		
		plot = plugin.getPlotsManager().getPlot(plugin.getPlotsManager().getBirthPlot(e.getEntity()));
		
		if (plot == null || !plot.getId().isInPlot(e.getLoc()))
			e.setCancelled(true);
	}

	////////////////////////////////////////////////////////////
	//                      OTHERS EVENTS                     //
	////////////////////////////////////////////////////////////
	
	@EventHandler //ajoute les entités du chunk au plot correspondant s'il existe, sinon les supprimer
	public void onChunkLoad(ChunkLoadEvent e) {
		if (e.isNewChunk())
			return;
		
		Arrays.asList(e.getChunk().getEntities()).forEach(ent -> {
			if (ent.getType() == EntityType.PLAYER)
				return;
			
			PlotId birthPlotId = plugin.getPlotsManager().getBirthPlot(ent);
			
			//si l'entité n'est pas dans son plot d'origine, remove
			if (birthPlotId == null || !birthPlotId.equals(PlotId.fromLoc(plugin, ent.getLocation()))) {
				ent.remove();
				return;
			}
			
			//ajout de l'entité au plot si le plot est déjà chargé
			Plot plot = plugin.getPlotsManager().getPlot(birthPlotId);
			if (plot != null)
				plot.addEntityInPlot(ent);
		});
	}
	
	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent e) {
		new ArrayList<Entity>(Arrays.asList(e.getChunk().getEntities())).forEach(ent -> {
			if (ent.getType() == EntityType.PLAYER)
				return;
			
			Plot plot = plugin.getPlotsManager().getPlot(plugin.getPlotsManager().getBirthPlot(ent));
			if (plot != null)
				plot.removeEntityInPlot(ent, false);
		});
	}
	
	@EventHandler //empêche la nourriture de descendre si paramètre du plot défini comme tel
	public void onFoodChange(FoodLevelChangeEvent e) {
		if (e.getEntityType() != EntityType.PLAYER)
			return;
		
		plot = plugin.getPlotsManager().getPlot(e.getEntity().getLocation());
		if (plot == null)
			return;
		
		if (plot.getParameters().getParameter(PlotParamType.KEEP_MAX_FOOD_LEVEL))
			e.setCancelled(true);
	}
}





