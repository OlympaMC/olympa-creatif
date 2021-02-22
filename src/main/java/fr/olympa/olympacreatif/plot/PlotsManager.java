package fr.olympa.olympacreatif.plot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers.NBT;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.api.holograms.Hologram;
import fr.olympa.api.holograms.Hologram.HologramLine;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.plot.PlotPerm.PlotRank;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagList;
import net.minecraft.server.v1_16_R3.NBTTagString;

public class PlotsManager {

	private static final int delayBetweenPlotsCheckup = 20 * 30;
	public static final int maxPlotsPerPlayer = 36;
	
	private OlympaCreatifMain plugin;
	
	private Set<Plot> loadedPlots = new HashSet<Plot>();
	
	private Vector<AsyncPlot> asyncPlots = new Vector<AsyncPlot>();
	
	private int plotCount;
	
	public PlotsManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;

		plugin.getServer().getPluginManager().registerEvents(new PlotsManagerListener(plugin), plugin);
		plugin.getServer().getPluginManager().registerEvents(new PlotsInstancesListener(plugin), plugin);
		
		//construit les objets Plot chargés depuis la bdd de manière synchrone avec le serveur
		new BukkitRunnable() {
			
			@Override
			public void run() {
				synchronized (asyncPlots) {
					for (AsyncPlot ap : asyncPlots)
						loadPlot(ap);
						
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
							if (!hasMemberOnline && !plot.getPlotId().equals(PlotId.fromId(plugin, 1))) {
								plot.unload();
								plugin.getDataManager().addPlotToSaveQueue(plot, false);
								loadedPlots.remove(plot);
							}
						}	
					}
				}
			}
		}.runTaskTimer(plugin, 20, delayBetweenPlotsCheckup);
		
		
		//kill les entités en dehors de leur plot attitré
		new BukkitRunnable() {
			
			@Override
			public void run() {
				//Map<Entity, Plot> listToRemove = new HashMap<Entity, Plot>();		
				
				//parcours les entités du monde. Si elles sont en dehors de leur plot, elles sont supprimées
				if (plugin.getWorldManager().getWorld() != null)
					new ArrayList<Entity>(plugin.getWorldManager().getWorld().getEntities()).forEach(e ->{
						if (e.getType() == EntityType.PLAYER)
							return;
						
						PlotId id = getBirthPlot(e);
						
						if (id  == null || !id.equals(PlotId.fromLoc(plugin, e.getLocation()))) {
							Plot plot = plugin.getPlotsManager().getPlot(id);
							
							if (plot != null)
								plot.removeEntityInPlot(e, true);
							else
								e.remove();
						}
					});
			}
		}.runTaskTimerAsynchronously(plugin, 10, 300);
	}
	
	/**
	 * Return the birth plot of an entity
	 * @param e
	 * @return
	 */
	public synchronized PlotId getBirthPlot(Entity e) {
		if (e.getType() == EntityType.PLAYER)
			return null;
		
		net.minecraft.server.v1_16_R3.Entity ent = ((CraftEntity)e).getHandle();
		NBTTagCompound tag = new NBTTagCompound();
		ent.save(tag);
		
		//Bukkit.broadcastMessage("birth plot of "  + e + " " + tag.asString() + " : " + tag.getList("Tags", NBT.TAG_STRING).getString(0));
		
		if (tag == null || !tag.hasKey("Tags"))
			return null;
		
		NBTTagList list = tag.getList("Tags", NBT.TAG_STRING);
		
		if (list == null || list.size() == 0)
			return null;
		
		return PlotId.fromString(plugin, list.getString(0));
	}
	
	public void setBirthPlot(PlotId plot, Entity e) {
		if (e.getType() == EntityType.PLAYER)
			return;
		
		NBTTagCompound tag = new NBTTagCompound();
		net.minecraft.server.v1_16_R3.Entity ent = ((CraftEntity)e).getHandle();
		ent.save(tag);
		
		NBTTagList list = new NBTTagList();			
		list.add(NBTTagString.a(plot.toString()));
		tag.set("Tags", list);
		
		try {
			ent.load(tag);	
		}catch(Exception ex) {
			plugin.getLogger().log(Level.WARNING, "§cError set birth plot " + plot + " for " + tag.toString());
			ex.printStackTrace();
		}
	}

	
	public void loadExistingPlot(PlotId id) {
		if (id == null)
			return;
		
		if (isPlotLoaded(id))
			return;
		
		//si le plot existe mais n'est pas encore chargé, chargement depuis la bdd
		plugin.getDataManager().addPlotToLoadQueue(id, false);
	}
	
	public boolean isPlotLoaded(PlotId id) {
		for (Plot plot : loadedPlots)
			if (plot.getPlotId().equals(id))
				return true;
		
		return false;
	}
	
	
	public Plot createNewPlot(OlympaPlayerCreatif pc) {
		Plot plot = new Plot(plugin, pc);
		
		plugin.getDataManager().addPlotToSaveQueue(plot, false);
		loadedPlots.add(plot);
		return plot;
	}
	
	public synchronized Set<Plot> getPlots(){
		return new HashSet<Plot>(loadedPlots);
	}

	public Plot getPlot(Location loc) {
		if (loc == null)
			return null;
		
		PlotId id = PlotId.fromLoc(plugin, loc);
		
		if (id == null)
			return null;
		
		for (Plot plot : loadedPlots)
			if (plot.getPlotId().equals(id))
				return plot;
		
		return null;
	}

	/*
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
	}*/
	
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

	public void setTotalPlotCount(int plotsCount) {
		this.plotCount = plotsCount;
	}
	
	public void addAsyncPlot(AsyncPlot plot) {
		if (plot != null)
			synchronized (plot) {
				asyncPlots.add(plot);	
			}
	}
	
	private void loadPlot(AsyncPlot ap) {
		if (!isPlotLoaded(ap.getId())) 
			loadedPlots.add(new Plot(ap));
	}
}
