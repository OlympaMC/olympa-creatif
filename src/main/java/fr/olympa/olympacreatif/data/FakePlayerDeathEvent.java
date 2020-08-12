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
	
	Location death;
	Location respawn;
	
	List<ItemStack> drops = new ArrayList<ItemStack>();
	
	public FakePlayerDeathEvent(Player p, Entity killer, Plot deathPlot) {
		this.killer = killer;
		this.p = p;
		this.plot = deathPlot;
		this.death = p.getLocation();
		
		for (PotionEffect pot : p.getActivePotionEffects())
			p.removePotionEffect(pot.getType());
		
		if (plot == null)
			respawn = OlympaCreatifMain.getMainClass().getWorldManager().getWorld().getSpawnLocation();
		
		else {
			respawn = plot.getParameters().getSpawnLoc(OlympaCreatifMain.getMainClass());
			
			if (!(boolean) plot.getParameters().getParameter(PlotParamType.KEEP_INVENTORY_ON_DEATH)) {
				p.getInventory().forEach(item -> {if (item != null) drops.add(item);});
				
				p.getInventory().clear();
			}
		}
		
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
	
	public Location getDeathLoc() {
		return death;
	}
	
	public Location getRespawnLoc() {
		return respawn;
	}
	
	public List<ItemStack> getDrops(){
		return drops;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
