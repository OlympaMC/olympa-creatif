 package fr.olympa.olympacreatif.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent;
import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent.ChangeType;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.player.Gender;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.perks.KitsManager.KitType;
import fr.olympa.olympacreatif.perks.UpgradesManager.UpgradeType;



public class ShopGui extends IGui{

	private ItemStack ranksRowHead;
	private ItemStack kitsRowHead;
	private ItemStack upgradesRowHead;
	
	private final ItemStack buyProcessNullItem = ItemUtils.item(Material.BEDROCK, "§7Sélectionnez un objet à acheter");
	private final ItemStack buyProcessArrow = ItemUtils.skullCustom(" ", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19");
	//private ItemStack buyProcess1 = ItemUtils.skullCustom("§71...", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzFiYzJiY2ZiMmJkMzc1OWU2YjFlODZmYzdhNzk1ODVlMTEyN2RkMzU3ZmMyMDI4OTNmOWRlMjQxYmM5ZTUzMCJ9fX0=");
	//private ItemStack buyProcess2 = ItemUtils.skullCustom("§72...", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNkOWVlZWU4ODM0Njg4ODFkODM4NDhhNDZiZjMwMTI0ODVjMjNmNzU3NTNiOGZiZTg0ODczNDE0MTk4NDcifX19");
	//private ItemStack buyProcess3 = ItemUtils.skullCustom("§73...", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWQ0ZWFlMTM5MzM4NjBhNmRmNWU4ZTk1NTY5M2I5NWE4YzNiMTVjMzZiOGI1ODc1MzJhYzA5OTZiYzM3ZTUifX19");
	private final ItemStack buyProcessQuestion = ItemUtils.skullCustom("§7En attente...", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmFkYzA0OGE3Y2U3OGY3ZGFkNzJhMDdkYTI3ZDg1YzA5MTY4ODFlNTUyMmVlZWQxZTNkYWYyMTdhMzhjMWEifX19");
	private final ItemStack buyProcessAccept = ItemUtils.skullCustom("§aCliquez §2§lICI §r§apour acheter", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzQwNjNiYTViMTZiNzAzMGEyMGNlNmYwZWE5NmRjZDI0YjA2NDgzNmY1NzA0NTZjZGJmYzllODYxYTc1ODVhNSJ9fX0=");
	private final ItemStack buyProcessDeny = ItemUtils.skullCustom("§cAchat impossible", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzIwZWYwNmRkNjA0OTk3NjZhYzhjZTE1ZDJiZWE0MWQyODEzZmU1NTcxODg2NGI1MmRjNDFjYmFhZTFlYTkxMyJ9fX0=");
	
	//private MarketItemData cartItem = null;
	//private boolean cartItemReadyToBuy = false;
	
	//int firstRankPrice = 10;
	//int secondRankPrice = 20;

	private List<MarketItemData> ranks = new ArrayList<MarketItemData>();
	private List<MarketItemData> kits = new ArrayList<MarketItemData>();
	private List<MarketItemData> upgrades = new ArrayList<MarketItemData>();

	int buyProcessItemSlot;
	int buyProcessStateSlot;
	
	public ShopGui(IGui gui) {
		super(gui, "Magasin (monnaie : " + gui.getPlayer().getGameMoney() + " " + gui.getPlayer().getGameMoneyName() + ")", 4);

		//init têtes
		ranksRowHead = ItemUtils.skullCustom("§6Grades", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWI1NzViNTU3N2NjYjMyZTQyZDU0MzA0YTFlZjVmMjNhZDZiYWQ1YTM0NTYzNDBhNDkxMmE2MmIzNzk3YmI1In19fQ==");
		kitsRowHead = ItemUtils.skullCustom("§6Kits", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjg4MjRkY2Y0YmEzMTc1MzNiZjI5ZGNhMThjZTdjNGZkMzI4YjQyNjgwZTZjMzIyZjVmNGZmMWEzOTRhODg3In19fQ==");
		upgradesRowHead = ItemUtils.skullCustom("§6Améliorations", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODA3M2FlNTQ3ZTZkYWE5ZDJkYzhjYjkwZTc4ZGQxYzcxY2RmYWRiNzQwMWRjMTY3ZDE2ODE5YjE3MzI4M2M1MSJ9fX0=");
		
		//init rangs
		ranks.add(new MarketItemData(p, OlympaGroup.CREA_CONSTRUCTOR, 10, ItemUtils.item(Material.IRON_PICKAXE, "§6Grade " + OlympaGroup.CREA_CONSTRUCTOR.getName(p.getGender()), 
				"§2Ce grade donne accès à :", 
				" ",
				"§aPréfixe " + OlympaGroup.CREA_CONSTRUCTOR.getPrefix(p.getGender()) + p.getPlayer().getName(),  
				"§a+1 parcelle (passage de " + p.getPlotsSlots(true) + " à " + (p.getPlotsSlots(true) + 1) + ")", 
				"§aAccès aux microblocks et aux têtes"
				)));
		
		ranks.add(new MarketItemData(p, OlympaGroup.CREA_ARCHITECT, 20, ItemUtils.item(Material.GOLDEN_PICKAXE, "§6Grade " + OlympaGroup.CREA_ARCHITECT.getName(p.getGender()), 
				"§2En plus des avantages du niveau précédent,",
				"§2ce grade donne accès à :",
				" ",
				"§aPréfixe " + OlympaGroup.CREA_ARCHITECT.getPrefix(p.getGender()) + p.getPlayer().getName(), 
				"§a+2 parcelles (passage de " + p.getPlotsSlots(true) + " à " + (p.getPlotsSlots(true) + 2) + ")",
				"§aAccès aux commandes WorldEdit",
				"§aExport de vos parcelles en .schematic",
				"§aAccès au /hat",
				" ",
				"§7Le niveau précédent est requis pour acheter ce grade.")));
		
		//ajout du grade créateur si les prérequis sont respectés
		boolean hasAllKits = true;
		for (KitType kit : KitType.values())
			if (!p.hasKit(kit) && kit != KitType.ADMIN)
				hasAllKits = false;
		
		if (p.getGroups().containsKey(OlympaGroup.CREA_ARCHITECT) && hasAllKits)
			ranks.add(new MarketItemData(p, OlympaGroup.CREA_CREATOR, 30, ItemUtils.item(Material.DIAMOND_PICKAXE, "§6Grade " + OlympaGroup.CREA_CREATOR.getName(p.getGender()), 
					"§2En plus des avantages du niveau précédent,",
					"§2ce grade donne accès à :",
					" ",
					"§aPréfixe " + OlympaGroup.CREA_CREATOR.getPrefix(p.getGender()) + p.getPlayer().getName(), 
					"§aAccès à toutes les couleurs dans le chat",
					"§aMessage quand vous rejoignez le serveur",
					" ",
					"§6Mais avant tout, nous vous remercions",
					"§6chaleureusementdu soutien que vous nous apportez !",
					"§6En espérant vous voir encore longtemps parmis nous,",
					"§cL'équipe dévouée d'Olympa")));

		kits.add(new MarketItemData(p, KitType.COMMANDBLOCK, 10, ItemUtils.item(Material.COMMAND_BLOCK, "§6Kit commandblocks", 
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
				"§7Pour voir votre consommation de CPS : /oco debug")));
		
		kits.add(new MarketItemData(p, KitType.REDSTONE, 10, ItemUtils.item(Material.REDSTONE_TORCH, "§6Kit redstone",
				"§2Caractéristiques :",
				" ",
				"§aCe kit vous permet d'utiliser",
				"§atous les blocsliés à la §eredstone§a,", 
				"§asur toutes les parcelles, y compris",
				"§acelles où vous n'êtes pas propriétaire.",
				" ",
				"§7Les machines à lag sont interdites. ", 
				"§7En cas d'abus, les courant de redstone",
				"§7se bloqueront sur la parcelle.")));
		
		kits.add(new MarketItemData(p, KitType.FLUIDS, 10, ItemUtils.item(Material.WATER_BUCKET, "§6Kit fluides",
				"§2Caractéristiques :",
				" ",
				"§aCe kit permet à l'§eeau et à la lave", 
				"§ade couler sur vos parcelles,",
				"§aau lieu de rester statique.",
				" ",
				"§7Les machines à lag sont interdites. ", 
				"§7En cas d'abus, les fluides arrêteront",
				"§7de couler sur la parcelle.")));
		
		kits.add(new MarketItemData(p, KitType.PEACEFUL_MOBS, 10, ItemUtils.item(Material.PIG_SPAWN_EGG, "§6Kit animaux",
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
		
		kits.add(new MarketItemData(p, KitType.HOSTILE_MOBS, 10, ItemUtils.item(Material.CREEPER_SPAWN_EGG, "§6Kit monstres",
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

	/**
	 * Set the buy process indicator to deny for 2 seconds
	 */
	private void startBuyDenyTimer(MarketItemData data) {
		setItem(buyProcessItemSlot, ItemUtils.item(data.getHolder().getType(), "§6Achat : " + data.getHolder().getItemMeta().getDisplayName()), null);
		setItem(buyProcessStateSlot, buyProcessDeny, null);
		
		plugin.getTask().runTaskLater(() -> {
			if (buyProcessDeny.equals(inv.getItem(buyProcessStateSlot))) {
				setItem(buyProcessItemSlot, buyProcessNullItem, null);
				setItem(buyProcessStateSlot, buyProcessQuestion, null);	
			}
		}, 40);
	}
	
	
	private void startBuyAcceptTimer(MarketItemData data) {
		useBuyAcceptTimer(data, 2, 2, 15, 
				ItemUtils.item(data.getHolder().getType(), "§6Achat : " + data.getHolder().getItemMeta().getDisplayName()));
	}
	
	private void useBuyAcceptTimer(MarketItemData data, int buyStep, int initBuyStep, int tickInterval, final ItemStack cartItem) {
		
		//cancel achat si le joueur a cliqué sur un autre item
		if (buyStep < 0 || (buyStep != initBuyStep && cartItem != null && !inv.getItem(buyProcessItemSlot).equals(cartItem)))
			return;
		
		if (buyStep == initBuyStep)
			setItem(buyProcessItemSlot, cartItem, null);
		
		
		if (buyStep > 0) {
			ItemStack it = ItemUtils.item(Material.WHITE_STAINED_GLASS_PANE, "§7" + buyStep + "...", "§7L'achat sera possible après la fin du timer.");
			it.setAmount(buyStep);
			setItem(buyProcessStateSlot, it, null);
		}
			
		if (buyStep == 0)
			setItem(buyProcessStateSlot, buyProcessAccept, (it, c, s) -> {
				data.tryToBuy(this);
			});
		
		plugin.getTask().runTaskLater(() -> useBuyAcceptTimer(data, buyStep - 1, initBuyStep, tickInterval, cartItem), tickInterval);
	}

	public static class MarketItemData{
		
		private OlympaPlayerCreatif p;
		private Object toBuy;
		private ItemStack itemHolder;
		private int price;
		private boolean isBuyable = true;
		
		public MarketItemData(OlympaPlayerCreatif p, Object toBuy, int defaultPrice, ItemStack holder){
			this.p = p;
			this.toBuy = toBuy;
			this.itemHolder = holder;
			this.price = defaultPrice;
			
			//Repère les objets non achetables (déjà achetés ou prérequis non validés)
			if (toBuy instanceof OlympaGroup) {
				if (p.getGroups().containsKey((OlympaGroup)toBuy)) {
					itemHolder = addInvisibleEnchant(itemHolder);
					isBuyable = false;
				}
				
				//détection prérequis des grades
				if ((OlympaGroup)toBuy == OlympaGroup.CREA_ARCHITECT && !p.getGroups().containsKey(OlympaGroup.CREA_CONSTRUCTOR))
					isBuyable = false;
				else if ((OlympaGroup)toBuy == OlympaGroup.CREA_CREATOR && !p.getGroups().containsKey(OlympaGroup.CREA_ARCHITECT))
					isBuyable = false;
					
			}else if (toBuy instanceof KitType) {
				if (p.hasKit((KitType) toBuy)) {
					itemHolder = addInvisibleEnchant(itemHolder);
					isBuyable = false;	
				}
				
			//détecte le prochain niveau d'upgrade dispo
			}else if (toBuy instanceof UpgradeType) {
				
				if (((UpgradeType)toBuy).getMaxLevel() > p.getUpgradeLevel((UpgradeType)toBuy))
					this.price = ((UpgradeType)toBuy).getPriceOf(p.getUpgradeLevel((UpgradeType)toBuy));
				else {
					itemHolder = addInvisibleEnchant(itemHolder);
					isBuyable = false;	
				}

				int oldValue = ((UpgradeType)toBuy).getValueOf(p.getUpgradeLevel((UpgradeType)toBuy));
				int newValue = ((UpgradeType)toBuy).getValueOf(p.getUpgradeLevel((UpgradeType)toBuy) + 1);

				if (toBuy == UpgradeType.CB_LEVEL) {
					oldValue *= 20;
					newValue *= 20;
				}

				String oldV = Integer.toString(oldValue);
				String newV = Integer.toString(newValue);
				
				if (oldValue == newValue)
					newV = "maximum atteint";
				
				itemHolder = ItemUtils.loreAdd(itemHolder, " ", "§eAmélioration : " + oldV + " ➔ " + newV);
				
				if (toBuy == UpgradeType.CB_LEVEL && !p.hasKit(KitType.COMMANDBLOCK))
					isBuyable = false;
			}

			itemHolder = ItemUtils.loreAdd(itemHolder, " ", "§ePrix : " + price);
			
			if (!isBuyable)
				itemHolder = ItemUtils.loreAdd(itemHolder, "§7Achat impossible");
			else if (!p.hasGameMoney(price))
				itemHolder = ItemUtils.loreAdd(itemHolder, "§cPas assez de fonds");
			else
				itemHolder = ItemUtils.loreAdd(itemHolder, "§aAchat possible");
				
		}
		
		private ItemStack addInvisibleEnchant(ItemStack item) {
			ItemMeta meta = item.getItemMeta();
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			meta.addEnchant(Enchantment.DURABILITY, 1, true);
			item.setItemMeta(meta);
			return item;
		}
		
		public Object getItem() {
			return toBuy;
		}
		
		public ItemStack getHolder() {
			return itemHolder;
		}
		
		public Integer getPrice() {
			return price;
		}
		
		public boolean isBuyable() {
			return isBuyable;
		}
		
		public void tryToBuy(ShopGui gui) {
			if (!isBuyable || !p.hasGameMoney(price))
				return;
			
			if (toBuy instanceof OlympaGroup) {
				if (p.getGroups().containsKey(toBuy))
					return;
				
				p.withdrawGameMoney(price, () -> {
					p.addGroup((OlympaGroup)toBuy);
					OlympaCore.getInstance().getNameTagApi().callNametagUpdate(p);
					
					OlympaCreatifMain.getInstance().getTask().runTask(() -> new ShopGui(gui).create(p.getPlayer()));
					
					String genreType = p.getGender() == Gender.FEMALE ? "elle" : "lui";
					
					if ((OlympaGroup)toBuy == OlympaGroup.CREA_CREATOR)
						Bukkit.broadcastMessage("§6----------------------------------------------\n§6\n"
								+ "Le joueur §c" + p.getName() + " §6a découvert le grade secret ! \nFélicitations à " + genreType + " !"
								+ "\n§6\n----------------------------------------------");	
				});
				
				/*plugin.getTask().runTaskAsynchronously(()-> {
					OlympaCore.getInstance().getServer().getPluginManager().callEvent(new AsyncOlympaPlayerChangeGroupEvent(p.getPlayer(), ChangeType.ADD, p, (OlympaGroup) toBuy));
					AccountProvider olympaAccount = new AccountProvider(p.getUniqueId());
					olympaAccount.saveToRedis(p);
					//olympaAccount.saveToDb(p);
				});*/
				

			}else if (toBuy instanceof KitType) {
				if (p.hasKit((KitType)toBuy))
					return;

				p.withdrawGameMoney(price, () -> {
					p.addKit((KitType)toBuy);
					OlympaCreatifMain.getInstance().getTask().runTask(() -> new ShopGui(gui).create(p.getPlayer()));
				});
			}else if (toBuy instanceof UpgradeType) {
				if (p.getUpgradeLevel((UpgradeType)toBuy) >= ((UpgradeType)toBuy).getMaxLevel())
					return;

				p.withdrawGameMoney(price, () -> {
					p.incrementUpgradeLevel((UpgradeType)toBuy);
					OlympaCreatifMain.getInstance().getTask().runTask(() -> new ShopGui(gui).create(p.getPlayer()));
				});
			}
			
			OCmsg.SHOP_BUY_SUCCESS.send(p, this);
			
			//new AccountProvider(p.getUniqueId()).saveToDb(p);
		}
	}
}
