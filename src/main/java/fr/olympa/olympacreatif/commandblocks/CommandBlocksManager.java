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
import fr.olympa.olympacreatif.data.PermissionsList;
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
	
	public final static int maxTeamsPerPlot = 20;
	public final static int maxScoreboardsPerPlot = 20;
	
	public CommandBlocksManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;

		plugin.getServer().getPluginManager().registerEvents(new CbObjectivesListener(plugin), plugin);
		plugin.getServer().getPluginManager().registerEvents(new CbTeamsListener(plugin), plugin);
		
		Bukkit.getPluginManager().registerEvents(new CbCommandListener(plugin), plugin);
	}
	
	public PlotCommandBlockData createPlotCbData() {
		if (unusedScoreboards.size() == 0)
			return new PlotCommandBlockData(plugin, Bukkit.getScoreboardManager().getNewScoreboard());
		else
			return new PlotCommandBlockData(plugin, unusedScoreboards.remove(0));
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
		
		if (scb != null) {
			if (scb.getObjective(DisplaySlot.BELOW_NAME) != null)
				scb.getObjective(DisplaySlot.BELOW_NAME).unregister();
			
			plotsScoreboards.remove(plot);
			unusedScoreboards.add(scb);	
		}
		
		//clear des teams
		if (plotTeams.containsKey(plot))
			for (CbTeam t : plotTeams.get(plot))
				t.executeDeletionActions();
		
		if (plot.getCbData().getBossBars() != null)
			for (CbBossBar bar : plot.getCbData().getBossBars().values())
				bar.getBar().removeAll();
	}
	
	//gestion des scoreboards (affichage sidebar/belowname)
	
	
	//Actions à exécuter en entrée et sortie de plot
	
	public void executeJoinActions(Plot toPlot, Player p) {
		
		p.setExp(0);
		
		OlympaPlayerCreatif pc = AccountProvider.get(p.getUniqueId());
		
		//maj belowName si un objectif y est positionné
		Scoreboard scb = toPlot.getCbData().getScoreboard();
		
		p.setScoreboard(scb);
		if (scb.getObjective(DisplaySlot.BELOW_NAME) != null)
			scb.getObjective(DisplaySlot.BELOW_NAME).getScore(p).setScore(0);
		
		//maj sidebar si on objectif y est positionné
		for (CbObjective obj : toPlot.getCbData().getObjectives()) {
			
			if (obj.getDisplaySlot() == DisplaySlot.SIDEBAR) {
				
				pc.setCustomScoreboardTitle(obj.getName());
				pc.setCustomScoreboardValues(obj.getValues(true));
			}	
		}
	}
	
	public void excecuteQuitActions(Plot fromPlot, Player p) {
		CbTeam team = fromPlot.getCbData().getTeamOf(p);
		if (team != null)
			team.removeMember(p);
		
		((OlympaPlayerCreatif) AccountProvider.get(p.getUniqueId())).clearCustomScoreboard();
		
		//maj du scoreboard (reset du score du scoreboard du plot et réaffectation du scoreboard
		p.getScoreboard().resetScores(p);
		p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		
		plugin.getPerksManager().getLinesOnHeadUtil().getLineDataWrapper(p).clearLines();
		for (PotionEffect eff : p.getActivePotionEffects())
			p.removePotionEffect(eff.getType());
		
		for (CbBossBar bar : fromPlot.getCbData().getBossBars().values())
			bar.getBar().removePlayer(p);
	}
}
