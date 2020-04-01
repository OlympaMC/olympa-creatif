package fr.olympa.olympacreatif.world;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
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
import org.bukkit.event.player.PlayerJoinEvent;
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

			//enregistrement du listener de l'événement de création d'un chunk pour y créer les routes
			//plugin.getServer().getPluginManager().registerEvents(new ChunkLoadListener(plugin), plugin);
			plugin.getServer().getPluginManager().registerEvents(new Listener() {
				@EventHandler
				public void onPlayerJoin(PlayerJoinEvent e) {
					e.getPlayer().teleport(new Location(world, 0,5,0));
				}
			}, plugin);

			
			WorldCreator worldCreator = new WorldCreator(plugin.worldName);
			worldCreator.generateStructures(false);
			worldCreator.environment(Environment.NORMAL);
			worldCreator.type(WorldType.FLAT);
			worldCreator.generator("minecraft:bedrock," + (plugin.worldLevel-2) + "*minecraft:dirt,minecraft:grass_block;minecraft:plains;");

			Bukkit.getLogger().log(Level.INFO, plugin.logPrefix + "World " + plugin.worldName + " not detected. Generation started. This may take a while...");
			
			world = worldCreator.createWorld();
			world.setDifficulty(Difficulty.PEACEFUL);
			world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
			world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
			world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
			world.setGameRule(GameRule.MOB_GRIEFING, false);
			world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
			world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, true);
			
			//crée la worldborder pour forcer le chargement des chunks
			//world.getWorldBorder().setCenter((plugin.plotXwidth - 1) / 2, (plugin.plotZwidth - 1) / 2);
			//world.getWorldBorder().setSize((plugin.plotXwidth + plugin.roadWidth), 1);

			Chunk ch = null;
			
			for (int a = -((plugin.plotHalfRowMaxCount-1)*(plugin.plotXwidth+plugin.roadWidth)) ; a < (plugin.plotHalfRowMaxCount*(plugin.plotXwidth+plugin.roadWidth)) ; a++)
				for (int b = -((plugin.plotHalfRowMaxCount-1)*(plugin.plotXwidth+plugin.roadWidth)) ; b < (plugin.plotHalfRowMaxCount*(plugin.plotXwidth+plugin.roadWidth)) ; b++)
					if (!world.getChunkAt(a, b).equals(ch)) {
						ch = world.getChunkAt(a, b);
						ch.load(true);

						//pour chaque bloc, on regarde si c'est une route, une bordure ou rien
						for (int x = ch.getX()*16 ; x < ch.getX()*16 + 16 ; x++) {
							for (int z = ch.getZ()*16 ; z < ch.getZ()*16 + 16 ; z++) {
								//blocs de route
								//suivant X
								if (Math.floorMod(x, (plugin.plotXwidth+plugin.roadWidth)) >= plugin.plotXwidth)
									world.getBlockAt(x, plugin.worldLevel, z).setType(Material.STONE);
								
								//suivant Z
								if (Math.floorMod(z, (plugin.plotZwidth+plugin.roadWidth)) >= plugin.plotZwidth)
									world.getBlockAt(x, plugin.worldLevel, z).setType(Material.STONE);
									
									
								//bordures de route	
								//suivant X
								if (Math.floorMod(x, (plugin.plotXwidth+plugin.roadWidth)) == plugin.plotXwidth || Math.floorMod(x, (plugin.plotXwidth+plugin.roadWidth)) == plugin.plotXwidth+plugin.roadWidth-1)
									world.getBlockAt(x, plugin.worldLevel+1, z).setType(Material.GRANITE_SLAB);
								
								//suivant Z
								if (Math.floorMod(z, (plugin.plotZwidth+plugin.roadWidth)) == plugin.plotZwidth || Math.floorMod(z, (plugin.plotZwidth+plugin.roadWidth)) == plugin.plotZwidth+plugin.roadWidth-1)
									world.getBlockAt(x, plugin.worldLevel+1, z).setType(Material.GRANITE_SLAB);
							}
						}
					}
			
			Bukkit.getLogger().info(plugin.logPrefix + "World fully generated !");
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
