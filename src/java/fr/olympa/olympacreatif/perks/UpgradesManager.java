package fr.olympa.olympacreatif.perks;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCparam;

public class UpgradesManager {

	private OlympaCreatifMain plugin;
	
	/*
	private static Map<Integer, Integer> cbLevels = ImmutableMap.<Integer, Integer>builder()
			.put(1, 1) //valeur de l'upgrade, prix de l'upgrade
			.put(2, 1)
			.put(3, 1)
			.put(4, 1)
			.put(5, 1)
			.put(7, 1)
			.build();
	
	private static Map<Integer, Integer> plotLevels = ImmutableMap.<Integer, Integer>builder()
			.put(1, 1000000)
			.put(2, 1000000)
			.put(3, 1000000)
			.put(4, 1000000)
			.put(5, 1000000)
			.put(6, 1000000)
			.put(7, 1000000)
			.build();
	
	private static Map<Integer, Integer> membersLevels = ImmutableMap.<Integer, Integer>builder()
			.put(4, 1)
			.put(5, 1)
			.put(6, 1)
			.put(7, 1)
			.put(8, 1)
			.put(9, 1)
			.build();*/
	
	public UpgradesManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		
		OCparam.SHOP_DATA.get().forEach((type, upgradess) ->
			upgradess.forEach(upgrade -> type.values.put(upgrade.level, upgrade))
		);
		//plugin.getServer().getPluginManager().registerEvents(new PlayerChangeRankListener(), plugin);
	}
	
	public enum UpgradeType {
		CB_LEVEL("upgrade_level_command_block", "augmentation des commandes par secondes pour les commandblocks"),
		BONUS_PLOTS_LEVEL("upgrade_level_bonus_plots", "augmentation du nombre de membres par parcelle"),
		BONUS_MEMBERS_LEVEL("upgrade_level_bonus_members", "augmentation de votre nombre de parcelles"); 
		
		private String bddKey;
		private String name;
		private Map<Integer, UpgradeData> values = new HashMap<Integer, UpgradeData>();
		
		UpgradeType(String bddKey, String name) {
			this.bddKey = bddKey;
			this.name = name;
		}
		
		public String getBddKey() {
			return bddKey;
		}
		
		public String getName() {
			return name;
		}
		
		/*public List<Integer> getValues() {
			return new ArrayList<Integer>(Arrays.asList(values.keySet().toArray(new Integer[values.keySet().size()])));
		}*/
		
		public int getMaxLevel() {
			return values.size() - 1;
		}
		
		
		public UpgradeData getDataOf(int level) {
			if (level < 0)
				return values.get(0);
			else if (level >= values.size())
				return values.get(values.size() - 1);
			else
				return values.get(level);
		}
		
		/*
		public int getPriceOf(int level) {
			if (level < 0 || level >= values.size())
				return 0;
			
			return values.get(getValueOf(level));
		}*/
	}
	
	
	public static class UpgradeData {
		/**
		 * Niveau de l'amélioration. Par défaut, tous les joueurs ont le niveau 0. Les indices DOIVENT se suivre de 1 en 1 depuis 0
		 */
		public final int level;
		public final UpgradeType type;
		/**
		 * Prix en euros de ce niveau d'upgrade. Uniquement à titre informatif
		 */
		public final String price;
		/**
		 * Valeur de l'upgrade (par exemple 2 pour 2 parcelles suplémentaires)
		 */
		public final int value;
		
		public UpgradeData(int level, UpgradeType type, String price, int value) {
			this.level = level;
			this.type = type;
			this.price = price;
			this.value = value;
		}		
	}
}


