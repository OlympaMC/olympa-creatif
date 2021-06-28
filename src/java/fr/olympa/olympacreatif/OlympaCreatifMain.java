package fr.olympa.olympacreatif;

import java.util.Random;

import org.bukkit.entity.Player;

import fr.olympa.api.spigot.command.essentials.BackCommand;
import fr.olympa.api.spigot.command.essentials.tp.TpaHandler;
import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.observable.ObservableValue;
import fr.olympa.api.spigot.holograms.Hologram;
import fr.olympa.api.spigot.lines.CyclingLine;
import fr.olympa.api.spigot.lines.DynamicLine;
import fr.olympa.api.spigot.lines.FixedLine;
import fr.olympa.api.spigot.lines.PlayerObservableLine;
import fr.olympa.api.spigot.lines.TimerLine;
import fr.olympa.api.common.permission.OlympaPermission;
import fr.olympa.api.common.permission.list.OlympaAPIPermissionsSpigot;
import fr.olympa.api.common.plugin.OlympaAPIPlugin;
import fr.olympa.api.common.provider.AccountProvider;
import fr.olympa.api.common.report.ReportReason;
import fr.olympa.api.spigot.scoreboard.sign.Scoreboard;
import fr.olympa.api.spigot.scoreboard.sign.ScoreboardManager;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.olympacreatif.commandblocks.CommandBlocksManager;
import fr.olympa.olympacreatif.commands.CiCommand;
import fr.olympa.olympacreatif.commands.CmdsLogic;
import fr.olympa.olympacreatif.commands.HatCommand;
import fr.olympa.olympacreatif.commands.MenuCommand;
import fr.olympa.olympacreatif.commands.MicroblockCommand;
import fr.olympa.olympacreatif.commands.OcCmd;
import fr.olympa.olympacreatif.commands.OcaCmd;
import fr.olympa.olympacreatif.commands.OcoCmd;
import fr.olympa.olympacreatif.commands.ShopCommand;
import fr.olympa.olympacreatif.commands.SkullCommand;
import fr.olympa.olympacreatif.commands.SpawnCommand;
import fr.olympa.olympacreatif.commands.SpeedCommand;
import fr.olympa.olympacreatif.commands.StoplagCommand;
import fr.olympa.olympacreatif.commands.TpfCommand;
import fr.olympa.olympacreatif.data.DataManager;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OCparam;
import fr.olympa.olympacreatif.data.OcPermissions;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.PermissionsManager;
import fr.olympa.olympacreatif.data.ReportReasonsList;
import fr.olympa.olympacreatif.perks.PerksManager;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotPerm;
import fr.olympa.olympacreatif.plot.PlotPerm.PlotRank;
import fr.olympa.olympacreatif.plot.PlotsManager;
import fr.olympa.olympacreatif.world.WorldManager;
import fr.olympa.olympacreatif.worldedit.AWorldEditManager;
import fr.olympa.olympacreatif.worldedit.OcFastAsyncWorldEdit;
import fr.olympa.olympacreatif.worldedit.OcWorldEdit;

public class OlympaCreatifMain extends OlympaAPIPlugin {

	private WorldManager worldManager;
	private DataManager dataManager;
	private PlotsManager plotsManager;
	private PerksManager perksManager;
	private CommandBlocksManager cbManager;

	private CmdsLogic cmdLogic;

	private PermissionsManager permsManager;
	private AWorldEditManager weManager = null;

	//private LuckPerms luckperms;

	/*private WorldEditPlugin we = null;
	private IAsyncWorldEdit awe = null;*/

	private static OlympaCreatifMain plugin;

	private ScoreboardManager<OlympaPlayerCreatif> scm;

	public Random random = new Random();

	/*
	@Override //defines the custom world generator
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
		return new CustomChunkGenerator(this);
	}*/

	public static OlympaCreatifMain getInstance() {
		return plugin;
	}

	@Override
	public void onEnable() {
		super.onEnable();

		plugin = this;
		OlympaCore.getInstance().setOlympaServer(OlympaServer.CREATIF);

		AccountProvider.getter().setPlayerProvider(OlympaPlayerCreatif.class, OlympaPlayerCreatif::new, "creatif", OlympaPlayerCreatif.COLUMNS);

		OlympaPermission.registerPermissions(OcPermissions.class);
		ReportReason.registerReason(ReportReasonsList.class);

		//new BackCommand(plugin, null);
		
		new OcCmd(this).register();
		new OcoCmd(this).register();
		new OcaCmd(this).register();

		new MenuCommand(this).register();
		new MicroblockCommand(this).register();
		new SkullCommand(this).register();
		new SpeedCommand(this).register();
		new SpawnCommand(this).register();
		new ShopCommand(this).register();
		new StoplagCommand(this).register();
		new TpfCommand(this).register();
		new CiCommand(this).register();
		
		new BackCommand(this, null).register();
		new HatCommand(this).register();
		
		//set restrictions to /gm command
		OlympaAPIPermissionsSpigot.GAMEMODE_COMMAND.setMinGroup(OlympaGroup.PLAYER);
		OlympaAPIPermissionsSpigot.GAMEMODE_COMMAND_CREATIVE.setMinGroup(OlympaGroup.PLAYER);
		
		OlympaCore.getInstance().gamemodeCommand.setCanExecuteFunction((sender, target) -> {
			
			if (!(sender instanceof Player))
				return false;
			
			OlympaPlayerCreatif pc = AccountProvider.getter().get(((Player)sender).getUniqueId());
			
			if (pc == null)
				return false;
			else if (OcPermissions.STAFF_OCA_CMD.hasPermission(pc))
				return true;
			
			Plot plot = getPlotsManager().getPlot(pc.getPlayer().getLocation());
			
			if (plot == null || !PlotPerm.CHANGE_GAMEMODE.has(plot, pc) || sender != target)
				return false;
			else
				return true;
		});
		
		getServer().getPluginManager().registerEvents(new TpaHandler(this, OcPermissions.CREA_TPA_COMMAND, 0), plugin);
		
		dataManager = new DataManager(this);
		worldManager = new WorldManager(this);
		perksManager = new PerksManager(this);
		plotsManager = new PlotsManager(this);
		cbManager = new CommandBlocksManager(this);
		permsManager = new PermissionsManager(this);
		
		createScoreboard2(dataManager.getServerIndex());

		cmdLogic = new CmdsLogic(this);

		if (getServer().getPluginManager().getPlugin("FastAsyncWorldEdit") != null)
			weManager = new OcFastAsyncWorldEdit(this);
		else if (getServer().getPluginManager().getPlugin("WorldEdit") != null && getServer().getPluginManager().getPlugin("AsyncWorldEdit") != null)
			weManager = new OcWorldEdit(this);
		
		//gestion particulière des hologrammes
		OlympaAPIPermissionsSpigot.COMMAND_HOLOGRAMS_MANAGE.setMinGroup(OcPermissions.USE_HOLOGRAMS.getMinGroup());
		OlympaCore.getInstance().getHologramsManager().setTempHoloCreationMode(true);
		OlympaCore.getInstance().getHologramsManager().setHoloControlSupplier((sender, holo, action) -> {
			if (!(sender instanceof Player))
				return true;
			
			OlympaPlayerCreatif pc = AccountProvider.getter().get(((Player)sender).getUniqueId());
			
			switch(action) {
			case COMMAND:
				return pc.getCurrentPlot() == null ? false : pc.getCurrentPlot().getCbData().containsHolo(holo);

				
			case CREATE_PREPROCESS:
				if (!canEditHoloWithMsg(pc, holo, true))
					return false;
				return true;
				
			case CREATED:
				if (!canEditHoloWithMsg(pc, holo, true))
					return false;
				pc.getCurrentPlot().getCbData().addHolo(holo);
				return true;
				
			case EDIT_ADDLINE:
				if (!canEditHoloWithMsg(pc, holo, false))
					return false;
				if (holo.getLines().size() >= OCparam.MAX_LINES_PER_HOLO.get()) {
					OCmsg.PLOT_TOO_MUCH_LINES_ON_HOLO.send(pc, OCparam.MAX_LINES_PER_HOLO.get() + "");
					return false;
				}
				return true;
				
			case EDIT_OTHER:
				if (!canEditHoloWithMsg(pc, holo, false))
					return false;
				return true;
				
			case MOVE:
				if (!canEditHoloWithMsg(pc, holo, false))
					return false;
				return true;
				
			case REMOVE:
				if (!canEditHoloWithMsg(pc, holo, false))
					return false;
				pc.getCurrentPlot().getCbData().removeHolo(holo);
				return true;
				
			case TELEPORT:
				if (!canEditHoloWithMsg(pc, holo, false))
					return false;
				return true;
				
			case VISIBILITY:
				return false;
				
			default:
				getLogger().warning("§cAn unexpected action on hologram " + holo.getID() + " has been detected : " + action);
				return false;
			
			}
			
		});
	}
	
	private boolean canEditHoloWithMsg(OlympaPlayerCreatif pc, Hologram holo, boolean isForHoloCreation) {
		if (pc.getCurrentPlot() == null) {
			OCmsg.NULL_CURRENT_PLOT.send(pc);
			return false;
			
		}else if (!PlotPerm.MANAGE_HOLOS.has(pc.getCurrentPlot(), pc)) {
			OCmsg.INSUFFICIENT_PLOT_PERMISSION.send(pc, PlotPerm.MANAGE_HOLOS);
			return false;
			
		}else if (!isForHoloCreation && !pc.getCurrentPlot().getCbData().containsHolo(holo)) {
			OCmsg.PLOT_UNKNOWN_HOLO.send(pc, holo.getID() + "");
			return false;
			
		}else if (isForHoloCreation && pc.getCurrentPlot().getCbData().getHolos().size() >= OCparam.MAX_HOLOS_PER_PLOT.get()) {
			OCmsg.PLOT_TOO_MUCH_HOLOS.send(pc, OCparam.MAX_HOLOS_PER_PLOT.get() + "");
			return false;
			
		}else 
			return true;
	}

	@Override
	public void onDisable() {
		super.onDisable();

		//save plots
		if (getPlotsManager() != null)
			for (Plot plot : getPlotsManager().getPlots()) {
				plot.unload();
				getDataManager().savePlot(plot, true);
			}
		if (scm != null)
			scm.unload();
	}

	//crée le scoreboard du joueur avec des lignes dynamiques, pour afficher le scoreboard custom du plot si besoin
	/*@SuppressWarnings("unchecked")
	private void createScoreboard(int serverIndex) {
		scm = new ScoreboardManager<>(plugin, "§6Olympa Créatif " + getAsRomanNumber(serverIndex));

		//initialisation lignes scoreboard
		for (int i = 0; i < OlympaPlayerCreatif.customScoreboardLinesSize; i++) {
			final int line = i;

			scm.addLines(
					new TimerLine<Scoreboard<OlympaPlayerCreatif>>(p -> {
						return getLine(p.getOlympaPlayer(), line);
					}, plugin, 20));
			
			//new DynamicLine<Scoreboard<OlympaPlayerCreatif>>(p -> p.getName(), null);
		}
		scm.addFooters(FixedLine.EMPTY_LINE, CyclingLine.olympaAnimation());
	}*/
	
	@SuppressWarnings("unchecked")
	private void createScoreboard2(int serverIndex) {
		scm = new ScoreboardManager<>(plugin, "§6Olympa Créatif " + getAsRomanNumber(serverIndex));
		
		//initialisation lignes scoreboard
		/*for (int i = 0; i < OlympaPlayerCreatif.maxSidebarRows; i++) {
			final int line = i;
			scm.addLines(new PlayerObservableLine<Scoreboard<OlympaPlayerCreatif>>(
					holder -> holder.getOlympaPlayer().getSidebarRow(line).get(), 
					holder -> holder.getOlympaPlayer().getSidebarRow(line)));
		}*/
		scm.addFooters(FixedLine.EMPTY_LINE, CyclingLine.olympaAnimation());
	}
	
	
/*
	public String getLine(OlympaPlayerCreatif p, int i) {

		//Bukkit.broadcastMessage(message)

		if (p.getCustomScoreboardLines() != null)
			return p.getCustomScoreboardLines()[i];

		Plot plot = p.getCurrentPlot();
		switch (i) {
		case 0:
			return "§1";

		case 1:
			if (plot == null)
				return "§7Parcelle : §eaucune";
			else
				return "§7Parcelle : §e" + plot;

		case 2:
			if (plot == null)
				return "§7Proprio : §eaucun";
			else
				return "§7Proprio : §e" + plot
						.getMembers()
						.getOwner()
						.getName();

		case 3:
			return "§2";

		case 4:
			return "§7Grade : " + p.getGroupNameColored();

		case 5:
			if (plot == null)
				return "§7Visiteurs : 0";
			else
				return "§7Visiteurs : " + plot.getPlayers().size();

		case 6:
			return "§3";

		case 7:
			if (plot == null)
				return "§7Rang : " + PlotRank.VISITOR.getRankName();
			else
				return "§7Rang : " + plot.getMembers().getPlayerRank(p).getRankName();

		case 8:
			return "§4";
		}
		return "";
	}*/

	private String getAsRomanNumber(int i) {
		switch (i) {
		case 1:
			return "I";
		case 2:
			return "II";
		case 3:
			return "III";
		case 4:
			return "IV";
		default:
			return "" + i;
		}
	}

	public WorldManager getWorldManager() {
		return worldManager;
	}

	public AWorldEditManager getWEManager() {
		return weManager;
	}

	public PlotsManager getPlotsManager() {
		return plotsManager;
	}

	public DataManager getDataManager() {
		return dataManager;
	}

	public PerksManager getPerksManager() {
		return perksManager;
	}

	public CommandBlocksManager getCommandBlocksManager() {
		return cbManager;
	}

	public CmdsLogic getCmdLogic() {
		return cmdLogic;
	}

	public PermissionsManager getPermissionsManager() {
		return permsManager;
	}
	
	public ScoreboardManager<OlympaPlayerCreatif> getScoreboardManager() {
		return scm;
	}
}
