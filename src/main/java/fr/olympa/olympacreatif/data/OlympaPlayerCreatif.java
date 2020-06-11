package fr.olympa.olympacreatif.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.scheduler.BukkitRunnable;

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
	private boolean hasAdminMode = false;
	private ScoreboardManager<OlympaPlayerCreatif> scm;

	private Map<Integer, String> scoreboardLines = new HashMap<Integer, String>();
	
	public OlympaPlayerCreatif(UUID uuid, String name, String ip) {
		super(uuid, name, ip);
		this.plugin = OlympaCreatifMain.getMainClass();
		
		for (int i = 0 ; i < 9 ; i++)
			scoreboardLines.put(i, null);
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
	
	public void setAdminMode(boolean b) {
		hasAdminMode = b;
	}
	
	public boolean hasAdminMode() {
		return hasAdminMode;
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
		if (line >= 0 && line < scoreboardLines.size())
			scoreboardLines.put(line, text);
	}
	
	public Map<Integer, String> getCustomScoreboardLines(){
		return scoreboardLines;
	}

	public String getGameMoneyFormated() {
		return gameMoney + "₼";
	}
}





