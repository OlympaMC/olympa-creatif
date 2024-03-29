package fr.olympa.olympacreatif.plot;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConstructor;
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
			plot.getPlayers().forEach(p -> {
				if (plot.getMembers().getPlayerRank(p) != PlotPerm.PlotRank.VISITOR)
					OCmsg.PLOT_STOPLAG_FIRED.send(p, type);
			});
		}
		else {
			PlotParamType.STOPLAG_STATUS.setValue(plot, 2);
			
			//message
			plot.getPlayers().forEach(p -> {
				if (plot.getMembers().getPlayerRank(p) != PlotPerm.PlotRank.VISITOR)
					OCmsg.PLOT_FORCED_STOPLAG_FIRED.send(p, type);
			});
		}
	}
	
	public double getScore() {
		return detections.entrySet().stream().mapToDouble(e -> ((double) e.getValue() / (double) e.getKey().max) / (StopLagDetect.values().length - 1)).sum();
	}
	
	public double getScore(StopLagDetect lag) {
		return (double) detections.get(lag) / lag.getMaxPerPeriod();
	}

	public enum StopLagDetect {//les valeurs sont par seconde
		PISTON(350, "pistons"),
		LAMP(200, "lampes de redstone"),
		LIQUID(800, "liquides fluides"),
		ENTITY(320, "spawn entités"),
		WIRE(4000, "systèmes de redstone"),
		UNKNOWN(1, "inconnu")
		;
		
		private int defaultMax;
		private int max;
		private String name;
		
		StopLagDetect(int maxPerPeriod, String name) {
			this.name = name;
			this.defaultMax = maxPerPeriod;

			try {
				rlConfig();
			} catch (Exception e) {
				e.printStackTrace();
			}
			/*YamlConfiguration config = OlympaCreatifMain.getInstance().getConfig();
			if (!config.contains("stoplag_limit." + this)) {
				config.set("stoplag_limit." + this, defaultMax);
				OlympaCreatifMain.getInstance().saveConfig();
			}

			this.max = config.getInt("stoplag_limit." + toString());*/
		}

		private void rlConfig() throws IOException, InvalidConfigurationException {
			File file = new File(OlympaCreatifMain.getInstance().getDataFolder(), "stoplag_config.yml");
			if (!file.exists())
				file.createNewFile();

			YamlConfiguration config = new YamlConfiguration();
			config.load(file);

			if (!config.contains("stoplag." + this)) {
				config.set("stoplag." + this, defaultMax);
				config.save(file);
			}

			max = config.getInt("stoplag." + this);
		}
		
		public int getMaxPerPeriod() {
			return max;
		}

		public String getName() {
			return name;
		}
		
		public static void reloadConfig() {

			for (StopLagDetect lag : StopLagDetect.values())
				try{
					lag.rlConfig();
				}catch (Exception ex){
					ex.printStackTrace();
				}

			/*System.out.println("Source config : " + (OlympaCreatifMain.getInstance().getConfig().saveToString()));

			OlympaCreatifMain.getInstance().reloadConfig();
			YamlConfiguration config = OlympaCreatifMain.getInstance().getConfig();

			System.out.println("Reloaded config : " + (config.saveToString()));

			for (StopLagDetect lag : StopLagDetect.values()) {

				System.out.println("Reload config avant " + lag + " : " + config.getInt("stoplag_limit." + lag));

				if (!config.contains("stoplag_limit." + lag))
					config.set("stoplag_limit." + lag, lag.defaultMax);

				System.out.println("Reload config après " + lag + " : " + config.getInt("stoplag_limit." + lag));

				lag.max = config.getInt("stoplag_limit." + lag);
			}
			
			OlympaCreatifMain.getInstance().saveConfig();*/
		}
	}
}
