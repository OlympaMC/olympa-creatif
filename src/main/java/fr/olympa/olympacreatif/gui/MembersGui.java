package fr.olympa.olympacreatif.gui;

import java.util.ArrayList;
import java.util.HashMap;
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
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;

public class MembersGui extends OlympaGUI {

	private OlympaCreatifMain plugin;
	private Plot plot;
	private Player p;
	private OlympaPlayerCreatif pc;
	
	public MembersGui(OlympaCreatifMain plugin, Player p, Plot plot) {
		super("§6Membres du plot : " + plot.getId().getAsString(), 3);
		
		this.plugin = plugin;
		this.p = p;
		this.pc = AccountProvider.get(p.getUniqueId());
		this.plot = plot;
		
		inv.setItem(26, ItemUtils.item(Material.ACACIA_DOOR, "§cRetour", ""));
		
		//affichage des membres
		int headIndex = -1;
		
		for (Entry<OlympaPlayerInformations, PlotRank> e : plot.getMembers().getList().entrySet()) {
			
			headIndex++;
			final int thisHeadIndex = headIndex;
			
			//création de la tête du joueur
			Consumer<ItemStack> consumer = sk -> inv.setItem(thisHeadIndex, sk);
			
			List<String> lore = new ArrayList<String>();
			lore.add("§6Rang : " + e.getValue().getRankName());

			//définition de son statut
			if (Bukkit.getPlayer(e.getKey().getUUID()) != null)
				lore.add("§eStatut : §aen ligne");
			else
				lore.add("§eStatut : §chors ligne");
			
			//définition de si le joueur a la permission de promouvoir/rétrogader un membre
			if ((plot.getMembers().getPlayerLevel(pc) == 3 && e.getValue().getLevel() < 3) || plot.getMembers().getPlayerLevel(pc) == 4 && e.getValue().getLevel() < 4) {
				lore.add(" ");
				lore.add("§8Clic gauche : promouvoir");
				lore.add("§8Clic droit : rétrograder");	
			}

			consumer.accept(ItemUtils.item(Material.PLAYER_HEAD, "§6" + e.getKey().getName(), (String[]) lore.toArray(new String[lore.size()])));
			ItemUtils.skull(consumer, "§6" + e.getKey().getName(), e.getKey().getName(), (String[]) lore.toArray(new String[lore.size()]));
		}
	}

	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (slot == 26) {
			if (plot == null)
				new MainGui(plugin, p, plot, "§9Menu").create(p);
			else
				new MainGui(plugin, p, plot, "§9Menu >> " + plot.getId().getAsString()).create(p);
			return true;
		}

		if (plot.getMembers().getPlayerLevel(pc) < 3) 
			return true;
		
		//recherche le joueur cliqué
		for (Entry<OlympaPlayerInformations, PlotRank> e : plot.getMembers().getList().entrySet())
			if (current != null && current.getType() != Material.AIR && ("§6" + e.getKey().getName()).equals(current.getItemMeta().getDisplayName())) {
				boolean hasChange = false;
				
				//promote le joueur
				if (click == ClickType.LEFT && plot.getMembers().getPlayerLevel(pc) > e.getValue().getLevel() + 1) {
					hasChange = true;
					plot.getMembers().set(e.getKey(), PlotRank.getPlotRank(e.getValue().getLevel() + 1));	
				}
				
				//démote le joueur
				if (click == ClickType.RIGHT && plot.getMembers().getPlayerLevel(pc) > e.getValue().getLevel() && e.getValue() != PlotRank.VISITOR) {
					hasChange = true;
					plot.getMembers().set(e.getKey(), PlotRank.getPlotRank(e.getValue().getLevel() - 1));	
				}
				
				current = ItemUtils.lore(current, "§6Rang : " + plot.getMembers().getPlayerRank(e.getKey()).getRankName());
				
				//définition de son statut
				if (Bukkit.getPlayer(e.getKey().getUUID()) != null)
					current = ItemUtils.loreAdd(current, "§eStatut : §aen ligne");
				else
					current = ItemUtils.loreAdd(current, "§eStatut : §chors ligne");
				
				if (hasChange)
					current = ItemUtils.loreAdd(current, " ", "§8Clic gauche : promouvoir", "§8Clic droit : rétrograder");	
			}
		return true;
	}
	

	@Override
	public boolean onClickCursor(Player p, ItemStack current, ItemStack cursor, int slot) {
		return true;
	}

}
