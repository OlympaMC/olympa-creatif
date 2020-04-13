package fr.olympa.olympacreatif.plot;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.objects.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;

public class PlotMembers{

	private OlympaCreatifMain plugin;
	private PlotId plotId;
	private Map<OlympaPlayerInformations, PlotRank> members = new HashMap<OlympaPlayerInformations, PlotRank>();

	public PlotMembers(OlympaCreatifMain plugin, PlotId plotId) {
		this.plugin = plugin;
		this.plotId = plotId;
	}

	public void set(Player p, PlotRank rank) {
		set(AccountProvider.get(p.getUniqueId()).getInformation(), rank);
	}
	
	public void set(OlympaPlayerInformations p, PlotRank rank) {
		//TODO
		members.put(p, rank);
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				plugin.getDataManager().updatePlayerPlotRank(p.getID(), plotId, rank);
			}
			
		}).start();
	}

	public PlotRank getPlayerRank(Player p) {
		if (members.containsKey(AccountProvider.get(p.getUniqueId()).getInformation()))
			return members.get(AccountProvider.get(p.getUniqueId()).getInformation());
		
		else return PlotRank.VISITOR;
	}

	public int getPlayerLevel(Player p) {
		if (members.containsKey(AccountProvider.get(p.getUniqueId()).getInformation()))
			return members.get(AccountProvider.get(p.getUniqueId()).getInformation()).getLevel();
		
		else return PlotRank.VISITOR.getLevel();
	}

	public enum PlotRank {

		VISITOR("visitor_level", 0, "Visiteur"),
		MEMBER("member_level", 1, "Membre"),
		TRUSTED("trusted_level", 2, "Contremaître"),
		CO_OWNER("coowner_level", 3, "Co-propriétaire"),
		OWNER("owner_level", 4, "Propriétaire");
		
		
		private String s;
		private int level;
		private String rankName;
		
		PlotRank(String s, int lvl, String desc){
			this.s = s;
			this.level = lvl;
			this.rankName = desc;
		}
		
		@Override
		public String toString() {
			return s;
		}
		
		public int getLevel() {
			return level;
		}
		
		public String getRankName() {
			return rankName;
		}
		
		public static PlotRank getPlotRank(String plotRankString) {
			for (PlotRank pr : PlotRank.values())
				if (pr.toString().equals(plotRankString))
					return pr;
			
			return null;
		}
	}

}
