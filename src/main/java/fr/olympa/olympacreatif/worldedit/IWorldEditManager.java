package fr.olympa.olympacreatif.worldedit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.plot.Plot;

public interface IWorldEditManager {
	
	public boolean isWeEnabled();
	
	public void setWeActivationState(boolean b);
	
	public void clearClipboard(Plot plot, Player p);
	
	public void resetPlot(Player requester, Plot plot);
	
	Listener bukkitListener = new Listener() {
		
		@EventHandler
		public void onCommand(PlayerCommandPreprocessEvent e) {
			
			if (e.getMessage().contains("/schem")) {
				OlympaPlayerCreatif pc = AccountProvider.get(e.getPlayer().getUniqueId());
				
				if (!PermissionsList.STAFF_BYPASS_WORLDEDIT.hasPermissionWithMsg(pc) || !pc.hasStaffPerm(StaffPerm.BYPASS_WORLDEDIT))
					e.setCancelled(true);
			}	
		}
	};
}
