package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdEffect extends CbCommand {

	private static final Map<String, PotionEffectType> potConversion = Stream.of(
			new AbstractMap.SimpleEntry<String, PotionEffectType>("DOLPHINS_GRACE", PotionEffectType.DOLPHINS_GRACE),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("ABSORPTION", PotionEffectType.ABSORPTION),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("BAD_OMEN", PotionEffectType.BAD_OMEN),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("BLINDESS", PotionEffectType.BLINDNESS),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("CONDUIT_POWER", PotionEffectType.CONDUIT_POWER),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("NAUSEA", PotionEffectType.CONFUSION),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("RESISTANCE", PotionEffectType.DAMAGE_RESISTANCE),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("HASTE", PotionEffectType.FAST_DIGGING),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("FIRES_RESISTANCE", PotionEffectType.FIRE_RESISTANCE),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("GLOWING", PotionEffectType.GLOWING),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("INSTANT_DAMAGE", PotionEffectType.HARM),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("INSTANT_HEALTH", PotionEffectType.HEAL),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("HEALTH_BOOST", PotionEffectType.HEALTH_BOOST),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("HERO_OF_THE_VILLAGE", PotionEffectType.HERO_OF_THE_VILLAGE),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("HUNGER", PotionEffectType.HUNGER),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("STRENGTH", PotionEffectType.INCREASE_DAMAGE),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("INVISIBILITY", PotionEffectType.INVISIBILITY),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("JUMP_BOOST", PotionEffectType.JUMP),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("LEVITATION", PotionEffectType.LEVITATION),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("LUCK", PotionEffectType.LUCK),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("NIGHT_VISION", PotionEffectType.NIGHT_VISION),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("POISON", PotionEffectType.POISON),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("REGENERATION", PotionEffectType.REGENERATION),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("SATURATION", PotionEffectType.SATURATION),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("SLOWNESS", PotionEffectType.SLOW),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("MINING_FATIGUE", PotionEffectType.SLOW_DIGGING),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("SLOW_FALLING", PotionEffectType.SLOW_FALLING),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("SPEED", PotionEffectType.SPEED),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("UNUCK", PotionEffectType.UNLUCK),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("WATER_BREATHING", PotionEffectType.WATER_BREATHING),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("WEAKNESS", PotionEffectType.WEAKNESS),
			new AbstractMap.SimpleEntry<String, PotionEffectType>("WITHER", PotionEffectType.WITHER))
			.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
	
	public CmdEffect(Entity sender, Location loc, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(CommandType.effect, sender, loc, plugin, plot, args);
	}

	@Override
	public int execute() {
		if (args.length == 0) 
			return 0;
		
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
				type = potConversion.get(getUndomainedString(args[1]));
				
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

			type = potConversion.get(getUndomainedString(args[2]));

			int duration = 0;
			int amplifier = -1;
			boolean showParticles = true;
			
			if (StringUtils.isNumeric(args[3]))
				duration = (int) (double) Math.max(Math.min(Double.valueOf(args[3]), 1000000), 0) * 20;
			if (StringUtils.isNumeric(args[4]))
				amplifier = (int) (double) Math.max(Math.min(Double.valueOf(args[4]), 50), 0);
			if (args[5].equals("true"))
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
