package fr.olympa.olympacreatif.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;

import com.google.common.collect.ImmutableMap;

import fr.olympa.api.spigot.economy.MoneyPlayerInterface;
import fr.olympa.api.spigot.economy.OlympaMoney;
import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.permission.OlympaSpigotPermission;
import fr.olympa.api.common.sql.SQLColumn;
import fr.olympa.api.commun.provider.OlympaPlayerObject;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.perks.KitsManager.KitType;
import fr.olympa.olympacreatif.perks.UpgradesManager.UpgradeType;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotPerm.PlotRank;
import fr.olympa.olympacreatif.plot.PlotsManager;

public class OlympaPlayerCreatif extends OlympaPlayerObject implements MoneyPlayerInterface {
	
	private static final SQLColumn<OlympaPlayerCreatif> COLUMN_MONEY = new SQLColumn<OlympaPlayerCreatif>("gameMoney", "INT NOT NULL DEFAULT 100", Types.INTEGER).setUpdatable();
	
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
	
	private static final SQLColumn<OlympaPlayerCreatif> COLUMN_HAS_CLAIMED_VIP = new SQLColumn<OlympaPlayerCreatif>("hasClaimedVipReward", "BOOLEAN NOT NULL DEFAULT FALSE", Types.BOOLEAN).setUpdatable();
	
	public static final List<SQLColumn<OlympaPlayerCreatif>> COLUMNS =
			Arrays.asList(COLUMN_MONEY, COLUMN_REDSTONE_KIT, COLUMN_PEACEFUL_KIT, 
					COLUMN_HOSTILE_KIT, COLUMN_FLUID_KIT, COLUMN_COMMANDBLOCK_KIT, 
					COLUMN_ADMIN_KIT, COLUMN_UPGRADE_COMMANDBLOCK, COLUMN_UPGRADE_BONUSPLOTS, 
					COLUMN_UPGRADE_BONUSMEMBERS, COLUMN_PARAM_DEFAULT_PLOT_CHAT, COLUMN_PARAM_MENU_ON_SNEAK, 
					COLUMN_HAS_CLAIMED_VIP);
	
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
	
	private boolean hasClaimedVipRewards = false; //utilisé pour les récompenses uniques VIP
	private OlympaMoney money = new OlympaMoney(0);

	private Set<KitType> kits = new HashSet<KitType>();
	private Map<UpgradeType, Integer> upgrades = new HashMap<UpgradeType, Integer>();
	private Set<PlayerParamType> playerParams = new HashSet<PlayerParamType>();

	public static final int customScoreboardLinesSize = 8;
	private String[] customScoreboardLines = new String[customScoreboardLinesSize];
	private boolean isCustomSidebarEnabled = false;
	
	private List<StaffPerm> staffPerm = new ArrayList<StaffPerm>();
	
	private Plot currentPlot = null;
	
	public OlympaPlayerCreatif(UUID uuid, String name, String ip) {
		super(uuid, name, ip);
		this.plugin = OlympaCreatifMain.getInstance();
		
		for (UpgradeType upg : UpgradeType.values())
			upgrades.put(upg, 0);

		playerParams.add(PlayerParamType.OPEN_GUI_ON_SNEAK);
	}
	
	@Override
	public void loadDatas(ResultSet resultSet) throws SQLException {
		money.set(resultSet.getInt("gameMoney"));
		
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
		
		hasClaimedVipRewards = resultSet.getBoolean("hasClaimedVipReward");
		
		//currentPlot = plugin.getPlotsManager().getPlot(PlotId.fromId(plugin, 1));
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

	@Override
	public void loaded() {
		super.loaded();
		money.observe("datas", () -> COLUMN_MONEY.updateAsync(this, money.get(), null, ex -> getPlayer().sendMessage("§cFailed to save your new money. Please send this to the staff.")));
		
		if (!hasClaimedVipRewards && getGroups().containsKey(OlympaGroup.VIP)) 
			COLUMN_HAS_CLAIMED_VIP.updateAsync(this, true, () -> {
				OCmsg.GIVE_VIP_REWARD.send(this);
				money.give(100);
			}, null);
		
		currentPlot = plugin.getPlotsManager().getPlot(getPlayer().getLocation());
	}

	@Override
	public OlympaMoney getGameMoney() {
		return money;
	}
	
	/*
	public OlympaMoney getMoney() {
		return money;
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
		COLUMN_MONEY.updateAsync(this, gameMoney, successRunnable, exception -> Prefix.DEFAULT_BAD.sendMessage(getPlayer(), "Une erreur est survenue lors de la mise à jour de vos informations. \nErreur à signaler au staff : §4" + exception.getCause().getMessage()));
	}*/
	
	public boolean hasKit(KitType kit) {
		return kits.contains(kit);
	}
	
	public void addKit(KitType kit) {
		if (!kits.contains(kit))
			kits.add(kit);
		
		if (kits.contains(KitType.HOSTILE_MOBS) && kits.contains(KitType.PEACEFUL_MOBS))
			for (Plot plot : getPlots(true)) {
				plot.getCbData().unlockSummon();
				plot.getCbData().unlockSpawnerSetblock();	
			}
		
		if (kit == KitType.COMMANDBLOCK)
			plugin.getPermissionsManager().setCbPerms(this);
		
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
				plot.getCbData().setCommandsPerSecond(UpgradeType.CB_LEVEL.getOf(upgrades.get(UpgradeType.CB_LEVEL)));
		
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
				return p1.getId().getId() - p2.getId().getId();
			}
		});
		
		return list;
	}
	
	//renvoie le nombre de plot dispo selon le rang
	public int getPlotsSlots(boolean onlyOwnedPlots) {
		if (!onlyOwnedPlots)
			return PlotsManager.maxPlotsPerPlayer;
		
		int count = UpgradeType.BONUS_PLOTS_LEVEL.getOf(upgrades.get(UpgradeType.BONUS_PLOTS_LEVEL)).value;

		if (getGroups().containsKey(OlympaGroup.CREA_CONSTRUCTOR))
			count += 1;

		if (getGroups().containsKey(OlympaGroup.CREA_ARCHITECT))
			count += 2;

		return count;
	}
	
	public void setCustomScoreboardLines(String title, LinkedHashMap<String, Integer> scores) {
		isCustomSidebarEnabled = true;
		customScoreboardLines[0] = ChatColor.BOLD + title;
		customScoreboardLines[customScoreboardLinesSize - 1] = "§7Custom sb plot " + currentPlot;
		
		for (int i = scores.size() + 1 ; i < customScoreboardLinesSize - 1 ; i++)
			scores.put("§" + i, Integer.MIN_VALUE);
		
		List<String> keys = new ArrayList<String>(scores.keySet());
		for (int i = 1 ; i < customScoreboardLinesSize - 1 ; i++)
			if (keys.get(i - 1).length() == 2 && scores.get(keys.get(i - 1)) == Integer.MIN_VALUE)
				customScoreboardLines[i] = keys.get(i - 1);
			else
				customScoreboardLines[i] = keys.get(i - 1) + "§7 : " + scores.get(keys.get(i - 1));
	}
	
	
	public String[] getCustomScoreboardLines() {
		return isCustomSidebarEnabled ? customScoreboardLines : null;
	}
	
	public void clearCustomSidebar() {
		isCustomSidebarEnabled = false;
		customScoreboardLines = new String[customScoreboardLinesSize];
	}
	
	
	

	public String getGameMoneyName() {
		return "Omegas";
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
		BYPASS_KICK_BAN(OcPermissions.STAFF_BYPASS_PLOT_KICK_AND_BAN),
		GHOST_MODE(OcPermissions.STAFF_BYPASS_VANILLA_COMMANDS),
		WORLDEDIT(OcPermissions.STAFF_BYPASS_WORLDEDIT),
		BUILD_ROADS(OcPermissions.STAFF_BUILD_ROADS),
		OWNER_EVERYWHERE(OcPermissions.STAFF_PLOT_FAKE_OWNER);
		
		OlympaSpigotPermission corePerm;
		
		StaffPerm(OlympaSpigotPermission perm) {
			corePerm = perm;
			
		}
		
		public OlympaSpigotPermission getOlympaPerm() {
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





