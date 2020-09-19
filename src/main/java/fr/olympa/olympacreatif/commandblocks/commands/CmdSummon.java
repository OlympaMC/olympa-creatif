package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.ArrayList;
import java.util.List;

import javax.swing.plaf.basic.BasicScrollPaneUI.HSBChangeListener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.EnumUtils;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand.CommandType;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.utils.NBTcontrollerUtil;
import fr.olympa.olympacreatif.utils.NbtParserUtil;
import fr.olympa.olympacreatif.utils.NbtParserUtil.EntitySourceType;
import net.minecraft.server.v1_15_R1.NBTTagCompound;

public class CmdSummon extends CbCommand {

	private static List<EntityType> allowedEntities = new ArrayList<EntityType>();
	
	public CmdSummon(CommandType type, CommandSender sender, Location loc, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(type, sender, loc, plugin, plot, args);
	}

	
	@Override
	public int execute() {
		//instanciateAllowedEntities();
		
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
			((CraftEntity)e).getHandle().f(tag);
			e.teleport(sendingLoc);
		}
		
		return 1;
	}
	
	private void instanciateAllowedEntities() {
		if (allowedEntities.size() > 0)
			return;

		allowedEntities.add(EntityType.ARMOR_STAND);
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
