package fr.olympa.olympacreatif.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotParamType;

public class FakePlayerDeathEvent extends Event{

    private static final HandlerList handlers = new HandlerList();
	
	Entity killer;
	Player p;
	Plot plot;
	
	List<ItemStack> drops = new ArrayList<ItemStack>();
	
	public static boolean fireFakeDeath(OlympaCreatifMain plugin, Player p, Entity killer, double damages, Plot deathPlot) {
		if (p.getHealth() > damages)
			return false;
		p.sendMessage("§7§oVous êtes mort !");
		plugin.getServer().getPluginManager().callEvent(new FakePlayerDeathEvent(plugin, p, killer, deathPlot));
		
		return true;
	}
	
	private FakePlayerDeathEvent(OlympaCreatifMain plugin, Player p, Entity killer, Plot deathPlot) {
		this.killer = killer;
		this.p = p;
		this.plot = deathPlot;
		
		Location respawnLoc = OCparam.SPAWN_LOC.get().toLoc();
		
		for (PotionEffect pot : p.getActivePotionEffects())
			p.removePotionEffect(pot.getType());
		
				
		if (plot != null) {
			respawnLoc = plot.getParameters().getSpawnLoc();
			
			if (!plot.getParameters().getParameter(PlotParamType.KEEP_INVENTORY_ON_DEATH)) {
				p.getInventory().forEach(item -> {if (item != null) plugin.getWorldManager().getWorld().dropItemNaturally(p.getLocation(), item);});
				
				p.getInventory().clear();
			}
		}
		
		p.teleport(respawnLoc);
		p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
		p.setFoodLevel(20);
	}
	
	public Player getPlayer() {
		return p;
	}
	
	public Entity getKiller() {
		return killer;
	}
	
	public Plot getPlot() {
		return plot;
	}
	/*
	public Location getDeathLoc() {
		return death;
	}
	
	
	public Location getRespawnLoc() {
		return respawn;
	}
	
	public List<ItemStack> getDrops(){
		return drops;
	}
	*/
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
