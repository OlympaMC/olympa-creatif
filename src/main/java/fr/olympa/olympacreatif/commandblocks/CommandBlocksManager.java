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
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.perks.PlayerMultilineUtil.LineDataWrapper;
import fr.olympa.olympacreatif.plot.Plot;
import net.minecraft.server.v1_15_R1.MinecraftServer;
import net.minecraft.server.v1_15_R1.ItemFireworks.EffectType;

public class CommandBlocksManager {

	private OlympaCreatifMain plugin;
	
	//scoreboards inutilisés qui seront réaffectés au besoin à d'autres plots chargés ultérieurement	 
	private List<Scoreboard> unusedScoreboards = new ArrayList<Scoreboard>();
	
	public static int maxTeamsPerPlot;
	public static int maxObjectivesPerPlot;
	
	public static int maxCommandsLeft;
	public static double perTickAddedCommandsLeft;
	public static int minTickBetweenEachCbExecution;
	
	public CommandBlocksManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;

		maxTeamsPerPlot = Integer.valueOf(Message.PARAM_CB_MAX_TEAMS_PER_PLOT.getValue());
		maxObjectivesPerPlot = Integer.valueOf(Message.PARAM_CB_MAX_OBJECTIVES_PER_PLOT.getValue());
		maxCommandsLeft = Integer.valueOf(Message.PARAM_CB_MAX_CMDS_LEFT.getValue());
		perTickAddedCommandsLeft = Double.valueOf(Message.PARAM_CB_PER_TICK_ADDED_CMDS.getValue());
		minTickBetweenEachCbExecution = Integer.valueOf(Message.PARAM_CB_MIN_TICKS_BETWEEN_EACH_CB_EXECUTION.getValue());

		plugin.getServer().getPluginManager().registerEvents(new CbObjectivesListener(plugin), plugin);
		plugin.getServer().getPluginManager().registerEvents(new CbTeamsListener(plugin), plugin);
		
		Bukkit.getPluginManager().registerEvents(new CbCommandListener(plugin), plugin);
	}
	
	public PlotCbData createPlotCbData() {
		if (unusedScoreboards.size() == 0)
			return new PlotCbData(plugin, Bukkit.getScoreboardManager().getNewScoreboard());
		else
			return new PlotCbData(plugin, unusedScoreboards.remove(0));
	}

	public void addUnusedScoreboard(Scoreboard scb) {
		unusedScoreboards.add(scb);
	}
	
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
