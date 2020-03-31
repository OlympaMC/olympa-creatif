package fr.olympa.olympacreatif;

import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.sql.DbConnection;
import fr.olympa.api.sql.DbCredentials;
import fr.olympa.world.WorldManager;

public class OlympaCreatifMain extends OlympaAPIPlugin {
	
	private DatabaseConnection sqlConnection;
	private WorldManager worldManager;
	
	public final String worldName = "OLYMPA_CREATIF";
	public final int worldLevel = 60;
	public final int plotXwidth = 300;
	public final int plotZwidth = 300;
	public final int roadWidth = 10;
	public final int plotMaxCount = 100;
	public final String logPrefix = "[" + getName()+ "] ";
	public final String prefix = "§2[§eO§6Crea§2] §a";
	
	public void onEnable() {
		//génération de la config de base
		saveDefaultConfig();
		
		sqlConnection = new DatabaseConnection(this.getConfig());
		worldManager = new WorldManager(this);
		
	}
	
	public DatabaseConnection getDatabase() {
		return sqlConnection;
	}
	public WorldManager getWorldManager() {
		return worldManager;
	}
}
