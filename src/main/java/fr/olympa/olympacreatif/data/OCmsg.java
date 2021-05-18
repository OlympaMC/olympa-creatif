package fr.olympa.olympacreatif.data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fr.olympa.api.afk.AfkHandler;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.CbCommandListener.CbCmdResult;
import fr.olympa.olympacreatif.gui.ShopGui.MarketItemData;
import fr.olympa.olympacreatif.perks.KitsManager.KitType;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotId;
import fr.olympa.olympacreatif.plot.PlotPerm;
import fr.olympa.olympacreatif.plot.PlotPerm.PlotRank;
import fr.olympa.olympacreatif.plot.PlotStoplagChecker.StopLagDetect;
import fr.olympa.olympacreatif.utils.TagsValues.TagParams;




public class OCmsg {
	
	private static final Map<Player, Set<OCmsg>> delayedMessages = new HashMap<Player, Set<OCmsg>>();
	
    public static final OCmsg CB_INVALID_CMD = new OCmsg();
    public static final OCmsg CB_NO_COMMANDS_LEFT = new OCmsg(10);
    public static final OCmsg CB_RESULT_FAILED = new OCmsg();
    public static final OCmsg CB_RESULT_SUCCESS = new OCmsg();
    public static final OCmsg INSUFFICIENT_PLOT_PERMISSION = new OCmsg(5);
    public static final OCmsg INVALID_PLOT_ID = new OCmsg();
    public static final OCmsg MAX_PLOT_COUNT_OWNER_REACHED = new OCmsg();
    public static final OCmsg MAX_PLOT_COUNT_REACHED = new OCmsg();
    public static final OCmsg OCO_HEAD_GIVED = new OCmsg();
    public static final OCmsg OCO_SET_FLY_SPEED = new OCmsg();
    public static final OCmsg OCO_UNKNOWN_MB = new OCmsg();
    public static final OCmsg PERIODIC_INCOME_RECEIVED = new OCmsg();
    public static final OCmsg PLAYER_TARGET_OFFLINE = new OCmsg();
    public static final OCmsg PLOT_ACCEPTED_INVITATION = new OCmsg();
    public static final OCmsg PLOT_BAN_PLAYER = new OCmsg();
    public static final OCmsg PLOT_CANT_BUILD = new OCmsg(10);
    public static final OCmsg PLOT_CANT_ENTER_BANNED = new OCmsg(7);
    public static final OCmsg PLOT_CANT_INTERRACT = new OCmsg(7);
    public static final OCmsg PLOT_CANT_INTERRACT_NULL_PLOT = new OCmsg(10);
    public static final OCmsg PLOT_CANT_PRINT_TNT = new OCmsg();
    public static final OCmsg PLOT_CANT_UNBAN_PLAYER = new OCmsg();
    public static final OCmsg PLOT_DENY_ITEM_DROP = new OCmsg(5);
    public static final OCmsg PLOT_FORCED_STOPLAG_FIRED = new OCmsg();
    public static final OCmsg PLOT_HAVE_BEEN_BANNED = new OCmsg();
    public static final OCmsg PLOT_HAVE_BEEN_KICKED = new OCmsg();
    public static final OCmsg PLOT_IMPOSSIBLE_TO_BAN_PLAYER = new OCmsg();
    public static final OCmsg PLOT_IMPOSSIBLE_TO_KICK_PLAYER = new OCmsg();
    public static final OCmsg PLOT_INSUFFICIENT_MEMBERS_SIZE = new OCmsg();
    public static final OCmsg PLOT_INVITATION_TARGET_ALREADY_MEMBER = new OCmsg();
    public static final OCmsg PLOT_ITEM_PROHIBITED_USED = new OCmsg(2);
    public static final OCmsg PLOT_JOIN_ERR_NOT_ENOUGH_SLOTS = new OCmsg();
    public static final OCmsg PLOT_JOIN_ERR_SENDER_OFFLINE = new OCmsg();
    public static final OCmsg PLOT_KICK_PLAYER = new OCmsg();
    public static final OCmsg PLOT_NEW_CLAIM = new OCmsg();
    public static final OCmsg PLOT_NO_PENDING_INVITATION = new OCmsg();
    public static final OCmsg PLOT_PLAYER_JOIN = new OCmsg();
    public static final OCmsg PLOT_RECIEVE_INVITATION = new OCmsg();
    public static final OCmsg PLOT_SEND_INVITATION = new OCmsg();
    public static final OCmsg PLOT_SPAWN_LOC_SET = new OCmsg();
    public static final OCmsg PLOT_STOPLAG_FIRED = new OCmsg();
    public static final OCmsg PLOT_UNBAN_PLAYER = new OCmsg();
    public static final OCmsg PLOT_UNLOADED = new OCmsg();
    public static final OCmsg SHOP_BUY_SUCCESS = new OCmsg();
    public static final OCmsg STAFF_ACTIVATE_COMPONENT = new OCmsg();
    public static final OCmsg STAFF_DEACTIVATE_COMPONENT = new OCmsg();
    public static final OCmsg TELEPORT_IN_PROGRESS = new OCmsg();
    public static final OCmsg TELEPORT_TO_RANDOM_PLOT = new OCmsg();
    public static final OCmsg TELEPORTED_TO_PLOT_SPAWN = new OCmsg();
    public static final OCmsg TELEPORTED_TO_WORLD_SPAWN = new OCmsg();
    public static final OCmsg WE_COMPLETE_GENERATING_PLOT_SCHEM = new OCmsg();
    public static final OCmsg WE_DISABLED = new OCmsg(2);
    public static final OCmsg WE_ERR_INSUFFICIENT_PERMISSION = new OCmsg();
    public static final OCmsg WE_PLOT_EXPORT_FAILED = new OCmsg();
    public static final OCmsg WE_START_GENERATING_PLOT_SCHEM = new OCmsg();
    
    public static final OCmsg PLOT_LEAVED = new OCmsg();
    
	public static final OCmsg NULL_CURRENT_PLOT = new OCmsg(3);

	public static final OCmsg PLOT_PRE_RESET = new OCmsg();
	public static final OCmsg PLOT_RESET_START = new OCmsg();
	public static final OCmsg PLOT_RESET_END = new OCmsg();
	public static final OCmsg PLOT_RESET_ERROR = new OCmsg();
	
	public static final OCmsg PLOT_CANT_ENTER_CLOSED = new OCmsg(5);

	public static final OCmsg GIVE_VIP_REWARD = new OCmsg();
	public static final OCmsg MONEY_RECIEVED_COMMAND = new OCmsg();
	public static final OCmsg MONEY_WITHDRAWED_COMMAND = new OCmsg();
	public static final OCmsg INSUFFICIENT_KIT_PERMISSION = new OCmsg(2);
	
	public static final OCmsg PLOT_STOPLAG_FIRED_CMD = new OCmsg();
	
	public static final OCmsg PLOT_ENTER_STOPLAG_ACTIVATED = new OCmsg();
	public static final OCmsg COMMANDBLOCK_COMMAND_SET = new OCmsg();
	
	public static final OCmsg PLOT_LOAD_TOO_MUCH_CB_CHUNK = new OCmsg(6);
	//public static final OCmsg PLOT_LOAD_TOO_MUCH_CB_PLOT = new OCmsg(3);
	
	public static final OCmsg CB_SET_TICK_SPEED = new OCmsg();
	public static final OCmsg PLOT_COMMANDBLOCKS_WILL_RELOAD = new OCmsg();
	public static final OCmsg HAT_SET = new OCmsg();

	public static final OCmsg HAS_NOT_UNLOCKED_SUMMON = new OCmsg(5);

	public static final OCmsg WAIT_BEFORE_REEXECUTE_COMMAND = new OCmsg();
	//public static final OCmsg WE_PLOT_RESTAURATION_FAILED = new OCmsg();
	public static final OCmsg WE_NO_PLOT_SCHEM_FOUND = new OCmsg();
	public static final OCmsg WE_FAIL_RESTORING_PLOT = new OCmsg();
	public static final OCmsg WE_COMPLETE_RESTORING_PLOT = new OCmsg();
	public static final OCmsg WE_START_RESTORING_PLOT = new OCmsg();

	public static final OCmsg PLOT_UNKNOWN_HOLO = new OCmsg();
	public static final OCmsg PLOT_TOO_MUCH_HOLOS = new OCmsg();
	public static final OCmsg PLOT_TOO_MUCH_LINES_ON_HOLO = new OCmsg();

	public static final OCmsg WE_TOO_LONG_NBT = new OCmsg(3);
	public static final OCmsg BOOK_TOO_LONG = new OCmsg();

	public static final OCmsg TAG_CHECKER_UNAUTHORIZED_TAG = new OCmsg(1);
	public static final OCmsg TAG_CHECKER_UNAUTHORIZED_VALUE = new OCmsg(1);
	public static final OCmsg WE_TOO_MUCH_TILES = new OCmsg(4);
	public static final OCmsg CI_COMMAND = new OCmsg();
	public static final OCmsg SIGN_UNAUTHORIZED_CHARACTER = new OCmsg(1);
	public static final OCmsg DEFAULT_CHAT_SET_TO = new OCmsg();

	public static final OCmsg ARMORSTAND_EDITOR_SELECT_ARMORSTAND = new OCmsg();
	public static final OCmsg ARMORSTAND_EDITOR_SELECT_ARMORSTAND_TOO_LONG = new OCmsg();
	public static final OCmsg ARMORSTAND_EDITOR_OPEN = new OCmsg();
	public static final OCmsg ARMORSTAND_EDITOR_EXIT = new OCmsg();
	
	public static final OCmsg INSUFFICIENT_GROUP_PERMISSION = new OCmsg();
	
	
	
	static {
		Bukkit.getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler(priority = EventPriority.LOW)
			public void onJoin(PlayerJoinEvent e) {
				delayedMessages.put(e.getPlayer(), new HashSet<OCmsg>());
			}
			@EventHandler
			public void onQuit(PlayerQuitEvent e) {
				delayedMessages.remove(e.getPlayer());
			}
		}, OlympaCreatifMain.getInstance());
	}
	
	private static final Map<String, Function<OlympaPlayerCreatif, String>> playerPlaceHolders = ImmutableMap.<String, Function<OlympaPlayerCreatif,String>>builder()
			.put("%playerName", pc -> {return pc.getName();})
			//.put("%plotOwnerName", pc -> {return pc.getCurrentPlot() == null ? "aucun" : pc.getCurrentPlot().getMembers().getOwner().getName();})
			
			.put("%playerGroup", pc -> {return pc.getGroupName();})

			.put("%playerMoneyAndSymbol", pc -> {return pc.getGameMoney().getFormatted();})
			.put("%playerMoney", pc -> {return pc.getGameMoney().get() + "";})
			
			.put("%playerPlotsCount", pc -> {return pc.getPlots(false).size() + "";})
			.put("%playerPlotsMaxCount", pc -> {return pc.getPlotsSlots(false) + "";})
			.put("%playerOwnedPlotsCount", pc -> {return pc.getPlots(true).size() + "";})
			.put("%playerOwnedPlotsMaxCount", pc -> {return pc.getPlotsSlots(true) + "";})
			.build();

	@SuppressWarnings("deprecation")
	private static final Map<String, BiFunction<OlympaPlayerCreatif, OlympaPermission, String>> permissionPlaceHolders = ImmutableMap.<String, BiFunction<OlympaPlayerCreatif, OlympaPermission,String>>builder()
			.put("%permMinGroup", (pc, perm) -> {return pc == null ? perm.getMinGroup().getName() : perm.getMinGroup().getName(pc.getGender());})
			.put("%permName", (pc, perm) -> {return perm.getName();})
			.build();


	@SuppressWarnings("deprecation")
	private static final Map<String, BiFunction<OlympaPlayerCreatif, PlotPerm, String>> plotPermissionPlaceHolders = ImmutableMap.<String, BiFunction<OlympaPlayerCreatif, PlotPerm,String>>builder()
			.put("%plotPermDesc", (pc, perm) -> {return perm.getDesc() == null ? "" : perm.getDesc();})
			.put("%plotPermMinGroup", (pc, perm) -> {return perm.getPerm() == null ? "§7aucun" : pc == null ? perm.getPerm().getMinGroup().getName() : perm.getPerm().getMinGroup().getName(pc.getGender());})
			.put("%plotPermMinPlotRank", (pc, perm) -> {return perm.getRank().getRankName();})
			.build();

	private static final Map<String, Function<PlotRank, String>> plotRankPlaceHolders = ImmutableMap.<String, Function<PlotRank,String>>builder()
			.put("%plotPermMinPlotRank", rank -> {return rank.getRankName();})
			.build();

	
	private static final Map<String, Function<CbCmdResult, String>> commandblockPlaceHolders = ImmutableMap.<String, Function<CbCmdResult,String>>builder()
			.put("%cbCmdName", (cmd) -> {return cmd.getCmd().toString().toLowerCase();})
			.put("%cbCmdResult", (cmd) -> {return cmd.getResult() + "";})
			.build();


	private static final Map<String, Function<MarketItemData, String>> shopPlaceHolders = ImmutableMap.<String, Function<MarketItemData,String>>builder()
			.put("%shopItemName", item -> {return item.getHolder().getItemMeta() == null ? item.getItem().toString() : item.getHolder().getItemMeta().getDisplayName();})
			.put("%shopItemPrice", item -> {return item.getPrice() + "";})
			.build();

	
	private static final Map<String, BiFunction<OlympaPlayerCreatif, Plot, String>> plotPlaceHolders = ImmutableMap.<String, BiFunction<OlympaPlayerCreatif, Plot,String>>builder()
			//.put("%playerPlotRank", (pc, plot) -> {return pc == null ? PlotRank.VISITOR.getRankName() : plot.getMembers().getPlayerRank(pc).getRankName();})
			//.put("%playerPlot", (pc, plot) -> {return plot.getPlotId().getId() + "";})
			//.put("%plotId", (pc, plot) -> {return plot.getPlotId().getId() + "";})
			.put("%playerPlotRank", (pc, plot) -> plot != null ? plot.getMembers().getPlayerRank(pc).getRankName() : PlotRank.VISITOR.getRankName())
			.put("%plotOwnerName", (pc, plot) -> plot != null ? plot.getMembers().getOwner().getName() : "none")
			.put("%plotMembersSize", (pc, plot) -> plot != null ? plot.getMembers().getMembers().size() + "" : "0")
			.put("%plotMembersMaxSize", (pc, plot) -> plot != null ? plot.getMembers().getMaxMembers() + "" : "0")
			.put("%plot", (pc, plot) -> plot != null ? plot.toString() : "none")
			
			.build();
	
	private static final Map<String, Function<PlotId, String>> plotIdPlaceHolders = ImmutableMap.<String, Function<PlotId,String>>builder()
			.put("%plot", plot -> plot != null ? plot.toString() : "none")
			.build();
	
	private static final Map<String, Function<StopLagDetect, String>> stoplagPlaceHolders = ImmutableMap.<String, Function<StopLagDetect,String>>builder()
			.put("%stopLagType", s -> s.toString())
			.build();

	
	private static final Map<String, Function<KitType, String>> kitPlaceHolders = ImmutableMap.<String, Function<KitType,String>>builder()
			.put("%kitName", kit -> kit.getName())
			.build();

	
	private static final Map<String, Function<String, String>> stringPlaceHolders = ImmutableMap.<String, Function<String,String>>builder()
			.put("%s", s -> s)
			.build();

	
	private static final Map<String, Supplier<String>> fixedPlaceHolders = ImmutableMap.<String, Supplier<String>>builder()
			.put("%incomeAsAfk", () -> OCparam.INCOME_AFK.get() + "")
			.put("%incomeAsNotAfk", () -> OCparam.INCOME_NOT_AFK.get() + "")
			.put("%serverIndex", () -> (OlympaCreatifMain.getInstance() == null || OlympaCreatifMain.getInstance().getDataManager() == null) ? "???" : OlympaCreatifMain.getInstance().getDataManager().getServerIndex() + "")
			.build();
	
	
	private static final Map<String, Function<TagParams, String>> nbtTagPlaceHolders = ImmutableMap.<String, Function<TagParams, String>>builder()
			.put("%tagId", param -> param.getName())
			.put("%tagMin", param -> param.getMin() + "")
			.put("%tagMax", param -> param.getMax() + "")
			.build();
	
	
	private String message = null;
	private int delay;
	
	public OCmsg() {
		this(0);
	}
	
	public OCmsg(int delay) {
		this.delay = delay;
	}
	
	private String getValue(OlympaPlayerCreatif pc, Object... args) {
		if (message == null)
			return "§cMessage manquant, veuillez alerter un membre du staff.";
		
		//ajoute le plot actuel du joueur si aucun n'a été fourni
		/*boolean setPlayerPlot = true;
		for (Object o : args)
			if (o instanceof Plot)
				setPlayerPlot = false;
		
		if (setPlayerPlot && pc != null) {
			Object[] args2 = new Object[args.length + 1];
			for (int i = 0 ; i < args.length ; i++)
				args2[i] = args[i];
			
			args2[args.length] = pc.getCurrentPlot();	
			args = args2;
		}*/
		
		String msg = message;

		for (Entry<String, Supplier<String>> e : fixedPlaceHolders.entrySet())
			msg = msg.replace(e.getKey(), e.getValue().get());
		
		if (pc != null)
			for (Entry<String, Function<OlympaPlayerCreatif, String>> e : playerPlaceHolders.entrySet())
				msg = msg.replace(e.getKey(), e.getValue().apply(pc));	
		
		boolean plotAlreadyAdded = false;
		
		//remplacement des placeholders
		for (Object o : args)
			if (o instanceof PlotPerm && pc != null)
				for (Entry<String, BiFunction<OlympaPlayerCreatif, PlotPerm, String>> e : plotPermissionPlaceHolders.entrySet())
					msg = msg.replace(e.getKey(), e.getValue().apply(pc, (PlotPerm) o));
		
			else if (o instanceof OlympaPermission && pc != null)
				for (Entry<String, BiFunction<OlympaPlayerCreatif, OlympaPermission, String>> e : permissionPlaceHolders.entrySet())
					msg = msg.replace(e.getKey(), e.getValue().apply(pc, (OlympaPermission) o));

			else if (o instanceof CbCmdResult)
				for (Entry<String, Function<CbCmdResult, String>> e : commandblockPlaceHolders.entrySet())
					msg = msg.replace(e.getKey(), e.getValue().apply((CbCmdResult) o));

			else if (o instanceof PlotRank)
				for (Entry<String, Function<PlotRank, String>> e : plotRankPlaceHolders.entrySet())
					msg = msg.replace(e.getKey(), e.getValue().apply((PlotRank) o));

			else if (o instanceof MarketItemData)
				for (Entry<String, Function<MarketItemData, String>> e : shopPlaceHolders.entrySet())
					msg = msg.replace(e.getKey(), e.getValue().apply((MarketItemData) o));

			else if (o instanceof PlotId)
				for (Entry<String, Function<PlotId, String>> e : plotIdPlaceHolders.entrySet())
					msg = msg.replace(e.getKey(), e.getValue().apply((PlotId) o));

			else if (o instanceof Plot && pc != null) {
				plotAlreadyAdded = true;
				for (Entry<String, BiFunction<OlympaPlayerCreatif, Plot, String>> e : plotPlaceHolders.entrySet())
					msg = msg.replace(e.getKey(), e.getValue().apply(pc, (Plot) o));	
			}

			else if (o instanceof StopLagDetect)
				for (Entry<String, Function<StopLagDetect, String>> e : stoplagPlaceHolders.entrySet())
					msg = msg.replace(e.getKey(), e.getValue().apply((StopLagDetect) o));

			else if (o instanceof String)
				for (Entry<String, Function<String, String>> e : stringPlaceHolders.entrySet())
					msg = msg.replace(e.getKey(), e.getValue().apply((String) o));

			else if (o instanceof KitType)
				for (Entry<String, Function<KitType, String>> e : kitPlaceHolders.entrySet())
					msg = msg.replace(e.getKey(), e.getValue().apply((KitType) o));

			else if (o instanceof TagParams)
				for (Entry<String, Function<TagParams, String>> e : nbtTagPlaceHolders.entrySet())
					msg = msg.replace(e.getKey(), e.getValue().apply((TagParams) o));
		
		
		
		if (!plotAlreadyAdded && pc != null)
			for (Entry<String, BiFunction<OlympaPlayerCreatif, Plot, String>> e : plotPlaceHolders.entrySet())
				msg = msg.replace(e.getKey(), e.getValue().apply(pc, pc.getCurrentPlot()));
		
		return msg;
	}
	
	public void setValue(String s) {
		message = StringEscapeUtils.unescapeJava(s);
	}
	
	public OCmsg valueOf(String s) {
		for (Entry<String, OCmsg> entry : values().entrySet())
			if (entry.getKey().equals(s))
				return entry.getValue();
		
		return null;
	}
	
	@Deprecated
	@Override
	public String toString() {
		return message;
	}
	
	@Deprecated
	public void send(CommandSender sender, Object... objs) {
		sender.sendMessage(getValue(null, objs));
	}
	
	public void send(Player p, Object... objs) {
		send((OlympaPlayerCreatif) AccountProvider.get(p.getUniqueId()), objs);
	}
	
	public void send(OlympaPlayerCreatif pc, Object... objs) {
		if (delayedMessages.get(pc.getPlayer()).contains(this) || pc.getPlayer() == null)
			return;
		
		if (delay > 0 && delayedMessages.containsKey(pc.getPlayer())) {
			delayedMessages.get(pc.getPlayer()).add(this);
			final OCmsg msg = this;
			
			OlympaCreatifMain.getInstance()
			.getTask()
			.runTaskLater(() -> {
				if (delayedMessages.containsKey(pc.getPlayer())) 
				delayedMessages.get(pc
						.getPlayer())
						.remove(msg);
				}, 20 * delay);
		}
		
		pc.getPlayer().sendMessage(getValue(pc, objs));
	}
	
	/**
	 * Return all public static fields of the class
	 * @return
	 */
	public static Map<String, OCmsg> values() {
		Map<String, OCmsg> map = new HashMap<String, OCmsg>();
		
		Field[] fields = OCmsg.class.getDeclaredFields();
		for (Field field : fields)
			if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()))
				try {
					field.setAccessible(true);
					map.put(field.getName(), (OCmsg) field.get(null));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
		
		return map;
	}
}
