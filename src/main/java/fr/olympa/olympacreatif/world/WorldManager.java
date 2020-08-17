package fr.olympa.olympacreatif.world;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;

public class WorldManager {
	private OlympaCreatifMain plugin;
	private World world = null;
	private net.minecraft.server.v1_15_R1.World nmsWorld = null;

	public static final int plotSize = 256;
	public static final int roadSize = 16;
	public static final int worldLevel = 60;
	
	//TODO remplir la liste
	public static int maxEntitiesPerTypePerPlot;
	public static int maxTotalEntitiesPerPlot;
	
	public WorldManager(final OlympaCreatifMain plugin) {
		this.plugin = plugin;

		maxEntitiesPerTypePerPlot = Integer.valueOf(Message.PARAM_MAX_ENTITIES_PER_TYPE_PER_PLOT.getValue());
		maxTotalEntitiesPerPlot = Integer.valueOf(Message.PARAM_MAX_TOTAL_ENTITIES_PER_PLOT.getValue());

		plugin.getServer().getPluginManager().registerEvents(new WorldEventsListener(plugin), plugin);
		plugin.getServer().getPluginManager().registerEvents(new PacketListener(plugin), plugin);
		Bukkit.setDefaultGameMode(GameMode.CREATIVE);

		WorldCreator worldCreator = new WorldCreator(Message.PARAM_WORLD_NAME.getValue());
		worldCreator.generateStructures(false);
		worldCreator.generator(new CustomChunkGenerator(plugin));

		Bukkit.getLogger().log(Level.INFO, plugin.getPrefixConsole() + "Creative world " + Message.PARAM_WORLD_NAME.getValue() + " loading...");
		
		world = worldCreator.createWorld();
		
		//définition des règles du monde
		world.setDifficulty(Difficulty.EASY);
		world.setTime(6000);
		world.setSpawnLocation(0, worldLevel + 1, 0);

		world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
		world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
		world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
		world.setGameRule(GameRule.MOB_GRIEFING, false);
		world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
		world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
		world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
		world.setGameRule(GameRule.DISABLE_RAIDS, true);
		world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
		world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
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

		nmsWorld = ((CraftWorld) world).getHandle();
	}

	public World getWorld() {
		return world;
	}
	
	public net.minecraft.server.v1_15_R1.World getNmsWorld(){
		return nmsWorld;
	}
}
