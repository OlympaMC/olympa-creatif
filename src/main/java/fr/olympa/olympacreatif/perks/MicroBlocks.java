package fr.olympa.olympacreatif.perks;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import fr.olympa.api.gui.templates.PagedGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.olympacreatif.OlympaCreatifMain;

public class MicroBlocks {

	private OlympaCreatifMain plugin;
	private Map<String, ItemStack> microBlocks = new HashMap<String, ItemStack>();
	private List<ItemStack> namedMicroBlocks = new ArrayList<ItemStack>();
	private MbGuiInterface gui;
	
	public MicroBlocks(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		initialize2();
		gui = new MbGuiInterface("§9Microblocks", DyeColor.ORANGE, namedMicroBlocks);
	}
	
	public ItemStack getMb(String name) {
		if (microBlocks.containsKey(name))
			return ItemUtils.name(microBlocks.get(name).clone(), "§9Microblock §6" + name);
		else
			return null;
	}
	
	public Map<String, ItemStack> getAllMbs() {
		return microBlocks;
	}
	
	public void openGui(Player p){
		gui.create(p);
	}
	
	private class MbGuiInterface extends PagedGUI<ItemStack>{

		protected MbGuiInterface(String name, DyeColor color, List<ItemStack> objects) {
			super(name, color, objects);
			// TODO Auto-generated constructor stub
		}

		@Override
		public ItemStack getItemStack(ItemStack object) {
			return object;
		}

		@Override
		public void click(ItemStack existing, Player p) {
			p.getInventory().addItem(ItemUtils.name(existing.clone(), "§9Microblock §6" + existing.getItemMeta().getDisplayName()));
		}
		
	}
	
	//renvoie une tête texturée selon le code donné
    public ItemStack getCustomTextureHead(String value) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), "");
        profile.getProperties().put("textures", new Property("textures", value));
        Field profileField = null;
        try {
            profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
        head.setItemMeta(meta);
        return head;
    }
	
    private void initialize2() {
    	microBlocks.put("test", getCustomTextureHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjk3ZDZkN2JlOTg1ZDA2MjJhNDhlOTA2OThlOTA3M2Y3ZmY4ODEzMjkyODEyZWJkMTczMGRiYTBlMDFjZjE4ZiJ9fX0="));
    	
    	for (Entry<String, ItemStack> e : microBlocks.entrySet())
    		namedMicroBlocks.add(ItemUtils.name(e.getValue().clone(), "§9MicroBlock : §6" + e.getKey()));
    }
	
    
    @Deprecated
	private void createMb(String mbName, String playerName) {
		microBlocks.put(mbName, ItemUtils.skull(mbName, playerName));
	}
    
    @Deprecated
	private void initialize() {    
		createMb("apple", "MHF_Apple");
	    createMb("arrowdown", "MHF_ArrowDown");
	    createMb("arrowleft", "MHF_ArrowLeft");
	    createMb("arrowright", "MHF_ArrowRight");
	    createMb("arrowup", "MHF_ArrowUp");
	    createMb("beachball", "PurplePenguinLPs");
	    createMb("bedrock", "BedrockSolid");
	    createMb("blaze", "MHF_Blaze");
	    createMb("bookshelf", "BowAimbot");
	    createMb("brick", "BrickInTheHead");
	    createMb("bslime", "Deathbeam");
	    createMb("cactus", "MHF_Cactus");
	    createMb("cake", "MHF_Cake");
	    createMb("camera", "FHG_Cam");
	    createMb("camera2", "gocodygo");
	    createMb("cavespider", "MHF_CaveSpider");
	    createMb("cherry", "TheEvilEnderman");
	    createMb("chest", "MHF_Chest");
	    createMb("chicken", "MHF_Chicken");
	    createMb("clock", "nikx004");
	    createMb("cobblestone", "_Rience");
	    createMb("coconut", "KyleWDM");
	    createMb("commandblock", "monkey354");
	    createMb("companioncube", "sk8erace1");
	    createMb("cookie", "QuadratCookie");
	    createMb("cow", "MHF_Cow");
	    createMb("derpysnow", "GLaDOS");
	    createMb("diamondblock", "Fyspyguy");
	    createMb("diamondore", "akaBruce");
	    createMb("diceblack", "azbandit2000");
	    createMb("dicered", "gumbo632");
	    createMb("dicewhite", "jmars213");
	    createMb("dirt", "zachman228");
	    createMb("dispenser", "scemm");
	    createMb("emeraldore", "Tereneckla");
	    createMb("enderchest", "_Brennian");
	    createMb("enderdragon", "KingEndermen");
	    createMb("enderman", "MHF_Enderman");
	    createMb("exclamation", "MHF_Exclamation");
	    createMb("eye", "Taizun");
	    createMb("eyeofender", "Edna_I");
	    createMb("fox", "hugge75");
	    createMb("furnace", "NegativeZeroTV");
	    createMb("gamecube", "ReflectedNicK");
	    createMb("ghast", "MHF_Ghast");
	    createMb("glowstone", "samstine11");
	    createMb("goldblock", "StackedGold");
	    createMb("golem", "MHF_Golem");
	    createMb("grass", "MoulaTime");
	    createMb("gslime", "nilaro");
	    createMb("haybale", "Bendablob");
	    createMb("headlight", "Toby_The_Coder");
	    createMb("herobrine", "MHF_Herobrine");
	    createMb("horse", "gavertoso");
	    createMb("ice", "icytouch");
	    createMb("ironblock", "metalhedd");
	    createMb("ironore", "IronBrin");
	    createMb("jukebox", "C418");
	    createMb("lampon", "AutoSoup");
	    createMb("lavaslime", "MHF_LavaSlime");
	    createMb("leaves", "rsfx");
	    createMb("leaves2", "half_bit");
	    createMb("lemon", "Aesixx");
	    createMb("lime", "greenskull27");
	    createMb("machine", "aetherX");
	    createMb("melon", "MHF_Melon");
	    createMb("monitor", "Alistor");
	    createMb("mossycobblestone", "Khrenan");
	    createMb("muffin", "ChoclateMuffin");
	    createMb("mushroomcow", "MHF_MushroomCow");
	    createMb("netherrack", "Numba_one_Stunna");
	    createMb("noteblock", "PixelJuke");
	    createMb("notexture", "ddrl46");
	    createMb("oaklog", "MHF_OakLog");
	    createMb("oaklog2", "MightyMega");
	    createMb("oakplanks", "terryxu");
	    createMb("obsidian", "loiwiol");
	    createMb("ocelot", "MHF_Ocelot");
	    createMb("orange", "hi1232");
	    createMb("orangewool", "titou36");
	    createMb("oslime", "md_5");
	    createMb("package", "ku228");
	    createMb("parrot", "Luk3011");
	    createMb("penguin", "Patty14");
	    createMb("pig", "MHF_Pig");
	    createMb("pigzombie", "MHF_PigZombie");
	    createMb("piston", "JL2579");
	    createMb("podzol", "PhasePvP");
	    createMb("pokeball", "Chuzard");
	    createMb("popcorn", "ZachWarnerHD");
	    createMb("portal", "TorchPvP");
	    createMb("potato", "CraftPotato13");
	    createMb("present", "I_Xenon_I");
	    createMb("pumpkin", "MHF_Pumpkin");
	    createMb("pumpkinface", "Koebasti");
	    createMb("qcube", "jarrettgabe");
	    createMb("quartzblock", "bubbadawg01");
	    createMb("question", "MHF_Question");
	    createMb("radio", "uioz");
	    createMb("redexclamation", "jona612");
	    createMb("redsand", "OmniSulfur");
	    createMb("redstoneblock", "AlexDr0ps");
	    createMb("redstoneore", "annayirb");
	    createMb("redstonetorch", "RedstoneMakerMe");
	    createMb("rubixcube", "iTactical17");
	    createMb("sand", "rugofluk");
	    createMb("sheep", "MHF_Sheep");
	    createMb("slime", "MHF_Slime");
	    createMb("socialicons", "titigogo70");
	    createMb("spacehelm", "Dctr_");
	    createMb("speaker", "b1418");
	    createMb("spider", "MHF_Spider");
	    createMb("sponge", "pomi44");
	    createMb("squid", "MHF_Squid");
	    createMb("stickypiston", "Panda4994");
	    createMb("stone", "Robbydeezle");
	    createMb("stonebrick", "Cakers");
	    createMb("swskeleton", "lesto123");
	    createMb("taco", "Crunchy_Taco34");
	    createMb("terminal", "Hack");
	    createMb("tnt", "MHF_TNT");
	    createMb("tnt2", "MHF_TNT2");
	    createMb("troll", "Trollface20");
	    createMb("tv", "Metroidling");
	    createMb("tv2", "nonesuchplace");
	    createMb("villager", "MHF_Villager");
	    createMb("water", "emack0714");
	    createMb("witch", "scrafbrothers4");
	}
	
}
