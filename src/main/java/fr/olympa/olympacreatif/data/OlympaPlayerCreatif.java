package fr.olympa.olympacreatif.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;

import com.google.common.collect.ImmutableMap; 

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.provider.OlympaPlayerObject;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.KitsManager.KitType;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;
import fr.olympa.olympacreatif.plot.PlotsManager;

public class OlympaPlayerCreatif extends OlympaPlayerObject {

	public static final Map<String, String> COLUMNS = ImmutableMap.<String, String>builder()
			.put("bonusPlots", "INT NOT NULL DEFAULT 0")
			.put("gameMoney", "INT NOT NULL DEFAULT 0")
			.put("hasRedstoneKit", "TINYINT NOT NULL DEFAULT 0")
			.put("hasPeacefulMobsKit", "TINYINT NOT NULL DEFAULT 0")
			.put("hasHostileMobsKit", "TINYINT NOT NULL DEFAULT 0")
			.put("hasFluidKit", "TINYINT NOT NULL DEFAULT 0")
			.put("hasCommandblockKit", "TINYINT NOT NULL DEFAULT 0")
			.put("hasAdminKit", "TINYINT NOT NULL DEFAULT 0")
			.build();
	
	private OlympaCreatifMain plugin;
	private int gameMoney = 0;
	private int bonusPlots = 0;

	private List<KitType> kits = new ArrayList<KitType>();
	
	private List<String> scoreboardLines = new ArrayList<String>();
	public static final int scoreboardLinesSize = 8;
	
	private List<StaffPerm> staffPerm = new ArrayList<StaffPerm>();
	
	public OlympaPlayerCreatif(UUID uuid, String name, String ip) {
		super(uuid, name, ip);
		this.plugin = OlympaCreatifMain.getMainClass();
	}
	
	@Override
	public void loadDatas(ResultSet resultSet) throws SQLException {
		bonusPlots = resultSet.getInt("bonusPlots");
		gameMoney = resultSet.getInt("gameMoney");
		
		for (KitType kit : KitType.values())
			if (resultSet.getBoolean(kit.getBddKey()))
				kits.add(kit);
	}
	
	@Override
	public void saveDatas(PreparedStatement statement) throws SQLException {
		statement.setInt(1, bonusPlots);
		statement.setInt(2, gameMoney);

		for (int i = 3 ; i < 3 + KitType.values().length ; i++)
			if (kits.contains(KitType.values()[i - 3]))
				statement.setBoolean(i, true);
			else
				statement.setBoolean(i, false);
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

	public void addBonusPlots(int i) {
		bonusPlots += i;
	}
	
	public int getBonusPlots() {
		return bonusPlots;
	}
	
	public boolean hasKit(KitType kit) {
		return kits.contains(kit);
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
			return PlotsManager.maxPlotsPerPlayer;
		
		int i = 1 + bonusPlots;

		if (getGroup() == OlympaGroup.CREA_CREATOR)
			i += 3;
		else if(getGroup() == OlympaGroup.CREA_ARCHITECT)
			i += 3;
		else if(getGroup() == OlympaGroup.CREA_CONSTRUCTOR)
			i += 1;

		return i;
	}
	
	//définit une ligne de scoreboard custom
	/*
	@Deprecated
	private void setCustomScoreboardLine(int line, String text) {
		
		if (scoreboardLines.get(0) == null)
			for (int i = 0 ; i < scoreboardLines.size() ; i++)
				scoreboardLines.put(i, "§" + i);
		
		if (line >= 0 && line < scoreboardLines.size())
			if (text != null)
				scoreboardLines.put(line, text);
			else
				scoreboardLines.put(line, "§" + line);
	}
	*/
	
	
	
	public void setCustomScoreboardTitle(String title) {
		initCustomScoreboard();
		
		scoreboardLines.set(0, title);
	}
	
	public void setCustomScoreboardLines(Map<String, Integer> scores) {
		
		initCustomScoreboard();
		
		List<String> keys = new ArrayList<String>(scores.keySet());
		List<Integer> values = new ArrayList<Integer>(scores.values());
		
		//Bukkit.broadcastMessage("Set CS : " + scores);
		
		for (int i = 1 ; i < scoreboardLinesSize ; i++)
			if (keys.size() >= i)
				//si le string commence et finit par %, on n'affiche pas le score
				if (keys.get(i - 1).startsWith("%") && keys.get(i - 1).endsWith("%"))
					scoreboardLines.set(i, keys.get(i - 1));
				else
					scoreboardLines.set(i, keys.get(i - 1) + "§r§7 : " + values.get(i - 1));
			else
				scoreboardLines.set(i, "§" + i);
		
		//Bukkit.broadcastMessage("Set CS : " + scoreboardLines);
	}
	
	
	public List<String> getCustomScoreboardLines(){
		return Collections.unmodifiableList(scoreboardLines);
	}
	
	private void initCustomScoreboard() {
		if (scoreboardLines.size() == 0)
			for (int i = 0 ; i < scoreboardLinesSize ; i++)
				scoreboardLines.add("§" + i);
	}
	
	public void clearCustomScoreboard() {
		for (int i = 0 ; i < scoreboardLines.size() ; i++)
			scoreboardLines.clear();
	}
	
	
	
	public String getGameMoneyFormated() {
		return gameMoney + "$";
	}
	
	public boolean hasStaffPerm(StaffPerm perm) {
		return staffPerm.contains(perm);
	}
	
	//renvoie true si le changement a été fait, false si le joueur n'a pas la permission
	public boolean toggleStaffPerm(StaffPerm perm) {
		if (!perm.getOlympaPerm().hasPermission(this))
			return false;
		
		if (staffPerm.contains(perm))
			staffPerm.remove(perm);
		else
			staffPerm.add(perm);
		
		/*
		if (hasStaffPerm(StaffPerm.FAKE_OWNER_EVERYWHERE)) {
			EntityPlayer nmsPlayer = ((CraftPlayer) getPlayer()).getHandle();
			nmsPlayer.playerConnection.sendPacket(new PacketPlayOutEntityStatus(nmsPlayer, (byte) 28));
		}else {
			EntityPlayer nmsPlayer = ((CraftPlayer) getPlayer()).getHandle();
			nmsPlayer.playerConnection.sendPacket(new PacketPlayOutEntityStatus(nmsPlayer, (byte) 24));	
		}
		*/
		
		return true;
	}
	
	public enum StaffPerm{
		BYPASS_KICK_AND_BAN(PermissionsList.STAFF_BYPASS_PLOT_KICK_AND_BAN),
		BYPASS_VANILLA_COMMANDS(PermissionsList.STAFF_BYPASS_VANILLA_COMMANDS),
		BYPASS_WORLDEDIT(PermissionsList.STAFF_BYPASS_WORLDEDIT),
		FAKE_OWNER_EVERYWHERE(PermissionsList.STAFF_PLOT_OWNER_EVERYWHERE);
		
		OlympaPermission corePerm;
		
		StaffPerm(OlympaPermission perm){
			corePerm = perm;
			
		}
		
		public OlympaPermission getOlympaPerm() {
			return corePerm;
		}
	}
}





