package fr.olympa.olympacreatif.perks;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
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

	File mbConfigFile;
	YamlConfiguration mbConfig = new YamlConfiguration();
	
	private Map<String, ItemStack> microBlocks = new LinkedHashMap<String, ItemStack>();
	private List<ItemStack> namedMicroBlocks = new ArrayList<ItemStack>();
	
	public MicroBlocks(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		
		boolean isFirstInitialization = false;
		
		mbConfigFile = new File(plugin.getDataFolder() + "microblocks.yml");
		
		if (!mbConfigFile.exists()) {
			mbConfigFile.mkdirs();
			isFirstInitialization = true;
		}
		
		try {
			mbConfig.load(mbConfigFile);
		} catch (IOException | InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//initialise la liste des têtes
		initialize(isFirstInitialization);
	}
	
	public ItemStack getMb(String name) {
		if (microBlocks.containsKey(name))
			return ItemUtils.name(microBlocks.get(name).clone(), "§9MicroBlock §6" + name);
		else
			return null;
	}
	
	public Map<String, ItemStack> getAllMbs() {
		return microBlocks;
	}
	
	public void openGui(Player p){
		new MbGuiInterface("§9Microblocks", DyeColor.ORANGE, namedMicroBlocks).create(p);
	}
	
	private class MbGuiInterface extends PagedGUI<ItemStack>{

		protected MbGuiInterface(String name, DyeColor color, List<ItemStack> objects) {
			super(name, color, objects, 5);
			// TODO Auto-generated constructor stub
		}

		@Override
		public ItemStack getItemStack(ItemStack object) {
			return object;
		}

		@Override
		public void click(ItemStack existing, Player p) {
			p.getInventory().addItem(ItemUtils.name(existing.clone(), existing.getItemMeta().getDisplayName()));
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
	
    private void initialize(boolean isFirstInitialization) {
    	if (isFirstInitialization) {
    		Map <String, String> map = new HashMap<String, String>();
    		
        	map.put("acacia_bark", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTZhM2JiYTJiN2EyYjRmYTQ2OTQ1YjE0NzE3NzdhYmU0NTk5Njk1NTQ1MjI5ZTc4MjI1OWFlZDQxZDYifX19");
        	map.put("acacia_log", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDgzYjIzYTJjMzU3ZTExNWIzMTA3NTI2NTMzYWVkNjI3MzFkNTEyNDE4OGQ5YTE1NzhmZTIzZmI1ZjI5NjkifX19");
        	
    		mbConfig.createSection("microblocks_data", map);
    		try {
				mbConfig.save(mbConfigFile);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    	}
    	
    	for (Entry<String, Object> e : mbConfig.getConfigurationSection("microblocks_data").getValues(false).entrySet())
    		microBlocks.put(e.getKey(), getCustomTextureHead((String) e.getValue()));
    	
    	for (Entry<String, ItemStack> e : microBlocks.entrySet())
    		namedMicroBlocks.add(ItemUtils.name(e.getValue().clone(), "§9MicroBlock : §6" + e.getKey()));
    }
}
