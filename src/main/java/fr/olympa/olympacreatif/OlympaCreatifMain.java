package fr.olympa.olympacreatif;

import java.util.Random;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.api.lines.TimerLine;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.scoreboard.sign.Scoreboard;
import fr.olympa.api.scoreboard.sign.ScoreboardManager;
import fr.olympa.olympacreatif.command.OcCommand;
import fr.olympa.olympacreatif.command.OcaCommand;
import fr.olympa.olympacreatif.command.OceCommand;
import fr.olympa.olympacreatif.command.OcoCommand;
import fr.olympa.olympacreatif.commandblocks.CommandBlocksManager;
import fr.olympa.olympacreatif.data.DataManager;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.perks.PerksManager;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotId;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;
import fr.olympa.olympacreatif.plot.PlotsManager;
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

	/*
	@Override //retourne le générateur de chunks custom
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
		return new CustomChunkGenerator(this);
	}
	*/

	public static OlympaCreatifMain getMainClass() {
		return plugin;
	}

	//private OlympaStatement statement = new OlympaStatement("SELECT * FROM xxx WHERE xx = ?");
	@Override
	public void onEnable() {
		super.onEnable();

		plugin = this;
		AccountProvider.setPlayerProvider(OlympaPlayerCreatif.class, OlympaPlayerCreatif::new, "creatif", OlympaPlayerCreatif.COLUMNS);

		OlympaPermission.registerPermissions(PermissionsList.class);

		createScoreboard();

		//saveDefaultConfig();
		new OcCommand(this, "olympacreatif", new String[] { "oc" }).register();
		new OceCommand(this, "olympacreatifedit", new String[] { "oce" }).register();
		new OcoCommand(this, "olympacreatifother", new String[] { "oco" }).register();
		new OcaCommand(this, "olympacreatifadmin", new String[] { "oca" }).register();

		dataManager = new DataManager(this);
		plotsManager = new PlotsManager(this);
		creativeWorldManager = new WorldManager(this);
		worldEditManager = new WorldEditManager(this);
		perksManager = new PerksManager(this);
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
		OlympaCorePermissions.GROUP_COMMAND.addAllowGroup(OlympaGroup.DEV);
	}

	@Override
	public void onDisable() {
		super.onDisable();

		scm.unload();
	}

	//crée le scoreboard du joueur avec des lignes dynamiques, pour afficher le scoreboard custom du plot si besoin
	@SuppressWarnings("unchecked")
	private void createScoreboard() {
		scm = new ScoreboardManager<>(plugin, "§6Olympa Créatif");

		//initialisation lignes scoreboard
		for (int i = 0; i < OlympaPlayerCreatif.scoreboardLinesSize; i++) {
			final int line = i;

			scm.addLines(
					new TimerLine<Scoreboard<OlympaPlayerCreatif>>(p -> {
						return getLine(p.getOlympaPlayer(), line);
					}, plugin, 20));
		}
		scm.addFooters(

				new TimerLine<Scoreboard<OlympaPlayerCreatif>>(p -> {
					return getLine(p.getOlympaPlayer(), OlympaPlayerCreatif.scoreboardLinesSize + 1);
				}, plugin, 20),

				new FixedLine<Scoreboard<OlympaPlayerCreatif>>("§6play.olympa.fr"));
	}

	public String getLine(OlympaPlayerCreatif p, int i) {

		//Bukkit.broadcastMessage(message)

		if (p.getCustomScoreboardLines().size() > i)
			return p.getCustomScoreboardLines().get(i);

		Plot plot;
		PlotId plotId;
		switch (i) {
		case 0:
			return "§1";

		case 1:
			plotId = PlotId.fromLoc(this, p.getPlayer().getLocation());
			if (plotId == null)
				return "§7Parcelle : §eaucune";
			else
				return "§7Parcelle : §e" + plotId;

		case 2:
			plot = plugin.getPlotsManager().getPlot(p.getPlayer().getLocation());
			if (plot == null)
				return "§7Proprio : §eaucun";
			else
				return "§7Proprio : §e" + plot.getMembers().getOwner().getName();

		case 3:
			return "§2";

		case 4:
			return "§7Grade : " + p.getGroupNameColored();

		case 5:
			plot = plugin.getPlotsManager().getPlot(p.getPlayer().getLocation());
			if (plot == null)
				return "§7Rang : " + PlotRank.VISITOR.getRankName();
			else
				return "§7Rang : " + plot.getMembers().getPlayerRank(p).getRankName();

		case 6:
			return "§3";

		case 7:
			return "§7Kumars : §6" + p.getGameMoneyFormated();

		case 8:
			plotId = PlotId.fromLoc(this, p.getPlayer().getLocation());

			if (p.getCustomScoreboardLines().size() > 0 && plotId != null)
				return "§8Sidebar plot " + plotId;
			else
				return "§4";
		}
		return "";
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
