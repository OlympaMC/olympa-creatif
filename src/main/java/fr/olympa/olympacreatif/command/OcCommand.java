package fr.olympa.olympacreatif.command;

import java.util.function.Predicate;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.olympacreatif.OlympaCreatifMain;

public class OcCommand extends ComplexCommand {

	private OlympaCreatifMain plugin;
	
	public OcCommand(OlympaCreatifMain plugin) {
		
		super(null, plugin, "olympacreative", "&2Commande principale du plugin. Tapez /oc help pour afficher l'aide.", null, new String[] {"oc"});
		
		this.plugin = plugin;
	}


	@Cmd(player = true, syntax = "Syntaxe : /oc find|info")
	public void gui(CommandContext cmd) {
		cmd.
	}
}
