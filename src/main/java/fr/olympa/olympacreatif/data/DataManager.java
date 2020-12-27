package fr.olympa.olympacreatif.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.EnumUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

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

public class DataManager implements Listener {

	private OlympaCreatifMain plugin;

	private Vector<PlotId> plotsToLoad = new Vector<PlotId>();
	private Vector<Plot> plotsToSave = new Vector<Plot>();
	
	//statements de création des tables
	private final OlympaStatement osTableCreateMessages = new OlympaStatement(
			"CREATE TABLE IF NOT EXISTS `creatif_messages` (" + 
				"`message_id` TINYTEXT NOT NULL DEFAULT ''," + 
				"`message_string` VARCHAR(512) NOT NULL DEFAULT ''," +
				"PRIMARY KEY (`message_id`));"
				);
	
	private final OlympaStatement osTableCreatePlotSchems = new OlympaStatement(
			"CREATE TABLE IF NOT EXISTS `creatif_plotschems` (" + 
				"`plot_id` INT NOT NULL," + 
				"`player_id` BIGINT(20) NOT NULL," +
				"`schem_name` VARCHAR(128) NOT NULL DEFAULT ''," +
				"`schem_data` MEDIUMBLOB NOT NULL," +
				"PRIMARY KEY (`plot_id`));"
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
	
	private final OlympaStatement osCountPlots2 = new OlympaStatement(
			"SELECT MAX (plot_id) FROM creatif_plotsdatas;"
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
	private final OlympaStatement osUpdatePlotSchem = new OlympaStatement(
			"INSERT INTO creatif_plotschems" +
			"(`plot_id`, `player_id`, `schem_name`, `schem_data`) " +
			"VALUES (?, ?, ?, ?) " + 
			"ON DUPLICATE KEY UPDATE " +
			"player_id = VALUES(player_id), " +
			"schem_name = VALUES(schem_name), " +
			"schem_data = VALUES(schem_data);"
			);
	
	public DataManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
		//création tables
		try {
			osTableCreateMessages.getStatement().execute();
			osTableCreatePlotSchems.getStatement().execute();
			osTableCreatePlotParameters.getStatement().execute();
			osTableCreatePlotMembers.getStatement().execute();
			
			ResultSet messages = osSelectMessages.getStatement().executeQuery();
			while (messages.next()) {				
				if (EnumUtils.isValidEnum(Message.class, messages.getString("message_id")))
					Message.valueOf(messages.getString("message_id")).setValue(messages.getString("message_string"));
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}

		//load plot task
		new BukkitRunnable() {
			@Override
			public void run() {
				if (plotsToLoad.size() > 0)
					loadPlot(plotsToLoad.remove(0));
			}
		}.runTaskTimerAsynchronously(plugin, 20, 1);
		
		//unload plot task
		new BukkitRunnable() {
			@Override
			public void run() {
				if (plotsToSave.size() > 0)
					savePlotToBddSync(plotsToSave.remove(0));
			}
		}.runTaskTimer(plugin, 20, 1);
	}
	
	public synchronized void addPlotToLoadQueue(PlotId id, boolean forceInstantLoad) {
		if (!forceInstantLoad)
			plugin.getTask().runTaskAsynchronously(() -> plotsToLoad.add(id));
		else
			loadPlot(id);
	}
	
	public synchronized void addPlotToSaveQueue(Plot plot, boolean forceInstantSave) {
		if (!forceInstantSave)
			plugin.getTask().runTaskAsynchronously(() -> plotsToSave.add(plot));
		else
			savePlotToBddSync(plot);
	}
	
	@EventHandler //charge les plots des joueurs se connectant
	public void onJoin(OlympaPlayerLoadEvent e) {
		try {
			//get player plots
			PreparedStatement getPlayerPlots = osSelectPlayerPlots.getStatement();
			getPlayerPlots.setLong(1, e.getOlympaPlayer().getId());
			ResultSet getPlayerPlotsResult = getPlayerPlots.executeQuery();
			
			while(getPlayerPlotsResult.next()) {
				PlotId id = PlotId.fromId(plugin, getPlayerPlotsResult.getInt("plot_id"));

				//update player name in members table
				if (!e.getPlayer().getName().equals(getPlayerPlotsResult.getString("player_name")) && !getPlayerPlotsResult.getString("player_name").equals("Spawn")) {
					PreparedStatement updPlayerMember = osUpdatePlayerPlotRank.getStatement();
					updPlayerMember.setInt(1, id.getId());
					updPlayerMember.setLong(2, e.getOlympaPlayer().getId());
					updPlayerMember.setString(3, e.getPlayer().getName());
					updPlayerMember.setString(4, e.getOlympaPlayer().getUniqueId().toString());
					updPlayerMember.setInt(5, getPlayerPlotsResult.getInt("player_plot_level"));
					
					updPlayerMember.executeUpdate();
				}  
				
				//add plot to load task
				addPlotToLoadQueue(id, false);
			}
			
		} catch (SQLException | IllegalArgumentException e1) {
			e1.printStackTrace();
		}
	}
	
	private synchronized void loadPlot(PlotId plotId) {
		if (plotId == null)
			return;
		
		//CREATION DU PLOT
		try {
			
			//création plotParameters
			PreparedStatement getPlotDatas;
			getPlotDatas = osSelectPlotDatas.getStatement();
			getPlotDatas.setInt(1, plotId.getId());
			ResultSet getPlotDatasResult = getPlotDatas.executeQuery();
			
			if (!getPlotDatasResult.next())
				return;
			
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
			
			plugin.getPlotsManager().addAsyncPlot(plot);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//sauvegarde les données du plot dans la db
	/*
	public synchronized void savePlot(Plot plot, boolean async) {
		
		if (async)
			plugin.getTask().runTaskAsynchronously(() -> savePlotToBddSync(plot));
		else
			savePlotToBddSync(plot);
	}*/
	
	private synchronized void savePlotToBddSync(Plot plot) {
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
			plugin.getLogger().log(Level.INFO, "Plot " + plot + " saved.");	
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "§4Failed to save plot" + plot + " !");
			e.printStackTrace();
		}
	}

	public synchronized int getPlotsCount() {
		try {
			PreparedStatement ps = osCountPlots.getStatement();
			ResultSet result = ps.executeQuery();
			result.next();
			
			PreparedStatement ps2 = osCountPlots2.getStatement();
			ResultSet result2 = ps2.executeQuery();
			result2.next();
			
			if (result.getInt(1) == result2.getInt(1))
				plugin.getLogger().log(Level.INFO, "§aIntégrité de la table creatif_plotsdata validée : MAX(plot_id) = COUNT(*)");
			else
				plugin.getLogger().log(Level.SEVERE, "§4ATTENTION problème dans la table creatif_plotsdata : nombre d'entrées différent de l'indice du plot maximal !!");
			
			return result.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
			
			return 0;
		}
	}
	
	public synchronized void saveSchemToDb(OlympaPlayerCreatif p, Plot plot, File schem) {
		try {
			PreparedStatement ps = osUpdatePlotSchem.getStatement();

			ps.setInt(1, plot.getPlotId().getId());
			ps.setLong(2, p.getId());
			ps.setString(3, schem.getName());
			ps.setBlob(4, new FileInputStream(schem));
			ps.executeUpdate();
			
		} catch (SQLException | FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}














