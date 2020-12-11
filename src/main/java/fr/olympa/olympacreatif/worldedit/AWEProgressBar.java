package fr.olympa.olympacreatif.worldedit;

import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.progressDisplay.IProgressDisplay;

public class AWEProgressBar /*implements IProgressDisplay */{
/*
	@Override
	public void disableMessage(IPlayerEntry arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMessage(IPlayerEntry player, int jobsCount, int queuedBlocks, int maxQueuedBlocks, double timeLeft, double placingSpeed, double percentage) {
		Player p = Bukkit.getPlayer(player.getUUID());
		
		if (p == null)
			return;
		
		p.sendActionBar("§6Progression WorldEdit : §e" + new DecimalFormat("#.##").format(percentage) + "%");
	}*/
}
