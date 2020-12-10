package fr.olympa.olympacreatif.gui;

import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.region.tracking.BypassCommand;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.utils.NBTcontrollerUtil;

public class StaffGui extends IGui {
	
	public StaffGui(IGui gui) {
		super(gui, "Interface staff", 2);
		
		inv.setItem(inv.getSize() - 1, new ItemStack(Material.AIR));
		
		//set items pour staff perms
		OlympaPermission perm = PermissionsList.STAFF_BYPASS_PLOT_KICK_AND_BAN;
		StaffPerm sPerm = StaffPerm.BYPASS_KICK_AND_BAN;
		
		setItem(0, ItemUtils.item(Material.ACACIA_FENCE_GATE, "§6Bypass kick/ban plot", "§7Permet d'entrer sur les plots même", "§7si le propriétaire vous en a banni"), getConsumer(perm, sPerm));	
		setItem(0 + 9, getStateIndicator(p.hasStaffPerm(sPerm), perm), null);

		perm = PermissionsList.STAFF_BYPASS_VANILLA_COMMANDS;
		sPerm = StaffPerm.BYPASS_VANILLA_COMMANDS;
		setItem(1, ItemUtils.item(Material.COMMAND_BLOCK, "§6Bypass commandes vanilla", "§7Permet de ne pas être affecté par", "§7les commandes vanilla du type /kill, /tp, ..."), getConsumer(perm, sPerm));	
		setItem(1 + 9, getStateIndicator(p.hasStaffPerm(sPerm), perm), null);

		perm = PermissionsList.STAFF_BYPASS_WORLDEDIT;
		sPerm = StaffPerm.BYPASS_WORLDEDIT;
		setItem(2, ItemUtils.item(Material.WOODEN_AXE, "§6Bypass WorldEdit", "§7Permet d'utiliser les fonctionnalités WorldEdit", "§7sur tous les plots et la route"), getConsumer(perm, sPerm));	
		setItem(2 + 9, getStateIndicator(p.hasStaffPerm(sPerm), perm), null);

		perm = PermissionsList.STAFF_PLOT_FAKE_OWNER;
		sPerm = StaffPerm.FAKE_OWNER_EVERYWHERE;
		setItem(3, ItemUtils.item(Material.REDSTONE_TORCH, "§6Fake owner", "§7Permet d'éditer les paramètres du plot comme si", "§7vous en étiez le propriétaire"), getConsumer(perm, sPerm));	
		setItem(3 + 9, getStateIndicator(p.hasStaffPerm(sPerm), perm), null);

		setItem(4, ItemUtils.item(Material.WHITE_STAINED_GLASS_PANE, " "), null);
		setItem(4 + 9, ItemUtils.item(Material.WHITE_STAINED_GLASS_PANE, " "), null);
		
		//désactivation worldedit et tags custom

		final OlympaPermission p1 = PermissionsList.STAFF_DEACTIVATE_CUSTOM_TAGS;
		setItem(5, ItemUtils.item(Material.PAPER, "§6Désactivation de tous les tags custom", "§2Fonction de sécurité.", "§2Clic molette pour modifier.", " ", "§7Permet de désactiver tous", "§7les tags NBT custom sur le serveur en cas", "§7de problème.", "§cAttention : au redémarage, les tags custom seront de nouveau activés !"), 
				(it, c, s) -> {
					if (!p1.hasPermission(p) || c != ClickType.MIDDLE)
						return;
					
					NBTcontrollerUtil.setDenyAllCustomFlags(!NBTcontrollerUtil.getDenyAllCustomFlags());
					setItem(5 + 9, getStateIndicator(NBTcontrollerUtil.getDenyAllCustomFlags(), p1), null);
				});	
		setItem(5 + 9, getStateIndicator(NBTcontrollerUtil.getDenyAllCustomFlags(), p1), null);

		
		final OlympaPermission p2 = PermissionsList.STAFF_DEACTIVATE_WORLD_EDIT;
		setItem(6, ItemUtils.item(Material.DIAMOND_AXE, "§6Désactivation totale de WorldEdit", "§2Fonction de sécurité.", "§2Clic molette pour modifier.", " ", "§7Permet d'interromptre instantanément", "§7toutes les tâches WorldEdit sur le serveur", "§7et de désactiver le plugin.", "§cAttention : au redémarage, WorldEdit sera de nouveau activé !", "§cIl est impossible de réactiver WorldEdit une fois qu'il a été désactivé."),
				(it, c, s) -> {
					if (!p2.hasPermission(p) || !plugin.isWeEnabled() || c != ClickType.MIDDLE)
						return;
					
					plugin.disableWorldEdit();
					
					setItem(6 + 9, getStateIndicator(!plugin.isWeEnabled(), p2), null);
				});
		setItem(6 + 9, getStateIndicator(!plugin.isWeEnabled(), p2), null);
		
	}

	private TriConsumer<ItemStack, ClickType, Integer> getConsumer(OlympaPermission perm, StaffPerm sPerm){
		return (it, c, s) -> {
			if (!perm.hasPermission(p))
				return;
			
			p.toggleStaffPerm(sPerm);
			setItem(s + 9, getStateIndicator(p.hasStaffPerm(sPerm), perm), null);
		};
	}
	
	private ItemStack getStateIndicator(boolean state, OlympaPermission perm) {
		if (state)
			return ItemUtils.item(Material.LIME_WOOL, "§aActif", "§7Rang nécessaire : " + perm.getMinGroup().getName(p.getGender()), " ", "§7Pour modifier la valeur, cliquez", "§7sur l'item au dessus");
		else
			return ItemUtils.item(Material.RED_WOOL, "§cInactif", "§7Rang nécessaire : " + perm.getMinGroup().getName(p.getGender()), " ", "§7Pour modifier la valeur, cliquez", "§7sur l'item au dessus");
	}
}
