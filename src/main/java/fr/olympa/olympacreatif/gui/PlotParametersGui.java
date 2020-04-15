package fr.olympa.olympacreatif.gui;

import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;
import fr.olympa.olympacreatif.plot.PlotParamType;
import fr.olympa.olympacreatif.plot.PlotParameters;

public class PlotParametersGui extends OlympaGUI {

	private OlympaCreatifMain plugin;
	private Player p;
	private Plot plot;
	
	private Biome newBiome;
	private String[] clickToChange = null;
	
	@SuppressWarnings("deprecation")
	public PlotParametersGui(OlympaCreatifMain plugin, Player p, Plot plot) {
		super("§6Paramètres du plot : " + plot.getId().getAsString(), 1);
		this.plugin = plugin;
		this.p = p;
		this.plot = plot;

		newBiome = (Biome) plot.getParameters().getParameter(PlotParamType.PLOT_BIOME);
		
		inv.setItem(8, ItemUtils.item(Material.ACACIA_DOOR, "§cRetour", ""));
		
		if (plot.getMembers().getPlayerLevel(p) >= 3)
			 clickToChange = new String[] {" ", "§8Cliquez pour changer la valeur"};
		else
			 clickToChange = new String[] {""};
		
		//ajout des options
		ItemStack it = null;
		ItemMeta im = null;
		
		//0 : Gamemode par défaut
		it = ItemUtils.item(Material.ACACIA_SIGN, "§6Gamemode par défaut", "§eMode actuel : " + plot.getParameters().getParameter(PlotParamType.GAMEMODE_INCOMING_PLAYERS).toString());
		it = ItemUtils.loreAdd(it, clickToChange);
		it.setAmount(((GameMode) plot.getParameters().getParameter(PlotParamType.GAMEMODE_INCOMING_PLAYERS)).getValue());
		
		inv.setItem(0,it);
		
		//1 : Autorisation fly
		it = ItemUtils.item(Material.FEATHER, Message.GUI_PARAMS_ALLOW_FLIGHT.getValue(), Message.GUI_PARAMS_ALLOW_FLIGHT_LORE.getValue());
		it = ItemUtils.loreAdd(it, clickToChange);
		im = it.getItemMeta();
		im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		it.setItemMeta(im);
		
		if ((boolean) plot.getParameters().getParameter(PlotParamType.ALLOW_FLY_INCOMING_PLAYERS))
			it = ItemUtils.addEnchant(it, Enchantment.DURABILITY, 1);

		inv.setItem(1,it);

		//2 : Forcer spawn zone
		it = ItemUtils.item(Material.ACACIA_FENCE_GATE, Message.GUI_PARAMS_FORCE_SPAWN_LOC.getValue(), Message.GUI_PARAMS_FORCE_SPAWN_LOC_LORE.getValue());
		it = ItemUtils.loreAdd(it, clickToChange);
		im = it.getItemMeta();
		im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		it.setItemMeta(im);
		
		if ((boolean) plot.getParameters().getParameter(PlotParamType.FORCE_SPAWN_LOC))
			it = ItemUtils.addEnchant(it, Enchantment.DURABILITY, 1);

		inv.setItem(2,it);

		//3 : Autorisation allumer tnt
		it = ItemUtils.item(Material.TNT, Message.GUI_PARAMS_ALLOW_TNT.getValue(), Message.GUI_PARAMS_ALLOW_TNT_LORE.getValue());
		it = ItemUtils.loreAdd(it, clickToChange);
		im = it.getItemMeta();
		im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		it.setItemMeta(im);
		
		if ((boolean) plot.getParameters().getParameter(PlotParamType.ALLOW_PRINT_TNT))
			it = ItemUtils.addEnchant(it, Enchantment.DURABILITY, 1);

		inv.setItem(3,it);

		//4 : Heure du plot
		it = ItemUtils.item(Material.CLOCK, Message.GUI_PARAMS_PLOT_TIME.getValue(), Message.GUI_PARAMS_PLOT_TIME_LORE.getValue());
		it = ItemUtils.loreAdd(it, clickToChange);
		it.setAmount((int) plot.getParameters().getParameter(PlotParamType.ALLOW_PRINT_TNT) / 1000);
		
		inv.setItem(4,it);

		//5 : Clear inventaire joueur
		it = ItemUtils.item(Material.CAULDRON, Message.GUI_PARAMS_CLEAR_PLAYERS.getValue(), Message.GUI_PARAMS_CLEAR_PLAYERS_LORE.getValue());
		it = ItemUtils.loreAdd(it, clickToChange);
		im = it.getItemMeta();
		im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		it.setItemMeta(im);
		
		if ((boolean) plot.getParameters().getParameter(PlotParamType.CLEAR_INCOMING_PLAYERS))
			it = ItemUtils.addEnchant(it, Enchantment.DURABILITY, 1);

		inv.setItem(5,it);

		//6 : Sélection du biome
		it = ItemUtils.item(Material.GRASS_PATH, "§2Biome : ");
		it = ItemUtils.loreAdd(it, clickToChange);
		for (Biome biome : PlotParameters.getAllPossibleBiomes())
			if (biome == plot.getParameters().getParameter(PlotParamType.PLOT_BIOME))
				it = ItemUtils.loreAdd(it, "§aActuel : " + biome.toString());
			else
				it = ItemUtils.loreAdd(it, "§2Disponible : " + biome.toString());
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (slot == 8) {
			new MainGui(plugin, p).create(p);
			return true;
		}
		
		if (plot.getMembers().getPlayerLevel(p)<3)
			return true;
		
		//modification des options
		switch (slot) {
		case 0:
			current.setAmount((current.getAmount()+1)%3);
			plot.getParameters().setParameter(PlotParamType.GAMEMODE_INCOMING_PLAYERS, GameMode.getByValue(current.getAmount()));
			current = ItemUtils.lore(current, "§eMode actuel : " + GameMode.getByValue(current.getAmount()));
			current = ItemUtils.loreAdd(current, clickToChange);
			break;
		case 1:
			if (current.containsEnchantment(Enchantment.DURABILITY)) {
				current.removeEnchantment(Enchantment.DURABILITY);
				plot.getParameters().setParameter(PlotParamType.ALLOW_FLY_INCOMING_PLAYERS, false);
			}else {
				current.addEnchantment(Enchantment.DURABILITY, 1);
				plot.getParameters().setParameter(PlotParamType.ALLOW_FLY_INCOMING_PLAYERS, true);
			}				
			break;
		case 2:
			if (current.containsEnchantment(Enchantment.DURABILITY)) {
				current.removeEnchantment(Enchantment.DURABILITY);
				plot.getParameters().setParameter(PlotParamType.FORCE_SPAWN_LOC, false);
			}else {
				current.addEnchantment(Enchantment.DURABILITY, 1);
				plot.getParameters().setParameter(PlotParamType.FORCE_SPAWN_LOC, true);
			}
			break;
		case 3:
			if (current.containsEnchantment(Enchantment.DURABILITY)) {
				current.removeEnchantment(Enchantment.DURABILITY);
				plot.getParameters().setParameter(PlotParamType.ALLOW_PRINT_TNT, false);
			}else {
				current.addEnchantment(Enchantment.DURABILITY, 1);
				plot.getParameters().setParameter(PlotParamType.ALLOW_PRINT_TNT, true);
			}
			break;
		case 4:
			current.setAmount((current.getAmount()+1)%25);
			plot.getParameters().setParameter(PlotParamType.PLOT_TIME, current.getAmount() * 1000);
			current = ItemUtils.lore(current, "§eHeure actuelle : " + current.getAmount() + "h");
			current = ItemUtils.loreAdd(current, clickToChange);
			for (Player pp : plot.getPlayers())
				pp.setPlayerTime(current.getAmount()*1000, true);
			break;
		case 5:
			if (current.containsEnchantment(Enchantment.DURABILITY)) {
				current.removeEnchantment(Enchantment.DURABILITY);
				plot.getParameters().setParameter(PlotParamType.CLEAR_INCOMING_PLAYERS, false);
			}else {
				current.addEnchantment(Enchantment.DURABILITY, 1);
				plot.getParameters().setParameter(PlotParamType.CLEAR_INCOMING_PLAYERS, true);
			}
			break;
		case 6:
			//édition du biome réservée au propriétaire
			if (plot.getMembers().getPlayerRank(p) != PlotRank.OWNER)
				return true;
			
			int biomeRank = PlotParameters.getAllPossibleBiomes().indexOf(plot.getParameters().getParameter(PlotParamType.PLOT_BIOME));
			newBiome = PlotParameters.getAllPossibleBiomes().get((biomeRank+1) % PlotParameters.getAllPossibleBiomes().size());
			
			current = ItemUtils.name(current, newBiome.toString());
			for (Biome biome : PlotParameters.getAllPossibleBiomes())
				if (biome == newBiome)
					current = ItemUtils.loreAdd(current, "§aActuel : " + biome.toString());
				else
					current = ItemUtils.loreAdd(current, "§2Disponible : " + biome.toString());			
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
		if (plot.getParameters().getParameter(PlotParamType.PLOT_BIOME) != newBiome) {
			for (int x = plot.getId().getLocation().getBlockX() ; x < plot.getId().getLocation().getBlockX() + Integer.valueOf(Message.PARAM_PLOT_X_SIZE.getValue()) ; x++)
				for (int z = plot.getId().getLocation().getBlockZ() ; z < plot.getId().getLocation().getBlockZ() + Integer.valueOf(Message.PARAM_PLOT_Z_SIZE.getValue()) ; z++)
					for (int y = 1 ; y < 255 ; y++)
						plugin.getWorldManager().getWorld().getBlockAt(x, y, z).setBiome(newBiome);
			
			plot.getParameters().setParameter(PlotParamType.PLOT_BIOME, newBiome);	
		}
		
		return true;
	}

}
