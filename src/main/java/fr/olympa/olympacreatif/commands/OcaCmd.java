package fr.olympa.olympacreatif.commands;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.gui.MainGui;
import fr.olympa.olympacreatif.gui.StaffGui;
import fr.olympa.olympacreatif.utils.NBTcontrollerUtil;

public class OcaCmd extends AbstractCmd {

	public OcaCmd(OlympaCreatifMain plugin) {
		super(plugin, "oca", PermissionsList.STAFF_OCA_CMD, "Panel de gestion du staff.");
		// TODO Auto-genocaerated constructor stub
	}

	
	@Cmd(player = true, syntax = "Ouvrir le menu staff du Créatif")
	public void menu(CommandContext cmd) {
		new StaffGui(MainGui.getMainGui(getOlympaPlayer())).create(getPlayer());
	}

	
	@Cmd(syntax = "Afficher la liste des parcelles actuellement chargées", args = "PLAYERS")
	public void plotslist(CommandContext cmd) {
		if (cmd.getArgumentsLength() == 0)
			plugin.getCmdLogic().sendPlotsList(getOlympaPlayer(), null);
		else
			plugin.getCmdLogic().sendPlotsList(getOlympaPlayer(), cmd.getArgument(0));
	}

	@Cmd(syntax = "Active l'un des composants du créatif", args = {"worldedit|commandblocks"}, min = 1)
	public void activate(CommandContext cmd) {
		switch(cmd.getArgument(0).toString()) {
		case "worldedit":
			plugin.getWEManager().setWeActivationState(true);
			break;
		
		case "commandblocks":
			plugin.getCommandBlocksManager().setCbActivationState(true);
			break;
			
		default:
			OCmsg.STAFF_ACTIVATE_COMPONENT.send(getPlayer(), "aucun");
			return;
		}

		OCmsg.STAFF_ACTIVATE_COMPONENT.send(getPlayer(), (String) cmd.getArgument(0));
	}

	@Cmd(syntax = "Désactive l'un des composants du créatif", args = {"worldedit|commandblocks"}, min = 1)
	public void deactivate(CommandContext cmd) {
		switch(cmd.getArgument(0).toString()) {
		case "worldedit":
			plugin.getWEManager().setWeActivationState(false);
			break;
		
		case "commandblocks":
			plugin.getCommandBlocksManager().setCbActivationState(false);
			break;
			
		default:
			OCmsg.STAFF_DEACTIVATE_COMPONENT.send(getPlayer(), "aucun");
			return;
		}

		OCmsg.STAFF_DEACTIVATE_COMPONENT.send(getPlayer(), (String) cmd.getArgument(0));
	}
	
	
	
}
