package fr.olympa.olympacreatif.perks;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.google.common.collect.ImmutableList;

import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent;
import fr.olympa.olympacreatif.OlympaCreatifMain;

public class UpgradesManager {

	OlympaCreatifMain plugin;
	
	private static List<Integer> cbLevels = ImmutableList.<Integer>builder()
			.add(1)
			.add(2)
			.add(4)
			.add(6)
			.add(8)
			.add(10)
			.build();
	
	private static List<Integer> plotLevels = ImmutableList.<Integer>builder()
			.add(1)
			.add(2)
			.add(3)
			.add(4)
			.add(5)
			.add(6)
			.build();
	
	private static List<Integer> membersLevels = ImmutableList.<Integer>builder()
			.add(4)
			.add(5)
			.add(6)
			.add(7)
			.add(8)
			.add(9)
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
		private List<Integer> values;
		
		UpgradeType(String bddKey, List<Integer> values){
			this.bddKey = bddKey;
			this.values = values;
		}
		
		public String getBddKey() {
			return bddKey;
		}
		
		public List<Integer> getValues(){
			return values;
		}
		
		public int getValueOf(int level) {
			if (level < 0)
				return values.get(0);
			else if (level >= values.size())
				return values.get(values.size() - 1);
			else
				return values.get(level);
		}
	}
	
	//listener change player rank (pour message chat)
	class PlayerChangeRankListener implements Listener{
		
		@EventHandler
		public void onChangeRank(AsyncOlympaPlayerChangeGroupEvent e) {
			Bukkit.broadcastMessage("(oui je sais mise en forme à revoir) Le joueur " + e.getPlayer().getName() + " a obtenu le grade secret !"); 
		}
	}
}
