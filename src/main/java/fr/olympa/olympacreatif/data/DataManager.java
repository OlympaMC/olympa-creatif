package fr.olympa.olympacreatif.data;

import java.sql.ResultSet;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.AsyncPlot;
import fr.olympa.olympacreatif.plot.PlotId;

public class DataManager {

	private OlympaCreatifMain plugin;
	
	public DataManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		
		plugin.getServer().getPluginManager().registerEvents(new DataManagerListener(plugin), plugin);
		
		Message.initialize();
	}
	
	public void loadPlayerPlots(OlympaPlayerCreatif p) {
		//TODO charger plots joueur
		
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
