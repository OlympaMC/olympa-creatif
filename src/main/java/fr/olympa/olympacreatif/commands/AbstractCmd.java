package fr.olympa.olympacreatif.commands;

import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.permission.OlympaSpigotPermission;
import fr.olympa.olympacreatif.OlympaCreatifMain;

public abstract class AbstractCmd extends ComplexCommand {

	protected OlympaCreatifMain plugin;
	
	public AbstractCmd(OlympaCreatifMain plugin, String command, OlympaSpigotPermission permission, String desc) {
		super(plugin, command, desc, permission);
		this.plugin = plugin;
	}
}
