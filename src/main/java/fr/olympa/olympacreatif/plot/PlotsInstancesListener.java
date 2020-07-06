package fr.olympa.olympacreatif.plot;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_15_R1.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.Blocks;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityStatus;
import net.minecraft.server.v1_15_R1.PacketPlayOutTileEntityData;
import net.minecraft.server.v1_15_R1.TileEntity;
import net.minecraft.server.v1_15_R1.TileEntityCommand;

public class PlotsInstancesListener implements Listener{

	private OlympaCreatifMain plugin;
	private static Map<Plot, Map<Player, List<ItemStack>>> inventoryStorage = new HashMap<Plot, Map<Player, List<ItemStack>>>();
	private Plot plot;
	
	private List<Material> prohibitedVisitorInteractItems = new ArrayList<Material>();
	private List<Material> commandBlockTypes = new ArrayList<Material>(Arrays.asList(new Material[] {Material.COMMAND_BLOCK, Material.CHAIN_COMMAND_BLOCK, Material.REPEATING_COMMAND_BLOCK}));
	
	public PlotsInstancesListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		initializeProhibitemVisitorItems();
		
		//gère le remplacement des commandblock dans la main du joueur (après le remplacement initial pour permettre le placement du cb)
		new BukkitRunnable() {
			
			@Override
			public void run() {
				
			}
		}.runTaskTimer(plugin, 10, 1);
	}


	private void initializeProhibitemVisitorItems() {

		prohibitedVisitorInteractItems.add(Material.ARMOR_STAND);
		prohibitedVisitorInteractItems.add(Material.BONE_MEAL);
		prohibitedVisitorInteractItems.add(Material.PAINTING);
		prohibitedVisitorInteractItems.add(Material.ITEM_FRAME);
		prohibitedVisitorInteractItems.add(Material.DEBUG_STICK);
		
		for (Material mat : Material.values())
			if (mat.toString().contains("EGG") || mat.toString().contains("MINECART") || mat.toString().contains("BOAT"))
				prohibitedVisitorInteractItems.add(mat);
	}


	@EventHandler //test place block (autorisé uniquement pour les membres et pour la zone protégeé)
	public void onPlaceBlockEvent(BlockPlaceEvent e) {
		
		if(((OlympaPlayerCreatif)AccountProvider.get(e.getPlayer().getUniqueId())).hasStaffPerm(StaffPerm.BYPASS_WORLDEDIT))
			return;
		
		plot = plugin.getPlotsManager().getPlot(e.getBlockPlaced().getLocation());
		if (plot == null) {
			e.getPlayer().sendMessage(Message.PLOT_CANT_BUILD.getValue());
			e.setCancelled(true);
			return;	
		}
		
		if (plot.getMembers().getPlayerRank(e.getPlayer()) == PlotRank.VISITOR && !plot.getProtectedZoneData().keySet().contains(e.getBlock().getLocation())) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.PLOT_CANT_BUILD.getValue());
			return;
		}
		
		//détection placement spawner
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
	}
	
	@EventHandler //test break block (autorisé uniquement pour les membres et pour la zone protégeé)
	public void onBreakBlockEvent(BlockBreakEvent e) {
		
		if(((OlympaPlayerCreatif)AccountProvider.get(e.getPlayer().getUniqueId())).hasStaffPerm(StaffPerm.BYPASS_WORLDEDIT))
			return;
		
		plot = plugin.getPlotsManager().getPlot(e.getBlock().getLocation());
		if (plot == null) {
			e.getPlayer().sendMessage(Message.PLOT_CANT_BUILD.getValue());
			e.setCancelled(true);
			return;	
		}
		
		if (plot.getMembers().getPlayerRank(e.getPlayer()) == PlotRank.VISITOR && !plot.getProtectedZoneData().keySet().contains(e.getBlock().getLocation())) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.PLOT_CANT_BUILD.getValue());
		}
	}
	
	@EventHandler //cancel splash potion sin interdites dans le plot
	public void onPotionThrows(PotionSplashEvent e) {
		Location loc = null;
		if (e.getHitBlock() == null)
			loc = e.getHitEntity().getLocation();
		else
			loc = e.getHitBlock().getLocation();

		plot = plugin.getPlotsManager().getPlot(loc);
		
		if (e.isCancelled() || plot == null)
			return;
		
		if(!(boolean)plot.getParameters().getParameter(PlotParamType.ALLOW_SPLASH_POTIONS))
			e.setCancelled(true);
	}
	
	
	@EventHandler //test interract block (cancel si pas la permission d'interagir avec le bloc) & test placement liquide
	public void onInterractEvent(PlayerInteractEvent e) {
		if (e.getClickedBlock() == null)
			return;
		
		plot = plugin.getPlotsManager().getPlot(e.getClickedBlock().getLocation());

		if (plot == null) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.PLOT_CANT_INTERRACT.getValue());
			return;
		}
		
		PlotRank playerRank = plot.getMembers().getPlayerRank(e.getPlayer());
		
		//test si permission d'interagir avec le bloc donné
		if (PlotParamType.getAllPossibleBlocksWithInteractions().contains(e.getClickedBlock().getType()))		
			if (playerRank == PlotRank.VISITOR && 
					!((ArrayList<Material>) plot.getParameters().getParameter(PlotParamType.LIST_ALLOWED_INTERRACTION)).contains(e.getClickedBlock().getType()) &&
					!plot.getProtectedZoneData().keySet().contains(e.getClickedBlock().getLocation())) {
				e.setCancelled(true);
				e.getPlayer().sendMessage(Message.PLOT_CANT_INTERRACT.getValue());
				
				return;
			}
		
		//cancel si usage d'autre chose qu'un oeuf, un arc, une arbalète ou une boule de neige
		if (e.getItem() != null)
			if (playerRank == PlotRank.VISITOR)
				if (prohibitedVisitorInteractItems.contains(e.getItem().getType())) {
					e.setCancelled(true);	
					e.getPlayer().sendMessage(Message.PLOT_CANT_INTERRACT.getValue());
				}

		//gère l'ouverture & le placement des commandblocks
		if (playerRank == PlotRank.OWNER && commandBlockTypes.contains(e.getClickedBlock().getType())) {
			
			if (e.getPlayer().isSneaking()) {
				
				BlockPosition pos = new BlockPosition(e.getClickedBlock().getLocation().getBlockX(), e.getClickedBlock().getLocation().getBlockY(), e.getClickedBlock().getLocation().getBlockZ());
				
				NBTTagCompound tag = new NBTTagCompound();
				plugin.getWorldManager().getNmsWorld().getTileEntity(pos).save(tag);
				
				PacketPlayOutTileEntityData packet = new PacketPlayOutTileEntityData(pos, 2, tag);
				
		        EntityPlayer nmsPlayer = ((CraftPlayer) e.getPlayer()).getHandle();
		        nmsPlayer.playerConnection.sendPacket(packet);
		        
			}else {
				e.getItem().setType(Material.DISPENSER);
			}
		}
		
		//change le type de block à un dispenser (qui sera placé à la place du commandblock car les joueurs non op 
		//(fake op ne fonctionne pas) ne peuvent pas poser de commandblock. Choix dispenser pour conserver le blockface
		
	      //  Bukkit.broadcastMessage(tag.asString());
	        
	        /*
			try {
		        
				//BlockPosition pos = new BlockPosition(e.getClickedBlock().getLocation().getBlockX(), e.getClickedBlock().getLocation().getBlockY(), e.getClickedBlock().getLocation().getBlockZ());
		        BlockState blockState = e.getClickedBlock().getState();//plugin.getWorldManager().getNmsWorld().getTileEntity(pos).getBlock().getBlock();
		        
		        Method getTile = CraftBlockEntityState.class.getDeclaredMethod("getTileEntity");
		        getTile.setAccessible(true);
		        TileEntityCommand nmsBlock = (TileEntityCommand) getTile.invoke(blockState);

		        ((CraftPlayer) e.getPlayer()).getHandle().playerConnection.sendPacket(nmsBlock.getUpdatePacket());
		    }catch (ReflectiveOperationException err) {
		        err.printStackTrace();
		    }	
			*/
		    
	}
	
	
	@EventHandler //cancel interraction avec un itemframe
	public void onInterractEntityEvent(PlayerInteractEntityEvent e) {
		plot = plugin.getPlotsManager().getPlot(e.getRightClicked().getLocation());
		if (plot == null)
			return;

		if (plot.getMembers().getPlayerRank(e.getPlayer()) == PlotRank.VISITOR && e.getRightClicked().getType() == EntityType.ITEM_FRAME) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(Message.PLOT_CANT_INTERRACT.getValue());
		}
		
		if ((e.getPlayer().getInventory().getItemInMainHand().getType() == Material.WOODEN_HOE || 
				e.getPlayer().getInventory().getItemInOffHand().getType() == Material.WOODEN_HOE) &&
				plot.getMembers().getPlayerRank(e.getPlayer()) != PlotRank.VISITOR && !(e.getRightClicked() instanceof Player))
			e.getRightClicked().remove();
			
	}
	
	@EventHandler //test print TNT
	public void onPrintTnt(BlockIgniteEvent e) {
		plot = plugin.getPlotsManager().getPlot(e.getBlock().getLocation());
		
		if (plot == null)
			return;
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
	
	@SuppressWarnings("unchecked")
	@EventHandler //édite destination téléport si joueur banni du plot
	public void onTeleportEvent(PlayerTeleportEvent e) {
		Player p = e.getPlayer();
		Plot plot = plugin.getPlotsManager().getPlot(p.getLocation());
		
		if (plot == null)
			return;
		
		if (((List<Long>) plot.getParameters().getParameter(PlotParamType.BANNED_PLAYERS)).contains(AccountProvider.get(p.getUniqueId()).getId())) {
			if (((OlympaPlayerCreatif) AccountProvider.get(p.getUniqueId())).hasStaffPerm(StaffPerm.BYPASS_KICK_AND_BAN)) {
				e.setTo(plot.getOutLoc());
				p.sendMessage(Message.PLOT_CANT_ENTER_BANNED.getValue());	
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@EventHandler //actions à effectuer lors de la sortie/entrée d'un joueur
	public void onPlayerMove(PlayerMoveEvent e) {
		if (e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockZ() == e.getTo().getBlockZ())
			return;
		
		Plot plotTo = plugin.getPlotsManager().getPlot(e.getTo());
		Plot plotFrom = plugin.getPlotsManager().getPlot(e.getFrom());
		
		//sortie de l'évent si pas de changement de plot
		if (plotTo == plotFrom)
			return;

		//expulse les joueurs bannis
		if (plotTo != null) {
			if (((List<Long>) plotTo.getParameters().getParameter(PlotParamType.BANNED_PLAYERS)).contains(AccountProvider.get(e.getPlayer().getUniqueId()).getId())) {
				e.setCancelled(true);
				//e.getPlayer().setVelocity(e.getPlayer().getVelocity().multiply(-1));
				//e.getPlayer().teleport(new Location(e.getPlayer().getWorld(), 5 * (e.getTo().getX() - e.getFrom().getX()), 0, 5 * (e.getTo().getZ() - e.getFrom().getZ())));
				e.getPlayer().sendMessage(Message.PLOT_CANT_ENTER_BANNED.getValue());
				return;
			}
			
			executeEntryActions(plugin, e.getPlayer(), plotTo);	
		}
		
		//actions de sortie de plot
		if (plotFrom != null) 
			executeQuitActions(plugin, e.getPlayer(), plotFrom);
	}

	@EventHandler //rendu inventaire en cas de déconnexion & tp au spawn
	public void onQuitEvent(PlayerQuitEvent e) {
		plot = plugin.getPlotsManager().getPlot(e.getPlayer().getLocation());
		if (plot == null)
			return;

		executeQuitActions(plugin, e.getPlayer(), plot);
		e.getPlayer().teleport(plugin.getWorldManager().getWorld().getSpawnLocation());
	}


	
	
	//actions à exécuter en entrée du plot 
	public static void executeEntryActions(OlympaCreatifMain plugin, Player p, Plot plotTo) {
		
		OlympaPlayerCreatif pc = AccountProvider.get(p.getUniqueId());
		
		//si le joueur est banni, téléportation en dehors du plot
		if (((List<Long>) plotTo.getParameters().getParameter(PlotParamType.BANNED_PLAYERS)).contains(pc.getId())) {
			
			if (!pc.hasStaffPerm(StaffPerm.BYPASS_KICK_AND_BAN)) {
				p.sendMessage(Message.PLOT_CANT_ENTER_BANNED.getValue());
				plotTo.teleportOut(p);
				return;	
			}
		}

		plotTo.addPlayerInPlot(p);
		
		//exécution instruction commandblock d'entrée
		plugin.getCommandBlocksManager().executeJoinActions(plotTo, p);
		
		//les actions suivantes ne sont effectuées que si le joueur n'appartient pas au plot
		if (plotTo.getMembers().getPlayerRank(p) != PlotRank.VISITOR)
			return;
		
		//clear les visiteurs en entrée & stockage de leur inventaire
		if ((boolean)plotTo.getParameters().getParameter(PlotParamType.CLEAR_INCOMING_PLAYERS)) {
			List<ItemStack> list = new ArrayList<ItemStack>();
			for (ItemStack it : p.getInventory().getContents()) {
				if (it != null && it.getType() != Material.AIR)
				list.add(it);
			}
			
			if (!inventoryStorage.containsKey(plotTo))
				inventoryStorage.put(plotTo, new HashMap<Player, List<ItemStack>>());
			
			inventoryStorage.get(plotTo).put(p, list);
			p.getInventory().clear();
			
			for (PotionEffect effect : p.getActivePotionEffects())
				p.removePotionEffect(effect.getType());
		}
		
		//tp au spawn de la zone
		if ((boolean)plotTo.getParameters().getParameter(PlotParamType.FORCE_SPAWN_LOC)) {
			p.teleport((Location) plotTo.getParameters().getParameter(PlotParamType.SPAWN_LOC));
			p.sendMessage(Message.TELEPORTED_TO_PLOT_SPAWN.getValue());
		}
		
		//définition de l'heure du joueur
		p.setPlayerTime((int) plotTo.getParameters().getParameter(PlotParamType.PLOT_TIME), false);
		
		//définition du gamemode
		p.setGameMode((GameMode) plotTo.getParameters().getParameter(PlotParamType.GAMEMODE_INCOMING_PLAYERS));
		
		//définition du flymode
		p.setAllowFlight((boolean) plotTo.getParameters().getParameter(PlotParamType.ALLOW_FLY_INCOMING_PLAYERS));
		
		//définition de la météo
		p.setPlayerWeather((WeatherType) plotTo.getParameters().getParameter(PlotParamType.PLOT_WEATHER));

		/*
		//fait croire au client qu'il est op (pour ouvrir l'interface des commandblocks)
		EntityPlayer nmsPlayer = ((CraftPlayer) p).getHandle();
		nmsPlayer.playerConnection.sendPacket(new PacketPlayOutEntityStatus(nmsPlayer, (byte) 28));		
		*/
	}

	public static void executeQuitActions(OlympaCreatifMain plugin, Player p, Plot plot) {

		plot.removePlayerInPlot(p);

		//rendu inventaire si stocké
		if (inventoryStorage.containsKey(plot) && inventoryStorage.get(plot).containsKey(p)) {
			p.getInventory().clear();
			for (ItemStack it : inventoryStorage.get(plot).get(p))
				p.getInventory().addItem(it);
			inventoryStorage.get(plot).remove(p);
		}
		
		p.setGameMode(GameMode.CREATIVE);
		p.setAllowFlight(true);
		p.resetPlayerTime();
		p.resetPlayerWeather();
		
		plugin.getCommandBlocksManager().excecuteQuitActions(plot, p);
		
		/*
		//fait croire au client qu'il est deop (pour ouvrir l'interface des commandblocks) sauf pour les staff
		if (!((OlympaPlayerCreatif)AccountProvider.get(p.getUniqueId())).hasStaffPerm(StaffPerm.FAKE_OWNER_EVERYWHERE)){
			EntityPlayer nmsPlayer = ((CraftPlayer) p).getHandle();
			nmsPlayer.playerConnection.sendPacket(new PacketPlayOutEntityStatus(nmsPlayer, (byte) 24));	
		}
		*/
	}
	
	@EventHandler //cancel remove paintings et itemsframes
	public void onItemFrameDestroy(HangingBreakByEntityEvent e) {
		if (e.getRemover().getType() != EntityType.PLAYER) {
			e.setCancelled(true);
			return;
		}
		
		if (((OlympaPlayerCreatif)AccountProvider.get(e.getRemover().getUniqueId())).hasStaffPerm(StaffPerm.BYPASS_WORLDEDIT))
			return;
		
		plot = plugin.getPlotsManager().getPlot(e.getEntity().getLocation());
		if (plot == null)
			return;
		
		/*
		if (e.getRemover().getType() != EntityType.PLAYER  && (e.getEntity().getType() == EntityType.PAINTING || e.getEntity().getType() == EntityType.ITEM_FRAME || e.getEntity().getType() == EntityType.ARMOR_STAND)) {
			e.setCancelled(true);
			return;
		}
		*/
		
		if (plot.getMembers().getPlayerRank((Player) e.getRemover()) == PlotRank.VISITOR)
			e.setCancelled(true);
		
	}
	
	@EventHandler //gestion autorisation pvp
	public void onDamageByEntity(EntityDamageByEntityEvent e) {
		plot = plugin.getPlotsManager().getPlot(e.getEntity().getLocation());
		if (plot == null)
			return;
		
		if (!(boolean) plot.getParameters().getParameter(PlotParamType.ALLOW_PVP) && e.getEntityType() == EntityType.PLAYER && e.getDamager().getType() == EntityType.PLAYER) {
			e.setCancelled(true);
			return;
		}
		
		if (!(boolean) plot.getParameters().getParameter(PlotParamType.ALLOW_PVE) && (e.getEntityType() != EntityType.PLAYER || e.getDamager().getType() != EntityType.PLAYER)) {
			e.setCancelled(true);
			return;
		}
		
		NBTTagCompound tag = new NBTTagCompound();
		((CraftEntity)e.getEntity()).getHandle().c(tag);
		
		if (tag.hasKey("EntityTag"))
			if (tag.getCompound("EntityTag").hasKey("Invulnerable"))
				e.setCancelled(true);
	}
	
	
	@EventHandler //gestion autorisation pvp
	public void onDamageByBlock(EntityDamageByBlockEvent e) {
		if (e.getEntityType() != EntityType.PLAYER)
			return;
		
		plot = plugin.getPlotsManager().getPlot(e.getEntity().getLocation());
		if (plot == null)
			return;
		
		if (!(boolean) plot.getParameters().getParameter(PlotParamType.ALLOW_ENVIRONMENT_DAMAGE)) {
			e.setCancelled(true);
			return;
		}
	}
	
	@EventHandler //force le respawn sur le spawn de la parcelle
	public void onRespawn(PlayerRespawnEvent e) {
		plot = plugin.getPlotsManager().getPlot(e.getPlayer().getLocation());
		if (plot == null)
			e.setRespawnLocation(plugin.getWorldManager().getWorld().getSpawnLocation());
		else
			e.setRespawnLocation((Location) plot.getParameters().getParameter(PlotParamType.SPAWN_LOC));
		
	}
	
	@EventHandler //gère le paramètre keepInventory de la parcelle
	public void onDeath(PlayerDeathEvent e) {
		plot = plugin.getPlotsManager().getPlot(e.getEntity().getLocation());
		
		if (plot == null)
			e.getDrops().clear();
		
		if ((boolean) plot.getParameters().getParameter(PlotParamType.KEEP_INVENTORY_ON_DEATH)) {
			e.getEntity().getInventory().addItem(e.getDrops().toArray(new ItemStack[e.getDrops().size()]));
			e.getDrops().clear();
		}
	}
	
	/*
	private void tpPlayerToPlotSpawnOnDeath(EntityDamageEvent e, Plot plot) {
		Player p =  (Player) e.getEntity();
		if (((Player)e.getEntity()).getHealth() - e.getDamage() <= 0) {
			e.getEntity().teleport((Location) plot.getParameters().getParameter(PlotParamType.SPAWN_LOC));
			e.setCancelled(true);
			p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
			p.setFoodLevel(20);
		}
	}	
	*/
	
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
}
