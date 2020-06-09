package fr.olympa.olympacreatif.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.provider.OlympaPlayerObject;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;

public class OlympaPlayerCreatif extends OlympaPlayerObject{

	private OlympaCreatifMain plugin;
	private int gameMoney = 0;
	private int bonusPlots = 0;
	private boolean isAdminMode = false;
	
	public OlympaPlayerCreatif(OlympaCreatifMain plugin, UUID uuid, String name, String ip) {
		super(uuid, name, ip);
		this.plugin = plugin;
	}

	public void addGameMoney(int i) {
		gameMoney += Math.max(i, 0);
	}
	
	public void removeGameMoney(int i) {
		gameMoney -= Math.max(i, 0);
	}
	
	public int getGameMoney() {
		return gameMoney;
	}
	
	public void setAdmin(boolean b) {
		isAdminMode = b;
	}
	
	public boolean isAdmin() {
		return isAdminMode;
	}

	public void addBonusPlots(int i) {
		bonusPlots += i;
	}
	
	public int getBonusPlots() {
		return bonusPlots;
	}
	
	public List<Plot> getPlots(boolean onlyOwnedPlots) {
		List<Plot> list = new ArrayList<Plot>();
		
		for (Plot plot : plugin.getPlotsManager().getPlots())
			if (plot.getMembers().getPlayerRank(getInformation()) != PlotRank.VISITOR)
				if (!onlyOwnedPlots || (plot.getMembers().getPlayerRank(getInformation()) == PlotRank.OWNER && onlyOwnedPlots))
					list.add(plot);
		
		return list;
	}
	
	public int getPlotsSlots(boolean onlyOwnedPlots) {
		if (!onlyOwnedPlots)
			return 36;
		
		int i = 1 + bonusPlots;
		
		if (getGroup() == OlympaGroup.CREA_CREATOR)
			i += 10;
		else if(getGroup() == OlympaGroup.CREA_ARCHITECT)
			i += 6;
		else if(getGroup() == OlympaGroup.CREA_CONSTRUCTOR)
			i += 4;
		
		return i;
	}
}





