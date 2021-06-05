 package fr.olympa.olympacreatif.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.olympa.api.common.chat.Gradient;
import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OcPermissions;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.perks.KitsManager.KitType;
import fr.olympa.olympacreatif.perks.UpgradesManager.UpgradeType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.HSVLike;
import net.kyori.adventure.util.RGBLike;



public class ShopGuiPourApresLaBeta extends IGui{
	
	//init têtes
	private static final ItemStack ranksRowHead = ItemUtils.skullCustom("§6Grades", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWI1NzViNTU3N2NjYjMyZTQyZDU0MzA0YTFlZjVmMjNhZDZiYWQ1YTM0NTYzNDBhNDkxMmE2MmIzNzk3YmI1In19fQ==");
	private static final ItemStack kitsRowHead = ItemUtils.skullCustom("§6Kits", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjg4MjRkY2Y0YmEzMTc1MzNiZjI5ZGNhMThjZTdjNGZkMzI4YjQyNjgwZTZjMzIyZjVmNGZmMWEzOTRhODg3In19fQ==");
	private static final ItemStack upgradesRowHead = ItemUtils.skullCustom("§6Améliorations", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODA3M2FlNTQ3ZTZkYWE5ZDJkYzhjYjkwZTc4ZGQxYzcxY2RmYWRiNzQwMWRjMTY3ZDE2ODE5YjE3MzI4M2M1MSJ9fX0=");
	
	private static final ItemStack buyProcessNullItem = ItemUtils.item(Material.BEDROCK, "§7Sélectionnez un objet à acheter");
	private static final ItemStack buyProcessArrow = ItemUtils.skullCustom(" ", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19");
	private static final ItemStack buyProcessQuestion = ItemUtils.skullCustom("§7En attente...", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmFkYzA0OGE3Y2U3OGY3ZGFkNzJhMDdkYTI3ZDg1YzA5MTY4ODFlNTUyMmVlZWQxZTNkYWYyMTdhMzhjMWEifX19");
	private static final ItemStack buyProcessAccept = ItemUtils.skullCustom("§aCliquez §2§lICI §r§apour acheter", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzQwNjNiYTViMTZiNzAzMGEyMGNlNmYwZWE5NmRjZDI0YjA2NDgzNmY1NzA0NTZjZGJmYzllODYxYTc1ODVhNSJ9fX0=");
	private static final ItemStack buyProcessDenyItem = ItemUtils.skullCustom("§cAchat impossible", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzIwZWYwNmRkNjA0OTk3NjZhYzhjZTE1ZDJiZWE0MWQyODEzZmU1NTcxODg2NGI1MmRjNDFjYmFhZTFlYTkxMyJ9fX0=");

	private List<MarketItemData> ranks = new ArrayList<MarketItemData>();
	private List<MarketItemData> kits = new ArrayList<MarketItemData>();
	private List<MarketItemData> upgrades = new ArrayList<MarketItemData>();

	int buyProcessItemSlot;
	int buyProcessStateSlot;
	
	public ShopGuiPourApresLaBeta(IGui gui) {
		super(gui, "Magasin", 3, gui.staffPlayer);

		inv.setItem(0, ranksRowHead);
		inv.setItem(9, kitsRowHead);
		inv.setItem(18, upgradesRowHead);
		
		//init rangs
		inv.setItem(1, ItemUtils.item(Material.IRON_AXE, "§6Grade " + OlympaGroup.CREA_CONSTRUCTOR.getName(p.getGender()), 
				"§2Ce grade donne accès à :", 
				" ",
				"§aPréfixe " + OlympaGroup.CREA_CONSTRUCTOR.getPrefix(p.getGender()) + p.getPlayer().getName(),  
				"§a+1 parcelle (passage de " + p.getPlotsSlots(true) + " à " + (p.getPlotsSlots(true) + 1) + ")", 
				"§aAccès aux microblocks et aux têtes (/mb, /skull)", 
				"§aAccès à la réinitialisation de vos parcelles (/oco reset)",
				"§aAccès aux chapeaux (/hat)"
				));
		
		inv.setItem(2, ItemUtils.item(Material.GOLDEN_AXE, "§6Grade " + OlympaGroup.CREA_ARCHITECT.getName(p.getGender()), 
				"§2En plus des avantages du niveau précédent,",
				"§2ce grade donne accès à :",
				" ",
				"§aPréfixe " + OlympaGroup.CREA_ARCHITECT.getPrefix(p.getGender()) + p.getPlayer().getName(), 
				"§a+2 parcelles (passage de " + p.getPlotsSlots(true) + " à " + (p.getPlotsSlots(true) + 2) + ")",
				"§aAccès aux commandes WorldEdit et goBrush",
				"§aExport de vos parcelles en .schematic (/oco export)",
				"§aRestauration de vos parcelles (/oco restore)",
				" ",
				"§7Le niveau précédent est requis pour acheter ce grade."
				));
		
		inv.setItem(3, ItemUtils.item(Material.DIAMOND_AXE, "§6Grade " + OlympaGroup.CREA_CREATOR.getName(p.getGender()), 
				"§2En plus des avantages du niveau précédent,",
				"§2ce grade donne accès à :",
				" ",
				"§aPréfixe " + OlympaGroup.CREA_CREATOR.getPrefix(p.getGender()) + p.getPlayer().getName(), 
				"§aAccès à toutes les couleurs dans le chat",
				"§aAccès aux hologrammes (/holo)",
				"§aAccès à l'éditeur d'armorstands (/oco armorstand_editor)",
				" ",
				"§7Le niveau précédent est requis pour acheter ce grade.",
				" ",
				"§6Mais avant tout, nous vous remercions",
				"§6chaleureusement du soutien que vous nous apportez !",
				"§6En espérant vous voir encore longtemps parmis nous,",
				"§cL'équipe dévouée d'Olympa"
				));

		
		
		inv.setItem(10, ItemUtils.item(Material.COMMAND_BLOCK, "§6Kit commandblocks", 
				"§2Caractéristiques :",
				" ",
				"§aCe kit vous permet d'§eutiliser les",
				"§ecommandblocks §asur vos parcelles.",
				"§aLa liste des commandes disponibles", 
				"§ase trouve sur le forum.",
				" ",
				"§7Attention, les commandes par seconde (CPS)",
				"§7sont limitées pour éviter les lags.",
				"§7Si vous avez besoin de plus de CPS",
				"§7achetez l'amélioration correspondante.",
				" ",
				"§7Pour voir votre consommation de CPS : /oco debug"
				));
		
		inv.setItem(11, ItemUtils.item(Material.REDSTONE_TORCH, "§6Kit redstone",
				"§2Caractéristiques :",
				" ",
				"§aCe kit vous permet d'utiliser",
				"§atous les blocs liés à la §eredstone§a,", 
				"§asur toutes les parcelles, y compris",
				"§acelles où vous n'êtes pas propriétaire.",
				" ",
				"§7Les machines à lag sont interdites. ", 
				"§7En cas d'abus, les courants de redstone",
				"§7se bloqueront sur la parcelle."
				));
		
		inv.setItem(12, ItemUtils.item(Material.WATER_BUCKET, "§6Kit fluides",
				"§2Caractéristiques :",
				" ",
				"§aCe kit permet à l'§eeau et à la lave", 
				"§ade couler sur vos parcelles,",
				"§aau lieu de rester statique.",
				" ",
				"§7Les machines à lag sont interdites. ", 
				"§7En cas d'abus, les fluides arrêteront",
				"§7de couler sur la parcelle."
				));
		
		kits.add(new MarketItemData(p, KitType.PEACEFUL_MOBS, 1, ItemUtils.item(Material.PIG_SPAWN_EGG, "§6Kit animaux",
				"§2Caractéristiques :",
				" ",
				"§aCe kit vous permet d'utiliser tous les ",
				"§eoeufs d'animaux §asur toutes les parcelles", 
				"§aoù vous êtes au minimum membre.",
				" ",
				"§eCe kit permet également l'utilisation", 
				"§ede tags sur les oeufs.",
				" ",
				"§7Les machines à lag sont interdites. ", 
				"§7En cas d'abus, les entités",
				"§7n'apparaîtront plus sur la parcelle.")));
		
		kits.add(new MarketItemData(p, KitType.HOSTILE_MOBS, 1, ItemUtils.item(Material.CREEPER_SPAWN_EGG, "§6Kit monstres",
				"§2Caractéristiques :",
				" ",
				"§aCe kit vous permet d'utiliser tous les ",
				"§eoeufs de monstres §asur toutes les parcelles", 
				"§aoù vous êtes au minimum membre.",
				" ",
				"§eCe kit permet également l'utilisation", 
				"§ede tags sur les oeufs.",
				" ",
				"§7Les machines à lag sont interdites. ", 
				"§7En cas d'abus, les entités",
				"§7n'apparaîtront plus sur la parcelle.")));

		upgrades.add(new MarketItemData(p, UpgradeType.BONUS_PLOTS_LEVEL, 0, ItemUtils.item(Material.GRASS_BLOCK, "§6Augmentation du nombre de parcelles",
				"§2Contenu :",
				" ",
				"§aCette amélioration augmente le",
				"§anombre de parcelles sur lesquelles",
				"§avous êtes propriétaire.")));
		
		upgrades.add(new MarketItemData(p, UpgradeType.BONUS_MEMBERS_LEVEL, 0, ItemUtils.item(Material.ACACIA_DOOR, "§6Augmentation nombre membres par parcelle",
				"§2Contenu :",
				" ",
				"§aCette amélioration augmente le", 
				"§anombre de membres que vous pouvez",
				"§arecruter sur vos parcelles.")));
		
		upgrades.add(new MarketItemData(p, UpgradeType.CB_LEVEL, 0, ItemUtils.item(Material.REPEATING_COMMAND_BLOCK, "§6Augmentation CPS commandblocks",
				"§2Contenu :",
				" ",
				"§aCette amélioration augmente le",
				"§anombre de commandblocks s'exécutant",
				"§achaque seconde sur vos parcelles.",
				" ",
				"§7Attention : cette amélioration est inutile si",
				"§7vous n'avez pas acheté le §6kit commandblocks §7!")));
		
		//CREATION GUI
		
		buyProcessItemSlot = inv.getSize() - 5;
		buyProcessStateSlot = inv.getSize() - 3;
		
		setItem(buyProcessItemSlot, buyProcessNullItem, null);
		setItem(inv.getSize() - 4, buyProcessArrow, null);
		setItem(buyProcessStateSlot, buyProcessQuestion, null);
		
		//ajout grades
		int i = 0;
		
		setItem(i, ranksRowHead, null);
		for (MarketItemData e : ranks) {
			i++;
			setItem(i, e.getHolder(), (it, c, s) -> {
				//si l'objet est achetable, lancement timer d'achat, sinon maj de l'indicateur d'achat
				if (e.isBuyable())
					startBuyAcceptTimer(e);
				else
					startBuyDenyTimer(e);
			});
		}
		
		//ajout kits
		i = 9;
		
		setItem(i, kitsRowHead, null);
		for (MarketItemData e : kits) {
			i++;
			setItem(i, e.getHolder(), (it, c, s) -> {
				//si l'objet est achetable, lancement timer d'achat, sinon maj de l'indicateur d'achat
				if (e.isBuyable())
					startBuyAcceptTimer(e);
				else
					startBuyDenyTimer(e);
			});
		}
		
		//ajout upgrades
		i = 18;
		
		setItem(i, upgradesRowHead, null);
		for (MarketItemData e : upgrades) {
			i++;
			setItem(i, e.getHolder(), (it, c, s) -> {
				//si l'objet est achetable, lancement timer d'achat, sinon maj de l'indicateur d'achat
				if (e.isBuyable())
					startBuyAcceptTimer(e);
				else
					startBuyDenyTimer(e);
			});
		}
	}

	private int rankIndex = 0;
	private int kitIndex = 9;
	private int upgradeIndex = 18;
	private Map<Integer, Runnable> onClick = new HashMap<Integer, Runnable>();
	
	
	private void addRank(final OlympaGroup group, ItemStack item, final String url, String price) {
		rankIndex++;
		
		OlympaGroup previousGroup = group == OlympaGroup.CREA_CONSTRUCTOR ? OlympaGroup.PLAYER :
									group == OlympaGroup.CREA_ARCHITECT ? OlympaGroup.CREA_CONSTRUCTOR :
									OlympaGroup.CREA_ARCHITECT;
		
		if (p.getGroups().containsKey(group))
			inv.setItem(rankIndex, ItemUtils.loreAdd(item, " ", "§9Grade déjà possédé"));
		else if (p.getGroups().containsKey(previousGroup)) {
			inv.setItem(rankIndex, 
					ItemUtils.loreAdd(item, " ", "§aAchetable", "§aPrix : " + price + " " + "§7Cliquez ici pour ouvrir la boutique"));
			
			onClick.put(rankIndex, () -> sendBuyMessage("Cliques ici pour acheter le grade " + group.getName(p.getGender()) + " !", url));
		}else
			inv.setItem(rankIndex, ItemUtils.loreAdd(item, " ", "§cCet achat nécessite le grade précédent"));
		
		
	}
	
	private void addKit(KitType kit, ItemStack item, String url, String price) {
		kitIndex++;
		
		if (p.hasKit(kit))
			inv.setItem(kitIndex, ItemUtils.loreAdd(item, " ", "§9Kit déjà possédé"));
		else {
			inv.setItem(kitIndex, 
					ItemUtils.loreAdd(item, " ", "§aAchetable", "§aPrix : " + price + " " + "§7Cliquez ici pour ouvrir la boutique"));
			
			onClick.put(kitIndex, () -> sendBuyMessage("Cliques ici pour acheter le kit " + kit.getName() + " !", url));
		}
	}
	
	private void addUpgrade(UpgradeType upgrade, ItemStack item, String url, String price) {
		
	}
	
	private void sendBuyMessage(String text, String url) {
		TextComponent component = Component.text()
				.color(NamedTextColor.GOLD)
				.append(Component.text("Olympa "))
				.color(NamedTextColor.GRAY)
				.append(Component.text("➤ "))
				.color(NamedTextColor.GREEN)
				.append(Component.text(text))
				.clickEvent(ClickEvent.openUrl(url)).build();
		
		p.getPlayer().sendMessage(component);
	}
}
