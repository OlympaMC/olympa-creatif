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
	
	private Map<Plot, Scoreboard> plotsScoreboards = new HashMap<Plot, Scoreboard>();
	
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
	
	
	//gestion sidebar scoreboards
	
	//renvoie le scoreboard affiché sur le slot désigné du plot choisi
	public Objective getObjectiveOnSlot(Plot plot, DisplaySlot slot) {
		if (!plotsScoreboards.containsKey(plot))
			createScoreboardHolder(plot);
		
		return plotsScoreboards.get(plot).getObjective(slot);
	}
	
	//crée le scoreboard pour le plot en paramètre
	private void createScoreboardHolder(Plot plot) {
		Scoreboard scb = Bukkit.getScoreboardManager().getNewScoreboard();

		Objective objSidebar = scb.registerNewObjective("sidebar", "dummy", "sidebar");
		objSidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		plotsScoreboards.put(plot, scb);
	}
	
	//vide le slot de scoreboard en paramètre
	public void clearScoreboardSlot(Plot plot, DisplaySlot displaySlot) {
		if (displaySlot == null)
			return;
		
		if (!plotsScoreboards.containsKey(plot))
			createScoreboardHolder(plot);
		
		//supression de l'ancien objectif et création d'un nouveau
		if (displaySlot == DisplaySlot.SIDEBAR) {
			plotsScoreboards.get(plot).getObjective(DisplaySlot.SIDEBAR).unregister();	
			Objective objSidebar = plotsScoreboards.get(plot).registerNewObjective("sidebar", "dummy", "sidebar");
			objSidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
		}
		
		//supression des textholders des joueurs et entités si nécessaire
		if (displaySlot == DisplaySlot.BELOW_NAME) {
			for (Player p : plot.getPlayers()){
				LineDataWrapper data = plugin.getPerksManager().getLinesOnHeadUtil().getLineDataWrapper(p);
				
				data.removeLine("score");
				if (data.getLinesCount() == 1)
					data.clearLines();
			}
		}
			
	}

	public Scoreboard getPlotScoreboard(Plot plot) {
		if (!plotsScoreboards.containsKey(plot)) {
			Scoreboard scb = Bukkit.getScoreboardManager().getNewScoreboard();
			scb.registerNewObjective("belowName", "dummy", "§4ERROR");

			plotsScoreboards.put(plot, scb);
		}
		
		return plotsScoreboards.get(plot);
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
	
	//renvoie la liste des équipes d'un plot
	public List<CbTeam> getTeams(Plot plot){
		if (plot == null || !plotTeams.containsKey(plot))
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
		
		if (plotsScoreboards.containsKey(toPlot))
			p.setScoreboard(plotsScoreboards.get(toPlot));
		
		for (CbObjective obj : getObjectives(toPlot))
			if (obj.getDisplaySlot() == DisplaySlot.BELOW_NAME)
				obj.set(p, 0);
	}
	
	public void excecuteQuitActions(Plot fromPlot, Player p) {
		CbTeam team = getTeamOf(fromPlot, p);
		if (team != null)
			team.removeMember(p);
		
		((OlympaPlayerCreatif) AccountProvider.get(p.getUniqueId())).getCustomScoreboardLines().clear();;
		p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		
		plugin.getPerksManager().getLinesOnHeadUtil().getLineDataWrapper(p).clearLines();
		for (PotionEffect eff : p.getActivePotionEffects())
			p.removePotionEffect(eff.getType());
	}
}
