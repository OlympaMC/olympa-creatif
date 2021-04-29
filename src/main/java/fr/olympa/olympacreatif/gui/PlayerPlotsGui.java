package fr.olympa.olympacreatif.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.plot.Plot;



public class PlayerPlotsGui extends IGui {
	
	private List<Plot> playerPlots = new ArrayList<Plot>();
	
	public PlayerPlotsGui(IGui gui) {
		super(gui, "Parcelles de " + gui.getPlayer().getName(), gui.getPlayer().getPlots(false).size()/9 + 2, gui.staffPlayer);
		
		playerPlots = p.getPlots(false);
		
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
								p.getPlayer().closeInventory();
								if (c == ClickType.LEFT) {
									if (isOpenByStaff) {
										staffPlayer.getPlayer().teleport(playerPlots.get(s).getId().getLocation());
										OCmsg.TELEPORT_IN_PROGRESS.send(staffPlayer, playerPlots.get(s).toString());
									}else {
										p.getPlayer().teleport(playerPlots.get(s).getId().getLocation());
										OCmsg.TELEPORT_IN_PROGRESS.send(p, playerPlots.get(s).toString());	
									}	
								}else if (c == ClickType.RIGHT) {
									if (isOpenByStaff)
										new PlotParametersGui(MainGui.getMainGuiForStaff(getPlayer(), staffPlayer, plot)).create(staffPlayer.getPlayer());
									else
										MainGui.getMainGui(getPlayer(), playerPlots.get(s)).create(getPlayer().getPlayer());
								}
						});
			}	
		}
	}
}
