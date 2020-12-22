package fr.olympa.olympacreatif.gui;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Consumer;

import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.olympacreatif.perks.UpgradesManager.UpgradeType;
import fr.olympa.olympacreatif.plot.PlotMembers.MemberInformations;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;

public class MembersGui extends IGui {
	
	public MembersGui(IGui gui) {
		super(gui, "Membres parcelle " + gui.getPlot().getPlotId() + " (" + gui.getPlot().getMembers().getCount() + "/" + 
				UpgradeType.BONUS_MEMBERS_LEVEL.getValueOf(gui.getPlayer().getUpgradeLevel(UpgradeType.BONUS_MEMBERS_LEVEL)) + ")", 3);
		
		//index de la tête à placer
		int headIndex = -1;
		
		for (Entry<MemberInformations, PlotRank> e : plot.getMembers().getMembers().entrySet()) {
			
			headIndex++;
			final int thisHeadIndex = headIndex;
			
			TriConsumer<ItemStack, ClickType, Integer> action = (it, c, s) -> {
				MemberInformations target = e.getKey(); 
				
				if (c == ClickType.LEFT && canPromote(target))
					plot.getMembers().set(target, PlotRank.getPlotRank(plot.getMembers().getPlayerLevel(target) + 1));
				
				else if (c == ClickType.RIGHT && canDemote(target))
					plot.getMembers().set(target, PlotRank.getPlotRank(plot.getMembers().getPlayerLevel(target) - 1));

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
		int playerLevel = plot.getMembers().getPlayerLevel(p);
		int memberLevel = plot.getMembers().getPlayerLevel(member);
		
		if (playerLevel >= 3 && memberLevel > 0 && memberLevel < playerLevel)
			return true;
		else
			return false;
	}

	private boolean canPromote(MemberInformations member) {
		int playerLevel = plot.getMembers().getPlayerLevel(p);
		int memberLevel = plot.getMembers().getPlayerLevel(member);
		
		if (playerLevel >= 3 && playerLevel > memberLevel + 1)
			return true;
		else
			return false;
	}
	
	/*
	@Override
	public boolean onClick(Player player, ItemStack current, int slot, ClickType click) {
		super.onClick(player, current, slot, click);
		
		MemberInformations target = null;
		if (slot >= 0 && slot < members.size())
			target = members.get(slot);
		else
			return true;
		
		if (click == ClickType.LEFT && canPromote(target))
			plot.getMembers().set(target, PlotRank.getPlotRank(plot.getMembers().getPlayerLevel(target) + 1));
		
		else if (click == ClickType.RIGHT && canDemote(target))
			plot.getMembers().set(target, PlotRank.getPlotRank(plot.getMembers().getPlayerLevel(target) - 1));
		
		inv.setItem(slot, getHeadLore(current, target, plot.getMembers().getPlayerRank(target)));
		
		return true;
	}

	@Override
	public boolean onClickCursor(Player p, ItemStack current, ItemStack cursor, int slot) {
		return true;
	}
	
	private class UpdateHeadConsumer implements TriConsumer<ItemStack, ClickType, Integer>{

		private MemberInformations target;
		
		private UpdateHeadConsumer(MemberInformations member) {
			target = member;
		}
		
		@Override
		public void accept(ItemStack it, ClickType c, Integer s) {
			
			if (c == ClickType.LEFT && canPromote(target))
				plot.getMembers().set(target, PlotRank.getPlotRank(plot.getMembers().getPlayerLevel(target) + 1));
			
			else if (c == ClickType.RIGHT && canDemote(target))
				plot.getMembers().set(target, PlotRank.getPlotRank(plot.getMembers().getPlayerLevel(target) - 1));

			setItem(s, ItemUtils.lore(inv.getItem(s), getHeadLore(target, plot.getMembers().getPlayerRank(target))), new UpdateHeadConsumer(target));
		}
	}*/
}
