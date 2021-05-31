package fr.olympa.olympacreatif.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;

public class CiCommand extends OlympaCommand {

	public CiCommand(OlympaCreatifMain plugin) {
		super(plugin, "ci", null, new String[]{});
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			((Player)sender).getInventory().clear();
			OCmsg.CI_COMMAND.send(sender);
		}
		
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	

}
