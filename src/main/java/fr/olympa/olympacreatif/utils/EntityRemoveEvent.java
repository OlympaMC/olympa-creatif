package fr.olympa.olympacreatif.utils;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;

public class EntityRemoveEvent extends Event{
	private static final HandlerList handlers = new HandlerList();
	
	private Entity ent;
	private Plot plot;
	
	public EntityRemoveEvent(OlympaCreatifMain plugin, Entity e) {
		this.ent = e;
		this.plot = plugin.getPlotsManager().getPlot(e.getLocation());
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

    public static HandlerList getHandlerList() {
        return handlers;
    }
	
	public Entity getEntity() {
		return ent;
	}
	
	public Plot getPlot() {
		return plot;
	}
}