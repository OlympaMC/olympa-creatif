package fr.olympa.olympacreatif.commands;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OcPermissions;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.plot.PlotPerm;

public class SkullCommand extends OlympaCommand {

	private OlympaCreatifMain plugin;
	
	public SkullCommand(OlympaCreatifMain plugin) {
		super(plugin, "sk", "Obtenir la tête d'un joueur", OcPermissions.SKULL_COMMAND, new String[] {"skull"});
		this.plugin = plugin;
		allowConsole = false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!PlotPerm.BUILD.has((OlympaPlayerCreatif) getOlympaPlayer())) {
			OCmsg.INSUFFICIENT_PLOT_PERMISSION.send((OlympaPlayerCreatif) getOlympaPlayer(), PlotPerm.BUILD);
			return false;	
		}
		
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
