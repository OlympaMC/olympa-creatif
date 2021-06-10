package fr.olympa.olympacreatif.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OcPermissions;

public class HatCommand extends OlympaCommand {

	public HatCommand(Plugin plugin) {
		super(plugin, "hat", OcPermissions.CREA_HAT_COMMAND, new String[] {});
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return false;

		getPlayer().getInventory().setHelmet(getPlayer().getInventory().getItemInMainHand());
		OCmsg.HAT_SET.send(getPlayer());

		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

}
