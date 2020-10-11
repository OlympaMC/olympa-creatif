package fr.olympa.olympacreatif.plot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.CbBossBar;
import fr.olympa.olympacreatif.commandblocks.CbObjective;
import fr.olympa.olympacreatif.commandblocks.CbTeam;
import fr.olympa.olympacreatif.commandblocks.CommandBlocksManager;
import fr.olympa.olympacreatif.commandblocks.CbTeam.ColorType;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand;
import fr.olympa.olympacreatif.perks.UpgradesManager.UpgradeType;

public class PlotCbData {

	private OlympaCreatifMain plugin;
	private List<CbCommand> queuedCommands = new ArrayList<CbCommand>();
	private List<CbObjective> objectives = new ArrayList<CbObjective>();
	private List<CbTeam> teams = new ArrayList<CbTeam>();
	
	private Map<String, CbBossBar> bossbarsMap = new HashMap<String, CbBossBar>();
	
	private int commandsLeft;
	private Scoreboard scb;
	
	private int cpt; //commandes par tick ajoutées
	
	private boolean hasUnlockedSummonCmd;
	private boolean hasUnlockedSetblockSpawnerCmd;
	
	public PlotCbData(OlympaCreatifMain plugin, int cpt, boolean hasUnlockedSummonCmd, boolean hasUnlockedSetblockSpawnerCmd){
		this.plugin = plugin;
		this.cpt = cpt;
		
		this.hasUnlockedSetblockSpawnerCmd = hasUnlockedSummonCmd;
		this.hasUnlockedSummonCmd = hasUnlockedSummonCmd;
		
		commandsLeft = CommandBlocksManager.maxCommandsTicketst;
	}

	public void executeSynchronousInit() {
		scb = Bukkit.getScoreboardManager().getNewScoreboard();
	}
	
	//GETTERS
	public Scoreboard getScoreboard() {
		//if (scb == null)
			//scb = plugin.getCommandBlocksManager().getScoreboardForPlotCbData();
		
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
	
	public void addCommandTickets() {
		commandsLeft = Math.min(CommandBlocksManager.maxCommandsTicketst, commandsLeft + cpt);
	}
	
	public void setCpt(int newCpt) {
		cpt = newCpt;
	}
	
	public int getCpt() {
		return cpt;
	}
	
	public boolean hasUnlockedSpawnerSetblock() {
		return hasUnlockedSetblockSpawnerCmd;
	}
	
	public boolean hasUnlockedSummon() {
		return hasUnlockedSummonCmd;
	}
	
	public void unlockSpawnerSetblock() {
		hasUnlockedSetblockSpawnerCmd = true;
	}
	
	public void unlockSummon() {
		hasUnlockedSummonCmd = true;
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
		if (objectives.size() > CommandBlocksManager.maxObjectivesPerPlot) {
			objectives.remove(0).clearDisplaySlot();
		}
		
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

		if (queuedCommands != null)
			queuedCommands.clear();
		if (objectives != null)
			objectives.clear();
		if (teams != null)
			teams.clear();
		if (bossbarsMap != null)
			bossbarsMap.clear();
		
		/*
		if (scb != null) {
			if (scb.getObjective(DisplaySlot.BELOW_NAME) != null)
				scb.getObjective(DisplaySlot.BELOW_NAME).unregister();
			plugin.getCommandBlocksManager().addUnusedScoreboard(scb);
		}*/	
	}
}
