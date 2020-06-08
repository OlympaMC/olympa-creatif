package fr.olympa.olympacreatif;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fr.olympa.api.provider.OlympaPlayerObject;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;

public class OlympaPlayerCreatif extends OlympaPlayerObject{

	private int gameMoney = 0;
	private List<Plot> plotsOwner = new ArrayList<Plot>();
	private List<Plot> plotsMember = new ArrayList<Plot>();
	
	public OlympaPlayerCreatif(UUID uuid, String name, String ip, int gameMoney) {
		super(uuid, name, ip);
		this.gameMoney = gameMoney; 
	}

	public void addPlot(Plot plot) {
		if (plot.getMembers().getPlayerRank(getInformation()) == PlotRank.OWNER) 
			plotsOwner.add(plot);
		else 
			plotsMember.add(plot);
	}
	
}
