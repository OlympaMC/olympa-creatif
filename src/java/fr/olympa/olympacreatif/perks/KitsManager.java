package fr.olympa.olympacreatif.perks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;

public class KitsManager {

	OlympaCreatifMain plugin;
	Map<Material, KitType> kits2;// = new HashMap<Material, KitType>();
	Map<Material, ItemStack> noKitItem2;// = new HashMap<Material, ItemStack>();
	Map<Material, net.minecraft.server.v1_16_R3.ItemStack> noKitItemNMS2;// = new HashMap<Material, net.minecraft.server.v1_16_R3.ItemStack>();
	
	public KitsManager (OlympaCreatifMain plugin) {
		this.plugin = plugin;
		initKitItems();

		Builder<Material, ItemStack> noKitItembuilder = ImmutableMap.<Material, ItemStack>builder();
		
		for (Material mat : kits2.keySet())
			noKitItembuilder.put(mat, ItemUtils.item(Material.STONE, "§cLe kit §6" + getKitOf(mat).toString().toLowerCase() + " §cest requis pour utiliser §6" + mat.toString().toLowerCase().replace("_", " ")));
		
		noKitItem2 = noKitItembuilder.build();
		Builder<Material, net.minecraft.server.v1_16_R3.ItemStack> noKitItemNMSbuilder = ImmutableMap.<Material, net.minecraft.server.v1_16_R3.ItemStack>builder();
		
		for (Entry<Material, ItemStack> e : noKitItem2.entrySet())
			noKitItemNMSbuilder.put(e.getKey(), CraftItemStack.asNMSCopy(e.getValue()));
		
		noKitItemNMS2 = noKitItemNMSbuilder.build();
	}
	
	public KitType getKitOf(Material mat) {
		return kits2.get(mat);
	}
    
    public boolean hasPlayerPermissionFor(OlympaPlayerCreatif p, Material mat) {
		
		KitType kit = getKitOf(mat);
		
		if (kit != null && !p.hasKit(kit)) {
			//p.getPlayer().sendMessage(Message.INSUFFICIENT_KIT_PERMISSION.getValue());
			return false;
		}else
			return true;
    }

    /*
	public List<Material> getMaterialsOf(KitType kit) {
		List<Material> list = new ArrayList<Material>();
		
		for (Entry<Material, KitType> e : kits2.entrySet()) 
			if (e.getValue() == kit)
				list.add(e.getKey());
		return list;
	}*/
	
	public ItemStack getNoKitPermItem(Material mat) {
		return noKitItem2.get(mat);
	}
	
	public net.minecraft.server.v1_16_R3.ItemStack getNoKitPermItemNMS(Material mat) {
		return noKitItemNMS2.get(mat);
	}
	
	private void initKitItems() {
		Builder<Material, KitType> builder = ImmutableMap.<Material, KitType>builder();
		
		List<Material> list = new ArrayList<Material>();

		//list.add(Material.DEBUG_STICK);
		list.add(Material.STRUCTURE_VOID);
		list.add(Material.STRUCTURE_BLOCK);
		list.add(Material.LINGERING_POTION);
		list.add(Material.SHULKER_SHELL);
		list.addAll(Stream.of(Material.values()).filter(mat -> mat.toString().contains("SHULKER_BOX")).collect(Collectors.toList()));
		list.add(Material.NETHER_PORTAL);
		list.add(Material.END_GATEWAY);
		list.add(Material.END_PORTAL);
		list.forEach(mat -> builder.put(mat, KitType.ADMIN));
		list.clear();
		
		
		list.add(Material.REDSTONE_TORCH);
		list.add(Material.REDSTONE_WALL_TORCH);
		
		list.add(Material.TRIPWIRE);
		list.add(Material.TRIPWIRE_HOOK);
		
		list.add(Material.REDSTONE_WIRE);
		list.add(Material.REPEATER);
		list.add(Material.COMPARATOR);
		list.add(Material.OBSERVER);
		
		list.add(Material.PISTON);
		list.add(Material.STICKY_PISTON);
		list.add(Material.PISTON_HEAD);

		list.add(Material.HOPPER);
		list.add(Material.HOPPER_MINECART);
		
		list.forEach(mat -> builder.put(mat, KitType.REDSTONE));
		list.clear();

		list.add(Material.COMMAND_BLOCK);
		list.add(Material.COMMAND_BLOCK_MINECART);
		list.add(Material.CHAIN_COMMAND_BLOCK);
		list.add(Material.REPEATING_COMMAND_BLOCK);
		
		list.forEach(mat -> builder.put(mat, KitType.COMMANDBLOCK));
		list.clear();

		list.add(Material.SPAWNER);
		list.add(Material.SPIDER_SPAWN_EGG);
		list.add(Material.CAVE_SPIDER_SPAWN_EGG);
		list.add(Material.ENDERMAN_SPAWN_EGG);
		list.add(Material.POLAR_BEAR_SPAWN_EGG);
		
		list.add(Material.BLAZE_SPAWN_EGG);
		list.add(Material.CREEPER_SPAWN_EGG);
		list.add(Material.DROWNED_SPAWN_EGG);
		list.add(Material.ELDER_GUARDIAN_SPAWN_EGG);
		list.add(Material.GUARDIAN_SPAWN_EGG);
		list.add(Material.ENDERMITE_SPAWN_EGG);
		list.add(Material.EVOKER_SPAWN_EGG);
		list.add(Material.GHAST_SPAWN_EGG);
		list.add(Material.HUSK_SPAWN_EGG);
		list.add(Material.MAGMA_CUBE_SPAWN_EGG);
		list.add(Material.PHANTOM_SPAWN_EGG);
		list.add(Material.PILLAGER_SPAWN_EGG);
		list.add(Material.RAVAGER_SPAWN_EGG);
		list.add(Material.SHULKER_SPAWN_EGG);
		list.add(Material.SILVERFISH_SPAWN_EGG);
		list.add(Material.SKELETON_SPAWN_EGG);
		list.add(Material.SKELETON_HORSE_SPAWN_EGG);
		list.add(Material.ZOMBIE_HORSE_SPAWN_EGG);
		list.add(Material.ZOMBIE_SPAWN_EGG);
		//list.add(Material.ZOMBIE_PIGMAN_SPAWN_EGG);
		list.add(Material.ZOMBIE_VILLAGER_SPAWN_EGG);
		list.add(Material.SLIME_SPAWN_EGG);
		list.add(Material.STRAY_SPAWN_EGG);
		list.add(Material.VEX_SPAWN_EGG);
		list.add(Material.VINDICATOR_SPAWN_EGG);
		list.add(Material.WITCH_SPAWN_EGG);
		list.add(Material.WITHER_SKELETON_SPAWN_EGG);
		
		list.forEach(mat -> builder.put(mat, KitType.HOSTILE_MOBS));
		list.clear();

		list.add(Material.LLAMA_SPAWN_EGG);
		list.add(Material.BEE_SPAWN_EGG);
		list.add(Material.DOLPHIN_SPAWN_EGG);
		list.add(Material.PANDA_SPAWN_EGG);
		list.add(Material.PUFFERFISH_SPAWN_EGG);
		list.add(Material.WOLF_SPAWN_EGG);
		list.add(Material.BAT_SPAWN_EGG);
		list.add(Material.CAT_SPAWN_EGG);
		list.add(Material.CHICKEN_SPAWN_EGG);
		list.add(Material.COD_SPAWN_EGG);
		list.add(Material.COW_SPAWN_EGG);
		list.add(Material.DONKEY_SPAWN_EGG);
		list.add(Material.FOX_SPAWN_EGG);
		list.add(Material.HORSE_SPAWN_EGG);
		list.add(Material.MOOSHROOM_SPAWN_EGG);
		list.add(Material.MULE_SPAWN_EGG);
		list.add(Material.OCELOT_SPAWN_EGG);
		list.add(Material.PARROT_SPAWN_EGG);
		list.add(Material.PIG_SPAWN_EGG);
		list.add(Material.RABBIT_SPAWN_EGG);
		list.add(Material.SALMON_SPAWN_EGG);
		list.add(Material.SHEEP_SPAWN_EGG);
		list.add(Material.SQUID_SPAWN_EGG);
		list.add(Material.TROPICAL_FISH_SPAWN_EGG);
		list.add(Material.TURTLE_SPAWN_EGG);
		list.add(Material.VILLAGER_SPAWN_EGG);
		list.add(Material.TRADER_LLAMA_SPAWN_EGG);
		list.add(Material.WANDERING_TRADER_SPAWN_EGG);

		list.add(Material.PUFFERFISH_BUCKET);
		list.add(Material.SALMON_BUCKET);
		list.add(Material.TROPICAL_FISH_BUCKET);

		list.forEach(mat -> builder.put(mat, KitType.PEACEFUL_MOBS));

		//builder.put(Material.DEBUG_STICK, KitType.DEBUG_STICK);

		kits2 = builder.build();
	}
	
	public static enum KitType{
		REDSTONE("hasRedstoneKit", "redstone"),
		PEACEFUL_MOBS("hasPeacefulMobsKit", "animaux"),
		HOSTILE_MOBS("hasHostileMobsKit", "monstres"),
		FLUIDS("hasFluidKit", "fluides"),
		COMMANDBLOCK("hasCommandblockKit", "command blocks"),
		//DEBUG_STICK("hasDebugStick", "débug stick"),
		ADMIN("hasAdminKit", "admin");
		
		String bddKey;
		String name;
		
		KitType(String bddKeyName, String name){
			bddKey = bddKeyName;
			this.name = name;
		}
		
		public String getBddKey() {
			return bddKey;
		}
		
		public String getName() {
			return name;
		}
		
		public boolean hasKit(OlympaPlayerCreatif pc) {
			return pc.hasKit(this);
		}
	}
}
