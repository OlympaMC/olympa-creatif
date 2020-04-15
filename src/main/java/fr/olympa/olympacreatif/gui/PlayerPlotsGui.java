package fr.olympa.olympacreatif.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;

public class PlayerPlotsGui extends OlympaGUI {

	private OlympaCreatifMain plugin;
	private Player p;
	private List<Plot> playerPlots = new ArrayList<Plot>();
	
	public PlayerPlotsGui(OlympaCreatifMain plugin, Player p) {
		super("§6Plots du joueur " + p.getDisplayName(), 3);
		this.plugin = plugin;
		this.p = p;

		//recherche des plots du joueur
		for (Plot plot : plugin.getPlotsManager().getPlots()) {
			Material mat = null;
			switch(plot.getMembers().getPlayerRank(p)) {
			case CO_OWNER:
				mat = Material.DIAMOND_BLOCK;
				break;
			case MEMBER:
				mat = Material.IRON_BLOCK;
				break;
			case OWNER:
				mat = Material.EMERALD_BLOCK;
				break;
			case TRUSTED:
				mat = Material.GOLD_BLOCK;
				break;
			}
			
			if (mat != null) {
				playerPlots.add(plot);
				inv.addItem(ItemUtils.item(mat, "§6 Parcelle " + plot.getId().getAsString(), "§eRang : " + plot.getMembers().getPlayerRank(p).getRankName()));	
			}
		}
			
	}

	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (slot == 26) {
			new MainGui(plugin, p).create(p);
			return true;
		}
		
		if (slot < playerPlots.size()) {
			p.closeInventory();
			p.teleport(playerPlots.get(slot).getId().getLocation());
			p.sendMessage(Message.TELEPORT_IN_PROGRESS.getValue());
		}
		
		return true;
	}


	@Override
	public boolean onClickCursor(Player p, ItemStack current, ItemStack cursor, int slot) {
		return true;
	}
}
