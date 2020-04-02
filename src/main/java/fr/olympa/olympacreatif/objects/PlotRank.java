package fr.olympa.olympacreatif.objects;

public enum PlotRank {

	PERMISSIONS_NULL("null_permissions_level", 0, "Visiteur"),
	PERMISSIONS_LOW("low_permissions_level", 1, "Membre"),
	PERMISSIONS_MEDIUM("medium_permissions_level", 2, "Contremaître"),
	PERMISSIONS_HIGH("high_permissions_level", 3, "Co-propriétaire"),
	PERMISSIONS_OWNER("owner_permission_level", 4, "Propriétaire");
	
	
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
