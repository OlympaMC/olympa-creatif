package fr.olympa.olympacreatif.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
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
			 clickToChange = new String[] {" ", "§8Ne concerne que les visiteurs", "§8Cliquez pour changer la valeur"};
		else
			 clickToChange = new String[] {" ", "§8Ne concerne que les visiteurs"};
		
		//ajout des options
		ItemStack it = null;
		ItemMeta im = null;
		
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
		for (Biome biome : PlotParameters.getAllPossibleBiomes())
			if (biome == plot.getParameters().getParameter(PlotParamType.PLOT_BIOME))
				it = ItemUtils.loreAdd(it, "§aActuel : " + biome.toString());
			else
				it = ItemUtils.loreAdd(it, "§2Disponible : " + biome.toString());
		
		if (plot.getMembers().getPlayerRank(p) == PlotRank.OWNER)
			it = ItemUtils.loreAdd(it, clickToChange);
		inv.setItem(6,it);
	}

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
			if (plot.getMembers().getPlayerRank(p) != PlotRank.OWNER)
				return true;
			
			int biomeRank = PlotParameters.getAllPossibleBiomes().indexOf(newBiome);
			newBiome = PlotParameters.getAllPossibleBiomes().get((biomeRank+1) % PlotParameters.getAllPossibleBiomes().size());
			
			current = ItemUtils.lore(current, "");
			for (Biome biome : PlotParameters.getAllPossibleBiomes())
				if (biome == newBiome)
					current = ItemUtils.loreAdd(current, "§aActuel : " + biome.toString());
				else
					current = ItemUtils.loreAdd(current, "§2Disponible : " + biome.toString());
			
			current = ItemUtils.loreAdd(current, clickToChange);			
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
			for (int x = plot.getId().getLocation().getBlockX() ; x < plot.getId().getLocation().getBlockX() + Integer.valueOf(Message.PARAM_PLOT_X_SIZE.getValue()) ; x++)
				for (int z = plot.getId().getLocation().getBlockZ() ; z < plot.getId().getLocation().getBlockZ() + Integer.valueOf(Message.PARAM_PLOT_Z_SIZE.getValue()) ; z++)
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
			list.add(0, "§eEtat : §aautorisé");
			ItemMeta im = it.getItemMeta();
			im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			it.setItemMeta(im);
			it.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
		}
		else {
			list.add(0, "§eEtat : §cinterdit");
			it.removeEnchantment(Enchantment.DURABILITY);
		}
		if (list.size() >= 2)
			list.remove(1);
		
		return ItemUtils.lore(it,  list.toArray(new String[list.size()]));
	}
	
	public boolean getSwitchState(ItemStack it) {
		if (it.getItemMeta().getLore() == null)
			return false;
		
		if (it.getItemMeta().getLore().get(0).equals("§eEtat : §aautorisé"))
			return true;
		else
			return false;
	}
}
