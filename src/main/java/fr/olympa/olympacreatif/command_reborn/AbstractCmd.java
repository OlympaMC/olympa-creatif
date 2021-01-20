package fr.olympa.olympacreatif.command_reborn;

import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;

public abstract class AbstractCmd extends ComplexCommand {

	protected OlympaCreatifMain plugin;
	
	public AbstractCmd(OlympaCreatifMain plugin, String command, OlympaPermission permission, String desc) {
		super(plugin, command, desc, permission);
		this.plugin = plugin;
	}
}
