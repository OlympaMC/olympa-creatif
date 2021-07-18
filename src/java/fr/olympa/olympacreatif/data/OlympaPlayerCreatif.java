package fr.olympa.olympacreatif.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableMap;

import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.observable.ObservableValue;
import fr.olympa.api.common.permission.OlympaSpigotPermission;
import fr.olympa.api.common.provider.OlympaPlayerObject;
import fr.olympa.api.common.sql.SQLColumn;
import fr.olympa.api.spigot.lines.PlayerObservableLine;
import fr.olympa.api.spigot.scoreboard.sign.Scoreboard;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.perks.KitsManager.KitType;
import fr.olympa.olympacreatif.perks.UpgradesManager.UpgradeType;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotMembers;
import fr.olympa.olympacreatif.plot.PlotPerm.PlotRank;
import fr.olympa.olympacreatif.plot.PlotsManager;

public class OlympaPlayerCreatif extends OlympaPlayerObject /*implements MoneyPlayerInterface*/ {

	private static final SQLColumn<OlympaPlayerCreatif> COLUMN_MONEY = new SQLColumn<OlympaPlayerCreatif>("gameMoney", "INT NOT NULL DEFAULT 100", Types.INTEGER).setUpdatable();

	private static final SQLColumn<OlympaPlayerCreatif> COLUMN_REDSTONE_KIT = new SQLColumn<OlympaPlayerCreatif>("hasRedstoneKit", "TINYINT(1) NOT NULL DEFAULT 0", Types.TINYINT).setUpdatable();
	private static final SQLColumn<OlympaPlayerCreatif> COLUMN_PEACEFUL_KIT = new SQLColumn<OlympaPlayerCreatif>("hasPeacefulMobsKit", "TINYINT(1) NOT NULL DEFAULT 0", Types.TINYINT).setUpdatable();
	private static final SQLColumn<OlympaPlayerCreatif> COLUMN_HOSTILE_KIT = new SQLColumn<OlympaPlayerCreatif>("hasHostileMobsKit", "TINYINT(1) NOT NULL DEFAULT 0", Types.TINYINT).setUpdatable();
	private static final SQLColumn<OlympaPlayerCreatif> COLUMN_FLUID_KIT = new SQLColumn<OlympaPlayerCreatif>("hasFluidKit", "TINYINT(1) NOT NULL DEFAULT 0", Types.TINYINT).setUpdatable();
	private static final SQLColumn<OlympaPlayerCreatif> COLUMN_COMMANDBLOCK_KIT = new SQLColumn<OlympaPlayerCreatif>("hasCommandblockKit", "TINYINT(1) NOT NULL DEFAULT 0", Types.TINYINT).setUpdatable();
	private static final SQLColumn<OlympaPlayerCreatif> COLUMN_ADMIN_KIT = new SQLColumn<OlympaPlayerCreatif>("hasAdminKit", "TINYINT(1) NOT NULL DEFAULT 0", Types.TINYINT).setUpdatable();

	private static final SQLColumn<OlympaPlayerCreatif> COLUMN_UPGRADE_COMMANDBLOCK = new SQLColumn<OlympaPlayerCreatif>(UpgradeType.CB_LEVEL.getBddKey(), "TINYINT(1) NOT NULL DEFAULT 0", Types.TINYINT).setUpdatable();
	private static final SQLColumn<OlympaPlayerCreatif> COLUMN_UPGRADE_BONUSPLOTS = new SQLColumn<OlympaPlayerCreatif>(UpgradeType.BONUS_PLOTS_LEVEL.getBddKey(), "TINYINT(1) NOT NULL DEFAULT 0", Types.TINYINT).setUpdatable();
	private static final SQLColumn<OlympaPlayerCreatif> COLUMN_UPGRADE_BONUSMEMBERS = new SQLColumn<OlympaPlayerCreatif>(UpgradeType.BONUS_MEMBERS_LEVEL.getBddKey(), "TINYINT(1) NOT NULL DEFAULT 0", Types.TINYINT).setUpdatable();

	private static final SQLColumn<OlympaPlayerCreatif> COLUMN_PARAM_DEFAULT_PLOT_CHAT = new SQLColumn<OlympaPlayerCreatif>("playerParamDefaultPlotChat", "TINYINT(1) NOT NULL DEFAULT 0", Types.TINYINT).setUpdatable();
	private static final SQLColumn<OlympaPlayerCreatif> COLUMN_PARAM_MENU_ON_SNEAK = new SQLColumn<OlympaPlayerCreatif>("playerParamOpenMenuOnSneak", "TINYINT(1) NOT NULL DEFAULT 1", Types.TINYINT).setUpdatable();
	private static final SQLColumn<OlympaPlayerCreatif> COLUMN_PARAM_SHOW_INVALID_NBT_ALERT = new SQLColumn<OlympaPlayerCreatif>("playerParamShowInvalidNbtAlert", "TINYINT(1) NOT NULL DEFAULT 1", Types.TINYINT).setUpdatable();

	private static final SQLColumn<OlympaPlayerCreatif> COLUMN_HAS_CLAIMED_VIP = new SQLColumn<OlympaPlayerCreatif>("hasClaimedVipReward", "BOOLEAN NOT NULL DEFAULT FALSE", Types.BOOLEAN).setUpdatable();

	public static final List<SQLColumn<OlympaPlayerCreatif>> COLUMNS = Arrays.asList(COLUMN_MONEY, COLUMN_REDSTONE_KIT, COLUMN_PEACEFUL_KIT,
			COLUMN_HOSTILE_KIT, COLUMN_FLUID_KIT, COLUMN_COMMANDBLOCK_KIT, COLUMN_ADMIN_KIT,
			COLUMN_UPGRADE_COMMANDBLOCK, COLUMN_UPGRADE_BONUSPLOTS, COLUMN_UPGRADE_BONUSMEMBERS,
			COLUMN_PARAM_DEFAULT_PLOT_CHAT, COLUMN_PARAM_MENU_ON_SNEAK, COLUMN_PARAM_SHOW_INVALID_NBT_ALERT,
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
	//private OlympaMoney money = new OlympaMoney(0);

	private Set<KitType> kits = new HashSet<>();
	private Map<UpgradeType, Integer> upgrades = new HashMap<>();
	private Set<PlayerParamType> playerParams = new HashSet<>();

	/*public static final int customScoreboardLinesSize = 8;
	private String[] customScoreboardLines = new String[customScoreboardLinesSize];*/

	private List<StaffPerm> staffPerm = new ArrayList<>();

	private boolean isCustomSidebarEnabled = true; //laisser l'init à true !

	@SuppressWarnings("unchecked")
	private ObservableValue<String>[] customScoreboard = new ObservableValue[10];
	@SuppressWarnings("unchecked")
	private ObservableValue<String>[] sidebarRows = new ObservableValue[6];

	private ObservableValue<Plot> currentPlotObs = new ObservableValue<>(null);
	private ObservableValue<PlotMembers> currentPlotMembersObs = new ObservableValue<>(null);
	//private Plot currentPlot = null;

	public OlympaPlayerCreatif(UUID uuid, String name, String ip) {
		super(uuid, name, ip);
		plugin = OlympaCreatifMain.getInstance();

		for (UpgradeType upg : UpgradeType.values())
			upgrades.put(upg, 0);

		playerParams.addAll(Arrays.asList(PlayerParamType.values()));
	}

	@Override
	public void loadDatas(ResultSet resultSet) throws SQLException {
		//money.set(resultSet.getInt("gameMoney"));

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
	}

	@Override
	public void updateGroups() {
		super.updateGroups();
		if (sidebarRows != null && sidebarRows.length >= 5)
			sidebarRows[4].set("§7Grade : " + getGroupNameColored());
	}

	@Override
	public void loaded() {
		super.loaded();

		if (!hasClaimedVipRewards && getGroups().containsKey(OlympaGroup.VIP))
			plugin.getLogger().severe("§4Player " + getName() + " recieved VIP advantages but none has been defined! Cancelling action.");

		//affiche la sidebar et définit le plot de spawn du joueur
		plugin.getTask().runTaskLater(() -> setPlot(plugin.getPlotsManager().getPlot(((Player) getPlayer()).getLocation())), 2);

		//init des lignes des 2 sidebars
		for (int i = 0; i < customScoreboard.length; i++)
			customScoreboard[i] = new ObservableValue<>("");

		for (int i = 0; i < sidebarRows.length; i++)
			sidebarRows[i] = new ObservableValue<>("");

		sidebarRows[0].set("§1");

		currentPlotMembersObs.observe("members_player_rank_change_" + getName(), () -> {
			sidebarRows[2].set(currentPlotMembersObs.mapOr(members -> "§7Proprio : §e" + members.getOwner().getName(), "§7Proprio : §eaucun"));
			sidebarRows[5].set(currentPlotMembersObs.mapOr(members -> "§7Rang : " + members.getPlayerRank(this).getRankName(), "§7Rang : " + PlotRank.VISITOR.getRankName()));
		});

		currentPlotObs.observe("change_plot_" + getName(), () -> sidebarRows[1].set(currentPlotObs.mapOr(plot -> "§7Parcelle : §e" + plot, "§7Parcelle : §eaucune")));

		sidebarRows[4].set("§7Grade : " + getGroupNameColored());

		//maj auto la liste des membres quand le plot change
		currentPlotObs.observe("change_plot_members_" + getName(), () -> currentPlotMembersObs.set(currentPlotObs.mapOr(Plot::getMembers, null)));
	}

	public void setCustomScoreboardLines(String title, LinkedHashMap<String, Integer> scores) {

		if (!isCustomSidebarEnabled) {
			isCustomSidebarEnabled = true;
			//currentPlotObs.unobserve("change_plot_" + getName());
			//currentPlotMembersObs.unobserve("members_owner_change_" + getName());
			//currentPlotMembersObs.unobserve("members_player_rank_change_" + getName());

			plugin.getScoreboardManager().refresh(this);
			Scoreboard<OlympaPlayerCreatif> sidebar = plugin.getScoreboardManager().getPlayerScoreboard(this);

			plugin.getTask().runTaskLater(() -> {
				for (ObservableValue<String> line : customScoreboard) {
					line.set("");
					sidebar.addLine(new PlayerObservableLine<Scoreboard<OlympaPlayerCreatif>>(scb ->
							line.get().substring(0, Math.min(line.get().length(), 24)), scb -> line));

					setCustomScoreboardLines(title, scores);
				}
			}, 3);
		}

		if (title != null) {
			customScoreboard[0].set(ChatColor.BOLD + title.substring(0, Math.min(title.length(), 24)));
			customScoreboard[customScoreboard.length - 1].set("§7[sidebar plot " + currentPlotObs.get() + "]");
		}

		List<String> keys = new ArrayList<>(scores.keySet());

		if (title != null)
			for (int i = 2; i < Math.min(customScoreboard.length - 2, keys.size()) + 2; i++)
				customScoreboard[i].set(keys.get(i - 2).substring(0, Math.min(keys.get(i - 2).length(), 24))
						+ "§7 : " + scores.get(keys.get(i - 2)));
		else
			for (int i = 0; i < Math.min(customScoreboard.length - 2, keys.size()); i++)
				customScoreboard[i].set(keys.get(i).substring(0, Math.min(keys.get(i).length(), 24)));
	}

	public void reinitSidebar() {
		if (!isCustomSidebarEnabled)
			return;

		isCustomSidebarEnabled = false;

		plugin.getScoreboardManager().refresh(this);

		plugin.getTask().runTaskLater(() -> {
			Scoreboard<OlympaPlayerCreatif> sidebar = plugin.getScoreboardManager().getPlayerScoreboard(this);
			if (sidebar == null)
				return;

			for (ObservableValue<String> line : sidebarRows)
				sidebar.addLine(new PlayerObservableLine<Scoreboard<OlympaPlayerCreatif>>(scb -> line.get(), scb -> line));
		}, 3);
	}

	public void updatePlotMembers() {
		currentPlotMembersObs.set(currentPlotObs.mapOr(Plot::getMembers, null));
	}

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

	public void removeKit(KitType kit) {
		kits.remove(kit);
		kitsColumns.get(kit).updateAsync(this, 0, null, null);
	}

	@Deprecated
	/**
	 * Prefer usage of UpgradeType.getDataOf(OlympaPlayerCreatif)
	 */
	public int getUpgradeLevel(UpgradeType upg) {
		return upgrades.get(upg);
	}

	/**
	 *
	 * @param upg
	 * @param levels may be negative
	 * @return true if change is possible, false if the new upgrade level is out of range
	 */
	public boolean incrementUpgradeLevel(UpgradeType upg, int levels) {
		if (upgrades.get(upg) + levels < 0 || upgrades.get(upg) + levels > upg.getMaxLevel())
			return false;

		upgrades.put(upg, upgrades.get(upg) + levels);

		//changement du cpt des plots du joueur
		if (upg == UpgradeType.CB_LEVEL)
			for (Plot plot : getPlots(true))
				plot.getCbData().setCommandsPerSecond(UpgradeType.CB_LEVEL.getDataOf(upgrades.get(UpgradeType.CB_LEVEL)).value);

		upgradesColumns.get(upg).updateAsync(this, upgrades.get(upg), null, null);

		return true;
	}

	//renvoie la liste des plots où le joueur est membre
	public List<Plot> getPlots(boolean onlyOwnedPlots) {
		List<Plot> list = new ArrayList<>();

		for (Plot plot : plugin.getPlotsManager().getPlots())
			if (plot.getMembers().getPlayerRank(this) != PlotRank.VISITOR)
				if (!onlyOwnedPlots || plot.getMembers().getPlayerRank(getInformation()) == PlotRank.OWNER && onlyOwnedPlots)
					list.add(plot);

		//tri de la liste de plots par ordre croissant d'id
		Collections.sort(list, (p1, p2) -> p1.getId().getId() - p2.getId().getId());

		return list;
	}

	//renvoie le nombre de plot dispo selon le rang
	public int getPlotsSlots(boolean onlyOwnedPlots) {
		if (!onlyOwnedPlots)
			return PlotsManager.maxPlotsPerPlayer;

		int count = UpgradeType.BONUS_PLOTS_LEVEL.getDataOf(this).value;

		if (getGroups().containsKey(OlympaGroup.CREA_CONSTRUCTOR))
			count += 1;

		if (getGroups().containsKey(OlympaGroup.CREA_ARCHITECT))
			count += 2;

		return count;
	}

	//true if player can enter in related plot, false otherwise
	public boolean setPlot(Plot plot) {
		Player player = (Player) getPlayer();
		if (currentPlotObs.get() != null && !currentPlotObs.get().equals(plot))
			currentPlotObs.get().executeExitActions(player);

		if (plot != null && !plot.getPlayers().contains(player))

			if (plot.canEnter(this)) {
				reinitSidebar();
				currentPlotObs.set(plot);
				plot.executeEntryActions(this, player.getLocation());
			} else
				return false;

		else {
			reinitSidebar();
			currentPlotObs.set(plot);
		}

		return true;
	}

	/*public ObservableValue<Plot> getPlot() {
		return currentPlotObs;
	}*/

	public Plot getCurrentPlot() {
		return currentPlotObs.get();
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

	public enum StaffPerm {
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

	public enum PlayerParamType {
		DEFAULT_PLOT_CHAT("playerParamDefaultPlotChat"),
		OPEN_GUI_ON_SNEAK("playerParamOpenMenuOnSneak"),
		SHOW_INVALID_NBT_ALERT("playerParamShowInvalidNbtAlert");

		private String bddKey;

		PlayerParamType(String bddKey) {
			this.bddKey = bddKey;
		}

		public String getBddKey() {
			return bddKey;
		}
	}
}
