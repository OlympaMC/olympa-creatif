package fr.olympa.olympacreatif.worldedit;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.olympacreatif.OlympaCreatifMain;

public class WorldEditManager {

	private OlympaCreatifMain plugin;
	private Map<OlympaPlayer, WorldEditInstance> playersWorldEdit = new HashMap<OlympaPlayer, WorldEditInstance>();
	
	public WorldEditManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		
	}
	
	public void addPlayer(OlympaPlayer p) {
		playersWorldEdit.put(p, new WorldEditInstance(plugin, p));
	}
	
	public void removePlayer(Player p) {
		playersWorldEdit.remove(p);
	}
	
}
