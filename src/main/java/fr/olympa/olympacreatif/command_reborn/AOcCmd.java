package fr.olympa.olympacreatif.command_reborn;

import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.olympacreatif.OlympaCreatifMain;

public abstract class AOcCmd extends ComplexCommand {

	protected OlympaCreatifMain plugin;
	
	public AOcCmd(OlympaCreatifMain plugin, String command, OlympaPermission permission, String desc) {
		super(plugin, command, desc, permission);
		this.plugin = plugin;
	}
}
