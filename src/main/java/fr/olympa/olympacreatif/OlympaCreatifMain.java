package fr.olympa.olympacreatif;

import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;

import com.boydti.fawe.Fawe;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import fr.olympa.api.command.essentials.tp.TpaHandler;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.api.lines.TimerLine;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.scoreboard.sign.Scoreboard;
import fr.olympa.api.scoreboard.sign.ScoreboardManager;
import fr.olympa.api.server.OlympaServer;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.olympacreatif.command.OcCommand;
import fr.olympa.olympacreatif.command.OcaCommand;
import fr.olympa.olympacreatif.command.OcoCommand;
import fr.olympa.olympacreatif.commandblocks.CommandBlocksManager;
import fr.olympa.olympacreatif.data.DataManager;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.data.PermissionsManager;
import fr.olympa.olympacreatif.perks.PerksManager;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotPerm.PlotRank;
import fr.olympa.olympacreatif.plot.PlotsManager;
import fr.olympa.olympacreatif.world.WorldManager;
import fr.olympa.olympacreatif.worldedit.AWEProgressBar;
import fr.olympa.olympacreatif.worldedit.WorldEditManager;

public class OlympaCreatifMain extends OlympaAPIPlugin {

	private WorldManager worldManager;
	private DataManager dataManager;
	private PlotsManager plotsManager;
	private PerksManager perksManager;
	private CommandBlocksManager cbManager;

	private PermissionsManager permsManager;
	private WorldEditManager weManager;
	
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

	
	@Override //actions on load: STARTUP. Main stuff is done in onEnablePOSTWORLD().
	public void onEnable() {
		super.onEnable();
		
		plugin = this;
		OlympaCore.getInstance().setOlympaServer(OlympaServer.CREATIF);
		
		AccountProvider.setPlayerProvider(OlympaPlayerCreatif.class, OlympaPlayerCreatif::new, "creatif", OlympaPlayerCreatif.COLUMNS);
		OlympaPermission.registerPermissions(PermissionsList.class);
		createScoreboard();
		
		new OcCommand(this, "oc", OcCommand.subArgsList.toArray(new String[OcCommand.subArgsList.size()])).register();
		new OcoCommand(this, "oco", OcoCommand.subArgsList.toArray(new String[OcoCommand.subArgsList.size()])).register();
		new OcaCommand(this, "oca", OcaCommand.subArgsList.toArray(new String[OcaCommand.subArgsList.size()])).register();

		getServer().getPluginManager().registerEvents(new TpaHandler(this, PermissionsList.TPA), plugin);
		
		dataManager = new DataManager(this);
		worldManager = new WorldManager(this);
		perksManager = new PerksManager(this);
		plotsManager = new PlotsManager(this);
		cbManager = new CommandBlocksManager(this);
		permsManager = new PermissionsManager(this);

		weManager = new WorldEditManager(this);
		
		//OlympaCorePermissions.GROUP_COMMAND.allowGroup(OlympaGroup.DEV);
		
		//get luckperms api provider
		/*
		RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
		if (provider != null) 
			luckperms = provider.getProvider();*/
		
		
		//hook into worldedit & asyncworldedit
		
		/*
		we = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
	    awe = (IAsyncWorldEdit) getServer().getPluginManager().getPlugin("AsyncWorldEdit");
	    
		if (we != null) {
			we.getWorldEdit().getEventBus().register(new WorldEditListener(this));
			
			if (awe == null) {
				Bukkit.getPluginManager().disablePlugin(we);
				Bukkit.getLogger().log(Level.WARNING, getPrefixConsole() + "WorldEdit disabled because AsyncWorldEdit wasn't found.");
			}
		}
		
		if (awe != null) {
			awe.getProgressDisplayManager().registerProgressDisplay(new AWEProgressBar());
			Bukkit.getLogger().log(Level.FINE, getPrefixConsole() + "Successfully loaded WorldEdit and AWE custom progressbar.");
			weEnabled = true;
		}   */
	}

	@Override
	public void onDisable() {
		super.onDisable();

		//save plots
		for (Plot plot : getPlotsManager().getPlots()) {
			plot.unload();
			getDataManager().addPlotToSaveQueue(plot, true);	
		}
		
		scm.unload();
	}

	//crée le scoreboard du joueur avec des lignes dynamiques, pour afficher le scoreboard custom du plot si besoin
	@SuppressWarnings("unchecked")
	private void createScoreboard() {
		scm = new ScoreboardManager<OlympaPlayerCreatif>(plugin, "§6Olympa Créatif");

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
				return "§7Proprio : §e" + plot.getMembers().getOwner().getName();

		case 3:
			return "§2";

		case 4:
			return "§7Grade : " + p.getGroupNameColored();

		case 5:
			if (plot == null)
				return "§7Rang : " + PlotRank.VISITOR.getRankName();
			else
				return "§7Rang : " + plot.getMembers().getPlayerRank(p).getRankName();

		case 6:
			return "§3";

		case 7:
			return "§7" + p.getGameMoneyName() + " : §6" + p.getGameMoney() + p.getGameMoneySymbol();

		case 8:
			if (p.getCustomScoreboardLines().size() > 0 && plot != null)
				return "§8Sidebar plot " + plot;
			else
				return "§4";
		}
		return "";
	}

	public WorldManager getWorldManager() {
		return worldManager;
	}

	public WorldEditManager getWEManager() {
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
}
