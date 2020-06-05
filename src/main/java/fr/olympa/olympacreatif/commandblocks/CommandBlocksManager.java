package fr.olympa.olympacreatif.commandblocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand;
import fr.olympa.olympacreatif.plot.Plot;
import net.minecraft.server.v1_15_R1.MinecraftServer;
import net.minecraft.server.v1_15_R1.ItemFireworks.EffectType;

public class CommandBlocksManager {

	private OlympaCreatifMain plugin;
	private Map<Plot, List<CbCommand>> queuedCommands = new LinkedHashMap<Plot, List<CbCommand>>(); 

	private Map<Plot, List<CbObjective>> plotObjectives = new HashMap<Plot, List<CbObjective>>();
	private Map<Plot, List<CbTeam>> plotTeams = new HashMap<Plot, List<CbTeam>>();
	
	private Map<Plot, Scoreboard> plotsScoreboards = new HashMap<Plot, Scoreboard>();

	private Map<Plot, List<Entity>> plotTeamNameHolders = new HashMap<Plot, List<Entity>>();
	private Map<Plot, Integer> plotLastTestedTick = new HashMap<Plot, Integer>();
	
	private int maxTeamsPerPlot = 20;
	private int maxScoreboardsPerPlot = 20;
	
	public CommandBlocksManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;

		plugin.getServer().getPluginManager().registerEvents(new CbObjectivesListener(plugin), plugin);
		plugin.getServer().getPluginManager().registerEvents(new CbTeamsListener(plugin), plugin);
		
		Bukkit.getPluginManager().registerEvents(new CbCommandListener(plugin), plugin);
	}
	
	public void registerPlot(Plot plot) {
		if (!queuedCommands.containsKey(plot))
			queuedCommands.put(plot, new ArrayList<CbCommand>());

		if (!plotObjectives.containsKey(plot))
			plotObjectives.put(plot, new ArrayList<CbObjective>());
		
		if (!plotTeams.containsKey(plot))
			plotTeams.put(plot, new ArrayList<CbTeam>());
	}
	
	public void unregisterPlot(Plot plot) {
		queuedCommands.remove(plot);
		plotObjectives.remove(plot);
		plotTeams.remove(plot);
	}
	
	//gestion des scoreboards (affichage sidebar/belowname)
	
	//macimum 20 objectifs par plot
	public boolean registerObjective(Plot plot, CbObjective obj) {
		
		//créée la liste d'objectifs si n'existe pas encore
		if (!plotObjectives.containsKey(plot))
			plotObjectives.put(plot, new ArrayList<CbObjective>());

		//n'enregistre pas le scoreboard si un autre avec le même nom existe déjà dans le plot
		for (CbObjective o : plotObjectives.get(plot))
			if (o.getId().equals(obj.getId()))
				return false;
		
		plotObjectives.get(plot).add(obj);
		if (plotObjectives.get(plot).size() > maxScoreboardsPerPlot)
			plotObjectives.get(plot).remove(0);
		
		return true;
	}
	
	public List<CbObjective> getObjectives(Plot plot){
		if (plot == null || !plotObjectives.containsKey(plot))
			return new ArrayList<CbObjective>();
		else
			return plotObjectives.get(plot);
	}
	
	public CbObjective getObjective(Plot plot, String objName) {
		
		objName = ChatColor.translateAlternateColorCodes('&', objName);
		
		for (CbObjective o : getObjectives(plot))
			if (o.getId().equals(objName))
				return o;
		
		return null;
	}
	
	//renvoie le scoreboard affiché sur le slot désigné du plot choisi
	public Objective getObjectiveOnSlot(Plot p, DisplaySlot slot) {
		if (!plotsScoreboards.containsKey(p))
			createScoreboardHolder(p);
		
		return plotsScoreboards.get(p).getObjective(slot);
	}
	
	private void createScoreboardHolder(Plot p) {
		Scoreboard scb = Bukkit.getScoreboardManager().getNewScoreboard();

		Objective objSidebar = scb.registerNewObjective("sidebar", "dummy", "sidebar");
		objSidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
		Objective objBelowName = scb.registerNewObjective("belowName", "dummy", "belowName");
		objBelowName.setDisplaySlot(DisplaySlot.BELOW_NAME);
		
		plotsScoreboards.put(p, scb);
	}
	
	@Deprecated
	private void removeScoreboard(Plot p) {
		plotsScoreboards.remove(p);
	}

	private boolean hasCustomScoreboard(Plot plotTo) {
		return plotsScoreboards.containsKey(plotTo);
	}

	public void setCustomScoreboardFor(Plot plot, Player p) {
		if (hasCustomScoreboard(plot))
			p.setScoreboard(plotsScoreboards.get(plot));
	}

	public void clearScoreboardSlot(Plot plot, DisplaySlot displayLoc) {
		if (displayLoc == null || !plotsScoreboards.containsKey(plot))
			return;
		
		Scoreboard scb = plotsScoreboards.get(plot);
		
		scb.getObjective(displayLoc).unregister();

		if (displayLoc == DisplaySlot.BELOW_NAME)
			scb.registerNewObjective("belowName", "dummy", "belowName");
		if (displayLoc == DisplaySlot.SIDEBAR)
			scb.registerNewObjective("sidebar", "dummy", "sidebar");
	}
	

	
	
	//gestion des équipes
	//ajoute la team à la liste du plot. Max teams autorisées : maxTeamsPerPlot
	public boolean registerTeam(Plot plot, CbTeam team) {

		//crée la liste des teams si inexistante
		if (!plotTeams.containsKey(plot))
			plotTeams.put(plot, new ArrayList<CbTeam>());
		
		//si une team avec ce nom existe déjà, return
		for (CbTeam t : getTeams(plot))
			if (t.getId().equals(team.getId()))
				return false;
		
		plotTeams.get(plot).add(team);
		
		if (plotTeams.get(plot).size() > maxTeamsPerPlot)
			plotTeams.get(plot).remove(0);
		
		return true;
	}
	
	////renvoie la liste des équipes d'un plot
	public List<CbTeam> getTeams(Plot plot){
		if (plot == null || !plotTeams.containsKey(plot))
			return new ArrayList<CbTeam>();
		
		return plotTeams.get(plot);
	}
	
	public CbTeam getTeamOfEntity(Plot plot, org.bukkit.entity.Entity e) {
		if (e instanceof Player)
			return getTeamOfString(plot, ((Player) e).getDisplayName());
		else
			return getTeamOfString(plot, e.getCustomName());
	}
	
	public CbTeam getTeamOfString(Plot plot, String memberName) {
		
		memberName = ChatColor.translateAlternateColorCodes('&', memberName);
		
		for (CbTeam t : getTeams(plot))
			if (t.getMembers().contains(memberName)) 
				return t;			
				
		return null;
	}

	public CbTeam getTeamById(Plot plot, String teamId) {
		for (CbTeam t : getTeams(plot))
			if (t.getId().equals(teamId)) 
				return t;			
				
		return null;
	}
	
	public void excecuteQuitActions(Plot fromPlot, Player p) {
		CbTeam team = getTeamOfString(fromPlot, p.getDisplayName());
		if (team != null)
			team.removeMember(p);
		
		for (PotionEffect eff : p.getActivePotionEffects())
			p.removePotionEffect(eff.getType());
	}
}
