package fr.olympa.olympacreatif.command;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.worldedit.ClipboardEdition.SymmetryPlan;

public class OceCommand extends OlympaCommand{

	private OlympaCreatifMain plugin;
	
	public OceCommand(OlympaCreatifMain plugin, String command, String[] alias) {
		super(plugin, command, alias);
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return false;
		Player p = (Player) sender;
		
		switch(args.length) {
		case 1:
			switch(args[0]) {
			case "copy":
				if (plugin.getWorldEditManager().getPlayerInstance(p).isSelectionValid())
					if (plugin.getWorldEditManager().getPlayerInstance(p).copySelection())
						p.sendMessage(Message.WE_CMD_CLIPBOARD_COPIED.getValue());
					else
						p.sendMessage(Message.WE_ACTION_TOO_BIG.getValue());
				else
					p.sendMessage(Message.WE_CMD_INVALID_SELECTION.getValue());
				break;
			case "paste":
				if (!plugin.getWorldEditManager().getPlayerInstance(p).pasteSelection())
					p.sendMessage(Message.WE_CMD_PASTE_ERROR.getValue());
				break;
			case "undo":
				if (plugin.getWorldEditManager().getPlayerInstance(p).executeUndo())
					p.sendMessage(Message.WE_UNDO_QUEUED.getValue());
				else
					p.sendMessage(Message.WE_NO_UNDO_AVAILABLE.getValue());
				break;
			default:
				sender.sendMessage(Message.WE_CMD_HELP.getValue());
				break;
			}
			break;
		case 2:
			switch (args[0]) {
			case "miror":
				SymmetryPlan plan = SymmetryPlan.getPlan(args[1]);
				if (plan != null) {
					plugin.getWorldEditManager().getPlayerInstance(p).symetricSelection(plan);
					p.sendMessage(Message.WE_CMD_CLIPBOARD_MIROR.getValue());
				}
				else
					p.sendMessage(Message.WE_CMD_HELP.getValue());
				break;
			default:
				sender.sendMessage(Message.WE_CMD_HELP.getValue());
				break;
			}
			break;
		case 3:
			switch (args[0]) {
			case "rotate":
				SymmetryPlan plan = SymmetryPlan.getPlan(args[1]);
				if (!StringUtils.isNumeric(args[2]) || Integer.valueOf(args[2]) % 90 != 0 || plan == null) {
					sender.sendMessage(Message.WE_CMD_HELP.getValue());
					return false;	
				}
				
				switch (plan) {
				case AXE_X:
					plugin.getWorldEditManager().getPlayerInstance(p).rotateSelection(Integer.valueOf(args[2]), 0, 0);
					p.sendMessage(Message.WE_CMD_CLIPBOARD_ROTATE.getValue());
					break;
				case AXE_Y:
					plugin.getWorldEditManager().getPlayerInstance(p).rotateSelection(0, Integer.valueOf(args[2]), 0);
					p.sendMessage(Message.WE_CMD_CLIPBOARD_ROTATE.getValue());
					break;
				case AXE_Z:
					plugin.getWorldEditManager().getPlayerInstance(p).rotateSelection(0, 0, Integer.valueOf(args[2]));
					p.sendMessage(Message.WE_CMD_CLIPBOARD_ROTATE.getValue());
					break;
				default:
					sender.sendMessage(Message.WE_CMD_HELP.getValue());
					break;
				}
				break;
			default:
				sender.sendMessage(Message.WE_CMD_HELP.getValue());
				break;
			}
			break;
		default:
			sender.sendMessage(Message.WE_CMD_HELP.getValue());
			break;
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> list = new ArrayList<String>();
		List<String> response = new ArrayList<String>();
		if (args.length == 1) {
			list.add("copy");
			list.add("paste");
			list.add("miror");
			list.add("rotate");
			list.add("undo");
			for (String s : list)
				if (s.startsWith(args[0]))
					response.add(s);
			return response;
		}
		return null;
	}

	
}
