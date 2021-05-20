package fr.olympa.olympacreatif.plot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers.NBT;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import fr.olympa.api.holograms.Hologram;
import fr.olympa.api.holograms.Hologram.HologramLine;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCparam;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.Position;
import fr.olympa.olympacreatif.plot.PlotPerm.PlotRank;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagList;
import net.minecraft.server.v1_16_R3.NBTTagString;

public class PlotsManager {

	//private final Set<Plot> forceLoadedPlots = new HashSet<Plot>();
	
	private Set<Hologram> serverHolos = new HashSet<Hologram>();
	
	public static final int maxPlotsPerPlayer = 36;
	
	private OlympaCreatifMain plugin;
	
	//contienty les plots chargés pour les membres du plot qui sont en ligne
	private Map<Integer, Plot> loadedPlots = new HashMap<Integer, Plot>();
	
	//contient les plots chargés pour les visiteurs
	/*private static Cache<Integer, Plot> visitorPlots = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES)
			.removalListener(removed -> plugin.getTask().runTask(() -> {
				if (((Plot)removed.getValue()).getPlayers().size() > 0)
					visitorPlots.put(removed.getKey(), removed.getValue());
			})).build();*/
	//private Vector<AsyncPlot> asyncPlots = new Vector<AsyncPlot>();
	
	private static Cache<UUID, PlotId> entityBirthPlotCache = CacheBuilder.newBuilder().expireAfterAccess(31, TimeUnit.SECONDS).build();
	
	private int plotCount;
	
	public PlotsManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;

		plugin.getServer().getPluginManager().registerEvents(new PlotsManagerListener(plugin), plugin);
		plugin.getServer().getPluginManager().registerEvents(new PlotsInstancesListener(plugin), plugin);
		
		//construit les objets Plot chargés depuis la bdd de manière synchrone avec le serveur
		/*new BukkitRunnable() {
			
			@Override
			public void run() {
				synchronized (asyncPlots) {
					for (AsyncPlot ap : asyncPlots)
						loadPlot(ap);
						
					asyncPlots.clear();	
				}
			}
		}.runTaskTimer(plugin, 20, 1);*/
		
		//libère la RAM des plots non utilisés (càd aucun membre connecté et aucun joueur dessus)
		/*new BukkitRunnable() {
			
			@Override
			public void run() {
				//pour tous les plots
				synchronized (loadedPlots) {
					 
					//Bukkit.broadcastMessage("Loaded plots: " + loadedPlots);
					
					for (Plot plot : new HashSet<Plot>(loadedPlots.values())) {
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
								plugin.getDataManager().savePlot(plot, false);
								loadedPlots.remove(plot.getId().getId());
							}
						}	
					}
				}
			}
		}.runTaskTimer(plugin, delayBetweenPlotsCheckup, delayBetweenPlotsCheckup);*/
		
		
		//kill les entités en dehors de leur plot attitré
		/*new BukkitRunnable() {
			
			@Override
			public void run() {
				//Map<Entity, Plot> listToRemove = new HashMap<Entity, Plot>();		
				
				//parcours les entités du monde. Si elles sont en dehors de leur plot, elles sont supprimées
				if (plugin.getWorldManager().getWorld() != null)
					plugin.getWorldManager().getWorld().getEntities().forEach(e ->{
						if (e.getType() == EntityType.PLAYER)
							return;
						
						PlotId id = getBirthPlot(e);
						
						if (id == null)
							e.remove();
						else if (!id.equals(PlotId.fromLoc(plugin, e.getLocation()))) {
							Plot plot = plugin.getPlotsManager().getPlot(id);
							
							if (plot != null)
								plot.removeEntityInPlot(e, true);
							else
								e.remove();
						}
					});
			}
		}.runTaskTimer(plugin, 10, 300);*/
	}
	
	/**
	 * Return the birth plot of an entity
	 * @param e
	 * @return
	 */
	public synchronized PlotId getBirthPlot(Entity e) {
		if (e.getType() == EntityType.PLAYER)
			return null;
		
		PlotId id = entityBirthPlotCache.getIfPresent(e.getUniqueId());
		
		if (id != null)
			return id;
		
		NBTTagCompound tag = new NBTTagCompound();
		((CraftEntity)e).getHandle().save(tag);
		
		//System.out.println("birth plot of "  + e + " " + tag.asString() + " : " + tag.getList("Tags", NBT.TAG_STRING).getString(0));
		
		if (tag != null && tag.hasKey("Tags")) {
			NBTTagList list = tag.getList("Tags", NBT.TAG_STRING);
			if (list != null && list.size() > 0)
				id = PlotId.fromString(plugin, list.getString(0));
		}
		
		entityBirthPlotCache.put(e.getUniqueId(), id);
		return id;
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
		if (id == null || isPlotLoaded(id))
			return;
		
		//si le plot existe mais n'est pas encore chargé, chargement depuis la bdd
		plugin.getDataManager().loadPlot(id, false);
	}
	
	public boolean isPlotLoaded(PlotId id) {
		return loadedPlots.containsKey(id.getId());
	}
	
	
	public Plot createNewPlot(OlympaPlayerCreatif pc) {
		Plot plot = new Plot(plugin, pc);
		
		plugin.getDataManager().savePlot(plot, true);
		loadedPlots.put(plot.getId().getId(), plot);
		return plot;
	}
	
	public synchronized Set<Plot> getPlots(){
		return new HashSet<Plot>(loadedPlots.values());
	}

	public Plot getPlot(Location loc) {
		if (loc == null)
			return null;

		PlotId id = PlotId.fromLoc(plugin, loc);
		return id == null ? null : loadedPlots.get(id.getId());
	}
	
	public void unloadPlot(Plot plot) {
		plot.unload();
		plugin.getDataManager().savePlot(plot, false);
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
		return loadedPlots.get(id == null ? null : id.getId());
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
	
	/*
	public void addAsyncPlot(AsyncPlot plot) {
		if (plot != null)
			synchronized (plot) {
				asyncPlots.add(plot);	
			}
	}*/
	
	public void loadPlot(AsyncPlot ap) {
		if (!isPlotLoaded(ap.getId())) {
			long init = System.currentTimeMillis();
			loadedPlots.put(ap.getId().getId(), new Plot(ap));
			long end = System.currentTimeMillis() - init;
			plugin.getLogger().info("§7Plot " + ap.getId() + " took " 
					+ (end <= 5 ? "§a" : end < 10 ? "§e" : end < 20 ? "§c" : "§4") 
					+ end + "ms §7to load.");
		}
	}

	public void loadHelpHolos() {
		//plugin.getDataManager().addPlotToLoadQueue(PlotId.fromLoc(plugin, OCparam.HOLO_HELP_1_LOC.get().toLoc()), false);
		//plugin.getDataManager().addPlotToLoadQueue(PlotId.fromLoc(plugin, OCparam.HOLO_HELP_2_LOC.get().toLoc()), false);

		setHelpHolo(OCparam.HOLO_HELP_1_LOC.get().toLoc(), OCparam.HOLO_HELP_1_TEXT.get());
		setHelpHolo(OCparam.HOLO_HELP_2_LOC.get().toLoc(), OCparam.HOLO_HELP_2_TEXT.get());
	}
	

	@SuppressWarnings("unchecked")
	private void setHelpHolo(Location loc, List<String> text) {
		Hologram holo = OlympaCore.getInstance().getHologramsManager()
				.createHologram(loc, false, false, true/*, text.stream().map(s -> new FixedLine<HologramLine>(s)).collect(Collectors.toSet()).toArray(FixedLine[]::new)*/);
		
		text.forEach(s -> holo.addLine(new FixedLine<HologramLine>(s)));
		//holo.getLines().forEach(HologramLine::updatePosition);
		serverHolos.add(holo);

		plugin.getLogger().info("§aHelp holo " + text.get(0) + " §aplaced at " + new Position(loc));
	}
	
	public void showHelpHolosTo(final Player p) {
		plugin.getTask().runTaskLater(() -> serverHolos.forEach(holo -> {
			holo.show(p);
			//System.out.println("Show holo " + holo.getLines() + " to " + p.getName());
		}), 15);
	}
}




