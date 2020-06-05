package fr.olympa.olympacreatif.perks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.craftbukkit.v1_15_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftSilverfish;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import net.minecraft.server.v1_15_R1.ChatMessage;
import net.minecraft.server.v1_15_R1.EntityArmorStand;

public class LinesOnNameUtil {

	Map<Entity, List<LineEntities>> rows = new HashMap<Entity, List<LineEntities>>();
	private OlympaCreatifMain plugin;
	
	public LinesOnNameUtil(OlympaCreatifMain plugin) {
		this.plugin = plugin;
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
		if (!rows.containsKey(e))
			return null;
		
		else if (rows.get(e).size() < row)
			return null;
		
		else
			return rows.get(e).get(row).getArmorStand().getCustomName(); 
	}
	
	
	/**
	 *
	 * Edite/crée un texte flotant au dessus de la tête d'une entité.
	 * Attention : cette opération supprime l'affichage du nom de l'entité,
	 * utiliser deux fois cette méthode pour le rafficher.
	 * 
	 * @param e Entité dont le texte sera étité
	 * @param row Index de la ligne. Commence à 0 et progresse de la tête de l'entité vers le haut
	 * @param Text texte à afficher
	 */
	public void setLine(Entity e, int row, String text) {
		if (!rows.containsKey(e))
			rows.put(e, new ArrayList<LineEntities>());
		
		if (rows.get(e).size() > row)
			rows.get(e).get(row).getArmorStand().setCustomName(text);
		
		else {
			//summon silverfish qui portera le porte armure
			LivingEntity silverfish = (LivingEntity) plugin.getWorldManager().getWorld().spawnEntity(e.getLocation(), EntityType.SILVERFISH);
			silverfish.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 0, false, false, false));
			silverfish.setInvulnerable(true);
			
			((CraftSilverfish)silverfish).getHandle().setNoAI(true);
			((CraftSilverfish)silverfish).getHandle().setSilent(true);
			
			//sélection de l'entité portant le silverfish
			Entity holdingEntity = e;
			if (rows.get(e).size() > 0)
				holdingEntity = rows.get(e).get(rows.get(e).size() - 1).getArmorStandHolder();
			
			holdingEntity.addPassenger(silverfish);
			
			//création de l'armorstand portant le nom visible
			EntityArmorStand armorStand = ((CraftArmorStand) plugin.getWorldManager().getWorld().spawnEntity(e.getLocation(), EntityType.ARMOR_STAND)).getHandle();
			armorStand.setInvisible(true);
			armorStand.setCustomName(new ChatMessage(text));
			armorStand.setCustomNameVisible(true);
			armorStand.setMarker(true);
			armorStand.setSmall(true);
			
			//ajout de l'armorstand comme passager du silverfish
			silverfish.addPassenger(armorStand.getBukkitEntity());
			
			rows.get(e).add(new LineEntities(silverfish, armorStand.getBukkitEntity()));
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
		if (!rows.containsKey(e))
			return false;

		List<LineEntities> list = rows.get(e);
		
		if (list.size() >= row)
			return false;
		
		//décalage des lignes vers le bas
		for (int i = row + 1 ; i < list.size() ; i++) 
			list.get(i - 1).getArmorStand().setCustomName(list.get(i).getArmorStand().getCustomName());

		//supression de la dernière ligne de l'armorstand (maintenant en doublon avec l'avant dernière après avoir écrasé la ligne row)
		list.get(list.size() - 1).getArmorStand().remove();
		list.get(list.size() - 1).getArmorStandHolder().remove();
		
		list.remove(list.size() - 1);
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
		if (rows.containsKey(e))
			return rows.get(e).size();
		
		return 0;
	}
	
	//objet contenant les couples armorstand/entité portant l'armorstand
	private class LineEntities{
		
		private Entity armHolder;
		private Entity arm;
		
		private LineEntities(Entity armHolder, Entity armorstand) {
			this.armHolder = armHolder; 
			this.arm = armorstand;
		}
		
		public Entity getArmorStand() {
			return arm;
		}
		
		public Entity getArmorStandHolder() {
			return armHolder;
		}
	}
	
}









