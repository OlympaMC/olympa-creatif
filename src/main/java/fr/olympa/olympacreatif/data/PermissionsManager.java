package fr.olympa.olympacreatif.data;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.olympacreatif.OlympaCreatifMain;



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
        
        try {
			config.load(file);
		} catch (IOException | InvalidConfigurationException e1) {
			e1.printStackTrace();
			return;
		}
        
		//FileConfiguration config = YamlConfiguration.loadConfiguration(new File(OlympaCreatifMain.getInstance().getDataFolder(), "permissions.yml"));

        //System.out.println("cb perms : " + config.getList("cb_perms"));
        //System.out.println("we perms : " + config.getList("we_perms"));
		config.getStringList("cb_perms").stream().forEach(OlympaGroup.PLAYER::setRuntimePermission);
		config.getStringList("we_perms").stream().forEach(PermissionsList.USE_WORLD_EDIT.getMinGroup()::setRuntimePermission);

        plugin.getLogger().log(Level.INFO, "§aVanilla & WorldEdit permissions have been successfully added to " + OlympaGroup.PLAYER + " and " + PermissionsList.USE_WORLD_EDIT.getMinGroup() + " groups.");
	}
	
	public void removeCbPerms() {
		config.getStringList("cb_perms").stream().forEach(OlympaGroup.PLAYER::unsetRuntimePermission);
		Bukkit.getOnlinePlayers().forEach(
				p -> p.getEffectivePermissions().stream().filter(
				perms -> perms.getAttachment() != null).forEach(
								perms -> config.getStringList("cb_perms").forEach(perm -> perms.getAttachment().unsetPermission(perm))));
	}
	
	public void removeWePerms() {
		config.getStringList("we_perms").stream().forEach(PermissionsList.USE_WORLD_EDIT.getMinGroup()::unsetRuntimePermission);
		Bukkit.getOnlinePlayers().forEach(
				p -> p.getEffectivePermissions().stream().filter(
				perms -> perms.getAttachment() != null).forEach(
								perms -> config.getStringList("we_perms").forEach(perm -> perms.getAttachment().unsetPermission(perm))));
	}
}















