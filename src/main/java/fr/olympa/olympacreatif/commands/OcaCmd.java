package fr.olympa.olympacreatif.commands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.economy.OlympaMoney;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.OcPermissions;
import fr.olympa.olympacreatif.data.PermissionsManager.ComponentCreatif;
import fr.olympa.olympacreatif.gui.MainGui;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.plot.Plot;

public class OcaCmd extends AbstractCmd {

	public OcaCmd(OlympaCreatifMain plugin) {
		super(plugin, "oca", OcPermissions.STAFF_OCA_CMD, "Panel de gestion pour le staff.");

		addArgumentParser("STAFF_PERM", StaffPerm.class);
		addArgumentParser("SERVER_COMPONENT", ComponentCreatif.class);
	}

	
	@Cmd(player = true, syntax = "Afficher la liste des parcelles actuellement chargées", args = "PLAYERS")
	public void plotslist(CommandContext cmd) {
		if (cmd.getArgumentsLength() == 0)
			plugin.getCmdLogic().sendPlotsList(getOlympaPlayer(), null);
		else
			plugin.getCmdLogic().sendPlotsList(getOlympaPlayer(), cmd.getArgument(0));
	}

	@Cmd(syntax = "Gérer les composants du créatif", args = {"activate|deactivate", "SERVER_COMPONENT"})
	public void component(CommandContext cmd) {
		if (cmd.getArgumentsLength() == 0) {

			sendMessage(Prefix.INFO, "§6Etat des composants créatif");
			if (!isConsole())
				for (ComponentCreatif component : ComponentCreatif.values())
					sendHoverAndCommand(Prefix.INFO, "§e" + component.getName() + " activé : " + (component.isActivated() ? "§aOUI" : "§cNON"),
							"§7Cliquez ici pour changer la valeur", 
							"/oca component " + (component.isActivated() ? "deactivate " : "activate ") + component.toString());
			else
				for (ComponentCreatif component : ComponentCreatif.values())
					sendMessage(Prefix.INFO, "§e" + component.getName() + " activé : " + (component.isActivated() ? "§aOUI" : "§cNON"));
			
		}else if (cmd.getArgumentsLength() == 1) {
			sendIncorrectSyntax();
		
		}else if (cmd.getArgumentsLength() >= 2) {
			ComponentCreatif component = cmd.getArgument(1);
			
			if (cmd.getArgument(0).equals("activate")) {
				component.activate();
				
				if (!isConsole())
					OCmsg.STAFF_ACTIVATE_COMPONENT.send(getPlayer(), component == null ? "§caucun" : (String) cmd.getArgument(0));
				else
					sendMessage(Prefix.INFO, "§aLe composant " + (component == null ? "§caucun§a" : (String) cmd.getArgument(0)) + " a été activé.");	
			}else {
				component.deactivate();
				
				if (!isConsole())
					OCmsg.STAFF_DEACTIVATE_COMPONENT.send(getPlayer(), component == null ? "§caucun" : (String) cmd.getArgument(0));
				else
					sendMessage(Prefix.INFO, "§cLe composant " + (component == null ? "§caucun§c" : (String) cmd.getArgument(0)) + " a été désactivé.");
			}
		}
	}

	/*
	@Cmd(syntax = "Active l'un des composants du créatif", args = {"SERVER_COMPONENT"})
	public void activate(CommandContext cmd) {
		if (cmd.getArgumentsLength() == 0) {
			sendComponentsStatus();
			return;
		}
		
		if (getSender() instanceof Player && !PermissionsList.STAFF_MANAGE_COMPONENT.hasPermissionWithMsg(getOlympaPlayer()))
			return;
		
		ComponentCreatif component = cmd.getArgument(0);
		
		if (component != null)
			component.activate();
		
		if (!isConsole())
			OCmsg.STAFF_ACTIVATE_COMPONENT.send(getPlayer(), component == null ? "§caucun" : (String) cmd.getArgument(0));
		else
			sendMessage(Prefix.INFO, "§aLe composant " + (component == null ? "§caucun§a" : (String) cmd.getArgument(0)) + " a été activé.");
	}

	@Cmd(syntax = "Désactive l'un des composants du créatif", args = {"SERVER_COMPONENT"})
	public void deactivate(CommandContext cmd) {
		if (cmd.getArgumentsLength() == 0) {
			sendComponentsStatus();
			return;
		}
		
		if (!isConsole() && !PermissionsList.STAFF_MANAGE_COMPONENT.hasPermissionWithMsg(getOlympaPlayer()))
			return;
		
		ComponentCreatif component = cmd.getArgument(0);
		
		if (component != null)
			component.deactivate();

		if (getSender() instanceof Player)
			OCmsg.STAFF_DEACTIVATE_COMPONENT.send(getPlayer(), component == null ? "§caucun" : (String) cmd.getArgument(0));
		else
			sendMessage(Prefix.INFO, "§cLe composant " + (component == null ? "§caucun§c" : (String) cmd.getArgument(0)) + " a été activé.");
	}*/	
	

	@Cmd(player = true, syntax = "Gérer ses permissions staff", description = "/oca perms <perm>", args = "STAFF_PERM")
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
		
		StaffPerm perm = cmd.getArgument(0);
		
		if (!perm.getOlympaPerm().hasPermissionWithMsg(getOlympaPlayer()))
			return;
		
		pc.toggleStaffPerm(perm);
		if (perm == StaffPerm.WORLDEDIT)
			plugin.getPermissionsManager().setWePermsAdmin(pc);
		
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
		String s = plot.getId().toString();
		while (s.length() < 7)
			s = " " + s;
		return s;
	}
	
	@Cmd(player = true, syntax = "Réinitialiser une parcelle", args = {"INTEGER", "code"}/*, description = "/oca resetplot [plot] [confirmationCode]"*/)
	public void resetplot(CommandContext cmd) {
		plugin.getCmdLogic().resetPlot(getOlympaPlayer(), (cmd.getArgumentsLength() > 0 ? (Integer) cmd.getArgument(0) : null), (cmd.getArgumentsLength() > 1 ? (String) cmd.getArgument(1) : null));
	}
	
	@Cmd(syntax = "Gérer l'argent d'un joueur", args = {"PLAYERS", "info|give|withdraw", "INTEGER", }, min = 2)
	public void money(CommandContext cmd) {
		if (getOlympaPlayer() != null && !OcPermissions.STAFF_MANAGE_MONEY.hasPermissionWithMsg(getOlympaPlayer()))
			return;
		
		OlympaPlayerCreatif target = AccountProvider.get(((Player)cmd.getArgument(0)).getUniqueId());
		
		int money = cmd.getArgumentsLength() == 3 ? cmd.getArgument(2) : 0;
		
		switch((String) cmd.getArgument(1)) {
		case "info":
			Prefix.DEFAULT.sendMessage(getSender(), "Le joueur %s a actuellement %s coins.", target.getName(), target.getGameMoney().getFormatted());
			break;
			
		case "give":
			if (money <= 0) {
				Prefix.DEFAULT_BAD.sendMessage(getSender(), "Veuillez entrer un montant valide !");
				return;
			}

			target.getGameMoney().give(money);
			
			Prefix.DEFAULT.sendMessage(getSender(), "Le joueur %s a reçu %s coins et en a maintenant %s.", 
					target.getName(), money + OlympaMoney.OMEGA, target.getGameMoney().getFormatted());
			
			OCmsg.MONEY_RECIEVED_COMMAND.send(target, money + OlympaMoney.OMEGA);
			break;
			
		case "withdraw":
			if (money <= 0) {
				Prefix.DEFAULT_BAD.sendMessage(getSender(), "Veuillez entrer un montant valide !");
				return;
			}
			
			if (target.getGameMoney().withdraw(money)) {
				Prefix.DEFAULT.sendMessage(getSender(), "Le joueur %s a perdu %s coins et en a maintenant %s.", 
						target.getName(), money + OlympaMoney.OMEGA, target.getGameMoney().getFormatted());
				OCmsg.MONEY_WITHDRAWED_COMMAND.send(target, money + OlympaMoney.OMEGA);
			} else 
				Prefix.DEFAULT_BAD.sendMessage(getSender(), "Le joueur %s n'a que %s, impossible de lui en retirer %s.", target.getName(), target.getGameMoney().getFormatted(),  money + "");
			
			break;
		}
	}
	
	@Cmd(player = true, syntax = "Ouvrir l'interface des parcelles d'un joueur", args = {"PLAYERS"}, min = 1)
	public void openmenuas(CommandContext cmd) {
		MainGui.getMainGuiForStaff(AccountProvider.get(((Player)cmd.getArgument(0)).getUniqueId()), getOlympaPlayer()).create(getPlayer());
	}
	
	
	
}
