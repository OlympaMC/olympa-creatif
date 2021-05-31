package fr.olympa.olympacreatif.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OCparam;

public class SpawnCommand extends OlympaCommand {

	public SpawnCommand(OlympaCreatifMain plugin) {
		super(plugin, "spawn", "Se téléporter au spawn", null, new String[] {});
		allowConsole = false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		OCparam.SPAWN_LOC.get().teleport(getPlayer());
		OCmsg.TELEPORTED_TO_WORLD_SPAWN.send(getPlayer());
		
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	
}
