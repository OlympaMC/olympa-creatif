package fr.olympa.olympacreatif.plot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.CommandBlock;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.world.World;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.FakePlayerDeathEvent;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.perks.KitsManager.KitType;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;
import fr.olympa.olympacreatif.plot.PlotStoplagChecker.StopLagDetect;
import fr.olympa.olympacreatif.utils.EntityRemoveEvent;
import fr.olympa.olympacreatif.utils.EntityRemoveListener;

import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.NBTTagList;
import net.minecraft.server.v1_15_R1.NBTTagString;
import net.minecraft.server.v1_15_R1.PacketPlayOutTileEntityData;

public class PlotsInstancesListener implements Listener{

	private OlympaCreatifMain plugin;
	private static Map<Player, List<ItemStack>> inventoryStorage = new HashMap<Player, List<ItemStack>>();
	private Plot plot;
	
	//private Map<UUID, List<ItemStack>> itemsToKeepOnDeath = new HashMap<UUID, List<ItemStack>>();
	
	private List<Material> commandBlockTypes = new ArrayList<Material>(Arrays.asList(new Material[] {Material.COMMAND_BLOCK, Material.CHAIN_COMMAND_BLOCK, Material.REPEATING_COMMAND_BLOCK}));

	//gère le placement des commandblocks
	private List<Player> cbPlacementPlayer = new ArrayList<Player>();
	private List<Location> cbPlacementLocation = new ArrayList<Location>();
	private List<Material> cbPlacementTypeCb = new ArrayList<Material>();
	
	private List<Material> interractProhibitedItems = ImmutableList.<Material>builder()
			.add(Material.WATER_BUCKET)
			.add(Material.WATER)
			.add(Material.LAVA_BUCKET)
			.add(Material.LAVA)
			.add(Material.HOPPER_MINECART)
			.add(Material.FURNACE_MINECART)
			.add(Material.CHEST_MINECART)
			.add(Material.TNT_MINECART)
			.add(Material.MINECART)
			.add(Material.BONE_MEAL)
			.build();
	
	public PlotsInstancesListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		
		plugin.getServer().getPluginManager().registerEvents(new EntityRemoveListener(plugin), plugin);
		
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
						
						Block target = plugin.getWorldManager().getWorld().getBlockAt(loc); 
						target.setType(mat);
						
						CommandBlock data = (CommandBlock) target.getBlockData();
						data.setFacing(face);
						target.setBlockData(data);
					}
				}
				
				cbPlacementLocation.clear();
				cbPlacementPlayer.clear();
				cbPlacementTypeCb.clear();
			}
		}.runTaskTimer(plugin, 10, 1);
	}

	@EventHandler //test place block (autorisé uniquement pour les membres et pour la zone protégeé)
	public void onPlaceBlockEvent(BlockPlaceEvent e) {
		if (e.isCancelled())
			return;
		
		if(((OlympaPlayerCreatif)AccountProvider.get(e.getPlayer().getUniqueId())).hasStaffPerm(StaffPerm.BYPASS_WORLDEDIT))
			return;
		
		plot = plugin.getPlotsManager().getPlot(e.getBlockPlaced().getLocation());
		if (plot == null) {
			e.getPlayer().sendMessage(Message.PLOT_CANT_BUILD.getValue("nul"));
			e.setCancelled(true);
			return;	
		}
		
		if (plot.getMembers().getPlayerRank(e.getPlayer()) == PlotRank.VISITOR) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.PLOT_CANT_BUILD.getValue(plot));
			return;
		}
		
		//détection placement spawner
		/*
		if (e.getBlock().getType() == Material.SPAWNER) {
			TileEntity tile = plugin.getWorldManager().getNmsWorld().getTileEntity(new BlockPosition(e.getBlockPlaced().getLocation().getBlockX(), e.getBlockPlaced().getLocation().getBlockY(), e.getBlockPlaced().getLocation().getBlockZ()));
			
			if (tile != null) {
				net.minecraft.server.v1_15_R1.ItemStack item = CraftItemStack.asNMSCopy(e.getItemInHand());
				NBTTagCompound tag = new NBTTagCompound();
				
				if (item.hasTag())
					item.save(tag);
				
				if (tag.hasKey("tag"))
					tile.load(tag.getCompound("tag"));
			}
		}
		*/
	}
	
	@EventHandler //test break block (autorisé uniquement pour les membres et pour la zone protégeé)
	public void onBreakBlockEvent(BlockBreakEvent e) {
		
		if(((OlympaPlayerCreatif)AccountProvider.get(e.getPlayer().getUniqueId())).hasStaffPerm(StaffPerm.BYPASS_WORLDEDIT))
			return;
		
		plot = plugin.getPlotsManager().getPlot(e.getBlock().getLocation());
		if (plot == null) {
			e.getPlayer().sendMessage(Message.PLOT_CANT_BUILD.getValue("nul"));
			e.setCancelled(true);
			return;	
		}
		
		if (plot.getMembers().getPlayerRank(e.getPlayer()) == PlotRank.VISITOR) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.PLOT_CANT_BUILD.getValue(plot));
		}
	}
	
	@EventHandler //cancel splash potion sin interdites dans le plot
	public void onPotionThrows(PotionSplashEvent e) {
		if (e.getAffectedEntities().size() == 0)
			return;
		
		plot = plugin.getPlotsManager().getPlot(((Entity) e.getAffectedEntities().toArray()[0]).getLocation());
		
		if (e.isCancelled() || plot == null)
			return;
		
		if(!(boolean)plot.getParameters().getParameter(PlotParamType.ALLOW_SPLASH_POTIONS) || plot.hasStoplag())
			e.setCancelled(true);
	}
	
	
	@SuppressWarnings("unchecked")
	@EventHandler //test interract block (cancel si pas la permission d'interagir avec le bloc) & test placement liquide
	public void onInterractEvent(PlayerInteractEvent e) {
		
		OlympaPlayerCreatif p = ((OlympaPlayerCreatif)AccountProvider.get(e.getPlayer().getUniqueId()));
		
		if (e.getClickedBlock() == null)
			return;
		
		plot = plugin.getPlotsManager().getPlot(e.getClickedBlock().getLocation());
		
		if (plot == null) {
			if (!p.hasStaffPerm(StaffPerm.BYPASS_WORLDEDIT)) {
				e.setCancelled(true);
				e.getPlayer().sendMessage(Message.PLOT_CANT_INTERRACT_NULL_PLOT.getValue());
			}
			return;
		}
		
		PlotRank playerRank = plot.getMembers().getPlayerRank(e.getPlayer());
		
		//test si permission d'interagir avec le bloc donné
		if (playerRank == PlotRank.VISITOR &&
				PlotParamType.getAllPossibleIntaractibleBlocks().contains(e.getClickedBlock().getType()) &&
				!((HashSet<Material>) plot.getParameters().getParameter(PlotParamType.LIST_ALLOWED_INTERRACTION)).contains(e.getClickedBlock().getType())
				) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.PLOT_CANT_INTERRACT.getValue());
			
			return;
		}

		//cancel interract si un item pouvant faire spawn une entité est utilisé
		if (e.getItem() != null && playerRank == PlotRank.VISITOR) {
			Material mat = e.getItem().getType();
			KitType kit = plugin.getPerksManager().getKitsManager().getKitOf(mat);
			
			if (interractProhibitedItems.contains(mat) || kit == KitType.HOSTILE_MOBS || kit == KitType.PEACEFUL_MOBS) {
				p.getPlayer().sendMessage(Message.PLOT_ITEM_PROHIBITED_USED.getValue());
				e.setCancelled(true);
				return;
			}
		}
		
		//GESTION COMMAND BLOCKS
		//si édition/placement du commandblock
		if (plot.getMembers().getPlayerLevel(p) >= 3 && e.getClickedBlock() != null && 
				plugin.getPerksManager().getKitsManager().hasPlayerPermissionFor(p, e.getClickedBlock().getType())) {
			
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				Block block = e.getClickedBlock();
				ItemStack item = e.getItem();
				
				//si le block cliqué est un commandblock, ouverture interface
				if (block != null && commandBlockTypes.contains(block.getType()) && !e.getPlayer().isSneaking()) {
					
					BlockPosition pos = new BlockPosition(e.getClickedBlock().getLocation().getBlockX(), e.getClickedBlock().getLocation().getBlockY(), e.getClickedBlock().getLocation().getBlockZ());
					
					NBTTagCompound tag = new NBTTagCompound();
					plugin.getWorldManager().getNmsWorld().getTileEntity(pos).save(tag);
					
					PacketPlayOutTileEntityData packet = new PacketPlayOutTileEntityData(pos, 2, tag);
					
			        EntityPlayer nmsPlayer = ((CraftPlayer) e.getPlayer()).getHandle();
			        nmsPlayer.playerConnection.sendPacket(packet);
			        
				//si l'item en main est un commandblock, placement de ce dernier
				}else if (item != null && commandBlockTypes.contains(item.getType())){
					
					//return si le Y est trop bas ou trop haut
					if (e.getClickedBlock().getLocation().getBlockY() < 2 || e.getClickedBlock().getLocation().getBlockY() > 254)
						return;
					
					Location loc = null;
					
					switch(e.getBlockFace()) {
					case DOWN:
						loc = e.getClickedBlock().getLocation().add(0, -1, 0);
						break;
					case EAST:
						loc = e.getClickedBlock().getLocation().add(1, 0, 0);
						break;
					case NORTH:
						loc = e.getClickedBlock().getLocation().add(0, 0, -1);
						break;
					case SOUTH:
						loc = e.getClickedBlock().getLocation().add(0, 0, 1);
						break;
					case UP:
						loc = e.getClickedBlock().getLocation().add(0, 1, 0);
						break;
					case WEST:
						loc = e.getClickedBlock().getLocation().add(-1, 0, 0);
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
				if (commandBlockTypes.contains(e.getClickedBlock().getType()))
					e.getClickedBlock().setType(Material.AIR);
			}
		}
	}
	
	@EventHandler //cancel interraction avec un itemframe
	public void onInterractEntityEvent(PlayerInteractEntityEvent e) {
		plot = plugin.getPlotsManager().getPlot(e.getRightClicked().getLocation());
		if (plot == null)
			return;

		if (plot.getMembers().getPlayerRank(e.getPlayer()) == PlotRank.VISITOR) {
			e.setCancelled(true);
			return;
		}
		
		//remove entity si clic droit dessus avec une houe
		if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.WOODEN_HOE && !(e.getRightClicked() instanceof Player) &&
				plot.getMembers().getPlayerRank(e.getPlayer()) != PlotRank.VISITOR) 
			plot.removeEntityInPlot(e.getRightClicked(), true);
	}
	
	@EventHandler//cancel spawn entité si paramètre du plot l'interdit
	public void onProjectileSpawn(ProjectileLaunchEvent e) {
		plot = plugin.getPlotsManager().getPlot(e.getLocation());

		if (plot == null || !(boolean)plot.getParameters().getParameter(PlotParamType.ALLOW_LAUNCH_PROJECTILES)) {
			e.setCancelled(true);
			return;
		}			
	}
	
	@EventHandler //test print TNT
	public void onPrintTnt(BlockIgniteEvent e) {
		plot = plugin.getPlotsManager().getPlot(e.getBlock().getLocation());
		
		if (plot == null)
			return;
		
		if (plot.hasStoplag()) {
			e.setCancelled(true);
			return;
		}
		
		if (e.getPlayer() == null || (e.getCause() != IgniteCause.ARROW && e.getCause() != IgniteCause.FLINT_AND_STEEL)) {
			e.setCancelled(true);
			return;
		}
		if (e.getBlock().getType() != Material.TNT && plot.getMembers().getPlayerRank(e.getPlayer()) == PlotRank.VISITOR) {
			e.setCancelled(true);
			return;
		}
		if (!(boolean)plot.getParameters().getParameter(PlotParamType.ALLOW_PRINT_TNT) && plot.getMembers().getPlayerRank(e.getPlayer()) == PlotRank.VISITOR) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.PLOT_CANT_PRINT_TNT.getValue());
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST) //modifie la destination téléport si joueur banni du plot
	public void onTeleportEvent(PlayerTeleportEvent e) {
		
		Plot plotFrom = plugin.getPlotsManager().getPlot(e.getFrom());
		Plot plotTo = plugin.getPlotsManager().getPlot(e.getTo());
		
		((OlympaPlayerCreatif)AccountProvider.get(e.getPlayer().getUniqueId())).setCurrentPlot(plotTo);
		
		if (plotFrom != null && plotFrom.equals(plotTo))
			return;
		
		if (plotFrom != null) 
			executeExitActions(plugin, e.getPlayer(), plotFrom);

		//OlympaPlayerCreatif p = ((OlympaPlayerCreatif)AccountProvider.get(e.getPlayer().getUniqueId()));

		if (plotTo != null) 
			if (!executeEntryActions(plugin, e.getPlayer(), plotTo))
				e.setTo(plotTo.getOutLoc());
	}
	
	@EventHandler(priority = EventPriority.LOWEST) //actions à effectuer lors de la sortie/entrée d'un joueur
	public void onPlayerMove(PlayerMoveEvent e) {
		if (e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockZ() == e.getTo().getBlockZ())
			return;
		
		Plot plotTo = plugin.getPlotsManager().getPlot(e.getTo());
		Plot plotFrom = plugin.getPlotsManager().getPlot(e.getFrom());
		
		//sortie de l'évent si pas de changement de plot
		if (plotTo == plotFrom)
			return;

		//OlympaPlayerCreatif p = ((OlympaPlayerCreatif)AccountProvider.get(e.getPlayer().getUniqueId()));
		
		//expulse les joueurs bannis & actions d'entrée de plot
		if (plotTo != null) 
			if (!executeEntryActions(plugin, e.getPlayer(), plotTo)) 
				plotTo.teleportOut(e.getPlayer());
			
		//actions de sortie de plot
		if (plotFrom != null) 
			executeExitActions(plugin, e.getPlayer(), plotFrom);
	}

	@EventHandler //rendu inventaire en cas de déconnexion & tp au spawn
	public void onQuitEvent(PlayerQuitEvent e) {
		//itemsToKeepOnDeath.remove(e.getPlayer().getUniqueId());
		inventoryStorage.remove(e.getPlayer());
		
		plot = plugin.getPlotsManager().getPlot(e.getPlayer().getLocation());
		if (plot == null)
			return;

		executeExitActions(plugin, e.getPlayer(), plot);
		e.getPlayer().teleport(plugin.getWorldManager().getWorld().getSpawnLocation());
	}


	
	
	//actions à exécuter en entrée du plot. return false si le joueur en est banni, true sinon
	@SuppressWarnings("unchecked")
	public static boolean executeEntryActions(OlympaCreatifMain plugin, Player p, Plot plotTo) {
		
		OlympaPlayerCreatif pc = AccountProvider.get(p.getUniqueId());
		pc.setCurrentPlot(plotTo);
		
		//si le joueur est banni, téléportation en dehors du plot
		if (((HashSet<Long>) plotTo.getParameters().getParameter(PlotParamType.BANNED_PLAYERS)).contains(pc.getId())) {
			
			if (!pc.hasStaffPerm(StaffPerm.BYPASS_KICK_AND_BAN)) {
				p.sendMessage(Message.PLOT_CANT_ENTER_BANNED.getValue(plotTo.getMembers().getOwner().getName()));
				return false;	
			}
		}
		
		//ajoute le joueur aux joueurs du plot s'il n'a pas la perm de bypass les commandes vanilla
		if (!pc.hasStaffPerm(StaffPerm.BYPASS_VANILLA_COMMANDS))
			plotTo.addPlayerInPlot(p);
		
		//exécution instruction commandblock d'entrée
		plugin.getCommandBlocksManager().executeJoinActions(plotTo, p);
		
		//clear les visiteurs en entrée & stockage de leur inventaire
		if ((boolean)plotTo.getParameters().getParameter(PlotParamType.CLEAR_INCOMING_PLAYERS) && plotTo.getMembers().getPlayerRank(pc) == PlotRank.VISITOR) {
			List<ItemStack> list = new ArrayList<ItemStack>();
			for (ItemStack it : p.getInventory().getContents()) {
				if (it != null && it.getType() != Material.AIR)
				list.add(it);
			}
			
			inventoryStorage.put(p, list);
			p.getInventory().clear();
			
			for (PotionEffect effect : p.getActivePotionEffects())
				p.removePotionEffect(effect.getType());
		}
		
		//tp au spawn de la zone
		if ((boolean)plotTo.getParameters().getParameter(PlotParamType.FORCE_SPAWN_LOC)) {
			p.teleport(plotTo.getParameters().getSpawnLoc(plugin));
			p.sendMessage(Message.TELEPORTED_TO_PLOT_SPAWN.getValue());
		}
		
		if (plotTo.getMembers().getPlayerRank(pc) == PlotRank.VISITOR) {
			//définition du gamemode
			p.setGameMode((GameMode) plotTo.getParameters().getParameter(PlotParamType.GAMEMODE_INCOMING_PLAYERS));
			
			//définition du flymode
			p.setAllowFlight((boolean) plotTo.getParameters().getParameter(PlotParamType.ALLOW_FLY_INCOMING_PLAYERS));

			//set max fly speed
			p.setFlySpeed(0.1f);
		}
		
		//définition de l'heure du joueur
		p.setPlayerTime((int) plotTo.getParameters().getParameter(PlotParamType.PLOT_TIME), false);
		
		//définition de la météo
		p.setPlayerWeather((WeatherType) plotTo.getParameters().getParameter(PlotParamType.PLOT_WEATHER));
		
		return true;
	}

	public static void executeExitActions(OlympaCreatifMain plugin, Player p, Plot plot) {

		((OlympaPlayerCreatif)AccountProvider.get(p.getUniqueId())).setCurrentPlot(null);
		
		plot.removePlayerInPlot(p);
		plugin.getCommandBlocksManager().excecuteQuitActions(plot, p);

		//rendu inventaire si stocké
		if (inventoryStorage.containsKey(p)) {
			p.getInventory().clear();
			for (ItemStack it : inventoryStorage.get(p))
				p.getInventory().addItem(it);
			inventoryStorage.remove(p);
		}
		
		p.setGameMode(GameMode.CREATIVE);
		p.setAllowFlight(true);
		p.resetPlayerTime();
		p.resetPlayerWeather();

		LocalSession weSession = plugin.getWorldEditManager().getSession(p);
		
		if (weSession != null) {
			//clear clipboard si le joueur n'en est pas le proprio
			if (plot.getMembers().getPlayerRank(p) != PlotRank.OWNER)
				weSession.setClipboard(null);
			
			World world = weSession.getSelectionWorld();
			
			//reset positions worldedit
			if (world != null && weSession.getRegionSelector(world) != null)
				weSession.getRegionSelector(world).clear();	
		}
	}
	
	@EventHandler //cancel remove paintings et itemsframes
	public void onItemFrameDestroy(HangingBreakByEntityEvent e) {
		if (e.getRemover().getType() != EntityType.PLAYER) {
			e.setCancelled(true);
			return;
		}
		
		plot = plugin.getPlotsManager().getPlot(e.getEntity().getLocation());
		if (plot == null)
			return;
		
		if (plot.getMembers().getPlayerRank((Player) e.getRemover()) == PlotRank.VISITOR) {
			e.setCancelled(true);	
			return;
		}
		
		plot.removeEntityInPlot(e.getEntity(), true);
		
		//remove entity si joueur a une houe en bois dans la main
		/*if (((Player)e.getRemover()).getInventory().getItemInMainHand() != null && 
				((Player)e.getRemover()).getInventory().getItemInMainHand().getType() == Material.WOODEN_HOE && 
				plot.getMembers().getPlayerRank((Player) e.getRemover()) != PlotRank.VISITOR) {

			plot.removeEntityInPlot(e.getEntity());
		}*/
	}
	
	@EventHandler
	public void onHangingPlace(HangingPlaceEvent e) {
		plot = plugin.getPlotsManager().getPlot(e.getEntity().getLocation());
		if (plot == null || plot.getMembers().getPlayerRank(e.getPlayer()) == PlotRank.VISITOR) 
			e.setCancelled(true);
	}
	
	@EventHandler //gestion autorisation pvp & fake player death
	public void onDamageByEntity(EntityDamageByEntityEvent e) {
		plot = plugin.getPlotsManager().getPlot(e.getEntity().getLocation());
		
		if (plot == null) {
			e.setCancelled(true);
			return;	
		}
		
		if (!(boolean) plot.getParameters().getParameter(PlotParamType.ALLOW_PVP) && e.getEntityType() == EntityType.PLAYER && e.getDamager().getType() == EntityType.PLAYER) {
			e.setCancelled(true);
			return;
		}
		
		if (!(boolean) plot.getParameters().getParameter(PlotParamType.ALLOW_PVE) && (e.getEntityType() != EntityType.PLAYER || e.getDamager().getType() != EntityType.PLAYER)) {
			e.setCancelled(true);
			return;
		}
		
		if (e.getEntity().getType() == EntityType.PLAYER)
			if (fireFakeDeath((Player) e.getEntity(), e.getDamager(), plot, e.getDamage()))
				e.setCancelled(true);
		
		/*
		NBTTagCompound tag = new NBTTagCompound();
		((CraftEntity)e.getEntity()).getHandle().c(tag);
		
		if (tag.hasKey("EntityTag"))
			if (tag.getCompound("EntityTag").hasKey("Invulnerable"))
				e.setCancelled(true);
				*/
	}
	
	
	@EventHandler //gestion autorisation pvp
	public void onDamageByBlock(EntityDamageByBlockEvent e) {
		if (e.getEntityType() != EntityType.PLAYER)
			return;
		
		plot = plugin.getPlotsManager().getPlot(e.getEntity().getLocation());
		
		if (plot == null) {
			e.setCancelled(true);
			return;	
		}
		
		if (!(boolean) plot.getParameters().getParameter(PlotParamType.ALLOW_ENVIRONMENT_DAMAGE)) {
			e.setCancelled(true);
			return;
		}
		
		if (e.getEntity().getType() == EntityType.PLAYER)
			if (fireFakeDeath((Player) e.getEntity(), null, plot, e.getDamage()))
				e.setCancelled(true);
	}
	
	//gestion fake death (le joueur ne doit jamais vraiment mourir sinon le fake op ne fonctionne plus)
	public static boolean fireFakeDeath(Player p, Entity killer, Plot plot, double damages) {
		
		if (p.getHealth() > damages)
			return false;
		
		FakePlayerDeathEvent event = new FakePlayerDeathEvent(p, killer, plot);
		Bukkit.getPluginManager().callEvent(event);
		
		p.teleport(event.getRespawnLoc());
		
		event.getDrops().forEach(item -> event.getDeathLoc().getWorld().dropItemNaturally(event.getDeathLoc(), item));
		
		return true;
	}
	
	@EventHandler //empêche le drop d'items si interdit sur le plot (et cancel si route)
	public void onDropItem(PlayerDropItemEvent e) {
		plot = plugin.getPlotsManager().getPlot(e.getPlayer().getLocation());
		
		if (plot == null) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.PLOT_DENY_ITEM_DROP.getValue());
			return;	
		}
		
		if (plot.getMembers().getPlayerRank(e.getPlayer()) == PlotRank.VISITOR && !((boolean) plot.getParameters().getParameter(PlotParamType.ALLOW_DROP_ITEMS))) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.PLOT_DENY_ITEM_DROP.getValue());
		}		
	}
	
	@EventHandler //empêche la nourriture de descendre si paramètre du plot défini comme tel
	public void onFoodChange(FoodLevelChangeEvent e) {
		if (e.getEntityType() != EntityType.PLAYER)
			return;
		
		plot = plugin.getPlotsManager().getPlot(e.getEntity().getLocation());
		if (plot == null)
			return;
		
		if ((boolean) plot.getParameters().getParameter(PlotParamType.KEEP_MAX_FOOD_LEVEL))
			e.setCancelled(true);
	}
	
	@EventHandler//cancel redstone si stoplag, sinon enregistre l'évent dans le stoplag checker
	public void onRedstoneChange(BlockRedstoneEvent e) {
		plot = plugin.getPlotsManager().getPlot(e.getBlock().getLocation());
		
		if (plot == null)
			e.setNewCurrent(0);
		else
			if (plot.hasStoplag())
				e.setNewCurrent(0);
			else if (e.getBlock().getType() == Material.REDSTONE_LAMP)
				plot.getStoplagChecker().addEvent(StopLagDetect.LAMP);
			else if (e.getBlock().getType() == Material.REDSTONE_WIRE)
				plot.getStoplagChecker().addEvent(StopLagDetect.WIRE);
	}
	
	@EventHandler(priority = EventPriority.HIGH) //si spawn d'entité, ajout à la liste des entités du plot
	public void onEntitySpawn(EntitySpawnEvent e) {
		if (e.isCancelled())
			return;
		
		Plot plot = plugin.getPlotsManager().getPlot(e.getLocation());
		if (plot == null) {
			e.setCancelled(true);
			return;
		}
		if (!plot.hasStoplag()) {
			plot.addEntityInPlot(e.getEntity());
			plot.getStoplagChecker().addEvent(StopLagDetect.ENTITY);
			
			
			NBTTagCompound tag = new NBTTagCompound();
			net.minecraft.server.v1_15_R1.Entity ent = ((CraftEntity)e.getEntity()).getHandle();
			ent.c(tag);
			
			NBTTagList list = new NBTTagList();			
			list.add(NBTTagString.a(plot.getPlotId().toString()));
			tag.set("Tags", list);
			
			ent.f(tag);

		}else
			e.setCancelled(true);
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
		
		for (Block block : e.getBlocks())
			if (!plot.getPlotId().equals(PlotId.fromLoc(plugin, block.getLocation())))
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
		
		//cancel évent si blocks poussés sur la route
		for (Block block : e.getBlocks()) {
			if (!plot.getPlotId().equals(PlotId.fromLoc(plugin, block.getLocation()))) {
				e.setCancelled(true);
				return;
			}
			else if (plot.getPlotId().isOnInteriorDiameter(block.getLocation())) {
				e.setCancelled(true);
				return;
			}
		}
		
		plot.getStoplagChecker().addEvent(StopLagDetect.PISTON);
	}
	
	@EventHandler //cancel lava/water flow en dehors du plot. Cancel aussi toute téléportation d'un oeuf de dragon
	public void onLiquidFlow(BlockFromToEvent e) {
		Plot plot = plugin.getPlotsManager().getPlot(e.getToBlock().getLocation());
		
		if (plot == null || plot.hasStoplag() || e.getBlock().getType() == Material.DRAGON_EGG || !plot.hasLiquidFlow()) {
			e.setCancelled(true);
			return;
		}
		
		plot.getStoplagChecker().addEvent(StopLagDetect.LIQUID);
	}
	
	@EventHandler //cancel pousse d'arbres, etc en dehors d'un plot
	public void onGrow(StructureGrowEvent e) {
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
	public void onGrow(BlockGrowEvent e) {
		Plot plot = plugin.getPlotsManager().getPlot(e.getBlock().getLocation());

		if (plot == null)
			e.setCancelled(true);
	}
	
	@EventHandler //ajoute les entités du chunk au plot correspondant s'il existe, sinon les supprimer
	public void onChunkLoad(ChunkLoadEvent e) {
		if (e.isNewChunk())
			return;
		
		new ArrayList<Entity>(Arrays.asList(e.getChunk().getEntities())).forEach(ent -> {
			if (ent.getType() == EntityType.PLAYER)
				return;
			
			Plot plot = plugin.getPlotsManager().getPlot(plugin.getPlotsManager().getPlotIdFromTagOf(ent));
			if (plot != null && plot.getPlotId().equals(plugin.getPlotsManager().getPlot(ent.getLocation()).getPlotId()))
				plot.addEntityInPlot(ent);
		});
	}
	
	
	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent e) {
		new ArrayList<Entity>(Arrays.asList(e.getChunk().getEntities())).forEach(ent -> {
			if (ent.getType() == EntityType.PLAYER)
				return;
			
			Plot plot = plugin.getPlotsManager().getPlot(plugin.getPlotsManager().getPlotIdFromTagOf(ent));
			if (plot != null)
				plot.removeEntityInPlot(ent, false);
		});
	}
	/*
	@EventHandler //remove entité de la liste des entités du plot si l'entité a despawn
	public void onEntityRemove(EntityRemoveEvent e) {
		if (e.getPlot() != null)
			e.getPlot().removeEntityInPlot(e.getEntity(), true);
	}
	*/
}





