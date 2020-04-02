package fr.olympa.olympacreatif;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.olympacreatif.objects.Plot;
import fr.olympa.olympacreatif.world.WorldManager;
import fr.olympa.olympacreatif.worldedit.WorldEditManager;

public class OlympaCreatifMain extends OlympaAPIPlugin {
	
	private WorldManager creativeWorldManager;
	private WorldEditManager worldEditManager;
	public final String worldName = "OLYMPA_CREATIF";
	
	public final int worldLevel = 3;
	public final int plotXwidth = 300;
	public final int plotZwidth = 300;
	public final int roadWidth = 10;
	public final int maxWorldEditBlocks = 1000;
	
	public final String logPrefix = "[" + getName()+ "] ";
	public final String prefix = "§2[§eO§6Crea§2] §a";

	private List<Plot> plots = new ArrayList<Plot>();
	
	//private OlympaStatement statement = new OlympaStatement("SELECT * FROM xxx WHERE xx = ?");
	public void onEnable() {
		//génération de la config de base
		super.onEnable();
		saveDefaultConfig();
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
	
	public void addPlot(Plot plot) {
		plots.add(plot);
	}
	
	public List<Plot> getPlots(){
		return Collections.unmodifiableList(plots);
	}
	
	public Plot getPlot(Location loc) {
		for (Plot p : getPlots())
			if (p.getArea().isInPlot(loc))
				return p;
		return null;
	}
}
