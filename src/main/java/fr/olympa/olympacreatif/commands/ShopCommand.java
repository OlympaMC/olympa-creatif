package fr.olympa.olympacreatif.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.gui.MainGui;
import fr.olympa.olympacreatif.gui.ShopGui;

public class ShopCommand extends OlympaCommand {

	private OlympaCreatifMain plugin;
	
	public ShopCommand(OlympaCreatifMain plugin) {
		super(plugin, "shop", "Ouvrir le magasin", null, new String[] {});
		this.plugin = plugin;
		allowConsole = false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		new ShopGui(MainGui.getMainGui(getOlympaPlayer())).create(getPlayer());
		return false;		
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
