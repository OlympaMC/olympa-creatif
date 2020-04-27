package fr.olympa.olympacreatif.data;

import java.sql.ResultSet;
import java.util.AbstractMap.SimpleEntry;

import org.bukkit.Bukkit;

import java.util.List;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.AsyncPlot;
import fr.olympa.olympacreatif.plot.PlotId;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;

public class DataManager {

	private OlympaCreatifMain plugin;
	
	public DataManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		Message.initialize();
	}
	
	
	public void loadPlot(PlotId plotId) {
		AsyncPlot plot = null;
		plugin.getPlotsManager().addAsyncPlot(plot, plotId);			
	}

	public void loadPlayerPlots(OlympaPlayer p) {
		// TODO Auto-generated method stub
		plugin.getPlotsManager().addLoadedPlayer(p.getPlayer());
		return;
	}
	
	public void updatePlayerPlotRank(long playerId, PlotId plotId, PlotRank rank) {
		
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


	public int getTotalPlotsCount() {
		// TODO Auto-generated method stub
		return 0;
	}
}
