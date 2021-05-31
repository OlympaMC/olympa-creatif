package fr.olympa.olympacreatif.commands;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OcPermissions;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.plot.PlotPerm;

public class MicroblockCommand extends OlympaCommand {

	private OlympaCreatifMain plugin;
	
	public MicroblockCommand(OlympaCreatifMain plugin) {
		super(plugin, "mb", "Ouvrir le menu des microblocks", OcPermissions.MICRO_BLOCKS_COMMAND, new String[] {"mbs", "microblock", "microblocks"});
		this.plugin = plugin;
		allowConsole = false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!PlotPerm.BUILD.has((OlympaPlayerCreatif) getOlympaPlayer())) {
			OCmsg.INSUFFICIENT_PLOT_PERMISSION.send((OlympaPlayerCreatif)getOlympaPlayer(), PlotPerm.BUILD);
			return false;	
		}
		
		if (args.length == 0)
			plugin.getPerksManager().getMicroBlocks().openGui(getPlayer());
		else if (plugin.getPerksManager().getMicroBlocks().getMb(args[0]) != null)
			getPlayer().getInventory().addItem(plugin.getPerksManager().getMicroBlocks().getMb(args[0]));
		else
			OCmsg.OCO_UNKNOWN_MB.send(getPlayer(), args[0]);
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return plugin.getPerksManager().getMicroBlocks().getAllMbs().keySet().stream().filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
	}
}
