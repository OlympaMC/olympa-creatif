package fr.olympa.olympacreatif.commandblocks;

import java.util.LinkedHashMap;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.plot.Plot;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityStatus;

public class CommandBlocksManager {

	private OlympaCreatifMain plugin;
	//scoreboards inutilisés qui seront réaffectés au besoin à d'autres plots chargés ultérieurement	 
	//private List<Scoreboard> unusedScoreboards = new ArrayList<Scoreboard>();
	
	/*public static int maxTeamsPerPlot;
	public static int maxObjectivesPerPlot;
	
	public static int maxCommandsTicketst;

	public static int minTickBetweenEachCbExecution;
	public static int cmdTicketByCmdSetblock;*/
	
	public CommandBlocksManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;

		plugin.getServer().getPluginManager().registerEvents(new CbObjectivesListener(plugin), plugin);
		plugin.getServer().getPluginManager().registerEvents(new CbTeamsListener(plugin), plugin);
		
		Bukkit.getPluginManager().registerEvents(new CbCommandListener(plugin), plugin);
		
		//Bukkit.getServer().getPluginManager().getPermission("minecraft.command.gamemode").setDefault(PermissionDefault.TRUE);
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
				
				//pc.setCustomScoreboardTitle(obj.getName());
				pc.setCustomScoreboardLines(obj.getName(), (LinkedHashMap<String, Integer>) obj.getValues(true));
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

	public void setFakeOp(Player player, boolean setFakeOp) {
		EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
		if (setFakeOp)
			nmsPlayer.playerConnection.sendPacket(new PacketPlayOutEntityStatus(nmsPlayer, (byte) 28));
		else
			nmsPlayer.playerConnection.sendPacket(new PacketPlayOutEntityStatus(nmsPlayer, (byte) 24));
	}
}
