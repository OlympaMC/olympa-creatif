package fr.olympa.olympacreatif.worldedit;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;

public class WorldEditManager {

	private OlympaCreatifMain plugin;
	private Map<Player, WorldEditInstance> playersWorldEdit = new HashMap<Player, WorldEditInstance>();
	
	//liste d'entrées comprenant : un joueur et une liste d'entrées de blocks à placer
	private List<SimpleEntry<Player, List<SimpleEntry<Location, BlockData>>>> blocksToBuild = new ArrayList<AbstractMap.SimpleEntry<Player,List<SimpleEntry<Location,BlockData>>>>();
	
	public WorldEditManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;

		//runnable de setblock délayé
		new BukkitRunnable() {
			
			Player p = blocksToBuild.get(0).getKey();
			List<SimpleEntry<Location, BlockData>> toPlace = new ArrayList<SimpleEntry<Location,BlockData>>();
			public void run() {
				int i = 0;
				
				//MAJ de la liste des blocs à placer si la précédente est vide
				 if (blocksToBuild.size() > 0) {
					 p = blocksToBuild.get(0).getKey();
					 toPlace = blocksToBuild.get(0).getValue();
				 }
				 
				while (i < Integer.valueOf(Message.PARAM_WORLDEDIT_BPS.getValue())/5 && toPlace.size() > 0) {
					plugin.getWorldManager().getWorld().loadChunk(toPlace.get(0).getKey().getChunk());
					toPlace.get(0).getKey().getBlock().setBlockData(toPlace.get(0).getValue());
					toPlace.remove(0);
					
					//MAJ liste des blocs en attente et envoi du message de fin au joueur
					if (toPlace.size() == 0) {
						 blocksToBuild.remove(0);
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
		}.runTaskTimer(plugin, 20, 4);
		
	}
	
	public void addPlayer(Player p) {
		playersWorldEdit.put(p, new WorldEditInstance(plugin, p));
	}
	
	public void removePlayer(Player p) {
		playersWorldEdit.remove(p);
	}
	
	public boolean hasPlayerPendingPastes(Player p) {
		for (SimpleEntry<Player, List<SimpleEntry<Location, BlockData>>> e : blocksToBuild) {
			if (e.getKey().equals(p)) {
				p.sendMessage(Message.WE_ANOTHER_ACTION_ALREADY_QUEUED.getValue());
				return true;
			}
		}
		return false;
	}
	
	public void addToBuildingList(Player p, List<SimpleEntry<Location, BlockData>> blocks) {
		if (blocks.size() > 0) {
			blocksToBuild.add(new SimpleEntry<Player, List<SimpleEntry<Location,BlockData>>>(p, blocks));
			p.sendMessage(Message.WE_ACTION_QUEUED.getValue());	
		}else {
			p.sendMessage(Message.WE_NOTHING_TO_DO.getValue());
		}
	}
	
}
