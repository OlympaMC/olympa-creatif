package fr.olympa.olympacreatif.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.gui.MainGui;

public class MenuCommand extends OlympaCommand {

	private OlympaCreatifMain plugin;
	
	public MenuCommand(OlympaCreatifMain plugin) {
		super(plugin, "menu", "Obtenir la tête d'un joueur", null, new String[] {"m"});
		this.plugin = plugin;
		allowConsole = false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		MainGui.getMainGui(getOlympaPlayer()).create(getPlayer());
		return false;		
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
