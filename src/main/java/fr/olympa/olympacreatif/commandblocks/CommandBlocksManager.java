package fr.olympa.olympacreatif.commandblocks;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.plot.Plot;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityStatus;

public class CommandBlocksManager {

	private OlympaCreatifMain plugin;
	
	//scoreboards inutilisés qui seront réaffectés au besoin à d'autres plots chargés ultérieurement	 
	//private List<Scoreboard> unusedScoreboards = new ArrayList<Scoreboard>();
	
	public static int maxTeamsPerPlot;
	public static int maxObjectivesPerPlot;
	
	public static int maxCommandsTicketst;

	public static int minTickBetweenEachCbExecution;
	public static int cmdTicketByCmdSetblock;
	
	public CommandBlocksManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;

		maxTeamsPerPlot = Integer.valueOf(Message.PARAM_CB_MAX_TEAMS_PER_PLOT.getValue());
		maxObjectivesPerPlot = Integer.valueOf(Message.PARAM_CB_MAX_OBJECTIVES_PER_PLOT.getValue());
		maxCommandsTicketst = Integer.valueOf(Message.PARAM_CB_MAX_CMDS_LEFT.getValue());
		
		minTickBetweenEachCbExecution = Integer.valueOf(Message.PARAM_CB_MIN_TICKS_BETWEEN_EACH_CB_EXECUTION.getValue());
		cmdTicketByCmdSetblock = Integer.valueOf(Message.PARAM_CB_COMMAND_TICKETS_CONSUMED_BY_SETBLOCK.getValue());

		plugin.getServer().getPluginManager().registerEvents(new CbObjectivesListener(plugin), plugin);
		plugin.getServer().getPluginManager().registerEvents(new CbTeamsListener(plugin), plugin);
		
		Bukkit.getPluginManager().registerEvents(new CbCommandListener(plugin), plugin);
		
		//Bukkit.getServer().getPluginManager().getPermission("minecraft.command.gamemode").setDefault(PermissionDefault.TRUE);
	}
	
	/*
	public Scoreboard getScoreboardForPlotCbData() {
		if (unusedScoreboards.size() == 0)
			return Bukkit.getScoreboardManager().getNewScoreboard();
		else
			return unusedScoreboards.remove(0);
	}

	public void addUnusedScoreboard(Scoreboard scb) {
		unusedScoreboards.add(scb);
	}*/
	
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
				pc.setCustomScoreboardLines(obj.getValues(true));
			}	
		}
	}
	
	public void excecuteQuitActions(Plot fromPlot, Player p) {
		CbTeam team = fromPlot.getCbData().getTeamOf(p);
		if (team != null)
			team.removeMember(p);
		
		((OlympaPlayerCreatif) AccountProvider.get(p.getUniqueId())).clearCustomSidebar();

		p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		
		for (PotionEffect eff : p.getActivePotionEffects())
			p.removePotionEffect(eff.getType());
		
		for (CbBossBar bar : fromPlot.getCbData().getBossBars().values())
			bar.getBar().removePlayer(p);
	}

	public void setFakeOp(Player player) {
		EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
		nmsPlayer.playerConnection.sendPacket(new PacketPlayOutEntityStatus(nmsPlayer, (byte) 28));
	}
}
