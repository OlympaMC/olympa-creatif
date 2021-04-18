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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.CbCommandListener.CbCmdResult;
import fr.olympa.olympacreatif.gui.ShopGui.MarketItemData;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotPerm;
import fr.olympa.olympacreatif.plot.PlotPerm.PlotRank;
import fr.olympa.olympacreatif.plot.PlotStoplagChecker.StopLagDetect;




public class OCmsg {

	/*
	public static final OCmsg CB_INVALID_CMD = new OCmsg(null);
	public static final OCmsg CB_NO_COMMANDS_LEFT = new OCmsg(null);
	public static final OCmsg CB_RESULT_FAILED = new OCmsg(null);
	public static final OCmsg CB_RESULT_SUCCESS = new OCmsg(null);
	
	public static final OCmsg COMMAND_HELP = new OCmsg(null);
	public static final OCmsg INSUFFICIENT_PLOT_PERMISSION = new OCmsg(null);
	public static final OCmsg INVALID_PLOT_ID = new OCmsg(null);
	public static final OCmsg MAX_PLOT_COUNT_OWNER_REACHED = new OCmsg(null);
	public static final OCmsg MAX_PLOT_COUNT_REACHED = new OCmsg(null);
	
	//public static final OCmsg OCO_COMMAND_HELP = new OCmsg(null);
	public static final OCmsg WE_PLOT_EXPORT_FAILED = new OCmsg(null);
	//public static final OCmsg OCO_EXPORT_SUCCESS = new OCmsg(null);
	//public static final OCmsg OCO_HAT_SUCCESS = new OCmsg(null);
	public static final OCmsg OCO_HEAD_GIVED = new OCmsg(null);
	public static final OCmsg OCO_SET_FLY_SPEED = new OCmsg(null);
	public static final OCmsg OCO_UNKNOWN_MB = new OCmsg(null);
	
	public static final OCmsg PLAYER_TARGET_OFFLINE = new OCmsg(null);
	public static final OCmsg PLOT_ACCEPTED_INVITATION = new OCmsg(null);
	public static final OCmsg PLOT_BAN_PLAYER = new OCmsg(null);
	public static final OCmsg PLOT_CANT_BUILD = new OCmsg(null);
	public static final OCmsg PLOT_CANT_ENTER_BANNED = new OCmsg(null);
	public static final OCmsg PLOT_CANT_INTERRACT = new OCmsg(null);
	public static final OCmsg PLOT_CANT_INTERRACT_NULL_PLOT = new OCmsg(null);
	public static final OCmsg PLOT_CANT_PRINT_TNT = new OCmsg(null);
	public static final OCmsg PLOT_CANT_UNBAN_PLAYER = new OCmsg(null);
	//public static final OCmsg PLOT_CANT_WORLDEDIT = new OCmsg(null);
	public static final OCmsg PLOT_DENY_ITEM_DROP = new OCmsg(null);
	public static final OCmsg PLOT_HAVE_BEEN_BANNED = new OCmsg(null);
	public static final OCmsg PLOT_HAVE_BEEN_KICKED = new OCmsg(null);
	public static final OCmsg PLOT_IMPOSSIBLE_TO_BAN_PLAYER = new OCmsg(null);
	public static final OCmsg PLOT_IMPOSSIBLE_TO_KICK_PLAYER = new OCmsg(null);
	public static final OCmsg PLOT_INSUFFICIENT_MEMBERS_SIZE = new OCmsg(null);
	public static final OCmsg PLOT_INVITATION_TARGET_ALREADY_MEMBER = new OCmsg(null);
	public static final OCmsg PLOT_ITEM_PROHIBITED_USED = new OCmsg(null);
	public static final OCmsg PLOT_JOIN_ERR_NOT_ENOUGH_SLOTS = new OCmsg(null);
	public static final OCmsg PLOT_KICK_PLAYER = new OCmsg(null);
	public static final OCmsg PLOT_NEW_CLAIM = new OCmsg(null);
	public static final OCmsg PLOT_NO_PENDING_INVITATION = new OCmsg(null);
	public static final OCmsg PLOT_RECIEVE_INVITATION = new OCmsg(null);
	public static final OCmsg PLOT_SEND_INVITATION = new OCmsg(null);
	public static final OCmsg PLOT_SPAWN_LOC_SET = new OCmsg(null);
	public static final OCmsg PLOT_UNBAN_PLAYER = new OCmsg(null);
	public static final OCmsg PROHIBITED_BLOCK_PLACED = new OCmsg(null);
	public static final OCmsg SHOP_BUY_SUCCESS = new OCmsg(null);
	public static final OCmsg TELEPORT_IN_PROGRESS = new OCmsg(null);
	//public static final OCmsg TELEPORT_PLOT_CENTER = new OCmsg(null);
	public static final OCmsg TELEPORT_TO_RANDOM_PLOT = new OCmsg(null);
	public static final OCmsg TELEPORTED_TO_PLOT_SPAWN = new OCmsg(null);
	public static final OCmsg WE_ERR_INSUFFICIENT_PERMISSION = new OCmsg(null); 
	public static final OCmsg PLOT_PLAYER_JOIN = new OCmsg(null); 
	public static final OCmsg PLOT_STOPLAG_FIRED = new OCmsg(null); 
	public static final OCmsg PLOT_FORCED_STOPLAG_FIRED = new OCmsg(null); 

	//public static final OCmsg INSUFFICIENT_GROUP_PERMISSION = new OCmsg(null); 
	//public static final OCmsg WE_ERR_SELECTION_TOO_BIG = new OCmsg(null); 

	public static final OCmsg PERIODIC_INCOME_RECEIVED = new OCmsg(null); 
	public static final OCmsg TELEPORTED_TO_WORLD_SPAWN = new OCmsg(null); 

	public static final OCmsg WE_START_GENERATING_PLOT_SCHEM = new OCmsg(null);
	public static final OCmsg WE_COMPLETE_GENERATING_PLOT_SCHEM = new OCmsg(null); 
	public static final OCmsg WE_DISABLED = new OCmsg(null); 
	//public static final OCmsg WE_ERR_SCHEM_CMD_DISABLED = new OCmsg(null);
	//public static final OCmsg WE_NO_KIT_FOR_MATERIAL = new OCmsg(null);
	//public static final OCmsg WE_DEACTIVATED_FOR_SAFETY = new OCmsg(null);
	public static final OCmsg WE_PLOT_RESETING = new OCmsg(null);
	
	public static final OCmsg PLOT_UNLOADED = new OCmsg(null);
	public static final OCmsg PLOT_JOIN_ERR_SENDER_OFFLINE = new OCmsg(null);

	public static final OCmsg STAFF_ACTIVATE_COMPONENT = new OCmsg(null); 
	public static final OCmsg STAFF_DEACTIVATE_COMPONENT = new OCmsg(null);
	public static final OCmsg UNKNOWN_MB = new OCmsg(null);
 
	//public static final OCmsg WE_ERR_INSUFFICENT_PERMISSION = new OCmsg(null); */

    public static final OCmsg CB_INVALID_CMD = new OCmsg(null);
    public static final OCmsg CB_NO_COMMANDS_LEFT = new OCmsg(null);
    public static final OCmsg CB_RESULT_FAILED = new OCmsg(null);
    public static final OCmsg CB_RESULT_SUCCESS = new OCmsg(null);
    public static final OCmsg INSUFFICIENT_PLOT_PERMISSION = new OCmsg(null);
    public static final OCmsg INVALID_PLOT_ID = new OCmsg(null);
    public static final OCmsg MAX_PLOT_COUNT_OWNER_REACHED = new OCmsg(null);
    public static final OCmsg MAX_PLOT_COUNT_REACHED = new OCmsg(null);
    public static final OCmsg OCO_HEAD_GIVED = new OCmsg(null);
    public static final OCmsg OCO_SET_FLY_SPEED = new OCmsg(null);
    public static final OCmsg OCO_UNKNOWN_MB = new OCmsg(null);
    public static final OCmsg PERIODIC_INCOME_RECEIVED = new OCmsg(null);
    public static final OCmsg PLAYER_TARGET_OFFLINE = new OCmsg(null);
    public static final OCmsg PLOT_ACCEPTED_INVITATION = new OCmsg(null);
    public static final OCmsg PLOT_BAN_PLAYER = new OCmsg(null);
    public static final OCmsg PLOT_CANT_BUILD = new OCmsg(null);
    public static final OCmsg PLOT_CANT_ENTER_BANNED = new OCmsg(null);
    public static final OCmsg PLOT_CANT_INTERRACT = new OCmsg(null);
    public static final OCmsg PLOT_CANT_INTERRACT_NULL_PLOT = new OCmsg(null);
    public static final OCmsg PLOT_CANT_PRINT_TNT = new OCmsg(null);
    public static final OCmsg PLOT_CANT_UNBAN_PLAYER = new OCmsg(null);
    public static final OCmsg PLOT_DENY_ITEM_DROP = new OCmsg(null);
    public static final OCmsg PLOT_FORCED_STOPLAG_FIRED = new OCmsg(null);
    public static final OCmsg PLOT_HAVE_BEEN_BANNED = new OCmsg(null);
    public static final OCmsg PLOT_HAVE_BEEN_KICKED = new OCmsg(null);
    public static final OCmsg PLOT_IMPOSSIBLE_TO_BAN_PLAYER = new OCmsg(null);
    public static final OCmsg PLOT_IMPOSSIBLE_TO_KICK_PLAYER = new OCmsg(null);
    public static final OCmsg PLOT_INSUFFICIENT_MEMBERS_SIZE = new OCmsg(null);
    public static final OCmsg PLOT_INVITATION_TARGET_ALREADY_MEMBER = new OCmsg(null);
    public static final OCmsg PLOT_ITEM_PROHIBITED_USED = new OCmsg(null);
    public static final OCmsg PLOT_JOIN_ERR_NOT_ENOUGH_SLOTS = new OCmsg(null);
    public static final OCmsg PLOT_JOIN_ERR_SENDER_OFFLINE = new OCmsg(null);
    public static final OCmsg PLOT_KICK_PLAYER = new OCmsg(null);
    public static final OCmsg PLOT_NEW_CLAIM = new OCmsg(null);
    public static final OCmsg PLOT_NO_PENDING_INVITATION = new OCmsg(null);
    public static final OCmsg PLOT_PLAYER_JOIN = new OCmsg(null);
    public static final OCmsg PLOT_RECIEVE_INVITATION = new OCmsg(null);
    public static final OCmsg PLOT_SEND_INVITATION = new OCmsg(null);
    public static final OCmsg PLOT_SPAWN_LOC_SET = new OCmsg(null);
    public static final OCmsg PLOT_STOPLAG_FIRED = new OCmsg(null);
    public static final OCmsg PLOT_UNBAN_PLAYER = new OCmsg(null);
    public static final OCmsg PLOT_UNLOADED = new OCmsg(null);
    public static final OCmsg SHOP_BUY_SUCCESS = new OCmsg(null);
    public static final OCmsg STAFF_ACTIVATE_COMPONENT = new OCmsg(null);
    public static final OCmsg STAFF_DEACTIVATE_COMPONENT = new OCmsg(null);
    public static final OCmsg TELEPORT_IN_PROGRESS = new OCmsg(null);
    public static final OCmsg TELEPORT_TO_RANDOM_PLOT = new OCmsg(null);
    public static final OCmsg TELEPORTED_TO_PLOT_SPAWN = new OCmsg(null);
    public static final OCmsg TELEPORTED_TO_WORLD_SPAWN = new OCmsg(null);
    public static final OCmsg WE_COMPLETE_GENERATING_PLOT_SCHEM = new OCmsg(null);
    public static final OCmsg WE_DISABLED = new OCmsg(null);
    public static final OCmsg WE_ERR_INSUFFICIENT_PERMISSION = new OCmsg(null);
    public static final OCmsg WE_PLOT_EXPORT_FAILED = new OCmsg(null);
    //public static final OCmsg WE_PLOT_RESETING = new OCmsg(null);
    public static final OCmsg WE_START_GENERATING_PLOT_SCHEM = new OCmsg(null);
    
    public static final OCmsg PLOT_LEAVED = new OCmsg(null);
    
	public static final OCmsg NULL_CURRENT_PLOT = new OCmsg(null);

	public static final OCmsg PLOT_PRE_RESET = new OCmsg(null);
	public static final OCmsg PLOT_RESET_START = new OCmsg(null);
	public static final OCmsg PLOT_RESET_END = new OCmsg(null);
	public static final OCmsg PLOT_RESET_ERROR = new OCmsg(null);
	

	public static final OCmsg PLOT_CANT_ENTER_CLOSED = new OCmsg(null);

	public static final OCmsg GIVE_VIP_REWARD = new OCmsg(null);
	public static final OCmsg MONEY_RECIEVED_COMMAND = new OCmsg(null);
	public static final OCmsg MONEY_WITHDRAWED_COMMAND = new OCmsg(null);
    
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
			.put("%plotPermDesc", (pc, perm) -> {return perm.getDesc();})
			.put("%plotPermMinGroup", (pc, perm) -> {return perm.getPerm() == null ? "aucun" : pc == null ? perm.getPerm().getMinGroup().getName() : perm.getPerm().getMinGroup().getName(pc.getGender());})
			.put("%plotPermMinPlotRank", (pc, perm) -> {return perm.getRank().getRankName();})
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
			.put("%playerPlotRank", (pc, plot) -> {return plot.getMembers().getPlayerRank(pc).getRankName();})
			.put("%plotOwnerName", (pc, plot) -> plot.getMembers().getOwner().getName())
			.put("%plotMembersSize", (pc, plot) -> plot.getMembers().getMembers().size() + "")
			.put("%plotMembersMaxSize", (pc, plot) -> plot.getMembers().getMaxMembers() + "")
			.put("%plot", (pc, plot) -> plot.toString())
			
			.build();

	
	private static final Map<String, Function<StopLagDetect, String>> stoplagPlaceHolders = ImmutableMap.<String, Function<StopLagDetect,String>>builder()
			.put("%stopLagType", s -> s.toString())
			.build();

	
	private static final Map<String, Function<String, String>> stringPlaceHolders = ImmutableMap.<String, Function<String,String>>builder()
			.put("%s", s -> s)
			.build();
	
	
	private static final Map<String, Supplier<String>> fixedPlaceHolders = ImmutableMap.<String, Supplier<String>>builder()
			.put("%incomeAsAfk", () -> OCparam.INCOME_AFK.get() + "")
			.put("%incomeAsNotAfk", () -> OCparam.INCOME_NOT_AFK.get() + "")
			.put("%serverIndex", () -> (OlympaCreatifMain.getInstance() == null || OlympaCreatifMain.getInstance().getDataManager() == null) ? "???" : OlympaCreatifMain.getInstance().getDataManager().getServerIndex() + "")
			.build();
	
	
	private String message;
	
	private OCmsg(String s) {
		message = s;
	}
	
	private String getValue(OlympaPlayerCreatif pc, Object... args) {
		if (message == null)
			return "§cMessage manquant, veuillez vérifier les logs.";
		
		//ajoute le plot actuel du joueur si aucun n'a été fourni
		boolean setPlayerPlot = true;
		for (Object o : args)
			if (o instanceof Plot)
				setPlayerPlot = false;
		
		if (setPlayerPlot && pc != null) {
			Object[] args2 = new Object[args.length + 1];
			for (int i = 0 ; i < args.length ; i++)
				args2[i] = args[i];
			
			args2[args.length] = pc.getCurrentPlot();	
			args = args2;
		}
		
		String msg = message;

		for (Entry<String, Supplier<String>> e : fixedPlaceHolders.entrySet())
			msg = msg.replace(e.getKey(), e.getValue().get());
		
		if (pc != null)
			for (Entry<String, Function<OlympaPlayerCreatif, String>> e : playerPlaceHolders.entrySet())
				msg = msg.replace(e.getKey(), e.getValue().apply(pc));	
		

		//remplacement des placeholders
		for (Object o : args)
			if (o instanceof PlotPerm)
				for (Entry<String, BiFunction<OlympaPlayerCreatif, PlotPerm, String>> e : plotPermissionPlaceHolders.entrySet())
					msg = msg.replace(e.getKey(), e.getValue().apply(pc, (PlotPerm) o));
		
			else if (o instanceof OlympaPermission)
				for (Entry<String, BiFunction<OlympaPlayerCreatif, OlympaPermission, String>> e : permissionPlaceHolders.entrySet())
					msg = msg.replace(e.getKey(), e.getValue().apply(pc, (OlympaPermission) o));

			else if (o instanceof CbCmdResult)
				for (Entry<String, Function<CbCmdResult, String>> e : commandblockPlaceHolders.entrySet())
					msg = msg.replace(e.getKey(), e.getValue().apply((CbCmdResult) o));

			else if (o instanceof MarketItemData)
				for (Entry<String, Function<MarketItemData, String>> e : shopPlaceHolders.entrySet())
					msg = msg.replace(e.getKey(), e.getValue().apply((MarketItemData) o));

			else if (o instanceof Plot)
				for (Entry<String, BiFunction<OlympaPlayerCreatif, Plot, String>> e : plotPlaceHolders.entrySet())
					msg = msg.replace(e.getKey(), e.getValue().apply(pc, (Plot) o));

			else if (o instanceof StopLagDetect)
				for (Entry<String, Function<StopLagDetect, String>> e : stoplagPlaceHolders.entrySet())
					msg = msg.replace(e.getKey(), e.getValue().apply((StopLagDetect) o));

			else if (o instanceof String)
				for (Entry<String, Function<String, String>> e : stringPlaceHolders.entrySet())
					msg = msg.replace(e.getKey(), e.getValue().apply((String) o));
		
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
