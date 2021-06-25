package fr.olympa.olympacreatif.world;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.generator.CustomChunkGenerator;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.api.spigot.economy.OlympaMoney;
import fr.olympa.api.common.provider.AccountProvider;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OCparam;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import net.minecraft.server.v1_16_R3.ChunkProviderServer;
import net.minecraft.server.v1_16_R3.DedicatedServer;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.WorldServer;

public class WorldManager {
	private OlympaCreatifMain plugin;
	private World world = null;
	private net.minecraft.server.v1_16_R3.World nmsWorld = null;

	//public static int plotSize = 256;
	public static final int roadSize = 16;
	public static final int worldLevel = 20;
	
	private ChunkProviderServer newChunkProvider;
	
	public WorldManager(final OlympaCreatifMain plugin) {
		this.plugin = plugin;
		
		Bukkit.setDefaultGameMode(GameMode.CREATIVE);
		
		//task pour donner l'argent aux joueurs périodiquement
		/*new BukkitRunnable() {

			int currentPeriod = -1;
			
			@Override
			public void run() {
				currentPeriod++;
				currentPeriod = currentPeriod % 10;
				
				Bukkit.getOnlinePlayers().forEach(p -> {
					OlympaPlayerCreatif pc = AccountProvider.getter().get(p.getUniqueId());

					int income = OlympaCore.getInstance().getAfkHandler().isAfk(p) ? OCparam.INCOME_AFK.get() : OCparam.INCOME_NOT_AFK.get();
					pc.getGameMoney().give(income);
					
					if (currentPeriod == 0)
						OCmsg.PERIODIC_INCOME_RECEIVED.send(p, income + OlympaMoney.OMEGA);
				});
			}
		}.runTaskTimer(plugin, 200, 20*60*6);*/
	}
	
	public void defineWorldParams() {
		
		world = Bukkit.getWorld(OCparam.WORLD_NAME.get());
		nmsWorld = ((CraftWorld) world).getHandle();
		
		//définition des règles du monde
		world.setDifficulty(Difficulty.EASY);
		world.setTime(6000);
		world.setSpawnLocation(0, worldLevel + 1, 0);

		world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
		world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
		world.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
		world.setGameRule(GameRule.DISABLE_RAIDS, true);
		world.setGameRule(GameRule.MOB_GRIEFING, false);
		
		world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
		world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
		world.setGameRule(GameRule.DISABLE_ELYTRA_MOVEMENT_CHECK, true);
		world.setGameRule(GameRule.DO_FIRE_TICK, false);
		world.setGameRule(GameRule.DO_INSOMNIA, false);
		
		world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
		world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
		world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
		world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
		world.setPVP(true);
		
		//édition server.properties
        Path path = Paths.get(plugin.getDataFolder().getParentFile().getAbsolutePath()).getParent().resolve("server.properties");

        try {
        	
            List<String> lines = Files.readAllLines(path);

            for (String s : new ArrayList<String>(lines)) {
            	if (s.contains("spawn-npcs") || s.contains("spawn-animals") || s.contains("spawn-monsters") || 
            			s.contains("spawn-protection") || s.contains("allow-nether") || s.contains("enable-command-block") || 
            			s.contains("difficulty") || s.contains("broadcast-rcon-to-ops") || s.contains("op-permission-level") ||
            			s.contains("broadcast-console-to-ops") || s.contains("view-distance"))
            		lines.remove(s);
            }

            lines.add("view-distance=7");
            lines.add("spawn-npcs=false");
            lines.add("spawn-animals=false");
            lines.add("spawn-monsters=false");
            lines.add("spawn-protection=0");
            lines.add("allow-nether=false");
            lines.add("enable-command-block=false");
            lines.add("difficulty=easy");
            lines.add("op-permission-level=1");
            lines.add("broadcast-rcon-to-ops=false");
            lines.add("broadcast-console-to-ops=false");
            
            
            Files.write(path, lines, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		//register listeners
		plugin.getServer().getPluginManager().registerEvents(new WorldEventsListener(plugin), plugin);
		plugin.getServer().getPluginManager().registerEvents(new PacketListener(plugin), plugin);
		
		//set all chunks to non-force loaded
		for (Chunk ch : world.getLoadedChunks())
			ch.setForceLoaded(false);
		
		updateWorldBorder();
	}
	
	public World getWorld() {
		return world;
	}
	
	public net.minecraft.server.v1_16_R3.World getNmsWorld() {
		return nmsWorld;
	}
	
	/*public void regenerateChunk(Chunk ch) {
		world.unloadChunk(ch);

	    //newChunkProvider.unloadQueue.remove(x, z);
		net.minecraft.server.v1_16_R3.Chunk chunk = ((CraftChunk)ch).getHandle();
		//world.loadc
		//.removeTileEntity(blockposition);
		
		/*if (!unloadChunk0(x, z, false)) 
			return false;  
		long chunkKey = ChunkCoordIntPair.a(x, z); 
		(this.world.getChunkProvider()).unloadQueue.remove(chunkKey); 
		Chunk chunk = this.world.getChunkProvider().generateChunk(x, z); 
		PlayerChunk playerChunk = this.world.getPlayerChunkMap().getChunk(x, z); 
		if (playerChunk != null) playerChunk.chunk = chunk;  
		if (chunk != null) refreshChunk(x, z);  
		return (chunk != null);
	}*/
	
	/**
	 * Updates worldborder size if necessary
	 */
	public void updateWorldBorder() {
		int circleIndex = 1;
		int newSize = OCparam.PLOT_SIZE.get() + roadSize;

		
		//recherche du premier cercle de plots non plein (plot central = circleIndex 1)
		while ((plugin.getPlotsManager() == null ? plugin.getDataManager().getPlotsCount() : plugin.getPlotsManager().getTotalPlotCount())  
				> Math.pow(circleIndex*2-1, 2))
			circleIndex++;
		
		newSize += (circleIndex - 1) * (OCparam.PLOT_SIZE.get() + roadSize) * 2;
		
		WorldBorder border = world.getWorldBorder();
		
		if (border.getSize() == newSize)
			return;
		
		border.setCenter(OCparam.PLOT_SIZE.get()/2, OCparam.PLOT_SIZE.get()/2);
		border.setWarningDistance(0);
		border.setSize(newSize);
	}
	
	
	public void loadCustomWorldGenerator() {
		try {

			//get copy as NMS of all required classes
			CraftWorld craftWorld = ((CraftWorld)world);
			WorldServer worldServer = craftWorld.getHandle();

			CraftServer craftServer = ((CraftServer)Bukkit.getServer());
			MinecraftServer server = craftServer.getServer();
			
			Field field1 = CraftServer.class.getDeclaredField("console");
			field1.setAccessible(true);
			DedicatedServer dediServer = (DedicatedServer) field1.get(craftServer);

			//create bukkit ChunkGenerator instance
			OCChunkGenerator bukkitGenerator = new OCChunkGenerator(plugin);
			
			//set custom generator in bukkit World
			Field field2 = CraftWorld.class.getDeclaredField("generator");
			field2.setAccessible(true);
			field2.set(craftWorld, bukkitGenerator);
			
			//get old chunk provider which will be used to create the new one
			Field field3 = WorldServer.class.getDeclaredField("chunkProvider");
			field3.setAccessible(true);
			ChunkProviderServer oldChunkProvider = (ChunkProviderServer) field3.get(worldServer);
			
			//new chunk generator which will be used below
			net.minecraft.server.v1_16_R3.ChunkGenerator generator = new CustomChunkGenerator(worldServer, oldChunkProvider.chunkGenerator, bukkitGenerator);
			
			//create new chunk provider
			newChunkProvider = new ChunkProviderServer(worldServer, 
					worldServer.convertable, server.getDataFixer(), server.getDefinedStructureManager(), 
					dediServer.executorService, generator, plugin.getServer().getViewDistance(), server.isSyncChunkWrites(), 
					server.worldLoadListenerFactory.create(11), () -> server.E().getWorldPersistentData());
			
			field3.set(worldServer, newChunkProvider);
			
			plugin.getLogger().info("§aGénérateur de chunks chargé avec succès §2(#ReflectionPower)");
			
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			plugin.getLogger().severe("§4Erreur lors du chargement du générateur de chunks custom. Arrêt du serveur.");
			e.printStackTrace();
			Bukkit.getServer().shutdown();
		}
	}
}









