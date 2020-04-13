package me.bullobily.GUIcreator;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class GuiListener implements Listener {

	GuiInventory inv;
	private JavaPlugin plugin;
	
	public GuiListener(JavaPlugin plugin, GuiInventory guiInventory) {
		this.plugin = plugin;
		inv = guiInventory;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onClickInventory(InventoryClickEvent e) {
		if (!e.getWhoClicked().equals(inv.getPlayer()))
			return;
		
		Player p = (Player) e.getWhoClicked();
		
		if (e.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) || e.getAction().equals(InventoryAction.COLLECT_TO_CURSOR)) {
			e.setCancelled(true);
			return;
		}
		
		if (e.getCurrentItem() == null || inv.getItemOnSlot(e.getRawSlot()) == null)
			return;
		
		if (!inv.getItemOnSlot(e.getRawSlot()).hasOption(GuiOption.ITEM_CAN_BE_TAKEN))
			e.setCancelled(true);
		
		//inv.setEvent(e);
		
		BukkitRunnable toRun = inv.getItemOnSlot(e.getRawSlot()).getAction(e.getClick());
		if (toRun != null) 
			toRun.runTask(plugin);
	}
	
	@EventHandler
	public void onCloseInventory(InventoryCloseEvent e) {
		if (!e.getPlayer().equals(inv.getPlayer()))
			return;

		//inv.setEvent(e);
		
		if (inv.getActionOnClose() != null)
			inv.getActionOnClose().runTask(plugin);

		HandlerList.unregisterAll(this);
	}
}









