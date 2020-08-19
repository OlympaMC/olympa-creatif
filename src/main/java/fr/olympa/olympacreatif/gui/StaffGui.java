package fr.olympa.olympacreatif.gui;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.player.Gender;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.world.WorldManager;

public class StaffGui extends IGui {
	
	public StaffGui(IGui gui) {
		super(gui, "Interface staff", 2);
		
		inv.setItem(inv.getSize() - 1, new ItemStack(Material.AIR));
		
		//set items pour staff perms
		inv.setItem(0, ItemUtils.item(Material.ACACIA_FENCE_GATE, "§6Bypass kick/ban plot", "§7Permet d'entrer sur les plots même", "§7si le propriétaire vous en a banni"));
		inv.setItem(0 + 9, ItemUtils.item(Material.RED_WOOL, "§cInactif", "§7Rang nécessaire : " + StaffPerm.BYPASS_KICK_AND_BAN.getOlympaPerm().getMinGroup().getName(p.getGender())));
		
		inv.setItem(1, ItemUtils.item(Material.COMMAND_BLOCK, "§6Bypass commandes vanilla", "§7Permet de ne pas être affecté par", "§7les commandes vanilla du type /kill, /tp, ..."));
		inv.setItem(1 + 9, ItemUtils.item(Material.RED_WOOL, "§cInactif", "§7Rang nécessaire : " + StaffPerm.BYPASS_VANILLA_COMMANDS.getOlympaPerm().getMinGroup().getName(p.getGender())));
		
		inv.setItem(2, ItemUtils.item(Material.WOODEN_AXE, "§6Bypass WorldEdit", "§7Permet d'utiliser les fonctionnalités WorldEdit", "§7sur tous les plots et la route"));
		inv.setItem(2 + 9, ItemUtils.item(Material.RED_WOOL, "§cInactif", "§7Rang nécessaire : " + StaffPerm.BYPASS_WORLDEDIT.getOlympaPerm().getMinGroup().getName(p.getGender())));
		
		inv.setItem(3, ItemUtils.item(Material.REDSTONE_TORCH, "§6Fake owner", "§7Permet d'éditer le plot comme si vous", "§7en étiez le propriétaire"));
		inv.setItem(3 + 9, ItemUtils.item(Material.RED_WOOL, "§cInactif", "§7Rang nécessaire : " + StaffPerm.BYPASS_WORLDEDIT.getOlympaPerm().getMinGroup().getName(p.getGender())));

		inv.setItem(4, ItemUtils.item(Material.WHITE_STAINED_GLASS_PANE, " "));
		inv.setItem(4 + 9, ItemUtils.item(Material.WHITE_STAINED_GLASS_PANE, " "));
		
		inv.setItem(5, ItemUtils.item(Material.TNT, "§4Reset de la parcelle " + plot, "§cPour reset la parcelle, cliquez ici", "§cavec une TNT dans la main", "§4ATTENTION : Cette action est irréversible !"));
		
		//TODO clear plot, stoplag plot, ...
		
		if (p.hasStaffPerm(StaffPerm.BYPASS_KICK_AND_BAN))
			toggleSwitch(0);
		if (p.hasStaffPerm(StaffPerm.BYPASS_VANILLA_COMMANDS))
			toggleSwitch(1);
		if (p.hasStaffPerm(StaffPerm.BYPASS_WORLDEDIT))
			toggleSwitch(2);
		if (p.hasStaffPerm(StaffPerm.FAKE_OWNER_EVERYWHERE))
			toggleSwitch(3);
	}

	@Override
	public boolean onClick(Player player, ItemStack current, int slot, ClickType click) {
		
		switch (slot) {
		case 0:
			if (p.toggleStaffPerm(StaffPerm.BYPASS_KICK_AND_BAN)) 
				toggleSwitch(slot);
			
			break;
		case 1:
			if (p.toggleStaffPerm(StaffPerm.BYPASS_VANILLA_COMMANDS))
				toggleSwitch(slot);
			
			break;
		case 2:
			if (p.toggleStaffPerm(StaffPerm.BYPASS_WORLDEDIT))
				toggleSwitch(slot);
			
			break;
		case 3:
			if (p.toggleStaffPerm(StaffPerm.FAKE_OWNER_EVERYWHERE))	
				toggleSwitch(slot);
				
			break;
		}
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onClickCursor(Player player, ItemStack current, ItemStack cursor, int slot) {
		if (cursor.getType() != Material.TNT)
			return true;
		
		if (plot == null)
			return true;
		
		current.setType(Material.AIR);
		player.closeInventory();
		player.sendMessage("§cLa parcelle " + plot + " est en train de se régénérer. Merci de ne pas relancer le processus.");
		
		//reset du plot
		for (int x = plot.getPlotId().getX()/16 ; x < plot.getPlotId().getX()/16 + WorldManager.plotSize/16 ; x++)
			for (int z = plot.getPlotId().getZ()/16 ; x < plot.getPlotId().getZ()/16 + WorldManager.plotSize/16 ; z++)
				//plugin.getWorldManager().getWorld().
				return true;
		
		return true;
	}
	
	//switch l'apparence du switch
	private void toggleSwitch(int slot) {
		if (inv.getItem(slot) == null)
			return;
		
		ItemStack it = inv.getItem(slot);
		
		if (ItemUtils.hasEnchant(it, Enchantment.DURABILITY)) {
			it = ItemUtils.removeEnchant(it, Enchantment.DURABILITY);
			
			inv.getItem(slot + 9).setType(Material.RED_WOOL);
			inv.setItem(slot + 9, ItemUtils.name(inv.getItem(slot + 9), "§cInactif"));
		}else {
			it = ItemUtils.addEnchant(it, Enchantment.DURABILITY, 1);
			
			ItemMeta itMeta = it.getItemMeta();
			itMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			it.setItemMeta(itMeta);
			
			inv.getItem(slot + 9).setType(Material.GREEN_WOOL);
			inv.setItem(slot + 9, ItemUtils.name(inv.getItem(slot + 9), "§aActif"));
		}
	}
}
