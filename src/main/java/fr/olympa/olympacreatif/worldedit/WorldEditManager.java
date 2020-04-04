package fr.olympa.olympacreatif.worldedit;

import java.util.AbstractMap;
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
	private List<AbstractMap.SimpleEntry<Location, BlockData>> plotsToBuild = new ArrayList<AbstractMap.SimpleEntry<Location,BlockData>>();
	
	public WorldEditManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;

		//runnable de setblock délayé
		new BukkitRunnable() {
			public void run() {
				int i = 0;
				while (i < Integer.valueOf(Message.PARAM_WORLDEDIT_BPS.getValue())/5 && plotsToBuild.size() > 0) {
					plugin.getWorldManager().getWorld().loadChunk(plotsToBuild.get(0).getKey().getChunk());
					plotsToBuild.get(0).getKey().getBlock().setBlockData(plotsToBuild.get(0).getValue());
					plotsToBuild.remove(0);
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
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addToBuildWaitingList(Location loc, BlockData data) {
		plotsToBuild.add(new AbstractMap.SimpleEntry(loc, data));
	}
	
}
