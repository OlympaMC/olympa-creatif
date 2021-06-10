package fr.olympa.olympacreatif.gui;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.olympacreatif.plot.PlotParamType;
import fr.olympa.olympacreatif.plot.PlotPerm;


public class InteractionParametersGui extends IGui {

	private String[] stateAllowed = new String[] {" ", "§eEtat : §aautorisé", "§7Cliquez pour changer l'état." + "§7Si autorisé, les visiteurs pourront", "§7interragir avec ce bloc, sinon non."};
	private String[] stateDenied = new String[] {" ", "§eEtat : §cinterdit", "§7Cliquez pour changer l'état." + "§7Si autorisé, les visiteurs pourront", "§7interragir avec ce bloc, sinon non."}; 
	
	public InteractionParametersGui(IGui gui) {
		super(gui, "Interactions parcelle " + gui.getPlot().getId(), 6, gui.staffPlayer);
		
		//pour tous les items interdits possibles, ajout de l'item au gui selon son état (interdit/autorisé)
		for (Material mat : PlotParamType.getAllPossibleIntaractibleBlocks()) {
			ItemStack it = new ItemStack(mat);
			ItemMeta im = it.getItemMeta();
			im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			it.setItemMeta(im);
			
			if (plot.getParameters().getParameter(PlotParamType.LIST_ALLOWED_INTERRACTION).contains(mat)) {
				it = ItemUtils.addEnchant(it, Enchantment.DURABILITY, 1);

				if (!PlotPerm.CHANGE_PARAM_INTERRACTION.has(plot, p))
					it = ItemUtils.loreAdd(it, "§eEtat : §aautorisé");
				else
					it = ItemUtils.loreAdd(it, stateAllowed);
				
			}else
				if (!PlotPerm.CHANGE_PARAM_INTERRACTION.has(plot, p))
					it = ItemUtils.loreAdd(it, "§eEtat : §cinterdit");
				else
					it = ItemUtils.loreAdd(it, stateDenied);
			
			inv.addItem(it);
		}
	}

	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		super.onClick(p, current, slot, click);
		
		//changement de l'état d'autorisation de l'interraction pour le bloc cliqué si le joueur a la permission
		if (click == ClickType.LEFT && PlotPerm.CHANGE_PARAM_INTERRACTION.has(plot, getPlayer()) && slot < PlotParamType.getAllPossibleIntaractibleBlocks().size()) {
			if (plot.getParameters().getParameter(PlotParamType.LIST_ALLOWED_INTERRACTION).contains(current.getType())) {
				plot.getParameters().getParameter(PlotParamType.LIST_ALLOWED_INTERRACTION).remove(current.getType());
				current = ItemUtils.removeEnchant(current, Enchantment.DURABILITY);
				current = ItemUtils.lore(current, stateDenied);
			}
			else {
				plot.getParameters().getParameter(PlotParamType.LIST_ALLOWED_INTERRACTION).add(current.getType());
				current = ItemUtils.addEnchant(current, Enchantment.DURABILITY, 1);
				current = ItemUtils.lore(current, stateAllowed);
			}
					
		}
		return true;
	}

	@Override
	public boolean onClickCursor(Player p, ItemStack current, ItemStack cursor, int slot) {
		return true;
	}

}
