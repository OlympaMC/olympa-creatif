package fr.olympa.olympacreatif.command_reborn;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.plot.Plot;

public class ClaimCmd extends OcCmd {

	public ClaimCmd(OlympaCreatifMain plugin) {
		super(plugin, "oc", PermissionsList.CLAIM, "Préfixe à toutes les commandes du Créatif d'Olympa.");
		// TODO Auto-generated constructor stub
	}


	@Cmd (player = true, description = "Permet l'acquisition d'une parcelle. DESC 2", syntax = "/claim")
	public void claim(CommandContext cmd) {
		OlympaPlayerCreatif pc = getOlympaPlayer();
		
		//teste si le joueur a encore des plots dispo
		if (pc.getPlotsSlots(true) - pc.getPlots(true).size() > 0) {
			if (pc.getPlotsSlots(false) - pc.getPlots(false).size() > 0) {
				
				Plot plot = plugin.getPlotsManager().createPlot(getPlayer());
				getPlayer().teleport(plot.getPlotId().getLocation());
				//PlotsInstancesListener.executeEntryActions(plugin, p, plot);
				getPlayer().sendMessage(OCmsg.PLOT_NEW_CLAIM.getValue(plot));	
				
			}else
				getPlayer().sendMessage(OCmsg.MAX_PLOT_COUNT_REACHED.getValue());
		}else
			getPlayer().sendMessage(OCmsg.MAX_PLOT_COUNT_OWNER_REACHED.getValue());
	}
}










