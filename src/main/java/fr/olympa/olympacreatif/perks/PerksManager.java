package fr.olympa.olympacreatif.perks;

import fr.olympa.olympacreatif.OlympaCreatifMain;

public class PerksManager {

	private OlympaCreatifMain plugin;
	private MicroBlocks mb;
	private SchematicCreator schem;
	private KitsManager kitsManager;
	private UpgradesManager upgradesManager;
	private MusicManager musicManager;
	private ArmorStandManager armorstandManager;
	
	public PerksManager(OlympaCreatifMain olympaCreatifMain) {
		this.plugin = olympaCreatifMain;
		mb = new MicroBlocks(plugin);
		schem = new SchematicCreator(plugin);
		kitsManager = new KitsManager(plugin);
		upgradesManager = new UpgradesManager(plugin);
		musicManager = new MusicManager(plugin);
		armorstandManager = new ArmorStandManager(plugin);
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
	
	public UpgradesManager getUpgradesManager() {
		return upgradesManager;
	}
	
	public MusicManager getSongManager() {
		return musicManager;		
	}
	
	public ArmorStandManager getArmorStandManager() {
		return armorstandManager;		
	}
}
