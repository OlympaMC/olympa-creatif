package fr.olympa.olympacreatif.gui;

import java.util.Random;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotParamType;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;

public class MainGui extends OlympaGUI {

	private OlympaCreatifMain plugin;
	private OlympaPlayer p;
	private Plot plot;
	public MainGui(OlympaCreatifMain plugin, Player player, Plot plot, String inventoryName) {
		super(inventoryName, 6);

		if (!plugin.getPlotsManager().isPlayerLoaded(player))
			return;
		
		this.plugin = plugin;
		this.p = AccountProvider.get(player.getUniqueId());
		this.plot = plot;
		
		String clickToOpenMenu = "§9Cliquez pour ouvrir le menu";
		int totalPlayerPlots = 0;
		int totalPlayerOwnedPlots = 0;
		
		//comptage du nombre de plots du joueur
		for (Plot plot2 : plugin.getPlotsManager().getPlots())
			if (plot2.getMembers().getPlayerRank(p.getInformation()) != PlotRank.VISITOR)
				if (plot2.getMembers().getPlayerRank(p.getInformation()) == PlotRank.OWNER)
					totalPlayerOwnedPlots++;
				else
					totalPlayerPlots++;
		
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

		final int totalPlayerOwnedPlotsFinal = totalPlayerOwnedPlots;
		final int totalPlayerPlotsFinal = totalPlayerPlots;
		
		//génération de l'interface
		
		//génération de la tête en async car l'appel aux serveurs mojang est nécessaire
		Consumer<ItemStack> consumer = sk -> {
			sk = ItemUtils.name(sk, "§6Profil de " + player.getDisplayName());
			sk = ItemUtils.loreAdd(sk, "§eGrade : " + p.getGroupNameColored(), 
					" ",
					"§eParcelles totales : " + totalPlayerPlotsFinal + "/36",  
					"§eParcelles propriétaire : " + totalPlayerOwnedPlotsFinal + "/" + (totalPlayerOwnedPlotsFinal + plugin.getPlotsManager().getAvailablePlotSlotsLeftOwner(player))
					);
			inv.setItem(12, sk);
		};

		consumer.accept(new ItemStack(Material.BEDROCK));
		ItemUtils.skull(consumer, "", p.getName());

		inv.setItem(13, ItemUtils.item(Material.BOOK, "§6Mes plots", clickToOpenMenu));
		inv.setItem(14, ItemUtils.item(Material.GOLD_INGOT, "§6Boutique", "§eVotre monnaie : TODO", clickToOpenMenu));
		
		if (plot != null) {
			inv.setItem(21, ItemUtils.item(Material.PAINTING, "§6Membres parcelle", "§eNombre de membres : " + plot.getMembers().getCount(), clickToOpenMenu));
			inv.setItem(22, ItemUtils.item(Material.COMPARATOR, "§6Paramètres généraux parcelle", clickToOpenMenu));
			inv.setItem(23, ItemUtils.item(Material.REPEATER, "§6Paramètres d'interraction parcelle", clickToOpenMenu));

			inv.setItem(31, ItemUtils.item(Material.ENDER_PEARL, "§6Téléportation au spawn parcelle"));	
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
		case 31:
			if (plot != null) {
				p.closeInventory();
				p.sendMessage(Message.TELEPORTED_TO_PLOT_SPAWN.getValue());
				p.teleport((Location) plot.getParameters().getParameter(PlotParamType.SPAWN_LOC));	
			}
			break;
		case 21:
			if (plot != null)
				new MembersGui(plugin, p, plot).create(p);
			break;
		case 23:
			if (plot != null)
				new InteractionParametersGui(plugin, p, plot).create(p);
			break;
		case 22:
			if (plot != null)
				new PlotParametersGui(plugin, p, plot).create(p);
			break;
		case 13:
			new PlayerPlotsGui(plugin, p).create(p);
			break;
		case 30 :
			p.teleport(plugin.getWorldManager().getWorld().getSpawnLocation());
			break;
		case 32:
			if (plugin.getPlotsManager().getPlots().size()>0) {
				Plot pl = ((Plot) plugin.getPlotsManager().getPlots().toArray()[plugin.random.nextInt(plugin.getPlotsManager().getPlots().size())]);
				p.teleport(pl.getId().getLocation());
				p.sendMessage(Message.TELEPORT_TO_RANDOM_PLOT.getValue());	
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
}
