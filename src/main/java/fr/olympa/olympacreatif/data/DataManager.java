package fr.olympa.olympacreatif.data;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.mysql.jdbc.PreparedStatement;

import fr.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.sql.OlympaStatement;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.AsyncPlot;
import fr.olympa.olympacreatif.plot.PlotId;

public class DataManager implements Listener {

	private OlympaCreatifMain plugin;

	//statements de création des tables
	private final OlympaStatement osTableCreateMessages = new OlympaStatement(
			"CREATE TABLE IF NOT EXISTS `creatif_messages` (" + 
				"`messageId` TINYTEXT NOT NULL DEFAULT ''," + 
				"`messageString` VARCHAR(512) NOT NULL DEFAULT '')" + 
				"PRIMARY KEY ('messageId');"
				);
	
	private final OlympaStatement osTableCreatePlotParameters = new OlympaStatement(
			"CREATE TABLE IF NOT EXISTS `creatif_plotsdatas` (" +
					"`plotId` INT NOT NULL," +
					"`plotCreation` DATETIME NOT NULL," +
					"`plotParameters` VARCHAR(2048) NOT NULL," +
					"PRIMARY KEY (`plotId`) );"
			);
	
	private final OlympaStatement osTableCreatePlotMembers = new OlympaStatement(
			"CREATE TABLE IF NOT EXISTS `creatif_plotsmembers` (" +
					"`plotId` INT NOT NULL," +
					"`playerId` INT NOT NULL," +
					"`playerName` VARCHAR(64) NOT NULL," +
					"`playerUuid` VARCHAR(256) NOT NULL," +
					"`playerPlotLevel` TINYINT NOT NULL DEFAULT 0," +
					"PRIMARY KEY (`plotId`, `playerId`) );"
			);
	
	//statements select
	private final OlympaStatement osSelectPlotParameters = new OlympaStatement(
			"SELECT * FROM creatif_plotsdatas WHERE plotId = ?;"
			);
	
	private final OlympaStatement osSelectPlayerPlots = new OlympaStatement(
			"SELECT * FROM creatif_plotsmembers WHERE playerId = ?;"
			);
	
	private final OlympaStatement osCountPlots = new OlympaStatement(
			"COUNT * FROM creatif_plots;"
			);
	
	//statement update data
	private final OlympaStatement osUpdatePlayerPlotRank = new OlympaStatement(
			"INSERT INTO creatif_plotsmembers " +
			"(plotId, playerId, playerName, playerUuid, playerPlotLevel) " + 
			"VALUES (?, ?, ?) " +
			"ON DUPLICATE KEY UPDATE " + 
			"playerName = VALUES(playerName)," +
			"playerUuid = VALUES(playerUuid)," +
			"playerPlotLevel = VALUES(playerPlotLevel);"
			);
	
	private final OlympaStatement osDeletePlayerPlotRank = new OlympaStatement(
			"DELETE FROM creatif_plotsmembers WHERE plotId = ? AND playerId = ?;"
			);
	
	private final OlympaStatement osUpdatePlotParameters = new OlympaStatement(
			"INSERT INTO creatif_plotsdatas " +
			"(plotId, plotParameters) " +
			"VALUES (?, ?) " + 
			"ON DUPLICATE KEY UPDATE " +
			"plotParameters = VALUES(plotParameters);"
			);
	
	public DataManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
		//création tables
		try {
			osTableCreateMessages.getStatement().execute();
			osTableCreatePlotParameters.getStatement().execute();
			osTableCreatePlotMembers.getStatement().execute();
			//ResultSet resultSet = statement.executeQuery();
			//resultSet.close();
		}catch (SQLException e) {
			e.printStackTrace();
		}
		
		Message.initialize();
	}
	
	@EventHandler //charge les plots des joueurs se connectant
	public void onJoin(OlympaPlayerLoadEvent e) {

		plugin.getTask().runTaskAsynchronously(() -> {

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
