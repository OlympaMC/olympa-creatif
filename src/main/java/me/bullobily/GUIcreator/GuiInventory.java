package me.bullobily.GUIcreator;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class GuiInventory {

	JavaPlugin plugin;
	
	Map<Integer, GuiItem> itemsList = new HashMap<Integer, GuiItem>();
	Inventory inv;
	
	Player p;
	
	InventoryEvent event;
	BukkitRunnable actionOnClose = null;
	boolean executeActionOnClose = true;
	
	
	public GuiInventory(JavaPlugin plugin, Player p, int rowsCount, BukkitRunnable actionOnClose) {
		this.plugin = plugin;
		this.p = p;
		this.inv = Bukkit.createInventory(null, rowsCount*9);
		this.actionOnClose = actionOnClose;
	}
	
	public boolean setItem(int slot, GuiItem item) {
		if (slot < inv.getSize()) {
			itemsList.put(slot, item);
			inv.setItem(slot, item.getItem());
		}
		else
			return false;
		return true;
	}
	
	public void cancelActionOnClose(boolean cancelStatus) {
		executeActionOnClose = !cancelStatus;
	}

	/*public void setEvent(InventoryEvent e) {
		event = e;
	}*/
	
	public Inventory getInventory() {
		return inv;
	}
	
	public GuiItem getItemOnSlot(int slot) {
		return itemsList.get(slot);
	}
	
	public BukkitRunnable getActionOnClose() {
		if (executeActionOnClose)
			return actionOnClose;
		else
			return null;
	}

	/*private InventoryEvent getEvent() {
		return event;
	}*/
	public Player getPlayer() {
		return p;
	}
	public void open() {
		p.openInventory(inv);
		new GuiListener(plugin, this);
	}
}
