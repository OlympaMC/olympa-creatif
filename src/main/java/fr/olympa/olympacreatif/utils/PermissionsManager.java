package fr.olympa.olympacreatif.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.PermissionsList;

public class PermissionsManager {
	
	@SuppressWarnings("unchecked")
	public PermissionsManager() {
		
		//chargement des tags depuis le fichier config
		
		//chargement des tags depuis le fichier config
        File file = new File(OlympaCreatifMain.getInstance().getDataFolder(), "permissions.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            OlympaCreatifMain.getInstance().saveResource("permissions.yml", false);
         }
        
        //YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(OlympaCreatifMain.getInstance().getDataFolder(), "tags.yml"));
        YamlConfiguration config = new YamlConfiguration();
        try {
			config.load(file);
		} catch (IOException | InvalidConfigurationException e1) {
			e1.printStackTrace();
			return;
		}
        
		//FileConfiguration config = YamlConfiguration.loadConfiguration(new File(OlympaCreatifMain.getInstance().getDataFolder(), "permissions.yml"));

		OlympaGroup.PLAYER.runtimePermissions.addAll((List<String>) config.getList("cb_perms"));
		PermissionsList.USE_WORLD_EDIT.getMinGroup().runtimePermissions.addAll((List<String>) config.getList("we_perms"));
	}
	
	/*
	public PermissionAttachment addWePerms(Player p) {
		PermissionAttachment perms = p.addAttachment(plugin);
		listWe.forEach(perm -> perms.setPermission(perm, true));
		
		return perms;
	}
	
	public PermissionAttachment addCbPerms(Player p) {
		PermissionAttachment perms = p.addAttachment(plugin);
		listCb.forEach(perm -> perms.setPermission(perm, true));
		
		return perms;
	}*/
}















