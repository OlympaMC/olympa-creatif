package fr.olympa.olympacreatif.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.PlayerParamType;

public class PlayerParametersGui extends OlympaGUI{

	private OlympaCreatifMain plugin;
	private OlympaPlayerCreatif p;
	
	public PlayerParametersGui(OlympaCreatifMain plugin, Player player) {
		super("Paramètres de " + player.getName(), 1);
		this.plugin = plugin;
		
		p = AccountProvider.get(p.getUniqueId());

		inv.setItem(inv.getSize() - 1, MainGui.getBackItem());
		
		inv.setItem(0, PlotParametersGui.setSwitchState(ItemUtils.item(Material.PAPER, "§6Activation par défaut du chat parcelle"), 
				p.getPlayerParam(PlayerParamType.DEFAULT_PLOT_CHAT)));
		inv.setItem(1, PlotParametersGui.setSwitchState(ItemUtils.item(Material.CRAFTING_TABLE, "§6Double sneak pour ouvrir le menu"), 
				p.getPlayerParam(PlayerParamType.OPEN_GUI_ON_SNEAK)));
	}
	
	@Override
	public boolean onClick(Player player, ItemStack current, int slot, ClickType click) {
		if (slot == inv.getSize() - 1) {
			MainGui.openMainGui(player);
			return true;
		}
		
		switch(slot) {
		case 0:
			p.setPlayerParam(PlayerParamType.DEFAULT_PLOT_CHAT, !p.getPlayerParam(PlayerParamType.DEFAULT_PLOT_CHAT));
			inv.setItem(0, PlotParametersGui.setSwitchState(current, p.getPlayerParam(PlayerParamType.DEFAULT_PLOT_CHAT)));
			break;
		case 1:
			p.setPlayerParam(PlayerParamType.OPEN_GUI_ON_SNEAK, !p.getPlayerParam(PlayerParamType.OPEN_GUI_ON_SNEAK));
			inv.setItem(0, PlotParametersGui.setSwitchState(current, p.getPlayerParam(PlayerParamType.OPEN_GUI_ON_SNEAK)));
			break;
		}
		
		return true;
	}
}
