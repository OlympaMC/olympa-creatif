package fr.olympa.olympacreatif.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.plot.Plot;



public class PlayerPlotsGui extends IGui {
	
	private List<Plot> playerPlots = new ArrayList<Plot>();
	
	public PlayerPlotsGui(IGui gui) {
		super(gui, "Parcelles de " + gui.getPlayer().getName(), gui.getPlayer().getPlots(false).size()/9 + 2, gui.staffPlayer);
		
		playerPlots = p.getPlots(false);

		Player player = (Player) p.getPlayer();
		int i = -1;
		
		//recherche des plots du joueur
		for (Plot plot : playerPlots) {
			Material mat = plot.getMembers().getPlayerRank(p).getMat();			
			
			if (mat != null) {
				i++;
				setItem(i, ItemUtils.item(mat, "§6 Parcelle " + plot.getId().getId(), 
						"§eRang : " + plot.getMembers().getPlayerRank(p).getRankName(), 
						"§7Clic gauche : téléportation vers le plot", 
						isOpenByStaff ? 
								"§7§8[STAFF] Clic droit : ouverture des paramètres de cette parcelle" : 
								"§7Clic droit : ouverture du menu pour celle parcelle"), 
						
						(it, c, s) -> {
							if (s < playerPlots.size()) 
								player.closeInventory();
							if (c == ClickType.LEFT) {
								if (isOpenByStaff) {
									playerPlots.get(s).getId().teleport((Player) staffPlayer.getPlayer());
									OCmsg.TELEPORT_IN_PROGRESS.send(staffPlayer, playerPlots.get(s).toString());
								}else {
									playerPlots.get(s).getId().teleport(player);
									OCmsg.TELEPORT_IN_PROGRESS.send(p, playerPlots.get(s).toString());	
								}	
							}else if (c == ClickType.RIGHT) {
								if (isOpenByStaff)
									new PlotParametersGui(MainGui.getMainGuiForStaff(getPlayer(), staffPlayer, plot)).create((Player) staffPlayer.getPlayer());
								else
									MainGui.getMainGui(getPlayer(), playerPlots.get(s)).create((Player) getPlayer().getPlayer());
							}
						});
			}	
		}
	}
}
