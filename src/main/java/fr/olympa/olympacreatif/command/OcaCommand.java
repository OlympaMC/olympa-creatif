package fr.olympa.olympacreatif.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
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
		
		OlympaPlayer p = AccountProvider.get(((Player)sender).getUniqueId());
		
		if (!p.hasPermission(PermissionsList.STAFF_ENABLE_ADMIN_MODE))
			return false;
		
		if (args.length == 1)
			switch(args[0]) {
			case "on":
				plugin.getPlotsManager().addAdminPlayer(p);
				sender.sendMessage("§cOlympaCréatif : mode admin activé.");
				return false;
			case "off":
				plugin.getPlotsManager().removeAdminPlayer(p);
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
