package fr.olympa.olympacreatif.commandblocks.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdEffect extends CbCommand {

	public CmdEffect(CommandType type, CommandSender sender, Location loc, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(type, sender, loc, plugin, plot, args);
	}

	@Override
	public int execute() {
		PotionEffectType type = null;
		
		if (args.length >= 2)
			targetEntities = parseSelector(args[1], false);
		else
			if (sender instanceof LivingEntity)
				targetEntities.add((LivingEntity) sender);
		
		int response = 0;
		
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
				if (args[2].split(":").length == 2)
					type = PotionEffectType.getByName(args[2].split(":")[1]);
				
				if (type == null)
					return 0;
				
				for (Entity e : targetEntities)
					if (e instanceof LivingEntity)
						if (((LivingEntity)e).hasPotionEffect(type)) {
							
							((LivingEntity)e).removePotionEffect(type);
							response++;
						}
				
				return response;
			}
			return 0;
			
		case "give":
			if (args.length != 6)
				if (args.length == 5)
					args = new String[] {args[0], args[1], args[2], args[3], args[4], "true"};
				else if (args.length == 4)
				args = new String[] {args[0], args[1], args[2], args[3], "0", "true"};
				else if (args.length == 3)
					args = new String[] {args[0], args[1], args[2], "30", "0", "true"};
				else
					return 0;

			if (args[2].split(":").length == 2)
				type = PotionEffectType.getByName(args[2].split(":")[1]);

			int duration = 0;
			int amplifier = -1;
			boolean showParticles = true;
			
			if (StringUtils.isNumeric(args[3]))
				duration = (int) (double) Math.max(Math.min(Double.valueOf(args[3]), 1000000), 0) * 20;
			if (StringUtils.isNumeric(args[4]))
				amplifier = (int) (double) Math.max(Math.min(Double.valueOf(args[4]), 4), 0);
			if (args[5].equals("false"))
				showParticles = false;
			
			if (type != null && duration != 0 && amplifier != -1) {
				PotionEffect effect = new PotionEffect(type, duration, amplifier, false, showParticles);
				
				for (Entity e : targetEntities)
					if (e instanceof LivingEntity)
						((LivingEntity)e).addPotionEffect(effect);

				return targetEntities.size();
			}else
				return 0;
			
		}
		return 0;
	}
}
