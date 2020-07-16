package fr.olympa.olympacreatif.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.TabCompleter;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionDefault;

import com.google.common.collect.ImmutableMap; 

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.provider.OlympaPlayerObject;
import fr.olympa.api.scoreboard.sign.ScoreboardManager;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand.CommandType;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;
import fr.olympa.olympacreatif.plot.PlotsManager;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityStatus;

public class OlympaPlayerCreatif extends OlympaPlayerObject {

	public static final Map<String, String> COLUMNS = ImmutableMap.<String, String>builder()
			.put("bonusPlots", "INT NOT NULL DEFAULT 0")
			.put("gameMoney", "INT NOT NULL DEFAULT 0")
			.build();
	
	private OlympaCreatifMain plugin;
	private int gameMoney = 0;
	private int bonusPlots = 0;

	private Map<Integer, String> scoreboardLines = new HashMap<Integer, String>();
	
	private List<StaffPerm> staffPerm = new ArrayList<StaffPerm>();
	
	public OlympaPlayerCreatif(UUID uuid, String name, String ip) {
		super(uuid, name, ip);
		this.plugin = OlympaCreatifMain.getMainClass();
		
		for (int i = 0 ; i < 8 ; i++)
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
			return PlotsManager.maxPlotsPerPlayer;
		
		int i = 100 + bonusPlots;

		if (getGroup() == OlympaGroup.CREA_CREATOR)
			i += 10;
		else if(getGroup() == OlympaGroup.CREA_ARCHITECT)
			i += 6;
		else if(getGroup() == OlympaGroup.CREA_CONSTRUCTOR)
			i += 4;
		
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
		if (scoreboardLines.get(0) == null) 
			for (int i = 1 ; i < scoreboardLines.size() ; i++)
				scoreboardLines.put(i, "§" + i);
		
		scoreboardLines.put(0, title);
	}
	
	public void setCustomScoreboardValues(Map<String, Integer> scores) {
		
		List<String> keys = new ArrayList<String>(scores.keySet());
		List<Integer> values = new ArrayList<Integer>(scores.values());
		
		for (int i = 1 ; i < scoreboardLines.size() ; i++)
			if (keys.size() >= i)
				scoreboardLines.put(i, keys.get(i - 1) + " §r: " + values.get(i - 1));
			else
				scoreboardLines.put(i, "§" + i);
	}
	
	
	public Map<Integer, String> getCustomScoreboardLines(){
		return Collections.unmodifiableMap(scoreboardLines);
	}
	
	
	public void clearCustomScoreboard() {
		for (int i = 0 ; i < scoreboardLines.size() ; i++)
			scoreboardLines.put(i, null);
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





