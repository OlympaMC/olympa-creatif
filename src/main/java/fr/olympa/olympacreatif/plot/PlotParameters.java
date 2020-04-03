package fr.olympa.olympacreatif.plot;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.block.Block;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.DatabaseSerializable;

public class PlotParameters implements DatabaseSerializable{

	private OlympaCreatifMain plugin;
	private Map <PlotParamType, Object> parameters = new HashMap<PlotParamType, Object>();
	
	public PlotParameters(OlympaCreatifMain plugin) {
		this.plugin = plugin;
	}

	public void setParameter(PlotParamType param, Object value) {
		parameters.put(param, value);
	}
	
	public Object getParameter(PlotParamType param) {
		if (parameters.containsKey(param))
			return parameters.get(param);
		
		return null;
	}
	
	@Override
	public String toDbFormat() {
		String s = "";
		for (Entry<PlotParamType, Object> e : parameters.entrySet())
			s += e.getKey() + "=" + e.getValue().toString() + " ";
		
		return s;
	}

	public static PlotParameters fromDbFormat(OlympaCreatifMain plugin, String data) {
		PlotParameters paramsSet = new PlotParameters(plugin);
		
		for (String s : data.split(" "))
			if (data.contains("="))
				if (PlotParamType.getFromString(data.split("=")[0]) != null)
					paramsSet.setParameter(PlotParamType.getFromString(data.split("=")[0]), data.split("=")[1]);

		return paramsSet;
	}
	
	

}
