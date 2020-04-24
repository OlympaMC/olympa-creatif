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
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotParamType;
import fr.olympa.olympacreatif.plot.PlotParameters;

public class InteractionParametersGui extends OlympaGUI {

	private OlympaCreatifMain plugin;
	private Player p;
	private Plot plot;
	
	@SuppressWarnings("unchecked")
	public InteractionParametersGui(OlympaCreatifMain plugin, Player p, Plot plot) {
		super("§6Interactions du plot : " + plot.getId().getAsString(), 6);
		this.plugin = plugin;
		this.p = p;
		this.plot = plot;

		inv.setItem(53, ItemUtils.item(Material.ACACIA_DOOR, "§cRetour", ""));
		
		//pour tous les items interdits possibles, ajout de l'item au gui selon son état (interdit/autorisé)
		for (Material mat : PlotParamType.getAllPossibleAllowedBlocks()) {
			ItemStack it = new ItemStack(mat);
			ItemMeta im = it.getItemMeta();
			im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			it.setItemMeta(im);

			if (((ArrayList<Material>) plot.getParameters().getParameter(PlotParamType.LIST_ALLOWED_INTERRACTION)).contains(mat)) {
				it = ItemUtils.loreAdd(it, "§eEtat : §aautorisé");
				it = ItemUtils.addEnchant(it, Enchantment.DURABILITY, 1);
			}
			else
				it = ItemUtils.loreAdd(it, "§eEtat : §cinterdit");
			
			if (plot.getMembers().getPlayerLevel(p) >= 3)
				it = ItemUtils.loreAdd(it, " ", "§8Clic gauche : changer l'état du bloc");
			
			inv.addItem(it);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (slot == 53) {
			if (plot == null)
				new MainGui(plugin, p, plot, "§9Menu").create(p);
			else
				new MainGui(plugin, p, plot, "§9Menu >> " + plot.getId().getAsString()).create(p);
			return true;
		}
		
		//changement de l'état d'autorisation de l'interraction pour le bloc cliqué si le joueur a la permission
		if (click == ClickType.LEFT && plot.getMembers().getPlayerLevel(p) >= 3 && slot < PlotParamType.getAllPossibleAllowedBlocks().size()) {
			if (((ArrayList<Material>) plot.getParameters().getParameter(PlotParamType.LIST_ALLOWED_INTERRACTION)).contains(current.getType())) {
				((ArrayList<Material>) plot.getParameters().getParameter(PlotParamType.LIST_ALLOWED_INTERRACTION)).remove(current.getType());
				current = ItemUtils.removeEnchant(current, Enchantment.DURABILITY);
				current = ItemUtils.lore(current, "§eEtat : §cinterdit", " ", "§8Clic gauche : changer l'état du bloc");
			}
			else {
				((ArrayList<Material>) plot.getParameters().getParameter(PlotParamType.LIST_ALLOWED_INTERRACTION)).add(current.getType());
				current = ItemUtils.addEnchant(current, Enchantment.DURABILITY, 1);
				current = ItemUtils.lore(current, "§eEtat : §aautorisé", " ", "§8Clic gauche : changer l'état du bloc");
			}
					
		}
		return true;
	}

	@Override
	public boolean onClickCursor(Player p, ItemStack current, ItemStack cursor, int slot) {
		return true;
	}

}
