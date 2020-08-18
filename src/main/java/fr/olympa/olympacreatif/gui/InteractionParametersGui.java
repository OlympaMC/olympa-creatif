package fr.olympa.olympacreatif.gui;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotParamType;
import fr.olympa.olympacreatif.plot.PlotParameters;

public class InteractionParametersGui extends OlympaGUI {

	private OlympaCreatifMain plugin;
	private OlympaPlayerCreatif pc;
	private Plot plot;
	private String[] stateAllowed = new String[] {" ", "§eEtat : §aautorisé", "§7Cliquez pour changer l'état. Si autorisé, les visiteurs", "§7pourront interragir avec ce bloc, sinon non."};
	private String[] stateDenied = new String[] {" ", "§eEtat : §cinterdit", "§7Cliquez pour changer l'état. Si autorisé, les visiteurs", "§7pourront interragir avec ce bloc, sinon non."}; 
	
	@SuppressWarnings("unchecked")
	public InteractionParametersGui(OlympaCreatifMain plugin, Player p, Plot plot) {
		super("§6Interactions du plot " + plot.getPlotId(), 6);
		this.plugin = plugin;
		this.pc = AccountProvider.get(p.getUniqueId());
		this.plot = plot;
		
		inv.setItem(inv.getSize() - 1, MainGui.getBackItem());
		
		//pour tous les items interdits possibles, ajout de l'item au gui selon son état (interdit/autorisé)
		for (Material mat : PlotParamType.getAllPossibleIntaractibleBlocks()) {
			ItemStack it = new ItemStack(mat);
			ItemMeta im = it.getItemMeta();
			im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			it.setItemMeta(im);

			if (((ArrayList<Material>) plot.getParameters().getParameter(PlotParamType.LIST_ALLOWED_INTERRACTION)).contains(mat)) {
				it = ItemUtils.addEnchant(it, Enchantment.DURABILITY, 1);

				if (plot.getMembers().getPlayerLevel(pc) < 3)
					it = ItemUtils.loreAdd(it, "§eEtat : §aautorisé");
				else
					it = ItemUtils.loreAdd(it, stateAllowed);
			}
			else
				if (plot.getMembers().getPlayerLevel(pc) < 3)
					it = ItemUtils.loreAdd(it, "§eEtat : §cinterdit");
				else
					it = ItemUtils.loreAdd(it, stateDenied);
			
			inv.addItem(it);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (slot == inv.getSize() - 1) {
			MainGui.openMainGui(p);
			return true;
		}
		
		//changement de l'état d'autorisation de l'interraction pour le bloc cliqué si le joueur a la permission
		if (click == ClickType.LEFT && plot.getMembers().getPlayerLevel(pc) >= 3 && slot < PlotParamType.getAllPossibleIntaractibleBlocks().size()) {
			if (((ArrayList<Material>) plot.getParameters().getParameter(PlotParamType.LIST_ALLOWED_INTERRACTION)).contains(current.getType())) {
				((ArrayList<Material>) plot.getParameters().getParameter(PlotParamType.LIST_ALLOWED_INTERRACTION)).remove(current.getType());
				current = ItemUtils.removeEnchant(current, Enchantment.DURABILITY);
				current = ItemUtils.lore(current, stateDenied);
			}
			else {
				((ArrayList<Material>) plot.getParameters().getParameter(PlotParamType.LIST_ALLOWED_INTERRACTION)).add(current.getType());
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
