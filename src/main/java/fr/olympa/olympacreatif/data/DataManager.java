package fr.olympa.olympacreatif.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.EnumUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import fr.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.sql.statement.OlympaStatement;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.perks.KitsManager.KitType;
import fr.olympa.olympacreatif.perks.UpgradesManager.UpgradeType;
import fr.olympa.olympacreatif.plot.AsyncPlot;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotCbData;
import fr.olympa.olympacreatif.plot.PlotId;
import fr.olympa.olympacreatif.plot.PlotMembers;
import fr.olympa.olympacreatif.plot.PlotMembers.MemberInformations;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;
import fr.olympa.olympacreatif.plot.PlotParameters;
import fr.olympa.olympacreatif.utils.PermissionsManager;

public class DataManager implements Listener {

	private OlympaCreatifMain plugin;

	private List<PlotId> loadedPlots = new ArrayList<PlotId>();
	
	private PermissionsManager permsManager = new PermissionsManager();
	
	public PermissionsManager getPermissionsManager() {
		return permsManager;
	}
	
	//statements de création des tables
	private final OlympaStatement osTableCreateMessages = new OlympaStatement(
			"CREATE TABLE IF NOT EXISTS `creatif_messages` (" + 
				"`message_id` TINYTEXT NOT NULL DEFAULT ''," + 
				"`message_string` VARCHAR(512) NOT NULL DEFAULT ''," +
				"PRIMARY KEY (`message_id`));"
				);
	
	private final OlympaStatement osTableCreatePlotParameters = new OlympaStatement(
			"CREATE TABLE IF NOT EXISTS `creatif_plotsdatas` (" +
					"`plot_id` INT NOT NULL," +
					"`plot_creation_date` DATETIME NOT NULL DEFAULT NOW()," +
					"`plot_parameters` VARCHAR(8192) NOT NULL DEFAULT '', " +
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
	private final OlympaStatement osSelectMessages = new OlympaStatement(
			"SELECT * FROM creatif_messages;"
			);
	
	private final OlympaStatement osSelectPlotOwner = new OlympaStatement(
			"SELECT * FROM creatif_plotsmembers WHERE `plot_id` = ? AND `player_plot_level` = ?;"
			);
	
	private final OlympaStatement osSelectPlotPlayers = new OlympaStatement(
			"SELECT * FROM creatif_plotsmembers WHERE `plot_id` = ?;"
			);
	
	private final OlympaStatement osSelectPlotDatas = new OlympaStatement(
			"SELECT * FROM creatif_plotsdatas WHERE `plot_id` = ?;"
			);
	
	private final OlympaStatement osSelectPlayerPlots = new OlympaStatement(
			"SELECT * FROM creatif_plotsmembers WHERE `player_id` = ?;"
			);
	
	private final OlympaStatement osCountPlots = new OlympaStatement(
			"SELECT COUNT (*) FROM creatif_plotsdatas;"
			);
	
	private final OlympaStatement osSelectPlayerDatas = new OlympaStatement(
			"SELECT * FROM creatif_players WHERE `player_id` = ?;"			
			);
	
	//statement update data
	private final OlympaStatement osUpdatePlayerPlotRank = new OlympaStatement(
			"INSERT INTO creatif_plotsmembers " +
			"(`plot_id`, `player_id`, `player_name`, `player_uuid`, `player_plot_level`) " + 
			"VALUES (?, ?, ?, ?, ?) " +
			"ON DUPLICATE KEY UPDATE " + 
			"player_name = VALUES(player_name)," +
			"player_uuid = VALUES(player_uuid)," +
			"player_plot_level = VALUES(player_plot_level);"
			);
	
	private final OlympaStatement osDeletePlayerPlotRank = new OlympaStatement(
			"DELETE FROM creatif_plotsmembers WHERE `plot_id`= ? AND `player_id`= ?;"
			);
	
	private final OlympaStatement osUpdatePlotDatas = new OlympaStatement(
			"INSERT INTO creatif_plotsdatas " +
			"(`plot_id`, `plot_parameters`) " +
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
			
			ResultSet messages = osSelectMessages.getStatement().executeQuery();
			while (messages.next()) {
				/*Bukkit.getLogger().log(Level.INFO, "Message " + messages.getString("message_id") + " : " + 
						messages.getString("message_string"));*/
				
				if (EnumUtils.isValidEnum(Message.class, messages.getString("message_id")))
					Message.valueOf(messages.getString("message_id")).setValue(messages.getString("message_string"));
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		
		
	}
	
	@EventHandler //charge les plots des joueurs se connectant
	public void onJoin(OlympaPlayerLoadEvent e) {
		
		plugin.getTask().runTaskAsynchronously(() -> {
			try {
				//get player plots
				PreparedStatement getPlayerPlots = osSelectPlayerPlots.getStatement();
				getPlayerPlots.setLong(1, e.getOlympaPlayer().getId());
				ResultSet getPlayerPlotsResult = getPlayerPlots.executeQuery();
				
				while(getPlayerPlotsResult.next()) {
					PlotId id = PlotId.fromId(plugin, getPlayerPlotsResult.getInt("plot_id"));
						loadPlot(id);
				}
				
			} catch (SQLException | IllegalArgumentException e1) {
				e1.printStackTrace();
			}
		});
	}

	public synchronized void loadPlot(PlotId plotId) {
		if (plotId == null || loadedPlots.contains(plotId))
			return;
		
		//Bukkit.broadcastMessage("Load plot " + plotId);
		
		loadedPlots.add(plotId);
		
		plugin.getTask().runTaskAsynchronously(() -> {
			
			//CREATION DU PLOT
			try {
				
				//création plotParameters
				PreparedStatement getPlotDatas;
				getPlotDatas = osSelectPlotDatas.getStatement();
				getPlotDatas.setInt(1, plotId.getId());
				ResultSet getPlotDatasResult = getPlotDatas.executeQuery();
				
				getPlotDatasResult.next();
				PlotParameters plotParams = PlotParameters.fromJson(plugin, plotId, getPlotDatasResult.getString("plot_parameters"));
				
				//get owner id
				PreparedStatement getPlotOwner = osSelectPlotOwner.getStatement();
				getPlotOwner.setInt(1, plotId.getId());
				getPlotOwner.setInt(2, 4);
				ResultSet getPlotOwnerResult = getPlotOwner.executeQuery();
				getPlotOwnerResult.next();
				
				//get owner data
				PreparedStatement getPlayerDatas = osSelectPlayerDatas.getStatement();
				getPlayerDatas.setLong(1, getPlotOwnerResult.getLong("player_id"));
				ResultSet getPlotOwnerDatasResult = getPlayerDatas.executeQuery();
				getPlotOwnerDatasResult.next();
				
				//création plotMembers
				PlotMembers plotMembers = new PlotMembers(UpgradeType.BONUS_MEMBERS_LEVEL.getValueOf(
						getPlotOwnerDatasResult.getInt(UpgradeType.BONUS_MEMBERS_LEVEL.getBddKey())));
				
				PreparedStatement getPlotMembers = osSelectPlotPlayers.getStatement();
				getPlotMembers.setInt(1, plotId.getId());
				ResultSet getPlotPlayersResult = getPlotMembers.executeQuery();
				
				while (getPlotPlayersResult.next()) {
					MemberInformations member = plotMembers.new MemberInformations(
							getPlotPlayersResult.getLong("player_id"), 
							getPlotPlayersResult.getString("player_name"), 
							UUID.fromString(getPlotPlayersResult.getString("player_uuid")));
					
					plotMembers.set(member, PlotRank.getPlotRank(getPlotPlayersResult.getInt("player_plot_level")));
				}
				
				//création plotCbData
				PlotCbData cbData = new PlotCbData(plugin, 
						UpgradeType.CB_LEVEL.getValueOf(getPlotOwnerDatasResult.getInt(UpgradeType.CB_LEVEL.getBddKey())),
						getPlotOwnerDatasResult.getBoolean(KitType.HOSTILE_MOBS.getBddKey()) && getPlotOwnerDatasResult.getBoolean(KitType.PEACEFUL_MOBS.getBddKey()),
						getPlotOwnerDatasResult.getBoolean(KitType.HOSTILE_MOBS.getBddKey())
						);
				
				AsyncPlot plot = new AsyncPlot(plugin, plotId, plotMembers, plotParams, cbData, 
						getPlotOwnerDatasResult.getBoolean(KitType.FLUIDS.getBddKey()));
				
				plugin.getPlotsManager().addAsyncPlot(plot, plotId);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		});
	}
	
	//sauvegarde les données du plot dans la db
	public void savePlot(Plot plot, boolean async) {
		loadedPlots.remove(plot.getPlotId());
		
		if (async)
			plugin.getTask().runTaskAsynchronously(() -> savePlotToBddSync(plot));
		else
			savePlotToBddSync(plot);
	}
	
	private synchronized void savePlotToBddSync(Plot plot) {
		
		//Bukkit.broadcastMessage("Save plot " + plot);
		
		try {
			int id = plot.getPlotId().getId();
			
			//update plot datas
			PreparedStatement updPlotParams = osUpdatePlotDatas.getStatement();
			updPlotParams.setInt(1, id);
			updPlotParams.setString(2, plot.getParameters().toJson());
			updPlotParams.executeUpdate();
			
			//update plot members
			for (Entry<MemberInformations, PlotRank> e : plot.getMembers().getList().entrySet())
				if (e.getValue() == PlotRank.VISITOR) {
					PreparedStatement delPlotMember = osDeletePlayerPlotRank.getStatement();
					delPlotMember.setInt(1, id);
					delPlotMember.setLong(2, e.getKey().getId());
					delPlotMember.executeUpdate();
				}else {
					PreparedStatement updPlotMember = osUpdatePlayerPlotRank.getStatement();
					updPlotMember.setInt(1, id);
					updPlotMember.setLong(2, e.getKey().getId());
					updPlotMember.setString(3, e.getKey().getName());
					updPlotMember.setString(4, e.getKey().getUUID().toString());
					updPlotMember.setInt(5, e.getValue().getLevel());
					updPlotMember.executeUpdate();
				}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	private ResultSet executeRequest(String request) {
		ResultSet resultSet = null;
		
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
		
		return resultSet;
	}
	*/

	public int getPlotsCount() {
		try {
			PreparedStatement ps = osCountPlots.getStatement();
			ResultSet result = ps.executeQuery();
			result.next();
			return result.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
			
			return 0;
		}
	}
}
