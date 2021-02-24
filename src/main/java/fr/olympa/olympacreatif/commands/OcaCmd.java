package fr.olympa.olympacreatif.commands;

import java.util.HashMap;
import java.util.Map;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.utils.Prefix;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.gui.MainGui;
import fr.olympa.olympacreatif.gui.StaffGui;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotId;
import fr.olympa.olympacreatif.utils.NBTcontrollerUtil;

public class OcaCmd extends AbstractCmd {

	private Map<PlotId, String> plotsResetVerifCode = new HashMap<PlotId, String>();

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
	
	@Cmd(player = true, syntax = "Réinitialiser une parcelle", description = "/oca resetplot <plot> [confirmationCode]")
	public void resetplot(CommandContext cmd) {
		if (!PermissionsList.STAFF_RESET_PLOT.hasPermissionWithMsg(getOlympaPlayer()))
			return;
		
		Plot plot = cmd.getArgumentsLength() == 0 ? ((OlympaPlayerCreatif)getOlympaPlayer()).getCurrentPlot() : plugin.getPlotsManager().getPlot(PlotId.fromString(plugin, cmd.getArgument(0)));
		
		if (plot == null) {
			OCmsg.NULL_CURRENT_PLOT.send(getPlayer());
			return;
		}
		
		if (!plotsResetVerifCode.containsKey(plot.getPlotId())) {
			String check = "";
			for (int i = 0 ; i < 6 ; i++) check += (char) (plugin.random.nextInt(26) + 'a');
			
			plotsResetVerifCode.put(plot.getPlotId(), check);
			
			Prefix.DEFAULT.sendMessage(getPlayer(), "§dVeuillez saisir la commande /oca resetplot %s %s pour réinitialiser la parcelle %s (%s). \n§cAttention cette action est irréversible !!", plot.getPlotId(), check, plot.getPlotId(), plot.getMembers().getOwner().getName());
			
			plugin.getTask().runTaskLater(() -> plotsResetVerifCode.remove(plot.getPlotId()), 400);
			
		} else if (cmd.getArgumentsLength() != 2) {
			Prefix.DEFAULT.sendMessage(getPlayer(), "§dVeuillez saisir la commande /oca resetplot %s %s pour réinitialiser la parcelle %s (%s). \n§cAttention cette action est irréversible !!", plot.getPlotId(), plotsResetVerifCode.get(plot.getPlotId()), plot.getPlotId(), plot.getMembers().getOwner().getName());
			
		}else {			
			if (!plotsResetVerifCode.containsKey(plot.getPlotId()) || !plotsResetVerifCode.get(plot.getPlotId()).equals(cmd.getArgument(1))) {
				Prefix.DEFAULT.sendMessage(getPlayer(), "§dLe code renseigné n'est pas valide.");
				return;
			}
			
			plugin.getWEManager().resetPlot(getPlayer(), plot);
			plotsResetVerifCode.remove(plot.getPlotId());
			//Prefix.DEFAULT.sendMessage(getPlayer(), "§dLa parcelle %s (%s) va se réinitialiser.", plot.getPlotId(), plot.getMembers().getOwner().getName());
		}
	}
	
	
	
}
