package fr.olympa.olympacreatif.worldedit;

import org.bukkit.entity.Player;

import fr.olympa.olympacreatif.plot.Plot;

public interface IWorldEditManager {
	
	public boolean isWeEnabled();
	
	public void setWeActivationState(boolean b);
	
	public void clearClipboard(Plot plot, Player p);
	
	public void resetPlot(Player requester, Plot plot);
}
