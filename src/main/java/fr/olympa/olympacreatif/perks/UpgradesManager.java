package fr.olympa.olympacreatif.perks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent;
import fr.olympa.olympacreatif.OlympaCreatifMain;

public class UpgradesManager {

	OlympaCreatifMain plugin;
	
	private static Map<Integer, Integer> cbLevels = ImmutableMap.<Integer, Integer>builder()
			.put(1, 10) //valeur de l'upgrade, prix de l'upgrade
			.put(2, 10)
			.put(4, 10)
			.put(6, 10)
			.put(8, 10)
			.put(10, 10)
			.build();
	
	private static Map<Integer, Integer> plotLevels = ImmutableMap.<Integer, Integer>builder()
			.put(1, 10)
			.put(2, 10)
			.put(3, 10)
			.put(4, 10)
			.put(5, 10)
			.put(6, 10)
			.build();
	
	private static Map<Integer, Integer> membersLevels = ImmutableMap.<Integer, Integer>builder()
			.put(4, 10)
			.put(5, 10)
			.put(6, 10)
			.put(7, 10)
			.put(8, 10)
			.put(9, 10)
			.build();
	
	public UpgradesManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		
		plugin.getServer().getPluginManager().registerEvents(new PlayerChangeRankListener(), plugin);
	}
	
	public enum UpgradeType{
		CB_LEVEL("upgradeLevelCommandBlock", cbLevels),
		BONUS_PLOTS_LEVEL("upgradeLevelBonusPlots", plotLevels),
		BONUS_MEMBERS_LEVEL("upgradeLevelBonusMembers", membersLevels); 
		
		private String bddKey;
		private Map<Integer, Integer> values;
		
		UpgradeType(String bddKey, Map<Integer, Integer> values){
			this.bddKey = bddKey;
			this.values = values;
		}
		
		public String getBddKey() {
			return bddKey;
		}
		
		public List<Integer> getValues(){
			return new ArrayList<Integer>(Arrays.asList(values.keySet().toArray(new Integer[values.keySet().size()])));
		}
		
		public int getMaxLevel() {
			return values.size() - 1;
		}
		
		public int getValueOf(int level) {
			if (level < 0)
				return getValues().get(0);
			else if (level >= values.size())
				return getValues().get(values.size() - 1);
			else
				return getValues().get(level);
		}
		
		public int getPriceOf(int level) {
			if (level < 0 || level >= values.size())
				return 0;
			
			return values.get(getValueOf(level));
		}
	}
	
	//listener change player rank (pour message chat)
	class PlayerChangeRankListener implements Listener{
		
		@EventHandler
		public void onChangeRank(AsyncOlympaPlayerChangeGroupEvent e) {
			Bukkit.broadcastMessage("(oui je sais mise en forme à revoir) Le joueur " + e.getPlayer().getName() + " a obtenu le grade secret !"); 
		}
		
		//TODO changement des plots du joueur
	}
}
