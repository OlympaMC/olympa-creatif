package fr.olympa.olympacreatif.gui;

import java.util.List;
import java.util.Map;
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
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.perks.KitsManager.KitType;

public class ShopGui extends OlympaGUI{

	private static ItemStack ranksRowHead = ItemUtils.skullCustom("§6Grades", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWNhM2YzMjRiZWVlZmI2YTBlMmM1YjNjNDZhYmM5MWNhOTFjMTRlYmE0MTlmYTQ3NjhhYzMwMjNkYmI0YjIifX19");
	
	private static ItemStack buyProcessWaitingItem = ItemUtils.item(Material.BEDROCK, "§7En attente...", "§7Sélectionnez un objet à acheter");
	private static ItemStack buyProcessArrow = ItemUtils.skullCustom(" ", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19");
	private static ItemStack buyProcess1 = ItemUtils.skullCustom("§71...", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzFiYzJiY2ZiMmJkMzc1OWU2YjFlODZmYzdhNzk1ODVlMTEyN2RkMzU3ZmMyMDI4OTNmOWRlMjQxYmM5ZTUzMCJ9fX0=");
	private static ItemStack buyProcess2 = ItemUtils.skullCustom("§72...", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNkOWVlZWU4ODM0Njg4ODFkODM4NDhhNDZiZjMwMTI0ODVjMjNmNzU3NTNiOGZiZTg0ODczNDE0MTk4NDcifX19");
	private static ItemStack buyProcess3 = ItemUtils.skullCustom("§73...", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWQ0ZWFlMTM5MzM4NjBhNmRmNWU4ZTk1NTY5M2I5NWE4YzNiMTVjMzZiOGI1ODc1MzJhYzA5OTZiYzM3ZTUifX19");
	private static ItemStack buyProcessQuestion = ItemUtils.skullCustom("§7En attente...", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmFkYzA0OGE3Y2U3OGY3ZGFkNzJhMDdkYTI3ZDg1YzA5MTY4ODFlNTUyMmVlZWQxZTNkYWYyMTdhMzhjMWEifX19");
	private static ItemStack buyProcessAccept = ItemUtils.skullCustom("§aCliquez pour acheter", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzQwNjNiYTViMTZiNzAzMGEyMGNlNmYwZWE5NmRjZDI0YjA2NDgzNmY1NzA0NTZjZGJmYzllODYxYTc1ODVhNSJ9fX0=");
	private static ItemStack buyProcessDeny = ItemUtils.skullCustom("§cPas assez de fonds", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzIwZWYwNmRkNjA0OTk3NjZhYzhjZTE1ZDJiZWE0MWQyODEzZmU1NTcxODg2NGI1MmRjNDFjYmFhZTFlYTkxMyJ9fX0=");
	
	private List<MarketItemData> rankss = ImmutableList.<MarketItemData>builder()
			.add(new MarketItemData(OlympaGroup.CREA_CONSTRUCTOR, 10, ItemUtils.item(Material.IRON_PICKAXE, "§6Grade §e" + OlympaGroup.CREA_CONSTRUCTOR, "descriptions à faire")))
			.add(new MarketItemData(OlympaGroup.CREA_ARCHITECT, 20, ItemUtils.item(Material.GOLDEN_PICKAXE, "§6Grade §e" + OlympaGroup.CREA_ARCHITECT, "descriptions à faire")))
			.add(new MarketItemData(OlympaGroup.CREA_CREATOR, 30, ItemUtils.item(Material.DIAMOND_PICKAXE, "§6Grade §e" + OlympaGroup.CREA_CREATOR, "descriptions à faire")))
			.build();
	
	public static Map<ItemStack, OlympaGroup> ranks = ImmutableMap.<ItemStack, OlympaGroup>builder()
			.put(ItemUtils.skullCustom("§6Grades", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWNhM2YzMjRiZWVlZmI2YTBlMmM1YjNjNDZhYmM5MWNhOTFjMTRlYmE0MTlmYTQ3NjhhYzMwMjNkYmI0YjIifX19"), null)
			.put(ItemUtils.item(Material.IRON_PICKAXE, "§6Grade §e" + OlympaGroup.CREA_CONSTRUCTOR, "descriptions à faire"), OlympaGroup.CREA_CONSTRUCTOR)
			.put(ItemUtils.item(Material.IRON_PICKAXE, "§6Grade §e" + OlympaGroup.CREA_ARCHITECT), OlympaGroup.CREA_ARCHITECT)
			.put(ItemUtils.item(Material.IRON_PICKAXE, "§6Grade §e" + OlympaGroup.CREA_CREATOR), OlympaGroup.CREA_CREATOR)
			.build();
	
	public static List<ItemStack> kits = ImmutableList.<ItemStack>builder()
			.add(ItemUtils.skullCustom("§6Kits", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTQ2MWIzOGM4ZTQ1NzgyYWRhNTlkMTYxMzJhNDIyMmMxOTM3NzhlN2Q3MGM0NTQyYzk1MzYzNzZmMzdiZTQyIn19fQ=="))
			.add(ItemUtils.item(Material.COMMAND_BLOCK, "§6Kit §e" + KitType.COMMANDBLOCK.getName()))
			.add(ItemUtils.item(Material.REDSTONE_TORCH, "§6Kit §e" + KitType.REDSTONE.getName()))
			.add(ItemUtils.item(Material.WATER_BUCKET, "§6Kit §e" + KitType.FLUIDS.getName()))
			.add(ItemUtils.item(Material.PIG_SPAWN_EGG, "§6Kit §e" + KitType.PEACEFUL_MOBS.getName()))
			.add(ItemUtils.item(Material.CREEPER_SPAWN_EGG, "§6Kit §e" + KitType.HOSTILE_MOBS.getName()))
			.build();
	
	public static List<ItemStack> upgrades = ImmutableList.<ItemStack>builder()
			.add(ItemUtils.skullCustom("§6Améliorations", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTY3ZDgxM2FlN2ZmZTViZTk1MWE0ZjQxZjJhYTYxOWE1ZTM4OTRlODVlYTVkNDk4NmY4NDk0OWM2M2Q3NjcyZSJ9fX0="))
			.add(ItemUtils.item(Material.GRASS_BLOCK, "§6Amélioration §enombre de parcelles"))
			.add(ItemUtils.item(Material.ACACIA_DOOR, "§6Amélioration §enombre de membres par parcelle"))
			.add(ItemUtils.item(Material.REPEATING_COMMAND_BLOCK, "§6Amélioration §enombre de cps pour les commandblocks"))
			.build();
	
	private MarketItemData itemReadyToBuy = null;
	private boolean isReadyToBuy = true;
	
	OlympaCreatifMain plugin;
	OlympaPlayerCreatif p;
	
	int firstRankPrice = 10;
	int secondRankPrice = 20;
	
	public ShopGui(OlympaCreatifMain plugin, Player player) {
		super("Magasin (monnaie : " + ((OlympaPlayerCreatif)AccountProvider.get(player.getUniqueId())).getGameMoney() + " bellis)", 4);

		inv.setItem(inv.getSize() - 1, MainGui.getBackItem());
		
		this.plugin = plugin;
		p = AccountProvider.get(player.getUniqueId());
		
		int i = 0;
		
		//CREATION GUI
		inv.setItem(inv.getSize() - 5, buyProcessWaitingItem);
		inv.setItem(inv.getSize() - 4, buyProcessArrow);
		inv.setItem(inv.getSize() - 3, buyProcessQuestion);
		
		//ajout grades
		inv.setItem(i, ranksRowHead);
		i++;
		
		for (MarketItemData e : rankss) {
			inv.setItem(i, e.getHolder());
			i++;
		}
	}

	
	@Override
	public boolean onClick(Player player, ItemStack current, int slot, ClickType click) {
		int row = slot / 9;
		int column = slot % 9;
		
		if (column == 0)
			return true;
		
		MarketItemData askedItem = null;
		
		//get asked item
		switch(row) {
		case 0:
			if (column <= rankss.size())
				askedItem = rankss.get(column - 1);
			break;
		}
		
		if (askedItem == null)
			return true;
		
		if (askedItem.getPrice() > p.getGameMoney())
			startBuyDeniedTimer(askedItem);
		else
			startBuyAcceptTimer(askedItem);
		
		//si tentative d'achat
		if (slot == inv.getSize() - 3)
			if (isReadyToBuy && itemReadyToBuy != null && p.getGameMoney() >= itemReadyToBuy.getPrice()) 
				itemReadyToBuy.buyItem();
		
		return true;
	}
	
	private void startBuyDeniedTimer(MarketItemData data) {
		
		itemReadyToBuy = null;
		
		inv.setItem(inv.getSize() - 5, ItemUtils.item(data.getHolder().getType(), "§dAchat de : " + data.getHolder().getItemMeta().getDisplayName().toLowerCase()));
		inv.setItem(inv.getSize() - 3, buyProcessDeny);
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				if (itemReadyToBuy == null) {
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
		if (buyStep == 3) {
			
			itemReadyToBuy = data;
			
			inv.setItem(inv.getSize() - 5, ItemUtils.item(data.getHolder().getType(), "§dAchat de : " + data.getHolder().getItemMeta().getDisplayName().toLowerCase()));
			inv.setItem(inv.getSize() - 3, buyProcess3);
			
			plugin.getTask().runTaskLater(() -> useBuyAcceptTimer(data, 2), 20);
			
		}else if (data.equals(itemReadyToBuy)) {
			switch (buyStep) {
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
				isReadyToBuy = true;
				break;
			}
		}
	}

	public class MarketItemData{
		
		private Object toBuy;
		private ItemStack itemHolder;
		private int price;
		
		public MarketItemData(Object toBuy, int price, ItemStack holder){
			this.toBuy = toBuy;
			this.itemHolder = holder;
			this.price = price;
		}
		
		public Object getItem() {
			return toBuy;
		}
		
		public ItemStack getHolder() {
			return itemHolder;
		}
		
		public int getPrice() {
			return price;
		}
		
		@SuppressWarnings("incomplete-switch")
		public void buyItem() {
			if (p.getGameMoney() < price)
				return;
			
			if (toBuy instanceof OlympaGroup) {
				if (p.getGroups().containsKey(toBuy)) 
					return;
				
				switch((OlympaGroup)toBuy) {
				case CREA_ARCHITECT:
					if (hasRequiredGroup(OlympaGroup.CREA_CONSTRUCTOR))
						return;
				case CREA_CREATOR:
					if (hasRequiredGroup(OlympaGroup.CREA_ARCHITECT))
						return;
				}
				
				p.removeGameMoney(price);
				p.addGroup((OlympaGroup)toBuy);
				
				p.getPlayer().sendMessage("Acquisition : grade " + toBuy.toString());
			}
		}

		private boolean hasRequiredGroup(OlympaGroup group) {
			if (p.getGroups().containsKey(group))
				return true;
			
			p.getPlayer().sendMessage(Message.SHOP_ERR_PREVIOUS_RANK_NEEDED.getValue());
			return false;
		}
	}
}
