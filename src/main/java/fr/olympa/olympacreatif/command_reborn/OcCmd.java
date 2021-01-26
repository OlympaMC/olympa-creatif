package fr.olympa.olympacreatif.command_reborn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OCparam;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.gui.IGui;
import fr.olympa.olympacreatif.gui.MainGui;
import fr.olympa.olympacreatif.gui.MembersGui;
import fr.olympa.olympacreatif.gui.PlayerPlotsGui;
import fr.olympa.olympacreatif.gui.ShopGui;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotId;
import fr.olympa.olympacreatif.plot.PlotPerm;
import fr.olympa.olympacreatif.plot.PlotPerm.PlotRank;

public class OcCmd extends AbstractCmd {

	public OcCmd(OlympaCreatifMain plugin) {
		super(plugin, "oc", null, "Commandes principales du Créatif d'Olympa");
	}


	@Cmd (player = true, syntax = "Acquérir une parcelle")
	public void claim(CommandContext cmd) {
		plugin.getCmdLogic().claimNewPlot(getOlympaPlayer());
	}
	
	
	@Cmd(player = true, syntax = "Ouvrir le menu principal du Créatif")
	public void menu(CommandContext cmd) {
		MainGui.getMainGui(getOlympaPlayer()).create(getPlayer());
	}

	
	@Cmd(player = true, syntax = "Ajouter un membre à la parcelle", args = "PLAYERS", min = 1)
	public void invite(CommandContext cmd) {
		plugin.getCmdLogic().invitePlayer(getOlympaPlayer(), cmd.getArgument(0));
	}

	
	@Cmd(player = true, syntax = "Accepter une invitation à une parcelle", args = "PLAYERS", min = 1)
	public void accept(CommandContext cmd) {
		plugin.getCmdLogic().acceptInvitation(getOlympaPlayer(), cmd.getArgument(0));
	}

	
	@Cmd(player = true, syntax = "Envoyer un message dans le chat parcelle", min = 1)
	public void chat(CommandContext cmd) {
		if (((OlympaPlayerCreatif) getOlympaPlayer()).getCurrentPlot() == null) {
			OCmsg.INVALID_PLOT_ID.send(getOlympaPlayer().getPlayer());
			return;
		}

		String concat = "";
		List<String> argsMsg = new ArrayList<String>();
		for (int i = 0 ; i < cmd.getArgumentsLength() ; i++)
			argsMsg.add(cmd.getArgument(i));
		
		for (String s : argsMsg)
			concat += s + " ";
		
		((OlympaPlayerCreatif) getOlympaPlayer()).getCurrentPlot().sendMessage(getOlympaPlayer(), concat);
	}

	
	
	@Cmd(player = true, syntax = "Expulser un visiteur de la parcelle", args = "PLAYERS", min = 1)
	public void kick(CommandContext cmd) {
		plugin.getCmdLogic().kickPlayerFromPlot(getOlympaPlayer(), cmd.getArgument(0));
	}
	
	
	@Cmd(player = true, syntax = "Bannir un visiteur de la parcelle", args = "PLAYERS", min = 1)
	public void ban(CommandContext cmd) {
		plugin.getCmdLogic().banPlayerFromPlot(getOlympaPlayer(), cmd.getArgument(0));
	}
	
	
	@Cmd(player = true, syntax = "Débannir un visiteur de la parcelle", args = "PLAYERS", min = 1)
	public void unban(CommandContext cmd) {
		plugin.getCmdLogic().unbanPlayerFromPlot(getOlympaPlayer(), cmd.getArgument(0));
	}
	
	
	@Cmd(player = true, syntax = "Lister les joueurs bannis de la parcelle")
	public void banlist(CommandContext cmd) {
		plugin.getCmdLogic().sendBanList(getOlympaPlayer());
	}
	
	
	@Cmd(player = true, syntax = "Visiter une parcelle en utilisant son id", args = "INTEGER", min = 1)
	public void visit(CommandContext cmd) {
		plugin.getCmdLogic().visitPlot(getOlympaPlayer(),PlotId.fromId(plugin, cmd.getArgument(0)));
	}
	
	@Cmd(player = true, syntax = "Visiter la parcelle d'un joueur", args = {"PLAYERS", "INTEGER"}, min = 1)
	public void visitp(CommandContext cmd) {
		if (cmd.getArgumentsLength() >= 2)
			plugin.getCmdLogic().visitPlotFrom(getOlympaPlayer(), cmd.getArgument(0), cmd.getArgument(1));
		else
			plugin.getCmdLogic().visitPlotFrom(getOlympaPlayer(), cmd.getArgument(0), 1);
	}

	
	@Cmd(player = true, syntax = "Afficher la liste de vos parcelles")
	public void plots(CommandContext cmd) {
		new PlayerPlotsGui(MainGui.getMainGui(getOlympaPlayer())).create(getPlayer());
	}

	
	@Cmd(player = true, syntax = "Définir le spawn de la parcelle")
	public void setspawn(CommandContext cmd) {
		Plot plot = ((OlympaPlayerCreatif)getOlympaPlayer()).getCurrentPlot();
		
		if (plot == null)
			OCmsg.INVALID_PLOT_ID.send(getPlayer());
		
		else if (!PlotPerm.SET_PLOT_SPAWN.has(plot, getOlympaPlayer()))
			OCmsg.INSUFFICIENT_PLOT_PERMISSION.send(getPlayer(), PlotPerm.SET_PLOT_SPAWN);
				
		else {
			if (plot.getParameters().setSpawnLoc(getPlayer().getLocation()))
				OCmsg.PLOT_SPAWN_LOC_SET.send(getPlayer());
		}
	}

	
	@Cmd(player = true, syntax = "Afficher la liste des membres de la parcelle")
	public void members(CommandContext cmd) {
		if (((OlympaPlayerCreatif)getOlympaPlayer()).getCurrentPlot() == null) {
			OCmsg.INVALID_PLOT_ID.send(getPlayer());
			return;
		}

		new MembersGui(MainGui.getMainGui(getOlympaPlayer())).create(player);
	}

	
	@Cmd(player = true, syntax = "Retourner au spawn du Créatif")
	public void spawn(CommandContext cmd) {
		OCparam.SPAWN_LOC.get().teleport(getPlayer());
		OCmsg.TELEPORTED_TO_WORLD_SPAWN.send(getPlayer());
	}

	
	@Cmd(player = true, syntax = "Retourner au spawn de la parcelle")
	public void pspawn(CommandContext cmd) {
		Plot plot = ((OlympaPlayerCreatif) getOlympaPlayer()).getCurrentPlot();
		
		if (plot == null)
			OCmsg.INVALID_PLOT_ID.send(getPlayer());
		else {
			getPlayer().teleport(plot.getParameters().getSpawnLoc());
			OCmsg.TELEPORTED_TO_PLOT_SPAWN.send(getPlayer(), plot);	
		}
	}

	
	@Cmd(player = true, syntax = "Obtenir la tête d'un joueur", args = "PLAYERS", min = 1)
	public void skull(CommandContext cmd) {
		if (!PermissionsList.USE_SKULL_COMMAND.hasPermissionWithMsg(getOlympaPlayer()))
			return;
		
		Consumer<ItemStack> consumer = sk -> getPlayer().getInventory().addItem(sk);
		ItemUtils.skull(consumer, "§6Tête de " + cmd.getArgument(0), cmd.getArgument(0));
		OCmsg.OCO_HEAD_GIVED.send(getPlayer(), cmd.getArgument(0));
	}

	@Cmd(player = true, syntax = "Ouvrir le menu des microblocs ou en obtenir une définie")
	public void mb(CommandContext cmd) {
		if (!PermissionsList.USE_MICRO_BLOCKS.hasPermissionWithMsg(getOlympaPlayer()) || !PlotPerm.BUILD.has((OlympaPlayerCreatif) getOlympaPlayer()))
			return;
		
		if (cmd.getArgumentsLength() == 0)
			plugin.getPerksManager().getMicroBlocks().openGui(getPlayer());
		else if (plugin.getPerksManager().getMicroBlocks().getMb(cmd.getArgument(0).toString()) != null)
			getPlayer().getInventory().addItem(plugin.getPerksManager().getMicroBlocks().getMb(cmd.getArgument(0).toString()));
		else
			OCmsg.OCO_UNKNOWN_MB.send(getPlayer(), cmd.getArgument(0));
	}

	@Cmd(player = true, syntax = "Afficher le magasin du Créatif")
	public void shop(CommandContext cmd) {
		new ShopGui(MainGui.getMainGui(getOlympaPlayer())).create(getPlayer());
	}

	@Cmd(player = true, syntax = "Redevenir simple visiteur sur la parcelle actuelle")
	public void leave(CommandContext cmd) {
		OlympaPlayerCreatif pc = getOlympaPlayer();
		PlotRank rank = pc.getCurrentPlot().getMembers().getPlayerRank(pc);
		
		if (pc.getCurrentPlot() == null || rank == PlotRank.VISITOR || rank == PlotRank.OWNER) {
			OCmsg.INSUFFICIENT_PLOT_PERMISSION.send(pc, PlotPerm.BUILD);
			return;
		}
		
		OCmsg.PLOT_LEAVED.send(pc);
			
	}
}










