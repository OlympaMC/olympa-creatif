package fr.olympa.olympacreatif.commands;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.gui.MainGui;
import fr.olympa.olympacreatif.gui.ShopGui;

public class TpfCommand extends OlympaCommand {

	private OlympaCreatifMain plugin;
	
	public TpfCommand(OlympaCreatifMain plugin) {
		super(plugin, "tpf", "Se téléporter à un joueur", PermissionsList.CREA_TPF, new String[] {});
		this.plugin = plugin;
		allowConsole = false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player target = args.length > 0 ? Bukkit.getPlayer(args[0]) : null;
		
		if (target == null) {
			OCmsg.PLAYER_TARGET_OFFLINE.send(getSender(), "none");
			return false;
		}
		
		getPlayer().teleport(target);
		getPlayer().sendMessage("§aTéléportation à " + target.getName() + " en cours...");
		
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return Bukkit.getOnlinePlayers().stream().filter(p -> p.getName().startsWith(args[0])).map(p -> p.getName()).collect(Collectors.toList());
	}

}