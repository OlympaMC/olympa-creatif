package fr.olympa.olympacreatif.perks;

import fr.olympa.olympacreatif.OlympaCreatifMain;

public class PerksManager {

	private OlympaCreatifMain plugin;
	private MicroBlocks mb = null;
	private SchematicCreator schem = null;
	private NbtEntityParser nbtParser = null;
	
	public PerksManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		mb = new MicroBlocks(plugin);
		schem = new SchematicCreator(plugin);
		nbtParser = new NbtEntityParser(plugin);
	}
	
	public MicroBlocks getMicroBlocks() {
		return mb;
	}
	
	public SchematicCreator getSchematicCreator() {
		return schem;
	}
	
	public NbtEntityParser getNbtEntityParser() {
		return nbtParser;
	}
}
