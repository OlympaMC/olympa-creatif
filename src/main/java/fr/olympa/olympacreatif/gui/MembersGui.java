package fr.olympa.olympacreatif.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.objects.OlympaPlayerInformations;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;

public class MembersGui extends OlympaGUI {

	private OlympaCreatifMain plugin;
	private Plot plot;
	private Player p;

	List<OlympaPlayerInformations> rank1 = new ArrayList<OlympaPlayerInformations>();
	List<OlympaPlayerInformations> rank2 = new ArrayList<OlympaPlayerInformations>();
	List<OlympaPlayerInformations> rank3 = new ArrayList<OlympaPlayerInformations>();
	List<OlympaPlayerInformations> rank4 = new ArrayList<OlympaPlayerInformations>();
	
	public MembersGui(OlympaCreatifMain plugin, Player p, Plot plot) {
		super("§6Membres du plot : " + plot.getId().getAsString(), 3);
		
		this.plugin = plugin;
		this.p = p;
		this.plot = plot;
		
		inv.setItem(26, ItemUtils.item(Material.ACACIA_DOOR, "§cRetour", ""));
		
		for (Entry<OlympaPlayerInformations, PlotRank> e : plot.getMembers().getList().entrySet()) {
			switch (e.getValue()) {
			case CO_OWNER:
				rank3.add(e.getKey());
				break;
			case MEMBER:
				rank1.add(e.getKey());
				break;
			case OWNER:
				rank4.add(e.getKey());
				break;
			case TRUSTED:
				rank2.add(e.getKey());
				break;
			}
		}
		
		for (Entry<OlympaPlayerInformations, PlotRank> e : plot.getMembers().getList().entrySet()) {
			//création de la tête du joueur
			ItemStack skull = ItemUtils.skull(e.getKey().getName(), e.getKey().getName(), "§6Rang : " + e.getValue().getRankName());

			//définition de son statut
			if (Bukkit.getPlayer(e.getKey().getUUID()) != null)
				skull = ItemUtils.loreAdd(skull, "§eStatut : §aen ligne");
			else
				skull = ItemUtils.loreAdd(skull, "§eStatut : §chors ligne");
			
			//définition de si le joueur a la permission de promouvoir/rétrogader un membre
			if ((plot.getMembers().getPlayerLevel(p) == 3 && e.getValue().getLevel() < 3) || plot.getMembers().getPlayerLevel(p) == 4 && e.getValue().getLevel() < 4) 
				skull = ItemUtils.loreAdd(skull, " ", "§8Clic gauche : promouvoir", "§8Clic droit : rétrograder");

			//placement de la tête selon le rang du joueur
			switch (e.getValue()) {
			case CO_OWNER:
				inv.setItem(rank4.size() + rank3.indexOf(e.getKey()), skull);
				break;
			case MEMBER:
				inv.setItem(rank4.size() +rank3.size() + rank2.size() + rank1.indexOf(e.getKey()), skull);
				break;
			case OWNER:
				inv.setItem(rank4.indexOf(e.getKey()), skull);
				break;
			case TRUSTED:
				inv.setItem(rank4.size() +rank3.size() + rank2.indexOf(e.getKey()), skull);
				break;
			}
		}
	}

	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (slot == 26) {
			new MainGui(plugin, p).create(p);
			return true;
		}
		for (Entry<OlympaPlayerInformations, PlotRank> e : plot.getMembers().getList().entrySet())
			if (current != null && current.getType() != Material.AIR && e.getKey().getName().equals(current.getItemMeta().getDisplayName()))
				if (plot.getMembers().getPlayerLevel(p) >= 3) {
					//promote le joueur
					if (click == ClickType.LEFT && plot.getMembers().getPlayerLevel(p) > e.getValue().getLevel() + 1)
						plot.getMembers().set(e.getKey(), PlotRank.getPlotRank(e.getValue().getLevel() + 1));
					
					if (click == ClickType.RIGHT && plot.getMembers().getPlayerLevel(p) > e.getValue().getLevel() && e.getValue() != PlotRank.VISITOR)
						plot.getMembers().set(e.getKey(), PlotRank.getPlotRank(e.getValue().getLevel() - 1));
					
					current = ItemUtils.lore(current, "§6Rang : " + PlotRank.getPlotRank(e.getValue().getLevel() - 1).getRankName());
					
					//définition de son statut
					if (Bukkit.getPlayer(e.getKey().getUUID()) != null)
						current = ItemUtils.loreAdd(current, "§eStatut : §aen ligne");
					else
						current = ItemUtils.loreAdd(current, "§eStatut : §chors ligne");
					
					//définition de si le joueur a la permission de promouvoir/rétrogader un membre
					if ((plot.getMembers().getPlayerLevel(p) == 3 && e.getValue().getLevel() < 3) || plot.getMembers().getPlayerLevel(p) == 4 && e.getValue().getLevel() < 4) 
						current = ItemUtils.loreAdd(current, " ", "§8Clic gauche : promouvoir", "§8Clic droit : rétrograder");	
				}
		return true;
	}
	

	@Override
	public boolean onClickCursor(Player p, ItemStack current, ItemStack cursor, int slot) {
		return true;
	}

}
