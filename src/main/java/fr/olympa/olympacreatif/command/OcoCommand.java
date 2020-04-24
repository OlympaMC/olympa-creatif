package fr.olympa.olympacreatif.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.data.PermissionsList;

public class OcoCommand extends OlympaCommand {

	public OcoCommand(OlympaCreatifMain plugin, String cmd, String[] aliases) {
		super(plugin, cmd, aliases);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return false;
		
		OlympaPlayer p = AccountProvider.get(((Player)sender).getUniqueId());
		
		switch (args.length) {
		case 0:
			switch (args[0]) {
			case "hat":
				if (!p.hasPermission(PermissionsList.USE_HAT_COMMAND)) {
					p.getPlayer().sendMessage(Message.INSUFFICIENT_GROUP_PERMISSION.getValue().replace("%group%", PermissionsList.USE_SKULL_COMMAND.getGroup().getName(p.getGender())));
					return false;
				}
				p.getPlayer().getInventory().setHelmet(new ItemStack(p.getPlayer().getInventory().getItemInMainHand().getType()));
				break;
			default:
				sender.sendMessage(Message.OCO_COMMAND_HELP.getValue());
				break;
			}
			break;
		case 1:
			switch(args[0]) {
			case "skull":
				if (!p.hasPermission(PermissionsList.USE_SKULL_COMMAND)) {
					p.getPlayer().sendMessage(Message.INSUFFICIENT_GROUP_PERMISSION.getValue().replace("%group%", PermissionsList.USE_SKULL_COMMAND.getGroup().getName(p.getGender())));
					return false;
				}
				p.getPlayer().getInventory().addItem(ItemUtils.skull("", args[1]));
				break;
			default:
				sender.sendMessage(Message.OCO_COMMAND_HELP.getValue());
				break;
			}
		default:
			sender.sendMessage(Message.OCO_COMMAND_HELP.getValue());
			break;
		}
		
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> list = new ArrayList<String>();
		List<String> response = new ArrayList<String>();
		
		if (args.length == 1) {
			list.add("skull");
			list.add("hat");
			for (String s : list)
				if (s.startsWith(args[0]))
					response.add(s);
		}
		else
			for (Player p : Bukkit.getOnlinePlayers())
				response.add(p.getName());

		return response;
	}
}
