package fr.olympa.olympacreatif.commands;

import java.util.ArrayList;
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
import fr.olympa.olympacreatif.data.OCparam;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.data.PermissionsManager.ComponentCreatif;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.gui.MainGui;
import fr.olympa.olympacreatif.gui.StaffGui;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotId;
import fr.olympa.olympacreatif.utils.NBTcontrollerUtil;
import net.minecraft.server.v1_16_R3.Chunk;

public class OcaCmd extends AbstractCmd {

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

	@Cmd(syntax = "Active l'un des composants du créatif", args = {"worldedit|commandblocks|entities|commandblocks_and_vanilla_commands"})
	public void activate(CommandContext cmd) {
		if (cmd.getArgumentsLength() == 0) {
			sendComponentsStatus();
			return;
		}
		
		if (!PermissionsList.STAFF_MANAGE_COMPONENT.hasPermissionWithMsg(getOlympaPlayer()))
			return;
		
		ComponentCreatif component = ComponentCreatif.fromString(cmd.getArgument(0));
		
		if (component != null)
			component.activate();

		OCmsg.STAFF_ACTIVATE_COMPONENT.send(getPlayer(), component == null ? "§caucun" : (String) cmd.getArgument(0));
	}

	@Cmd(syntax = "Désactive l'un des composants du créatif", args = {"worldedit|commandblocks|entities|commandblocks_and_vanilla_commands"})
	public void deactivate(CommandContext cmd) {
		if (cmd.getArgumentsLength() == 0) {
			sendComponentsStatus();
			return;
		}
		
		if (!PermissionsList.STAFF_MANAGE_COMPONENT.hasPermissionWithMsg(getOlympaPlayer()))
			return;
		
		ComponentCreatif component = ComponentCreatif.fromString(cmd.getArgument(0));
		
		if (component != null)
			component.deactivate();

		OCmsg.STAFF_DEACTIVATE_COMPONENT.send(getPlayer(), component == null ? "§caucun" : (String) cmd.getArgument(0));
	}
	
	private void sendComponentsStatus() {
		sendMessage(Prefix.INFO, "§6Etat des composants créatif");
		for (ComponentCreatif component : ComponentCreatif.values())
			sendHoverAndCommand(Prefix.INFO, "§e" + component.getName() + " activé : " + (component.isActivated() ? "§aOUI" : "§cNON"),
					"§7Cliquez ici pour changer la valeur", 
					"/oca " + (component.isActivated() ? "deactivate" : "activate") + " " + component.getName());
	}

	@Cmd(player = true, syntax = "Gérer ses permissions staff", description = "/oca perms <perm>", min = 0, 
			args = "ghost_mode|owner_everywhere|worldedit_everywhere|bypass_kick_ban")
	public void perms(CommandContext cmd) {
		OlympaPlayerCreatif pc = ((OlympaPlayerCreatif)getOlympaPlayer());
		
		if (cmd.getArgumentsLength() == 0) {
			sendMessage(Prefix.INFO, "§6Etat de vos permissions");
			for (StaffPerm perm : StaffPerm.values())
				sendHoverAndCommand(Prefix.INFO, "§ePermission " + perm.toString().toLowerCase() + " : " + 
						(pc.hasStaffPerm(perm) ? "§aOUI" : "§cNON"),
						"§7Cliquez ici pour changer la valeur", 
						"/oca perms "+ perm.toString().toLowerCase());
			
			return;
		}
		
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
		
		pc.toggleStaffPerm(perm);
		sendMessage(Prefix.DEFAULT_GOOD, "§eVotre permission %s est désormais %s§a.", perm.toString().toLowerCase(), pc.hasStaffPerm(perm) ? "§aactivée" : "§cdésactivée");
	}

	@Cmd(syntax = "Afficher les informations relatives au créatif", min = 1, args = "general|entities|tile_entities|stoplag")
	public void performances(CommandContext cmd) {
		
		List<Plot> topScores;
		
		switch((String)cmd.getArgument(0)) {
		case "general":
			sendMessage(Prefix.INFO, "§6>>> Informations de performances Créatif " + plugin.getDataManager().getServerIndex());
			sendMessage(Prefix.INFO, "§6Informations générales");
			sendMessage(Prefix.INFO, "§eNombre de parcelles chargées : %s", plugin.getPlotsManager().getPlots().size());
			sendMessage(Prefix.INFO, "§eEntités chargées : %s", plugin.getWorldManager().getWorld().getEntities().size());
			//sendMessage(Prefix.INFO, "§eTile entities chargées : %s", plugin.getWorldManager().getNmsWorld().tileEntityList.size());
			//sendMessage(Prefix.INFO, "§eTicking tile entities chargées : %s", plugin.getWorldManager().getNmsWorld().tileEntityListTick.size());
			break;
			
		case "entities":
			topScores = plugin.getPlotsManager().getPlots().stream().sorted(Comparator.comparingInt(plot -> -plot.getEntities().size())).collect(Collectors.toCollection(() -> new ArrayList<Plot>()));

			sendMessage(Prefix.INFO, "§6>>> TOP entités chargées Créatif " + plugin.getDataManager().getServerIndex());
			for (int i = 0 ; i < Math.min(9, topScores.size()) ; i++)
				sendHoverAndCommand(Prefix.INFO, "§e" + (i+1) + ". : parcelle §c" + getPlotIdOnFixedLength(topScores.get(i)) + " §eavec un score de " + topScores.get(i).getEntities().size(),
						"§7Se téléporter à la parcelle " + topScores.get(i), "/oc visit " + topScores.get(i));
			break;
			
		case "tile_entities":
			topScores = plugin.getPlotsManager().getPlots().stream().sorted(Comparator.comparingLong(plot -> -plot.getLoadedTileEntitiesCount())).collect(Collectors.toCollection(() -> new ArrayList<Plot>()));

			sendMessage(Prefix.INFO, "§6>>> TOP tile entities chargées Créatif " + plugin.getDataManager().getServerIndex());
			for (int i = 0 ; i < Math.min(9, topScores.size()) ; i++)
				sendHoverAndCommand(Prefix.INFO, "§e" + (i+1) + ". : parcelle §c" + getPlotIdOnFixedLength(topScores.get(i)) + " §eavec un score de " + topScores.get(i).getLoadedTileEntitiesCount(),
						"§7Se téléporter à la parcelle " + topScores.get(i), "/oc visit " + topScores.get(i));
			break;
			
		case "stoplag":
			topScores = plugin.getPlotsManager().getPlots().stream().sorted(Comparator.comparingInt(plot -> -plot.getStoplagChecker().getCurrentCount())).collect(Collectors.toCollection(() -> new ArrayList<Plot>()));

			sendMessage(Prefix.INFO, "§6>>> TOP scores stoplag Créatif " + plugin.getDataManager().getServerIndex());
			for (int i = 0 ; i < Math.min(9, topScores.size()) ; i++)
				sendHoverAndCommand(Prefix.INFO, "§e" + (i+1) + ". : parcelle §c" + getPlotIdOnFixedLength(topScores.get(i)) + " §eavec un score de " + topScores.get(i).getStoplagChecker().getCurrentCount(),
						"§7Se téléporter à la parcelle " + topScores.get(i), "/oc visit " + topScores.get(i));
			break;
		}
/*
		List<Plot> mostEntities = plugin.getPlotsManager().getPlots().stream().sorted(Comparator.comparingInt(plot -> -plot.getEntities().size())).collect(Collectors.toList());
		List<Plot> mostTileEntities = plugin.getPlotsManager().getPlots().stream().sorted(Comparator.comparingLong(plot -> -plot.getLoadedTileEntitiesCount())).collect(Collectors.toList());
		List<Plot> mostStoplagDetect = plugin.getPlotsManager().getPlots().stream().sorted(Comparator.comparingInt(plot -> -plot.getStoplagChecker().getCurrentCount())).collect(Collectors.toList());

		sendMessage(Prefix.INFO, "§6Informations parcelles les moins performantes");
			sendMessage(Prefix.INFO, "§ePERF TYPE________ENTITIES__________TILES________STOPLAG");
		for (int i = 0 ; i < Math.min(plugin.getPlotsManager().getPlots().size(), 9) ; i++)
			sendMessage(Prefix.INFO, "§e____NUM " + (i + 1) + 
					getPlotIdOnFixedLength(mostEntities.get(i), 15) + 
					getPlotIdOnFixedLength(mostTileEntities.get(i), 15) + 
					getPlotIdOnFixedLength(mostStoplagDetect.get(i), 15));
		

		for (int i = 0 ; i < Math.min(3, mostEntities.size()) ; i++)
			sendHoverAndCommand(Prefix.INFO, "§eLe plus d'entités §4n°" + (i+1) + " §e: " +
					mostEntities.get(i) + " §7(" + mostEntities.get(i).getEntities().size() + ")", 
					"§7Se téléporter à la parcelle " + mostEntities.get(i), "/oc visit " + mostEntities.get(i));
		
		for (int i = 0 ; i < Math.min(3, mostTileEntities.size()) ; i++)
			sendHoverAndCommand(Prefix.INFO, "§eLe plus de tiles entities §4n°" + (i+1) + " §e: " +
					mostTileEntities.get(i) + " §7(" + mostTileEntities.get(i).getLoadedTileEntitiesCount() + ")", 
					"§7Se téléporter à la parcelle " + mostTileEntities.get(i), "/oc visit " + mostTileEntities.get(i));
		
		for (int i = 0 ; i < Math.min(3, mostStoplagDetect.size()) ; i++)
			sendHoverAndCommand(Prefix.INFO, "§eLe plus haut score stoplag §4n°" + (i+1) + " §e: " +
					mostStoplagDetect.get(i) + " §7(" + mostStoplagDetect.get(i).getStoplagChecker().getCurrentCount() + ")",  
					"§7Se téléporter à la parcelle " + mostStoplagDetect.get(i), "/oc visit " + mostStoplagDetect.get(i));*/
		
		
	}
	
	
	
	private String getPlotIdOnFixedLength(Plot plot) {
		String s = plot.getPlotId().toString();
		while (s.length() < 7)
			s = "_" + s;
		return s;
	}
	
	@Cmd(player = true, syntax = "Réinitialiser une parcelle"/*, description = "/oca resetplot [plot] [confirmationCode]"*/)
	public void resetplot(CommandContext cmd) {
		plugin.getCmdLogic().resetPlot(getOlympaPlayer(), cmd);
	}
	
	
	
}
