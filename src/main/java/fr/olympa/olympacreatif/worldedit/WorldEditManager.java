package fr.olympa.olympacreatif.worldedit;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.olympacreatif.OlympaCreatifMain;

public class WorldEditManager {

	private OlympaCreatifMain plugin;
	private Map<Player, WorldEditInstance> playersWorldEdit = new HashMap<Player, WorldEditInstance>();
	
	public WorldEditManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		
	}
	
	public void addPlayer(Player p) {
		playersWorldEdit.put(p, new WorldEditInstance(plugin, p));
	}
	
	public void removePlayer(Player p) {
		playersWorldEdit.remove(p);
	}
	
}
