package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.perks.NbtEntityParser;
import fr.olympa.olympacreatif.perks.NbtEntityParser.EntitySourceType;
import fr.olympa.olympacreatif.plot.Plot;
import net.minecraft.server.v1_15_R1.NBTTagCompound;

public class CmdSummon extends CbCommand {

	private static List<EntityType> allowedEntities = new ArrayList<EntityType>();
	
	public CmdSummon(CommandSender sender, Location loc, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(sender, loc, plugin, plot, args);
	}

	
	@Override
	public int execute() {
		instanciateAllowedEntities();
		
		NBTTagCompound tag = new NBTTagCompound();
		
		if (args.length < 4 )
			return 0;
		
		if (args.length == 5)
			tag = plugin.getPerksManager().getNbtEntityParser().getEntityNbtData(args[4], EntitySourceType.SUMMON);
			
		EntityType type = EntityType.fromName(args[0]);
		
		Location spawnLoc = getLocation(args[1], args[2], args[3]);
		
		if (type == null || !allowedEntities.contains(type) || spawnLoc == null || tag == null) 
			return 0;
		
		Entity e = plugin.getWorldManager().getWorld().spawnEntity(spawnLoc, type);

		//application du tag
		((CraftEntity)e).getHandle().f(tag);
		
		return 1;
	}
	
	private void instanciateAllowedEntities() {
		if (allowedEntities.size() == 0)
			return;
		
		allowedEntities.add(EntityType.BAT);
		allowedEntities.add(EntityType.BEE);
		allowedEntities.add(EntityType.BLAZE);
		allowedEntities.add(EntityType.CAT);
		allowedEntities.add(EntityType.CAVE_SPIDER);
		allowedEntities.add(EntityType.CHICKEN);
		allowedEntities.add(EntityType.COD);
		allowedEntities.add(EntityType.COW);
		allowedEntities.add(EntityType.CREEPER);
		allowedEntities.add(EntityType.DOLPHIN);
		allowedEntities.add(EntityType.DONKEY);
		allowedEntities.add(EntityType.DROWNED);
		allowedEntities.add(EntityType.ELDER_GUARDIAN);
		allowedEntities.add(EntityType.ENDERMAN);
		allowedEntities.add(EntityType.ENDERMITE);
		allowedEntities.add(EntityType.EVOKER);
		allowedEntities.add(EntityType.FOX);
		allowedEntities.add(EntityType.GHAST);
		allowedEntities.add(EntityType.GUARDIAN);
		allowedEntities.add(EntityType.HORSE);
		allowedEntities.add(EntityType.HUSK);
		allowedEntities.add(EntityType.LLAMA);
		allowedEntities.add(EntityType.MAGMA_CUBE);
		allowedEntities.add(EntityType.MUSHROOM_COW);
		allowedEntities.add(EntityType.MULE);
		allowedEntities.add(EntityType.OCELOT);
		allowedEntities.add(EntityType.PANDA);
		allowedEntities.add(EntityType.PARROT);
		allowedEntities.add(EntityType.PHANTOM);
		allowedEntities.add(EntityType.PIG);
		allowedEntities.add(EntityType.PILLAGER);
		allowedEntities.add(EntityType.POLAR_BEAR);
		allowedEntities.add(EntityType.PUFFERFISH);
		allowedEntities.add(EntityType.RABBIT);
		allowedEntities.add(EntityType.RAVAGER);
		allowedEntities.add(EntityType.SALMON);
		allowedEntities.add(EntityType.SHEEP);
		allowedEntities.add(EntityType.SHULKER);
		allowedEntities.add(EntityType.SILVERFISH);
		allowedEntities.add(EntityType.SKELETON_HORSE);
		allowedEntities.add(EntityType.SKELETON);
		allowedEntities.add(EntityType.SLIME);
		allowedEntities.add(EntityType.SPIDER);
		allowedEntities.add(EntityType.SQUID);
		allowedEntities.add(EntityType.STRAY);
		allowedEntities.add(EntityType.TRADER_LLAMA);
		allowedEntities.add(EntityType.TROPICAL_FISH);
		allowedEntities.add(EntityType.TURTLE);
		allowedEntities.add(EntityType.VEX);
		allowedEntities.add(EntityType.VILLAGER);
		allowedEntities.add(EntityType.VINDICATOR);
		allowedEntities.add(EntityType.WANDERING_TRADER);
		allowedEntities.add(EntityType.WITCH);
		allowedEntities.add(EntityType.WITHER_SKELETON);
		allowedEntities.add(EntityType.WOLF);
		allowedEntities.add(EntityType.ZOMBIE_HORSE);
		allowedEntities.add(EntityType.PIG_ZOMBIE);
		allowedEntities.add(EntityType.ZOMBIE);
		allowedEntities.add(EntityType.ZOMBIE_VILLAGER);

	}
	
}
