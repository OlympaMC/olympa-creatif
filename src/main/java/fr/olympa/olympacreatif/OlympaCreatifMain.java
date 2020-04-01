package fr.olympa.olympacreatif;

import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.olympacreatif.world.WorldManager;

public class OlympaCreatifMain extends OlympaAPIPlugin {
	
	private WorldManager worldManager;
	
	public final String worldName = "OLYMPA_CREATIF";
	public final int worldLevel = 60;
	public final int plotXwidth = 300;
	public final int plotZwidth = 300;
	public final int roadWidth = 10;
	public final int plotMaxCount = 100;
	public final String logPrefix = "[" + getName()+ "] ";
	public final String prefix = "§2[§eO§6Crea§2] §a";

	//private OlympaStatement statement = new OlympaStatement("SELECT * FROM xxx WHERE xx = ?");
	public void onEnable() {
		//génération de la config de base
		saveDefaultConfig();
		
		worldManager = new WorldManager(this);
		
		/*try {
			//OlympaCore.getInstance().getDatabase();
			PreparedStatement preparedStatement = statement.getStatement();
			preparedStatement.setString(1, "xxxxx");
			ResultSet resultSet = preparedStatement.executeQuery();
			resultSet.close();
		}catch (SQLException e) {
			e.printStackTrace();
		}*/
	}

	public WorldManager getWorldManager() {
		return worldManager;
	}
}
