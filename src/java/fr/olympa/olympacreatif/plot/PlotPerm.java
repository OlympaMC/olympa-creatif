package fr.olympa.olympacreatif.plot;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.olympa.api.common.permission.OlympaPermission;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OcPermissions;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;

public class PlotPerm {

	public static final PlotPerm EXPORT_PLOT = new PlotPerm(PlotRank.OWNER, "Exporter/restaurer la parcelle", OcPermissions.USE_PLOT_EXPORTATION);
	public static final PlotPerm RESET_PLOT = new PlotPerm(PlotRank.OWNER, "Réinitialiser la parcelle", OcPermissions.USE_PLOT_RESET);
	public static final PlotPerm INVITE_MEMBER = new PlotPerm(PlotRank.OWNER, "Inviter des membres");
	public static final PlotPerm SET_PLOT_SPAWN = new PlotPerm(PlotRank.CO_OWNER, "Définir le spawn parcelle");
	//public static final PlotPerm RESET_PLOT = new PlotPerm(PlotRank.OWNER, "Reset la parcelle ", PermissionsList.CREA_USE_PLOT_RESET);
	public static final PlotPerm BYPASS_EXIT_CLIPBOARD_CLEAR = new PlotPerm(PlotRank.OWNER, null);

	public static final PlotPerm BAN_VISITOR = new PlotPerm(PlotRank.CO_OWNER, "Bannir un visiteur");
	public static final PlotPerm PROMOTE_DEMOTE = new PlotPerm(PlotRank.CO_OWNER, "Promouvoir/Dégrader un membre");
	public static final PlotPerm CHANGE_PARAM_SETTINGS = new PlotPerm(PlotRank.CO_OWNER, "Modifier params généraux");
	public static final PlotPerm CHANGE_PARAM_INTERRACTION = new PlotPerm(PlotRank.CO_OWNER, "Modifier params d'interraction");
	public static final PlotPerm COMMAND_BLOCK = new PlotPerm(PlotRank.CO_OWNER, "Interragir avec les commandblocks");
	public static final PlotPerm EXECUTE_CB_CMD = new PlotPerm(PlotRank.CO_OWNER, "Utiliser les commandes vanilla");
	public static final PlotPerm MANAGE_HOLOS = new PlotPerm(PlotRank.CO_OWNER, "Gérer les hologrammes", OcPermissions.USE_HOLOGRAMS);
	
	public static final PlotPerm USE_WE = new PlotPerm(PlotRank.TRUSTED, "Utiliser WorldEdit", OcPermissions.USE_WORLD_EDIT);
	public static final PlotPerm DEFINE_MUSIC = new PlotPerm(PlotRank.TRUSTED, "Définir la musique", OcPermissions.USE_PLOT_MUSIC);
	public static final PlotPerm KICK_VISITOR = new PlotPerm(PlotRank.TRUSTED, "Ejecter un visiteur");
	public static final PlotPerm USE_STOPLAG = new PlotPerm(PlotRank.TRUSTED, "Gérer l'état de stoplag de la parcelle (/stoplag)");

	public static final PlotPerm BYPASS_ENTRY_ACTIONS = new PlotPerm(PlotRank.MEMBER, null);
	public static final PlotPerm BUILD = new PlotPerm(PlotRank.MEMBER, "Construire sur la parcelle");
	public static final PlotPerm CHANGE_GAMEMODE = new PlotPerm(PlotRank.MEMBER, "Gérer votre gamemode avec /gm");
	public static final PlotPerm USE_ARMORSTAND_EDITOR = new PlotPerm(PlotRank.MEMBER, "Utiliser l'éditeur de porte armures", OcPermissions.USE_ARMORSTAND_EDITOR);
	public static final PlotPerm DROP_ITEM = new PlotPerm(PlotRank.MEMBER, null);
	public static final PlotPerm DEFINE_OWN_FLY_SPEED = new PlotPerm(PlotRank.MEMBER, null);
	
	
	
	private PlotRank minRank;
	private String description;
	private OlympaPermission requiredPerm = null;
	
	private PlotPerm(PlotRank minRank, String desc) {
		this.minRank = minRank;
		this.description = desc;
	}
	
	private PlotPerm(PlotRank minRank, String desc, OlympaPermission perm) {
		this.minRank = minRank;
		this.description = desc + " §7(" + perm.getMinGroup().getName() + " requis)";
		this.requiredPerm = perm;
	}
	
	public boolean has(OlympaPlayerCreatif p) {
		return has(OlympaCreatifMain.getInstance().getPlotsManager().getPlot(((Player) p.getPlayer()).getLocation()), p);
	}
	
	public boolean has(Plot plot, OlympaPlayerCreatif p) {
		if (plot == null || (requiredPerm != null && !requiredPerm.hasPermission(p)))
			return false;
		
		/*if (requiredPerm != null && !requiredPerm.hasPermission(p)) {
			OCmsg.INSUFFICIENT_GROUP_PERMISSION.send(p, requiredPerm.getMinGroup().getName(p.getGender()));
			return false;
		}*/
		return has(plot.getMembers().getPlayerRank(p));
	}
	
	public boolean has(PlotRank rank) {
		return rank.getLevel() >= minRank.getLevel();
	}
	
	public String getDesc() {
		return description;
	}

	public PlotRank getRank() {
		return minRank;
	}
	
	public OlympaPermission getPerm() {
		return requiredPerm;
	}
	
	
	
	public enum PlotRank {
 
		VISITOR("visitor_level", 0, "§7Visiteur", null),
		MEMBER("member_level", 1, "§bMembre", Material.IRON_BLOCK),
		TRUSTED("trusted_level", 2, "§3Contremaître", Material.GOLD_BLOCK),
		CO_OWNER("co-owner_level", 3, "§9Co-propriétaire", Material.DIAMOND_BLOCK),
		OWNER("owner_level", 4, "§cPropriétaire", Material.EMERALD_BLOCK);
		
		
		private String s;
		private int level;
		private String rankName;
		private List<String> relatedPermsDescs = null;
		private Material mat;
		
		PlotRank(String s, int lvl, String desc, Material mat){
			this.s = s;
			this.level = lvl;
			this.rankName = desc;
			this.mat = mat;
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
		
		public Material getMat() {
			return mat;
		}
		
		public boolean has(Plot plot, OlympaPlayerCreatif p) {
			return plot == null ? false : plot.getMembers().getPlayerRank(p).getLevel() >= level;
		}
		
		public int compare(PlotRank rank) {
			return level - rank.getLevel();
		}
		
		public PlotRank getUpgrade() {
			if (level == 4)
				return null;
			return getPlotRank(level + 1);
		}
		
		public PlotRank getDowngrade() {
			if (level == 0)
				return null;
			return getPlotRank(level - 1);
		}
		
		public List<String> getPermsDescs(){
			if (relatedPermsDescs != null)
				return relatedPermsDescs;
			
			relatedPermsDescs = new ArrayList<String>();
			
			try {
				for (Field f : PlotPerm.class.getFields())
					if (f.getType() == PlotPerm.class && Modifier.isStatic(f.getModifiers())) {
						PlotPerm perm = (PlotPerm) f.get(null);
						//Bukkit.broadcastMessage(this + " : " + f.getName() + " : " + (perm.getRank() == this && perm.getDesc() != null));
						
						if (perm.getRank() == this && perm.getDesc() != null)
							relatedPermsDescs.add(perm.getDesc());
					}
				
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
			
			return relatedPermsDescs;
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
}