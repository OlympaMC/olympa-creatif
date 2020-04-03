package fr.olympa.olympacreatif.plot;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.objects.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.data.DatabaseSerializable;

public class PlotMembers implements DatabaseSerializable{

	private Map<OlympaPlayerInformations, PlotRank> members = new HashMap<OlympaPlayerInformations, PlotRank>();
	
	@Override
	public String toDbFormat() {
		String s = "";
		for (Entry<OlympaPlayerInformations, PlotRank> e : members.entrySet())
			s += e.getKey().getID() + ":" + e.getValue().getRankName() + " ";
		
		return s;
	}

	public static PlotMembers fromDbFormat(String data) {
		PlotMembers pm = new PlotMembers();
		for (String s : data.split(" "))
			if (s.contains(":"))
				pm.set(AccountProvider.getPlayerInformations(Long.valueOf(s.split(":")[0])), PlotRank.getPlotRank(s.split(":")[1]));
		
		return pm;
	}

	public void set(OlympaPlayerInformations p, PlotRank rank) {
		members.put(p, rank);
	}

	public PlotRank getPlayerRank(OlympaPlayer p) {
		if (members.containsKey(p.getInformation()))
			return members.get(p.getInformation());
		
		else return PlotRank.PERMISSIONS_NULL;
	}

	public int getPlayerLevel(OlympaPlayer p) {
		if (members.containsKey(p.getInformation()))
			return members.get(p.getInformation()).getLevel();
		
		else return PlotRank.PERMISSIONS_NULL.getLevel();
	}

}
