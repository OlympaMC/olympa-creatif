package fr.olympa.olympacreatif.commands;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.PermissionsList;

public class SkullCommand extends OlympaCommand {

	private OlympaCreatifMain plugin;
	
	public SkullCommand(OlympaCreatifMain plugin) {
		super(plugin, "sk", "Obtenir la tête d'un joueur", PermissionsList.USE_SKULL_COMMAND, new String[] {"skull"});
		this.plugin = plugin;
		allowConsole = false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length != 1) {
			sendIncorrectSyntax("/skull <player>");
			return false;
		}
		Consumer<ItemStack> consumer = sk -> getPlayer().getInventory().addItem(sk);
		ItemUtils.skull(consumer, "§6Tête de " + args[0], args[0]);
		OCmsg.OCO_HEAD_GIVED.send(getPlayer(), args[0]);
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return Bukkit.getOnlinePlayers().stream().map(p -> p.getName()).filter(p -> p.startsWith(args[0])).collect(Collectors.toList());
	}

}
