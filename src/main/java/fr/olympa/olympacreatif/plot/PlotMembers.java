package fr.olympa.olympacreatif.plot;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.bukkit.entity.Player;

import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;

public class PlotMembers{

	private OlympaCreatifMain plugin;
	private PlotId plotId;
	private int maxMembers;
	
	//membres triés par ordre alphabétique
	private Map<OlympaPlayerInformations, PlotRank> members = new TreeMap<OlympaPlayerInformations, PlotRank>(new Comparator<OlympaPlayerInformations>() {

		@Override
		public int compare(OlympaPlayerInformations o1, OlympaPlayerInformations o2) {
			return o1.getName().compareTo(o2.getName());
		}
	});

	public PlotMembers(OlympaCreatifMain plugin, PlotId plotId, int maxMembers) {
		this.plugin = plugin;
		this.plotId = plotId;
		this.maxMembers = maxMembers;
	}

	//return false si le nombre de membres max est dépassé
	public boolean set(Player p, PlotRank rank) {
		return set(AccountProvider.get(p.getUniqueId()).getInformation(), rank);
	}
	
	public boolean set(OlympaPlayerCreatif p, PlotRank rank) {
		return set(p.getInformation(), rank);
	}
	
	public boolean set(OlympaPlayerInformations p, PlotRank rank) {
		if (members.containsKey(p)) {
			if (rank != PlotRank.VISITOR)
				members.put(p, rank);
			else
				members.remove(p);	
			
			return true;
		}
		
		if (members.size() < maxMembers) {
			members.put(p, rank);	
			return true;
		}
		
		return false;
	}

	public int getMaxMembers() {
		return maxMembers;
	}
	
	public void setMaxMembers(int max) {
		maxMembers = max;
	}
	
	public PlotRank getPlayerRank(OlympaPlayerCreatif p) {
		if (p.hasStaffPerm(StaffPerm.FAKE_OWNER_EVERYWHERE))
			return PlotRank.OWNER;
		
		return getPlayerRank(p.getInformation());
	}
	
	public PlotRank getPlayerRank(OlympaPlayerInformations pi) {
		if (members.containsKey(pi))
			return members.get(pi);
		else
			return PlotRank.VISITOR;
	}

	public PlotRank getPlayerRank(Player p) {
		return getPlayerRank((OlympaPlayerCreatif) AccountProvider.get(p.getUniqueId()));
	}
	
	
	
	
	public int getPlayerLevel(OlympaPlayerCreatif p) {
		return getPlayerRank(p).getLevel();
	}

	public int getPlayerLevel(OlympaPlayerInformations p) {
		return getPlayerRank(p).getLevel();
	}
	
	public int getPlayerLevel(Player p) {
		return getPlayerRank(p).getLevel();
	}
	
	
	
	
	public Map<OlympaPlayerInformations, PlotRank> getList(){
		return members;
	}

	public int getCount() {
		return members.size(); 
	}

	public enum PlotRank {

		VISITOR("visitor_level", 0, "§7Visiteur"),
		MEMBER("member_level", 1, "§bMembre"),
		TRUSTED("trusted_level", 2, "§3Contremaître"),
		CO_OWNER("coowner_level", 3, "§9Co-propriétaire"),
		OWNER("owner_level", 4, "§cPropriétaire");
		
		
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
			
			return VISITOR;
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
