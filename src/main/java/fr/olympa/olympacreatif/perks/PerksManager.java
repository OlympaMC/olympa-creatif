package fr.olympa.olympacreatif.perks;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.KitsManager;

public class PerksManager {

	private OlympaCreatifMain plugin;
	private MicroBlocks mb;
	private SchematicCreator schem;
	private KitsManager kitsManager;
	
	public PerksManager(OlympaCreatifMain olympaCreatifMain) {
		this.plugin = olympaCreatifMain;
		mb = new MicroBlocks(plugin);
		schem = new SchematicCreator(plugin);
		kitsManager = new KitsManager(plugin);
	}

	public MicroBlocks getMicroBlocks() {
		return mb;
	}

	public SchematicCreator getSchematicCreator() {
		return schem;
	}
	
	public KitsManager getKitsManager() {
		return kitsManager;
	}

}
