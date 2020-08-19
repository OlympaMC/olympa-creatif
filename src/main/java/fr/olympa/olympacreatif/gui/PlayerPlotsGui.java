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
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.plot.Plot;

public class PlayerPlotsGui extends IGui {
	
	private List<Plot> playerPlots = new ArrayList<Plot>();
	
	public PlayerPlotsGui(IGui gui) {
		super(gui, "Plots du joueur " + gui.getPlayer().getName(), 5);
		
		playerPlots = p.getPlots(false);
		
		//tri de la liste de plots par ordre croissant d'id
		Collections.sort(playerPlots, new Comparator<Plot>() {

			@Override
			public int compare(Plot p1, Plot p2) {
				return p1.getPlotId().getId() - p2.getPlotId().getId();
			}
		});
		
		//recherche des plots du joueur
		for (Plot plot : playerPlots) {
			Material mat = null;
			switch(plot.getMembers().getPlayerRank(p)) {
			case OWNER:
				mat = Material.EMERALD_BLOCK;
				break;
			case CO_OWNER:
				mat = Material.DIAMOND_BLOCK;
				break;
			case TRUSTED:
				mat = Material.GOLD_BLOCK;
				break;
			case MEMBER:
				mat = Material.IRON_BLOCK;
				break;
			}
			
			if (mat != null) 
				inv.addItem(ItemUtils.item(mat, "§6 Parcelle " + plot.getPlotId().getId(), "§eRang : " + plot.getMembers().getPlayerRank(p).getRankName(), "§7Cliquez pour vous téléporter"));	
			
		}
			
	}

	@Override
	public boolean onClick(Player player, ItemStack current, int slot, ClickType click) {
		super.onClick(player, current, slot, click);
		
		if (slot < playerPlots.size()) {
			player.closeInventory();
			player.teleport(playerPlots.get(slot).getParameters().getSpawnLoc(plugin));
			player.sendMessage(Message.TELEPORT_IN_PROGRESS.getValue(playerPlots.get(slot)));
		}
		
		return true;
	}


	@Override
	public boolean onClickCursor(Player p, ItemStack current, ItemStack cursor, int slot) {
		return true;
	}
}
