package fr.olympa.olympacreatif.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.gui.MainGui;
import fr.olympa.olympacreatif.gui.StaffGui;
import fr.olympa.olympacreatif.plot.Plot;

public class OcaCommand extends OlympaCommand {

	public static final List<String> subArgsList = ImmutableList.<String>builder()
			.add("menu")
			.add("listplots")
			.build();

	private OlympaCreatifMain plugin;
	
	public OcaCommand(OlympaCreatifMain plugin, String cmd, String[] args) {
		super(plugin, cmd, args);
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return false;

		args = OcCommand.updatedArgs(label, "oca", args);

		Player p = (Player) sender;
		
		switch (args[0]) {
		case "menu":
			new StaffGui(MainGui.getMainGui(AccountProvider.get(p.getUniqueId()))).create(p);
			break;
			
		case "listplots":
			String lp = "";
			List<Plot> plots = new ArrayList<Plot>(plugin.getPlotsManager().getPlots());
			Collections.sort(plots, new Comparator<Plot>() {
				@Override
				public int compare(Plot o1, Plot o2) {
					return o1.getPlotId().getId() - o2.getPlotId().getId(); 
				}
			});
			
			for (Plot plot : plots)
				lp += plot + " ";
			
			p.sendMessage("§aListe des parcelles actuellement chargées : " + lp);
			break;
		}
		
		
		
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return subArgsList;
	}

}
