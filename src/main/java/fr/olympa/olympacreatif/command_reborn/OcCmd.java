package fr.olympa.olympacreatif.command_reborn;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.olympacreatif.OlympaCreatifMain;

public abstract class OcCmd extends ComplexCommand {

	protected OlympaCreatifMain plugin;
	
	public OcCmd(OlympaCreatifMain plugin, String command, OlympaPermission permission, String desc) {
		super(plugin, command, desc, permission);
		this.plugin = plugin;
	}
}
