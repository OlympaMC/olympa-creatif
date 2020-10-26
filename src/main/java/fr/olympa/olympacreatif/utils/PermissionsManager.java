package fr.olympa.olympacreatif.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.PermissionsList;


@SuppressWarnings("unchecked")
public class PermissionsManager implements Listener{
	
	private OlympaCreatifMain plugin;
    YamlConfiguration config = new YamlConfiguration();
	
	public PermissionsManager(OlympaCreatifMain plugin) {
		
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
		//chargement des tags depuis le fichier config
        File file = new File(plugin.getDataFolder(), "permissions.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource("permissions.yml", false);
         }
        
        //YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(OlympaCreatifMain.getInstance().getDataFolder(), "tags.yml"));
        try {
			config.load(file);
		} catch (IOException | InvalidConfigurationException e1) {
			e1.printStackTrace();
			return;
		}
        
		//FileConfiguration config = YamlConfiguration.loadConfiguration(new File(OlympaCreatifMain.getInstance().getDataFolder(), "permissions.yml"));

        //System.out.println("cb perms : " + config.getList("cb_perms"));
        //System.out.println("we perms : " + config.getList("we_perms"));
		OlympaGroup.PLAYER.runtimePermissions.addAll((List<String>) config.getList("cb_perms"));
		PermissionsList.USE_WORLD_EDIT.getMinGroup().runtimePermissions.addAll((List<String>) config.getList("we_perms"));

        plugin.getLogger().log(Level.INFO, "§aWorldEdit & vanilla perms successfully respectively added to " + OlympaGroup.PLAYER + " and " + PermissionsList.USE_WORLD_EDIT.getMinGroup());
	}
	
	public void removeCbPerms() {
		OlympaGroup.PLAYER.runtimePermissions.removeAll((List<String>) config.getList("cb_perms"));
		Bukkit.getOnlinePlayers().forEach(
				p -> p.getEffectivePermissions().stream().filter(
				perms -> perms.getAttachment() != null).forEach(
				perms -> config.getList("cb_perms").forEach(
				perm -> perms.getAttachment().unsetPermission((String) perm))));
	}
	
	public void removeWePerms() {
		PermissionsList.USE_WORLD_EDIT.getMinGroup().runtimePermissions.removeAll((List<String>) config.getList("we_perms"));
		Bukkit.getOnlinePlayers().forEach(
				p -> p.getEffectivePermissions().stream().filter(
				perms -> perms.getAttachment() != null).forEach(
				perms -> config.getList("we_perms").forEach(
				perm -> perms.getAttachment().unsetPermission((String) perm))));
	}
}















