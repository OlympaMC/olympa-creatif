package me.bullobily.GUIcreator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class GuiItem {

	ItemStack item;
	Map<ClickType, BukkitRunnable> actions = new HashMap<ClickType, BukkitRunnable>();  
	List<GuiOption> options = new ArrayList<GuiOption>();
	
	public GuiItem(ItemStack item){
		this.item = item;
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public BukkitRunnable getAction(ClickType clickType) {
		return actions.get(clickType);
	}
	
	public boolean hasOption(GuiOption option) {
		if (options.contains(option))
			return true;
		else
			return false;
	}
	
	public void addAction(ClickType onClick, BukkitRunnable action) {
		actions.put(onClick, action);
	}
	public void addOption(GuiOption option) {
		options.add(option);
	}
	public void setItem(ItemStack item) {
		this.item = item;
	}
}
