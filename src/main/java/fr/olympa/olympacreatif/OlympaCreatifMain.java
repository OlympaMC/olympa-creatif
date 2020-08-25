package fr.olympa.olympacreatif;

import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import fr.olympa.api.command.essentials.tp.TpaHandler;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.api.lines.TimerLine;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.scoreboard.sign.Scoreboard;
import fr.olympa.api.scoreboard.sign.ScoreboardManager;
import fr.olympa.olympacreatif.command.OcCommand;
import fr.olympa.olympacreatif.command.OcaCommand;
import fr.olympa.olympacreatif.command.OcoCommand;
import fr.olympa.olympacreatif.commandblocks.CommandBlocksManager;
import fr.olympa.olympacreatif.data.DataManager;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.perks.PerksManager;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotId;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;
import fr.olympa.olympacreatif.plot.PlotsManager;
import fr.olympa.olympacreatif.world.CustomChunkGenerator;
import fr.olympa.olympacreatif.world.WorldManager;
import fr.olympa.olympacreatif.worldedit.AWEProgressBar;
import fr.olympa.olympacreatif.worldedit.WorldEditListener;
import net.luckperms.api.LuckPerms;

public class OlympaCreatifMain extends OlympaAPIPlugin {

	private WorldManager creativeWorldManager;
	private DataManager dataManager;
	private PlotsManager plotsManager;
	private PerksManager perksManager;
	private CommandBlocksManager cbManager;

	private LuckPerms luckperms;
	private WorldEditPlugin worldedit;
	
	private static OlympaCreatifMain plugin;

	private ScoreboardManager<OlympaPlayerCreatif> scm;

	public Random random = new Random();

	/*
	@Override //retourne le générateur de chunks custom
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
		return new CustomChunkGenerator(this);
	}*/
	

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
		new OcCommand(this, "oc", OcCommand.subArgsList.toArray(new String[OcCommand.subArgsList.size()])).register();
		new OcoCommand(this, "oco", OcoCommand.subArgsList.toArray(new String[OcoCommand.subArgsList.size()])).register();
		new OcaCommand(this, "oca", new String[] { "oca" }).register();

		getServer().getPluginManager().registerEvents(new TpaHandler(this, PermissionsList.TPA), plugin);
		
		dataManager = new DataManager(this);
		plotsManager = new PlotsManager(this);
		creativeWorldManager = new WorldManager(this);
		//worldEditManager = new WorldEditManager(this);
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
		//OlympaCorePermissions.GROUP_COMMAND.allowGroup(OlympaGroup.DEV);
		
		//get luckperms api provider
		RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
		if (provider != null) 
			luckperms = provider.getProvider();
		
		
		//hook into worldedit & asyncworldedit
		WorldEditPlugin we = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
	    IAsyncWorldEdit awe = (IAsyncWorldEdit)getServer().getPluginManager().getPlugin("AsyncWorldEdit");
		if (we != null) {
			worldedit = we;
			worldedit.getWorldEdit().getEventBus().register(new WorldEditListener(this));
			
			if (awe == null) {
				Bukkit.getPluginManager().disablePlugin(we);
				Bukkit.getLogger().log(Level.WARNING, getPrefixConsole() + "WorldEdit disabled because AsyncWorldEdit wasn't found.");
			}
		}
		if (awe != null) {
			awe.getProgressDisplayManager().registerProgressDisplay(new AWEProgressBar());
			Bukkit.getLogger().log(Level.FINE, getPrefixConsole() + "Successfully loaded WorldEdit and AWE custom progressbar.");
		}
	}

	@Override
	public void onDisable() {
		super.onDisable();

		//save plots
		for (Plot plot : getPlotsManager().getPlots())
			getDataManager().savePlot(plot, false);
		
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
			plot = p.getCurrentPlot();
			if (plot == null)
				return "§7Parcelle : §eaucune";
			else
				return "§7Parcelle : §e" + plot;

		case 2:
			plot = p.getCurrentPlot();
			if (plot == null)
				return "§7Proprio : §eaucun";
			else
				return "§7Proprio : §e" + plot.getMembers().getOwner().getName();

		case 3:
			return "§2";

		case 4:
			return "§7Grade : " + p.getGroupNameColored();

		case 5:
			plot = p.getCurrentPlot();
			if (plot == null)
				return "§7Rang : " + PlotRank.VISITOR.getRankName();
			else
				return "§7Rang : " + plot.getMembers().getPlayerRank(p).getRankName();

		case 6:
			return "§3";

		case 7:
			return "§7" + p.getGameMoneyName() + " : §6" + p.getGameMoney() + p.getGameMoneySymbol();

		case 8:
			plot = p.getCurrentPlot();

			if (p.getCustomScoreboardLines().size() > 0 && plot != null)
				return "§8Sidebar plot " + plot;
			else
				return "§4";
		}
		return "";
	}

	public WorldManager getWorldManager() {
		return creativeWorldManager;
	}

	public WorldEditPlugin getWorldEditManager() {
		return worldedit;
	}
	
	public LuckPerms getLuckPerms() {
		return luckperms;
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
