package fr.olympa.olympacreatif.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.ImmutableMap; 

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.provider.OlympaPlayerObject;
import fr.olympa.api.scoreboard.sign.ScoreboardManager;
import fr.olympa.api.scoreboard.sign.lines.FixedLine;
import fr.olympa.api.scoreboard.sign.lines.TimerLine;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotId;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;

public class OlympaPlayerCreatif extends OlympaPlayerObject {

	public static final Map<String, String> COLUMNS = ImmutableMap.<String, String>builder()
			.put("bonusPlots", "INT NOT NULL DEFAULT 0")
			.put("gameMoney", "INT NOT NULL DEFAULT 0")
			.build();
	
	private OlympaCreatifMain plugin;
	private int gameMoney = 0;
	private int bonusPlots = 0;
	private boolean isAdminMode = false;
	private ScoreboardManager<OlympaPlayerCreatif> scm;

	private Map<Integer, String> scoreboardLines = new HashMap<Integer, String>();
	
	public OlympaPlayerCreatif(UUID uuid, String name, String ip) {
		super(uuid, name, ip);
		this.plugin = OlympaCreatifMain.getMainClass();
		
		createScoreboard();
	}
	
	@Override
	public void loadDatas(ResultSet resultSet) throws SQLException {
		bonusPlots = resultSet.getInt("bonusPlots");
		gameMoney = resultSet.getInt("gameMoney");
	}
	
	@Override
	public void saveDatas(PreparedStatement statement) throws SQLException {
		statement.setInt(1, bonusPlots);
		statement.setInt(2, gameMoney);
	}

	
	public void addGameMoney(int i) {
		gameMoney += Math.max(i, 0);
	}
	
	public void removeGameMoney(int i) {
		gameMoney -= Math.max(i, 0);
	}
	
	public int getGameMoney() {
		return gameMoney;
	}
	
	public void setAdmin(boolean b) {
		isAdminMode = b;
	}
	
	public boolean hasAdminMode() {
		return isAdminMode;
	}

	public void addBonusPlots(int i) {
		bonusPlots += i;
	}
	
	public int getBonusPlots() {
		return bonusPlots;
	}
	
	//renvoie la liste des plots où le joueur est membre
	public List<Plot> getPlots(boolean onlyOwnedPlots) {
		List<Plot> list = new ArrayList<Plot>();
		
		for (Plot plot : plugin.getPlotsManager().getPlots())
			if (plot.getMembers().getPlayerRank(getInformation()) != PlotRank.VISITOR)
				if (!onlyOwnedPlots || (plot.getMembers().getPlayerRank(getInformation()) == PlotRank.OWNER && onlyOwnedPlots))
					list.add(plot);
		
		return list;
	}
	
	//renvoie le nombre de plot dispo selon le rang
	public int getPlotsSlots(boolean onlyOwnedPlots) {
		if (!onlyOwnedPlots)
			return 36;
		
		int i = 1 + bonusPlots;

		if (getGroup() == OlympaGroup.CREA_CREATOR)
			i += 10;
		else if(getGroup() == OlympaGroup.CREA_ARCHITECT)
			i += 6;
		else if(getGroup() == OlympaGroup.CREA_CONSTRUCTOR)
			i += 4;
		
		return i;
	}
	
	//définit une ligne de scoreboard custom
	public void setCustomScoreboardLine(int line, String text) {
		if (scoreboardLines.size() == 0)
			for (int i = 1 ; i <= 8 ; i++)
				scoreboardLines.put(i, "§" + i);
		
		if (line >= 0 && line < 8)
			scoreboardLines.put(line, text);
	}
	
	//rétablit le scoreboard par défaut
	public void clearCustomScoreboard() {
		scoreboardLines.clear();
	}
	
	//crée le scoreboard du joueur avec des lignes dynamiques, pour afficher le scoreboard custom du plot si besoin
	@SuppressWarnings("unchecked")
	public void createScoreboard() {
		scm = new ScoreboardManager<OlympaPlayerCreatif>(plugin, "§6Olympa Créatif");

		//initialisation lignes de base scoreboard
		scm.addLines(
					new TimerLine<OlympaPlayerCreatif>( p -> {				
						if (scoreboardLines.size() >= 0)
							return scoreboardLines.get(0);
						else
							return "§1";
					}, plugin, 20),
	
					new TimerLine<OlympaPlayerCreatif>( p -> {				
						if (scoreboardLines.size() >= 0)
							return scoreboardLines.get(1);
						else {
							PlotId plotId = plugin.getPlotsManager().getPlotId(p.getPlayer().getLocation());
							if (plotId == null)
								return "§7Parcelle : §6route";
							else
								return "§7Parcelle : §6" + plotId;
						}
					}, plugin, 20),
	
					new TimerLine<OlympaPlayerCreatif>( p -> {				
						if (scoreboardLines.size() >= 0)
							return scoreboardLines.get(2);
						else
							return "§2";
					}, plugin, 20),
				
				new TimerLine<OlympaPlayerCreatif>( p -> {				
					if (scoreboardLines.size() >= 0)
						return scoreboardLines.get(3);
					else
						return "§7Grade : " + getGroupNameColored();
				}, plugin, 20),
	
				new TimerLine<OlympaPlayerCreatif>( p -> {				
					if (scoreboardLines.size() >= 0)
						return scoreboardLines.get(4);
					else {
						Plot plot = plugin.getPlotsManager().getPlot(getPlayer().getLocation());
						if (plot == null)
							return "§7Rang : §cAucun";
						else
							return "§7Rang : " + plot.getMembers().getPlayerRank(this).getRankName();
					}
				}, plugin, 20),
	
				new TimerLine<OlympaPlayerCreatif>( p -> {				
					if (scoreboardLines.size() >= 0)
						return scoreboardLines.get(5);
					else
						return "§3";
				}, plugin, 20),
	
				new TimerLine<OlympaPlayerCreatif>( p -> {				
					if (scoreboardLines.size() >= 0)
						return scoreboardLines.get(6);
					else
						return "§7[monnaie jeu] : §6" + gameMoney + "$";
				}, plugin, 20),
	
				new TimerLine<OlympaPlayerCreatif>( p -> {				
					if (scoreboardLines.size() >= 0)
						return scoreboardLines.get(7);
					else
						return "§7[monnaie serveur] : §6" + getStoreMoney().getFormatted();
				}, plugin, 20)
		);
		
		scm.addFooters(
				FixedLine.EMPTY_LINE,
				//new AnimLine(plugin, "play.olympa.fr", 1, 200)
				new FixedLine<OlympaPlayerCreatif>("§eplay.olympa.fr")
				);
		
		scm.create(this);
	}
}





