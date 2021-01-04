package fr.olympa.olympacreatif.world;

import java.io.IOException;
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
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.api.plugin.OlympaSpigot;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.redis.RedisAccess;
import fr.olympa.api.redis.RedisChannel;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OCparam;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.RedisListener;

public class WorldManager {
	private OlympaCreatifMain plugin;
	private World world = null;
	private net.minecraft.server.v1_16_R3.World nmsWorld = null;

	//public static int plotSize = 256;
	public static final int roadSize = 16;
	public static final int worldLevel = 60;
	
	public WorldManager(final OlympaCreatifMain plugin) {
		this.plugin = plugin;
		
		Bukkit.setDefaultGameMode(GameMode.CREATIVE);
		
		/*
		WorldCreator worldCreator = new WorldCreator(Message.PARAM_WORLD_NAME.getValue());
		worldCreator.generateStructures(false);
		worldCreator.generator(new CustomChunkGenerator(plugin));

		
		plugin.getLogger().log(Level.INFO, "Creative world " + Message.PARAM_WORLD_NAME.getValue() + " loading...");
		
		world = worldCreator.createWorld();
		
		System.out.println("world : " + world);*/
		
		//task pour donner l'argent aux joueurs périodiquement
		new BukkitRunnable() {
			
			final int cMax = 60;
			final int noAfkIncome = Integer.valueOf(OCparam.INCOME_NOT_AFK.get());
			final int afkIncome = Integer.valueOf(OCparam.INCOME_AFK.get());
			int c = 0;
			
			@Override
			public void run() {
				Bukkit.getOnlinePlayers().forEach(p -> {
					OlympaPlayerCreatif pp = AccountProvider.get(p.getUniqueId());

					int income = OlympaCore.getInstance().getAfkHandler().isAfk(p) ? afkIncome : noAfkIncome;
					pp.addGameMoney(income, null);
					
					c++;
					
					if (c == cMax) {
						c = 0;
						p.sendMessage(OCmsg.PERIODIC_INCOME_RECEIVED.getValue(income, noAfkIncome, afkIncome));	
					}
				});
			}
		}.runTaskTimer(plugin, 20*60, 20*60);
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
		world.setPVP(true);
		
		//édition server.properties
        Path path = Paths.get(plugin.getDataFolder().getParentFile().getAbsolutePath()).getParent().resolve("server.properties");

        try {
        	
            List<String> lines = Files.readAllLines(path);

            for (String s : new ArrayList<String>(lines)) {
            	if (s.contains("spawn-npcs") || s.contains("spawn-animals") || s.contains("spawn-monsters") || 
            			s.contains("spawn-protection") || s.contains("allow-nether") || s.contains("enable-command-block") || 
            			s.contains("difficulty") || s.contains("broadcast-rcon-to-ops") || s.contains("op-permission-level") ||
            			s.contains("broadcast-console-to-ops"))
            		lines.remove(s);
            }
            
            lines.add("spawn-npcs=true");
            lines.add("spawn-animals=true");
            lines.add("spawn-monsters=true");
            lines.add("spawn-protection=0");
            lines.add("allow-nether=false");
            lines.add("enable-command-block=true");
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
	
	public net.minecraft.server.v1_16_R3.World getNmsWorld(){
		return nmsWorld;
	}
	
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
}









