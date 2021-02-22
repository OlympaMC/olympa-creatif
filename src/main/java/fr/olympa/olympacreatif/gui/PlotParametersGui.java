package fr.olympa.olympacreatif.gui;

import java.util.ArrayList;
import java.util.HashMap;
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
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotParamType;
import fr.olympa.olympacreatif.plot.PlotPerm;


public class PlotParametersGui extends IGui {
	
	private String[] clickToChange = null;
	
	private int timeToAdd = 6000;
	
	private String clearWeather = "§eMétéo actuelle : ensoleillée";
	private String rainyWeather = "§eMétéo actuelle : pluvieuse";
	private String[] stoplagLevels = {"§eEtat : §ainactif", "§eEtat : §cactif", "§eEtat : §cforcé §4(contacter un staff)"};
	
	private boolean canChangeSettings = false; 
	
	public PlotParametersGui(IGui gui) {
		super(gui, "Paramètres du plot " + gui.getPlot().getPlotId(), 3);
		
		canChangeSettings = PlotPerm.CHANGE_PARAM_SETTINGS.has(plot, p);
		
		if (canChangeSettings)
			 clickToChange = new String[] {" ", "§7Cliquez pour changer la valeur"};
		else
			 clickToChange = new String[] {};
		
		//item allant porter les différents paramètres
		ItemStack it = null;
		
		//0 : Gamemode par défaut
		it = ItemUtils.item(Material.ACACIA_SIGN, "§6Gamemode par défaut", "§eMode actuel : " + plot.getParameters().getParameter(PlotParamType.GAMEMODE_INCOMING_PLAYERS).toString());
		it = ItemUtils.loreAdd(it, clickToChange);
		
		setItem(0, it, (item, c, s) -> {
			if (!canChangeSettings)
				return;
			
			GameMode gm = GameMode.valueOf(ItemUtils.getLore(item)[0].split(" : ")[1]);
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
			PlotParamType.GAMEMODE_INCOMING_PLAYERS.setValue(plot, gm);
			ItemStack item2 = ItemUtils.lore(item.clone(), "§eMode actuel : " + gm.toString());
			item2 = ItemUtils.loreAdd(item2, clickToChange);
			changeItem(item, item2);
			
			plot.getPlayers().forEach(pp -> pp.setGameMode(plot.getParameters().getParameter(PlotParamType.GAMEMODE_INCOMING_PLAYERS)));
		});

		//1 : Heure du plot
		it = ItemUtils.item(Material.CLOCK, "§6Heure de la parcelle");
		it = ItemUtils.lore(it, "§eHeure actuelle : " + (plot.getParameters().getParameter(PlotParamType.PLOT_TIME) + timeToAdd)/1000 + "h");
		
		it = ItemUtils.loreAdd(it, clickToChange);
		
		setItem(1, it, (item, c, s) -> {
			if (!canChangeSettings)
				return;
			
			PlotParamType.PLOT_TIME.setValue(plot, (plot.getParameters().getParameter(PlotParamType.PLOT_TIME) + 1000)%25000);
			ItemStack item2 = ItemUtils.lore(item.clone(), "§eHeure actuelle : " + (plot.getParameters().getParameter(PlotParamType.PLOT_TIME) + timeToAdd)/1000 + "h");
			item2 = ItemUtils.loreAdd(item2, clickToChange);
			changeItem(item, item2);
			
			plot.getPlayers().forEach(pp -> pp.setPlayerTime(plot.getParameters().getParameter(PlotParamType.PLOT_TIME), false));
		});
		

		//2 : Définir la météo
		it = ItemUtils.item(Material.SUNFLOWER, "§6Météo de la parcelle");
		if (plot.getParameters().getParameter(PlotParamType.PLOT_WEATHER) == WeatherType.CLEAR)
			it = ItemUtils.lore(it, clearWeather);
		else
			it = ItemUtils.lore(it, rainyWeather);
		
		it = ItemUtils.loreAdd(it, clickToChange);
		
		setItem(2, it, (item, c, s) -> {
			if (!canChangeSettings)
				return;
			
			ItemStack item2 = item.clone();
			
			if (plot.getParameters().getParameter(PlotParamType.PLOT_WEATHER) == WeatherType.CLEAR) {
				PlotParamType.PLOT_WEATHER.setValue(plot, WeatherType.DOWNFALL);
				item2 = ItemUtils.lore(item2, rainyWeather);	
			}else {
				PlotParamType.PLOT_WEATHER.setValue(plot, WeatherType.CLEAR);
				item2 = ItemUtils.lore(item2, clearWeather);	
			}
			item2 = ItemUtils.loreAdd(item2, clickToChange);
			changeItem(item, item2);
			
			plot.getPlayers().forEach(pp -> pp.setPlayerWeather(plot.getParameters().getParameter(PlotParamType.PLOT_WEATHER)));
		});

		//3 : Etat stoplag
		it = ItemUtils.item(Material.COMMAND_BLOCK, "§6Blocage tâches intensives (redstone & cb)");
		ItemUtils.lore(it, stoplagLevels[plot.getParameters().getParameter(PlotParamType.STOPLAG_STATUS)]);
		ItemUtils.loreAdd(it, clickToChange);
		
		setItem(3, it, (item, c, s) -> {
			if (!canChangeSettings)
				return;
			
			ItemStack item2 = item.clone();
			
			int mod = 2;
			if (p.hasStaffPerm(StaffPerm.FAKE_OWNER_EVERYWHERE))
				mod = 3;
			
			int currentState = plot.getParameters().getParameter(PlotParamType.STOPLAG_STATUS);
			
			//si le plot est en stoplag forcé et que le joueur n'a pas la perm staff FAKE OWNER EVERYWHERE, return
			if (currentState == 2 && mod == 2)
				return;
			
			PlotParamType.STOPLAG_STATUS.setValue(plot, Math.floorMod(currentState + 1, mod));
			
			item2 = ItemUtils.lore(item2, stoplagLevels[plot.getParameters().getParameter(PlotParamType.STOPLAG_STATUS)]);
			item2 = ItemUtils.loreAdd(item2, clickToChange);
			
			changeItem(item, item2);
		});

		//4 : Playback music selection
		it = ItemUtils.item(Material.MUSIC_DISC_STRAD, "§6Musique de la parcelle");
		if ("".equals(plot.getParameters().getParameter(PlotParamType.SONG)))
			ItemUtils.loreAdd(it, "§aMusique actuelle : §cAucune");
		else
			ItemUtils.loreAdd(it, "§aMusique actuelle : §d" + plot.getParameters().getParameter(PlotParamType.SONG));
		
		ItemUtils.loreAdd(it, " ", "§7Le grade " + PermissionsList.USE_PLOT_MUSIC.getMinGroup().getPrefix(p.getGender()) + "§7 est nécessaire", "§7pour utiliser cette fonctionnalité.");
		
		ItemUtils.loreAdd(it, clickToChange);
		
		setItem(4, it, (item, c, s) -> {
			if (!canChangeSettings)
				return;

			if (PlotPerm.DEFINE_MUSIC.has(plot, p))
				plugin.getPerksManager().getSongManager().openGui(p.getPlayer(), plot);
		});
		
		Map<ItemStack, PlotParamType<Boolean>> switchButtons = new LinkedHashMap<ItemStack, PlotParamType<Boolean>>();
		
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
		switchButtons.put(ItemUtils.item(Material.FIREWORK_ROCKET, "§6Reset vitesse de vol des visiteurs"), PlotParamType.RESET_VISITOR_FLY_SPEED);
		
		int i = 4;
		
		for(Entry<ItemStack, PlotParamType<Boolean>> e : switchButtons.entrySet()) {
			i++;
			setItem(i, setSwitchState(e.getKey(), plot.getParameters().getParameter(e.getValue())), (item, c, s) -> {
				if (!canChangeSettings)
					return;

				e.getValue().setValue(plot, !getSwitchState(item));
				changeItem(item, setSwitchState(item, !getSwitchState(item)));
			});
		}
	}

	public static ItemStack setSwitchState(ItemStack item, boolean newState) {
		ItemStack it = item.clone();
		
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
