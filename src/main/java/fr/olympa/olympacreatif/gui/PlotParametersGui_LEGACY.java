package fr.olympa.olympacreatif.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.block.Biome;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;
import fr.olympa.olympacreatif.world.WorldManager;
import fr.olympa.olympacreatif.plot.PlotParamType;
import fr.olympa.olympacreatif.plot.PlotParameters;

public class PlotParametersGui_LEGACY extends OlympaGUI {

	private OlympaCreatifMain plugin;
	private OlympaPlayerCreatif pc;
	private Plot plot;
	
	private Biome newBiome;
	private String[] clickToChange = null;
	
	private String clearWeather = "§eMétéo actuelle : ensoleillée";
	private String rainyWeather = "§eMétéo actuelle : pluvieuse";
	
	public PlotParametersGui_LEGACY(OlympaCreatifMain plugin, Player p, Plot plot) {
		super("Paramètres du plot : " + plot.getLoc(), 2);
		this.plugin = plugin;
		this.pc = AccountProvider.get(p.getUniqueId());
		this.plot = plot;

		newBiome = (Biome) plot.getParameters().getParameter(PlotParamType.PLOT_BIOME);
		
		inv.setItem(17, ItemUtils.item(Material.ACACIA_DOOR, "§cRetour", ""));
		
		if (plot.getMembers().getPlayerLevel(pc) >= 3)
			 clickToChange = new String[] {" ", "§7Ne concerne que les visiteurs", "§7Cliquez pour changer la valeur"};
		else
			 clickToChange = new String[] {" ", "§7Ne concerne que les visiteurs"};
		
		//ajout des options
		ItemStack it = null;
		
		//0 : Gamemode par défaut
		it = ItemUtils.item(Material.ACACIA_SIGN, "§6Gamemode par défaut", "§eMode actuel : " + plot.getParameters().getParameter(PlotParamType.GAMEMODE_INCOMING_PLAYERS).toString());
		
		it = ItemUtils.loreAdd(it, clickToChange);
		inv.setItem(0,it);
		
		//1 : Autorisation fly
		it = ItemUtils.item(Material.FEATHER, "§6Vol des visiteurs");
		
		it = setSwitchState(it, (boolean) plot.getParameters().getParameter(PlotParamType.ALLOW_FLY_INCOMING_PLAYERS));

		it = ItemUtils.loreAdd(it, clickToChange);
		inv.setItem(1,it);

		//2 : Forcer spawn zone
		it = ItemUtils.item(Material.ACACIA_FENCE_GATE, "§6Forcer le spawn parcelle");

		it = setSwitchState(it, (boolean) plot.getParameters().getParameter(PlotParamType.FORCE_SPAWN_LOC));

		it = ItemUtils.loreAdd(it, clickToChange);
		inv.setItem(2,it);

		//3 : Autorisation allumer tnt
		it = ItemUtils.item(Material.TNT, "§6Amorçage de la TNT");

		it = setSwitchState(it, (boolean) plot.getParameters().getParameter(PlotParamType.ALLOW_PRINT_TNT));

		it = ItemUtils.loreAdd(it, clickToChange);
		inv.setItem(3,it);

		//4 : Heure du plot
		it = ItemUtils.item(Material.SUNFLOWER, "§6Heure de la parcelle");
		it = ItemUtils.lore(it, "§eHeure actuelle : " + ((int)plot.getParameters().getParameter(PlotParamType.PLOT_TIME))/1000 + "h");
		
		it = ItemUtils.loreAdd(it, clickToChange);
		
		inv.setItem(4,it);

		//5 : Clear inventaire joueur
		it = ItemUtils.item(Material.CAULDRON, "§6Clear des visiteurs");

		it = setSwitchState(it, (boolean) plot.getParameters().getParameter(PlotParamType.CLEAR_INCOMING_PLAYERS));

		it = ItemUtils.loreAdd(it, clickToChange);
		inv.setItem(5,it);

		//6 : Sélection du biome
		it = ItemUtils.item(Material.BOOKSHELF, "§6Biome de la parcelle");
		for (Biome biome : PlotParamType.getAllPossibleBiomes())
			if (biome == plot.getParameters().getParameter(PlotParamType.PLOT_BIOME))
				it = ItemUtils.loreAdd(it, "§aActuel : " + biome.toString());
			else
				it = ItemUtils.loreAdd(it, "§2Disponible : " + biome.toString());
		
		if (plot.getMembers().getPlayerRank(pc) == PlotRank.OWNER)
			it = ItemUtils.loreAdd(it, clickToChange);
		inv.setItem(6,it);

		//7 : Autoriser les potions splash
		it = ItemUtils.item(Material.SPLASH_POTION, "§6Utilisation des potions jetables");

		it = setSwitchState(it, (boolean) plot.getParameters().getParameter(PlotParamType.ALLOW_SPLASH_POTIONS));

		it = ItemUtils.loreAdd(it, clickToChange);
		inv.setItem(7,it);

		//8 : Autoriser le pvp
		it = ItemUtils.item(Material.DIAMOND_SWORD, "§6Activation du PvP");

		it = setSwitchState(it, (boolean) plot.getParameters().getParameter(PlotParamType.ALLOW_PVP));

		it = ItemUtils.loreAdd(it, clickToChange);
		inv.setItem(8,it);

		//9 : Autoriser le pvp
		it = ItemUtils.item(Material.SLIME_BLOCK, "§6Activation des dégâts environnementaux");

		it = setSwitchState(it, (boolean) plot.getParameters().getParameter(PlotParamType.ALLOW_ENVIRONMENT_DAMAGE));

		it = ItemUtils.loreAdd(it, clickToChange);
		inv.setItem(9,it);

		//10 : Définir la météo
		it = ItemUtils.item(Material.SUNFLOWER, "§6Météo de la parcelle");
		if (plot.getParameters().getParameter(PlotParamType.PLOT_WEATHER) == WeatherType.CLEAR)
			it = ItemUtils.lore(it, clearWeather);
		else
			it = ItemUtils.lore(it, rainyWeather);
		
		it = ItemUtils.loreAdd(it, clickToChange);
		inv.setItem(10,it);

		//11 : Autoriser le pvp
		it = ItemUtils.item(Material.DROPPER, "§6Drop des items");

		it = setSwitchState(it, (boolean) plot.getParameters().getParameter(PlotParamType.ALLOW_DROP_ITEMS));

		it = ItemUtils.loreAdd(it, clickToChange);
		inv.setItem(11,it);

		//12 : Autoriser le pvp
		it = ItemUtils.item(Material.COOKED_BEEF, "§6Satiété maximale permanente");

		it = setSwitchState(it, (boolean) plot.getParameters().getParameter(PlotParamType.KEEP_MAX_FOOD_LEVEL));

		it = ItemUtils.loreAdd(it, clickToChange);
		inv.setItem(12,it);

		//13 : Autoriser le pvp
		it = ItemUtils.item(Material.DROWNED_SPAWN_EGG, "§6Activation du PvE");

		it = setSwitchState(it, (boolean) plot.getParameters().getParameter(PlotParamType.ALLOW_PVE));

		it = ItemUtils.loreAdd(it, clickToChange);
		inv.setItem(13,it);

		//14 : Gamerule keepInventory
		it = ItemUtils.item(Material.BUCKET, "§6Conservation items à la mort");

		it = setSwitchState(it, (boolean) plot.getParameters().getParameter(PlotParamType.KEEP_INVENTORY_ON_DEATH));

		it = ItemUtils.loreAdd(it, clickToChange);
		inv.setItem(14,it);
	}

	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (slot == 17) {
			if (plot == null)
				new MainGui(plugin, p, plot, "Menu").create(p);
			else
				new MainGui(plugin, p, plot, "Menu >> " + plot.getLoc()).create(p);
			return true;
		}
		
		if (plot.getMembers().getPlayerLevel(pc) < 3)
			return true;
		
		//modification des options
		switch (slot) {
		case 0:
			GameMode gm = GameMode.valueOf(ItemUtils.getLore(current)[0].split(" : ")[1]);
			switch (gm) {
			case ADVENTURE:
				gm = GameMode.SURVIVAL;
				break;
			case CREATIVE:
				gm = GameMode.ADVENTURE;
				break;
			case SURVIVAL:
				gm = GameMode.CREATIVE;
				break;
			
			}
			plot.getParameters().setParameter(PlotParamType.GAMEMODE_INCOMING_PLAYERS, gm);
			current = ItemUtils.lore(current, "§eMode actuel : " + gm.toString());
			current = ItemUtils.loreAdd(current, clickToChange);
			break;
		case 1:
			current = setSwitchState(current, !getSwitchState(current));
			plot.getParameters().setParameter(PlotParamType.ALLOW_FLY_INCOMING_PLAYERS, getSwitchState(current));
			break;
		case 2:
			current = setSwitchState(current, !getSwitchState(current));
			plot.getParameters().setParameter(PlotParamType.FORCE_SPAWN_LOC, getSwitchState(current));
			break;
		case 3:
			current = setSwitchState(current, !getSwitchState(current));
			plot.getParameters().setParameter(PlotParamType.ALLOW_PRINT_TNT, getSwitchState(current));
			break;
		case 4:
			plot.getParameters().setParameter(PlotParamType.PLOT_TIME, ((int) plot.getParameters().getParameter(PlotParamType.PLOT_TIME) + 1000)%25000);
			current = ItemUtils.lore(current, "§eHeure actuelle : " + (int)plot.getParameters().getParameter(PlotParamType.PLOT_TIME)/1000 + "h");
			current = ItemUtils.loreAdd(current, clickToChange);
			for (Player pp : plot.getPlayers())
				pp.setPlayerTime(current.getAmount()*1000, true);
			break;
		case 5:
			current = setSwitchState(current, !getSwitchState(current));
			plot.getParameters().setParameter(PlotParamType.CLEAR_INCOMING_PLAYERS, getSwitchState(current));
			break;
		case 6:
			//édition du biome réservée au propriétaire
			if (plot.getMembers().getPlayerRank(pc) != PlotRank.OWNER)
				return true;
			
			int biomeRank = PlotParamType.getAllPossibleBiomes().indexOf(newBiome);
			newBiome = PlotParamType.getAllPossibleBiomes().get((biomeRank+1) % PlotParamType.getAllPossibleBiomes().size());
			
			current = ItemUtils.lore(current, "");
			for (Biome biome : PlotParamType.getAllPossibleBiomes())
				if (biome == newBiome)
					current = ItemUtils.loreAdd(current, "§aActuel : " + biome.toString());
				else
					current = ItemUtils.loreAdd(current, "§2Disponible : " + biome.toString());
			
			current = ItemUtils.loreAdd(current, clickToChange);			
			break;
		case 7:
			current = setSwitchState(current, !getSwitchState(current));
			plot.getParameters().setParameter(PlotParamType.ALLOW_SPLASH_POTIONS, getSwitchState(current));
			break;
		case 8:
			current = setSwitchState(current, !getSwitchState(current));
			plot.getParameters().setParameter(PlotParamType.ALLOW_PVP, getSwitchState(current));
			break;
		case 9:
			current = setSwitchState(current, !getSwitchState(current));
			plot.getParameters().setParameter(PlotParamType.ALLOW_ENVIRONMENT_DAMAGE, getSwitchState(current));
			break;
		case 10:
			if (plot.getParameters().getParameter(PlotParamType.PLOT_WEATHER) == WeatherType.CLEAR) {
				plot.getParameters().setParameter(PlotParamType.PLOT_WEATHER, WeatherType.DOWNFALL);
				current = ItemUtils.lore(current, rainyWeather);	
			}
			else {
				plot.getParameters().setParameter(PlotParamType.PLOT_WEATHER, WeatherType.CLEAR);
				current = ItemUtils.lore(current, clearWeather);	
			}
			current = ItemUtils.loreAdd(current, clickToChange);
			break;
		case 11:
			current = setSwitchState(current, !getSwitchState(current));
			plot.getParameters().setParameter(PlotParamType.ALLOW_DROP_ITEMS, getSwitchState(current));
			break;
		case 12:
			current = setSwitchState(current, !getSwitchState(current));
			plot.getParameters().setParameter(PlotParamType.KEEP_MAX_FOOD_LEVEL, getSwitchState(current));
			break;
		case 13:
			current = setSwitchState(current, !getSwitchState(current));
			plot.getParameters().setParameter(PlotParamType.ALLOW_PVE, getSwitchState(current));
			break;
		case 14:
			current = setSwitchState(current, !getSwitchState(current));
			plot.getParameters().setParameter(PlotParamType.KEEP_INVENTORY_ON_DEATH, getSwitchState(current));
			break;
		}
		
		return true;
	}

	@Override
	public boolean onClickCursor(Player p, ItemStack current, ItemStack cursor, int slot) {
		return true;
	}
	
	@Override
	public boolean onClose(Player p) {
		//MAJ biome
		if (!plot.getParameters().getParameter(PlotParamType.PLOT_BIOME).equals(newBiome)) {
			for (int x = plot.getLoc().getLocation().getBlockX() ; x < plot.getLoc().getLocation().getBlockX() + WorldManager.plotSize ; x++)
				for (int z = plot.getLoc().getLocation().getBlockZ() ; z < plot.getLoc().getLocation().getBlockZ() + WorldManager.plotSize ; z++)
					for (int y = 1 ; y < 255 ; y++) 
						plugin.getWorldManager().getWorld().getBlockAt(x, y, z).setBiome(newBiome);

			plot.getParameters().setParameter(PlotParamType.PLOT_BIOME, newBiome);	
		}
		
		return true;
	}

	public ItemStack setSwitchState(ItemStack it, boolean newState) {
		List<String> list = it.getItemMeta().getLore();
		if (list == null)
			list = new ArrayList<String>();
		
		if (newState) {
			list.add(0, "§eEtat : §aactif");
			ItemMeta im = it.getItemMeta();
			im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			it.setItemMeta(im);
			it.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
		}
		else {
			list.add(0, "§eEtat : §cinactif");
			it.removeEnchantment(Enchantment.DURABILITY);
		}
		if (list.size() >= 2)
			list.remove(1);
		
		return ItemUtils.lore(it, list.toArray(new String[list.size()]));
	}
	
	public boolean getSwitchState(ItemStack it) {
		if (it.getItemMeta().getLore() == null)
			return false;
		
		if (it.getItemMeta().getLore().get(0).equals("§eEtat : §aactif"))
			return true;
		else
			return false;
	}
}
