package fr.olympa.olympacreatif.world;

import java.util.HashMap;
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

import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.data.PermissionsList;

public class WorldManager {

	private World world = null;
	private net.minecraft.server.v1_15_R1.World nmsWorld = null;
	
	//TODO remplir la liste
	private Map<Material, OlympaPermission> restrictedItems = new HashMap<Material, OlympaPermission>();
	
	public WorldManager(final OlympaCreatifMain plugin) {
		
		plugin.getServer().getPluginManager().registerEvents(new WorldEventsListener(plugin), plugin);
		generateRestrictedItems();
		
		//chargement du monde s'il existe
		for (World w : Bukkit.getWorlds())
			if (w.getName().equals(Message.PARAM_WORLD_NAME.getValue())) {
				world = w;
				nmsWorld = ((CraftWorld) w).getHandle();
			}
		
		
		//création du monde s'il n'existe pas
		if (world == null) {
			Bukkit.setDefaultGameMode(GameMode.CREATIVE);
			WorldCreator worldCreator = new WorldCreator(Message.PARAM_WORLD_NAME.getValue());
			worldCreator.generateStructures(false);
			worldCreator.generator(new CustomChunkGenerator(plugin));

			Bukkit.getLogger().log(Level.INFO, Message.PARAM_PREFIX.getValue() + "World " + Message.PARAM_WORLD_NAME.getValue() + " not detected. Generation started. This may take a while...");
			world = worldCreator.createWorld();
			world.setDifficulty(Difficulty.EASY);
			world.setTime(6000);
			world.setSpawnLocation(0, Integer.valueOf(Message.PARAM_WORLD_LEVEL.getValue())+1, 0);

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
			world.setPVP(true);
			
			Bukkit.getLogger().info(Message.PARAM_PREFIX.getValue() + "World fully generated !");
		}
	}

	public World getWorld() {
		return world;
	}
	
	public net.minecraft.server.v1_15_R1.World getNmsWorld(){
		return nmsWorld;
	}
	
	public Map<Material, OlympaPermission> getRestrictedItems(){
		return restrictedItems;
	}

	private void generateRestrictedItems() {
		restrictedItems.put(Material.LINGERING_POTION, PermissionsList.KIT_ADMIN);
		restrictedItems.put(Material.DEBUG_STICK, PermissionsList.KIT_ADMIN);
		restrictedItems.put(Material.STRUCTURE_BLOCK, PermissionsList.KIT_ADMIN);
		restrictedItems.put(Material.STRUCTURE_VOID, PermissionsList.KIT_ADMIN);
		
		restrictedItems.put(Material.BEE_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.CAT_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.CHICKEN_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.COD_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.COW_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.DOLPHIN_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.DONKEY_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.FOX_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.HORSE_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.LLAMA_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.MAGMA_CUBE_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.MOOSHROOM_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.MULE_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.OCELOT_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.PANDA_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.PARROT_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.PIG_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.PUFFERFISH_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.RABBIT_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.SALMON_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.SHEEP_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.TRADER_LLAMA_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.TROPICAL_FISH_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.TURTLE_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.VILLAGER_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.WANDERING_TRADER_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		restrictedItems.put(Material.WOLF_SPAWN_EGG, PermissionsList.KIT_ANIMALS);
		
		restrictedItems.put(Material.BAT_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.BLAZE_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.CAVE_SPIDER_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.CREEPER_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.DROWNED_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.ELDER_GUARDIAN_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.ENDERMAN_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.ENDERMITE_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.EVOKER_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.GHAST_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.GUARDIAN_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.HUSK_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.PHANTOM_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.PILLAGER_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.POLAR_BEAR_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.RAVAGER_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.SHULKER_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.SILVERFISH_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.SKELETON_HORSE_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.SKELETON_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.SLIME_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.SPIDER_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.SQUID_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.STRAY_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.VEX_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.VINDICATOR_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.WITCH_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.WITHER_SKELETON_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.ZOMBIE_HORSE_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.ZOMBIE_PIGMAN_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.ZOMBIE_SPAWN_EGG, PermissionsList.KIT_MOBS);
		restrictedItems.put(Material.ZOMBIE_VILLAGER_SPAWN_EGG, PermissionsList.KIT_MOBS);
		
		restrictedItems.put(Material.DROPPER, PermissionsList.KIT_REDSTONE);
		restrictedItems.put(Material.DISPENSER, PermissionsList.KIT_REDSTONE);
		restrictedItems.put(Material.REPEATER, PermissionsList.KIT_REDSTONE);
		restrictedItems.put(Material.REDSTONE_TORCH, PermissionsList.KIT_REDSTONE);
		restrictedItems.put(Material.PISTON, PermissionsList.KIT_REDSTONE);
		restrictedItems.put(Material.STICKY_PISTON, PermissionsList.KIT_REDSTONE);
		restrictedItems.put(Material.COMPARATOR, PermissionsList.KIT_REDSTONE);
		restrictedItems.put(Material.DAYLIGHT_DETECTOR, PermissionsList.KIT_REDSTONE);
		
		restrictedItems.put(Material.LAVA, PermissionsList.KIT_LAVA);
		restrictedItems.put(Material.LAVA_BUCKET, PermissionsList.KIT_LAVA);

	}
}
