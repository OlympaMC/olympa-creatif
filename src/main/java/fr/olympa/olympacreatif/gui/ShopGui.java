package fr.olympa.olympacreatif.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent;
import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent.ChangeType;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.player.Gender;
import fr.olympa.api.plugin.OlympaSpigot;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.redis.RedisSpigotSend;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.perks.KitsManager.KitType;
import fr.olympa.olympacreatif.perks.UpgradesManager.UpgradeType;

public class ShopGui extends IGui{

	private ItemStack ranksRowHead;
	private ItemStack kitsRowHead;
	private ItemStack upgradesRowHead;
	
	private ItemStack buyProcessWaitingItem = ItemUtils.item(Material.BEDROCK, "§7Sélectionnez un objet à acheter");
	private ItemStack buyProcessArrow = ItemUtils.skullCustom(" ", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19");
	private ItemStack buyProcess1 = ItemUtils.skullCustom("§71...", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzFiYzJiY2ZiMmJkMzc1OWU2YjFlODZmYzdhNzk1ODVlMTEyN2RkMzU3ZmMyMDI4OTNmOWRlMjQxYmM5ZTUzMCJ9fX0=");
	private ItemStack buyProcess2 = ItemUtils.skullCustom("§72...", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNkOWVlZWU4ODM0Njg4ODFkODM4NDhhNDZiZjMwMTI0ODVjMjNmNzU3NTNiOGZiZTg0ODczNDE0MTk4NDcifX19");
	private ItemStack buyProcess3 = ItemUtils.skullCustom("§73...", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWQ0ZWFlMTM5MzM4NjBhNmRmNWU4ZTk1NTY5M2I5NWE4YzNiMTVjMzZiOGI1ODc1MzJhYzA5OTZiYzM3ZTUifX19");
	private ItemStack buyProcessQuestion = ItemUtils.skullCustom("§7En attente...", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmFkYzA0OGE3Y2U3OGY3ZGFkNzJhMDdkYTI3ZDg1YzA5MTY4ODFlNTUyMmVlZWQxZTNkYWYyMTdhMzhjMWEifX19");
	private ItemStack buyProcessAccept = ItemUtils.skullCustom("§aCliquez pour acheter", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzQwNjNiYTViMTZiNzAzMGEyMGNlNmYwZWE5NmRjZDI0YjA2NDgzNmY1NzA0NTZjZGJmYzllODYxYTc1ODVhNSJ9fX0=");
	private ItemStack buyProcessDeny = ItemUtils.skullCustom("§cAchat impossible", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzIwZWYwNmRkNjA0OTk3NjZhYzhjZTE1ZDJiZWE0MWQyODEzZmU1NTcxODg2NGI1MmRjNDFjYmFhZTFlYTkxMyJ9fX0=");
	
	private MarketItemData itemReadyToBuy = null;
	private boolean readyToBuy = false;
	
	int firstRankPrice = 10;
	int secondRankPrice = 20;

	private List<MarketItemData> ranks = new ArrayList<MarketItemData>();
	private List<MarketItemData> kits = new ArrayList<MarketItemData>();
	private List<MarketItemData> upgrades = new ArrayList<MarketItemData>();
			
	
	public ShopGui(IGui gui) {
		super(gui, "Magasin (monnaie : " + gui.getPlayer().getGameMoney() + " " + gui.getPlayer().getGameMoneyName() + ")", 4);

		//init têtes
		ranksRowHead = ItemUtils.skullCustom("§6Grades", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWI1NzViNTU3N2NjYjMyZTQyZDU0MzA0YTFlZjVmMjNhZDZiYWQ1YTM0NTYzNDBhNDkxMmE2MmIzNzk3YmI1In19fQ==");
		kitsRowHead = ItemUtils.skullCustom("§6Kits", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjg4MjRkY2Y0YmEzMTc1MzNiZjI5ZGNhMThjZTdjNGZkMzI4YjQyNjgwZTZjMzIyZjVmNGZmMWEzOTRhODg3In19fQ==");
		upgradesRowHead = ItemUtils.skullCustom("§6Améliorations", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODA3M2FlNTQ3ZTZkYWE5ZDJkYzhjYjkwZTc4ZGQxYzcxY2RmYWRiNzQwMWRjMTY3ZDE2ODE5YjE3MzI4M2M1MSJ9fX0=");
		
		//init rangs
		ranks.add(new MarketItemData(p, OlympaGroup.CREA_CONSTRUCTOR, 10, ItemUtils.item(Material.IRON_PICKAXE, "§6Grade " + OlympaGroup.CREA_CONSTRUCTOR.getName(p.getGender()), "descriptions à faire")));
		ranks.add(new MarketItemData(p, OlympaGroup.CREA_ARCHITECT, 20, ItemUtils.item(Material.GOLDEN_PICKAXE, "§6Grade " + OlympaGroup.CREA_ARCHITECT.getName(p.getGender()), "descriptions à faire")));
		
		//ajout du grade créateur si les prérequis sont respectés
		boolean hasAllKits = true;
		for (KitType kit : KitType.values())
			if (!p.hasKit(kit) && kit != KitType.ADMIN)
				hasAllKits = false;
		if ((p.getGroups().containsKey(OlympaGroup.CREA_ARCHITECT) || p.getGroups().containsKey(OlympaGroup.CREA_CREATOR)) && hasAllKits)
			ranks.add(new MarketItemData(p, OlympaGroup.CREA_CREATOR, 30, ItemUtils.item(Material.DIAMOND_PICKAXE, "§6Grade " + OlympaGroup.CREA_CREATOR.getName(p.getGender()), "descriptions à faire")));

		kits.add(new MarketItemData(p, KitType.COMMANDBLOCK, 10, ItemUtils.item(Material.COMMAND_BLOCK, "§6Kit commandblocks")));
		kits.add(new MarketItemData(p, KitType.REDSTONE, 10, ItemUtils.item(Material.REDSTONE_TORCH, "§6Kit redstone")));
		kits.add(new MarketItemData(p, KitType.FLUIDS, 10, ItemUtils.item(Material.WATER_BUCKET, "§6Kit fluides")));
		kits.add(new MarketItemData(p, KitType.PEACEFUL_MOBS, 10, ItemUtils.item(Material.PIG_SPAWN_EGG, "§6Kit animaux")));
		kits.add(new MarketItemData(p, KitType.HOSTILE_MOBS, 10, ItemUtils.item(Material.CREEPER_SPAWN_EGG, "§6Kit monstres")));

		upgrades.add(new MarketItemData(p, UpgradeType.BONUS_PLOTS_LEVEL, 0, ItemUtils.item(Material.GRASS_BLOCK, "§6Augmentation du nombre de parcelles")));
		upgrades.add(new MarketItemData(p, UpgradeType.BONUS_MEMBERS_LEVEL, 0, ItemUtils.item(Material.ACACIA_DOOR, "§6Augmentation nombre membres par parcelle")));
		upgrades.add(new MarketItemData(p, UpgradeType.CB_LEVEL, 0, ItemUtils.item(Material.REPEATING_COMMAND_BLOCK, "§6Augmentation CPS commandblocks")));
		
		//CREATION GUI
		
		inv.setItem(inv.getSize() - 5, buyProcessWaitingItem);
		inv.setItem(inv.getSize() - 4, buyProcessArrow);
		inv.setItem(inv.getSize() - 3, buyProcessQuestion);

		//ajout grades
		int i = 0;
		
		inv.setItem(i, ranksRowHead);
		i++;
		for (MarketItemData e : ranks) {
			inv.setItem(i, e.getHolder());
			i++;
		}
		
		//ajout kits
		i = 9;
		
		inv.setItem(i, kitsRowHead);
		i++;
		for (MarketItemData e : kits) {
			inv.setItem(i, e.getHolder());
			i++;
		}
		
		//ajout upgrades
		i = 18;
		
		inv.setItem(i, upgradesRowHead);
		i++;
		for (MarketItemData e : upgrades) {
			inv.setItem(i, e.getHolder());
			i++;
		}
	}

	
	@Override
	public boolean onClick(Player player, ItemStack current, int slot, ClickType click) {
		super.onClick(player, current, slot, click);
		
		int row = slot / 9;
		int column = slot % 9;
		
		if (column == 0)
			return true;
		
		MarketItemData askedItem = null;
		
		//get asked item
		switch(row) {
		case 0:
			if (column <= ranks.size())
				askedItem = ranks.get(column - 1);
			break;
		case 1:
			if (column <= kits.size())
				askedItem = kits.get(column - 1);
			break;
		case 2:
			if (column <= upgrades.size())
				askedItem = upgrades.get(column - 1);
			break;
		}

		//tentative d'achat
		if (slot == inv.getSize() - 3) {
			if (readyToBuy && itemReadyToBuy != null) 
				itemReadyToBuy.tryToItem(this);
			return true;
		}
		
		//lancement du timer pour l'objet désigné
		if (askedItem != null)
			if (askedItem.getPrice() == null || askedItem.getPrice() > p.getGameMoney() || !askedItem.isBuyable())
				startBuyDeniedTimer(askedItem);
			else
				startBuyAcceptTimer(askedItem);
		
		return true;
	}
	
	private void startBuyDeniedTimer(MarketItemData data) {
		
		readyToBuy = false;
		
		itemReadyToBuy = data;
		
		inv.setItem(inv.getSize() - 5, ItemUtils.item(data.getHolder().getType(), "§dAchat de : " + data.getHolder().getItemMeta().getDisplayName().toLowerCase()));
		inv.setItem(inv.getSize() - 3, buyProcessDeny);
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				if (data.equals(itemReadyToBuy)) {
					inv.setItem(inv.getSize() - 5, buyProcessWaitingItem);
					inv.setItem(inv.getSize() - 4, buyProcessArrow);
					inv.setItem(inv.getSize() - 3, buyProcessQuestion);	
				}
			}
		}.runTaskLater(plugin, 40);
	}
	
	private void startBuyAcceptTimer(MarketItemData data) {
		useBuyAcceptTimer(data, 3);
	}
	
	private void useBuyAcceptTimer(MarketItemData data, int buyStep) {
		
		readyToBuy = false;
		
		if (!data.equals(itemReadyToBuy) && buyStep != 3)
			return;
		
		ItemStack item;
		
		switch (buyStep) {
		case 3:
			itemReadyToBuy = data;
			
			inv.setItem(inv.getSize() - 5, ItemUtils.item(data.getHolder().getType(), "§dAchat de : " + data.getHolder().getItemMeta().getDisplayName().toLowerCase()));
			//inv.setItem(inv.getSize() - 3, buyProcess3);
			
			item = ItemUtils.item(Material.WHITE_STAINED_GLASS_PANE, "§73...");
			item.setAmount(3);
			inv.setItem(inv.getSize() - 3, item);
			
			plugin.getTask().runTaskLater(() -> useBuyAcceptTimer(data, 2), 20);
			break;
		case 2:
			//inv.setItem(inv.getSize() - 3, buyProcess2);
			
			item = ItemUtils.item(Material.WHITE_STAINED_GLASS_PANE, "§72...");
			item.setAmount(2);
			inv.setItem(inv.getSize() - 3, item);
			
			plugin.getTask().runTaskLater(() -> useBuyAcceptTimer(data, 1), 20);
			break;
		case 1:
			//inv.setItem(inv.getSize() - 3, buyProcess1);
			
			inv.setItem(inv.getSize() - 3, ItemUtils.item(Material.WHITE_STAINED_GLASS_PANE, "§71..."));
			plugin.getTask().runTaskLater(() -> useBuyAcceptTimer(data, 0), 20);
			break;
		case 0:
			inv.setItem(inv.getSize() - 3, buyProcessAccept);
			readyToBuy = true;
			break;
		}
	}

	public class MarketItemData{
		
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
				//Bukkit.broadcastMessage("upgrade : " + toBuy + " - player lvl : " + p.getUpgradeLevel((UpgradeType)toBuy));
				
					if (((UpgradeType)toBuy).getMaxLevel() > p.getUpgradeLevel((UpgradeType)toBuy))
						this.price = ((UpgradeType)toBuy).getPriceOf(p.getUpgradeLevel((UpgradeType)toBuy));
					else {
						itemHolder = addInvisibleEnchant(itemHolder);
						isBuyable = false;	
					}
					
					if (isBuyable)
						itemHolder = ItemUtils.loreAdd(itemHolder, " ", "§eAmélioration : " + 
								((UpgradeType)toBuy).getValueOf(p.getUpgradeLevel((UpgradeType)toBuy)) + " ➔ " + 
								((UpgradeType)toBuy).getValueOf(p.getUpgradeLevel((UpgradeType)toBuy) + 1));
					else
						itemHolder = ItemUtils.loreAdd(itemHolder, " ", "§eAmélioration : " + 
								((UpgradeType)toBuy).getValueOf(p.getUpgradeLevel((UpgradeType)toBuy)) + " ➔ §7maximum atteint");
			}

			itemHolder = ItemUtils.loreAdd(itemHolder, " ", "§ePrix : " + price);
			
			if (!isBuyable)
				itemHolder = ItemUtils.loreAdd(itemHolder, "§7Achat impossible");
			else if (price > p.getGameMoney())
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
		
		/*public ItemStack getCompressedHolder() {
			return itemHolderCompressed;
		}*/
		
		public Integer getPrice() {
			return price;
		}
		
		public boolean isBuyable() {
			return isBuyable;
		}
		
		public void tryToItem(ShopGui gui) {
			if (!isBuyable || p.getGameMoney() < price)
				return;
			
			if (toBuy instanceof OlympaGroup) {
				if (p.getGroups().containsKey(toBuy)) 
					return;
				
				p.removeGameMoney(price);
				p.addGroup((OlympaGroup)toBuy);
				
				plugin.getTask().runTaskAsynchronously(()-> {
					OlympaCore.getInstance().getServer().getPluginManager().callEvent(new AsyncOlympaPlayerChangeGroupEvent(p.getPlayer(), ChangeType.ADD, p, (OlympaGroup) toBuy));
					AccountProvider olympaAccount = new AccountProvider(p.getUniqueId());
					olympaAccount.saveToRedis(p);
					olympaAccount.saveToDb(p);
				});
				
				String genreType = p.getGender() == Gender.FEMALE ? "elle" : "lui";
				
				if ((OlympaGroup)toBuy == OlympaGroup.CREA_CREATOR)
					Bukkit.broadcastMessage("§6----------------------------------------------\n§6\n"
							+ "Le joueur §c" + p.getName() + " §6a découvert le grade secret ! \nFélicitations à " + genreType + " !"
							+ "\n§6\n----------------------------------------------");

			}else if (toBuy instanceof KitType) {
				if (p.hasKit((KitType)toBuy))
					return;

				p.removeGameMoney(price);
				p.addKit((KitType)toBuy);
			}else if (toBuy instanceof UpgradeType) {
				if (p.getUpgradeLevel((UpgradeType)toBuy) >= ((UpgradeType)toBuy).getMaxLevel())
					return;

				p.removeGameMoney(price);
				p.incrementUpgradeLevel((UpgradeType)toBuy);
			}
			
			p.getPlayer().sendMessage(Message.SHOP_BUY_SUCCESS.getValue(itemHolder.getItemMeta().getDisplayName().toLowerCase()));
			new ShopGui(gui).create(p.getPlayer());
			new AccountProvider(p.getUniqueId()).saveToDb(p);
		}
	}
}
