package fr.olympa.olympacreatif.worldedit;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.TileEntity;

public class WorldEditManager {

	private OlympaCreatifMain plugin;
	private Map<Player, WorldEditInstance> playersWorldEdit = new HashMap<Player, WorldEditInstance>();
	
	//liste d'entrées comprenant : un joueur et une liste d'entrées de blocks à placer
	private List<SimpleEntry<Player, List<SimpleEntry<Location, SimpleEntry<BlockData, TileEntity>>>>> blocksToBuild = new ArrayList<SimpleEntry<Player,List<SimpleEntry<Location,SimpleEntry<BlockData,TileEntity>>>>>();
	
	public WorldEditManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		
		plugin.getServer().getPluginManager().registerEvents(new WorldEditListener(plugin), plugin);
		
		//runnable de setblock délayé
		new BukkitRunnable() {
			
			Player p = null;
			List<SimpleEntry<Location, SimpleEntry<BlockData, TileEntity>>> toPlace = new ArrayList<AbstractMap.SimpleEntry<Location,SimpleEntry<BlockData,TileEntity>>>();

			//variables permettant de déterminer le nombre de blocks à placer par seconde
			long oldTime = System.currentTimeMillis()-1;
			double tps = 0;
			int bps = 0;
			public void run() {
				int i = 0;
				
				//MAJ de la liste des blocs à placer si la précédente est vide
				 if (blocksToBuild.size() > 0 && toPlace.size() == 0) {
					 p = blocksToBuild.get(0).getKey();
					 toPlace = blocksToBuild.get(0).getValue();
				 }
				 
				 //placement synchrone des blocks
				 tps = Math.min(1000.0/(double)(System.currentTimeMillis() - oldTime), 20);
				 oldTime = System.currentTimeMillis();
				 bps = (int) ((Integer.valueOf(Message.PARAM_WORLDEDIT_BPS.getValue()) / 20) * Math.max(tps-18.5, 0));
				 
				//place des blocs si tps>18.5 (proportion de blocs placés dépendant du tps)				 
				while (i < bps && toPlace.size() > 0) {
					plugin.getWorldManager().getWorld().loadChunk(toPlace.get(0).getKey().getChunk());
					
					//place le blockdata et le tileentity, si elle existe
					toPlace.get(0).getKey().getBlock().setBlockData(toPlace.get(0).getValue().getKey());
					
					if (toPlace.get(0).getValue().getValue() != null) {
						NBTTagCompound tag = new NBTTagCompound();
						
						toPlace.get(0).getValue().getValue().save(tag);

						tag.setInt("x", toPlace.get(0).getKey().getBlockX());
						tag.setInt("y", toPlace.get(0).getKey().getBlockY());
						tag.setInt("z", toPlace.get(0).getKey().getBlockZ());
						
						plugin.getWorldManager().getNmsWorld().getTileEntity(new BlockPosition(toPlace.get(0).getKey().getBlockX(), toPlace.get(0).getKey().getBlockY(), toPlace.get(0).getKey().getBlockZ())).load(tag);
						//plugin.getWorldManager().getNmsWorld().setTileEntity(new BlockPosition(toPlace.get(0).getKey().getBlockX(), 
							//	toPlace.get(0).getKey().getBlockY(), toPlace.get(0).getKey().getBlockZ()), toPlace.get(0).getValue().getValue());
					}
					
					//supprime le bloc une fois placé
					toPlace.remove(0);
					
					//MAJ liste des blocs en attente et envoi du message de fin au joueur
					if (toPlace.size() == 0) {
						 blocksToBuild.remove(0);
						 
						 if (blocksToBuild.size() > 0)
							 toPlace = blocksToBuild.get(0).getValue();
						 
						 if (p != null && p.isOnline())
							 p.sendMessage(Message.WE_ACTION_ENDED.getValue());
						 
						 //définition de la nouvelle liste de blocs à traiter
						 if (blocksToBuild.size() > 0) {
							 p = blocksToBuild.get(0).getKey();
							 toPlace = blocksToBuild.get(0).getValue();
						 }
					}
					
					i++;
				}
			}
		}.runTaskTimer(plugin, 0, 1);
		
	}
	
	public WorldEditInstance addPlayer(Player p) {
		WorldEditInstance ins = new WorldEditInstance(plugin, p);
		playersWorldEdit.put(p, ins);
		return ins;
	}
	
	public void removePlayer(Player p) {
		playersWorldEdit.remove(p);
	}
	
	public boolean hasPlayerPendingPastes(Player p) {
		for (SimpleEntry<Player, List<SimpleEntry<Location, SimpleEntry<BlockData, TileEntity>>>> e : blocksToBuild) {
			if (e.getKey().equals(p)) {
				p.sendMessage(Message.WE_ANOTHER_ACTION_ALREADY_QUEUED.getValue());
				return true;
			}
		}
		return false;
	}
	
	//renvoie vrai si le les blocs ont bien été ajoutés à la liste, false si le joueur avait déjà trop de travail en attente
	public WorldEditError addToBuildingList(Player p, List<SimpleEntry<Location, SimpleEntry<BlockData, TileEntity>>> toBuild) {
		int queued = 0;
		for (SimpleEntry<Player, List<SimpleEntry<Location, SimpleEntry<BlockData, TileEntity>>>> e : blocksToBuild)
			if (e.getKey().equals(p))
				queued++;
		
		if (queued >= Integer.valueOf(Message.PARAM_WE_MAX_QUEUED_ACTIONS_PER_PLAYER.getValue())) {
			return WorldEditError.ERR_TOO_MANY_ACTIONS_QUEUED;
		}
		
		if (toBuild.size() == 0)
			return WorldEditError.NO_ERROR;
		
		blocksToBuild.add(new SimpleEntry<Player, List<SimpleEntry<Location, SimpleEntry<BlockData, TileEntity>>>>(p, toBuild));	
		return WorldEditError.NO_ERROR;
	}
	
	public WorldEditInstance getPlayerInstance(Player p) {
		return playersWorldEdit.get(p);
	}
	
	public enum WorldEditError{
		ERR_NULL_PLOT(Message.PLOT_NULL_PLOT),
		ERR_INSUFFICIENT_PLOT_PERMISSION(Message.INSUFFICIENT_PLOT_PERMISSION),
		ERR_OPERATION_CROSS_PLOT(Message.WE_CMD_INVALID_SELECTION),
		ERR_OPERATION_TOO_BIG(Message.WE_ERR_SELECTION_TOO_BIG),
		ERR_UNDO_LIST_EMPTY(Message.WE_NO_UNDO_AVAILABLE),
		ERR_TOO_MANY_ACTIONS_QUEUED(Message.WE_TOO_MANY_ACTIONS),
		ERR_PASTE_NULL_BLOCK_LIST(Message.WE_ERR_NULL_CLIPBOARD),
		ERR_PASTE_PART_ON_NULL_TARGET(Message.WE_ERR_PASTE_PART_ON_NULL_TARGET),
		ERR_PASTE_NOT_OWNER_OF_2_PLOTS(Message.WE_ERR_NOT_OWNER_OF_2_PLOTS),
		ERR_NULL_PLAN(Message.WE_ERR_NULL_PLAN),
		ERR_COPY_INCORRECT_DEGREES(Message.WE_ERR_COPY_INCORRECT_DEGREES),
		ERR_SET_INVALID_BLOCKDATA(Message.WE_ERR_SET_INVALID_BLOCKDATA),
		ERR_PROTECTED_ZONE_ALREADY_SAVED(Message.WE_ERR_PROTECTED_ZONE_ALREADY_SAVED),
		ERR_PROTECTED_ZONE_NOT_DEFINED(Message.WE_ERR_PROTECTED_ZONE_NOT_DEFINED),
		ERR_PROTECTED_ZONE_EMPTY(Message.WE_ERR_PROTECTED_ZONE_EMPTY),
		ERR_POS_NOT_DEFINED(Message.WE_ERR_POS_NOT_DEFINED),
		NO_ERROR(Message.UNKNOWN_MESSAGE),    
		 
		;
		
		private Message errMessage;
		
		WorldEditError(Message m){
			errMessage = m; 
		}
		
		public Message getErrorMessage() {
			return errMessage;
		}
	}
	
	/* 
wizjanyAujourd’hui à 03:21
for general usage just use world.getChunkAtAsync() which gives you a Future
Flo | GestankbratwurstAujourd’hui à 03:21
Then you can just enable async chunk loading in your paper.yml and use something like CompletableFuture.runAsync(() -> "something something load chunk at pos")
wizjanyAujourd’hui à 03:21
and related methods
eg there's also entity#teleportasync which will async load the target location before teleporting
BullobilyAujourd’hui à 03:22
Okay, I though that the chunks coming from world.getChunkAtAsync() were read-only
@Flo | Gestankbratwurst ok, I will search about this
wizjanyAujourd’hui à 03:23
it's literally exactly the same as getChunkAt except it gives you a future that you can .thenApply some stuff in once it's loaded
and no absolutely do not do what flo said
you must get your future from the getChunkAtAsync methods etc exposed by the api
and you must call those methods from the main thread
using CompletableFuture.runAsync will at best trigger the async catcher and throw an exception
*/
}
