package fr.olympa.olympacreatif.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.PlayerParamType;



public class PlayerParametersGui extends IGui{
	
	public PlayerParametersGui(IGui gui) {
		super(gui, "Paramètres de " + gui.getPlayer().getName(), 1, gui.staffPlayer);
		
		setItem(0, PlotParametersGui.setSwitchState(ItemUtils.item(Material.PAPER, "§6Activation par défaut du chat parcelle"), 
				p.hasPlayerParam(PlayerParamType.DEFAULT_PLOT_CHAT)),
				(it, i, s) -> {
					if (!isOpenByStaff)
						p.setPlayerParam(PlayerParamType.DEFAULT_PLOT_CHAT, !p.hasPlayerParam(PlayerParamType.DEFAULT_PLOT_CHAT));
					
					changeItem(it, PlotParametersGui.setSwitchState(it, p.hasPlayerParam(PlayerParamType.DEFAULT_PLOT_CHAT)));
				});
		
		setItem(1, PlotParametersGui.setSwitchState(ItemUtils.item(Material.CRAFTING_TABLE, "§6Double sneak pour ouvrir le menu"), 
				p.hasPlayerParam(PlayerParamType.OPEN_GUI_ON_SNEAK)), 
				(it, i, s) -> {
					if (!isOpenByStaff)
						p.setPlayerParam(PlayerParamType.OPEN_GUI_ON_SNEAK, !p.hasPlayerParam(PlayerParamType.OPEN_GUI_ON_SNEAK));
					
					changeItem(it, PlotParametersGui.setSwitchState(it, p.hasPlayerParam(PlayerParamType.OPEN_GUI_ON_SNEAK)));
				});
	}
	
	/*
	@Override
	public boolean onClick(Player player, ItemStack current, int slot, ClickType click) {
		super.onClick(player, current, slot, click);
		
		switch(slot) {
		case 0:
			p.setPlayerParam(PlayerParamType.DEFAULT_PLOT_CHAT, !p.getPlayerParam(PlayerParamType.DEFAULT_PLOT_CHAT));
			inv.setItem(0, PlotParametersGui.setSwitchState(current, p.getPlayerParam(PlayerParamType.DEFAULT_PLOT_CHAT)));
			break;
		case 1:
			p.setPlayerParam(PlayerParamType.OPEN_GUI_ON_SNEAK, !p.getPlayerParam(PlayerParamType.OPEN_GUI_ON_SNEAK));
			inv.setItem(1, PlotParametersGui.setSwitchState(current, p.getPlayerParam(PlayerParamType.OPEN_GUI_ON_SNEAK)));
			break;
		}
		
		return true;
	}*/
}
