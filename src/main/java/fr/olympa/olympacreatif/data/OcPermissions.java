package fr.olympa.olympacreatif.data;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.permission.OlympaSpigotPermission;

public class OcPermissions {	
	public static final OlympaSpigotPermission MICRO_BLOCKS_COMMAND = new OlympaSpigotPermission(OlympaGroup.CREA_CONSTRUCTOR);
	public static final OlympaSpigotPermission SKULL_COMMAND = new OlympaSpigotPermission(OlympaGroup.CREA_CONSTRUCTOR);
	public static final OlympaSpigotPermission USE_PLOT_RESET = new OlympaSpigotPermission(OlympaGroup.CREA_CONSTRUCTOR);
	public static final OlympaSpigotPermission CREA_HAT_COMMAND = new OlympaSpigotPermission(OlympaGroup.CREA_CONSTRUCTOR);
	
	public static final OlympaSpigotPermission USE_WORLD_EDIT = new OlympaSpigotPermission(OlympaGroup.CREA_ARCHITECT);
	public static final OlympaSpigotPermission USE_PLOT_MUSIC = new OlympaSpigotPermission(OlympaGroup.CREA_ARCHITECT);
	public static final OlympaSpigotPermission USE_PLOT_EXPORTATION = new OlympaSpigotPermission(OlympaGroup.CREA_ARCHITECT);
	
	public static final OlympaSpigotPermission USE_HOLOGRAMS = new OlympaSpigotPermission(OlympaGroup.CREA_CREATOR);
	public static final OlympaSpigotPermission USE_COLORED_TEXT = new OlympaSpigotPermission(OlympaGroup.CREA_CREATOR);
	public static final OlympaSpigotPermission USE_ARMORSTAND_EDITOR = new OlympaSpigotPermission(OlympaGroup.CREA_CREATOR);
	
	public static final OlympaSpigotPermission STAFF_BYPASS_PLOT_KICK_AND_BAN = new OlympaSpigotPermission(OlympaGroup.DEV);
	public static final OlympaSpigotPermission STAFF_BYPASS_VANILLA_COMMANDS = new OlympaSpigotPermission(OlympaGroup.DEV);
	public static final OlympaSpigotPermission STAFF_BYPASS_WORLDEDIT = new OlympaSpigotPermission(OlympaGroup.DEV);
	public static final OlympaSpigotPermission STAFF_PLOT_FAKE_OWNER = new OlympaSpigotPermission(OlympaGroup.DEV);
	public static final OlympaSpigotPermission STAFF_RESET_PLOT = new OlympaSpigotPermission(OlympaGroup.DEV);
	public static final OlympaSpigotPermission STAFF_OCA_CMD = new OlympaSpigotPermission(OlympaGroup.DEV);

	public static final OlympaSpigotPermission STAFF_MANAGE_COMPONENT = new OlympaSpigotPermission(OlympaGroup.DEV);
	//public static final OlympaSpigotPermission STAFF_DEACTIVATE_WORLD_EDIT = new OlympaSpigotPermission(OlympaGroup.DEV);

	public static final OlympaSpigotPermission CREA_TPA_COMMAND = new OlympaSpigotPermission(OlympaGroup.PLAYER);
	public static final OlympaSpigotPermission CREA_TPF_COMMAND = new OlympaSpigotPermission(OlympaGroup.DEV);
	
	public static final OlympaSpigotPermission STAFF_MANAGE_MONEY = new OlympaSpigotPermission(OlympaGroup.DEV);

	public static final OlympaSpigotPermission STAFF_BYPASS_OP_CHECK = new OlympaSpigotPermission(OlympaGroup.DEV);
	public static final OlympaSpigotPermission STAFF_BUILD_ROADS = new OlympaSpigotPermission(OlympaGroup.DEV);

	
	
	 
	/*nb de plots propriétaire :
	 * Joueur : 1
	 * Constructeur : 3
	 * Architecte : 6
	 * Créateur : 10
	 * Plots proprio/membre max : 36
	 */
}
