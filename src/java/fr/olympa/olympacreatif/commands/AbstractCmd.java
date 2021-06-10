package fr.olympa.olympacreatif.commands;

import fr.olympa.api.common.permission.OlympaSpigotPermission;
import fr.olympa.api.spigot.command.ComplexCommand;
import fr.olympa.olympacreatif.OlympaCreatifMain;

public abstract class AbstractCmd extends ComplexCommand {

	protected OlympaCreatifMain plugin;

	public AbstractCmd(OlympaCreatifMain plugin, String command, OlympaSpigotPermission permission, String desc) {
		this(plugin, command, permission, desc, new String[] {});
	}

	public AbstractCmd(OlympaCreatifMain plugin, String command, OlympaSpigotPermission permission, String desc, String... aliases) {
		super(plugin, command, desc, permission, aliases);
		this.plugin = plugin;
	}
}
