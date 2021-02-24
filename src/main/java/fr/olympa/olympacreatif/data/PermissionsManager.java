package fr.olympa.olympacreatif.data;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.olympacreatif.OlympaCreatifMain;



public class PermissionsManager implements Listener{
	
	private OlympaCreatifMain plugin;
    YamlConfiguration config = new YamlConfiguration();
    List<String> cbPerms;
    List<String> wePerms;
	
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
		cbPerms = config.getStringList("cb_perms");
		wePerms = config.getStringList("we_perms");
		
		//cbPerms.forEach(OlympaGroup.PLAYER::setRuntimePermission);
		//wePerms.forEach(PermissionsList.USE_WORLD_EDIT.getMinGroup()::setRuntimePermission);
		setCbPerms(true);
		setWePerms(true);

        //plugin.getLogger().log(Level.INFO, "§aVanilla & WorldEdit permissions have been successfully added to " + OlympaGroup.PLAYER + " and " + PermissionsList.USE_WORLD_EDIT.getMinGroup() + " groups.");
	}
	
	public void setWePerms(boolean givePerm) {
		if (givePerm)
			wePerms.forEach(PermissionsList.USE_WORLD_EDIT.getMinGroup()::setRuntimePermission);
		else
			wePerms.forEach(PermissionsList.USE_WORLD_EDIT.getMinGroup()::unsetRuntimePermission);
		
		recalculatePermissions();
		plugin.getLogger().log(Level.INFO, "§aWorldEdit permissions have been " + (givePerm ? "§2added §2to " : "§cremoved §afrom ") + PermissionsList.USE_WORLD_EDIT.getMinGroup() + " §agroup.");
	}
	
	public void setCbPerms(boolean givePerm) {
		if (givePerm)
			cbPerms.forEach(OlympaGroup.PLAYER::setRuntimePermission);
		else
			cbPerms.forEach(OlympaGroup.PLAYER::unsetRuntimePermission);
		
		recalculatePermissions();
		plugin.getLogger().log(Level.INFO, "§aVanilla permissions have been " + (givePerm ? "§2added §2to " : "§cremoved §afrom ") + OlympaGroup.PLAYER + " §agroup.");
	}
	
	private void recalculatePermissions() {		
		Bukkit.getOnlinePlayers().forEach(p -> {
			for (PermissionAttachmentInfo attachmentInfo : p.getEffectivePermissions())
				if (attachmentInfo.getAttachment() != null && attachmentInfo.getAttachment().getPlugin() == OlympaCore.getInstance())
					attachmentInfo.getAttachment().remove();

			PermissionAttachment attachment = p.addAttachment(OlympaCore.getInstance());
			
			AccountProvider.get(p.getUniqueId()).getGroup().getAllGroups().sorted(Comparator.comparing(OlympaGroup::getPower))
			.forEach(group -> group.runtimePermissions.forEach((key, value) -> attachment.setPermission(key, value)));
			
			p.recalculatePermissions();
			((CraftServer) Bukkit.getServer()).getHandle().getServer().getCommandDispatcher().a(((CraftPlayer) p).getHandle());
		});
	}
}















