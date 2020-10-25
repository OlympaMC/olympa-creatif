package fr.olympa.olympacreatif.plot;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import net.minecraft.server.v1_15_R1.MinecraftServer;

public class PlotStoplagChecker {

	public static final int periodDuration = 20;
	private static int currentPeriod = 0;
	
	public static final int forcedStoplagPeriodDuration = 200;
	public static final int forcedStoplagStoplagCount = 3;
	
	private static final BukkitRunnable updatePeriod = new BukkitRunnable() {
		@Override
		public void run() {
			currentPeriod++;
		}
	};
	
	private OlympaCreatifMain plugin;
	private Plot plot;
	
	private int localCurrentPeriod = 0;
	
	private Map<StopLagDetect, Integer> detections = new HashMap<PlotStoplagChecker.StopLagDetect, Integer>();
	private int stoplagCount = 0;
	private int stoplagResetTick = MinecraftServer.currentTick + forcedStoplagPeriodDuration;
	
	public PlotStoplagChecker(OlympaCreatifMain plugin, Plot plot) {
		this.plugin = plugin;
		this.plot = plot;
		
		if (currentPeriod == 0) {
			currentPeriod++;
			updatePeriod.runTaskTimer(plugin, periodDuration, periodDuration);
		}
	}
	
	public void addEvent(StopLagDetect type) {
		//update current period & clear list si nouvelle periode
		if (localCurrentPeriod <= currentPeriod) {
			localCurrentPeriod = currentPeriod + 1;
			
			for (StopLagDetect sld : StopLagDetect.values())
				detections.put(sld, 0);
		}
		
		detections.put(type, detections.get(type) + 1);
		
		if (detections.get(type) >= type.getMaxPerPeriod())
			fireStopLag(type);
	}
	
	private void fireStopLag(StopLagDetect type) {
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
				if (stoplagCount < forcedStoplagStoplagCount)
					p.sendMessage(Message.PLOT_STOPLAG_FIRED.getValue(type, stoplagCount, forcedStoplagStoplagCount));});
		}
		else {
			PlotParamType.STOPLAG_STATUS.setValue(plot, 2);
			
			//message
			plot.getPlayers().forEach(p -> {
				if (plot.getMembers().getPlayerLevel(p) > 0)
					p.sendMessage(Message.PLOT_FORCED_STOPLAG_FIRED.getValue(type));});
		}
	}

	public enum StopLagDetect{
		PISTON(200, "pistons"),
		LAMP(200, "lampes de redstone"),
		LIQUID(1000, "liquides fluides"), 
		ENTITY(50, "spawn entités"), 
		WIRE(5000, "fil de redstone"),
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
