package fr.olympa.olympacreatif.commandblocks.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdEffect extends CbCommand {

	public CmdEffect(CommandSender sender, Location loc, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(sender, loc, plugin, plot, args);
	}

	@Override
	public int execute() {
		PotionEffectType type;
		
		targetEntities = parseSelector(plot, args[1], false);
		
		switch (args[0]) {
		case "clear":
			switch (args.length) {
			case 2: //clear tous les effets de potion
				for (Entity e : targetEntities)
					if (e instanceof LivingEntity)
						for (PotionEffect eff : ((LivingEntity)e).getActivePotionEffects())
							((LivingEntity)e).removePotionEffect(eff.getType());
				
				return targetEntities.size();
				
			case 3: //clear un seul effet de potion
				type = PotionEffectType.getByName(args[2]);
				if (type == null)
					return targetEntities.size();
				
				for (Entity e : targetEntities)
					if (e instanceof LivingEntity)
						((LivingEntity)e).removePotionEffect(type);
				
				return targetEntities.size();
			}
			return 0;
			
		case "give":
			if (args.length != 5)
				return targetEntities.size();

			type = PotionEffectType.getByName(args[2]);

			int duration = 0;
			int amplifier = -1;

			if (StringUtils.isNumeric(args[3]))
				duration = (int) (double) Math.max(Math.min(Double.valueOf(args[3]), 5), 0);
			if (StringUtils.isNumeric(args[4]))
				amplifier = (int) (double) Math.max(Math.min(Double.valueOf(args[4]), 5), 0);
			
			if (type != null && duration != 0 && amplifier != -1) {
				PotionEffect effect = new PotionEffect(PotionEffectType.getByName(args[2]), duration, amplifier);
				
				if (args[0].equals("clear"))
					for (Entity e : targetEntities)
						if (e instanceof LivingEntity)
							((LivingEntity)e).addPotionEffect(effect);	
			}
			
			return targetEntities.size();
		}
		return 0;
	}
}
