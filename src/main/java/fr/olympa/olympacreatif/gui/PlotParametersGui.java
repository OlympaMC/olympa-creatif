package fr.olympa.olympacreatif.gui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotParamType;

public class PlotParametersGui extends IGui {
	
	private String[] clickToChange = null;
	
	private String clearWeather = "§eMétéo actuelle : ensoleillée";
	private String rainyWeather = "§eMétéo actuelle : pluvieuse";
	private String[] stoplagLevels = {"§eEtat : §ainactif", "§eEtat : §cactif", "§eEtat : §cforcé §4(contacter un staff)"};
	
	private Map<ItemStack, PlotParamType> switchButtons = new LinkedHashMap<ItemStack, PlotParamType>(); 
	
	public PlotParametersGui(IGui gui) {
		super(gui, "Paramètres du plot " + gui.getPlot().getPlotId(), 3);

		//newBiome = (Biome) plot.getParameters().getParameter(PlotParamType.PLOT_BIOME);
		
		inv.setItem(inv.getSize() - 1, MainGui.getBackItem());
		
		if (plot.getMembers().getPlayerLevel(p) >= 3)
			 clickToChange = new String[] {" ", "§7Cliquez pour changer la valeur"};
		else
			 clickToChange = new String[] {};
		
		//ajout des options
		ItemStack it = null;
		
		//0 : Gamemode par défaut
		it = ItemUtils.item(Material.ACACIA_SIGN, "§6Gamemode par défaut", "§eMode actuel : " + plot.getParameters().getParameter(PlotParamType.GAMEMODE_INCOMING_PLAYERS).toString());
		
		it = ItemUtils.loreAdd(it, clickToChange);
		inv.setItem(0,it);

		//1 : Heure du plot
		it = ItemUtils.item(Material.CLOCK, "§6Heure de la parcelle");
		it = ItemUtils.lore(it, "§eHeure actuelle : " + ((int)plot.getParameters().getParameter(PlotParamType.PLOT_TIME) + 7000)/1000 + "h");
		
		it = ItemUtils.loreAdd(it, clickToChange);
		
		inv.setItem(1,it);
		
		//2 : Sélection du biome
		/*
		it = ItemUtils.item(Material.BOOKSHELF, "§6Biome de la parcelle");
		for (Biome biome : PlotParamType.getAllPossibleBiomes())
			if (biome == plot.getParameters().getParameter(PlotParamType.PLOT_BIOME))
				it = ItemUtils.loreAdd(it, "§aActuel : " + biome.toString());
			else
				it = ItemUtils.loreAdd(it, "§2Disponible : " + biome.toString());
		
		if (plot.getMembers().getPlayerRank(pc) == PlotRank.OWNER)
			it = ItemUtils.loreAdd(it, clickToChange);
		inv.setItem(2,it);
		*/

		//3 : Définir la météo
		it = ItemUtils.item(Material.SUNFLOWER, "§6Météo de la parcelle");
		if (plot.getParameters().getParameter(PlotParamType.PLOT_WEATHER) == WeatherType.CLEAR)
			it = ItemUtils.lore(it, clearWeather);
		else
			it = ItemUtils.lore(it, rainyWeather);
		
		it = ItemUtils.loreAdd(it, clickToChange);
		inv.setItem(2,it);

		//4 : Etat stoplag
		it = ItemUtils.item(Material.COMMAND_BLOCK, "§6Blocage tâches intensives (redstone & cb)");
		ItemUtils.lore(it, stoplagLevels[(int) plot.getParameters().getParameter(PlotParamType.STOPLAG_STATUS)]);
		inv.setItem(3, it);
		
		switchButtons.put(ItemUtils.item(Material.SLIME_BLOCK, "§6Activation des dégâts environnementaux"), PlotParamType.ALLOW_ENVIRONMENT_DAMAGE);
		switchButtons.put(ItemUtils.item(Material.DROWNED_SPAWN_EGG, "§6Activation du PvE"), PlotParamType.ALLOW_PVE);
		switchButtons.put(ItemUtils.item(Material.DIAMOND_SWORD, "§6Activation du PvP"), PlotParamType.ALLOW_PVP);
		switchButtons.put(ItemUtils.item(Material.BUCKET, "§6Conservation items à la mort"), PlotParamType.KEEP_INVENTORY_ON_DEATH);
		switchButtons.put(ItemUtils.item(Material.COOKED_BEEF, "§6Satiété maximale permanente"), PlotParamType.KEEP_MAX_FOOD_LEVEL);
		switchButtons.put(ItemUtils.item(Material.DROPPER, "§6Drop des items"), PlotParamType.ALLOW_DROP_ITEMS);
		switchButtons.put(ItemUtils.item(Material.SPLASH_POTION, "§6Utilisation des potions jetables"), PlotParamType.ALLOW_SPLASH_POTIONS);
		switchButtons.put(ItemUtils.item(Material.CAULDRON, "§6Clear des visiteurs"), PlotParamType.CLEAR_INCOMING_PLAYERS);
		switchButtons.put(ItemUtils.item(Material.TNT, "§6Amorçage de la TNT"), PlotParamType.ALLOW_PRINT_TNT);
		switchButtons.put(ItemUtils.item(Material.ACACIA_FENCE_GATE, "§6Forcer le spawn parcelle"), PlotParamType.FORCE_SPAWN_LOC);
		switchButtons.put(ItemUtils.item(Material.FEATHER, "§6Vol des visiteurs"), PlotParamType.ALLOW_FLY_INCOMING_PLAYERS);
		switchButtons.put(ItemUtils.item(Material.ARROW, "§6Activation des projectiles"), PlotParamType.ALLOW_LAUNCH_PROJECTILES);
		
		
		Map<ItemStack, PlotParamType> switches = new LinkedHashMap<ItemStack, PlotParamType>();
		
		for(Entry<ItemStack, PlotParamType> e : switchButtons.entrySet()) {
			switches.put(setSwitchState(e.getKey(), (boolean)plot.getParameters().getParameter(e.getValue())), e.getValue());
		}
		
		switchButtons = switches;
		inv.addItem(switches.keySet().toArray(new ItemStack[switches.keySet().size()]));
	}
	
	@Override
	public boolean onClick(Player player, ItemStack current, int slot, ClickType click) {
		if (slot == inv.getSize() - 1) {
			MainGui.getMainGui(player);
			return true;
		}
		
		if (plot.getMembers().getPlayerLevel(player) < 3)
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
			plot.getPlayers().forEach(pp -> pp.setGameMode((GameMode) plot.getParameters().getParameter(PlotParamType.GAMEMODE_INCOMING_PLAYERS)));
			break;
			
		case 1:
			plot.getParameters().setParameter(PlotParamType.PLOT_TIME, ((int) plot.getParameters().getParameter(PlotParamType.PLOT_TIME)  - 7000 + 1000)%25000);
			current = ItemUtils.lore(current, "§eHeure actuelle : " + ((int)plot.getParameters().getParameter(PlotParamType.PLOT_TIME) + 7000)/1000 + "h");
			current = ItemUtils.loreAdd(current, clickToChange);
			plot.getPlayers().forEach(pp -> pp.setPlayerTime((int)plot.getParameters().getParameter(PlotParamType.PLOT_TIME), false));
			break;
			
			/*
		case 2:
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
			*/
		case 2:
			if (plot.getParameters().getParameter(PlotParamType.PLOT_WEATHER) == WeatherType.CLEAR) {
				plot.getParameters().setParameter(PlotParamType.PLOT_WEATHER, WeatherType.DOWNFALL);
				current = ItemUtils.lore(current, rainyWeather);	
			}
			else {
				plot.getParameters().setParameter(PlotParamType.PLOT_WEATHER, WeatherType.CLEAR);
				current = ItemUtils.lore(current, clearWeather);	
			}
			current = ItemUtils.loreAdd(current, clickToChange);
			plot.getPlayers().forEach(pp -> pp.setPlayerWeather((WeatherType)plot.getParameters().getParameter(PlotParamType.PLOT_WEATHER)));
			
			break;
			
		case 3:
			int mod = 2;
			if (p.hasStaffPerm(StaffPerm.FAKE_OWNER_EVERYWHERE))
				mod = 3;
			
			int currentState = (int) plot.getParameters().getParameter(PlotParamType.STOPLAG_STATUS);
			
			//si le plot est en stoplag forcé et que le joueur n'a pas la perm staff FAKE OWNER EVERYWHERE, return
			if (currentState == 2 && mod == 2)
				break;
			
			plot.getParameters().setParameter(PlotParamType.STOPLAG_STATUS, Math.floorMod(currentState + 1, mod));
			
			current = ItemUtils.lore(current, stoplagLevels[(int)plot.getParameters().getParameter(PlotParamType.STOPLAG_STATUS)]);
			current = ItemUtils.loreAdd(current, clickToChange);
			break;
			
		default:
			if (slot - 4 <= switchButtons.size()) {
				PlotParamType param = switchButtons.get(current);
				switchButtons.remove(current);
				
				current = setSwitchState(current, !getSwitchState(current));
				switchButtons.put(current, param);
				
				plot.getParameters().setParameter(param, getSwitchState(current));
			}
			break;
		}
		
		return true;
	}

	@Override
	public boolean onClickCursor(Player p, ItemStack current, ItemStack cursor, int slot) {
		return true;
	}
	
	/*
	@Override //Gestion du changement de biome
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
	*/

	public static ItemStack setSwitchState(ItemStack it, boolean newState) {
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
