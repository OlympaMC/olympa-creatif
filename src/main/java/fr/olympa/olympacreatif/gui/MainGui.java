package fr.olympa.olympacreatif.gui;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotId;
import fr.olympa.olympacreatif.plot.PlotParamType;
import fr.olympa.olympacreatif.plot.PlotsManager;

public class MainGui extends IGui {
	
	private MainGui(OlympaCreatifMain plugin, OlympaPlayerCreatif player, Plot plot, String inventoryName) {
		super(plugin, player, plot, inventoryName, 6); 
		
		String clickToOpenMenu = "§9Cliquez pour ouvrir le menu";
		
		//création de l'interface Olympa
		for (int i = 0 ; i < 9*6 ; i++) {
			if ((i+1)%9 >= 3 && (i+1)%9 <= 7)
				inv.setItem(i, ItemUtils.item(Material.ORANGE_STAINED_GLASS_PANE, " "));
			else
				inv.setItem(i, ItemUtils.item(Material.WHITE_STAINED_GLASS_PANE, " "));
		}
		inv.setItem(2, ItemUtils.item(Material.WHITE_STAINED_GLASS_PANE, " "));
		inv.setItem(6, ItemUtils.item(Material.WHITE_STAINED_GLASS_PANE, " "));
		inv.setItem(38, ItemUtils.item(Material.WHITE_STAINED_GLASS_PANE, " "));
		inv.setItem(42, ItemUtils.item(Material.WHITE_STAINED_GLASS_PANE, " "));
		
		//génération de l'interface
		
		//génération de la tête en async car l'appel aux serveurs mojang est nécessaire
		Consumer<ItemStack> consumer = sk -> {
			//Bukkit.broadcastMessage("tête chargée : " + sk.toString());
			
			sk = ItemUtils.name(sk, "§6Paramètres de " + p.getName());
			sk = ItemUtils.lore(sk, clickToOpenMenu);
			inv.setItem(12, sk);
			
		};

		consumer.accept(new ItemStack(Material.PLAYER_HEAD));
		ItemUtils.skull(consumer, player.getName(), player.getName());
		//plugin.getPerksManager().getMicroBlocks().skull(consumer, p.getName(), p.getName());

		inv.setItem(13, ItemUtils.item(Material.BOOK, "§6Mes parcelles", 
				"§eParcelles possédées : " + p.getPlots(true).size() + "/" + p.getPlotsSlots(true),
				"§eParcelles totales : " + p.getPlots(false).size() + "/" + p.getPlotsSlots(false), 
				clickToOpenMenu));
		inv.setItem(14, ItemUtils.item(Material.GOLD_INGOT, "§6Boutique", clickToOpenMenu));
		
		if (plot != null) {
			inv.setItem(21, ItemUtils.item(Material.PAINTING, "§6Membres parcelle", "§eNombre de membres : " + plot.getMembers().getCount(), clickToOpenMenu));
			inv.setItem(22, ItemUtils.item(Material.COMPARATOR, "§6Paramètres généraux parcelle", clickToOpenMenu));
			inv.setItem(23, ItemUtils.item(Material.REPEATER, "§6Paramètres d'interraction parcelle", clickToOpenMenu));

			inv.setItem(31, ItemUtils.item(Material.ENDER_PEARL, "§6Téléportation au spawn parcelle", "§7Uniquement si vous êtes en mode créatif"));	
		}
		inv.setItem(30, ItemUtils.item(Material.RED_BED, "§6Téléportation au spawn"));
		inv.setItem(32, ItemUtils.item(Material.ENDER_EYE, "§6Téléportation à une parcelle aléatoire"));

		inv.setItem(40, ItemUtils.item(Material.COMPASS, "§6Trouver une nouvelle parcelle"));
		inv.setItem(49, ItemUtils.item(Material.PAPER, "§6Ouvrir l'aide"));
		
		/*Options à intégrer au menu :
		 * Infos générales parcelle
		 * TP spawn parcelle
		 * Membres parcelle
		 * Paramètres interraction
		 * Paramètres : setspawn, fly, gamemode, vider l'inventaire, forcespawn, biome
		 * Liste zones où le joueur est membre
		 * TP sur parcelle aléatoire
		 * Infos joueur (parcelles totales, parcelles owner, grade)
		 * 
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
		case 21:
			if (plot != null)
				new MembersGui(this).create(p);
			break;
			
			
		case 23:
			if (plot != null)
				new InteractionParametersGui(this).create(p);
			break;
			
			
		case 22:
			if (plot != null)
				new PlotParametersGui(this).create(p);
			break;
			
			
		case 12:
			new PlayerParametersGui(this).create(p);
			break;
			
			
		case 13:
			new PlayerPlotsGui(this).create(p);
			break;
			
			
		case 14:
			new ShopGui(this).create(p);
			break;
			
		case 30 :
			p.teleport(plugin.getWorldManager().getWorld().getSpawnLocation());
			break;
			
		case 31:
			if (plot != null && p.getGameMode() == GameMode.CREATIVE) {
				p.closeInventory();
				p.sendMessage(Message.TELEPORTED_TO_PLOT_SPAWN.getValue());
				p.teleport(plot.getParameters().getSpawnLoc());	
			}
				
			break;
			
		case 32:
			if (plugin.getPlotsManager().getPlots().size()>0) {
				Plot plot = ((Plot) plugin.getPlotsManager().getPlots().toArray()[plugin.random.nextInt(plugin.getPlotsManager().getPlots().size())]);
				p.teleport(plot.getParameters().getSpawnLoc());
				p.sendMessage(Message.TELEPORTED_TO_PLOT_SPAWN.getValue(plot));
			}
			break;
			
			
		case 40:
			Bukkit.dispatchCommand(p, "oc find");
			break;
		}
		return true;
	}

	@Override
	public boolean onClickCursor(Player p, ItemStack current, ItemStack cursor, int slot) {
		return true;
	}
	
	public Plot getPlot() {
		return plot;
	}
	
	//open main gui
	public static MainGui getMainGui(OlympaPlayerCreatif p) {
		return getMainGui(p, p.getCurrentPlot());
	}

	public static MainGui getMainGui(OlympaPlayerCreatif p, String stringPlotId) {
		
		Plot plot = null;
		
		PlotId plotId = PlotId.fromString(OlympaCreatifMain.getMainClass(), stringPlotId);
		
		if (plotId != null) 
			plot = OlympaCreatifMain.getMainClass().getPlotsManager().getPlot(plotId);
		
		return getMainGui(p, plot);
	}
	
	public static MainGui getMainGui(OlympaPlayerCreatif p, IGui gui) {
		return getMainGui(p, gui.getPlot());
	}
	
	public static MainGui getMainGui(OlympaPlayerCreatif p, Plot plot) {
		if (plot == null)
			return new MainGui(OlympaCreatifMain.getMainClass(), p, null, "Menu");
		else
			return new MainGui(OlympaCreatifMain.getMainClass(), p, plot, "Menu >> " + plot.getPlotId());
	}
}
