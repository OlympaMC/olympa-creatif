package fr.olympa.olympacreatif.perks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftArmorStand;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import net.minecraft.server.v1_15_R1.ChatMessage;
import net.minecraft.server.v1_15_R1.EntityArmorStand;

public class LinesOnNameUtil {

	Map<Entity, List<LineData>> rowsMap = new HashMap<Entity, List<LineData>>();
	List<Entity> holdersEntities = new ArrayList<Entity>();
	
	private OlympaCreatifMain plugin;
	
	public LinesOnNameUtil(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		
		//listener supprimant les textes si l'entité meurt ou se déconnecte
		plugin.getServer().getPluginManager().registerEvents(new AutoRemoveListener(), plugin);
	}	
	
	//renvoie le texte de la ligne spétifiée. Index commençant à 0, puis de bas en haut
	/**
	 * 
	 * Renvoie le texte de la ligne row pour l'entité e, ou null
	 * si la ligne n'existe pas.
	 * 
	 * @param e Entité à tester
	 * @param row Index de la ligne à récupérer
	 * @return Contenu de la ligne donnée, null si la ligne n'existe pas
	 */
	public String getLine(Player e, int row) {
		if (!rowsMap.containsKey(e))
			return null;
		
		else if (rowsMap.get(e).size() < row)
			return null;
		
		else
			return rowsMap.get(e).get(row).getArmorStand().getCustomName(); 
	}
	
	
	/**
	 *
	 * Edite/crée un texte flotant au dessus de la tête d'une entité.
	 * Attention : cette opération supprime l'affichage du nom de l'entité,
	 * utiliser deux fois cette méthode pour le rafficher.
	 * 
	 * @param e Entité dont le texte sera étité
	 * @param row Index de la ligne. Commence à 0 et progresse de haut en bas
	 * @param text Text texte à afficher
	 * @param overrideOldText Si vrai, remplace l'ancien texte. Si faux, déplace l'ancien texte vers le haut.  
	 */
	public void setLine(Entity e, int row, String text, boolean overrideOldText) {
		if (!rowsMap.containsKey(e))
			rowsMap.put(e, new ArrayList<LineData>());
		
		List<LineData> list = rowsMap.get(e);
		
		//Si l'index est trop élevé, ramène l'index de la ligne au prochain slot disponible
		row = Math.min(list.size(), row);
		
		
		
		//modification de texte d'une ligne existance
		if (list.size() > row && overrideOldText) {
			list.get(row).getArmorStand().setCustomName(text);
			
		//modification du texte d'une ligne existante et conservation de l'ancien texte	
		}else if (list.size() > row && !overrideOldText) {
			
			//sélection de l'entité portant le texte
			Entity holdingEntity = list.get(row).getArmorStandHolder();
			
			if (row > 0)
				holdingEntity.removePassenger(list.get(row - 1).getArmorStandHolder());
			
			//création du texte
			LineData data = new LineData(holdingEntity, text);
			
			if (row > 0)
				data.getArmorStandHolder().addPassenger(list.get(row - 1).getArmorStandHolder());
			
			list.add(row, data);
			
			//décalage des textes supérieurs
			for (int i = row + 1 ; i < list.size() ; i++)
				list.get(i - 1).getArmorStandHolder().addPassenger(list.get(i).getArmorStandHolder());
			
		//création d'une nouvelle ligne
		}else {
			
			if (list.size() > 0) 
				e.removePassenger(list.get(list.size() - 1).getArmorStandHolder());	
			
			LineData data = new LineData(e, text);
			
			if (list.size() > 0)
				data.getArmorStandHolder().addPassenger(list.get(list.size() - 1).getArmorStandHolder());
			
			list.add(data);
		}
	}
	
	
	/**
	 * 
	 * Supprime une ligne d'une entité.
	 * 
	 * @param e Entité à traiter
	 * @param row Index de la ligne à retirer
	 * @return Vrai si la ligne a bien été supprimée, faux sinon
	 */
	public boolean removeLine(Entity e, int row) {		
		if (!rowsMap.containsKey(e))
			return false;
		
		List<LineData> list = rowsMap.get(e);

		row = Math.min(list.size() - 1, row);
		
		if (list.size() == 0)
			return false;
		
		//décalage des lignes vers le bas
		for (int i = 0 ; i < row ; i++)
			list.get(i + 1).getArmorStand().setCustomName(list.get(i).getArmorStand().getCustomName());

		//supression de la dernière ligne de l'armorstand (maintenant en doublon avec l'avant dernière après avoir écrasé la ligne row)
		list.get(0).killEntities();
		
		list.remove(0);
		
		//supression de la liste de la hashmap si elle est vide
		if (list.size() == 0)
			rowsMap.remove(e);
		
		return true;
	}
	
	/**
	 * 
	 * Retourne le nombre de lignes sur une entité
	 * 
	 * @param e Entité à tester
	 * @return Nombre de lignes sur l'entité
	 */
	public int getLinesCount(Entity e) {
		if (rowsMap.containsKey(e))
			return rowsMap.get(e).size();
		
		return 0;
	}

	/**
	 * 
	 * Supprime tous les textes de l'entité en paramètre
	 * 
	 * @param e Entité à traiter
	 */
	public void clearLines(Entity e) {
		if (!rowsMap.containsKey(e))
			return;
		
		for (LineData le : rowsMap.get(e)) 
			le.killEntities();
		
		rowsMap.remove(e);
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
	
	//objet contenant les couples armorstand/entité portant l'armorstand
	private class LineData{
		
		private Entity armHolder;
		private Entity arm;
		
		private LineData(Entity holdingEntity, String text) {

			//summon silverfish qui portera le porte armure
			LivingEntity silverfish = (LivingEntity) plugin.getWorldManager().getWorld().spawnEntity(holdingEntity.getLocation(), EntityType.SILVERFISH);
			silverfish.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 0, false, false, false));
			silverfish.setInvulnerable(true);
			silverfish.getBoundingBox().resize(-5, -5, -5, -4, -4, -4);
			
			((CraftSilverfish)silverfish).getHandle().setNoAI(true);
			((CraftSilverfish)silverfish).getHandle().setSilent(true);
			
			//création de l'armorstand portant le nom visible
			EntityArmorStand armorStand = ((CraftArmorStand) plugin.getWorldManager().getWorld().spawnEntity(holdingEntity.getLocation(), EntityType.ARMOR_STAND)).getHandle();
			armorStand.setInvisible(true);
			armorStand.setCustomName(new ChatMessage(text));
			armorStand.setCustomNameVisible(true);
			armorStand.setMarker(true);
			armorStand.setSmall(true);
			armorStand.getBukkitEntity().getBoundingBox().resize(-5, -5, -5, -4, -4, -4);
			
			this.armHolder = silverfish; 
			this.arm = armorStand.getBukkitEntity();
			
			//ajout de l'armorstand comme passager du silverfish
			silverfish.addPassenger(armorStand.getBukkitEntity());
			
			holdingEntity.addPassenger(silverfish);
			
			holdersEntities.add(arm);
			holdersEntities.add(armHolder);
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
		}
	}
	
	private class AutoRemoveListener implements Listener{
		
		@EventHandler
		public void onQuitEvent(PlayerQuitEvent e) {
			clearLines(e.getPlayer());
		}
		
		@EventHandler
		public void onEntityDeathEvent(EntityDeathEvent e) {
			if (e.getEntityType() == EntityType.PLAYER)
				return;
			
			clearLines(e.getEntity());
		}
	}
}









