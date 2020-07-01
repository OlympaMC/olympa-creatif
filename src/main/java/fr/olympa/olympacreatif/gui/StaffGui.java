package fr.olympa.olympacreatif.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.player.Gender;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;

public class StaffGui extends OlympaGUI {

	private OlympaCreatifMain plugin;
	private OlympaPlayerCreatif pc;
	public StaffGui(OlympaCreatifMain plugin, Player p) {
		super("Interface staff", 2);
		
		this.plugin = plugin;
		pc = AccountProvider.get(p.getUniqueId());
		
		inv.setItem(0, ItemUtils.item(Material.ACACIA_FENCE_GATE, "§6Bypass kick/ban plot", "§7Permet d'entrer sur les plots même", "§7si le propriétaire vous en a banni"));
		inv.setItem(0 + 9, ItemUtils.item(Material.RED_WOOL, "§cInactif", "§7Rang nécessaire : " + StaffPerm.BYPASS_KICK_AND_BAN.getOlympaPerm().getGroup().getName(pc.getGender())));
		
		inv.setItem(1, ItemUtils.item(Material.COMMAND_BLOCK, "§6Bypass commandes vanilla", "§7Permet de ne pas être affecté par", "§7les commandes vanilla du type /kill, /tp, ..."));
		inv.setItem(1 + 9, ItemUtils.item(Material.RED_WOOL, "§cInactif", "§7Rang nécessaire : " + StaffPerm.BYPASS_VANILLA_COMMANDS.getOlympaPerm().getGroup().getName(pc.getGender())));
		
		inv.setItem(2, ItemUtils.item(Material.WOODEN_AXE, "§6Bypass WorldEdit", "§7Permet d'utiliser les fonctionnalités WorldEdit", "§7sur tous les plots et la route"));
		inv.setItem(2 + 9, ItemUtils.item(Material.RED_WOOL, "§cInactif", "§7Rang nécessaire : " + StaffPerm.BYPASS_WORLDEDIT.getOlympaPerm().getGroup().getName(pc.getGender())));
		
		inv.setItem(3, ItemUtils.item(Material.REDSTONE_TORCH, "§6Fake owner", "§7Permet d'éditer le plot comme si vous", "§7en étiez le propriétaire"));
		inv.setItem(3 + 9, ItemUtils.item(Material.RED_WOOL, "§cInactif", "§7Rang nécessaire : " + StaffPerm.BYPASS_WORLDEDIT.getOlympaPerm().getGroup().getName(pc.getGender())));

		if (pc.hasStaffPerm(StaffPerm.BYPASS_KICK_AND_BAN))
			toggleSwitch(0);
		if (pc.hasStaffPerm(StaffPerm.BYPASS_VANILLA_COMMANDS))
			toggleSwitch(1);
		if (pc.hasStaffPerm(StaffPerm.BYPASS_WORLDEDIT))
			toggleSwitch(2);
		if (pc.hasStaffPerm(StaffPerm.FAKE_OWNER_EVERYWHERE))
			toggleSwitch(3);
	}

	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		
		Bukkit.broadcastMessage(p.getName() + " a la perm bypass kick ban plot : " + StaffPerm.BYPASS_KICK_AND_BAN.getOlympaPerm().hasPermission(p.getUniqueId()));
		
		switch (slot) {
		case 0:
			if (StaffPerm.BYPASS_KICK_AND_BAN.getOlympaPerm().hasPermission(pc)) {
				pc.toggleStaffPerm(StaffPerm.BYPASS_KICK_AND_BAN);
				toggleSwitch(slot);
			}
			break;
		case 1:
			if (StaffPerm.BYPASS_VANILLA_COMMANDS.getOlympaPerm().hasPermission(pc)) {
				pc.toggleStaffPerm(StaffPerm.BYPASS_VANILLA_COMMANDS);
				toggleSwitch(slot);
			}
			break;
		case 2:
			if (StaffPerm.BYPASS_WORLDEDIT.getOlympaPerm().hasPermission(pc)) {
				pc.toggleStaffPerm(StaffPerm.BYPASS_WORLDEDIT);
				toggleSwitch(slot);
			}
			break;
		case 3:
			if (StaffPerm.FAKE_OWNER_EVERYWHERE.getOlympaPerm().hasPermission(pc)) {
				pc.toggleStaffPerm(StaffPerm.FAKE_OWNER_EVERYWHERE);
				toggleSwitch(slot);
			}
			break;
		}
		
		
		return true;
	}
	//switch l'apparence du switch
	private void toggleSwitch(int slot) {
		if (inv.getItem(slot) == null)
			return;
		
		ItemStack it = inv.getItem(slot);
		
		if (ItemUtils.hasEnchant(it, Enchantment.DURABILITY)) {
			it = ItemUtils.removeEnchant(it, Enchantment.DURABILITY);
			
			inv.getItem(slot).setType(Material.RED_WOOL);
			inv.setItem(slot, ItemUtils.name(inv.getItem(slot), "§cInactif"));
		}else {
			it = ItemUtils.addEnchant(it, Enchantment.DURABILITY, 1);

			inv.getItem(slot).setType(Material.GREEN_WOOL);
			inv.setItem(slot, ItemUtils.name(inv.getItem(slot), "§aActif"));
		}
	}
}
