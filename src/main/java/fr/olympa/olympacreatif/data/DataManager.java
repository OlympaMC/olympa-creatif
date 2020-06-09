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
	private Map<Player, OlympaPlayerCreatif> players = new HashMap<Player, OlympaPlayerCreatif>();
	
	public DataManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		
		plugin.getServer().getPluginManager().registerEvents(new DataManagerListener(plugin), plugin);
		
		Message.initialize();
	}

	public OlympaPlayerCreatif getCreatifPlayer(Player p) {
		return players.get(p);
	}
	
	public void loadPlayer(OlympaPlayer p) {
		// TODO Auto-generated method stub
		
		//charger plots joueur
		//charger monnaie joueur
		//charger plots suplémentaires joueur
		
		OlympaPlayerCreatif pc = new OlympaPlayerCreatif(plugin, p.getUniqueId(), p.getName(), p.getIp());
		pc.addBonusPlots(0);
		pc.addGameMoney(0);
		
		players.put(p.getPlayer(), pc);
	}

	public void unloadPlayer(Player p) {
		// TODO Auto-generated method stub
		
	}
	
	public void loadPlot(PlotId plotId) {
		AsyncPlot plot = null;
		plugin.getPlotsManager().addAsyncPlot(plot, plotId);			
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
