package fr.olympa.olympacreatif.perks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import com.google.common.collect.ImmutableMap;

import fr.olympa.api.spigot.editor.Editor;
import fr.olympa.api.spigot.editor.TextEditor;
import fr.olympa.api.spigot.gui.OlympaGUI;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import net.md_5.bungee.api.ChatColor;

public class ArmorStandManager implements Listener {
	
	public static final int modifierStep = 10;//Math.PI/12;
	
	public final Map<Integer, GuiItem<?>> guiItems = ImmutableMap.<Integer, GuiItem<?>>builder()
			//.put(0, new GuiItemToggle(0, "Gravité", Material.FEATHER, ent -> ent.hasGravity(), (ent, b) -> ent.setGravity(b)))
			.put(0, new GuiItemToggle(0, "Bras visibles", Material.WOODEN_SWORD, ent -> ent.hasArms(), (ent, b) -> ent.setArms(b)))
			.put(1, new GuiItemToggle(1, "Base visible", Material.STONE_PRESSURE_PLATE, ent -> ent.hasBasePlate(), (ent, b) -> ent.setBasePlate(b)))
			.put(2, new GuiItemToggle(2, "Invulnérable", Material.GOLDEN_APPLE, ent -> ent.isInvulnerable(), (ent, b) -> ent.setInvulnerable(b)))
			.put(3, new GuiItemToggle(3, "Petite", Material.OBSERVER, ent -> ent.isSmall(), (ent, b) -> ent.setSmall(b)))
			.put(4, new GuiItemToggle(4, "Brillante", Material.GLOWSTONE_DUST, ent -> ent.isGlowing(), (ent, b) -> ent.setGlowing(b)))
			//.put(6, new GuiItemToggle(6, "Marker", Material.POTION, ent -> ent.isMarker(), (ent, b) -> ent.setMarker(b)))
			
			.build();
	
	public final Map<Integer, BiConsumer<PlayerEditorData, Action>> invItems = ImmutableMap.<Integer, BiConsumer<PlayerEditorData, Action>>builder()
			.put(0, (pData, click) -> {				
				List<ArmorstandPart> list = Arrays.asList(ArmorstandPart.values());
				
				pData.entPart = ArmorstandPart.values()[(list.indexOf(pData.entPart) + 1) % list.size()];
				
				pData.p.getInventory().setItem(0, pData.entPart.item);
				pData.menuInventory[0] = pData.entPart.item;
				//pData.p.getInventory().setItem(1, pData.entSide.item);
				//pData.menuInventory[1] = pData.entSide.item;
			})
			
			.put(1, (pData, click) -> {				
				List<ArmorstandSide> list = Arrays.asList(ArmorstandSide.values());
				
				pData.entSide = ArmorstandSide.values()[(list.indexOf(pData.entSide) + 1) % list.size()];

				pData.p.getInventory().setItem(1, pData.entSide.item);
				pData.menuInventory[1] = pData.entSide.item;
			})
			
			.put(2, (pData, click) -> {
				pData.modifyValue(ArmorstandAxis.X, click == Action.LEFT_CLICK_AIR ? modifierStep : -modifierStep);
			})
			
			.put(3, (pData, click) -> {
				pData.modifyValue(ArmorstandAxis.Y, click == Action.LEFT_CLICK_AIR ? modifierStep : -modifierStep);
			})
			
			.put(4, (pData, click) -> {
				pData.modifyValue(ArmorstandAxis.Z, click == Action.LEFT_CLICK_AIR ? modifierStep : -modifierStep);
			})
			
			.put(5, (pData, click) -> {
				if (pData.ent.isDead()) {
					closeFor(pData.p);
					return;
				}
				
				ArmorStand newEnt = (ArmorStand) plugin.getWorldManager().getWorld().spawnEntity(pData.p.getLocation().getBlock().getLocation().clone().add(0.5, 0, 0.5), EntityType.ARMOR_STAND);
				
				newEnt.setBodyPose(pData.ent.getBodyPose());
				newEnt.setHeadPose(pData.ent.getHeadPose());
				newEnt.setLeftArmPose(pData.ent.getLeftArmPose());
				newEnt.setLeftLegPose(pData.ent.getLeftLegPose());
				newEnt.setRightArmPose(pData.ent.getRightArmPose());
				newEnt.setRightLegPose(pData.ent.getRightLegPose());
				newEnt.setRotation(pData.ent.getLocation().getYaw(), pData.ent.getLocation().getPitch());

				newEnt.setBasePlate(pData.ent.hasBasePlate());
				newEnt.setArms(pData.ent.hasArms());
				newEnt.setGlowing(pData.ent.isGlowing());
				newEnt.setSmall(pData.ent.isSmall());
				newEnt.setInvulnerable(pData.ent.isInvulnerable());
				
				newEnt.setCustomNameVisible(pData.ent.isCustomNameVisible());
				newEnt.customName(pData.ent.customName());
				
				Arrays.asList(EquipmentSlot.values()).forEach(slot -> newEnt.setItem(slot, pData.ent.getItem(slot)));
			})
			
			.put(6, (pData, click) -> {
				if (!pData.ent.isDead())
					pData.moveTo(pData.p.getLocation());
				else
					closeFor(pData.p);
			})
			
			.put(7, (pData, click) -> {
				if (!pData.ent.isDead())
					new ArmorstandEditorGui(pData).create(pData.p);
				else
					closeFor(pData.p);
			})
			
			.put(8, (pData, click) -> {
				closeFor(pData.p);
			})
			.build();
	
	private static final Set<Player> waitingEntSelection = new HashSet<Player>();
	private static final Map<Player, PlayerEditorData> players = new HashMap<Player, PlayerEditorData>();
	private static OlympaCreatifMain plugin;
	
	ArmorStandManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	public void listeningFor(Player p) {
		if (waitingEntSelection.add(p))
			plugin.getTask().runTaskLater(() -> {
				if (waitingEntSelection.remove(p))
					OCmsg.ARMORSTAND_EDITOR_SELECT_ARMORSTAND_TOO_LONG.send(p);
			}, 100);
	}
	
	public void closeFor(Player p) {
		if (players.containsKey(p))
			players.remove(p).close();
	}
	

	
	
	@EventHandler
	public void onEntityLaunched(ProjectileLaunchEvent e) {
		if (players.containsKey(e.getEntity().getShooter()))
			e.setCancelled(true);
	}
	
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		if (players.containsKey(e.getDamager()))
			e.setCancelled(true);
	}
	
	@EventHandler
	public void onDamage(PlayerArmorStandManipulateEvent e) {
		if (players.containsKey(e.getPlayer()))
			e.setCancelled(true);
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		if (players.containsKey(e.getPlayer()))
			players.remove(e.getPlayer()).close();
	}
	
	@EventHandler
	public void onInterractEntity(PlayerInteractAtEntityEvent e) {
		if (e.getRightClicked().getType() == EntityType.ARMOR_STAND && waitingEntSelection.remove(e.getPlayer())) {
			if (!players.containsKey(e.getPlayer())) {
				players.put(e.getPlayer(), new PlayerEditorData(e.getPlayer(), (ArmorStand) e.getRightClicked()));	
				OCmsg.ARMORSTAND_EDITOR_OPEN.send(e.getPlayer());
			}
			
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onInterract(PlayerInteractEvent e) {
		PlayerEditorData data = players.get(e.getPlayer());
		if (data == null)
			return;

		e.setCancelled(true);

		BiConsumer<PlayerEditorData, Action> consumer = invItems.get(e.getPlayer().getInventory().getHeldItemSlot());
		if (consumer != null)
			if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK)
				consumer.accept(data, Action.LEFT_CLICK_AIR);
			else if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)
				consumer.accept(data, Action.RIGHT_CLICK_AIR);		
	}
	
	public class ArmorstandEditorGui extends OlympaGUI {
		
		private PlayerEditorData editor;
		
		public ArmorstandEditorGui(PlayerEditorData editor) {
			super(editor.ent.getCustomName() == null ? "Edition de porte-armure" : "Edition du porte-armure " + editor.ent.getCustomName() , 1);
			this.editor = editor;
			
			guiItems.forEach((slot, item) -> inv.setItem(slot, item.get(editor.ent, null)));
			inv.setItem(6, ItemUtils.item(Material.NAME_TAG, "§6Définir le nom du porte-armure", "§7Cliquez pour donner un nom", "§7spécifique au porte-armure"));
			inv.setItem(7, ItemUtils.itemSeparator(DyeColor.LIGHT_GRAY));
			inv.setItem(8, ItemUtils.skullCustom("§2Valider", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjViNWZhYThlNDgxZmNiODRjYmVmMWU1YzQyMGQ2YTgxYTZlNjhmNWEwNzUwMDFhMDI4ODI1YWMyMDE4ZWJlNyJ9fX0="));
		}
		
		@Override
		public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
			if (slot < guiItems.size())
				inv.setItem(slot, guiItems.get(slot).get(editor.ent, click));
			else if (slot == 6) {
				Prefix.DEFAULT_GOOD.sendMessage(p, "§aEcrivez le nom du porte-armure, ou §7none §apour annuler :");
				Editor.leave(p);
				
				new TextEditor<String>(p, s -> {
					if (editor.ent.isDead()) {
						closeFor(p);
						return;
					}
					
					if (!s.equals("none")) {
						editor.ent.setCustomNameVisible(true);
						editor.ent.setCustomName(ChatColor.translateAlternateColorCodes('&', s));
						Prefix.DEFAULT_GOOD.sendMessage(p, "Nom du porte-armure défini à " + editor.ent.getCustomName() + ".");
					}else {
						editor.ent.setCustomNameVisible(false);
						editor.ent.setCustomName(null);	
						Prefix.DEFAULT_GOOD.sendMessage(p, "Nom du porte-armure supprimé.");
					}
					
				}, null, true).enterOrLeave();
			}
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
		
		public abstract ItemStack get(ArmorStand ent, ClickType click);
		
		public int getSlot() {
			return slot;
		}
	}
	
	static class GuiItemToggle extends GuiItem<Boolean> {

		public GuiItemToggle(int slot, String paramName, Material mat, Function<ArmorStand, Boolean> getValue, 
				BiConsumer<ArmorStand, Boolean> setValue) {
			this(slot, "§6" + paramName, new ItemStack(mat), getValue, setValue);
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
	
	static class PlayerEditorData {
		
		private static ItemStack[] defaultInv = new ItemStack[] {
				ArmorstandPart.BODY.item, 
				ArmorstandSide.ALL.item, 
				//ItemUtils.itemSeparator(DyeColor.GRAY),
				ItemUtils.skullCustom("§6Mouvement selon X §7(clic gauche = sens horaire | clic droit = sens antihoraire)", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmNjNWZhZTNiNjUwZTZlZGIzMjQ1ZmE0MWU1YmUyZGE3OWIwZTE3ZjIyYzRiNGUxZTU5YjMyZjU3MzIwMmQxOCJ9fX0="),
				ItemUtils.skullCustom("§6Mouvement selon Y §7(clic gauche = sens horaire | clic droit = sens antihoraire)", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzY3NDc3ZjljYjVmMDM2NDM4Y2U5OGNlNjk3YjkxMzU4Y2I5NzY0MWM1ZDE1M2E3ZTM4ODhkYWUzMmUyMTUifX19"),
				ItemUtils.skullCustom("§6Mouvement selon Z §7(clic gauche = sens horaire | clic droit = sens antihoraire)", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2ZhNzMwYjVlYzczY2VhZjRhYWQ1MjRlMjA3ZmYzMWRmNzg1ZTdhYjZkYjFhZWIzZjFhYTRkMjQ1ZWQyZSJ9fX0="),
				ItemUtils.item(Material.ENCHANTING_TABLE, "§bDupliquer le porte-armure"),
				ItemUtils.item(Material.ENDER_PEARL, "§bTéléporter le porte-armure"),
				ItemUtils.skullCustom("§9Paramètres avancés", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWMyZmYyNDRkZmM5ZGQzYTJjZWY2MzExMmU3NTAyZGM2MzY3YjBkMDIxMzI5NTAzNDdiMmI0NzlhNzIzNjZkZCJ9fX0="),
				ItemUtils.skullCustom("§2Valider", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjViNWZhYThlNDgxZmNiODRjYmVmMWU1YzQyMGQ2YTgxYTZlNjhmNWEwNzUwMDFhMDI4ODI1YWMyMDE4ZWJlNyJ9fX0=")}; 
		
		private Player p;
		private ArmorStand ent;
		private int taskId;
		
		private ArmorstandPart entPart = ArmorstandPart.BODY;
		private ArmorstandSide entSide = ArmorstandSide.ALL;
		
		private ItemStack[] menuInventory;
		private ItemStack[] savedInventory;
		
		PlayerEditorData(Player p, ArmorStand ent) {
			this.p = p;
			this.ent = ent;
			menuInventory = defaultInv.clone();
			savedInventory = p.getInventory().getContents();
			taskId = plugin.getTask().scheduleSyncRepeatingTask(() -> p.getInventory().setContents(menuInventory), 1, 10);
		}
		
		public void close() {
			Editor.leave(p);
			p.closeInventory();
			plugin.getTask().cancelTaskById(taskId);
			p.getInventory().setContents(savedInventory);
			OCmsg.ARMORSTAND_EDITOR_EXIT.send(p);
		}
		
		public void modifyValue(ArmorstandAxis axis, double modifier) {
			if (ent.isDead()) {
				plugin.getPerksManager().getArmorStandManager().closeFor(p);
				return;
			}
			
			switch(entPart) {
			case ARMS:
				switch (entSide) {
				case ALL:
					ent.setLeftArmPose(getNewAngle(ent.getLeftArmPose(), axis, modifier));
					ent.setRightArmPose(getNewAngle(ent.getRightArmPose(), axis, modifier));
					break;
					
				case LEFT:
					ent.setLeftArmPose(getNewAngle(ent.getLeftArmPose(), axis, modifier));
					break;
					
				case RIGHT:
					ent.setRightArmPose(getNewAngle(ent.getRightArmPose(), axis, modifier));
					break;
				}
				break;

			case BODY:
				if (axis == ArmorstandAxis.Y)
					ent.setRotation(Math.floorMod((int) (ent.getLocation().getYaw() + modifier), 360), ent.getLocation().getPitch());
				else
					ent.setRotation(ent.getLocation().getYaw(), Math.floorMod((int) (ent.getLocation().getPitch() + 90 + modifier), 180) - 90);
				break;
				
			case CHEST:
				ent.setBodyPose(getNewAngle(ent.getBodyPose(), axis, modifier));
				break;
				
			case HEAD:
				ent.setHeadPose(getNewAngle(ent.getHeadPose(), axis, modifier));
				break;
				
			case LEGS:
				switch (entSide) {
				case ALL:
					ent.setLeftLegPose(getNewAngle(ent.getLeftLegPose(), axis, modifier));
					ent.setRightLegPose(getNewAngle(ent.getRightLegPose(), axis, modifier));
					break;
					
				case LEFT:
					ent.setLeftLegPose(getNewAngle(ent.getLeftLegPose(), axis, modifier));
					break;
					
				case RIGHT:
					ent.setRightLegPose(getNewAngle(ent.getRightLegPose(), axis, modifier));
					break;
				}
				break;
				
			case POSITION:
				modifier = modifier > 0 ? 0.1 : -0.1;
				switch (axis) {
				case X:
					moveTo(ent.getLocation().add(modifier, 0, 0));
					break;
				case Y:
					moveTo(ent.getLocation().add(0, modifier, 0));
					break;
				case Z:
					moveTo(ent.getLocation().add(0, 0, modifier));
					break;
				}
				break;
			}
			
		}
		
		private void moveTo(Location loc) {
			EulerAngle[] pose = new EulerAngle[] {ent.getBodyPose(), ent.getHeadPose(), ent.getLeftArmPose(), ent.getLeftLegPose(), ent.getRightArmPose(), ent.getRightLegPose()};
			float yaw = ent.getLocation().getYaw();
			float pitch = ent.getLocation().getPitch();

			ent.teleport(loc);
			
			ent.setBodyPose(pose[0]);
			ent.setHeadPose(pose[1]);
			ent.setLeftArmPose(pose[2]);
			ent.setLeftLegPose(pose[3]);
			ent.setRightArmPose(pose[4]);
			ent.setRightLegPose(pose[5]);
			ent.setRotation(yaw, pitch);
		}
		
		//modifier in degrees
		private EulerAngle getNewAngle(EulerAngle angle, ArmorstandAxis axis, double modifier) {
			
			switch(axis) {
			case X:
				return angle.setX(Math.toRadians(Math.floorMod((int) (Math.toDegrees(angle.getX()) + modifier), 360)));
			case Y:
				return angle.setY(Math.toRadians(Math.floorMod((int) (Math.toDegrees(angle.getY()) + modifier), 360)));
			case Z:
				return angle.setZ(Math.toRadians(Math.floorMod((int) (Math.toDegrees(angle.getZ()) + modifier), 360)));
			default:
				return null;
			}
		}
	}
	
	static enum ArmorstandPart {
		BODY(ItemUtils.item(Material.ARMOR_STAND, "§aRotation porte-armure §7(clic gauche pour changer)")),
		HEAD(ItemUtils.item(Material.GOLDEN_HELMET, "§aRotation de la tête §7(clic gauche pour changer)")),
		CHEST(ItemUtils.item(Material.GOLDEN_CHESTPLATE, "§aRotation du corps §7(clic gauche pour changer)")),
		LEGS(ItemUtils.item(Material.GOLDEN_LEGGINGS, "§aRotation des jambes §7(clic gauche pour changer)")),
		ARMS(ItemUtils.item(Material.GOLDEN_SWORD, "§aRotation des bras §7(clic gauche pour changer)")),
		POSITION(ItemUtils.item(Material.ENDER_EYE, "§aPosition du porte-armure §7(clic gauche pour changer)"))
		;
		
		ItemStack item;
		
		ArmorstandPart(ItemStack item) {
			this.item = item;
		}
	}
	
	static enum ArmorstandSide {
		RIGHT(ItemUtils.skullCustom("§eMouvement sur moitiée §adroite §edu porte armure §7(clic gauche pour changer)", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTQxZmY2YmM2N2E0ODEyMzJkMmU2NjllNDNjNGYwODdmOWQyMzA2NjY1YjRmODI5ZmI4Njg5MmQxM2I3MGNhIn19fQ==")),
		LEFT(ItemUtils.skullCustom("§eMouvement sur moitiée §agauche §edu porte armure §7(clic gauche pour changer)", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDliMmJlZTM5YjZlZjQ3ZTE4MmQ2ZjFkY2E5ZGVhODQyZmNkNjhiZGE5YmFjYzZhNmQ2NmE4ZGNkZjNlYyJ9fX0=")),
		ALL(ItemUtils.skullCustom("§eMouvement sur la §atotalité §edu porte armure §7(clic gauche pour changer)", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzY2MzNhZTVlYjcxMjFmOGJkZTVhY2RmODdhMDlmNjc4MDBkN2NlZGY0MTkwZjEyOWU3OGVmZWU5ZTYzIn19fQ==")),
		;
		
		ItemStack item;
		
		ArmorstandSide(ItemStack item) {
			this.item = item;
		}
	}
	
	static enum ArmorstandAxis {
		X,
		Y,
		Z,
		;
	}
}









