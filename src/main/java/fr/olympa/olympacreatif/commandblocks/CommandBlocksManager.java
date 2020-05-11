package fr.olympa.olympacreatif.commandblocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand;
import fr.olympa.olympacreatif.plot.Plot;

public class CommandBlocksManager {

	private OlympaCreatifMain plugin;
	private Map<Plot, List<CbCommand>> queuedCommands = new LinkedHashMap<Plot, List<CbCommand>>(); 

	private Map<Plot, List<CbObjective>> plotObjectives = new HashMap<Plot, List<CbObjective>>();
	private Map<Plot, List<CbTeam>> plotTeams = new HashMap<Plot, List<CbTeam>>();
	
	private Map<Plot, Scoreboard> plotsScoreboards = new HashMap<Plot, Scoreboard>();
	
	public CommandBlocksManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;

		plugin.getServer().getPluginManager().registerEvents(new CbObjectivesListener(plugin), plugin);
		plugin.getServer().getPluginManager().registerEvents(new CbTeamListener(plugin), plugin);
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
		//n'enregistre pas le scoreboard si un autre avec le même nom existe déjà dans le plot
		for (CbObjective o : plotObjectives.get(plot))
			if (o.getName().equals(obj.getName()))
				return false;
		
		plotObjectives.get(plot).add(obj);
		if (plotObjectives.get(plot).size()>20)
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
			if (o.getName().equals(objName))
				return o;
		
		return null;
	}
	
	//renvoie le scoreboard affiché sur le slot désigné du plot choisi
	public Objective getObjectiveOnSlot(Plot p, DisplaySlot slot) {
		if (!plotsScoreboards.containsKey(p))
			createScoreboard(p);
		
		return plotsScoreboards.get(p).getObjective(slot);
	}
	
	private void createScoreboard(Plot p) {
		Scoreboard scb = Bukkit.getScoreboardManager().getNewScoreboard();

		Objective objSidebar = scb.registerNewObjective("sidebar", "dummy", "sidebar");
		objSidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
		Objective objBelowName = scb.registerNewObjective("belowName", "dummy", "belowName");
		objBelowName.setDisplaySlot(DisplaySlot.BELOW_NAME);
		
		plotsScoreboards.put(p, scb);
	}
	
	@Deprecated
	public void removeScoreboard(Plot p) {
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
	public boolean registerTeam(Plot plot, CbTeam team) {
		for (CbTeam t : getTeams(plot))
			if (t.getId().equals(team.getId()))
				return false;
		
		plotTeams.get(plot).add(team);
		
		if (plotTeams.get(plot).size() > 20)
			plotTeams.get(plot).remove(0);
		
		return true;
	}
	public List<CbTeam> getTeams(Plot plot){
		if (plot == null || !plotTeams.containsKey(plot))
			return new ArrayList<CbTeam>();
		else
			return plotTeams.get(plot);
	}
	
	public CbTeam getTeamOfPlayer(Plot plot, String memberName) {
		
		memberName = ChatColor.translateAlternateColorCodes('&', memberName);
		
		for (CbTeam t : getTeams(plot))
			if (t.getMembers().contains(memberName)) 
				return t;			
				
		return null;
	}

	public CbTeam getTeam(Plot plot, String teamId) {
		for (CbTeam t : getTeams(plot))
			if (t.getId().equals(teamId)) 
				return t;			
				
		return null;
	}
}
