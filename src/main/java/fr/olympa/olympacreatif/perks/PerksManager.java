package fr.olympa.olympacreatif.perks;

import fr.olympa.olympacreatif.OlympaCreatifMain;

public class PerksManager {

	private OlympaCreatifMain plugin;
	private MicroBlocks mb = null;
	
	public PerksManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		mb = new MicroBlocks(plugin);
	}
	
	public MicroBlocks getMicroBlocks() {
		return mb;
	}
}
