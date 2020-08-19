package fr.olympa.olympacreatif.command;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.gui.MainGui;
import fr.olympa.olympacreatif.gui.StaffGui;

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
		
		Player p = (Player) sender;
		new StaffGui(MainGui.getMainGui(p)).create(p);
		
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
