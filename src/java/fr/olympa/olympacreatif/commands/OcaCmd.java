package fr.olympa.olympacreatif.commands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.spigot.economy.OlympaMoney;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.utils.Prefix;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OcPermissions;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.data.PermissionsManager.ComponentCreatif;
import fr.olympa.olympacreatif.gui.MainGui;
import fr.olympa.olympacreatif.perks.KitsManager.KitType;
import fr.olympa.olympacreatif.perks.UpgradesManager.UpgradeType;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotMembers.MemberInformations;
import fr.olympa.olympacreatif.plot.PlotStoplagChecker.StopLagDetect;
import fr.olympa.olympacreatif.utils.NBTcontrollerUtil;

public class OcaCmd extends AbstractCmd {

	private Function<Double, String> stoplagFormatter = d -> ((int) (d * 100)) + "%%";
	//private static final DecimalFormat stoplagFormatter = new DecimalFormat("###,##");;
	
	public OcaCmd(OlympaCreatifMain plugin) {
		super(plugin, "oca", OcPermissions.STAFF_OCA_CMD, "Panel de gestion pour le staff.");

		addArgumentParser("STAFF_PERM", StaffPerm.class);
		addArgumentParser("SERVER_COMPONENT", ComponentCreatif.class);
		
		addArgumentParser("UPGRADE_TYPE", UpgradeType.class);
		addArgumentParser("KIT_TYPE", KitType.class);
		
		addArgumentParser("PLOT_OWNER", (sender, str) -> {
			List<String> players = Bukkit.getOnlinePlayers().stream().map(p -> p.getName()).collect(Collectors.toList());
			players.add("spawn");
			players.add("system");
			return players.stream().filter(s -> s.toLowerCase().startsWith(str.toLowerCase())).collect(Collectors.toList());
		}, str -> {
			Player p = Bukkit.getPlayerExact(str);
			if (p != null)
				return new MemberInformations(AccountProviderAPI.getter().get(p.getUniqueId()).getInformation());
			else if (str.equalsIgnoreCase("system"))
				return new MemberInformations(28l, "System", UUID.fromString("1f2993c6-5c5f-3968-b8dc-b0f3ef15f7f0"));
			else if (str.equalsIgnoreCase("spawn"))
				return new MemberInformations(28l, "Spawn", UUID.fromString("1f2993c6-5c5f-3968-b8dc-b0f3ef15f7f0"));
			else
				return null;
		}, s -> "§4%s §cn'est pas valide, vous devez utiliser un nom de joueur, §4SYSTEM§c ou §4SPAWN§c.");
	}

	
	@Cmd(player = false, syntax = "Afficher la liste des parcelles actuellement chargées", args = "PLAYERS")
	public void plotslist(CommandContext cmd) {
		if (cmd.getArgumentsLength() == 0)
			plugin.getCmdLogic().sendPlotsList(getSender(), null);
		else
			plugin.getCmdLogic().sendPlotsList(getSender(), cmd.getArgument(0));
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
			topScores = plugin.getPlotsManager().getPlots().stream().sorted(Comparator.comparingLong(plot -> -plot.getTilesCount())).collect(Collectors.toCollection(() -> new ArrayList<Plot>()));

			sendMessage(Prefix.INFO, "§6>>> TOP tile entities chargées Créatif " + plugin.getDataManager().getServerIndex());
			for (int i = 0 ; i < Math.min(9, topScores.size()) ; i++)
				sendHoverAndCommand(Prefix.INFO, "§e" + (i+1) + ". : parcelle §c" + getPlotIdOnFixedLength(topScores.get(i)) + " §eavec un score de " + topScores.get(i).getTilesCount(),
						"§7Se téléporter à la parcelle " + topScores.get(i), "/oc visit " + topScores.get(i));
			break;
			
		case "stoplag":
			topScores = plugin.getPlotsManager().getPlots().stream().sorted(Comparator.comparingDouble(plot -> -plot.getStoplagChecker().getScore())).collect(Collectors.toCollection(() -> new ArrayList<Plot>()));

			sendMessage(Prefix.INFO, "§6>>> TOP scores stoplag Créatif " + plugin.getDataManager().getServerIndex());
			
			for (int i = 0 ; i < Math.min(9, topScores.size()) ; i++) 
				sendHoverAndCommand(Prefix.INFO, "§e" + (i+1) + ". parcelle §c" + 
					getPlotIdOnFixedLength(topScores.get(i)) + " §e: " + stoplagFormatter.apply(topScores.get(i).getStoplagChecker().getScore()) +
					" §7E." + stoplagFormatter.apply(topScores.get(i).getStoplagChecker().getScore(StopLagDetect.ENTITY)) + " " +
					"R." + stoplagFormatter.apply(topScores.get(i).getStoplagChecker().getScore(StopLagDetect.WIRE)) + " " +
					"P." + stoplagFormatter.apply(topScores.get(i).getStoplagChecker().getScore(StopLagDetect.PISTON)) + " " +
					"LP." + stoplagFormatter.apply(topScores.get(i).getStoplagChecker().getScore(StopLagDetect.LAMP)) + " " +
					"LQ." + stoplagFormatter.apply(topScores.get(i).getStoplagChecker().getScore(StopLagDetect.LIQUID)), 
					"§7Se téléporter à la parcelle " + topScores.get(i), "/oc visit " + topScores.get(i));
			
			break;
		}		
	}
	//String.format("%,.2f", topScores.get(i).getStoplagChecker().getScore()),
	
	
	
	private String getPlotIdOnFixedLength(Plot plot) {
		String s = plot.getId().toString();
		while (s.length() < 4)
			s = " " + s;
		return s;
	}
	
	@Cmd(player = true, syntax = "Réinitialiser une parcelle", args = {"INTEGER"}/*, description = "/oca resetplot [plot] [confirmationCode]"*/)
	public void resetplot(CommandContext cmd) {
		plugin.getCmdLogic().resetPlot(getOlympaPlayer(), (cmd.getArgumentsLength() > 0 ? (Integer) cmd.getArgument(0) : null));
	}
	
	/*
	@Cmd(syntax = "Gérer l'argent d'un joueur", args = {"PLAYERS", "info|give|withdraw", "INTEGER", }, min = 2)
	public void money(CommandContext cmd) {
		if (getOlympaPlayer() != null && !OcPermissions.STAFF_MANAGE_MONEY.hasPermissionWithMsg(getOlympaPlayer()))
			return;
		
		OlympaPlayerCreatif target = AccountProviderAPI.getter().get(((Player)cmd.getArgument(0)).getUniqueId());
		
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
	}*/
	
	@Cmd(player = true, syntax = "Ouvrir l'interface des parcelles d'un joueur", args = {"PLAYERS"}, min = 1)
	public void openmenuas(CommandContext cmd) {
		MainGui.getMainGuiForStaff(AccountProviderAPI.getter().get(((Player)cmd.getArgument(0)).getUniqueId()), getOlympaPlayer()).create(getPlayer());
	}

	@Cmd(player = true, syntax = "Définir le propriétaire d'une parcelle", args = {"PLOT_OWNER"}, min = 1)
	public void setowner(CommandContext cmd) {
		if (!OcPermissions.STAFF_SET_PLOT_OWNER.hasPermissionWithMsg((OlympaPlayerCreatif)getOlympaPlayer()))
			return;
		
		OlympaPlayerCreatif pc = getOlympaPlayer();
		
		if (pc.getCurrentPlot() == null) {
			OCmsg.NULL_CURRENT_PLOT.send(pc);
			return;
		}
		
		pc.getCurrentPlot().getMembers().setOwner(cmd.getArgument(0), true);
		sendMessage(Prefix.DEFAULT_GOOD, "Le propriétaire de la parcelle " + pc.getCurrentPlot() + 
				" est désormais " + ((MemberInformations)cmd.getArgument(0)).getName() + ".");
	}

	@Cmd(syntax = "Accéder aux informations VIP d'un joueur", args = {"info|set", "PLAYERS", "KIT_TYPE|UPGRADE_TYPE", "INTEGER"}, min = 2)
	public void manageshop(CommandContext cmd) {
		
		OlympaPlayerCreatif pc = AccountProviderAPI.getter().get(((Player)cmd.getArgument(1)).getUniqueId());
		
		if (cmd.getArgument(0).equals("info")) {
			sendMessage(Prefix.DEFAULT, "§6Elements VIP du joueur " + ((Player)cmd.getArgument(1)).getName());
			sendMessage(Prefix.DEFAULT, "   §eGrades : ");
			
			//System.out.println(String.format("      §a" + OlympaGroup.CREA_CONSTRUCTOR.getPrefix() + "§r : %s", pc.getGroups().containsKey(OlympaGroup.CREA_CONSTRUCTOR) ? "§aOUI" : "§cNON"));
			
			sendMessage(Prefix.DEFAULT, "      §a" + OlympaGroup.CREA_CONSTRUCTOR.getName(pc.getGender()) + "§r : %s", pc.getGroups().containsKey(OlympaGroup.CREA_CONSTRUCTOR) ? "§aOUI" : "§cNON");
			sendMessage(Prefix.DEFAULT, "      §a" + OlympaGroup.CREA_ARCHITECT.getName(pc.getGender()) + "§r : %s", pc.getGroups().containsKey(OlympaGroup.CREA_ARCHITECT) ? "§aOUI" : "§cNON");
			sendMessage(Prefix.DEFAULT, "      §a" + OlympaGroup.CREA_CREATOR.getName(pc.getGender()) + "§r : %s", pc.getGroups().containsKey(OlympaGroup.CREA_CREATOR) ? "§aOUI" : "§cNON");

			sendMessage(Prefix.DEFAULT, "   §eKits : ");
			for (KitType kit : KitType.values())
				if (kit != KitType.ADMIN)
					sendMessage(Prefix.DEFAULT, "      §aKit " + kit.getName() + "§r : %s", pc.hasKit(kit) ? "§aOUI" : "§cNON");

			sendMessage(Prefix.DEFAULT, "   §eAméliorations : ");
			for (UpgradeType upg : UpgradeType.values())
				sendMessage(Prefix.DEFAULT, "      §aKit " + upg.toString().toLowerCase() + " : %s", "§7niveau " + upg.getDataOf(pc).level + ", valeur " + upg.getDataOf(pc).value);
				
		}else if (cmd.getArgumentsLength() == 4) {
			if (cmd.getArgument(2) instanceof KitType) {
				KitType kit = cmd.getArgument(2);
				
				if (kit == KitType.ADMIN) {
					sendMessage(Prefix.BAD, "Vous ne pouvez pas gérer ce kit.");
					return;
				}
					
				
				if (pc.hasKit(kit) && (int) cmd.getArgument(3) == 1) {
					sendMessage(Prefix.BAD, "Le joueur " + pc.getName() + " possède déjà le kit " + kit.getName() + ".");
				}else if (pc.hasKit(kit) && (int) cmd.getArgument(3) == 0) {
					sendMessage(Prefix.DEFAULT_GOOD, "Le kit " + kit.getName() + " a été retiré à " + pc.getName() + ".");
					pc.removeKit(kit);
					
				}else if (!pc.hasKit(kit) && (int) cmd.getArgument(3) == 1) {
					sendMessage(Prefix.DEFAULT_GOOD, "Le kit " + kit.getName() + " a été donné à " + pc.getName() + ".");
					pc.addKit(kit);
				}else if (!pc.hasKit(kit) && (int) cmd.getArgument(3) == 0) {
						sendMessage(Prefix.BAD, "Le joueur " + pc.getName() + " ne possède déjà pas le kit " + kit.getName() + ".");
				}else
					sendIncorrectSyntax("Vous devez spécifier 1 pour ajouter le kit ou 0 pour le retirer.");
				
			} else if (cmd.getArgument(2) instanceof UpgradeType) {
				UpgradeType upg = cmd.getArgument(2);
				int level = (int) cmd.getArgument(3);
				
				if (upg.getDataOf(pc).level == level)
					sendMessage(Prefix.BAD, "Le joueur " + pc.getName() + " possède déjà l'amélioration " + upg.toString().toLowerCase() + " au niveau " + upg.getDataOf(pc).level + ".");
				else if (level < 0 || level > upg.getMaxLevel())
					sendMessage(Prefix.BAD, "Le niveau de l'amélioration " + upg.toString().toLowerCase() + " doit être compris entre 0 et " + upg.getMaxLevel() + ".");
				else {
					pc.incrementUpgradeLevel(upg, level - upg.getDataOf(pc).level);
					sendMessage(Prefix.DEFAULT_GOOD, "L'amélioration " + upg.toString().toLowerCase() + " du joueur " + pc.getName() + " est maintenant au niveau " + upg.getDataOf(pc).level + ".");
				}
			}
			
			
		}else
			sendIncorrectSyntax();
	}

	@Cmd(syntax = "Recharger les messages depuis la bdd", min = 1, args = "messages|nbttags|stoplag_limits")
	public void reload(CommandContext cmd) {
		if (!OcPermissions.STAFF_RELOAD.hasPermissionWithMsg(getOlympaPlayer()))
			return;
		
		if (cmd.getArgument(0).equals("messages")) 
			plugin.getDataManager().reloadMessages();	
		else if (cmd.getArgument(0).equals("nbttags"))
			NBTcontrollerUtil.reloadConfig();
		else if (cmd.getArgument(0).equals("stoplag_limits"))
			StopLagDetect.reloadConfig();
		
		sendMessage(Prefix.DEFAULT_GOOD, "§aLa config §2" + cmd.getArgument(0) + "§aa été rechargée. §7Veuillez ne pas abuser de cette commande, elle peut faire lag le serveur.");
	}
	
	
}






