package fr.olympa.olympacreatif.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
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
import fr.olympa.olympacreatif.plot.PlotParamType;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;

public class PlayerPlotsGui extends OlympaGUI {

	private OlympaCreatifMain plugin;
	private OlympaPlayerCreatif pc;
	private List<Plot> playerPlots = new ArrayList<Plot>();
	
	public PlayerPlotsGui(OlympaCreatifMain plugin, Player p) {
		super("§6Plots du joueur " + p.getDisplayName(), 5);
		this.plugin = plugin;
		this.pc = AccountProvider.get(p.getUniqueId());
		
		//recherche des plots du joueur
		for (Plot plot : plugin.getPlotsManager().getPlots()) {
			Material mat = null;
			switch(plot.getMembers().getPlayerRank(pc)) {
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
			
			if (mat != null) {
				playerPlots.add(plot);
				inv.addItem(ItemUtils.item(mat, "§6 Parcelle " + plot.getLoc(), "§eRang : " + plot.getMembers().getPlayerRank(pc).getRankName()));	
			}
		}

		inv.setItem(44, ItemUtils.item(Material.ACACIA_DOOR, "§cRetour", ""));
			
	}

	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (slot == 44) {
			Plot plot = plugin.getPlotsManager().getPlot(p.getLocation());
			if (plot == null)
				new MainGui(plugin, p, plot, "Menu").create(p);
			else
				new MainGui(plugin, p, plot, "Menu >> " + plot.getLoc()).create(p);
			return true;
		}
		
		if (slot < playerPlots.size()) {
			p.closeInventory();
			p.teleport((Location) playerPlots.get(slot).getParameters().getParameter(PlotParamType.SPAWN_LOC));
			p.sendMessage(Message.TELEPORT_IN_PROGRESS.getValue());
		}
		
		return true;
	}


	@Override
	public boolean onClickCursor(Player p, ItemStack current, ItemStack cursor, int slot) {
		return true;
	}
}
