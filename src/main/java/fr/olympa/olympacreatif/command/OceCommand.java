package fr.olympa.olympacreatif.command;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.worldedit.ClipboardEdition.SymmetryPlan;
import fr.olympa.olympacreatif.worldedit.WorldEditManager.WorldEditError;

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

		if (!plugin.getPlotsManager().isPlayerLoaded(p)) {
			p.sendMessage("§4Chargement des données en cours, commande annulée...");
			return false;	
		}
		
		if (!AccountProvider.get(p.getUniqueId()).hasPermission(PermissionsList.USE_WORLD_EDIT)) {
			p.sendMessage(Message.WE_INSUFFICIENT_PERMISSION.getValue());
			return false;
		}
		
		WorldEditError err = null;
		
		switch(args.length) {
		case 1:
			switch(args[0]) {
			case "copy":
				err = plugin.getWorldEditManager().getPlayerInstance(p).copySelection();
				if (err == WorldEditError.NO_ERROR)
					p.sendMessage(Message.WE_CMD_COPY_SUCCESS.getValue());
				else
					p.sendMessage(err.getErrorMessage().getValue());
			case "paste":
				err = plugin.getWorldEditManager().getPlayerInstance(p).pasteSelection();
				if (err == WorldEditError.NO_ERROR)
					p.sendMessage(Message.WE_CMD_PASTE_SUCCESS.getValue());
				else
					p.sendMessage(err.getErrorMessage().getValue());
			case "undo":
				err = plugin.getWorldEditManager().getPlayerInstance(p).executeUndo();
				if (err == WorldEditError.NO_ERROR)
					p.sendMessage(Message.WE_UNDO_SUCCESS.getValue());
				else
					p.sendMessage(err.getErrorMessage().getValue());
			case "cut":
				err = plugin.getWorldEditManager().getPlayerInstance(p).cutBlocks();
				if (err == WorldEditError.NO_ERROR)
					p.sendMessage(Message.WE_CUT_SUCCESS.getValue());
				else
					p.sendMessage(err.getErrorMessage().getValue());
			default:
				sender.sendMessage(Message.WE_CMD_HELP.getValue());
				break;
			}
			break;
		case 2:
			switch (args[0]) {
			case "set":
				err = plugin.getWorldEditManager().getPlayerInstance(p).setRandomBlocks(args[1]);
				if (err == WorldEditError.NO_ERROR)
					p.sendMessage(Message.WE_CMD_SET_SUCCESS.getValue());
				else
					p.sendMessage(err.getErrorMessage().getValue());
				break;
			case "miror":
				err = plugin.getWorldEditManager().getPlayerInstance(p).symetricSelection(args[1]);
				if (err == WorldEditError.NO_ERROR)
					p.sendMessage(Message.WE_CMD_MIROR_SUCCESS.getValue());
				else
					p.sendMessage(err.getErrorMessage().getValue());
			default:
				sender.sendMessage(Message.WE_CMD_HELP.getValue());
				break;
			}
			break;
		case 3:
			switch (args[0]) {
			case "rotate":
				err = plugin.getWorldEditManager().getPlayerInstance(p).rotateSelection(args[1], args[2]);
				if (err == WorldEditError.NO_ERROR)
					p.sendMessage(Message.WE_CMD_ROTATE_SUCCESS.getValue());
				else
					p.sendMessage(err.getErrorMessage().getValue());
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
			list.add("cut");
			list.add("set");
			for (String s : list)
				if (s.startsWith(args[0]))
					response.add(s);
			return response;
		}
		return null;
	}

	
}
