package fr.olympa.olympacreatif.plot;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import net.minecraft.server.v1_16_R3.MinecraftServer;

public class PlotStoplagChecker {

	public static final int periodDuration = 100; //in ticks
	//private static int currentPeriod = 0;
	
	public static final int forcedStoplagPeriodDuration = 200;
	public static final int forcedStoplagStoplagCount = 3;
	
	static {
		new BukkitRunnable() {
			@Override
			public void run() {
				OlympaCreatifMain.getInstance().getPlotsManager().getPlots().forEach(plot -> plot.getStoplagChecker().resetHistory());
			}
		}.runTaskTimer(OlympaCreatifMain.getInstance(), periodDuration, periodDuration);
		
	}
	
	private OlympaCreatifMain plugin;
	private Plot plot;
	
	private Map<StopLagDetect, Integer> detections = new HashMap<PlotStoplagChecker.StopLagDetect, Integer>();
	private int stoplagCount = 0;
	private int detectionsCount = 0;
	private int stoplagResetTick = MinecraftServer.currentTick + forcedStoplagPeriodDuration;
	
	public PlotStoplagChecker(OlympaCreatifMain plugin, Plot plot) {
		this.plugin = plugin;
		this.plot = plot;
		
		for (StopLagDetect sld : StopLagDetect.values())
			detections.put(sld, 0);
	}
	
	private void resetHistory() {
		for (StopLagDetect sld : StopLagDetect.values())
			detections.put(sld, 0);
		
		detectionsCount = 0;
	}

	public void addEvent(StopLagDetect type) {		
		detections.put(type, detections.get(type) + 1);
		detectionsCount++;
		
		if (detections.get(type) >= type.getMaxPerPeriod())
			fireStopLag(type);
	}
	
	private void fireStopLag(StopLagDetect type) {
		detections.put(type, 0);
		
		if (stoplagResetTick < MinecraftServer.currentTick)
			stoplagCount = 1;
		else
			stoplagCount++;
		
		stoplagResetTick = MinecraftServer.currentTick + forcedStoplagPeriodDuration;
		
		//si le nombre d'avertissement a été dépassé, mise en stoplag forcée
		if (stoplagCount < forcedStoplagStoplagCount) {
			PlotParamType.STOPLAG_STATUS.setValue(plot, 1);	
			
			//message
			plot.getPlayers().forEach(p -> {
				OCmsg.PLOT_STOPLAG_FIRED.send(p, type);
				});
		}
		else {
			PlotParamType.STOPLAG_STATUS.setValue(plot, 2);
			
			//message
			plot.getPlayers().forEach(p -> {
				OCmsg.PLOT_FORCED_STOPLAG_FIRED.send(p, type);
				});
		}
	}
	
	public int getCurrentCount() {
		return detectionsCount;
	}

	public enum StopLagDetect{
		PISTON(200, "pistons"),
		LAMP(200, "lampes de redstone"),
		LIQUID(1000, "liquides fluides"),
		ENTITY(150, "spawn entités"),
		WIRE(50000, "systèmes de redstone"),
		;
		
		int max;
		String name;
		
		StopLagDetect(int maxPerPeriod, String name){
			max = maxPerPeriod;
			this.name = name;
		}
		
		public int getMaxPerPeriod() {
			return max;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
}
