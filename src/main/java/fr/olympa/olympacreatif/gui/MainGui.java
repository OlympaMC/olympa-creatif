package fr.olympa.olympacreatif.gui;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotParamType;

public class MainGui extends OlympaGUI {

	private OlympaCreatifMain plugin;
	private Player p;
	private Plot plot;
	public MainGui(OlympaCreatifMain plugin, Player p) {
		super("§6Menu principal",1);
		
		this.plugin = plugin;
		this.p = p;
		plot = plugin.getPlotsManager().getPlot(p.getLocation());
		
		if (plot != null) {
			inv.setItem(0, ItemUtils.item(Material.GRASS_BLOCK, Message.GUI_MAIN_PLOT_INFO.getValue().replace("%plot%", plot.getId().getAsString()), Message.GUI_MAIN_PLOT_INFO_LORE.getValue().replace("%membersCount%", ""+plot.getMembers().getCount()).split(",") ));
			inv.setItem(1, ItemUtils.item(Material.ENDER_PEARL, Message.GUI_MAIN_TELEPORT_PLOT_SPAWN.getValue(), Message.GUI_MAIN_TELEPORT_PLOT_SPAWN_LORE.getValue().split(",")));
			inv.setItem(2, ItemUtils.item(Material.PLAYER_HEAD, Message.GUI_MAIN_MEMBERS_LIST.getValue(), Message.GUI_MAIN_MEMBERS_LIST_LORE.getValue().split(",")));
			inv.setItem(4, ItemUtils.item(Material.COMPARATOR, Message.GUI_MAIN_INTERACTION_PARAMETERS.getValue(), Message.GUI_MAIN_INTERACTION_PARAMETERS_LORE.getValue().split(",")));
			inv.setItem(5, ItemUtils.item(Material.REPEATER, Message.GUI_MAIN_PLOT_PARAMETERS.getValue(), Message.GUI_MAIN_PLOT_PARAMETERS_LORE.getValue().split(",")));
			inv.setItem(7, ItemUtils.item(Material.BOOK, Message.GUI_MAIN_PLOTS_LIST.getValue(), Message.GUI_MAIN_PLOTS_LIST_LORE.getValue().split(",")));
			inv.setItem(8, ItemUtils.item(Material.ENDER_EYE, Message.GUI_MAIN_TELEPORT_RANDOM_PLOT.getValue(), Message.GUI_MAIN_TELEPORT_RANDOM_PLOT_LORE.getValue().split(",")));	
		}else {
			inv.setItem(0, ItemUtils.item(Material.GRASS_BLOCK, Message.GUI_MAIN_PLOT_INFO.getValue().replace("%plot%", "§caucun§r"), Message.GUI_MAIN_PLOT_INFO_LORE.getValue().replace("%membersCount%", "0").split(",") ));
			inv.setItem(7, ItemUtils.item(Material.BOOK, Message.GUI_MAIN_PLOTS_LIST.getValue(), Message.GUI_MAIN_PLOTS_LIST_LORE.getValue().split(",")));
			inv.setItem(8, ItemUtils.item(Material.ENDER_EYE, Message.GUI_MAIN_TELEPORT_RANDOM_PLOT.getValue(), Message.GUI_MAIN_TELEPORT_RANDOM_PLOT_LORE.getValue().split(",")));
		}
		/*Options à intégrer au menu :
		 * Infos générales parcelle
		 * TP spawn parcelle
		 * Membres parcelle
		 * Paramètres interraction
		 * Paramètres : setspawn, fly, gamemode, vider l'inventaire, forcespawn, biome
		 * Liste zones où le joueur est membre
		 * TP sur parcelle aléatoire
		 * 
		 * pas dans le gui mais par commande : 
		 * option de chat (général et/ou plot)
		 * set time
		 * kick, ban
		 * invite, promote, demote
		 * tp joueur, plot
		 * ouvrir menu
		 */
	}

	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		switch (slot) {
		case 1:
			if (plot != null) {
				p.closeInventory();
				p.sendMessage(Message.TELEPORTED_TO_PLOT_SPAWN.getValue());
				p.teleport((Location) plot.getParameters().getParameter(PlotParamType.SPAWN_LOC));	
			}
			break;
		case 2:
			if (plot != null)
				new MembersGui(plugin, p, plot).create(p);
			break;
		case 4:
			if (plot != null)
				new InteractionParametersGui(plugin, p, plot).create(p);
			break;
		case 5:
			if (plot != null)
				new PlotParametersGui(plugin, p, plot).create(p);
			break;
		case 7:
			new PlayerPlotsGui(plugin, p).create(p);
			break;
		case 8:
			if (plugin.getPlotsManager().getPlots().size()>0) {
				Plot pl = ((Plot) plugin.getPlotsManager().getPlots().toArray()[plugin.random.nextInt(plugin.getPlotsManager().getPlots().size())]);
				p.teleport(pl.getId().getLocation());
				p.sendMessage(Message.TELEPORT_TO_RANDOM_PLOT.getValue());	
			}
			break;
		}
		return true;
	}

	@Override
	public boolean onClickCursor(Player p, ItemStack current, ItemStack cursor, int slot) {
		return true;
	}

}
