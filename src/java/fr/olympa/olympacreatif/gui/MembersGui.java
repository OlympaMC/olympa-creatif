package fr.olympa.olympacreatif.gui;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.perks.UpgradesManager.UpgradeType;
import fr.olympa.olympacreatif.plot.PlotMembers.MemberInformations;
import fr.olympa.olympacreatif.plot.PlotPerm;
import fr.olympa.olympacreatif.plot.PlotPerm.PlotRank;


public class MembersGui extends IGui {
	
	public MembersGui(IGui gui) {
		super(gui, "Membres parcelle " + gui.getPlot().getId() + " (" + gui.getPlot().getMembers().getCount() + "/" + 
				UpgradeType.BONUS_MEMBERS_LEVEL.getDataOf(gui.p).value + ")", 3, gui.staffPlayer);
		
		//affichage des perms par rang
		int i = inv.getSize() - 1 - PlotRank.values().length;
		
		for(PlotRank r : Arrays.asList(PlotRank.values())) {
			if (r.getMat() == null) continue;
			
			ItemStack it = ItemUtils.item(r.getMat(), "§eInformations rang " + r.getRankName());
			if (r.getLevel() == 1)
				it = ItemUtils.lore(it, "§7Permissions incluses :", " ");				
			else
				it = ItemUtils.lore(it, "§7Permissions de " + r.getDowngrade().getRankName() + "§7, et :", " ");
			
			//Bukkit.broadcastMessage(r + " : " + r.getPermsDescs());
			
			for (String s : r.getPermsDescs())
				it = ItemUtils.loreAdd(it, "§7- " + s);
			
			setItem(i++, it, null);
		}
		
		//index de la tête à placer
		int headIndex = -1;
		
		for (Entry<MemberInformations, PlotRank> e : plot.getMembers().getMembers().entrySet()) {
			
			headIndex++;
			final int thisHeadIndex = headIndex;
			//Bukkit.broadcastMessage(e.getKey().toString() + " : " + e.getValue());
			
			TriConsumer<ItemStack, ClickType, Integer> action = (it, c, s) -> {
				MemberInformations target = e.getKey(); 
				
				if (c == ClickType.LEFT && canPromote(target))
					plot.getMembers().set(target, plot.getMembers().getPlayerRank(target).getUpgrade());
				
				else if (c == ClickType.RIGHT && canDemote(target))
					plot.getMembers().set(target, plot.getMembers().getPlayerRank(target).getDowngrade());

				changeItem(it, ItemUtils.lore(it.clone(), getHeadLore(target, plot.getMembers().getPlayerRank(target))));
			};
			
			//création de la tête du joueur
			Consumer<ItemStack> createHead = sk -> {
				ItemUtils.lore(sk, getHeadLore(e.getKey(), e.getValue()));
				
				setItem(thisHeadIndex, sk, action);
			};

			createHead.accept(ItemUtils.item(Material.PLAYER_HEAD, "§6" + e.getKey().getName()));
			ItemUtils.skull(createHead, "§6" + e.getKey().getName(), e.getKey().getName());
		}
	}
	
	private String[] getHeadLore(MemberInformations member, PlotRank rank) {
		
		String[] lore = new String[5];
		
		lore[0] = "§6Rang : " + rank.getRankName();
		
		if (Bukkit.getPlayer(member.getUUID()) != null)
			lore[1] = "§6Statut : §aen ligne";
		else
			lore[1] = "§6Statut : §chors ligne";

		boolean promote = canPromote(member);
		boolean demote = canDemote(member);
		
		if (promote || demote) {
			lore[2] = " ";
			if (promote)
				lore[3] = "§7Clic gauche : promouvoir";
			if (demote)
				lore[4] = "§7Clic droit : rétrograder";
		}
		
		return lore;
	}
	
	private boolean canDemote(MemberInformations member) {
		int playerLevel = isOpenByStaff ? 
				staffPlayer.hasStaffPerm(StaffPerm.OWNER_EVERYWHERE) ? 
						PlotRank.OWNER.getLevel() : 
						PlotRank.VISITOR.getLevel() : 
				plot.getMembers().getPlayerRank(p).getLevel();
						
		int memberLevel = plot.getMembers().getPlayerRank(member).getLevel();

		if ((PlotPerm.PROMOTE_DEMOTE.has(plot, p) || (isOpenByStaff && staffPlayer.hasStaffPerm(StaffPerm.OWNER_EVERYWHERE))) 
				&& memberLevel > 0 && memberLevel < playerLevel)
			return true;
		else
			return false;
	}

	private boolean canPromote(MemberInformations member) {
		int playerLevel = isOpenByStaff ? 
								staffPlayer.hasStaffPerm(StaffPerm.OWNER_EVERYWHERE) ? 
										PlotRank.OWNER.getLevel() : 
										PlotRank.VISITOR.getLevel() : 
								plot.getMembers().getPlayerRank(p).getLevel();
						
		int memberLevel = plot.getMembers().getPlayerRank(member).getLevel();
		
		if ((PlotPerm.PROMOTE_DEMOTE.has(plot, p) || (isOpenByStaff && staffPlayer.hasStaffPerm(StaffPerm.OWNER_EVERYWHERE))) 
				&& playerLevel > memberLevel + 1/* && memberLevel < PlotRank.OWNER.getLevel()*/)
			return true;
		else
			return false;
	}
}
