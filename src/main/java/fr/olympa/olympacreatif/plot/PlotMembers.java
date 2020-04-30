package fr.olympa.olympacreatif.plot;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import fr.olympa.api.item.OlympaItemBuild;
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
		
		if (members.size() >= 18)
			return;
		
		if (rank != PlotRank.VISITOR)
			members.put(p, rank);
		else
			members.remove(p);
		
		plugin.getTask().runTaskAsynchronously(() -> plugin.getDataManager().updatePlayerPlotRank(p.getId(), plotId, rank));
	}

	public PlotRank getPlayerRank(Player p) {
		return getPlayerRank(AccountProvider.get(p.getUniqueId()).getInformation());
	}

	public int getPlayerLevel(Player p) {
		return getPlayerRank(AccountProvider.get(p.getUniqueId()).getInformation()).getLevel();
	}
	
	public PlotRank getPlayerRank(OlympaPlayerInformations p) {
		if (plugin.getPlotsManager().isAdmin(p))
			return PlotRank.OWNER;
		
		if (members.containsKey(p))
			return members.get(p);
		
		else return PlotRank.VISITOR;
	}
	
	public Map<OlympaPlayerInformations, PlotRank> getList(){
		return members;
	}

	public int getCount() {
		return members.size(); 
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
		
		public static PlotRank getPlotRank(int plotRank) {
			for (PlotRank pr : PlotRank.values())
				if (pr.getLevel() == plotRank)
					return pr;
			
			return VISITOR;
		}
	}

	public OlympaPlayerInformations getOwner() {
		for (Entry<OlympaPlayerInformations, PlotRank> e : members.entrySet())
			if (e.getValue() == PlotRank.OWNER)
				return e.getKey();
		return null;
	}
}
