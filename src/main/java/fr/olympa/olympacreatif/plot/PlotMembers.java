package fr.olympa.olympacreatif.plot;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;

public class PlotMembers{

	private int maxMembers;
	
	//membres triés par ordre alphabétique
	private Map<MemberInformations, PlotRank> members = new TreeMap<MemberInformations, PlotRank>(new Comparator<MemberInformations>() {

		@Override
		public int compare(MemberInformations o1, MemberInformations o2) {
			return o1.getName().compareTo(o2.getName());
		}
	});
	
	public PlotMembers(int maxMembers) {
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
		return set(new MemberInformations(p), rank);
	}
	
	public boolean set(MemberInformations p, PlotRank rank) {
		if (rank == getPlayerRank(p))
			return true;
		
		if (getOwner() != null && rank == PlotRank.OWNER)
			return true;
			
		if (rank == PlotRank.VISITOR || getCount() < maxMembers) {
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

	public PlotRank getPlayerRank(Player p) {
		if (p == null)
			return PlotRank.VISITOR;
		
		return getPlayerRank((OlympaPlayerCreatif) AccountProvider.get(p.getUniqueId()));
	}
	
	public PlotRank getPlayerRank(OlympaPlayerCreatif p) {
		if (p.hasStaffPerm(StaffPerm.FAKE_OWNER_EVERYWHERE))
			return PlotRank.OWNER;
		
		return getPlayerRank(p.getInformation());
	}
	
	public PlotRank getPlayerRank(OlympaPlayerInformations p) {
		return getPlayerRank(new MemberInformations(p));
	}
	
	public PlotRank getPlayerRank(MemberInformations p) {
		for (MemberInformations info : members.keySet())
			if (info.equals(p))
				return members.get(info);
		
		return PlotRank.VISITOR;
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

	public int getPlayerLevel(MemberInformations member) {
		return getPlayerRank(member).getLevel();
	}
	
	
	
	
	public Map<MemberInformations, PlotRank> getMembers(){
		Map<MemberInformations, PlotRank> map = new HashMap<MemberInformations, PlotRank>();
		
		for (Entry<MemberInformations, PlotRank> e : members.entrySet())
			if (e.getValue() != PlotRank.VISITOR)
				map.put(e.getKey(), e.getValue());
		
		return map;
	}

	public Map<MemberInformations, PlotRank> getList(){
		return members;
	}
	
	public int getCount() {
		return getMembers().size(); 
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
			if (plotRank < 0)
				return VISITOR;
			else if (plotRank > 4)
				return OWNER;
			else
				for (PlotRank pr : PlotRank.values())
					if (pr.getLevel() == plotRank)
						return pr;
			
			return VISITOR;
		}
	}

	
	public MemberInformations getOwner() {
		for (Entry<MemberInformations, PlotRank> e : members.entrySet())
			if (e.getValue() == PlotRank.OWNER)
				return e.getKey();
		return null;
	}
	
	public class MemberInformations{

		private long id;
		private String name;
		private UUID uuid;
		
		public MemberInformations(long id, String name, UUID uuid) {
			this.id = id;
			this.name = name;
			this.uuid = uuid;
		}
		
		public MemberInformations(OlympaPlayerInformations infos) {
			id = infos.getId();
			name = infos.getName();
			uuid = infos.getUUID();
		}
		
		public long getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public UUID getUUID() {
			return uuid;
		}
		
		@Override
		public boolean equals(Object obj) {
			
			//Bukkit.broadcastMessage("COMPARE " + this.toString() + " ----TO---- "  + obj.toString() + " ----RESULT---- " + (((MemberInformations)obj).getId() == this.id));
			
			return obj instanceof MemberInformations && ((MemberInformations)obj).getId() == this.id;
		}
		
		@Override
		public String toString() {
			return new Gson().toJson(this).toString();
		}
	}
	
	public String toJson() {
		return new Gson().toJson(this);
	}
	
	
	public static PlotMembers fromJson(int maxMembers, String jsonText) {
		try {
			return new Gson().fromJson(jsonText, PlotMembers.class);	
		}catch(JsonSyntaxException e) {
			return new PlotMembers(maxMembers);
		}
	}
}
