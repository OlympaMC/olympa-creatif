package fr.olympa.olympacreatif.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.plot.Plot;

public abstract class IGui extends OlympaGUI{

	protected OlympaCreatifMain plugin;
	protected Plot plot; 
	protected OlympaPlayerCreatif p;
	
	public IGui(OlympaCreatifMain plugin, Player player, Plot plot, String inventoryName, int rows) {
		super(inventoryName, rows);

		this.plugin = plugin;
		this.plot = plot;
		
		this.p = AccountProvider.get(player.getUniqueId());
		
		inv.setItem(inv.getSize() - 1, MainGui.getBackItem());
	}
	
	public IGui(IGui gui, String inventoryName, int rows) {
		this(gui.getPlugin(), gui.getPlayer().getPlayer(), gui.getPlot(), inventoryName, rows);
	}
	
	public OlympaCreatifMain getPlugin() {
		return plugin;
	}
	
	public Plot getPlot() {
		return plot;
	}
	
	public OlympaPlayerCreatif getPlayer(){
		return p;
	}

	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (slot == inv.getSize() - 1) 
			MainGui.getMainGui(p, plot).create(p);
		return true;
	}
}
