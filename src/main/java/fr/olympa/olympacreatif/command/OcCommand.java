package fr.olympa.olympacreatif.command;

import java.util.function.Predicate;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.datas.Message;
import fr.olympa.olympacreatif.plot.Plot;

public class OcCommand extends ComplexCommand {

	private OlympaCreatifMain plugin;
	
	public OcCommand(OlympaCreatifMain plugin) {
		
		super(null, plugin, "olympacreative", "&2Commande principale du plugin. Tapez /oc help pour afficher l'aide.", null, new String[] {"oc"});
		
		this.plugin = plugin;
	}


	@SuppressWarnings("unused")
	@Cmd(player = true, min=1, syntax = "Syntaxe : /oc find|info")
	public void plotClaim(CommandContext cmd) {
		if (cmd.args[0].equals("find")) {
			int plotCount = 0;
			for (Plot p : plugin.getPlotsManager().getPlots()) {
				if (p.getMembers().getPlayerLevel(cmd.player) > 0)
					plotCount++;
			}
			
			//TODO limite d'obtention de plots
			if (true) {
				cmd.player.teleport(plugin.getPlotsManager().createPlot(cmd.player).getArea().getFirstCorner());
				cmd.player.sendMessage(Message.PLOT_NEW_CLAIM.getValue());
			}else {
				cmd.player.sendMessage(Message.MAX_PLOT_COUNT_REACHED.getValue());
			}
		}
	}
}
