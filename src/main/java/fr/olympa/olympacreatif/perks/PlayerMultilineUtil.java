package fr.olympa.olympacreatif.perks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftSilverfish;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import net.minecraft.server.v1_15_R1.ChatMessage;
import net.minecraft.server.v1_15_R1.EntityArmorStand;
import net.minecraft.server.v1_15_R1.EntityFox.i;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityDestroy;

public class PlayerMultilineUtil {

	//Map<Player, List<LineData>> rowsMap = new HashMap<Player, List<LineData>>();
	Map<Player, LineDataWrapper> rowsMap = new HashMap<Player, LineDataWrapper>();
	List<Entity> holdersEntities = new ArrayList<Entity>();
	
	private OlympaCreatifMain plugin;
	
	public PlayerMultilineUtil(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		
		//listener supprimant les textes si l'entité meurt ou se déconnecte
		plugin.getServer().getPluginManager().registerEvents(new AutoRemoveListener(), plugin);
	}	
	

	/**
	 * 
	 * Retourne vrai si l'entité est un textholder, faux sinon
	 * 
	 * @param entity Entité à tester
	 * @return Vrai si l'entité est un textholder, faux sinon
	 */
	public boolean isTextHolder(Entity entity) {
		return holdersEntities.contains(entity);
	}
	
	public LineDataWrapper getLineDataWrapper(Player p) {
		if (!rowsMap.containsKey(p))
			rowsMap.put(p, new LineDataWrapper(p));
		
		return rowsMap.get(p);
	}
	
	public class LineDataWrapper{
		
		private Player p;
		List<LineData> lines = new ArrayList<LineData>();
		
		private LineDataWrapper(Player p) {
			this.p = p;
		}
		
		public Player getPlayer() {
			return p;
		}
		
		public int getLinesCount() {
			return lines.size();
		}
		
		public void clearLines() {
			for (LineData data : lines)
				data.killEntities();
			lines.clear();
			
			rowsMap.remove(p);
		}
		
		public String getLineId(int row) {
			if (lines.size() > row)
				return lines.get(row).getId();
			
			return null;
		}
		
		public int getLineIndex(String id) {
			for (LineData data : lines)
				if (data.getId().equals(id))
					return lines.indexOf(data);
			
			return -1;
		}
		
		public boolean addLine(String id, String text) {
			
			if (getLineIndex(id) >= 0)
				return lines.get(getLineIndex(id)).setText(text);
			
			if (lines.size() > 0) 
				p.removePassenger(lines.get(lines.size() - 1).getArmorStandHolder());	
			
			LineData data = new LineData(id, p, text);
			
			if (lines.size() > 0)
				data.getArmorStandHolder().addPassenger(lines.get(lines.size() - 1).getArmorStandHolder());

			plugin.getTask().runTaskLater(new Runnable() {

				@Override
				public void run() {
					
					//masquage de l'entité pour le joueur concerné
					PacketPlayOutEntityDestroy packetHolder = new PacketPlayOutEntityDestroy(data.getArmorStandHolder().getEntityId());
					PacketPlayOutEntityDestroy packetArmorStand = new PacketPlayOutEntityDestroy(data.getArmorStand().getEntityId());
					
					((CraftPlayer) p).getHandle().playerConnection.sendPacket(packetHolder);
					((CraftPlayer) p).getHandle().playerConnection.sendPacket(packetArmorStand);
				
			}}, 1);
			
			lines.add(data);
			
			return true;
		}
		
		public boolean moveLine(String lineIdToMove, int destination) {
			return moveLine(getLineIndex(lineIdToMove), destination);
		}
		
		public boolean moveLine(int lineToMove, int destination) {
			if (lines.size() <= lineToMove || lines.size() <= destination || lineToMove == destination)
				return false;

			String idToMove = lines.get(lineToMove).getId();
			String textToMove = lines.get(lineToMove).getText();

			lines.get(lineToMove).setId(lines.get(destination).getId());
			lines.get(lineToMove).setText(lines.get(destination).getText());
			
			lines.get(destination).setId(idToMove);
			lines.get(destination).setText(textToMove);
			
			return true;
		}
		
		public boolean editLine(String id, String text) {
			return editLine(getLineIndex(id), text);
		}
		
		public boolean editLine(int row, String text) {
			if (lines.size() > row && row >= 0) {
				lines.get(row).setText(text);
				return true;
			}
			
			return false;
		}
		
		public String getLineText(String id) {
			return getLineText(getLineIndex(id));
		}
		
		public String getLineText(int row) {
			if (row < 0 || lines.size() <= row)
				return "";
			
			return lines.get(row).getText();
		}
		
		public boolean removeLine(String rowId) {
			int i = getLineIndex(rowId);
			
			if (i >= 0)
				return removeLine(i);
			else
				return false;
		}
		
		public boolean removeLine(int row) {		
			if (!rowsMap.containsKey(p))
				return false;

			row = Math.min(lines.size() - 1, row);
			
			if (lines.size() == 0)
				return false;
			
			LineData topData = null;
			if (row > 0)
				topData = lines.get(row - 1);

			Entity bottomHolder = p;
			if (row + 1 < lines.size())
				bottomHolder = lines.get(row + 1).getArmorStandHolder();
			
			lines.get(row).killEntities();
			lines.remove(row);
			
			if (topData != null)
				bottomHolder.addPassenger(topData.getArmorStandHolder());
			
			if (lines.size() == 0)
				rowsMap.remove(p);
			
			return true;
		}
	}
	
	//objet contenant les couples armorstand/entité portant l'armorstand
	private class LineData{
		
		private String id;
		private Entity armHolder;
		private Entity arm;
		
		private LineData(String id, Entity holdingEntity, String text) {

			this.id = id;
			
			//summon silverfish qui portera le porte armure
			LivingEntity silverfish = (LivingEntity) plugin.getWorldManager().getWorld().spawnEntity(holdingEntity.getLocation(), EntityType.SILVERFISH);
			silverfish.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 0, false, false, false));
			silverfish.setInvulnerable(true);
			silverfish.getBoundingBox().resize(-0.1, -5, -0.1, 0.1, -4.9, 0.1);
			
			((CraftSilverfish)silverfish).getHandle().setNoAI(true);
			((CraftSilverfish)silverfish).getHandle().setSilent(true);
			
			//création de l'armorstand portant le nom visible
			EntityArmorStand armorStand = ((CraftArmorStand) plugin.getWorldManager().getWorld().spawnEntity(holdingEntity.getLocation(), EntityType.ARMOR_STAND)).getHandle();
			armorStand.setInvisible(true);
			armorStand.setCustomName(new ChatMessage(text));
			armorStand.setCustomNameVisible(true);
			armorStand.setMarker(true);
			armorStand.setSmall(true);
			armorStand.getBukkitEntity().getBoundingBox().resize(-0.1, -5, -0.1, 0.1, -4.9, 0.1);
			
			this.armHolder = silverfish; 
			this.arm = armorStand.getBukkitEntity();
			
			//ajout de l'armorstand comme passager du silverfish
			silverfish.addPassenger(armorStand.getBukkitEntity());
			
			holdingEntity.addPassenger(silverfish);
			
			holdersEntities.add(arm);
			holdersEntities.add(armHolder);
		}
		
		public String getText() {
			return arm.getCustomName();
		}

		public Entity getArmorStand() {
			return arm;
		}
		
		public Entity getArmorStandHolder() {
			return armHolder;
		}
		
		public void killEntities() {
			arm.remove();
			armHolder.remove();
			holdersEntities.remove(arm);
			holdersEntities.remove(armHolder);
			id = "";
		}
		
		public boolean setText(String text) {
			arm.setCustomName(text);
			return true;
		}
		
		public String getId() {
			return id;
		}
		
		public void setId(String newId) {
			id = newId;
		}
	}
	
	private class AutoRemoveListener implements Listener{
		
		@EventHandler
		public void onQuitEvent(PlayerQuitEvent e) {
			getLineDataWrapper(e.getPlayer()).clearLines();
			rowsMap.remove(e.getPlayer());
		}
		
		/*
		@EventHandler
		public void join(PlayerJoinEvent e) {
			LineDataWrapper data = getLineDataWrapper(e.getPlayer());
			
			new BukkitRunnable() {
				
				@Override
				public void run() {
					data.addLine("id 1", "1");
					data.addLine("id 2", "2");
					data.addLine("id 3", "3");
					data.addLine("id 4", "4");
					data.addLine("id 5", "5");

					data.removeLine("id 4");
					data.moveLine("id 5", 1);
				}
			}.runTaskLater(plugin, 5);
		}
		
		*/
	}
}









