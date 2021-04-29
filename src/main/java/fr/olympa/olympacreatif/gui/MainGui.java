package fr.olympa.olympacreatif.gui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OCparam;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotId;
import fr.olympa.olympacreatif.plot.PlotParamType;
import fr.olympa.olympacreatif.plot.PlotsManager;









public class MainGui extends IGui {
	
	private MainGui(OlympaCreatifMain plugin, OlympaPlayerCreatif player, Plot plot, String inventoryName, OlympaPlayerCreatif staffPlayer) {
		super(plugin, player, plot, inventoryName, 6, staffPlayer); 
		
		String clickToOpenMenu = "§9Cliquez pour ouvrir le menu";
		
		//création de l'interface Olympa
		for (int i = 0 ; i < 9*6 ; i++) {
			if ((i+1)%9 >= 3 && (i+1)%9 <= 7)
				setItem(i, ItemUtils.item(Material.ORANGE_STAINED_GLASS_PANE, " "), null);
			else
				setItem(i, ItemUtils.item(Material.WHITE_STAINED_GLASS_PANE, " "), null);
		}

		setItem(2, ItemUtils.item(Material.WHITE_STAINED_GLASS_PANE, " "), null);
		setItem(6, ItemUtils.item(Material.WHITE_STAINED_GLASS_PANE, " "), null);
		setItem(38, ItemUtils.item(Material.WHITE_STAINED_GLASS_PANE, " "), null);
		setItem(42, ItemUtils.item(Material.WHITE_STAINED_GLASS_PANE, " "), null);
		
		//génération de l'interface
		
		//génération de la tête en async car l'appel aux serveurs mojang est nécessaire
		Consumer<ItemStack> headConsumer = sk -> {
			sk = ItemUtils.name(sk, "§6Paramètres de " + p.getName());
			sk = ItemUtils.lore(sk, clickToOpenMenu);
			
			setItem(12, sk, (it, c, s) -> new PlayerParametersGui(this).create(isOpenByStaff ? staffPlayer.getPlayer() : p.getPlayer()));
		};

		headConsumer.accept(new ItemStack(Material.PLAYER_HEAD));
		ItemUtils.skull(headConsumer, player.getName(), player.getName());

		setItem(13, ItemUtils.item(Material.BOOK, "§6Mes parcelles", 
				"§eParcelles possédées : " + p.getPlots(true).size() + "/" + p.getPlotsSlots(true),
				"§eParcelles totales : " + p.getPlots(false).size() + "/" + p.getPlotsSlots(false), 
				clickToOpenMenu), 
				(it, c, s) -> new PlayerPlotsGui(this).create(isOpenByStaff ? staffPlayer.getPlayer() : p.getPlayer()));
		
		setItem(14, ItemUtils.item(Material.GOLD_INGOT, "§6Boutique", clickToOpenMenu), 
				(it, c, s) -> new ShopGui(this).create(isOpenByStaff ? staffPlayer.getPlayer() : p.getPlayer()));
		
		if (plot != null) {
			setItem(21, ItemUtils.item(Material.PAINTING, "§6Membres parcelle", "§eNombre de membres : " + plot.getMembers().getCount(), clickToOpenMenu), 
					(it, c, s) -> new MembersGui(this).create(isOpenByStaff ? staffPlayer.getPlayer() : p.getPlayer()));
			
			setItem(22, ItemUtils.item(Material.COMPARATOR, "§6Paramètres généraux parcelle", clickToOpenMenu), 
					(it, c, s) -> new PlotParametersGui(this).create(isOpenByStaff ? staffPlayer.getPlayer() : p.getPlayer()));
			
			setItem(23, ItemUtils.item(Material.REPEATER, "§6Paramètres d'interraction parcelle", clickToOpenMenu), 
					(it, c, s) -> new InteractionParametersGui(this).create(isOpenByStaff ? staffPlayer.getPlayer() : p.getPlayer()));

			setItem(31, ItemUtils.item(Material.ENDER_PEARL, "§6Téléportation au spawn parcelle"), 
					(it, c, s) -> {
						p.getPlayer().closeInventory();
						plot.getParameters().getParameter(PlotParamType.SPAWN_LOC).teleport(p.getPlayer());
						OCmsg.TELEPORTED_TO_PLOT_SPAWN.send(p);
					});	
		}
		
		setItem(30, ItemUtils.item(Material.RED_BED, "§6Téléportation au spawn"), 
				(it, c, s) -> OCparam.SPAWN_LOC.get().teleport(p.getPlayer()));
		
		setItem(32, ItemUtils.item(Material.ENDER_EYE, "§6Téléportation à une parcelle aléatoire"), 
				(it, c, s) -> {
					if (plugin.getPlotsManager().getPlots().size() > 0) {
						List<Integer> set = new ArrayList<Integer>();
						
						for (int i = 1 ; i <= plugin.getDataManager().getPlotsCount() ; i++)
							set.add(i);

						set.removeAll(p.getPlots(false).stream().map(pl -> pl.getPlotId().getId()).collect(Collectors.toList()));
						
						set.removeAll(plugin.getPlotsManager().getPlots().stream()
								.filter(pl -> pl.getMembers().getOwner().getName().equals("Spawn"))
								.map(pl -> pl.getPlotId().getId()).collect(Collectors.toList()));

						if (set.size() == 0)
							return;
						
						PlotId id = PlotId.fromId(plugin, set.get(ThreadLocalRandom.current().nextInt(set.size())));
						Plot plotR = plugin.getPlotsManager().getPlot(id);
						
						if (plotR != null)
							plotR.getParameters().getParameter(PlotParamType.SPAWN_LOC).teleport(p.getPlayer());
						else
							p.getPlayer().teleport(id.getLocation());
						
						OCmsg.TELEPORTED_TO_PLOT_SPAWN.send(p, id);
					}
				});

		if (!isOpenByStaff)
			setItem(40, ItemUtils.item(Material.COMPASS, "§6Trouver une nouvelle parcelle"),
					(it, c, s) -> plugin.getCmdLogic().claimNewPlot(getPlayer()));
		else
			setItem(40, ItemUtils.item(Material.COMPASS, "§cImpossible de claim en mode staff"),
					null);
		
		setItem(49, ItemUtils.item(Material.PAPER, "§6Ouvrir l'aide"), 
				(it, c, s) -> getPlayer().getPlayer().sendMessage("§7L'aide n'a pas encore été définie. En attendant, vous pouvez utiliser /oc ou /oco help !"));
		
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
	
	public Plot getPlot() {
		return plot;
	}
	
	//open main gui
	public static MainGui getMainGui(OlympaPlayerCreatif p) {
		return getMainGui(p, p.getCurrentPlot());
	}

	public static MainGui getMainGui(OlympaPlayerCreatif p, String stringPlotId) {
		
		Plot plot = null;
		
		PlotId plotId = PlotId.fromString(OlympaCreatifMain.getInstance(), stringPlotId);
		
		if (plotId != null) 
			plot = OlympaCreatifMain.getInstance().getPlotsManager().getPlot(plotId);
		
		return getMainGui(p, plot);
	}
	
	public static MainGui getMainGui(OlympaPlayerCreatif p, IGui gui) {
		return getMainGui(p, gui.getPlot());
	}
	
	public static MainGui getMainGui(OlympaPlayerCreatif p, Plot plot) {
		if (plot == null)
			return new MainGui(OlympaCreatifMain.getInstance(), p, null, "Menu", null);
		else
			return new MainGui(OlympaCreatifMain.getInstance(), p, plot, "Menu >> " + plot.getPlotId(), null);
	}
	
	public static MainGui getMainGuiForStaff(OlympaPlayerCreatif p, OlympaPlayerCreatif staffPlayer) {
		return getMainGuiForStaff(p, staffPlayer, staffPlayer.getCurrentPlot());
	}
	
	public static MainGui getMainGuiForStaff(OlympaPlayerCreatif p, OlympaPlayerCreatif staffPlayer, Plot plot) {
		return new MainGui(OlympaCreatifMain.getInstance(), p, plot, 
				plot == null ? "Menu" : "Menu >> " + plot, staffPlayer);
	}
	
	
}
