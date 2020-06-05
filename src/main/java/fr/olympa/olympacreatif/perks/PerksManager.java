package fr.olympa.olympacreatif.perks;

import fr.olympa.olympacreatif.OlympaCreatifMain;

public class PerksManager {

	private OlympaCreatifMain plugin;
	private MicroBlocks mb = null;
	private SchematicCreator schem = null;
	private LinesOnNameUtil linesOnName = null;
	
	public PerksManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		mb = new MicroBlocks(plugin);
		schem = new SchematicCreator(plugin);
		linesOnName = new LinesOnNameUtil(plugin);
	}
	
	public MicroBlocks getMicroBlocks() {
		return mb;
	}
	
	public SchematicCreator getSchematicCreator() {
		return schem;
	}
	
	public LinesOnNameUtil getLinesOnHeadUtil() {
		return linesOnName;
	}
}
