package fr.olympa.olympacreatif.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.plugin.OlympaSpigot;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.perks.KitsManager.KitType;
import fr.olympa.olympacreatif.perks.UpgradesManager.UpgradeType;

public class ShopGui extends OlympaGUI{

	private ItemStack ranksRowHead;
	
	private ItemStack buyProcessWaitingItem = ItemUtils.item(Material.BEDROCK, "§7Sélectionnez un objet à acheter");
	private ItemStack buyProcessArrow = ItemUtils.skullCustom(" ", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19");
	private ItemStack buyProcess1 = ItemUtils.skullCustom("§71...", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzFiYzJiY2ZiMmJkMzc1OWU2YjFlODZmYzdhNzk1ODVlMTEyN2RkMzU3ZmMyMDI4OTNmOWRlMjQxYmM5ZTUzMCJ9fX0=");
	private ItemStack buyProcess2 = ItemUtils.skullCustom("§72...", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNkOWVlZWU4ODM0Njg4ODFkODM4NDhhNDZiZjMwMTI0ODVjMjNmNzU3NTNiOGZiZTg0ODczNDE0MTk4NDcifX19");
	private ItemStack buyProcess3 = ItemUtils.skullCustom("§73...", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWQ0ZWFlMTM5MzM4NjBhNmRmNWU4ZTk1NTY5M2I5NWE4YzNiMTVjMzZiOGI1ODc1MzJhYzA5OTZiYzM3ZTUifX19");
	private ItemStack buyProcessQuestion = ItemUtils.skullCustom("§7En attente...", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmFkYzA0OGE3Y2U3OGY3ZGFkNzJhMDdkYTI3ZDg1YzA5MTY4ODFlNTUyMmVlZWQxZTNkYWYyMTdhMzhjMWEifX19");
	private ItemStack buyProcessAccept = ItemUtils.skullCustom("§aCliquez pour acheter", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzQwNjNiYTViMTZiNzAzMGEyMGNlNmYwZWE5NmRjZDI0YjA2NDgzNmY1NzA0NTZjZGJmYzllODYxYTc1ODVhNSJ9fX0=");
	private ItemStack buyProcessDeny = ItemUtils.skullCustom("§cAchat impossible", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzIwZWYwNmRkNjA0OTk3NjZhYzhjZTE1ZDJiZWE0MWQyODEzZmU1NTcxODg2NGI1MmRjNDFjYmFhZTFlYTkxMyJ9fX0=");
	
	/*
	private Map<ItemStack, OlympaGroup> ranks = ImmutableMap.<ItemStack, OlympaGroup>builder()
			.put(ItemUtils.skullCustom("§6Grades", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWNhM2YzMjRiZWVlZmI2YTBlMmM1YjNjNDZhYmM5MWNhOTFjMTRlYmE0MTlmYTQ3NjhhYzMwMjNkYmI0YjIifX19"), null)
			.put(ItemUtils.item(Material.IRON_PICKAXE, "§6Grade §e" + OlympaGroup.CREA_CONSTRUCTOR, "descriptions à faire"), OlympaGroup.CREA_CONSTRUCTOR)
			.put(ItemUtils.item(Material.IRON_PICKAXE, "§6Grade §e" + OlympaGroup.CREA_ARCHITECT), OlympaGroup.CREA_ARCHITECT)
			.put(ItemUtils.item(Material.IRON_PICKAXE, "§6Grade §e" + OlympaGroup.CREA_CREATOR), OlympaGroup.CREA_CREATOR)
			.build();
	
	private List<ItemStack> kits = ImmutableList.<ItemStack>builder()
			.add(ItemUtils.skullCustom("§6Kits", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTQ2MWIzOGM4ZTQ1NzgyYWRhNTlkMTYxMzJhNDIyMmMxOTM3NzhlN2Q3MGM0NTQyYzk1MzYzNzZmMzdiZTQyIn19fQ=="))
			.add(ItemUtils.item(Material.COMMAND_BLOCK, "§6Kit §e" + KitType.COMMANDBLOCK.getName()))
			.add(ItemUtils.item(Material.REDSTONE_TORCH, "§6Kit §e" + KitType.REDSTONE.getName()))
			.add(ItemUtils.item(Material.WATER_BUCKET, "§6Kit §e" + KitType.FLUIDS.getName()))
			.add(ItemUtils.item(Material.PIG_SPAWN_EGG, "§6Kit §e" + KitType.PEACEFUL_MOBS.getName()))
			.add(ItemUtils.item(Material.CREEPER_SPAWN_EGG, "§6Kit §e" + KitType.HOSTILE_MOBS.getName()))
			.build();
	
	private List<ItemStack> upgrades = ImmutableList.<ItemStack>builder()
			.add(ItemUtils.skullCustom("§6Améliorations", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTY3ZDgxM2FlN2ZmZTViZTk1MWE0ZjQxZjJhYTYxOWE1ZTM4OTRlODVlYTVkNDk4NmY4NDk0OWM2M2Q3NjcyZSJ9fX0="))
			.add(ItemUtils.item(Material.GRASS_BLOCK, "§6Amélioration §enombre de parcelles"))
			.add(ItemUtils.item(Material.ACACIA_DOOR, "§6Amélioration §enombre de membres par parcelle"))
			.add(ItemUtils.item(Material.REPEATING_COMMAND_BLOCK, "§6Amélioration §enombre de cps pour les commandblocks"))
			.build();
	*/
	
	private MarketItemData itemReadyToBuy = null;
	private boolean readyToBuy = false;
	
	OlympaCreatifMain plugin;
	OlympaPlayerCreatif p;
	
	int firstRankPrice = 10;
	int secondRankPrice = 20;
	
	private List<MarketItemData> ranks = new ArrayList<MarketItemData>();
			
	
	public ShopGui(OlympaCreatifMain plugin, Player player) {
		super("Magasin (monnaie : " + ((OlympaPlayerCreatif)AccountProvider.get(player.getUniqueId())).getGameMoney() + " bellis)", 4);

		inv.setItem(inv.getSize() - 1, MainGui.getBackItem());
		
		this.plugin = plugin;
		p = AccountProvider.get(player.getUniqueId());

		//init têtes
		ranksRowHead = ItemUtils.skullCustom("§6Grades", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWNhM2YzMjRiZWVlZmI2YTBlMmM1YjNjNDZhYmM5MWNhOTFjMTRlYmE0MTlmYTQ3NjhhYzMwMjNkYmI0YjIifX19");
		
		//init rangs
		ranks.add(new MarketItemData(p, OlympaGroup.CREA_CONSTRUCTOR, 10, ItemUtils.item(Material.IRON_PICKAXE, "§6Grade " + OlympaGroup.CREA_CONSTRUCTOR.getName(p.getGender()), "descriptions à faire")));
		ranks.add(new MarketItemData(p, OlympaGroup.CREA_ARCHITECT, 20, ItemUtils.item(Material.GOLDEN_PICKAXE, "§6Grade " + OlympaGroup.CREA_ARCHITECT.getName(p.getGender()), "descriptions à faire")));
		ranks.add(new MarketItemData(p, OlympaGroup.CREA_CREATOR, 30, ItemUtils.item(Material.DIAMOND_PICKAXE, "§6Grade " + OlympaGroup.CREA_CREATOR.getName(p.getGender()), "descriptions à faire")));
		
		//CREATION GUI
		int i = 0;
		
		inv.setItem(inv.getSize() - 5, buyProcessWaitingItem);
		inv.setItem(inv.getSize() - 4, buyProcessArrow);
		inv.setItem(inv.getSize() - 3, buyProcessQuestion);
		
		//ajout grades
		inv.setItem(i, ranksRowHead);
		i++;
		
		for (MarketItemData e : ranks) {
			inv.setItem(i, e.getHolder());
			i++;
		}
	}

	
	@Override
	public boolean onClick(Player player, ItemStack current, int slot, ClickType click) {
		if (inv.getSize() - 1 == slot) {
			MainGui.openMainGui(player);
			return true;
		}
		
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
		}

		//tentative d'achat
		if (slot == inv.getSize() - 3) {
			if (readyToBuy && itemReadyToBuy != null) 
				itemReadyToBuy.tryToItem();
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
		
		switch (buyStep) {
		case 3:
			itemReadyToBuy = data;
			
			inv.setItem(inv.getSize() - 5, ItemUtils.item(data.getHolder().getType(), "§dAchat de : " + data.getHolder().getItemMeta().getDisplayName().toLowerCase()));
			inv.setItem(inv.getSize() - 3, buyProcess3);
			plugin.getTask().runTaskLater(() -> useBuyAcceptTimer(data, 2), 20);
			break;
		case 2:
			inv.setItem(inv.getSize() - 3, buyProcess2);
			plugin.getTask().runTaskLater(() -> useBuyAcceptTimer(data, 1), 20);
			break;
		case 1:
			inv.setItem(inv.getSize() - 3, buyProcess1);
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
		private ItemStack itemHolderCompressed;
		private int price;
		private boolean isBuyable = true;
		
		public MarketItemData(OlympaPlayerCreatif p, Object toBuy, int price, ItemStack holder){
			this.p = p;
			this.itemHolderCompressed = holder;
			this.toBuy = toBuy;
			this.itemHolder = holder;
			this.price = price;

			//Bukkit.broadcastMessage("joueur : " + this.p);
			//Bukkit.broadcastMessage("groupes : " + this.p.getGroups());
			
			//Repère les objets non achetables (déjà achetés ou prérequis non validés)
			if (toBuy instanceof OlympaGroup) {
				if (p.getGroups().containsKey((OlympaGroup)toBuy))
					isBuyable = false;	
				
				//détection prérequis des grades
				if ((OlympaGroup)toBuy == OlympaGroup.CREA_ARCHITECT && !p.getGroups().containsKey(OlympaGroup.CREA_CONSTRUCTOR))
					isBuyable = false;
				else if ((OlympaGroup)toBuy == OlympaGroup.CREA_CREATOR && !p.getGroups().containsKey(OlympaGroup.CREA_ARCHITECT))
					isBuyable = false;
					
			}else if (toBuy instanceof KitType) {
				if (p.hasKit((KitType) toBuy))
					isBuyable = false;
				
			//détecte le prochain niveau d'upgrade dispo
			}else if (toBuy instanceof UpgradeType) {
					if (((UpgradeType)toBuy).getMaxLevel() > p.getUpgradeLevel((UpgradeType)toBuy))
						price = ((UpgradeType)toBuy).getPriceOf(p.getUpgradeLevel((UpgradeType)toBuy));
					else
						isBuyable = false;
					
					if (isBuyable)
						itemHolder = ItemUtils.loreAdd(itemHolder, " ", " §6Amélioration : " + 
								((UpgradeType)toBuy).getValueOf(p.getUpgradeLevel((UpgradeType)toBuy)) + " ▶ " + 
								((UpgradeType)toBuy).getValueOf(p.getUpgradeLevel((UpgradeType)toBuy) + 1));
			}

			itemHolder = ItemUtils.loreAdd(itemHolder, " ", "§ePrix : " + price);
			
			if (!isBuyable)
				itemHolder = ItemUtils.loreAdd(itemHolder, "§7Achat indisponible");
			else if (price > p.getGameMoney())
				itemHolder = ItemUtils.loreAdd(itemHolder, "§cFonds insuffisants");
			else
				itemHolder = ItemUtils.loreAdd(itemHolder, "§aAchat possible");
				
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
		
		public void tryToItem() {
			if (!isBuyable || p.getGameMoney() < price)
				return;
			
			if (toBuy instanceof OlympaGroup) {
				if (p.getGroups().containsKey(toBuy)) 
					return;
				
				p.removeGameMoney(price);
				p.removeGroup((OlympaGroup)toBuy);
				
				p.getPlayer().sendMessage("Acquisition : grade " + toBuy.toString());
			}
			
			new ShopGui(plugin, p.getPlayer()).create(p.getPlayer());
			new AccountProvider(p.getUniqueId()).saveToDb(p);
		}

		private boolean hasRequiredGroup(OlympaGroup group) {
			if (p.getGroups().containsKey(group))
				return true;
			
			p.getPlayer().sendMessage(Message.SHOP_ERR_PREVIOUS_RANK_NEEDED.getValue());
			return false;
		}
	}
	
	/*
	public static boolean hasPlayerAlreadyBought(OlympaPlayerCreatif p, Object obj, int... level) {
		if (obj instanceof OlympaGroup) {
			if (p.getGroups().containsKey(obj))
				return true;
			
		}else if (obj instanceof KitType) {
			if (p.hasKit((KitType)obj))
				return true;
			
		}else if (obj instanceof UpgradeType && level.length > 0)
			if (p.getUpgradeLevel((UpgradeType)obj) >= level[0])
				return true;
		
		return false;
	}
	*/
}
