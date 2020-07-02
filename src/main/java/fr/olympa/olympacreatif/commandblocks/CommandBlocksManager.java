package fr.olympa.olympacreatif.commandblocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.scoreboard.sign.ScoreboardManager;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.perks.PlayerMultilineUtil.LineDataWrapper;
import fr.olympa.olympacreatif.plot.Plot;
import net.minecraft.server.v1_15_R1.MinecraftServer;
import net.minecraft.server.v1_15_R1.ItemFireworks.EffectType;

public class CommandBlocksManager {

	private OlympaCreatifMain plugin;
	private Map<Plot, List<CbCommand>> queuedCommands = new LinkedHashMap<Plot, List<CbCommand>>();

	private Map<Plot, List<CbObjective>> plotObjectives = new HashMap<Plot, List<CbObjective>>();
	private Map<Plot, List<CbTeam>> plotTeams = new HashMap<Plot, List<CbTeam>>();
	
	//scoreboards utilisés pour l'affichage du belowName
	private Map<Plot, Scoreboard> plotsScoreboards = new HashMap<Plot, Scoreboard>();
	//scoreboards inutilisés qui seront réaffectés au besoin à d'autres plots chargés ultérieurement
	private List<Scoreboard> unusedScoreboards = new ArrayList<Scoreboard>();
	
	private int maxTeamsPerPlot = 20;
	private int maxScoreboardsPerPlot = 20;
	
	public CommandBlocksManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;

		plugin.getServer().getPluginManager().registerEvents(new CbObjectivesListener(plugin), plugin);
		plugin.getServer().getPluginManager().registerEvents(new CbTeamsListener(plugin), plugin);
		
		Bukkit.getPluginManager().registerEvents(new CbCommandListener(plugin), plugin);
	}
	
	//création des variables nécessaires pour ce plot 
	public void registerPlot(Plot plot) {
		if (!queuedCommands.containsKey(plot))
			queuedCommands.put(plot, new ArrayList<CbCommand>());

		if (!plotObjectives.containsKey(plot))
			plotObjectives.put(plot, new ArrayList<CbObjective>());
		
		if (!plotTeams.containsKey(plot))
			plotTeams.put(plot, new ArrayList<CbTeam>());
		
		if (unusedScoreboards.size() == 0) 
			plotsScoreboards.put(plot, Bukkit.getServer().getScoreboardManager().getNewScoreboard());
		else 
			plotsScoreboards.put(plot, unusedScoreboards.remove(0));
		
	}
	
	//unload les données du plot pour libérer de la mémoire
	public void unregisterPlot(Plot plot) {
		queuedCommands.remove(plot);
		plotObjectives.remove(plot);
		plotTeams.remove(plot);
		
		//met de côté le scoreboard pour un usage ultérieur
		Scoreboard scb = plotsScoreboards.get(plot);
		if (scb.getObjective(DisplaySlot.BELOW_NAME) != null)
			scb.getObjective(DisplaySlot.BELOW_NAME).unregister();
		
		plotsScoreboards.remove(plot);
		unusedScoreboards.add(scb);
		
		//clear des teams
		for (CbTeam t : plotTeams.get(plot))
			t.executeDeletionActions();
		
		for (BossBar bar : plot.getBossBars().values())
			bar.removeAll();
	}
	
	//gestion des scoreboards (affichage sidebar/belowname)
	
	//macimum 20 objectifs par plot
	public boolean registerObjective(Plot plot, CbObjective obj) {

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
		if (plot == null)
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
	
	public Objective getObjectiveBelowName(Plot plot) {		
		Scoreboard scb = plotsScoreboards.get(plot);
		if (scb.getObjective(DisplaySlot.BELOW_NAME) != null)
			return scb.getObjective(DisplaySlot.BELOW_NAME);
		else {
			Objective obj = scb.registerNewObjective("belowName", "dummy", "à spécifier");
			obj.setDisplaySlot(DisplaySlot.BELOW_NAME);
			
			return obj;
		}
	}
	
	public void clearBelowName(Plot plot) {
		if (plotsScoreboards.get(plot).getObjective(DisplaySlot.BELOW_NAME) != null)
			plotsScoreboards.get(plot).getObjective(DisplaySlot.BELOW_NAME).unregister();
	}
	
	//gestion des équipes
	//ajoute la team à la liste du plot. Max teams autorisées : maxTeamsPerPlot
	public boolean registerTeam(Plot plot, CbTeam team) {
		
		//si une team avec ce nom existe déjà, return
		for (CbTeam t : getTeams(plot))
			if (t.getId().equals(team.getId()))
				return false;
		
		plotTeams.get(plot).add(team);
		
		if (plotTeams.get(plot).size() > maxTeamsPerPlot)
			plotTeams.get(plot).remove(0);
		
		return true;
	}
	
	//renvoie la liste des équipes d'un plot
	public List<CbTeam> getTeams(Plot plot){
		if (plot == null)
			return new ArrayList<CbTeam>();
		
		return plotTeams.get(plot);
	}
	
	public CbTeam getTeamOf(Plot plot, Entity e) {
		
		for (CbTeam t : getTeams(plot))
			if (t.getMembers().contains(e)) 
				return t;			
				
		return null;
	}

	public CbTeam getTeamById(Plot plot, String teamId) {
		for (CbTeam t : getTeams(plot))
			if (t.getId().equals(teamId)) 
				return t;			
				
		return null;
	}
	
	
	//Actions à exécuter en entrée et sortie de plot
	
	public void executeJoinActions(Plot toPlot, Player p) {
		
		p.setExp(0);
		
		OlympaPlayerCreatif pc = AccountProvider.get(p.getUniqueId());
		
		//maj belowName si un objectif y est positionné
		Scoreboard scb = plotsScoreboards.get(toPlot);
		
		p.setScoreboard(plotsScoreboards.get(toPlot));
		if (scb.getObjective(DisplaySlot.BELOW_NAME) != null)
			scb.getObjective(DisplaySlot.BELOW_NAME).getScore(p).setScore(0);
		
		//maj sidebar si on objectif y est positionné
		for (CbObjective obj : getObjectives(toPlot)) {
			
			if (obj.getDisplaySlot() == DisplaySlot.SIDEBAR) {
				
				pc.setCustomScoreboardTitle(obj.getName());
				pc.setCustomScoreboardValues(obj.getValues(true));
			}	
		}
	}
	
	public void excecuteQuitActions(Plot fromPlot, Player p) {
		CbTeam team = getTeamOf(fromPlot, p);
		if (team != null)
			team.removeMember(p);
		
		((OlympaPlayerCreatif) AccountProvider.get(p.getUniqueId())).clearCustomScoreboard();
		
		//maj du scoreboard (reset du score du scoreboard du plot et réaffectation du scoreboard
		p.getScoreboard().resetScores(p);
		p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		
		plugin.getPerksManager().getLinesOnHeadUtil().getLineDataWrapper(p).clearLines();
		for (PotionEffect eff : p.getActivePotionEffects())
			p.removePotionEffect(eff.getType());
		
		for (BossBar bar : fromPlot.getBossBars().values())
			bar.removePlayer(p);
	}
}
