package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableSet;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.utils.NBTcontrollerUtil;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagDouble;
import net.minecraft.server.v1_16_R3.NBTTagList;

public class CmdSummon extends CbCommand {

	//private static List<EntityType> allowedEntities = new ArrayList<EntityType>();
	public static Set<EntityType> allowedEntities  = ImmutableSet.<EntityType>builder()
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
			.add(EntityType.VINDICATOR)
			.add(EntityType.WANDERING_TRADER)
			.add(EntityType.WITCH)
			.add(EntityType.WITHER_SKELETON)
			.add(EntityType.WOLF)
			.add(EntityType.ZOMBIE_HORSE)
			
			.add(EntityType.ZOMBIE)
			.add(EntityType.ZOMBIE_VILLAGER)
			.add(EntityType.VILLAGER)

			.add(EntityType.MINECART)
			.add(EntityType.MINECART_CHEST)
			.add(EntityType.MINECART_FURNACE)
			.add(EntityType.MINECART_HOPPER)
			
			.add(EntityType.BOAT)
			
			.add(EntityType.PIGLIN)
			.add(EntityType.PIGLIN_BRUTE)
			.add(EntityType.ZOMBIFIED_PIGLIN)

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

			.add(EntityType.ARMOR_STAND)
			.add(EntityType.ITEM_FRAME)
			.add(EntityType.PAINTING)
			.add(EntityType.ENDER_CRYSTAL)
			.build();

	private NBTTagCompound tag = null;
	private Location spawnLoc = null;
	private EntityType type = null;
	
	public CmdSummon(Entity sender, Location loc, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(CommandType.summon, sender, loc, plugin, plot, args);

		if (args.length < 1)
			return;
		
		if (args.length >= 4) 
			spawnLoc = parseLocation(args[1], args[2], args[3]);
		else
			spawnLoc = sendingLoc;
		
		if (args.length >= 5) 			
			tag = NBTcontrollerUtil.getValidTags(args[4], getSender() instanceof Player ? (Player) getSender() : null);
		
		type = EntityType.fromName(getUndomainedString(args[0]));
		
		if (!allowedEntities.contains(type)) 
			type = null;
		
	}

	
	@Override
	public int execute() {
		
		if (!plotCbData.hasUnlockedSummon()) {
			if (sender instanceof Player)
				OCmsg.HAS_NOT_UNLOCKED_SUMMON.send((Player)sender);
			return 0;
		}
		
		//return si le proprio du plot n'a pas débloqué le /summon
		if (spawnLoc == null || type == null)
			return 0;
		
		Entity e = plugin.getWorldManager().getWorld().spawnEntity(spawnLoc, type);
		
		//application du tag
		if (tag != null) {
			NBTTagList list = new NBTTagList();
			list.add(NBTTagDouble.a(e.getLocation().getX()));
			list.add(NBTTagDouble.a(e.getLocation().getY()));
			list.add(NBTTagDouble.a(e.getLocation().getZ()));
			tag.set("Pos", list);
			try {
				((CraftEntity)e).getHandle().load(tag);	
			} catch(Exception ex) {
				plugin.getLogger().warning("Error while trying to set entity data to the following: " + tag.asString() + ". Error message: " + ex.getMessage() + "Error cause: " + ex.getCause().getMessage());				
			}
			/*NBTTagCompound tag2 = new NBTTagCompound();
			((CraftEntity)e).getHandle().save(tag2);
			System.out.println("TAG of " + e.getType() + " : " + tag2.asString());*/
			/*if (tag.hasKey("Rotation")) {
				net.minecraft.server.v1_16_R3.Entity ent = ((CraftEntity)e).getHandle();
				
				NBTTagList list = tag.getList("Rotation", NBT.TAG_FLOAT);
				if (list.size() >= 2)
					ent.setPositionRotation(ent.locX(), ent.locY(), ent.locZ(), 0f, 0f);
			}*/
			
			//e.teleport(sendingLoc);
		}
		
		return 1;
	}
}
