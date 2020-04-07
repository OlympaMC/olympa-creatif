package fr.olympa.olympacreatif.world;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
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
import fr.olympa.olympacreatif.datas.Message;
import fr.olympa.olympacreatif.plot.Plot;

public class WorldManager {

	private World world = null;
	private List<Material> prohibitedBlocks = new ArrayList<Material>();
	
	public WorldManager(final OlympaCreatifMain plugin) {
		prohibitedBlocks.add(Material.DISPENSER);
		
		plugin.getServer().getPluginManager().registerEvents(new WorldEventsListener(plugin), plugin);
		
		//chargement du monde s'il existe
		for (World w : Bukkit.getWorlds())
			if (w.getName().equals(Message.PARAM_WORLD_NAME.getValue()))
				world = w;
		
		
		//création du monde s'il n'existe pas
		if (world == null) {
			Bukkit.getServer().setDefaultGameMode(GameMode.CREATIVE);
			
			WorldCreator worldCreator = new WorldCreator(Message.PARAM_WORLD_NAME.getValue());
			worldCreator.generateStructures(false);
			worldCreator.generator(new CustomChunkGenerator(plugin));

			Bukkit.getLogger().log(Level.INFO, Message.PARAM_PREFIX.getValue() + "World " + Message.PARAM_WORLD_NAME.getValue() + " not detected. Generation started. This may take a while...");
			world = worldCreator.createWorld();
			world.setDifficulty(Difficulty.PEACEFUL);
			world.setTime(6000);
			world.setSpawnLocation(0, Integer.valueOf(Message.PARAM_WORLD_LEVEL.getValue())+1, 0);

			world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
			world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
			world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
			world.setGameRule(GameRule.MOB_GRIEFING, false);
			world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
			world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
			world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
			
			Bukkit.getLogger().info(Message.PARAM_PREFIX.getValue() + "World fully generated !");
		}
	}

	public World getWorld() {
		return world;
	}
	
	public List<Material> getProhibitedBlocks(){
		return prohibitedBlocks;
	}
}
