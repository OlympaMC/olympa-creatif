package fr.olympa.olympacreatif.commandblocks.commands;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdSetblock extends CbCommand {

	public CmdSetblock(CommandType cmdType, CommandSender sender, Location sendingLoc, OlympaCreatifMain plugin,
			Plot plot, String[] commandString) {
		super(cmdType, sender, sendingLoc, plugin, plot, commandString);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int execute() {
		if (args.length < 4)
			return 0;
		
		Location placingLoc = getLocation(args[0], args[1], args[2]);
		
		Material mat = null;
		
		if (args[3].split(":").length == 2)
			mat = Material.getMaterial(args[3].split(":")[1].toUpperCase());
		else
			mat = Material.getMaterial(args[3].toUpperCase());
		
		if (placingLoc == null || mat == null)
			return 0;
		
		plugin.getWorldManager().getWorld().getBlockAt(placingLoc).setType(mat);
		
		return 1;
	}
}
