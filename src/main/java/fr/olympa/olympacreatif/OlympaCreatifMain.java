package fr.olympa.olympacreatif;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;

import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.olympacreatif.objects.Plot;
import fr.olympa.olympacreatif.world.WorldManager;

public class OlympaCreatifMain extends OlympaAPIPlugin {
	
	private WorldManager worldManager;
	
	public final String worldName = "OLYMPA_CREATIF";
	
	public final int worldLevel = 3;
	public final int plotXwidth = 10;
	public final int plotZwidth = 10;
	public final int roadWidth = 3;
	public final int plotHalfRowMaxCount = 10;
	
	public final String logPrefix = "[" + getName()+ "] ";
	public final String prefix = "§2[§eO§6Crea§2] §a";

	private List<Plot> plots = new ArrayList<Plot>();
	
	//private OlympaStatement statement = new OlympaStatement("SELECT * FROM xxx WHERE xx = ?");
	public void onEnable() {
		//génération de la config de base
		super.onEnable();
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
	
	public void addPlot(Plot plot) {
		plots.add(plot);
	}
	
	public List<Plot> getPlots(){
		return Collections.unmodifiableList(plots);
	}
}
