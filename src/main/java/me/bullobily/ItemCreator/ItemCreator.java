package me.bullobily.ItemCreator;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ItemCreator {

	ItemStack item;
	ItemMeta itemMeta;

    /**
     * Cr�e un item facilement �ditable
     * @item ItemStack de base (type et quantit�)
     */
	public ItemCreator (ItemStack item) {
		this.item = item;
		itemMeta = item.getItemMeta();
		itemMeta.setLore(new ArrayList<String>());
	}

    /**
     * Ajouter un enchantement unsafe
     * @ench enchantement de l'enum
     * @level niveau d'enchantement
     */
	public void addEnchant(Enchantment ench, int level) {
		itemMeta.addEnchant(ench, level, true);
	}
	
	public void setName(String name) {
		itemMeta.setDisplayName(name);
	}
	
	public void addLore(String lore) {
		List<String> list = itemMeta.getLore();
		list.add(lore);
		itemMeta.setLore(list);
	}
	
	public void setLore(String lore) {
		List<String> list = new ArrayList<String>();
		String row = "";
		int rowSize = 0;
		int maxRowSize = 100;
		
		for (String s : lore.split(" ")) {
			row += s;
			rowSize += s.length();
			
			if (rowSize > maxRowSize) {
				rowSize = 0;
				list.add(row.replace("&", "§"));
				row = "";
			}	
		}
		
		
		itemMeta.setLore(list);
	}
	
	public void addItemFlag(ItemFlag flag) {
		itemMeta.addItemFlags(flag);
	}
	
	public void setUnbreakabke(boolean b) {
		itemMeta.setUnbreakable(b);
	}
	
	public void addPotionEffect(PotionEffectType effect, int duration, int strengh) {
		if (!(itemMeta instanceof PotionMeta))
			return;
		
		((PotionMeta) itemMeta).addCustomEffect(new PotionEffect(effect, duration, strengh), true); 
	}

	public void setSkullOwner(String playerName) {
		if (!(itemMeta instanceof SkullMeta))
			return;
		((SkullMeta) itemMeta).setOwner(playerName);
	}
	
	public void setLeatherArmorColor(Color c) {
		if (!(itemMeta instanceof LeatherArmorMeta))
			return;

		((LeatherArmorMeta) itemMeta).setColor(c); 
		
	}
	
	public ItemStack getItem() {
		item.setItemMeta(itemMeta);
		return item;
	}
	
}













