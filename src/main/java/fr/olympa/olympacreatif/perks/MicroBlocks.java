package fr.olympa.olympacreatif.perks;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.UUID;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
	//private List<ItemStack> namedMicroBlocks = new ArrayList<ItemStack>();
	
	public MicroBlocks(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		
		boolean isFirstInitialization = false;
		
		mbConfigFile = new File(plugin.getDataFolder() + "/microblocks.yml");
		
		if (!mbConfigFile.exists()) {
			try {
				mbConfigFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			isFirstInitialization = true;
		}
		
		try {
			mbConfig.load(mbConfigFile);
		} catch (IOException | InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			plugin.getLogger().warning("Failed to load microblocks config!");
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
		new MbGuiInterface("§9Microblocks", DyeColor.ORANGE, microBlocks.values()).create(p);
	}
	
	private class MbGuiInterface extends PagedGUI<ItemStack>{

		protected MbGuiInterface(String name, DyeColor color, Collection<ItemStack> collection) {
			super(name, color, new ArrayList<ItemStack>(Arrays.asList(collection.toArray(new ItemStack[collection.size()]))), 5);
		}

		@Override
		public ItemStack getItemStack(ItemStack object) {
			return object;
		}

		@Override
		public void click(ItemStack existing, Player p, ClickType click) {
			ItemStack it = ItemUtils.name(existing.clone(), existing.getItemMeta().getDisplayName());
			p.getInventory().addItem(it);
		}
		
	}
	
    private void initialize(boolean isFirstInitialization) {
    	if (isFirstInitialization) {
    		Map <String, String> map = new HashMap<String, String>();
    		
        	map.put("bullobily", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmE1MjdkYzAxMDM0ZGI4MDBlMGRhYjZiZDgyMWFhNzhkMTc1NWE5M2E3YzY1OGE5NTUyMmFlYTRjMWZlNzIwZSJ9fX0=");
        	
    		mbConfig.createSection("microblocks_data", map);
    		try {
				mbConfig.save(mbConfigFile);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    	}
    	
    	for (Entry<String, Object> e : mbConfig.getConfigurationSection("microblocks_data").getValues(false).entrySet())
    		microBlocks.put(e.getKey(), ItemUtils.skullCustom("§9MicroBlock §6" + e.getKey(), (String) e.getValue()));
    		//microBlocks.put(e.getKey(), getTexturedHead((String) e.getValue()));
    		
    	//for (Entry<String, ItemStack> e : microBlocks.entrySet())
    	//	namedMicroBlocks.add(ItemUtils.name(e.getValue().clone(), "§9MicroBlock : §6" + e.getKey()));
    }
	
	//renvoie une tête texturée selon le code donné
    @Deprecated //méthode déjà dans l'api
    public ItemStack getTexturedHead(String value) {
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

    @Deprecated //méthode déjà dans l'api
	public void skull(Consumer<ItemStack> callback, String name, String skull, String... lore) {
    	new BukkitRunnable() {
			
			@Override
			public void run() { 
				URL url_0;
				try {
					url_0 = new URL("https://api.mojang.com/users/profiles/minecraft/" + skull);
		            InputStreamReader reader_0 = new InputStreamReader(url_0.openStream());
		            String uuid = new JsonParser().parse(reader_0).getAsJsonObject().get("id").getAsString();

		            URL url_1 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
		            InputStreamReader reader_1 = new InputStreamReader(url_1.openStream());
		            JsonObject textureProperty = new JsonParser().parse(reader_1).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
		            String texture = textureProperty.get("value").getAsString();
		            
		            ItemStack it = getTexturedHead(texture);
		            ItemUtils.name(it, name);
		            ItemUtils.lore(it, lore);
		            
					callback.accept(it);
		            
				} catch (IOException e) {
	            	callback.accept(ItemUtils.item(Material.PLAYER_HEAD, name, lore));
				}
			}
		}.runTaskAsynchronously(plugin);
    }
}
