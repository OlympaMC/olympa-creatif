package fr.olympa.olympacreatif.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Consumer;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.plot.Plot;



public abstract class IGui extends OlympaGUI{

	protected boolean isOpenByStaff;
	protected OlympaPlayerCreatif staffPlayer;
	
	protected OlympaCreatifMain plugin;
	protected Plot plot; 
	protected OlympaPlayerCreatif p;
	
	private Map<ItemStack, TriConsumer<ItemStack, ClickType, Integer>> actionItems = new HashMap<ItemStack, TriConsumer<ItemStack, ClickType, Integer>>(); 
	
	public IGui(OlympaCreatifMain plugin, OlympaPlayerCreatif player, Plot plot, String inventoryName, int rows, OlympaPlayerCreatif staffPlayer) {
		super((staffPlayer == null ? "§8" : "§c[STAFF] §8") + inventoryName, rows);

		this.plugin = plugin;
		this.plot = plot;
		
		this.p = player;
		this.staffPlayer = staffPlayer;
		this.isOpenByStaff = staffPlayer != null;

		setItem(inv.getSize() - 1, getBackItem(), (it, c, s) -> {
			if (isOpenByStaff)
				MainGui.getMainGuiForStaff(p, staffPlayer).create(staffPlayer.getPlayer());
			else
				MainGui.getMainGui(this.p, this).create(p.getPlayer());
		});
	}
	
	public IGui(IGui gui, String inventoryName, int rows, OlympaPlayerCreatif staffPlayer) {
		this(gui.getPlugin(), gui.getPlayer(), gui.getPlot(), inventoryName, rows, staffPlayer);
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
	
	public static ItemStack getBackItem() {
		return ItemUtils.skullCustom("§aRetour", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==");
	}

	/**
	 * Define which item to place on which slot and the action to execute when clicked. If the item is placed on a slot already
	 * occupied by another item, the former one will be erased
	 * @param slot Slot to place item in
	 * @param it Concerned item
	 * @param Consumer executed when item clicked (may be null)
	 */
	protected void setItem(int slot, ItemStack it, TriConsumer<ItemStack, ClickType, Integer> consumer) {
		if (inv.getItem(slot) != null)
			actionItems.remove(inv.getItem(slot));
		
		actionItems.put(it, consumer);
		inv.setItem(slot, it);
	}
	
	/**
	 * Change item from its former state to the new one. Copy the action too
	 * @param from
	 * @param to
	 */
	protected void changeItem(ItemStack from, ItemStack to) {
		if (!actionItems.containsKey(from))
			return;
		
		if (inv.first(from) == -1)
			return;
		
		inv.setItem(inv.first(from.clone()), to.clone());
		actionItems.put(to, actionItems.remove(from));		
	}
	
	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (actionItems.get(current) != null)
			actionItems.get(current).accept(current, click, slot);
		
		return true;
	}
}
