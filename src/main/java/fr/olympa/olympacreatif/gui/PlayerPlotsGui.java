package fr.olympa.olympacreatif.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;

public class PlayerPlotsGui extends OlympaGUI {

	private OlympaCreatifMain plugin;
	private Player p;
	
	public PlayerPlotsGui(OlympaCreatifMain plugin, Player p) {
		super("§6Plots du joueur " + p.getDisplayName(), 3);
		this.plugin = plugin;
		this.p = p;
	}

	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		// TODO Auto-generated method stub
		return false;
	}

}
