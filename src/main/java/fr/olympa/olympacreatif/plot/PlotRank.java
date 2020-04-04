package fr.olympa.olympacreatif.plot;

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
