package fr.olympa.olympacreatif.world;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.objects.Plot;

public class WorldManager {

	private World world = null;
	private List<AbstractMap.SimpleEntry<Location, BlockData>> plotsToBuild = new ArrayList<AbstractMap.SimpleEntry<Location,BlockData>>();
	
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
			plugin.getServer().getPluginManager().registerEvents(new Listener() {
				@EventHandler
				public void onPlayerJoin(PlayerChatEvent e) {
					plugin.addPlot(new Plot(plugin, e.getPlayer()));
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
			world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
			world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, true);
			world.setTime(6000);
			//crée la worldborder pour forcer le chargement des chunks
			//world.getWorldBorder().setCenter((plugin.plotXwidth - 1) / 2, (plugin.plotZwidth - 1) / 2);
			//world.getWorldBorder().setSize((plugin.plotXwidth + plugin.roadWidth), 1);
			
			Bukkit.getLogger().info(plugin.logPrefix + "World fully generated !");
			
			new BukkitRunnable() {
				public void run() {
					int i = 0;
					while (i < (plugin.plotXwidth+plugin.plotZwidth)/2 && plotsToBuild.size() > 0) {
						plugin.getWorldManager().getWorld().loadChunk(plotsToBuild.get(0).getKey().getChunk());
						plotsToBuild.get(0).getKey().getBlock().setBlockData(plotsToBuild.get(0).getValue());
						plotsToBuild.remove(0);
						i++;
					}
				}
			}.runTaskTimer(plugin, 20, 2);
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
	
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void addToBuildWaitingList(Location loc, BlockData data) {
		plotsToBuild.add(new AbstractMap.SimpleEntry(loc, data));
	}
}
