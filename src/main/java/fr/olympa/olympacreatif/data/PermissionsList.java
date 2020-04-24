package fr.olympa.olympacreatif.data;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.permission.OlympaPermission;

public class PermissionsList {

	public static final OlympaPermission KIT_REDSTONE = new OlympaPermission(OlympaGroup.CREA_CONSTRUCTOR);
	public static final OlympaPermission KIT_MOBS = new OlympaPermission(OlympaGroup.CREA_CONSTRUCTOR);
	public static final OlympaPermission KIT_ANIMALS = new OlympaPermission(OlympaGroup.CREA_CONSTRUCTOR);
	//public static final OlympaPermission KIT_LIQUIDS = new OlympaPermission(OlympaGroup.CREA_ARCHITECT);
	public static final OlympaPermission KIT_COMMAND_BLOCKS = new OlympaPermission(OlympaGroup.CREA_CREATOR);
	public static final OlympaPermission KIT_LAVA = new OlympaPermission(OlympaGroup.ADMIN);
	public static final OlympaPermission KIT_ADMIN = new OlympaPermission(OlympaGroup.ADMIN);
	
	public static final OlympaPermission USE_WORLD_EDIT = new OlympaPermission(OlympaGroup.CREA_ARCHITECT);
	public static final OlympaPermission USE_MICRO_BLOCKS = new OlympaPermission(OlympaGroup.CREA_CONSTRUCTOR);
	public static final OlympaPermission USE_COLORED_TEXT = new OlympaPermission(OlympaGroup.CREA_ARCHITECT);
	public static final OlympaPermission USE_PLOT_EXPORTATION = new OlympaPermission(OlympaGroup.CREA_CONSTRUCTOR);
	
	public static final OlympaPermission USE_SKULL_COMMAND = new OlympaPermission(OlympaGroup.CREA_CONSTRUCTOR);
	public static final OlympaPermission USE_HAT_COMMAND = new OlympaPermission(OlympaGroup.CREA_CONSTRUCTOR);

	public static final OlympaPermission PLOTS_COUNT_CREATOR = new OlympaPermission(OlympaGroup.CREA_CREATOR);
	public static final OlympaPermission PLOTS_COUNT_ARCHITECT= new OlympaPermission(OlympaGroup.CREA_ARCHITECT);
	public static final OlympaPermission PLOTS_COUNT_CONSTRUCTOR = new OlympaPermission(OlympaGroup.CREA_CONSTRUCTOR);
	
	 
	/*nb de plots propriétaire :
	 * Joueur : 1
	 * Constructeur : 3
	 * Architecte : 6
	 * Créateur : 10
	 * Plots proprio/membre max : 27
	 */
}
