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

public class WorldEditManager {

	private OlympaCreatifMain plugin;
	private Map<Player, WorldEditInstance> playersWorldEdit = new HashMap<Player, WorldEditInstance>();
	
	//liste d'entrées comprenant : un joueur et une liste d'entrées de blocks à placer
	private List<SimpleEntry<Player, List<SimpleEntry<Location, BlockData>>>> blocksToBuild = new ArrayList<AbstractMap.SimpleEntry<Player,List<SimpleEntry<Location,BlockData>>>>();
	
	public WorldEditManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		
		plugin.getServer().getPluginManager().registerEvents(new WorldEditListener(plugin), plugin);
		
		//runnable de setblock délayé
		new BukkitRunnable() {
			
			Player p = null;
			List<SimpleEntry<Location, BlockData>> toPlace = new ArrayList<AbstractMap.SimpleEntry<Location,BlockData>>();

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
					toPlace.get(0).getKey().getBlock().setBlockData(toPlace.get(0).getValue());
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
		for (SimpleEntry<Player, List<SimpleEntry<Location, BlockData>>> e : blocksToBuild) {
			if (e.getKey().equals(p)) {
				p.sendMessage(Message.WE_ANOTHER_ACTION_ALREADY_QUEUED.getValue());
				return true;
			}
		}
		return false;
	}
	
	public void addToBuildingList(Player p, List<SimpleEntry<Location, BlockData>> blocks) {
		if (blocks.size() == 0)
			return;
		
		blocksToBuild.add(new SimpleEntry<Player, List<SimpleEntry<Location,BlockData>>>(p, blocks));
		p.sendMessage(Message.WE_ACTION_QUEUED.getValue());	
		
	}
	
	public WorldEditInstance getPlayerInstance(Player p) {
		return playersWorldEdit.get(p);
	}
	
}
