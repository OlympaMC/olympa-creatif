package fr.olympa.olympacreatif.data;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.permission.OlympaPermission;

public class PermissionsList {	
	public static final OlympaPermission USE_WORLD_EDIT = new OlympaPermission(OlympaGroup.CREA_ARCHITECT);
	public static final OlympaPermission USE_MICRO_BLOCKS = new OlympaPermission(OlympaGroup.CREA_CONSTRUCTOR);
	public static final OlympaPermission USE_COLORED_TEXT = new OlympaPermission(OlympaGroup.CREA_CREATOR);
	public static final OlympaPermission USE_PLOT_EXPORTATION = new OlympaPermission(OlympaGroup.CREA_CONSTRUCTOR);
	public static final OlympaPermission USE_PROTECTED_AREA = new OlympaPermission(OlympaGroup.CREA_CREATOR);
	
	public static final OlympaPermission USE_SKULL_COMMAND = new OlympaPermission(OlympaGroup.CREA_CONSTRUCTOR);
	public static final OlympaPermission USE_HAT_COMMAND = new OlympaPermission(OlympaGroup.CREA_CONSTRUCTOR);
	
	public static final OlympaPermission STAFF_BYPASS_PLOT_KICK_AND_BAN = new OlympaPermission(OlympaGroup.DEV);
	public static final OlympaPermission STAFF_BYPASS_VANILLA_COMMANDS = new OlympaPermission(OlympaGroup.DEV);
	public static final OlympaPermission STAFF_BYPASS_WORLDEDIT = new OlympaPermission(OlympaGroup.DEV);
	public static final OlympaPermission STAFF_PLOT_OWNER_EVERYWHERE = new OlympaPermission(OlympaGroup.DEV);
	
	 
	/*nb de plots propriétaire :
	 * Joueur : 1
	 * Constructeur : 3
	 * Architecte : 6
	 * Créateur : 10
	 * Plots proprio/membre max : 36
	 */
}
