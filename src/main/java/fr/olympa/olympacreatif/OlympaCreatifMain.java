package fr.olympa.olympacreatif;

import java.util.Random;

import org.bukkit.generator.ChunkGenerator;

import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.olympacreatif.command.OcCommand;
import fr.olympa.olympacreatif.data.DataManager;
import fr.olympa.olympacreatif.plot.PlotsManager;
import fr.olympa.olympacreatif.world.CustomChunkGenerator;
import fr.olympa.olympacreatif.world.WorldManager;
import fr.olympa.olympacreatif.worldedit.WorldEditManager;

public class OlympaCreatifMain extends OlympaAPIPlugin {
	
	private WorldManager creativeWorldManager;
	private WorldEditManager worldEditManager;
	private DataManager dataManager;
	private PlotsManager plotsManager;
	
	public Random random = new Random();
	
	@Override //retourne le générateur de chunks custom
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
		return new CustomChunkGenerator(this);
	}
	
	//private OlympaStatement statement = new OlympaStatement("SELECT * FROM xxx WHERE xx = ?");
	public void onEnable() {
		//génération de la config de base
		super.onEnable();
		//saveDefaultConfig();
		new OcCommand(this, "olympacreatif", new String[] {"oc"}).register();
		
		dataManager = new DataManager(this);
		plotsManager = new PlotsManager(this);
		creativeWorldManager = new WorldManager(this);
		worldEditManager = new WorldEditManager(this);
		
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
		return creativeWorldManager;
	}
	
	public WorldEditManager getWorldEditManager() {
		return worldEditManager;
	}
	
	public PlotsManager getPlotsManager() {
		return plotsManager;
	}
	
	public DataManager getDataManager() {
		return dataManager;
	}
}
