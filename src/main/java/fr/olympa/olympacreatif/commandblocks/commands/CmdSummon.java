package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.google.common.collect.ImmutableList;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.utils.NBTcontrollerUtil;
import net.minecraft.server.v1_16_R3.NBTTagCompound;

public class CmdSummon extends CbCommand {

	//private static List<EntityType> allowedEntities = new ArrayList<EntityType>();
	public static List<EntityType> allowedEntities  = ImmutableList.<EntityType>builder()
			.add(EntityType.ARMOR_STAND)
			.add(EntityType.BAT)
			.add(EntityType.BEE)
			.add(EntityType.BLAZE)
			.add(EntityType.CAT)
			.add(EntityType.CAVE_SPIDER)
			.add(EntityType.CHICKEN)
			.add(EntityType.COD)
			.add(EntityType.COW)
			.add(EntityType.CREEPER)
			.add(EntityType.DOLPHIN)
			.add(EntityType.DONKEY)
			.add(EntityType.DROWNED)
			.add(EntityType.ELDER_GUARDIAN)
			.add(EntityType.ENDERMAN)
			.add(EntityType.ENDERMITE)
			.add(EntityType.EVOKER)
			.add(EntityType.FOX)
			.add(EntityType.GHAST)
			.add(EntityType.GUARDIAN)
			.add(EntityType.HORSE)
			.add(EntityType.HUSK)
			.add(EntityType.LLAMA)
			.add(EntityType.MAGMA_CUBE)
			.add(EntityType.MUSHROOM_COW)
			.add(EntityType.MULE)
			.add(EntityType.OCELOT)
			.add(EntityType.PANDA)
			.add(EntityType.PARROT)
			.add(EntityType.PHANTOM)
			.add(EntityType.PIG)
			.add(EntityType.PILLAGER)
			.add(EntityType.POLAR_BEAR)
			.add(EntityType.PUFFERFISH)
			.add(EntityType.RABBIT)
			.add(EntityType.RAVAGER)
			.add(EntityType.SALMON)
			.add(EntityType.SHEEP)
			.add(EntityType.SHULKER)
			.add(EntityType.SILVERFISH)
			.add(EntityType.SKELETON_HORSE)
			.add(EntityType.SKELETON)
			.add(EntityType.SLIME)
			.add(EntityType.SPIDER)
			.add(EntityType.SQUID)
			.add(EntityType.STRAY)
			.add(EntityType.TRADER_LLAMA)
			.add(EntityType.TROPICAL_FISH)
			.add(EntityType.TURTLE)
			.add(EntityType.VEX)
			.add(EntityType.VILLAGER)
			.add(EntityType.VINDICATOR)
			.add(EntityType.WANDERING_TRADER)
			.add(EntityType.WITCH)
			.add(EntityType.WITHER_SKELETON)
			.add(EntityType.WOLF)
			.add(EntityType.ZOMBIE_HORSE)
			//.add(EntityType.PIG_ZOMBIE)
			.add(EntityType.ZOMBIE)
			.add(EntityType.ZOMBIE_VILLAGER)

			.add(EntityType.MINECART)
			.add(EntityType.MINECART_CHEST)
			.add(EntityType.MINECART_FURNACE)
			.add(EntityType.MINECART_HOPPER)
			
			.add(EntityType.BOAT)

			.add(EntityType.ARROW)
			.add(EntityType.SPECTRAL_ARROW)
			.add(EntityType.SNOWBALL)
			.add(EntityType.ENDER_PEARL)
			.add(EntityType.FALLING_BLOCK)
			.add(EntityType.AREA_EFFECT_CLOUD)
			.add(EntityType.DROPPED_ITEM)
			
			.add(EntityType.FISHING_HOOK)
			.add(EntityType.FIREBALL)
			.add(EntityType.SMALL_FIREBALL)
			.add(EntityType.FIREWORK)

			.add(EntityType.SPLASH_POTION)
			.add(EntityType.DROPPED_ITEM)
			
			.add(EntityType.ITEM_FRAME)
			.build();
	
	public CmdSummon(CommandType type, CommandSender sender, Location loc, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(type, sender, loc, plugin, plot, args);
	}

	
	@Override
	public int execute() {
		
		//return si le proprio du plot n'a pas débloqué le /summon
		if (!plotCbData.hasUnlockedSummon())
			return 0;
		
		NBTTagCompound tag = null;
		
		if (args.length >= 4) {
			Location loc = parseLocation(args[1], args[2], args[3]);
			
			if (loc != null)
				sendingLoc = loc;
			else
				return 0;
		}
		
		if (args.length >= 5) 			
			tag = NBTcontrollerUtil.getValidTags(args[4]);
		
		EntityType type = EntityType.fromName(getUndomainedString(args[0]));
		
		if (type == null || !allowedEntities.contains(type)) 
			return 0;
		
		Entity e = plugin.getWorldManager().getWorld().spawnEntity(sendingLoc, type);
		
		//application du tag
		if (tag != null) {
			((CraftEntity)e).getHandle().load(tag);
			e.teleport(sendingLoc);
		}
		
		return 1;
	}
}
