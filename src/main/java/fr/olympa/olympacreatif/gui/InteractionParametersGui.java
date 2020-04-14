package fr.olympa.olympacreatif.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;

public class InteractionParametersGui extends OlympaGUI {

	private OlympaCreatifMain plugin;
	private Player p;
	private Plot plot;
	
	public InteractionParametersGui(OlympaCreatifMain plugin, Player p, Plot plot) {
		super("§6Paramètres d'interaction du plot : " + plot.getId().getAsString(), 6);
		this.plugin = plugin;
		this.p = p;
		this.plot = plot;
	}

	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		// TODO Auto-generated method stub
		return false;
	}

}
