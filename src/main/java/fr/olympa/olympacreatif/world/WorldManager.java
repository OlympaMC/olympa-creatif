package fr.olympa.olympacreatif.world;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.generator.ChunkGenerator;

import fr.olympa.olympacreatif.OlympaCreatifMain;

public class WorldManager {

	private World world = null;
	
	public WorldManager(final OlympaCreatifMain plugin) {
		//chargement du monde s'il existe
		for (World w : Bukkit.getWorlds())
			if (w.getName().equals(plugin.worldName))
				world = w;
		
		
		//création du monde s'il n'existe pas
		if (world == null) {
			
			WorldCreator worldCreator = new WorldCreator(plugin.worldName);
			worldCreator.generateStructures(false);
			worldCreator.environment(Environment.NORMAL);
			worldCreator.type(WorldType.FLAT);
			worldCreator.generator("minecraft:bedrock," + (plugin.worldLevel-2) + "*minecraft:dirt,minecraft:grass_block;minecraft:plains;");
			
			world = worldCreator.createWorld();
			world.setDifficulty(Difficulty.PEACEFUL);
			world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
			world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
			world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
			world.setGameRule(GameRule.MOB_GRIEFING, false);
			world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
			world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, true);

			Bukkit.getLogger().info(plugin.logPrefix + "Monde " + plugin.worldName + " non détecté. Création d'un nouveau monde...");
			
			//enregistrement du listener de l'événement de création d'un chunk pour y créer les routes
			plugin.getServer().getPluginManager().registerEvents(new Listener() {
				
				double chunksToLoad = (plugin.plotMaxCount * (plugin.plotXwidth + plugin.roadWidth) * (plugin.plotZwidth + plugin.roadWidth)) / 256;
				double loadedChunks = 0;
				double currentPercentage = 0;
				
				//listener création de chunk
				@EventHandler
				public void onChunkLoadEvent(ChunkLoadEvent e) {
					if (e.getWorld().equals(world))
						if (e.isNewChunk())
							//pour chaque bloc, on regarde si c'est une route, une bordure ou rien
							for (int x = e.getChunk().getX() ; x < e.getChunk().getX() + 16 ; x++)
								for (int z = e.getChunk().getX() ; z < e.getChunk().getX() + 16 ; z++) {
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
					
					//gestion du compteur d'avancement du traitement
					loadedChunks += 1;
					if (loadedChunks / chunksToLoad > currentPercentage) {
						currentPercentage += 0.1;
						Bukkit.getLogger().info(plugin.logPrefix + "Chargement du monde : " + currentPercentage);
					}
				}
			}, plugin);
			
			//crée la worldborder pour forcer le chargement des chunks
			world.getWorldBorder().setCenter((plugin.plotXwidth - 1) / 2, (plugin.plotZwidth - 1) / 2);
			world.getWorldBorder().setSize((plugin.plotXwidth + plugin.roadWidth), 1);
			
			Bukkit.getLogger().info(plugin.logPrefix + "Génération du monde terminée !");
		}
		
		
		plugin.getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onAnimalSpawn(CreatureSpawnEvent e) {
				if (e.getLocation().getWorld().equals(world))
					if (e.getEntityType() != EntityType.PLAYER)
						e.setCancelled(true);	
			}
		}, plugin);
	}

	public World getWorld() {
		return world;
	}
}
