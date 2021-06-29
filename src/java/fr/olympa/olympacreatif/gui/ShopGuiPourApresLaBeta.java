 package fr.olympa.olympacreatif.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.olympacreatif.perks.KitsManager.KitType;
import fr.olympa.olympacreatif.perks.UpgradesManager.UpgradeType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;



public class ShopGuiPourApresLaBeta extends IGui {
	
	//init têtes
	private static final ItemStack ranksRowHead = ItemUtils.skullCustom("§6Grades", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWI1NzViNTU3N2NjYjMyZTQyZDU0MzA0YTFlZjVmMjNhZDZiYWQ1YTM0NTYzNDBhNDkxMmE2MmIzNzk3YmI1In19fQ==", 
			"§a§lLes grades sont tous à vie",
			"",
			"§aPour acheter un grade, vous devez",
			"§aposséder le précédent");
	private static final ItemStack kitsRowHead = ItemUtils.skullCustom("§6Kits", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjg4MjRkY2Y0YmEzMTc1MzNiZjI5ZGNhMThjZTdjNGZkMzI4YjQyNjgwZTZjMzIyZjVmNGZmMWEzOTRhODg3In19fQ==",
			"§a§lLes kits sont tous à vie",
			"",
			"§aLes kits sont des permissions",
			"§asuplémentaires indépendantes des",
			"§agrades. Ils sont valables sur toutes",
			"§ales parcelles que vous possédez");
	private static final ItemStack upgradesRowHead = ItemUtils.skullCustom("§6Améliorations", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODA3M2FlNTQ3ZTZkYWE5ZDJkYzhjYjkwZTc4ZGQxYzcxY2RmYWRiNzQwMWRjMTY3ZDE2ODE5YjE3MzI4M2M1MSJ9fX0=",
			"§a§lLes améliorations sont toutes à vie",
			"",
			"§aChaque amélioration possède plusieurs",
			"§aniveaux, chacun procurant une augmentation",
			"§alinéaire de la valeur indiquée");

	private int rankIndex = 0;
	private int kitIndex = 9;
	private int upgradeIndex = 18;
	
	
	public ShopGuiPourApresLaBeta(IGui gui) {
		super(gui, "Magasin", 3, gui.staffPlayer);

		inv.setItem(0, ranksRowHead);
		inv.setItem(9, kitsRowHead);
		inv.setItem(18, upgradesRowHead);
		
		setItem(8, ItemUtils.item(Material.GOLD_INGOT, "§6Ouvrir la boutique", "§7Cliquez ici pour ouvrir", "§7la boutique sur le site"), 
				(it, click, slot) -> sendBuyMessage("Cliquez ici pour ouvrir la boutique.", "olympa.fr"));
		
		Player player = (Player) p.getPlayer();
		//init rangs
		addRank(OlympaGroup.CREA_CONSTRUCTOR, ItemUtils.item(Material.IRON_AXE, "§6Grade " + OlympaGroup.CREA_CONSTRUCTOR.getName(p.getGender()), 
				"§2Ce grade donne accès à :", 
				" ",
				"§aPréfixe " + OlympaGroup.CREA_CONSTRUCTOR.getPrefix(p.getGender()) + player.getName(),  
				"§a+1 parcelle (passage de " + p.getPlotsSlots(true) + " à " + (p.getPlotsSlots(true) + 1) + ")", 
				"§aAccès aux microblocks et aux têtes (/mb, /skull)", 
				"§aAccès à la réinitialisation de vos parcelles (/oco reset)",
				"§aAccès aux chapeaux (/hat)"
				
				), "olympa.fr", "1 €");
		
		addRank(OlympaGroup.CREA_ARCHITECT, ItemUtils.item(Material.GOLDEN_AXE, "§6Grade " + OlympaGroup.CREA_ARCHITECT.getName(p.getGender()), 
				"§2En plus des avantages du niveau précédent,",
				"§2ce grade donne accès à :",
				" ",
				"§aPréfixe " + OlympaGroup.CREA_ARCHITECT.getPrefix(p.getGender()) + player.getName(), 
				"§a+2 parcelles (passage de " + p.getPlotsSlots(true) + " à " + (p.getPlotsSlots(true) + 2) + ")",
				"§aAccès aux commandes WorldEdit et goBrush",
				"§aExport de vos parcelles en .schematic (/oco export)",
				"§aRestauration de vos parcelles (/oco restore)",
				" ",
				"§7Le niveau précédent est requis pour acheter ce grade."
				
				), "olympa.fr", "2 €");
		
		addRank(OlympaGroup.CREA_CREATOR, ItemUtils.item(Material.DIAMOND_AXE, "§6Grade " + OlympaGroup.CREA_CREATOR.getName(p.getGender()), 
				"§2En plus des avantages du niveau précédent,",
				"§2ce grade donne accès à :",
				" ",
				"§aPréfixe " + OlympaGroup.CREA_CREATOR.getPrefix(p.getGender()) + player.getName(), 
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
				
				), "olympa.fr", "3 €");

		
		
		addKit(KitType.COMMANDBLOCK, ItemUtils.item(Material.COMMAND_BLOCK, "§6Kit commandblocks", 
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
				
				), "olympa.fr", "4 €");
		
		addKit(KitType.REDSTONE, ItemUtils.item(Material.REDSTONE_TORCH, "§6Kit redstone",
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
				
				), "olympa.fr", "5 €");
		
		addKit(KitType.FLUIDS, ItemUtils.item(Material.WATER_BUCKET, "§6Kit fluides",
				"§2Caractéristiques :",
				" ",
				"§aCe kit permet à l'§eeau et à la lave", 
				"§ade couler sur vos parcelles,",
				"§aau lieu de rester statique.",
				" ",
				"§7Les machines à lag sont interdites. ", 
				"§7En cas d'abus, les fluides arrêteront",
				"§7de couler sur la parcelle."
				
				), "olympa.fr", "6 €");
		
		addKit(KitType.PEACEFUL_MOBS, ItemUtils.item(Material.PIG_SPAWN_EGG, "§6Kit animaux",
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
				"§7n'apparaîtront plus sur la parcelle."
				
				), "olympa.fr", "7 €");
		
		addKit(KitType.HOSTILE_MOBS, ItemUtils.item(Material.CREEPER_SPAWN_EGG, "§6Kit monstres",
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
				"§7n'apparaîtront plus sur la parcelle."
				
				), "olympa.fr", "8 €");
		
		addUpgrade(UpgradeType.BONUS_PLOTS_LEVEL, ItemUtils.item(Material.GRASS_BLOCK, "§6Augmentation du nombre de parcelles",
				"§2Contenu :",
				" ",
				"§aCette amélioration augmente le",
				"§anombre de parcelles sur lesquelles",
				"§avous êtes propriétaire."
				
				), "olympa.fr");

		addUpgrade(UpgradeType.BONUS_MEMBERS_LEVEL, ItemUtils.item(Material.ACACIA_DOOR, "§6Augmentation du nombre de parcelles",
				"§2Contenu :",
				" ",
				"§aCette amélioration augmente le", 
				"§anombre de membres que vous pouvez",
				"§arecruter sur vos parcelles."
				
				), "olympa.fr");

		addUpgrade(UpgradeType.CB_LEVEL, ItemUtils.item(Material.REPEATING_COMMAND_BLOCK, "§6Augmentation du nombre de parcelles",
				"§2Contenu :",
				" ",
				"§aCette amélioration augmente le nombre",
				"§ade commandblocks pouvant s'exécuter",
				"§achaque seconde sur vos parcelles."
				
				), "olympa.fr");

	}
	
	
	private void addRank(final OlympaGroup group, ItemStack item, final String url, String price) {
		rankIndex++;
		
		OlympaGroup previousGroup = group == OlympaGroup.CREA_CONSTRUCTOR ? OlympaGroup.PLAYER :
									group == OlympaGroup.CREA_ARCHITECT ? OlympaGroup.CREA_CONSTRUCTOR :
									OlympaGroup.CREA_ARCHITECT;
		
		if (p.getGroups().containsKey(group))
			setItem(rankIndex, ItemUtils.loreAdd(item, " ", "§9Grade déjà possédé"), null);
		
		else if (p.getGroups().containsKey(previousGroup)) 
			setItem(rankIndex, 
					ItemUtils.loreAdd(item, " ", "§bAchetable", "§bPrix : " + price),
					(it, click, slot) -> sendBuyMessage("Cliquez ici pour acheter le grade " + group.getName(p.getGender()) + " !", url));
		
		else
			setItem(rankIndex, ItemUtils.loreAdd(item, " ", "§cCet achat nécessite le grade précédent"), null);
		
		
	}
	
	private void addKit(KitType kit, ItemStack item, String url, String price) {
		kitIndex++;
		
		if (p.hasKit(kit))
			setItem(kitIndex, ItemUtils.loreAdd(item, " ", "§9Kit déjà possédé"), null);
		
		else 
			setItem(kitIndex, 
					ItemUtils.loreAdd(item, " ", "§bAchetable", "§bPrix : " + price),
					(it, click, slot) -> sendBuyMessage("Cliquez ici pour acheter le kit " + kit.getName() + " !", url));
	}
	
	private void addUpgrade(UpgradeType upgrade, ItemStack item, String url) {
		upgradeIndex++;
		int level = upgrade.getDataOf(p).level;
		
		if (upgrade == UpgradeType.CB_LEVEL && !p.hasKit(KitType.COMMANDBLOCK))
			setItem(upgradeIndex, ItemUtils.loreAdd(item, " ", "§7Le kit commandblock est nécessaire", "§7pour effectuer cet achat"), null);
		
		else if (level < upgrade.getMaxLevel())
			setItem(upgradeIndex, ItemUtils.loreAdd(item, " ", 
					"§eAmélioration : " + upgrade.getDataOf(level).value + " ➔ " + upgrade.getDataOf(level + 1).value, 
					" ", "§bAchetable", "§bPrix : " + upgrade.getDataOf(level + 1).price), 
					(it, click, slot) -> sendBuyMessage("Cliquez ici pour acheter l'amélioration " + upgrade.getName() + " !", url));
		
		else
			setItem(upgradeIndex, ItemUtils.loreAdd(item, " ", "§8Niveau maximum atteint"), null);
			
	}
	
	private void sendBuyMessage(String text, String url) {
		Player player = (Player) p.getPlayer();
		player.closeInventory();
		
		TextComponent component = Component.text()
				.append(Component.text("Olympa ").color(NamedTextColor.GOLD))
				.append(Component.text("➤ ").color(NamedTextColor.GRAY))
				.append(Component.text(text).color(NamedTextColor.AQUA))
				.clickEvent(ClickEvent.openUrl(url)).build();
		
		player.sendMessage(component);
	}
}
