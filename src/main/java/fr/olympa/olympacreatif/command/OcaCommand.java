package fr.olympa.olympacreatif.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.PermissionsList;

public class OcaCommand extends OlympaCommand {

	private OlympaCreatifMain plugin;
	
	public OcaCommand(OlympaCreatifMain plugin, String cmd, String[] args) {
		super(plugin, cmd, args);
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return false;
		
		if (!PermissionsList.STAFF_ENABLE_ADMIN_MODE.hasPermission(((Player)sender).getUniqueId()))
			return false;
		
		if (args.length == 1)
			switch(args[0]) {
			case "on":
				((OlympaPlayerCreatif) AccountProvider.get(player.getUniqueId())).setAdminMode(true);
				sender.sendMessage("§cOlympaCréatif : mode admin activé.");
				return false;
			case "off":
				((OlympaPlayerCreatif) AccountProvider.get(player.getUniqueId())).setAdminMode(false);
				sender.sendMessage("§aOlympaCréatif : mode admin désactivé.");
				return false;
			}

		sender.sendMessage("§cUtilisation du mode admin : /oca <on|off>");
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> list = new ArrayList<String>();
		
		if ("on".startsWith(args[0]))
			list.add("on");

		if ("off".startsWith(args[0]))
			list.add("off");
		
		return list;
	}

}
