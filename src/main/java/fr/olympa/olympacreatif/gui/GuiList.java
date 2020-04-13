package fr.olympa.olympacreatif.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.plot.Plot;
import me.bullobily.GUIcreator.GuiInventory;
import me.bullobily.GUIcreator.GuiItem;
import me.bullobily.ItemCreator.ItemCreator;

public class GuiList {

	private OlympaCreatifMain plugin;
	
	public GuiList(OlympaCreatifMain plugin) {
		this.plugin = plugin;
	}
	
	public void openMainInterface(Player p) {
		GuiInventory gui = new GuiInventory(plugin, p, 6, null);
		
		//item liste des membres
		ItemCreator ic = new ItemCreator(new ItemStack(Material.PLAYER_HEAD));
		ic.setSkullOwner(p.getDisplayName());
		ic.setName(Message.GUI_MAIN_MEMBERS_LIST.getValue());
		ic.setLore(Message.GUI_MAIN_MEMBERS_LIST_LORE.getValue());
		
		GuiItem item = new GuiItem(ic.getItem());
		item.addAction(ClickType.LEFT, new BukkitRunnable() {
			
			@Override
			public void run() {
				p.closeInventory();
				Plot plot = plugin.getPlotsManager().getPlot(p.getLocation());
				if (plot != null)
					openMembersInterface(p, plot);
			}
		});
		
		gui.setItem(0, item);
		
		gui.open();
	}
	
	public void openMembersInterface(Player p, Plot plot) {
		p.sendMessage("TODO : member list");
	}
	
	public void openParametersInterface(Player p, Plot plot) {
		p.sendMessage("TODO : parameters list");
	}
}
