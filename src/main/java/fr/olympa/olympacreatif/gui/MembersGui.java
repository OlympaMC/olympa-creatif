package fr.olympa.olympacreatif.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.perks.UpgradesManager.UpgradeType;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotMembers.MemberInformations;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;

public class MembersGui extends OlympaGUI {

	private OlympaCreatifMain plugin;
	private Plot plot;
	private Player p;
	private OlympaPlayerCreatif pc;
	
	private List<MemberInformations> members = new ArrayList<MemberInformations>();
	
	public MembersGui(OlympaCreatifMain plugin, Player p, Plot plot) {
		super("Membres parcelle " + plot.getPlotId() + "(" + plot.getMembers().getCount() + "/" + 
				UpgradeType.BONUS_MEMBERS_LEVEL.getValueOf(((OlympaPlayerCreatif)AccountProvider.get(p.getUniqueId())).getUpgradeLevel(UpgradeType.BONUS_MEMBERS_LEVEL)) + ")", 3);
		
		this.plugin = plugin;
		this.p = p;
		this.pc = AccountProvider.get(p.getUniqueId());
		this.plot = plot;
		
		inv.setItem(inv.getSize() - 1, MainGui.getBackItem());
		
		members = new ArrayList<MemberInformations>(plot.getMembers().getMembers().keySet());
		
		//index de la tête à placer
		int headIndex = -1;
		
		for (Entry<MemberInformations, PlotRank> e : plot.getMembers().getMembers().entrySet()) {
			
			headIndex++;
			final int thisHeadIndex = headIndex;
			
			//création de la tête du joueur
			Consumer<ItemStack> consumer = sk -> inv.setItem(thisHeadIndex, addBasicInfos(sk, e.getKey(), e.getValue()));

			consumer.accept(ItemUtils.item(Material.PLAYER_HEAD, "§6" + e.getKey().getName()));
			ItemUtils.skull(consumer, "§6" + e.getKey().getName(), e.getKey().getName());
		}
	}
	
	private ItemStack addBasicInfos(ItemStack item, MemberInformations member, PlotRank rank) {
		item = ItemUtils.lore(item, "§6Rang : " + rank.getRankName());
		if (Bukkit.getPlayer(member.getUUID()) != null)
			item = ItemUtils.loreAdd(item, "§6Statut : §aen ligne");
		else
			item = ItemUtils.loreAdd(item, "§6Statut : §chors ligne");

		boolean promote = canPromote(member);
		boolean demote = canDemote(member);
		
		if (promote || demote) {
			item = ItemUtils.loreAdd(item, " ");
			if (promote)
				item = ItemUtils.loreAdd(item, "§7Clic gauche : promouvoir");
			if (demote)
				item = ItemUtils.loreAdd(item, "§7Clic droit : rétrograder");
		}
		
		return item;
	}
	
	private boolean canDemote(MemberInformations member) {
		int playerLevel = plot.getMembers().getPlayerLevel(pc);
		int memberLevel = plot.getMembers().getPlayerLevel(member);
		
		if (playerLevel >= 3 && memberLevel > 0 && memberLevel < 4)
			return true;
		else
			return false;
	}

	private boolean canPromote(MemberInformations member) {
		int playerLevel = plot.getMembers().getPlayerLevel(pc);
		int memberLevel = plot.getMembers().getPlayerLevel(member);
		
		if (playerLevel >= 3 && playerLevel > memberLevel + 1)
			return true;
		else
			return false;
	}
	
	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (slot == inv.getSize() - 1) {
			MainGui.openMainGui(p);
			return true;
		}
		
		MemberInformations target = null;
		if (slot >= 0 && slot < members.size())
			target = members.get(slot);
		else
			return true;
		
		if (click == ClickType.LEFT && canPromote(target))
			plot.getMembers().set(target, PlotRank.getPlotRank(plot.getMembers().getPlayerLevel(target) + 1));
		
		else if (click == ClickType.RIGHT && canDemote(target))
			plot.getMembers().set(target, PlotRank.getPlotRank(plot.getMembers().getPlayerLevel(target) - 1));
		
		inv.setItem(slot, addBasicInfos(current, target, plot.getMembers().getPlayerRank(target)));
		
		return true;
	}
	

	@Override
	public boolean onClickCursor(Player p, ItemStack current, ItemStack cursor, int slot) {
		return true;
	}

}
