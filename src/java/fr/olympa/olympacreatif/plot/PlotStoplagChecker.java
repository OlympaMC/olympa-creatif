package fr.olympa.olympacreatif.plot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.checkerframework.checker.index.qual.HasSubsequence;

import fr.olympa.api.spigot.config.CustomConfig;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import net.minecraft.server.v1_16_R3.MinecraftServer;

public class PlotStoplagChecker {

	public static final int eventRetentionDuration = 100; //in ticks
	
	public static final int forcedStoplagPeriodDuration = 200;
	public static final int forcedStoplagStoplagCount = 3;
	
	/*static {
		OlympaCreatifMain.getInstance().getTask().scheduleSyncRepeatingTask(
				() -> OlympaCreatifMain.getInstance().getPlotsManager().getPlots().forEach(plot -> plot.getStoplagChecker().resetHistory()), 
				5, 5, TimeUnit.SECONDS);
	}*/
	
	private OlympaCreatifMain plugin;
	private Plot plot;
	
	private Map<StopLagDetect, Integer> detections = new HashMap<PlotStoplagChecker.StopLagDetect, Integer>();
	private int stoplagCount = 0;
	private int forcedStoplagResetTick = MinecraftServer.currentTick + forcedStoplagPeriodDuration;
	
	public PlotStoplagChecker(OlympaCreatifMain plugin, Plot plot) {
		this.plugin = plugin;
		this.plot = plot;
		
		for (StopLagDetect sld : StopLagDetect.values())
			detections.put(sld, 0);
	}
	
	/*private void resetHistory() {
		for (StopLagDetect sld : StopLagDetect.values())
			detections.put(sld, 0);
	}*/

	public void addEvent(StopLagDetect type) {
		if (plot.hasStoplag())
			return;
		
		int detects = detections.get(type) + 1;
		
		detections.put(type, detects);
		plugin.getTask().runTaskLater(() -> detections.put(type, detections.get(type) - 1), eventRetentionDuration);
		
		if (detects >= type.getMaxPerPeriod())
			fireStopLag(type);
	}
	
	private void fireStopLag(StopLagDetect type) {
		//detections.put(type, 0);
		
		if (forcedStoplagResetTick < Bukkit.getCurrentTick())
			stoplagCount = 1;
		else
			stoplagCount++;
		
		forcedStoplagResetTick = Bukkit.getCurrentTick() + forcedStoplagPeriodDuration;
		
		//si le nombre d'avertissement a été dépassé, mise en stoplag forcée
		if (stoplagCount < forcedStoplagStoplagCount) {
			PlotParamType.STOPLAG_STATUS.setValue(plot, 1);	
			
			//message
			plot.getPlayers().forEach(p -> OCmsg.PLOT_STOPLAG_FIRED.send(p, type));
		}
		else {
			PlotParamType.STOPLAG_STATUS.setValue(plot, 2);
			
			//message
			plot.getPlayers().forEach(p -> OCmsg.PLOT_FORCED_STOPLAG_FIRED.send(p, type));
		}
	}
	
	public double getScore() {
		return detections.entrySet().stream().mapToDouble(e -> ((double) e.getValue() / (double) e.getKey().max) / StopLagDetect.values().length).sum() * 100;
	}
	
	public double getScore(StopLagDetect lag) {
		return (double) detections.get(lag) / lag.getMaxPerPeriod();
	}

	public enum StopLagDetect {//les valeurs sont par seconde
		PISTON(400, "pistons"),
		LAMP(250, "lampes de redstone"),
		LIQUID(750, "liquides fluides"),
		ENTITY(400, "spawn entités"),
		WIRE(4000, "systèmes de redstone"),
		UNKNOWN(1, "inconnu")
		;
		
		private int defaultMax;
		private int max;
		private String name;
		
		StopLagDetect(int maxPerPeriod, String name) {
			this.name = name;
			this.defaultMax = maxPerPeriod;
			
			YamlConfiguration config = OlympaCreatifMain.getInstance().getConfig();
			if (!config.contains("stoplag_limit." + toString())) {
				config.set("stoplag_limit." + toString(), defaultMax);
				OlympaCreatifMain.getInstance().saveConfig();
			}

			this.max = config.getInt("stoplag_limit." + toString());
		}
		
		public int getMaxPerPeriod() {
			return max;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public static void reloadConfig() {
			YamlConfiguration config = OlympaCreatifMain.getInstance().getConfig();
			OlympaCreatifMain.getInstance().reloadConfig();
			
			for (StopLagDetect lag : StopLagDetect.values()) {
				if (config.getInt("stoplag_limit." + lag.toString()) == 0) {
					config.set("stoplag_limit." + lag.toString(), lag.defaultMax);
				}
				lag.max = config.getInt("stoplag_limit." + lag.toString());
			}
			
			OlympaCreatifMain.getInstance().saveConfig();
		}
	}
}
