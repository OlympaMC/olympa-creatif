package fr.olympa.olympacreatif.commandblocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Entity;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.CbTeam.ColorType;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand;

public class PlotCbData {

	private OlympaCreatifMain plugin;
	private List<CbCommand> queuedCommands = new ArrayList<CbCommand>();
	private List<CbObjective> objectives = new ArrayList<CbObjective>();
	private List<CbTeam> teams = new ArrayList<CbTeam>();
	
	private Map<String, CbBossBar> bossbarsMap = new HashMap<String, CbBossBar>();
	
	private int commandsLeft;
	private Scoreboard scb;
	
	PlotCbData(OlympaCreatifMain plugin, Scoreboard scb){
		this.plugin = plugin;
		this.scb = scb;
		commandsLeft = CommandBlocksManager.maxCommandsTicketst;
	}
	
	//GETTERS
	public Scoreboard getScoreboard() {
		return scb;
	}
	
	public List<CbObjective> getObjectives() {
		return objectives;
	}
	
	public CbObjective getObjective(String id) {
		for (CbObjective o : objectives)
			if (o.getId().equals(id))
				return o;
		return null;
	}
	
	public List<CbTeam> getTeams(){
		return teams;
	}
	
	public int getCommandsTicketsLeft() {
		return commandsLeft;
	}

	public void removeCommandTickets(int tickets) {
		commandsLeft -= tickets;
	}
	
	public void addCommandTickets( int tickets) {
		commandsLeft = Math.min(CommandBlocksManager.maxCommandsTicketst, commandsLeft + tickets);
	}
	
	public CbBossBar getBossBar(String id) {
		if (bossbarsMap.containsKey(id))
			return bossbarsMap.get(id);
		else
			return null;
	}

	//GESTION BOSSBAR
	public void addBossBar(String id, CbBossBar bar){
		if (!bossbarsMap.containsKey(id)) 
			bossbarsMap.put(id, bar);
	}
	
	public boolean removeBossBar(String id) {
		if (bossbarsMap.containsKey(id)) {
			bossbarsMap.get(id).getBar().removeAll();
			bossbarsMap.remove(id);
			return true;
		}else
			return false;
			
	}
	
	public Map<String, CbBossBar> getBossBars() {
		return Collections.unmodifiableMap(bossbarsMap);
	}
	
	
	//GESTION CbOBJECTIVES

	
	//macimum 20 objectifs par plot
	public boolean registerObjective(CbObjective obj) {

		//n'enregistre pas le scoreboard si un autre avec le même nom existe déjà dans le plot
		for (CbObjective o : objectives)
			if (o.getId().equals(obj.getId()))
				return false;
		
		objectives.add(obj);
		if (objectives.size() > CommandBlocksManager.maxObjectivesPerPlot)
			objectives.remove(0);
		
		return true;
	}
	
	public Objective getObjectiveBelowName() {
		if (scb.getObjective(DisplaySlot.BELOW_NAME) != null)
			return scb.getObjective(DisplaySlot.BELOW_NAME);
		else {
			Objective obj = scb.registerNewObjective("belowName", "dummy", "à spécifier");
			obj.setDisplaySlot(DisplaySlot.BELOW_NAME);
			
			return obj;
		}
	}
	
	public void clearBelowName() {
		if (scb.getObjective(DisplaySlot.BELOW_NAME) != null)
			scb.getObjective(DisplaySlot.BELOW_NAME).unregister();
	}
	
	//GESTION TEAMS
	//ajoute la team à la liste du plot. Max teams autorisées : maxTeamsPerPlot
	public boolean registerTeam(CbTeam team) {
		
		//si une team avec ce nom existe déjà, return
		for (CbTeam t : teams)
			if (t.getId().equals(team.getId()))
				return false;
		
		teams.add(team);
		
		if (teams.size() > CommandBlocksManager.maxTeamsPerPlot)
			teams.remove(0);
		
		return true;
	}
	
	public CbTeam getTeamOf(Entity e) {
		
		for (CbTeam t : teams)
			if (t.getMembers().contains(e)) 
				return t;			
				
		return null;
	}

	public CbTeam getTeamById(String teamId) {
		for (CbTeam t : teams)
			if (t.getId().equals(teamId)) 
				return t;			
				
		return null;
	}

	public CbTeam getTeamByColor(ColorType color) {
		for (CbTeam t : teams)
			if (t.getColor() == color) 
				return t;			
				
		return null;
	}
	
	public void unload() {
		for (CbObjective o : objectives) 
			o.clearDisplaySlot();
			
		for (CbTeam t : teams)
			for (Entity e : t.getMembers())
				t.removeMember(e);
		
		for (Entry<String, CbBossBar> e : bossbarsMap.entrySet())
			e.getValue().getBar().removeAll();

		queuedCommands.clear();
		objectives.clear();
		teams.clear();
		bossbarsMap.clear();
		
		if (scb.getObjective(DisplaySlot.BELOW_NAME) != null)
			scb.getObjective(DisplaySlot.BELOW_NAME).unregister();
		
		plugin.getCommandBlocksManager().addUnusedScoreboard(scb);
	}
	
}
