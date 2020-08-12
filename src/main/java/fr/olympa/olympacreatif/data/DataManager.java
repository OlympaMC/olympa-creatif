package fr.olympa.olympacreatif.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import fr.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.sql.OlympaStatement;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.PlotCbData;
import fr.olympa.olympacreatif.perks.KitsManager.KitType;
import fr.olympa.olympacreatif.perks.UpgradesManager.UpgradeType;
import fr.olympa.olympacreatif.plot.AsyncPlot;
import fr.olympa.olympacreatif.plot.PlotId;
import fr.olympa.olympacreatif.plot.PlotMembers;
import fr.olympa.olympacreatif.plot.PlotMembers.MemberInformations;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;
import fr.olympa.olympacreatif.plot.PlotParameters;

public class DataManager implements Listener {

	private OlympaCreatifMain plugin;

	//statements de création des tables
	private final OlympaStatement osTableCreateMessages = new OlympaStatement(
			"CREATE TABLE IF NOT EXISTS `creatif_messages` (" + 
				"`message_id` TINYTEXT NOT NULL DEFAULT ''," + 
				"`message_string` VARCHAR(512) NOT NULL DEFAULT '')" + 
				"PRIMARY KEY ('message_id');"
				);
	
	private final OlympaStatement osTableCreatePlotParameters = new OlympaStatement(
			"CREATE TABLE IF NOT EXISTS `creatif_plotsdatas` (" +
					"`plot_id` INT NOT NULL," +
					"`plot_creation_date` DATETIME NOT NULL DEFAULT NOW()," +
					"`plot_parameters` VARCHAR(2048) NOT NULL," +
					"`plot_upgrade_level_commandblock` TINYINT NOT NULL," +
					"`plot_upgrade_level_maxmembers` TINYINT NOT NULL," +
					"PRIMARY KEY (`plot_Id`) );"
			);
	
	private final OlympaStatement osTableCreatePlotMembers = new OlympaStatement(
			"CREATE TABLE IF NOT EXISTS `creatif_plotsmembers` (" +
					"`plot_id` INT NOT NULL," +
					"`player_id` BIGINT(20) NOT NULL," +
					"`player_name` VARCHAR(64) NOT NULL," +
					"`player_uuid` VARCHAR(256) NOT NULL," +
					"`player_plot_level` TINYINT NOT NULL DEFAULT 0," +
					"PRIMARY KEY (`plot_id`, `player_id`) );"
			);
	
	//statements select
	private final OlympaStatement osSelectPlotParameters = new OlympaStatement(
			"SELECT * FROM creatif_plotsdatas WHERE plot_id = ?;"
			);
	
	private final OlympaStatement osSelectPlayerPlots = new OlympaStatement(
			"SELECT * FROM creatif_plotsmembers WHERE player_id = ?;"
			);
	
	private final OlympaStatement osSelectPlotPlayers = new OlympaStatement(
			"SELECT * FROM creatif_plotsmembers WHERE plot_id = ?;"
			);
	
	private final OlympaStatement osCountPlots = new OlympaStatement(
			"COUNT * FROM creatif_plotsdatas;"
			);
	
	//statement update data
	private final OlympaStatement osUpdatePlayerPlotRank = new OlympaStatement(
			"INSERT INTO creatif_plotsmembers " +
			"(plot_id, player_id, player_name, player_uuid, player_plot_level) " + 
			"VALUES (?, ?, ?, ?, ?) " +
			"ON DUPLICATE KEY UPDATE " + 
			"player_name = VALUES(player_name)," +
			"player_uuid = VALUES(player_uuid)," +
			"player_plot_level = VALUES(player_plot_level);"
			);
	
	private final OlympaStatement osDeletePlayerPlotRank = new OlympaStatement(
			"DELETE FROM creatif_plotsmembers WHERE plot_id = ? AND player_id = ?;"
			);
	
	private final OlympaStatement osUpdatePlotParameters = new OlympaStatement(
			"INSERT INTO creatif_plotsdatas " +
			"(plot_id, plot_parameters) " +
			"VALUES (?, ?) " + 
			"ON DUPLICATE KEY UPDATE " +
			"plot_parameters = VALUES(plot_parameters);"
			);
	
	public DataManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
		//création tables
		try {
			osTableCreateMessages.getStatement().execute();
			osTableCreatePlotParameters.getStatement().execute();
			osTableCreatePlotMembers.getStatement().execute();
		}catch (SQLException e) {
			e.printStackTrace();
		}
		
		Message.initialize();
	}
	
	@EventHandler //charge les plots des joueurs se connectant
	public void onJoin(OlympaPlayerLoadEvent e) {

		OlympaPlayerCreatif p = e.getOlympaPlayer();
		
		plugin.getTask().runTaskAsynchronously(() -> {
			try {
				//get player plots
				PreparedStatement getPlayerPlots = osSelectPlayerPlots.getStatement();
				getPlayerPlots.setLong(1, e.getOlympaPlayer().getId());
				ResultSet getPlayerPlotsResult = getPlayerPlots.executeQuery();
				
				while(getPlayerPlotsResult.next()) {
					//CREATION DU PLOT
					
					//création plotId
					PlotId plotId = PlotId.fromId(plugin, getPlayerPlotsResult.getInt("plot_id"));
					
					//création plotParameters
					PreparedStatement getPlotParams = osSelectPlotParameters.getStatement();
					getPlotParams.setInt(1, plotId.getId());
					ResultSet getPlotParamsResult = getPlotParams.executeQuery();
					
					getPlotParamsResult.next();
					PlotParameters plotParams = PlotParameters.fromJson(plotId, getPlotParamsResult.getString("plot_parameters"));
					
					//création plotMembers
					PreparedStatement getPlotMembers = osSelectPlotPlayers.getStatement();
					getPlotMembers.setInt(1, getPlayerPlotsResult.getInt("plot_id"));
					ResultSet getPlotPlayersResult = getPlotMembers.executeQuery();
					
					PlotMembers plotMembers = new PlotMembers(getPlotParamsResult.getInt("plot_upgrade_level_maxmembers"));
					
					while (getPlotPlayersResult.next()) {
						MemberInformations member = plotMembers.new MemberInformations(
								getPlotPlayersResult.getLong("player_id"), 
								getPlotPlayersResult.getString("player_name"), 
								UUID.fromString(getPlotPlayersResult.getString("player_uuid")));
						
						plotMembers.set(member, PlotRank.getPlotRank(getPlotPlayersResult.getInt("player_plot_level")));
					}
					
					//création plotCbData
					PlotCbData cbData = new PlotCbData(plugin, plugin.getCommandBlocksManager().getScoreboardForPlotCbData(), 
							UpgradeType.CB_LEVEL.getValueOf(p.getUpgradeLevel(UpgradeType.CB_LEVEL)), 
							p.hasKit(KitType.HOSTILE_MOBS) && p.hasKit(KitType.PEACEFUL_MOBS),
							p.hasKit(KitType.HOSTILE_MOBS)
							);
					
					AsyncPlot plot = new AsyncPlot(plugin, plotId, plotMembers, plotParams, cbData);
					
					plugin.getPlotsManager().addAsyncPlot(plot, plotId);
				}
				
			} catch (SQLException | IllegalArgumentException e1) {
				e1.printStackTrace();
			}
			
			//TODO charger plots joueur
			//TODO update les params du plot selon les éventuelles améliorations du joueur
			Bukkit.broadcastMessage("TODO : chargement plots joueurs à la connexion (DataManager)");
			
		});
	}
	
	public void loadPlot(PlotId newId) {
		AsyncPlot plot = null;
		
		plugin.getPlotsManager().addAsyncPlot(plot, newId);			
	}

	private ResultSet executeRequest(String request) {
		ResultSet resultSet = null;
		/*
		try {
			OlympaCore.getInstance().getDatabase();
			//table  
			OlympaStatement statement = new OlympaStatement("CREATE TABLE `BDDolympa`.`PlotsMembers` ( `plot-id` VARCHAR(127) NOT NULL , `player-id` BIGINT NOT NULL , `plot-rank` TINYINT NOT NULL , PRIMARY KEY (`plot-id`, `player-id`)) ENGINE = MyISAM;");
			PreparedStatement preparedStatement = statement.getStatement();
			preparedStatement.setString(1, "xxxxx");
			resultSet = preparedStatement.executeQuery();
			resultSet.close();
		}catch (SQLException e) {
			e.printStackTrace();
		}
		*/
		return resultSet;
	}

	public int getPlotsCount() {
		// TODO Auto-generated method stub
		return 0;
	}
}
