package fr.olympa.olympacreatif.plot;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftMagicNumbers.NBT;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;
import fr.olympa.olympacreatif.world.WorldManager;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.NBTTagList;

public class PlotsManager {

	private final int delayBetweenCheckup = 20 * 30;
	public static final int maxPlotsPerPlayer = 36;
	
	private OlympaCreatifMain plugin;
	
	private Set<Plot> loadedPlots = new HashSet<Plot>();
	
	private List<AsyncPlot> asyncPlots = new Vector<AsyncPlot>();
	
	private int plotCount;
	
	public PlotsManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;

		plugin.getServer().getPluginManager().registerEvents(new PlotsManagerListener(plugin), plugin);
		plugin.getServer().getPluginManager().registerEvents(new PlotsInstancesListener(plugin), plugin);
		
		plotCount = plugin.getDataManager().getPlotsCount();
		
		//construit les objets Plot chargés depuis la bdd de manière synchrone avec le serveur
		new BukkitRunnable() {
			
			@Override
			public void run() {
				synchronized (asyncPlots) {
					for (AsyncPlot ap : asyncPlots)
						loadedPlots.add(new Plot(ap));
					
					asyncPlots.clear();	
				}
			}
		}.runTaskTimer(plugin, 20, 1);
		
		//libère la RAM des plots non utilisés (càd aucun membre connecté et aucun joueur dessus)
		new BukkitRunnable() {
			
			@Override
			public void run() {
				//pour tous les plots
				synchronized (loadedPlots) {
					
					//Bukkit.broadcastMessage("Loaded plots: " + loadedPlots);
					
					for (Plot plot : new ArrayList<Plot>(loadedPlots)) {
						boolean hasMemberOnline = false;
						
						//s'il n'y a aucun joueur sur la parcelle
						if (plot.getPlayers().size() == 0) {
							//si aucun membre n'est en ligne, unload de la parcelle
							for (Player p : Bukkit.getOnlinePlayers()) {
								if (plot.getMembers().getPlayerRank(p) != PlotRank.VISITOR) {
									hasMemberOnline = true;
								}
							}
							if (!hasMemberOnline) {
								plot.unload();
								plugin.getDataManager().savePlot(plot, true);
								loadedPlots.remove(plot);
							}
						}	
					}
				}
			}
		}.runTaskTimer(plugin, 20, delayBetweenCheckup);
		
		
		//kill les entités en dehors de leur plot attitré
		new BukkitRunnable() {
			
			@Override
			public void run() {
				Map<Entity, Plot> listToRemove = new HashMap<Entity, Plot>();		
				
				new ArrayList<Entity>(plugin.getWorldManager().getWorld().getEntities()).forEach(e ->{
					if (e.getType() == EntityType.PLAYER)
						return;
					
					PlotId id = getPlotIdFromTagOf(e);
					
					if (id  == null || !id.equals(PlotId.fromLoc(plugin, e.getLocation())))
						listToRemove.put(e, plugin.getPlotsManager().getPlot(id));
				});
				//remove entités
				plugin.getTask().runTask(()-> listToRemove.forEach((e, plot) -> {
					if (plot != null)
						plot.removeEntityInPlot(e, true);
					else
						e.remove();
				}));
			}
		}.runTaskTimerAsynchronously(plugin, 10, 100);
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				/*
				for (Plot plot : plugin.getPlotsManager().getPlots())
					for (Entity e : plot.getEntities())
						if (e.isDead())
							plot.removeEntityInPlot(e, true);
				*/
					plugin.getPlotsManager().getPlots().forEach(plot -> plot.getEntities().forEach(e -> {
					if (e.isDead())
						plot.removeEntityInPlot(e, true);
				}));
			}
		}.runTaskTimerAsynchronously(plugin, 10, 1);
	}
	
	public synchronized PlotId getPlotIdFromTagOf(Entity e) {
		if (e.getType() == EntityType.PLAYER)
			return null;
		
		net.minecraft.server.v1_15_R1.Entity ent = ((CraftEntity)e).getHandle();
		NBTTagCompound tag = new NBTTagCompound();
		ent.c(tag);
		
		if (tag == null || !tag.hasKey("Tags"))
			return null;
		
		NBTTagList list = tag.getList("Tags", NBT.TAG_STRING);
		
		if (list == null || list.size() == 0)
			return null;
		
		return PlotId.fromString(plugin, list.getString(0));
	}

	
	public void loadExistingPlot(PlotId newId) {
		if (newId == null)
			return;
		
		for (Plot plot : loadedPlots)
			if (plot.getPlotId().equals(newId))
				return;
		
		//si le plot existe mais n'est pas encore chargé, chargement depuis la bdd
		plugin.getDataManager().loadPlot(newId);
	}
	
	
	
	public Plot createPlot(Player p) {
		Plot plot = new Plot(plugin, AccountProvider.get(p.getUniqueId()));
				
		loadedPlots.add(plot);
		return plot;
	}
	
	public synchronized Set<Plot> getPlots(){
		return new HashSet<Plot>(loadedPlots);
	}

	public Plot getPlot(Location loc) {
		PlotId id = PlotId.fromLoc(plugin, loc);
		
		if (id == null)
			return null;
		
		for (Plot plot : loadedPlots)
			if (plot.getPlotId().equals(id))
				return plot;
		
		return null;
	}

	public List<Plot> getPlotsOf(Player p, boolean onlyOwnedPlots) {
		List<Plot> list = new ArrayList<Plot>();
		
		for (Plot plot : loadedPlots) {
			PlotRank rank = plot.getMembers().getPlayerRank(p);
			if (onlyOwnedPlots) {
				if (rank == PlotRank.OWNER)
					list.add(plot);	
			}else
				if (rank != PlotRank.VISITOR)
					list.add(plot);
		}
		
		return list;
	}
	
	public Plot getPlot(PlotId id) {
		if (id == null)
			return null;
		
		for (Plot plot : loadedPlots)
			if (plot.getPlotId().equals(id))
				return plot;
		return null;
	}
	
	
	public void incrementTotalPlotCount() {
		plotCount++;
	}
	
	public int getTotalPlotCount() {
		return plotCount;
	}
	
	public void addAsyncPlot(AsyncPlot plot, PlotId plotId) {
		if (plot != null)
			synchronized (asyncPlots) {
				asyncPlots.add(plot);
			}
	}
	
	public static Integer getPlotIdFromString(String id) {
		try {
			return Integer.valueOf(id, 36);
		}catch(NumberFormatException e) {
			return null;
		}
	}
	
	public static String getPlotIdAsString(int id) {
		return Integer.toString(id, 36).toUpperCase();
	}
}
