package fr.olympa.olympacreatif.world;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.olympacreatif.OlympaCreatifMain;

public class ChunkLoadListener implements Listener {
		
	OlympaCreatifMain plugin;
	
	public ChunkLoadListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;
	}
	
		//double chunksToLoad = (plugin.plotMaxCount * (plugin.plotXwidth + plugin.roadWidth) * (plugin.plotZwidth + plugin.roadWidth)) / 256;
		//double loadedChunks = 0;
		//double currentPercentage = 0;
		
		//listener création de chunk
	@EventHandler
	public void onChunkLoadEvent(final ChunkLoadEvent e) {
		if (e.getWorld().getName().equals(plugin.worldName)) {
			//if (true) {
			if (e.isNewChunk()) {
				//Début du thread async pour éviter le plantage du serveur
				new Thread(new Runnable() {

					public void run() {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						e.getChunk().load(true);
						
						//pour chaque bloc, on regarde si c'est une route, une bordure ou rien
						for (int x = e.getChunk().getX()*16 ; x < e.getChunk().getX()*16 + 16 ; x++) {
							for (int z = e.getChunk().getZ()*16 ; z < e.getChunk().getZ()*16 + 16 ; z++) {
								//blocs de route
								//suivant X
								if (Math.floorMod(x, (plugin.plotXwidth+plugin.roadWidth)) >= plugin.plotXwidth)
									e.getWorld().getBlockAt(x, plugin.worldLevel, z).setType(Material.STONE);
								
								//suivant Z
								if (Math.floorMod(z, (plugin.plotZwidth+plugin.roadWidth)) >= plugin.plotZwidth)
									e.getWorld().getBlockAt(x, plugin.worldLevel, z).setType(Material.STONE);
									
									
								//bordures de route	
								//suivant X
								if (Math.floorMod(x, (plugin.plotXwidth+plugin.roadWidth)) == plugin.plotXwidth || Math.floorMod(x, (plugin.plotXwidth+plugin.roadWidth)) == plugin.plotXwidth+plugin.roadWidth-1)
									e.getWorld().getBlockAt(x, plugin.worldLevel+1, z).setType(Material.GRANITE_SLAB);
								
								//suivant Z
								if (Math.floorMod(z, (plugin.plotZwidth+plugin.roadWidth)) == plugin.plotZwidth || Math.floorMod(z, (plugin.plotZwidth+plugin.roadWidth)) == plugin.plotZwidth+plugin.roadWidth-1)
									e.getWorld().getBlockAt(x, plugin.worldLevel+1, z).setType(Material.GRANITE_SLAB);
							}
						}
					}
				}).start();
			}
		}
		
		//gestion du compteur d'avancement du traitement
		/*loadedChunks += 1;
		if (loadedChunks / chunksToLoad > currentPercentage) {
			currentPercentage += 0.1;
			Bukkit.getLogger().info(plugin.logPrefix + "Chargement du monde : " + currentPercentage);
		}*/
}
	//@EventHandler
	public void onSaySomething(PlayerChatEvent e) {
		Bukkit.broadcastMessage("Loc x = " + e.getPlayer().getLocation().getChunk().getX() + " , z = " + e.getPlayer().getLocation().getChunk().getZ());
				//pour chaque bloc, on regarde si c'est une route, une bordure ou rien
				for (int x = e.getPlayer().getLocation().getChunk().getX()*16 ; x < e.getPlayer().getLocation().getChunk().getX()*16 + 16 ; x++) {
					for (int z = e.getPlayer().getLocation().getChunk().getZ()*16 ; z < e.getPlayer().getLocation().getChunk().getZ()*16 + 16 ; z++) {
						//blocs de route
						//suivant X
						if (Math.floorMod(x, (plugin.plotXwidth+plugin.roadWidth)) >= plugin.plotXwidth)
							e.getPlayer().getLocation().getWorld().getBlockAt(x, plugin.worldLevel, z).setType(Material.STONE);
						
						//suivant Z
						if (Math.floorMod(z, (plugin.plotZwidth+plugin.roadWidth)) >= plugin.plotZwidth)
							e.getPlayer().getLocation().getWorld().getBlockAt(x, plugin.worldLevel, z).setType(Material.STONE);
							
							
						//bordures de route	
						//suivant X
						if (Math.floorMod(x, (plugin.plotXwidth+plugin.roadWidth)) == plugin.plotXwidth || Math.floorMod(x, (plugin.plotXwidth+plugin.roadWidth)) == plugin.plotXwidth+plugin.roadWidth-1)
							e.getPlayer().getLocation().getWorld().getBlockAt(x, plugin.worldLevel+1, z).setType(Material.GRANITE_SLAB);
						
						//suivant Z
						if (Math.floorMod(z, (plugin.plotZwidth+plugin.roadWidth)) == plugin.plotZwidth || Math.floorMod(z, (plugin.plotZwidth+plugin.roadWidth)) == plugin.plotZwidth+plugin.roadWidth-1)
							e.getPlayer().getLocation().getWorld().getBlockAt(x, plugin.worldLevel+1, z).setType(Material.GRANITE_SLAB);

					}
				}
			}
		
		
		//gestion du compteur d'avancement du traitement
		/*loadedChunks += 1;
		if (loadedChunks / chunksToLoad > currentPercentage) {
			currentPercentage += 0.1;
			Bukkit.getLogger().info(plugin.logPrefix + "Chargement du monde : " + currentPercentage);
		}*/
	
	
}
