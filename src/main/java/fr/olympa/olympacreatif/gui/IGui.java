package fr.olympa.olympacreatif.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.plot.Plot;

public abstract class IGui extends OlympaGUI{

	protected OlympaCreatifMain plugin;
	protected Plot plot; 
	protected OlympaPlayerCreatif p;
	
	public IGui(OlympaCreatifMain plugin, OlympaPlayerCreatif player, Plot plot, String inventoryName, int rows) {
		super(inventoryName, rows);

		this.plugin = plugin;
		this.plot = plot;
		
		this.p = player;
		
		inv.setItem(inv.getSize() - 1, ItemUtils.skullCustom("§aVers menu principal", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ=="));
		//inv.setItem(inv.getSize() - 1, ItemUtils.skullCustom("§aVers menu principal", "skull"));
	}
	
	public IGui(IGui gui, String inventoryName, int rows) {
		this(gui.getPlugin(), gui.getPlayer(), gui.getPlot(), inventoryName, rows);
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
			MainGui.getMainGui(this.p, this).create(p);
		return true;
	}
}
