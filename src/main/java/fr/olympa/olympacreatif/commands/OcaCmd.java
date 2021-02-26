package fr.olympa.olympacreatif.commands;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.utils.Prefix;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.data.PermissionsManager.ComponentCreatif;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
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

	
	/*
	@Cmd(player = true, syntax = "Ouvrir le menu staff du Créatif")
	public void menu(CommandContext cmd) {
		new StaffGui(MainGui.getMainGui(getOlympaPlayer())).create(getPlayer());
	}*/

	
	@Cmd(syntax = "Afficher la liste des parcelles actuellement chargées", args = "PLAYERS")
	public void plotslist(CommandContext cmd) {
		if (cmd.getArgumentsLength() == 0)
			plugin.getCmdLogic().sendPlotsList(getOlympaPlayer(), null);
		else
			plugin.getCmdLogic().sendPlotsList(getOlympaPlayer(), cmd.getArgument(0));
	}

	@Cmd(syntax = "Active l'un des composants du créatif", args = {"worldedit|commandblocks|entities"}, min = 1)
	public void activate(CommandContext cmd) {
		if (!PermissionsList.STAFF_MANAGE_COMPONENT.hasPermissionWithMsg(getOlympaPlayer()))
			return;
		
		ComponentCreatif component = ComponentCreatif.fromString(cmd.getArgument(0));
		
		if (component != null)
			component.activate();

		OCmsg.STAFF_ACTIVATE_COMPONENT.send(getPlayer(), component == null ? "§caucun" : (String) cmd.getArgument(0));
	}

	@Cmd(syntax = "Désactive l'un des composants du créatif", args = {"worldedit|commandblocks|entities"}, min = 1)
	public void deactivate(CommandContext cmd) {
		if (!PermissionsList.STAFF_MANAGE_COMPONENT.hasPermissionWithMsg(getOlympaPlayer()))
			return;
		
		ComponentCreatif component = ComponentCreatif.fromString(cmd.getArgument(0));
		
		if (component != null)
			component.deactivate();

		OCmsg.STAFF_DEACTIVATE_COMPONENT.send(getPlayer(), component == null ? "§caucun" : (String) cmd.getArgument(0));
	}

	@Cmd(player = true, syntax = "Gérer ses permissions staff", description = "/oca perm <perm>", min = 1, 
			args = "ghost_mode|owner_everywhere|worldedit_everywhere|bypass_kick_ban")
	public void perm(CommandContext cmd) {
		StaffPerm perm = null;
		
		switch ((String) cmd.getArgument(0)) {
		case "ghost_mode":
			perm = StaffPerm.GHOST_MODE;
			break;

		case "owner_everywhere":
			perm = StaffPerm.OWNER_EVERYWHERE;
			break;

		case "worldedit_everywhere":
			perm = StaffPerm.WORLDEDIT_EVERYWHERE;
			break;

		case "bypass_kick_ban":
			perm = StaffPerm.BYPASS_KICK_BAN;
			break;
		default:
			sendIncorrectSyntax();
			return;
		}
		
		if (!perm.getOlympaPerm().hasPermissionWithMsg(getOlympaPlayer()))
			return;
		
		OlympaPlayerCreatif pc = ((OlympaPlayerCreatif)getOlympaPlayer());
		pc.toggleStaffPerm(perm);
		sendMessage(Prefix.DEFAULT_GOOD, "§eVotre permission %s est désormais %s§a.", perm.toString().toLowerCase(), pc.hasStaffPerm(perm) ? "§aactivée" : "§cdésactivée");
	}

	@Cmd(syntax = "Afficher les informations relatives au créatif", min = 1, args = "general|performances")
	public void info(CommandContext cmd) {
		
		if (cmd.getArgument(0).equals("general")) {

			sendMessage(Prefix.INFO, "§6>>> Informations générales Créatif " + plugin.getDataManager().getServerIndex());

			sendMessage(Prefix.INFO, "§7Etat des composants");
			sendHoverAndCommand(Prefix.INFO, "§eWorldedit activé : " + (ComponentCreatif.WORLDEDIT.isActivated() ? "§aOUI" : "§cNON"),
					"§7Cliquez ici pour changer la valeur", 
					"/oca " + (ComponentCreatif.WORLDEDIT.isActivated() ? "deactivate" : "activate") + " worldedit");
			sendHoverAndCommand(Prefix.INFO, "§eCommandblocks activés : " + (ComponentCreatif.COMMANDBLOCKS.isActivated() ? "§aOUI" : "§cNON"),
					"§7Cliquez ici pour changer la valeur", 
					"/oca " + (ComponentCreatif.COMMANDBLOCKS.isActivated() ? "deactivate" : "activate") + " commandblocks");
			
			OlympaPlayerCreatif pc = getOlympaPlayer();
			
			sendMessage(Prefix.INFO, "§7Etat de vos permissions");
			for (StaffPerm perm : StaffPerm.values())
				sendHoverAndCommand(Prefix.INFO, "§ePermission " + perm.toString().toLowerCase() + " : " + 
						(pc.hasStaffPerm(perm) ? "§aOUI" : "§cNON"),
						"§7Cliquez ici pour changer la valeur", 
						"/oca perm "+ perm.toString().toLowerCase());
			
		}else if (cmd.getArgument(0).equals("performances")) {

			sendMessage(Prefix.INFO, "§6>>> Informations de performances Créatif " + plugin.getDataManager().getServerIndex());
			sendMessage(Prefix.INFO, "§7Informations générales");
			sendMessage(Prefix.INFO, "§eNombre de parcelles chargées : %s", plugin.getPlotsManager().getPlots().size());
			sendMessage(Prefix.INFO, "§eEntités chargées : %s", plugin.getWorldManager().getWorld().getEntities().size());

			List<Plot> mostEntities = plugin.getPlotsManager().getPlots().stream().sorted(Comparator.comparingInt(plot -> -plot.getEntities().size())).collect(Collectors.toList());
			List<Plot> mostStoplagDetect = plugin.getPlotsManager().getPlots().stream().sorted(Comparator.comparingInt(plot -> -plot.getStoplagChecker().getCurrentCount())).collect(Collectors.toList());

			sendMessage(Prefix.INFO, "§7Informations parcelles les moins performantes");
			
			for (int i = 0 ; i < Math.min(3, mostEntities.size()) ; i++)
				sendHoverAndCommand(Prefix.INFO, "§eLe plus d'entités §4n°" + (i+1) + " §e: " +
						mostEntities.get(i) + " §7(" + mostEntities.get(i).getEntities().size() + ")", 
						"§7Se téléporter à la parcelle " + mostEntities.get(i), "/oc visit " + mostEntities.get(i));
			
			for (int i = 0 ; i < Math.min(3, mostStoplagDetect.size()) ; i++)
				sendHoverAndCommand(Prefix.INFO, "§eLe plus haut score stoplag §4n°" + (i+1) + " §e: " +
						mostStoplagDetect.get(i) + " §7(" + mostStoplagDetect.get(i).getStoplagChecker().getCurrentCount() + ")",  
						"§7Se téléporter à la parcelle " + mostStoplagDetect.get(i), "/oc visit " + mostStoplagDetect.get(i));
			
		}else
			sendIncorrectSyntax();
		
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
