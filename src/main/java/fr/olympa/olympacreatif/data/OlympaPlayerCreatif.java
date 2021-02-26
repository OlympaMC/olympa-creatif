package fr.olympa.olympacreatif.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.ImmutableMap;

import fr.olympa.api.economy.OlympaMoney;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.provider.OlympaPlayerObject;
import fr.olympa.api.sql.SQLColumn;
import fr.olympa.api.utils.Prefix;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.perks.KitsManager.KitType;
import fr.olympa.olympacreatif.perks.UpgradesManager.UpgradeType;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotId;
import fr.olympa.olympacreatif.plot.PlotPerm.PlotRank;


import fr.olympa.olympacreatif.plot.PlotsManager;

public class OlympaPlayerCreatif extends OlympaPlayerObject {
	
	private static final SQLColumn<OlympaPlayerCreatif> COLUMN_MONEY = new SQLColumn<OlympaPlayerCreatif>("gameMoney", "INT NOT NULL DEFAULT 0", Types.INTEGER).setUpdatable();
	
	private static final SQLColumn<OlympaPlayerCreatif> COLUMN_REDSTONE_KIT = new SQLColumn<OlympaPlayerCreatif>("hasRedstoneKit", "TINYINT(1) NOT NULL DEFAULT 0", Types.TINYINT).setUpdatable();
	private static final SQLColumn<OlympaPlayerCreatif> COLUMN_PEACEFUL_KIT = new SQLColumn<OlympaPlayerCreatif>("hasPeacefulMobsKit", "TINYINT(1) NOT NULL DEFAULT 0", Types.TINYINT).setUpdatable();
	private static final SQLColumn<OlympaPlayerCreatif> COLUMN_HOSTILE_KIT = new SQLColumn<OlympaPlayerCreatif>("hasHostileMobsKit", "TINYINT(1) NOT NULL DEFAULT 0", Types.TINYINT).setUpdatable();
	private static final SQLColumn<OlympaPlayerCreatif> COLUMN_FLUID_KIT = new SQLColumn<OlympaPlayerCreatif>("hasFluidKit", "TINYINT(1) NOT NULL DEFAULT 0", Types.TINYINT).setUpdatable();
	private static final SQLColumn<OlympaPlayerCreatif> COLUMN_COMMANDBLOCK_KIT = new SQLColumn<OlympaPlayerCreatif>("hasCommandblockKit", "TINYINT(1) NOT NULL DEFAULT 0", Types.TINYINT).setUpdatable();
	private static final SQLColumn<OlympaPlayerCreatif> COLUMN_ADMIN_KIT = new SQLColumn<OlympaPlayerCreatif>("hasAdminKit", "TINYINT(1) NOT NULL DEFAULT 0", Types.TINYINT).setUpdatable();
	
	private static final SQLColumn<OlympaPlayerCreatif> COLUMN_UPGRADE_COMMANDBLOCK = new SQLColumn<OlympaPlayerCreatif>("upgradeLevelCommandBlock", "TINYINT(1) NOT NULL DEFAULT 0", Types.TINYINT).setUpdatable();
	private static final SQLColumn<OlympaPlayerCreatif> COLUMN_UPGRADE_BONUSPLOTS = new SQLColumn<OlympaPlayerCreatif>("upgradeLevelBonusPlots", "TINYINT(1) NOT NULL DEFAULT 0", Types.TINYINT).setUpdatable();
	private static final SQLColumn<OlympaPlayerCreatif> COLUMN_UPGRADE_BONUSMEMBERS = new SQLColumn<OlympaPlayerCreatif>("upgradeLevelBonusMembers", "TINYINT(1) NOT NULL DEFAULT 0", Types.TINYINT).setUpdatable();
	
	private static final SQLColumn<OlympaPlayerCreatif> COLUMN_PARAM_DEFAULT_PLOT_CHAT = new SQLColumn<OlympaPlayerCreatif>("playerParamDefaultPlotChat", "TINYINT(1) NOT NULL DEFAULT 0", Types.TINYINT).setUpdatable();
	private static final SQLColumn<OlympaPlayerCreatif> COLUMN_PARAM_MENU_ON_SNEAK = new SQLColumn<OlympaPlayerCreatif>("playerParamOpenMenuOnSneak", "TINYINT(1) NOT NULL DEFAULT 1", Types.TINYINT).setUpdatable();
	
	public static final List<SQLColumn<OlympaPlayerCreatif>> COLUMNS =
			Arrays.asList(COLUMN_MONEY, COLUMN_REDSTONE_KIT, COLUMN_PEACEFUL_KIT, COLUMN_HOSTILE_KIT, COLUMN_FLUID_KIT, COLUMN_COMMANDBLOCK_KIT, COLUMN_ADMIN_KIT, COLUMN_UPGRADE_COMMANDBLOCK, COLUMN_UPGRADE_BONUSPLOTS, COLUMN_UPGRADE_BONUSMEMBERS, COLUMN_PARAM_DEFAULT_PLOT_CHAT, COLUMN_PARAM_MENU_ON_SNEAK);
	
	private static final Map<KitType, SQLColumn<OlympaPlayerCreatif>> kitsColumns = ImmutableMap.<KitType, SQLColumn<OlympaPlayerCreatif>>builder()
			.put(KitType.ADMIN, COLUMN_ADMIN_KIT)
			.put(KitType.COMMANDBLOCK, COLUMN_COMMANDBLOCK_KIT)
			.put(KitType.FLUIDS, COLUMN_FLUID_KIT)
			.put(KitType.HOSTILE_MOBS, COLUMN_HOSTILE_KIT)
			.put(KitType.PEACEFUL_MOBS, COLUMN_PEACEFUL_KIT)
			.put(KitType.REDSTONE, COLUMN_REDSTONE_KIT)
			.build();

	private static final Map<UpgradeType, SQLColumn<OlympaPlayerCreatif>> upgradesColumns = ImmutableMap.<UpgradeType, SQLColumn<OlympaPlayerCreatif>>builder()
			.put(UpgradeType.BONUS_MEMBERS_LEVEL, COLUMN_UPGRADE_BONUSMEMBERS)
			.put(UpgradeType.BONUS_PLOTS_LEVEL, COLUMN_UPGRADE_BONUSPLOTS)
			.put(UpgradeType.CB_LEVEL, COLUMN_UPGRADE_COMMANDBLOCK)
			.build();
			
	private OlympaCreatifMain plugin;
	
	//A CHANGER AVANT BETA OUVERTE
	//private int gameMoney = 0;
	//private OlympaMoney gameMoney = new OlympaMoney(0);
	private int gameMoney = 0;

	private Set<KitType> kits = new HashSet<KitType>();
	private Map<UpgradeType, Integer> upgrades = new HashMap<UpgradeType, Integer>();
	private Set<PlayerParamType> playerParams = new HashSet<PlayerParamType>();
	
	private List<String> scoreboardLines = new ArrayList<String>();
	public static final int scoreboardLinesSize = 8;
	
	private List<StaffPerm> staffPerm = new ArrayList<StaffPerm>();
	
	private Plot currentPlot = null;
	
	public OlympaPlayerCreatif(UUID uuid, String name, String ip) {
		super(uuid, name, ip);
		this.plugin = OlympaCreatifMain.getInstance();
		
		for (UpgradeType upg : UpgradeType.values())
			upgrades.put(upg, 0);

		//playerParams.add(PlayerParamType.DEFAULT_PLOT_CHAT);
		playerParams.add(PlayerParamType.OPEN_GUI_ON_SNEAK);
		
		currentPlot = plugin.getPlotsManager().getPlot(PlotId.fromId(plugin, 1));
		//gameMoney.observe("datas", () -> COLUMN_MONEY.updateAsync(this, gameMoney.get(), null, null));
	}
	
	@Override
	public void loadDatas(ResultSet resultSet) throws SQLException {
		gameMoney = resultSet.getInt("gameMoney");
		
		for (KitType kit : KitType.values())
			if (resultSet.getBoolean(kit.getBddKey()))
				kits.add(kit);
		
		for (UpgradeType upg : UpgradeType.values())
			upgrades.put(upg, resultSet.getInt(upg.getBddKey()));
		
		for (PlayerParamType param : PlayerParamType.values())
			if (resultSet.getBoolean(param.getBddKey()))
				playerParams.add(param);
			else
				playerParams.remove(param);
	}
	
	/*@Override
	public void saveDatas(PreparedStatement statement) throws SQLException {
		
		statement.setInt(1, (int) gameMoney.get());
		
		//kits
		for (int i = 2 ; i < 2 + KitType.values().length ; i++)
			if (kits.contains(KitType.values()[i - 2]))
				statement.setBoolean(i, true);
			else
				statement.setBoolean(i, false);
		
		int oldKeysLength = 2 + KitType.values().length;
		
		//améliorations
		for (int i = oldKeysLength ; i < oldKeysLength + UpgradeType.values().length ; i++)
			if (upgrades.get(UpgradeType.values()[i - oldKeysLength]) != null)
				statement.setInt(i, upgrades.get(UpgradeType.values()[i - oldKeysLength]));
			else
				statement.setInt(i, 0);
		
		oldKeysLength += UpgradeType.values().length;
		
		//player params
		for (int i = oldKeysLength ; i < oldKeysLength + PlayerParamType.values().length; i++)
			if (playerParams.contains(PlayerParamType.values()[i - oldKeysLength]))
				statement.setBoolean(i, true);
			else
				statement.setBoolean(i, false);
	}*/
	
	public int getGameMoney() {
		return gameMoney;
	}
	
	public boolean hasGameMoney(int money) {
		return getGameMoney() > money;
	}
	
	public void addGameMoney(int money, Runnable successRunnable) {
		setGameMoney(money + getGameMoney(), successRunnable);
	}
	
	public boolean withdrawGameMoney(int money, Runnable successRunnable) {
		if (!hasGameMoney(money))
			return false;
		
		setGameMoney(getGameMoney() - money, successRunnable);
		return true;
	}
	
	public void setGameMoney(int money, Runnable successRunnable) {
		gameMoney = money;
		COLUMN_MONEY.updateAsync(this, gameMoney, successRunnable, exception -> Prefix.DEFAULT_BAD.sendMessage(getPlayer(), "Une erreur est survenue lors de la mise à jour de vos informations. Erreur à signaler au staff : §4" + exception.getCause().getMessage()));
	}
	
	public boolean hasKit(KitType kit) {
		return kits.contains(kit);
	}
	
	public void addKit(KitType kit) {
		if (!kits.contains(kit))
			kits.add(kit);
		
		if (kits.contains(KitType.HOSTILE_MOBS) && kits.contains(KitType.PEACEFUL_MOBS))
			for (Plot plot : getPlots(true))
				plot.getCbData().unlockSummon();
		
		if (kits.contains(KitType.HOSTILE_MOBS))
			for (Plot plot : getPlots(true))
				plot.getCbData().unlockSpawnerSetblock();
		
		if (kit == KitType.FLUIDS)
			for (Plot plot : getPlots(true))
				plot.setAllowLiquidFlow();
		
		kitsColumns.get(kit).updateAsync(this, 1, null, null);
	}
	
	public int getUpgradeLevel(UpgradeType upg) {
		return upgrades.get(upg);
	}
	
	public void incrementUpgradeLevel(UpgradeType upg) {
		upgrades.put(upg, upgrades.get(upg) + 1);
		
		//changement du cpt des plots du joueur
		if (upg == UpgradeType.CB_LEVEL)
			for (Plot plot : getPlots(true))
				plot.getCbData().setCpt(UpgradeType.CB_LEVEL.getValueOf(upgrades.get(UpgradeType.CB_LEVEL)));
		
		upgradesColumns.get(upg).updateAsync(this, getUpgradeLevel(upg), null, null);
	}
	
	//renvoie la liste des plots où le joueur est membre
	public List<Plot> getPlots(boolean onlyOwnedPlots) {
		List<Plot> list = new ArrayList<Plot>();
		
		for (Plot plot : plugin.getPlotsManager().getPlots())
			if (plot.getMembers().getPlayerRank(this) != PlotRank.VISITOR)
				if (!onlyOwnedPlots || (plot.getMembers().getPlayerRank(getInformation()) == PlotRank.OWNER && onlyOwnedPlots))
					list.add(plot);

		
		//tri de la liste de plots par ordre croissant d'id
		Collections.sort(list, new Comparator<Plot>() {
			@Override
			public int compare(Plot p1, Plot p2) {
				return p1.getPlotId().getId() - p2.getPlotId().getId();
			}
		});
		
		return list;
	}
	
	//renvoie le nombre de plot dispo selon le rang
	public int getPlotsSlots(boolean onlyOwnedPlots) {
		if (!onlyOwnedPlots)
			return PlotsManager.maxPlotsPerPlayer;
		
		int count = UpgradeType.BONUS_PLOTS_LEVEL.getValueOf(upgrades.get(UpgradeType.BONUS_PLOTS_LEVEL));

		if (getGroups().containsKey(OlympaGroup.CREA_CONSTRUCTOR))
			count += 1;

		if (getGroups().containsKey(OlympaGroup.CREA_ARCHITECT))
			count += 2;

		return count;
	}
	
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
	
	public void clearCustomSidebar() {
		for (int i = 0 ; i < scoreboardLines.size() ; i++)
			scoreboardLines.clear();
	}
	
	
	

	public String getGameMoneyName() {
		return "Kumars";
	}
	
	public String getGameMoneySymbol() {
		return "K";
	}
	
	public Plot getCurrentPlot() {
		return currentPlot;
	}
	
	public void setCurrentPlot(Plot plot) {
		currentPlot = plot;
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

		return true;
	}
	
	public enum StaffPerm{
		BYPASS_KICK_BAN(PermissionsList.STAFF_BYPASS_PLOT_KICK_AND_BAN),
		GHOST_MODE(PermissionsList.STAFF_BYPASS_VANILLA_COMMANDS),
		WORLDEDIT_EVERYWHERE(PermissionsList.STAFF_BYPASS_WORLDEDIT),
		OWNER_EVERYWHERE(PermissionsList.STAFF_PLOT_FAKE_OWNER);
		
		OlympaPermission corePerm;
		
		StaffPerm(OlympaPermission perm){
			corePerm = perm;
			
		}
		
		public OlympaPermission getOlympaPerm() {
			return corePerm;
		}
	}

	public void setPlayerParam(PlayerParamType param, boolean state) {
		if (state) 
			playerParams.add(param);
		else
			playerParams.remove(param);
		
		if (hasPlayerParam(PlayerParamType.DEFAULT_PLOT_CHAT))
			COLUMN_PARAM_DEFAULT_PLOT_CHAT.updateAsync(this, 1, null, null);
		else
			COLUMN_PARAM_DEFAULT_PLOT_CHAT.updateAsync(this, 0, null, null);
		
		if (hasPlayerParam(PlayerParamType.OPEN_GUI_ON_SNEAK))
			COLUMN_PARAM_MENU_ON_SNEAK.updateAsync(this, 1, null, null);
		else
			COLUMN_PARAM_MENU_ON_SNEAK.updateAsync(this, 0, null, null);
	}
	
	public boolean hasPlayerParam(PlayerParamType param) {
		return playerParams.contains(param);
	}
	
	public enum PlayerParamType{
		DEFAULT_PLOT_CHAT("playerParamDefaultPlotChat"),
		OPEN_GUI_ON_SNEAK("playerParamOpenMenuOnSneak");
		
		private String bddKey;
		
		PlayerParamType(String bddKey){
			this.bddKey = bddKey;
		}
		
		public String getBddKey() {
			return bddKey;
		}
	}
}





