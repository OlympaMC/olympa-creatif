package fr.olympa.olympacreatif.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.api.common.player.OlympaPlayerInformations;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.common.sql.statement.OlympaStatement;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.perks.KitsManager.KitType;
import fr.olympa.olympacreatif.perks.UpgradesManager.UpgradeType;
import fr.olympa.olympacreatif.plot.AsyncPlot;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotCbData;
import fr.olympa.olympacreatif.plot.PlotId;
import fr.olympa.olympacreatif.plot.PlotMembers;
import fr.olympa.olympacreatif.plot.PlotMembers.MemberInformations;
import fr.olympa.olympacreatif.plot.PlotParameters;
import fr.olympa.olympacreatif.plot.PlotPerm.PlotRank;

public class DataManager implements Listener {

	private OlympaCreatifMain plugin;
	private int serverIndex = -1;
	private int nextPlotSyncInstantiateTick = 1;

	//statements de création des tables
	private final String osTableCreateMessages =
			"CREATE TABLE IF NOT EXISTS `creatif_messages` (" +
					"`message_id` TINYTEXT NOT NULL DEFAULT ''," +
					"`message_string` VARCHAR(512) NOT NULL DEFAULT ''," +
					"PRIMARY KEY (`message_id`));";

	private final String osTableCreateServerParams =
			"CREATE TABLE IF NOT EXISTS `creatif_server_params` (" +
					"`server_id` INT NOT NULL," +
					"`server_params` VARCHAR(8192) NOT NULL DEFAULT ''," +
					"PRIMARY KEY (`server_id`));";

	private final String osTableCreatePlotSchems =
			"CREATE TABLE IF NOT EXISTS `creatif_plotschems` (" +
					"`server_id` INT NOT NULL," +
					"`plot_id` INT NOT NULL," +
					"`player_id` BIGINT(20) NOT NULL," +
					"`schem_name` VARCHAR(128) NOT NULL DEFAULT ''," +
					"`schem_data` MEDIUMBLOB NOT NULL," +
					"PRIMARY KEY (`server_id`, `plot_id`));";

	private final String osTableCreatePlotParameters =
			"CREATE TABLE IF NOT EXISTS `creatif_plotsdatas` (" +
					"`server_id` INT NOT NULL," +
					"`plot_id` INT NOT NULL," +
					"`plot_creation_date` DATETIME NOT NULL DEFAULT NOW()," +
					"`plot_parameters` TEXT NOT NULL DEFAULT '', " +
					"PRIMARY KEY (`server_id`, `plot_Id`));";

	private final String osTableCreatePlotMembers =
			"CREATE TABLE IF NOT EXISTS `creatif_plotsmembers` (" +
					"`server_id` INT NOT NULL," +
					"`plot_id` INT NOT NULL," +
					"`player_id` BIGINT(20) NOT NULL," +
					"`player_name` VARCHAR(64) NOT NULL," +
					"`player_uuid` VARCHAR(256) NOT NULL," +
					"`player_plot_level` TINYINT NOT NULL DEFAULT 0," +
					"PRIMARY KEY (`server_id`, `plot_id`, `player_id`));";

	//statements select
	private final OlympaStatement osSelectMessages = new OlympaStatement(
			"SELECT * FROM creatif_messages;");

	private final OlympaStatement osSelectPlotOwner = new OlympaStatement(
			"SELECT * FROM creatif_plotsmembers WHERE `server_id` = ? AND `plot_id` = ? AND `player_plot_level` = ?;");

	private final OlympaStatement osSelectPlotPlayers = new OlympaStatement(
			"SELECT * FROM creatif_plotsmembers WHERE `server_id` = ? AND `plot_id` = ?;");

	private final OlympaStatement osSelectPlotDatas = new OlympaStatement(
			"SELECT * FROM creatif_plotsdatas WHERE `server_id` = ? AND `plot_id` = ?;");

	private final OlympaStatement osSelectPlayerPlots = new OlympaStatement(
			"SELECT * FROM creatif_plotsmembers WHERE `server_id` = ? AND `player_id` = ?;");

	private final OlympaStatement osSelectOwnedPlayerPlots = new OlympaStatement(
			"SELECT * FROM creatif_plotsmembers WHERE `server_id` = ? AND `player_id` = ? " +
					"AND `player_plot_level` = " + PlotRank.OWNER.getLevel() + ";");

	private final OlympaStatement osCountPlots = new OlympaStatement(
			"SELECT COUNT (*) FROM creatif_plotsdatas WHERE `server_id` = ?;");

	private final OlympaStatement osCountPlots2 = new OlympaStatement(
			"SELECT MAX (plot_id) FROM creatif_plotsdatas WHERE `server_id` = ?;");

	private final OlympaStatement osSelectPlayerDatas = new OlympaStatement(
			"SELECT * FROM creatif_players WHERE `player_id` = ?;");

	private final OlympaStatement osSelectServerParams = new OlympaStatement(
			"SELECT * FROM creatif_server_params WHERE `server_id` = ?;");

	private final OlympaStatement osSelectPlotSchem = new OlympaStatement(
			"SELECT * FROM creatif_plotschems WHERE `server_id` = ? AND `plot_id` = ?;");

	//statement update data
	private final OlympaStatement osUpdatePlayerPlotRank = new OlympaStatement(
			"INSERT INTO creatif_plotsmembers " +
					"(`server_id`, `plot_id`, `player_id`, `player_name`, `player_uuid`, `player_plot_level`) " +
					"VALUES (?, ?, ?, ?, ?, ?) " +
					"ON DUPLICATE KEY UPDATE " +
					"player_name = VALUES(player_name)," +
					"player_uuid = VALUES(player_uuid)," +
					"player_plot_level = VALUES(player_plot_level);");

	private final OlympaStatement osDeletePlayerPlotRank = new OlympaStatement(
			"DELETE FROM creatif_plotsmembers WHERE `server_id` = ? AND `plot_id`= ? AND `player_id`= ?;");

	private final OlympaStatement osUpdatePlotDatas = new OlympaStatement(
			"INSERT INTO creatif_plotsdatas " +
					"(`server_id`, `plot_id`, `plot_parameters`) " +
					"VALUES (?, ?, ?) " +
					"ON DUPLICATE KEY UPDATE " +
					"plot_parameters = VALUES(plot_parameters);");

	private final OlympaStatement osUpdatePlotSchem = new OlympaStatement(
			"INSERT INTO creatif_plotschems" +
					"(`server_id`, `plot_id`, `player_id`, `schem_name`, `schem_data`) " +
					"VALUES (?, ?, ?, ?, ?) " +
					"ON DUPLICATE KEY UPDATE " +
					"player_id = VALUES(player_id), " +
					"schem_name = VALUES(schem_name), " +
					"schem_data = VALUES(schem_data);");

	private final OlympaStatement osUpdateServerParams = new OlympaStatement(
			"INSERT INTO creatif_server_params " +
					"(`server_id`, `server_params`) " +
					"VALUES (?, ?) " +
					"ON DUPLICATE KEY UPDATE " +
					"server_params = VALUES(server_params);");

	public DataManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		plugin.saveDefaultConfig();
		
		if (!plugin.getConfig().isInt("server_index") || plugin.getConfig().getInt("server_index") == -1) {
			plugin.getLogger().severe("§4L'index du serveur n'a pas été défini. Veuillez le renseigner dans le fichier config.yml.");
			plugin.getConfig().set("server_index", -1);
			plugin.saveConfig();
			Bukkit.shutdown();
			return;
		}else
			updateWithServerIndex(plugin.getConfig().getInt("server_index"));
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		//register redis
		//OlympaCore.getInstance().registerRedisSub(RedisAccess.INSTANCE.connect(), new OCRedisListener(plugin), RedisChannel.BUNGEE_ASK_SEND_SERVERNAME.name());

		//création tables
		try {
			Statement statement = OlympaCore.getInstance().getDatabase().createStatement();
			statement.execute(osTableCreateMessages);
			statement.execute(osTableCreateServerParams);
			statement.execute(osTableCreatePlotSchems);
			statement.execute(osTableCreatePlotParameters);
			statement.execute(osTableCreatePlotMembers);
			
			reloadMessages();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void reloadMessages() {
		try {
			PreparedStatement statement = osSelectMessages.createStatement();
			ResultSet messages = statement.executeQuery();

			Map<String, OCmsg> ocMsgs = OCmsg.values();
			Set<String> inexistantMessagesInBdd = new HashSet<>();
			inexistantMessagesInBdd.addAll(ocMsgs.keySet());

			while (messages.next())
				if (ocMsgs.containsKey(messages.getString("message_id"))) {
					ocMsgs.get(messages.getString("message_id")).setValue(messages.getString("message_string"));
					inexistantMessagesInBdd.remove(messages.getString("message_id"));
					//plugin.getLogger().info("§aMessage " + messages.getString("message_id") + " : " + ocMsgs.get(messages.getString("message_id")).getValue());
				} else
					plugin.getLogger().info("Message " + messages.getString("message_id") + " existant EN BDD mais pas dans le plugin, veuillez supprimer l'entrée.");

			inexistantMessagesInBdd.forEach(msg -> plugin.getLogger().warning("§eMessage " + msg + " existant DANS LE PLUGIN mais pas en bdd, veuiller ajouter l'entrée !"));
			messages.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public synchronized void loadPlot(PlotId id, boolean syncLoad) {
		loadPlot(id, syncLoad, null);
	}

	public synchronized void loadPlot(PlotId id, boolean syncLoad, Consumer<Plot> callback) {
		if (!syncLoad)
			plugin.getTask().runTaskAsynchronously(() -> loadPlot(id, callback));
		else
			loadPlot(id, callback);
	}

	public synchronized void loadPlot(final OlympaPlayerCreatif requester, final String player, final int plotId, final Consumer<Plot> callback) {
		plugin.getTask().runTaskAsynchronously(() -> {
			try {
				OlympaPlayerInformations playerInformations = AccountProviderAPI.getter().getPlayerInformations(player);
				if (playerInformations == null) {
					callback.accept(null);
					return;
				}
				
				PreparedStatement statement = osSelectOwnedPlayerPlots.createStatement();
				statement.setInt(1, serverIndex);
				statement.setLong(2, playerInformations.getId());
				ResultSet result = statement.executeQuery();
				
				PlotId id = PlotId.fromId(plugin, plotId); 
				
				int currentIndex = 0;
				while (result.next()) {
					if (plotId == ++currentIndex) {
						loadPlot(PlotId.fromId(plugin, result.getInt("plot_id")), false, callback);
						currentIndex = -1;
						break;
						/*final int taskId = plugin.getTask().scheduleSyncRepeatingTask(() -> {
							Plot plot = plugin.getPlotsManager().getPlot(id);
								
							if (plot != null && requester.isConnected())
								callback.accept(plot);
								
						}, 5, 5);
						
						plugin.getTask().runTaskLater(() -> {
							if (plugin.getPlotsManager().getPlot(id) == null)
								OCmsg.UNKNOWN_PLOT_INDEX.send(requester);
							plugin.getTask().cancelTaskById(taskId);
						}, 80);
						break;*/
					}
				}
				
				if (currentIndex != -1)
					callback.accept(null);
				
				statement.close();
				result.close();
				
			}catch(Exception e) {
				e.printStackTrace();
			}
		});
	}

	public synchronized void savePlot(Plot plot, boolean syncSave) {
		if (!syncSave)
			plugin.getTask().runTaskAsynchronously(() -> savePlot(plot));
		else
			savePlot(plot);
	}

	@EventHandler
	public void onJoinAsync(AsyncPlayerPreLoginEvent e) {
		if (serverIndex == -1)
			e.disallow(Result.KICK_OTHER, "§cIndex du serveur encore inconnu. Réessayez dans quelques instants...");
	}

	@EventHandler //charge les plots des joueurs se connectant
	public void onJoin(PlayerJoinEvent e) {
		if (serverIndex == -1) {
			plugin.getLogger().log(Level.WARNING, "§4[DataManager] §cIndex du serveur = -1 : impossible de charger un nouveau joueur !");
			return;
		}
		OlympaPlayerCreatif pc = AccountProviderAPI.getter().get(e.getPlayer().getUniqueId());
		
		//get player plots
		plugin.getTask().runTaskAsynchronously(() -> {
			try (PreparedStatement getPlayerPlots = osSelectPlayerPlots.createStatement()) {
				getPlayerPlots.setLong(1, serverIndex);
				getPlayerPlots.setLong(2, pc.getId());
				ResultSet getPlayerPlotsResult = osSelectPlayerPlots.executeQuery(getPlayerPlots);
				
				while (getPlayerPlotsResult.next()) {
					final PlotId id = PlotId.fromId(plugin, getPlayerPlotsResult.getInt("plot_id"));
					
					//update player name in members table
					if (!e.getPlayer().getName().equals(getPlayerPlotsResult.getString("player_name")) && !getPlayerPlotsResult.getString("player_name").equals("Spawn")) {
						try (PreparedStatement updPlayerMember = osUpdatePlayerPlotRank.createStatement()) {
							updPlayerMember.setInt(1, serverIndex);
							updPlayerMember.setInt(2, id.getId());
							updPlayerMember.setLong(3, pc.getId());
							updPlayerMember.setString(4, e.getPlayer().getName());
							updPlayerMember.setString(5, pc.getUniqueId().toString());
							updPlayerMember.setInt(6, getPlayerPlotsResult.getInt("player_plot_level"));
							
							osUpdatePlayerPlotRank.executeUpdate(updPlayerMember);
							updPlayerMember.close();
						}
					}
					
					//add plot to load task
					int i = 1;
					plugin.getTask().runTaskLater(() -> loadPlot(id, false), i+=5);
				}
				
				getPlayerPlots.close();
				getPlayerPlotsResult.close();
			}catch (SQLException ex) {
				ex.printStackTrace();
			}
		});
	}

	private synchronized void loadPlot(PlotId plotId, Consumer<Plot> callback) {
		if (plotId == null)
			return;
		
		if (serverIndex == -1) {
			plugin.getLogger().log(Level.WARNING, "§4[DataManager] §cIndex du serveur = -1 : impossible de charger une parcelle !");
			return;
		}

		//System.out.println("LOADING PLOT " + plotId + " ON MAIN THREAD : " + Bukkit.isPrimaryThread());

		//CREATION DU PLOT
		try {

			//création plotParameters
			PreparedStatement getPlotDatas = osSelectPlotDatas.createStatement();
			getPlotDatas.setInt(1, serverIndex);
			getPlotDatas.setInt(2, plotId.getId());
			ResultSet getPlotDatasResult = osSelectPlotDatas.executeQuery(getPlotDatas);

			if (!getPlotDatasResult.next()) {
				getPlotDatas.close();
				return;
			}

			PlotParameters plotParams = PlotParameters.fromJson(plugin, plotId, getPlotDatasResult.getString("plot_parameters"));
			getPlotDatasResult.close();
			getPlotDatas.close();

			//get owner id
			PreparedStatement getPlotOwner = osSelectPlotOwner.createStatement();
			getPlotOwner.setInt(1, serverIndex);
			getPlotOwner.setInt(2, plotId.getId());
			getPlotOwner.setInt(3, 4);
			ResultSet getPlotOwnerResult = osSelectPlotOwner.executeQuery(getPlotOwner);
			getPlotOwnerResult.next();

			//get owner data
			PreparedStatement getPlayerDatas = osSelectPlayerDatas.createStatement();
			getPlayerDatas.setLong(1, getPlotOwnerResult.getLong("player_id"));
			ResultSet getPlotOwnerDatasResult = osSelectPlayerDatas.executeQuery(getPlayerDatas);
			getPlotOwnerDatasResult.next();
			
			//création plotMembers
			PlotMembers plotMembers = new PlotMembers(UpgradeType.BONUS_MEMBERS_LEVEL.getDataOf(
					getPlotOwnerDatasResult.getInt(UpgradeType.BONUS_MEMBERS_LEVEL.getBddKey())).value);

			//System.out.println("Plot " + plotId + " max members : " + plotMembers.getMaxMembers());
			
			PreparedStatement getPlotMembers = osSelectPlotPlayers.createStatement();
			getPlotMembers.setInt(1, serverIndex);
			getPlotMembers.setInt(2, plotId.getId());
			ResultSet getPlotPlayersResult = osSelectPlotPlayers.executeQuery(getPlotMembers);
			
			while (getPlotPlayersResult.next()) {				
				MemberInformations member = new MemberInformations(
						getPlotPlayersResult.getLong("player_id"),
						getPlotPlayersResult.getString("player_name"),
						UUID.fromString(getPlotPlayersResult.getString("player_uuid")));

				plotMembers.set(member, PlotRank.getPlotRank(getPlotPlayersResult.getInt("player_plot_level")));
			}

			//création plotCbData
			PlotCbData cbData = new PlotCbData(plugin,  
					UpgradeType.CB_LEVEL.getDataOf(getPlotOwnerDatasResult.getInt(UpgradeType.CB_LEVEL.getBddKey())).value,
					getPlotOwnerDatasResult.getBoolean(KitType.HOSTILE_MOBS.getBddKey()) && getPlotOwnerDatasResult.getBoolean(KitType.PEACEFUL_MOBS.getBddKey()),
					getPlotOwnerDatasResult.getBoolean(KitType.HOSTILE_MOBS.getBddKey()));

			AsyncPlot plot = new AsyncPlot(plugin, plotId, plotMembers, plotParams, cbData,
					getPlotOwnerDatasResult.getBoolean(KitType.FLUIDS.getBddKey()));
			
			nextPlotSyncInstantiateTick += 10;
			plugin.getTask().runTaskLater(() -> {
				plugin.getPlotsManager().loadPlot(plot, callback);
				nextPlotSyncInstantiateTick -= 10;
			}, nextPlotSyncInstantiateTick);
			
			getPlotOwnerResult.close();
			getPlotOwner.close();
			getPlotOwnerDatasResult.close();
			getPlayerDatas.close();
			getPlotPlayersResult.close();
			getPlotMembers.close();
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

	private synchronized void savePlot(Plot plot) {
		if (serverIndex == -1) {
			plugin.getLogger().log(Level.WARNING, "§4[DataManager] §cIndex du serveur = -1 : impossible de sauvegarder une parcelle !");
			return;
		}

		try {
			int id = plot.getId().getId();

			//update plot datas
			PreparedStatement updPlotParams = osUpdatePlotDatas.createStatement();
			updPlotParams.setInt(1, serverIndex);
			updPlotParams.setInt(2, id);
			updPlotParams.setString(3, plot.getParameters().toJson());
			osUpdatePlotDatas.executeUpdate(updPlotParams);
			updPlotParams.close();

			//update plot members
			for (Entry<MemberInformations, PlotRank> e : plot.getMembers().getList().entrySet())
				if (e.getValue() == PlotRank.VISITOR) {
					PreparedStatement delPlotMember = osDeletePlayerPlotRank.createStatement();
					delPlotMember.setInt(1, serverIndex);
					delPlotMember.setInt(2, id);
					delPlotMember.setLong(3, e.getKey().getId());
					osDeletePlayerPlotRank.executeUpdate(delPlotMember);
					delPlotMember.close();
				} else {
					PreparedStatement updPlotMember = osUpdatePlayerPlotRank.createStatement();
					updPlotMember.setInt(1, serverIndex);
					updPlotMember.setInt(2, id);
					updPlotMember.setLong(3, e.getKey().getId());
					updPlotMember.setString(4, e.getKey().getName());
					updPlotMember.setString(5, e.getKey().getUUID().toString());
					updPlotMember.setInt(6, e.getValue().getLevel());
					osUpdatePlayerPlotRank.executeUpdate(updPlotMember);
					updPlotMember.close();
				}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "§4Failed to save plot " + plot + " !");
			e.printStackTrace();
		}
	}

	public synchronized int getPlotsCount() {
		if (serverIndex == -1) {
			plugin.getLogger().log(Level.WARNING, "§4[DataManager] §cIndex du serveur = -1 : impossible de récupérer le nombre de parcelles !");
			return 0;
		}

		try {
			PreparedStatement ps = osCountPlots.createStatement();
			ps.setInt(1, serverIndex);
			ResultSet result = osCountPlots.executeQuery(ps);
			result.next();

			PreparedStatement ps2 = osCountPlots2.createStatement();
			ps2.setInt(1, serverIndex);
			ResultSet result2 = osCountPlots2.executeQuery(ps2);
			
			result2.next(); // what ? -> sert à récupérer le nombre de lignes de la bdd (regarde la requête)
			int finalResult = result.getInt(1);
			
			result.close();
			ps.close();
			result2.close();
			ps2.close();
			return finalResult;
		} catch (SQLException e) {
			e.printStackTrace();

			return 0;
		}
	}

	public synchronized void saveSchemToDb(OlympaPlayerCreatif p, Plot plot, File schem) {
		if (serverIndex == -1) {
			plugin.getLogger().log(Level.WARNING, "§4[DataManager] §cIndex du serveur = -1 : impossible de sauvegarder une parcelle en schematic !");
			return;
		}

		try {
			PreparedStatement ps = osUpdatePlotSchem.createStatement();

			ps.setInt(1, serverIndex);
			ps.setInt(2, plot.getId().getId());
			ps.setLong(3, p.getId());
			ps.setString(4, schem.getName());
			ps.setBlob(5, new FileInputStream(schem));
			osUpdatePlotSchem.executeUpdate(ps);
			ps.close();

		} catch (SQLException | FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public synchronized Blob loadSchemFromDb(OlympaPlayerCreatif p, Plot plot) {
		if (serverIndex == -1) {
			plugin.getLogger().warning("§4[DataManager] §cIndex du serveur = -1 : impossible de charger un schematic depuis la bdd !");
			return null;
		}
		
		PreparedStatement ps;
		try {
			ps = osSelectPlotSchem.createStatement();
			ps.setInt(1, serverIndex);
			ps.setInt(2, plot.getId().getId());
			ResultSet result = ps.executeQuery();
			ps.close();
			
			Blob blob = null;
			
			if (result.next()) 
				blob = result.getBlob("schem_data");
			
			result.close();
			return blob;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void updateWithServerIndex(int i) {
		serverIndex = i;

		this.getPlotsCount();
		if (getPlotsCount() == -1) {
			Bukkit.getServer().shutdown();
			throw new UnsupportedOperationException("§4ATTENTION problème dans la table creatif_plotsdata : nombre d'entrées différent de l'indice du plot maximal !! §cArrêt du serveur.");
		}

		//actions exécutées à la fin du chargement du serveur
		new BukkitRunnable() {
			@Override
			public void run() {
				plugin.getWorldManager().defineWorldParams();	
				plugin.getWorldManager().loadCustomWorldGenerator();
				plugin.getPlotsManager().loadHelpHolos();
			}
		}.runTask(plugin);
		
		
		try {
			PreparedStatement ps = osSelectServerParams.createStatement();
			ps.setInt(1, serverIndex);

			ResultSet result = osSelectServerParams.executeQuery(ps);

			if (result.next()) {
				OCparam.initFromJson(result.getString("server_params"));
				plugin.getLogger().info("§aParamètres chargés pour le serveur créatif " + serverIndex);
			}else {
				plugin.getLogger().warning("§ePas de paramètres existant pour le serveur " + serverIndex + ". Création des paramètres par défaut. Le serveur va s'arrêter.");
				new BukkitRunnable() {
					@Override
					public void run() {
						Bukkit.getServer().shutdown();
					}
				}.runTaskLater(plugin, 1);
			}
			result.close();
			ps.close();

			PreparedStatement ps2 = osUpdateServerParams.createStatement();
			ps2.setInt(1, serverIndex);
			ps2.setString(2, OCparam.toJson());
			osUpdateServerParams.executeUpdate(ps2);
			ps2.close();


			plugin.getLogger().info("§aINDEX DU SERVEUR CREATIF : " + serverIndex + "§7 - Nombre de parcelles : " + getPlotsCount() + " - Taille parcelles : " + OCparam.PLOT_SIZE.get() + "*" + OCparam.PLOT_SIZE.get());

		} catch (SQLException e) {
			e.printStackTrace();
		}

		//load plot 1
		//loadPlot(PlotId.fromId(plugin, 1), false);
	}

	public int getServerIndex() {
		return serverIndex;
	}
}
