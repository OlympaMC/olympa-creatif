package fr.olympa.olympacreatif;

import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.sql.DbConnection;
import fr.olympa.api.sql.DbCredentials;

public class OlympaCreatif extends OlympaAPIPlugin {
	
	public void onEnable() {
		//génération de la config de base
		saveDefaultConfig();
		
	}
}
