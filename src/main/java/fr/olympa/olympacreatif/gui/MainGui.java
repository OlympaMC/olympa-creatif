package fr.olympa.olympacreatif.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;

public class MainGui extends OlympaGUI {

	private OlympaCreatifMain plugin;
	
	public MainGui(OlympaCreatifMain plugin) {
		super(Message.GUI_MAIN_NAME.getValue(), 6);
		this.plugin = plugin;
		
		inv.setItem(arg0, arg1);
	}

	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		// TODO Auto-generated method stub
		return false;
	}

}
