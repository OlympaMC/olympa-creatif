package fr.olympa.olympacreatif.perks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public class ArmorStandManager {

	public final ItemStack headX = ItemUtils.skullCustom("§6Rotation selon l'axe X", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmNjNWZhZTNiNjUwZTZlZGIzMjQ1ZmE0MWU1YmUyZGE3OWIwZTE3ZjIyYzRiNGUxZTU5YjMyZjU3MzIwMmQxOCJ9fX0=");
	public final ItemStack headY = ItemUtils.skullCustom("§6Rotation selon l'axe Y", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzY3NDc3ZjljYjVmMDM2NDM4Y2U5OGNlNjk3YjkxMzU4Y2I5NzY0MWM1ZDE1M2E3ZTM4ODhkYWUzMmUyMTUifX19");
	public final ItemStack headZ = ItemUtils.skullCustom("§6Rotation selon l'axe Z", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2ZhNzMwYjVlYzczY2VhZjRhYWQ1MjRlMjA3ZmYzMWRmNzg1ZTdhYjZkYjFhZWIzZjFhYTRkMjQ1ZWQyZSJ9fX0=");
	
	public final static ItemStack arrowRight = ItemUtils.skullCustom("§eMouvement sur partie §adroite §edu porte armure", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTQxZmY2YmM2N2E0ODEyMzJkMmU2NjllNDNjNGYwODdmOWQyMzA2NjY1YjRmODI5ZmI4Njg5MmQxM2I3MGNhIn19fQ==");
	public final static ItemStack arrowLeft = ItemUtils.skullCustom("§eMouvement sur partie §agauche §edu porte armure", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDliMmJlZTM5YjZlZjQ3ZTE4MmQ2ZjFkY2E5ZGVhODQyZmNkNjhiZGE5YmFjYzZhNmQ2NmE4ZGNkZjNlYyJ9fX0=");
	public final static ItemStack arrowEquals = ItemUtils.skullCustom("§eMouvement sur partie §aentière §edu porte armure", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzY2MzNhZTVlYjcxMjFmOGJkZTVhY2RmODdhMDlmNjc4MDBkN2NlZGY0MTkwZjEyOWU3OGVmZWU5ZTYzIn19fQ==");
	
	public final ItemStack settings = ItemUtils.skullCustom("§7Paramètres avancés", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWMyZmYyNDRkZmM5ZGQzYTJjZWY2MzExMmU3NTAyZGM2MzY3YjBkMDIxMzI5NTAzNDdiMmI0NzlhNzIzNjZkZCJ9fX0=");

	//public final ItemStack cancel = ItemUtils.skullCustom("§cAnnuler", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTMzYTViZmM4YTJhM2ExNTJkNjQ2YTViZWE2OTRhNDI1YWI3OWRiNjk0YjIxNGYxNTZjMzdjNzE4M2FhIn19fQ==");
	public final ItemStack validate = ItemUtils.skullCustom("§aValider", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjViNWZhYThlNDgxZmNiODRjYmVmMWU1YzQyMGQ2YTgxYTZlNjhmNWEwNzUwMDFhMDI4ODI1YWMyMDE4ZWJlNyJ9fX0=");
	//public final ItemStack validate = ItemUtils.skullCustom("§aValider", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjViNWZhYThlNDgxZmNiODRjYmVmMWU1YzQyMGQ2YTgxYTZlNjhmNWEwNzUwMDFhMDI4ODI1YWMyMDE4ZWJlNyJ9fX0=");
	
	public final Map<Integer, GuiItem<?>> guiItems = ImmutableMap.<Integer, GuiItem<?>>builder()
			.put(0, new GuiItemToggle(0, "Gravité", Material.FEATHER, ent -> ent.hasGravity(), (ent, b) -> ent.setGravity(b)))
			.put(1, new GuiItemToggle(1, "Bras visibles", Material.WOODEN_SWORD, ent -> ent.hasArms(), (ent, b) -> ent.setArms(b)))
			.put(2, new GuiItemToggle(2, "Base visible", Material.STONE_PRESSURE_PLATE, ent -> ent.hasBasePlate(), (ent, b) -> ent.setBasePlate(b)))
			.put(3, new GuiItemToggle(3, "Invulnérable", Material.GOLDEN_APPLE, ent -> ent.isInvulnerable(), (ent, b) -> ent.setInvulnerable(b)))
			.put(4, new GuiItemToggle(4, "Petite", Material.WOODEN_SWORD, ent -> ent.isSmall(), (ent, b) -> ent.setSmall(b)))
			.put(5, new GuiItemToggle(5, "Brillante", Material.GLOWSTONE_DUST, ent -> ent.isGlowing(), (ent, b) -> ent.setGlowing(b)))
			.put(6, new GuiItemToggle(6, "Marker", Material.POTION, ent -> ent.isMarker(), (ent, b) -> ent.setMarker(b)))
			
			.build();
	
	public final Map<Integer, TriConsumer<Player, Action, ItemStack>> invItems = ImmutableMap.<Integer, TriConsumer<Player, Action, ItemStack>>builder()
			.put(0, (p, click, it) -> {
				if (!players.containsKey(p))
					return;

				PlayerEditorData data = players.get(p);
				List<ArmorstandPart> list = Arrays.asList(ArmorstandPart.values());
				int index = click == Action.LEFT_CLICK_AIR ? 1 : list.size() - 1;
				
				data.entPart = list.get(list.indexOf(data.entPart) + index % list.size());
				data.entSide = ArmorstandSide.ALL;
				
				p.getInventory().setItem(0, data.entPart.item);
				p.getInventory().setItem(1, data.entSide.item);
			})
			
			.put(1, (p, click, it) -> {
				if (!players.containsKey(p))
					return;

				PlayerEditorData data = players.get(p);
				List<ArmorstandSide> list = Arrays.asList(ArmorstandSide.values());
				int index = click == Action.LEFT_CLICK_AIR ? 1 : list.size() - 1;

				data.entSide = list.get(list.indexOf(data.entSide) + index % list.size());

				p.getInventory().setItem(1, data.entSide.item);
			})
			
			.put(7, (p, click, it) -> {
				if (players.containsKey(p))
					new ArmorstandEditorGui(players.get(p).ent).create(p);
			})
			
			.put(8, (p, click, it) -> {
				closeFor(p);
			})
			.build();
	
	private static final Map<Player, PlayerEditorData> players = new HashMap<Player, PlayerEditorData>();
	private OlympaCreatifMain plugin;
	
	ArmorStandManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;
	}
	
	public void openFor(OlympaPlayerCreatif pc, ArmorStand ent) {
		players.put(pc, plugin.getTask().scheduleSyncRepeatingTask(() -> {
			if (pc.getPlayer().getInventory().getItemInMainHand() != null)
				pc.getPlayer().sendActionBar(pc.getPlayer().getInventory().getItemInMainHand().displayName());
			else
				pc.getPlayer().sendActionBar(Component.text(""));
		}, 1, 5));
	}
	
	public void closeFor(Player p) {
		if (players.containsKey(p)) {
			p.getPlayer().closeInventory();
			plugin.getTask().cancelTaskById(players.remove(p));	
		}
	}
	
	@EventHandler
	public void onInterract(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK)
			
	}
	
	public class ArmorstandEditorGui extends OlympaGUI {
		
		private ArmorStand armorstand;
		
		public ArmorstandEditorGui(ArmorStand armorstand) {
			super(armorstand.getCustomName() == null ? "Edition de porte-armure" : "Edition du porte-armure " + armorstand.getCustomName() , 1);
			this.armorstand = armorstand;
			
			guiItems.forEach((slot, item) -> inv.setItem(slot, item.get(armorstand, null)));
			inv.setItem(7, ItemUtils.itemSeparator(DyeColor.LIGHT_GRAY));
			inv.setItem(8, validate);
		}
		
		@Override
		public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
			if (slot < guiItems.size())
				inv.setItem(slot, guiItems.get(slot).get(armorstand, click));
			else if (slot == 8)
				inv.close();
			
			return true;
		}
		
	}
	
	static abstract class GuiItem<T> {
		
		private int slot;
		
		protected String paramName;
		protected ItemStack item;
		
		protected Function<ArmorStand, T> getValue;
		protected BiConsumer<ArmorStand, T> setValue;
		
		public GuiItem(int slot, String paramName, ItemStack item, Function<ArmorStand, T> getValue, BiConsumer<ArmorStand, T> setValue) {
			this.slot = slot;
			this.item = item;
			this.paramName = paramName;
			this.getValue = getValue;
			this.setValue = setValue;
		}
		
		public abstract ItemStack get(ArmorStand ent, ClickType click); //{
			//return onClick.apply(ent, click);
		//}
		
		public int getSlot() {
			return slot;
		}
	}
	
	static class GuiItemToggle extends GuiItem<Boolean> {

		public GuiItemToggle(int slot, String paramName, Material mat, Function<ArmorStand, Boolean> getValue, 
				BiConsumer<ArmorStand, Boolean> setValue) {
			this(slot, paramName, new ItemStack(mat), getValue, setValue);
		}
		
		public GuiItemToggle(int slot, String paramName, ItemStack item, Function<ArmorStand, Boolean> getValue, 
				BiConsumer<ArmorStand, Boolean> setValue) {
			super(slot, paramName, item, getValue, setValue);
			ItemUtils.lore(super.item, "§7Cliquez pour changer la valeur");
		}

		@Override
		public ItemStack get(ArmorStand ent, ClickType click) {
			if (click != null) 
				setValue.accept(ent, !getValue.apply(ent));
			return ItemUtils.name(item.clone(), paramName + (getValue.apply(ent) ? " : §aoui" : " : §cnon"));
		}
	}
	
	/*static class GuiItemInteger extends GuiItem<Integer> {

		public GuiItemInteger(int slot, String paramName, Material mat, Function<ArmorStand, Integer> getValue,
				BiConsumer<ArmorStand, Integer> setValue) {
			this(slot, paramName, new ItemStack(mat), getValue, setValue);
		}
		
		public GuiItemInteger(int slot, String paramName, ItemStack item, Function<ArmorStand, Integer> getValue,
				BiConsumer<ArmorStand, Integer> setValue) {
			super(slot, paramName, item, getValue, setValue);
			ItemUtils.lore(super.item, "§7Clic gauche : §aaugmente §7la valeur de 1", "§7Clic droit : §cdiminue §7la valeur de 1", 
					" ", "§7Appuyez sur shift pour utiliser un pas de 10");
		}

		@Override
		public ItemStack get(ArmorStand ent, ClickType click) {
			int modifier = 0;
			
			switch (click) {
			case RIGHT:
				modifier+=2;
				break;
			case LEFT:
				modifier+=2;
				break;
			case SHIFT_RIGHT:
				modifier+=10;
				break;
			case SHIFT_LEFT:
				modifier-=10;
				break;
			}
			
			setValue.accept(ent, getValue.apply(ent) + modifier);
			return ItemUtils.name(item.clone(), paramName + " : §e" + getValue.apply(ent));
		}
	}*/
	
	static class PlayerEditorData {
		
		private int taskId;
		private ArmorStand ent;
		private ArmorstandPart entPart = ArmorstandPart.BODY;
		private ArmorstandSide entSide = ArmorstandSide.ALL;
		
		private ItemStack[] savedInventory;
		
	}
	
	static enum ArmorstandPart {
		BODY(ItemUtils.item(Material.ARMOR_STAND, "Rotation porte armure")),
		HEAD(ItemUtils.item(Material.GOLDEN_HELMET, "Rotation de la tête")),
		CHEST(ItemUtils.item(Material.GOLDEN_CHESTPLATE, "Rotation du corps")),
		LEGGINGS(ItemUtils.item(Material.GOLDEN_LEGGINGS, "Rotation des jambes")),
		ARMS(ItemUtils.item(Material.STICK, "Rotation des bras"))
		;
		
		ItemStack item;
		
		ArmorstandPart(ItemStack item) {
			this.item = item;
		}
	}

	/*public final static ItemStack arrowRight = ItemUtils.skullCustom("§eMouvement sur partie §adroite §edu porte armure", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTQxZmY2YmM2N2E0ODEyMzJkMmU2NjllNDNjNGYwODdmOWQyMzA2NjY1YjRmODI5ZmI4Njg5MmQxM2I3MGNhIn19fQ==");
	public final static ItemStack arrowLeft = ItemUtils.skullCustom("§eMouvement sur partie §agauche §edu porte armure", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDliMmJlZTM5YjZlZjQ3ZTE4MmQ2ZjFkY2E5ZGVhODQyZmNkNjhiZGE5YmFjYzZhNmQ2NmE4ZGNkZjNlYyJ9fX0=");
	public final static ItemStack arrowEquals = ItemUtils.skullCustom("§eMouvement sur partie §aentière §edu porte armure", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzY2MzNhZTVlYjcxMjFmOGJkZTVhY2RmODdhMDlmNjc4MDBkN2NlZGY0MTkwZjEyOWU3OGVmZWU5ZTYzIn19fQ==");*/
	
	static enum ArmorstandSide {
		RIGHT(ItemUtils.skullCustom("§eMouvement sur partie §adroite §edu porte armure", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTQxZmY2YmM2N2E0ODEyMzJkMmU2NjllNDNjNGYwODdmOWQyMzA2NjY1YjRmODI5ZmI4Njg5MmQxM2I3MGNhIn19fQ==")),
		LEFT(ItemUtils.skullCustom("§eMouvement sur partie §agauche §edu porte armure", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDliMmJlZTM5YjZlZjQ3ZTE4MmQ2ZjFkY2E5ZGVhODQyZmNkNjhiZGE5YmFjYzZhNmQ2NmE4ZGNkZjNlYyJ9fX0=")),
		ALL(ItemUtils.skullCustom("§eMouvement sur partie §aentière §edu porte armure", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzY2MzNhZTVlYjcxMjFmOGJkZTVhY2RmODdhMDlmNjc4MDBkN2NlZGY0MTkwZjEyOWU3OGVmZWU5ZTYzIn19fQ==")),
		;
		
		ItemStack item;
		
		ArmorstandSide(ItemStack item) {
			this.item = item;
		}
	}
}









