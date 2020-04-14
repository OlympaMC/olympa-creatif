package fr.olympa.olympacreatif.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;

public class MembersGui extends OlympaGUI {

	private OlympaCreatifMain plugin;
	private Plot plot;
	private Player p;
	
	public MembersGui(OlympaCreatifMain plugin, Player p, Plot plot) {
		super("§6Membres du plot : " + plot.getId().getAsString(), 3);
		
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
