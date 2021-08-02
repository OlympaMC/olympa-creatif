package fr.olympa.olympacreatif.data;

import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.permission.OlympaSpigotPermission;

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

	public static final OlympaSpigotPermission STAFF_BYPASS_PLOT_KICK_AND_BAN = new OlympaSpigotPermission(OlympaGroup.ASSISTANT);
	public static final OlympaSpigotPermission STAFF_BYPASS_VANILLA_COMMANDS = new OlympaSpigotPermission(OlympaGroup.ASSISTANT);
	public static final OlympaSpigotPermission STAFF_BYPASS_WORLDEDIT = new OlympaSpigotPermission(OlympaGroup.MOD);
	public static final OlympaSpigotPermission STAFF_PLOT_FAKE_OWNER = new OlympaSpigotPermission(OlympaGroup.MODP);
	public static final OlympaSpigotPermission STAFF_RESET_PLOT = new OlympaSpigotPermission(OlympaGroup.MOD);
	public static final OlympaSpigotPermission STAFF_OCA_CMD = new OlympaSpigotPermission(OlympaGroup.ASSISTANT);

	public static final OlympaSpigotPermission STAFF_MANAGE_COMPONENT = new OlympaSpigotPermission(OlympaGroup.MODP);
	public static final OlympaSpigotPermission STAFF_MANAGE_SHOP = new OlympaSpigotPermission(OlympaGroup.MODP);

	public static final OlympaSpigotPermission CREA_TPA_COMMAND = new OlympaSpigotPermission(OlympaGroup.PLAYER);
	public static final OlympaSpigotPermission CREA_TPF_COMMAND = new OlympaSpigotPermission(OlympaGroup.ASSISTANT);

	//public static final OlympaSpigotPermission STAFF_MANAGE_MONEY = new OlympaSpigotPermission(OlympaGroup.DEV);

	public static final OlympaSpigotPermission STAFF_BYPASS_OP_CHECK = new OlympaSpigotPermission(OlympaGroup.RESP);
	public static final OlympaSpigotPermission STAFF_BUILD_ROADS = new OlympaSpigotPermission(OlympaGroup.MOD);

	public static final OlympaSpigotPermission STAFF_SET_PLOT_OWNER = new OlympaSpigotPermission(OlympaGroup.MODP);
	public static final OlympaSpigotPermission STAFF_RELOAD = new OlympaSpigotPermission(OlympaGroup.MODP);

	public static final OlympaSpigotPermission STAFF_STOPLAG_MANAGEMENT = new OlympaSpigotPermission(OlympaGroup.ASSISTANT);
	public static final OlympaSpigotPermission STRUCTURE_COMMAND = new OlympaSpigotPermission(OlympaGroup.CREA_ARCHITECT);

	public static final OlympaSpigotPermission WEBSHOP = new OlympaSpigotPermission(OlympaGroup.FONDA);


	/*nb de plots propriétaire :
	 * Joueur : 1
	 * Constructeur : 3
	 * Architecte : 6
	 * Créateur : 10
	 * Plots proprio/membre max : 36
	 */
}
