package fr.olympa.olympacreatif.commandblocks;

import org.bukkit.boss.BossBar;

import fr.olympa.olympacreatif.OlympaCreatifMain;

public class CbBossBar {

	private BossBar bar;
	private double value = 100;
	private double max = 100;
	
	public CbBossBar(OlympaCreatifMain plugin, BossBar bar) {
		this.bar = bar;
	}
	
	public void setMax(int max) {
		this.max = max;
		
		bar.setProgress(Math.max(0, Math.min(1, value / max)));
	}

	public void setValue(Integer value) {
		this.value = value;

		bar.setProgress(Math.max(0, Math.min(1, value / max)));
	}
	
	public BossBar getBar() {
		return bar;
	}

	public int getMax() {
		return (int) max;
	}
}
