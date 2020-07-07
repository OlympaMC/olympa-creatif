package fr.olympa.olympacreatif;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.api.lines.AnimLine;
import fr.olympa.api.lines.DynamicLine;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.api.lines.LinesHolder;
import fr.olympa.api.lines.TimerLine;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.player.OlympaPlayerProvider;
import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.scoreboard.sign.Scoreboard;
import fr.olympa.api.scoreboard.sign.ScoreboardManager;
import fr.olympa.olympacreatif.command.OcCommand;
import fr.olympa.olympacreatif.command.OcaCommand;
import fr.olympa.olympacreatif.command.OceCommand;
import fr.olympa.olympacreatif.command.OcoCommand;
import fr.olympa.olympacreatif.commandblocks.CommandBlocksManager;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand.CommandType;
import fr.olympa.olympacreatif.data.DataManager;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.perks.PerksManager;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotId;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;
import fr.olympa.olympacreatif.plot.PlotsManager;
import fr.olympa.olympacreatif.world.CustomChunkGenerator;
import fr.olympa.olympacreatif.world.WorldManager;
import fr.olympa.olympacreatif.worldedit.WorldEditManager;

public class OlympaCreatifMain extends OlympaAPIPlugin {
	
	private WorldManager creativeWorldManager;
	private WorldEditManager worldEditManager;
	private DataManager dataManager;
	private PlotsManager plotsManager;
	private PerksManager perksManager;
	private CommandBlocksManager cbManager;
	
	private static OlympaCreatifMain plugin;
	
	private ScoreboardManager<OlympaPlayerCreatif> scm;
	
	public Random random = new Random();
	
	@Override //retourne le générateur de chunks custom
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
		return new CustomChunkGenerator(this);
	}
	
	public static OlympaCreatifMain getMainClass() {
		return plugin;
	}
	
	//private OlympaStatement statement = new OlympaStatement("SELECT * FROM xxx WHERE xx = ?");
	public void onEnable() {
		super.onEnable();
		
		plugin = this;
		AccountProvider.setPlayerProvider(OlympaPlayerCreatif.class, OlympaPlayerCreatif::new, "creatif", OlympaPlayerCreatif.COLUMNS);
		
		OlympaPermission.registerPermissions(PermissionsList.class);
		
		createScoreboard();
		
		//saveDefaultConfig();
		new OcCommand(this, "olympacreatif", new String[] {"oc"}).register();
		new OceCommand(this, "olympacreatifedit", new String[] {"oce"}).register();
		new OcoCommand(this, "olympacreatifother", new String[] {"oco"}).register();
		new OcaCommand(this, "olympacreatifadmin", new String[] {"oca"}).register();
		
		dataManager = new DataManager(this);
		plotsManager = new PlotsManager(this);
		creativeWorldManager = new WorldManager(this);
		worldEditManager = new WorldEditManager(this);
		perksManager  = new PerksManager(this);
		cbManager = new CommandBlocksManager(this);

		
		/*try {
			//OlympaCore.getInstance().getDatabase();
			PreparedStatement preparedStatement = statement.getStatement();
			preparedStatement.setString(1, "xxxxx");
			ResultSet resultSet = preparedStatement.executeQuery();
			resultSet.close();
		}catch (SQLException e) {
			e.printStackTrace();
		}*/
	}
	
	@Override
	public void onDisable() {
		super.onDisable();
		
		scm.unload();
	}
	
	//crée le scoreboard du joueur avec des lignes dynamiques, pour afficher le scoreboard custom du plot si besoin
	@SuppressWarnings("unchecked")
	private void createScoreboard() {
		scm = new ScoreboardManager<OlympaPlayerCreatif>(plugin, "§6Olympa Créatif");
/*
		scm.addLines(new TimerLine<OlympaPlayerCreatif>( p -> {
						if (p.getCustomScoreboardLines().get(0) != null)
							return p.getCustomScoreboardLines().get(0);
						else
							return "super test";
					}, plugin, 20));*/
		
		
		//initialisation lignes de base scoreboard
		scm.addLines(
				
				new TimerLine<Scoreboard<OlympaPlayerCreatif>>( p -> {
					if (p.getOlympaPlayer().getCustomScoreboardLines().get(0) != null)
						return p.getOlympaPlayer().getCustomScoreboardLines().get(0);
					else
						return "§1";
				}, plugin, 20),

				new TimerLine<Scoreboard<OlympaPlayerCreatif>>( p -> {			
					if (p.getOlympaPlayer().getCustomScoreboardLines().get(1) != null)
						return p.getOlympaPlayer().getCustomScoreboardLines().get(1);
					else {
						PlotId plotId = plugin.getPlotsManager().getPlotId(p.getOlympaPlayer().getPlayer().getLocation());
						if (plotId == null)
							return "§7Parcelle : §eroute";
						else
							return "§7Parcelle : §e" + plotId.getAsString();
					}
				}, plugin, 20),

				new TimerLine<Scoreboard<OlympaPlayerCreatif>>( p -> {			
					if (p.getOlympaPlayer().getCustomScoreboardLines().get(2) != null)
						return p.getOlympaPlayer().getCustomScoreboardLines().get(2);
					else {
						Plot plot = plugin.getPlotsManager().getPlot(p.getOlympaPlayer().getPlayer().getLocation());
						if (plot == null)
							return "§7Proprio : §eaucun";
						else
							return "§7Proprio : §e" + plot.getMembers().getOwner().getName();
					}
				}, plugin, 20),

				new TimerLine<Scoreboard<OlympaPlayerCreatif>>( p -> {			
					if (p.getOlympaPlayer().getCustomScoreboardLines().get(3) != null)
						return p.getOlympaPlayer().getCustomScoreboardLines().get(3);
					else
						return "§2";
				}, plugin, 20),
				
				new TimerLine<Scoreboard<OlympaPlayerCreatif>>( p -> {				
					if (p.getOlympaPlayer().getCustomScoreboardLines().get(4) != null)
						return p.getOlympaPlayer().getCustomScoreboardLines().get(4);
					else
						return "§7Grade : " + p.getOlympaPlayer().getGroupNameColored();
				}, plugin, 20),
	
				new TimerLine<Scoreboard<OlympaPlayerCreatif>>( p -> {			
					if (p.getOlympaPlayer().getCustomScoreboardLines().get(5) != null)
						return p.getOlympaPlayer().getCustomScoreboardLines().get(5);
					else {
						Plot plot = plugin.getPlotsManager().getPlot(p.getOlympaPlayer().getPlayer().getLocation());
						if (plot == null)
							return "§7Rang : " + PlotRank.VISITOR.getRankName();
						else
							return "§7Rang : " + plot.getMembers().getPlayerRank(p.getOlympaPlayer()).getRankName();
					}
				}, plugin, 20),
	
				new TimerLine<Scoreboard<OlympaPlayerCreatif>>( p -> {				
					if (p.getOlympaPlayer().getCustomScoreboardLines().get(6) != null)
						return p.getOlympaPlayer().getCustomScoreboardLines().get(6);
					else
						return "§3";
				}, plugin, 20),
	
				new TimerLine<Scoreboard<OlympaPlayerCreatif>>( p -> {			
					if (p.getOlympaPlayer().getCustomScoreboardLines().get(7) != null)
						return p.getOlympaPlayer().getCustomScoreboardLines().get(7);
					else
						return "§7Kumars : §6" + p.getOlympaPlayer().getGameMoneyFormated();
				}, plugin, 20)/*,
	
				new TimerLine<Scoreboard<OlympaPlayerCreatif>>( p -> {				
					if (p.getOlympaPlayer().getCustomScoreboardLines().get(8) != null)
						return p.getOlympaPlayer().getCustomScoreboardLines().get(8);
					else
						return "§7Omégas : §6" + p.getOlympaPlayer().getgetStoreMoney().getFormatted();
				}, plugin, 20)*/
		);
		
		scm.addFooters(
				FixedLine.EMPTY_LINE,
				//new AnimLine<LinesHolder<T>>(plugin, animation, ticksAmount, ticksBetween)
				//AnimLine.olympaAnimation(plugin)
				new FixedLine<Scoreboard<OlympaPlayerCreatif>>("§6play.olympa.fr")
				);
	}

	public WorldManager getWorldManager() {
		return creativeWorldManager;
	}
	
	public WorldEditManager getWorldEditManager() {
		return worldEditManager;
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
}
