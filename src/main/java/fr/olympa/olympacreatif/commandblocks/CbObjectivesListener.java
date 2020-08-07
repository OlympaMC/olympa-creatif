package fr.olympa.olympacreatif.commandblocks;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.CbObjective.ObjType;
import fr.olympa.olympacreatif.commandblocks.CbTeam.ColorType;
import fr.olympa.olympacreatif.gui.PlayerPlotsGui;
import fr.olympa.olympacreatif.plot.FakePlayerDeathEvent;
import fr.olympa.olympacreatif.plot.Plot;

public class CbObjectivesListener implements Listener {

	private OlympaCreatifMain plugin;
	
	public CbObjectivesListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if (!(e instanceof LivingEntity))
			return;
		
		Plot plot = plugin.getPlotsManager().getPlot(e.getEntity().getLocation());
		
		if (plot == null)
			return;
		
		for (CbObjective o : plot.getCbData().getObjectives())
			switch(o.getType()) {
			case health:
				if (((LivingEntity) e).getHealth() - e.getDamage() >= 0)
					o.set(e.getEntity(), (int) ((LivingEntity) e).getHealth());
				else
					o.set(e.getEntity(), (int) ((LivingEntity)e).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()*2);
				break;
			}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onSpawn(EntitySpawnEvent e) {
		if (e.isCancelled() || !(e instanceof LivingEntity))
			return;
		
		Plot plot = plugin.getPlotsManager().getPlot(e.getEntity().getLocation());
		
		if (plot == null)
			return;
		
		for (CbObjective o : plot.getCbData().getObjectives())
			switch(o.getType()) {
			case health:
				o.set(e.getEntity(), (int) ((LivingEntity)e).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()*2);
				break;
			}
	}
	
	@EventHandler
	public void onEntityDeathEvent(EntityDeathEvent e) {
		
		Plot plot = plugin.getPlotsManager().getPlot(e.getEntity().getLocation());
		
		if (plot == null)
			return;

		for (CbObjective o : plot.getCbData().getObjectives())
			switch(o.getType()) {
			
			case minecraft_killed:
				if (e.getEntityType() == o.getParamType() && e.getEntity().getKiller() != null)
					o.add(e.getEntity().getKiller(), 1);
				break;
				
			case minecraft_killed_by:
				if (e.getEntity().getLastDamageCause().getEntity() == o.getParamType())
					o.add(e.getEntity(), 1);
				break;
				
			case totalKillCount:
				if (e.getEntity().getKiller() != null)
					o.add(e.getEntity().getKiller(), 1);
				break;
				
			case teamkill:
				if (e.getEntity().getKiller() == null)
					return;
				
				CbTeam targetTeam = plot.getCbData().getTeamOf(e.getEntity());
				
				if (targetTeam != null && o.getParamType() == targetTeam.getColor())
					o.add(e.getEntity().getKiller(), 1);
				break;
				
			case killedByTeam:
				if (e.getEntity().getType() != EntityType.PLAYER)
					return;
				
				CbTeam killedTeam = plot.getCbData().getTeamOf(e.getEntity().getKiller());
				
				if (killedTeam != null && o.getParamType() == killedTeam.getColor())
					o.add(e.getEntity(), 1);
				break;
			}
	}
	
	@EventHandler
	public void onFakeDeath(FakePlayerDeathEvent e) {
		
		Plot plot = e.getPlot();
		
		if (plot == null)
			return;

		for (CbObjective o : plot.getCbData().getObjectives())
			switch(o.getType()) {
			case minecraft_killed:
				if (o.getParamType() == EntityType.PLAYER && e.getKiller() != null)
					o.add(e.getKiller(), 1);
				break;
				
			case minecraft_killed_by:
				if (e.getKiller() != null && o.getParamType() == e.getKiller().getType())
					o.add(e.getPlayer(), 1);
				break;
				
			case totalKillCount:
				if (e.getKiller() != null)
					o.add(e.getKiller(), 1);
				break;
				
			case deathCount:
				o.add(e.getPlayer(), 1);
				break;
				
			case playerKillCount:
				if (e.getKiller() != null)
					o.add(e.getKiller(), 1);
				break;
				
			case teamkill:
				if (e.getKiller() == null)
					return;
				
				CbTeam targetTeam = plot.getCbData().getTeamOf(e.getPlayer());
				
				if (targetTeam != null && o.getParamType() == targetTeam.getColor())
					o.add(e.getKiller(), 1);
				break;
				
			case killedByTeam:
				CbTeam killedTeam = plot.getCbData().getTeamOf(e.getKiller());
				
				if (killedTeam != null && o.getParamType() == killedTeam.getColor())
					o.add(e.getPlayer(), 1);
				break;
			}		
	}
	
	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
		
		Plot plot = plugin.getPlotsManager().getPlot(e.getPlayer().getLocation());
		
		if (plot == null)
			return;
		
		for (CbObjective o : plot.getCbData().getObjectives())
			if (o.getType() == ObjType.minecraft_dropped && e.getItemDrop().getItemStack().getType() == o.getParamType())
				o.add(e.getPlayer(), 1);		
	}
	
	@EventHandler
	public void onDrop(EntityPickupItemEvent e) {
		
		Plot plot = plugin.getPlotsManager().getPlot(e.getEntity().getLocation());
		
		if (plot == null)
			return;
		
		for (CbObjective o : plot.getCbData().getObjectives())
			if (o.getType() == ObjType.minecraft_picked_up && e.getItem().getItemStack().getType() == o.getParamType())
				o.add(e.getEntity(), 1);		
	}
	
	@EventHandler
	public void onFoodChange(FoodLevelChangeEvent e) {
		
		Plot plot = plugin.getPlotsManager().getPlot(e.getEntity().getLocation());
		
		if (plot == null)
			return;
		
		for (CbObjective o : plot.getCbData().getObjectives())
			if (o.getType() == ObjType.food)
				o.add(e.getEntity(), 1);
	}
	
	@EventHandler
	public void onCraft(CraftItemEvent e) {
		
		Plot plot = plugin.getPlotsManager().getPlot(e.getWhoClicked().getLocation());
		
		if (plot == null)
			return;
		
		for (CbObjective o : plot.getCbData().getObjectives())
			if (o.getType() == ObjType.minecraft_crafted && e.getRecipe().getResult().getType() == o.getParamType())
				o.add(e.getWhoClicked(), 1);
	}
	
	@EventHandler
	public void onItemUse(BlockPlaceEvent e) {
		
		Plot plot = plugin.getPlotsManager().getPlot(e.getPlayer().getLocation());
		
		if (plot == null)
			return;
		
		for (CbObjective o : plot.getCbData().getObjectives())
			if (o.getType() == ObjType.minecraft_used && e.getBlockPlaced() != null && e.getBlockPlaced().getType() == o.getParamType())
				o.add(e.getPlayer(), 1);
	}
	
	@EventHandler
	public void onItemBreak(PlayerItemBreakEvent e) {
		
		Plot plot = plugin.getPlotsManager().getPlot(e.getPlayer().getLocation());
		
		if (plot == null)
			return;
		
		for (CbObjective o : plot.getCbData().getObjectives())
			if (o.getType() == ObjType.minecraft_broken && e.getBrokenItem().getType() == o.getParamType())
				o.add(e.getPlayer(), 1);
	}
	
	@EventHandler
	public void onMineBlock(BlockBreakEvent e) {
		
		Plot plot = plugin.getPlotsManager().getPlot(e.getPlayer().getLocation());
		
		if (plot == null)
			return;

		for (CbObjective o : plot.getCbData().getObjectives())
			if (o.getType() == ObjType.minecraft_mined && e.getBlock().getType() == o.getParamType())
				o.add(e.getPlayer(), 1);		
	}
	
	@EventHandler
	public void onLevelChange(PlayerLevelChangeEvent e) {
		if (e.getNewLevel() <= e.getOldLevel())
			return;
		
		Plot plot = plugin.getPlotsManager().getPlot(e.getPlayer().getLocation());
		
		if (plot == null)
			return;
		
		for (CbObjective o : plot.getCbData().getObjectives())
			if (o.getType() == ObjType.level)
				o.add(e.getPlayer(), e.getNewLevel() - e.getOldLevel());
	}
	
	@EventHandler
	public void onExpChange(PlayerExpChangeEvent e) {
		
		Plot plot = plugin.getPlotsManager().getPlot(e.getPlayer().getLocation());
		
		if (plot == null)
			return;
		
		for (CbObjective o : plot.getCbData().getObjectives())
			if (o.getType() == ObjType.xp)
				o.add(e.getPlayer(), e.getAmount());
	}
	
}
