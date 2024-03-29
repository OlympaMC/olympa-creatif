package fr.olympa.olympacreatif.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers.NBT;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagDouble;
import net.minecraft.server.v1_16_R3.NBTTagFloat;
import net.minecraft.server.v1_16_R3.NBTTagInt;
import net.minecraft.server.v1_16_R3.NBTTagLong;
import net.minecraft.server.v1_16_R3.NBTTagShort;
import net.minecraft.server.v1_16_R3.NBTTagString;

public class TagsValues {

	private Map<String, TagParams> tags = new HashMap<String, TagsValues.TagParams>();
	
	@SuppressWarnings("rawtypes")
	public TagsValues() {
		
		//chargement des tags depuis le fichier config
        File file = new File(OlympaCreatifMain.getInstance().getDataFolder(), "tags.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            OlympaCreatifMain.getInstance().saveResource("tags.yml", false);
         }
        
        //YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(OlympaCreatifMain.getInstance().getDataFolder(), "tags.yml"));
        YamlConfiguration config = new YamlConfiguration();
        try {
			config.load(file);
		} catch (IOException | InvalidConfigurationException e1) {
			e1.printStackTrace();
			return;
		}
        
		Map<String, String> map = new HashMap<String, String>();
		
		config.getConfigurationSection("tags").getValues(false).entrySet().forEach(entry -> map.put(entry.getKey(), (String)entry.getValue()));

		//Bukkit.getLogger().log(Level.SEVERE, "config : " + map.toString());
		
		for (Entry<String, String> e : map.entrySet()) {
			String[] vals = e.getValue().split(",");
			
			Class nbt = null;
			int min = 0;
			int max = 0;
			Integer listType = null;
			
			switch(vals[0]) {
			case "NBTTagInt":
				nbt = NBTTagInt.class;
				break;
			case "NBTTagShort":
				nbt = NBTTagShort.class;
				break;
			case "NBTTagLong":
				nbt = NBTTagLong.class;
				break;
			case "NBTTagString":
				nbt = NBTTagString.class;
				break;
			case "NBTTagFloat":
				nbt = NBTTagFloat.class;
				break;
			case "NBTTagCompound":
				nbt = NBTTagCompound.class;
				break;
				
			case "NBTTagDouble":
				nbt = NBTTagDouble.class;
				break;
				/*case "NBTTagList":
				nbt = NBTTagList.class;
				break;
			case "NBTTagByte":
				nbt = NBTTagByte.class;
				break;
				*/
			}
			
			if (nbt == null)
				continue;
			
			try {
				min = Integer.valueOf(vals[1]);
				max = Integer.valueOf(vals[2]);
			}catch(NumberFormatException ex) {
				continue;
			}

			switch(vals[3]) {
			case "NBT_STRING":
				listType = NBT.TAG_STRING;
				break;
			case "NBT_INT":
				listType = NBT.TAG_INT;
				break;
			case "NBT_SHORT":
				listType = NBT.TAG_SHORT;
				break;
			case "NBT_COMPOUND":
				listType = NBT.TAG_COMPOUND;
				break;
			case "NBT_FLOAT":
				listType = NBT.TAG_FLOAT;
				break;
			case "NBT_DOUBLE":
				listType = NBT.TAG_DOUBLE;
				break;
			case "NBT_BYTE":
				listType = NBT.TAG_BYTE;
				break;
			}
			
			tags.put(e.getKey(), new TagParams(e.getKey(), nbt, min, max, listType));
		}
		
		//Bukkit.getLogger().log(Level.SEVERE, tags.toString());
	}
	
	public TagParams getTagParams(String key) {
			return tags.get(key);
	}

	@SuppressWarnings("rawtypes")
	public class TagParams{
		
		private String tagName;
		private int min;
		private int max;
		private Integer listType;
		private Class nbtClass;
		
		public TagParams(String tagName, Class nbtClass, int min, int max, Integer listContainType) {
			this.tagName = tagName;
			this.nbtClass= nbtClass; 
			this.min = min;
			this.max = max;
			this.listType = listContainType;
		}

		public Class getTagNbtClass() {
			return nbtClass;
		}

		public String getName() {
			return tagName;
		}
		public int getMin() {
			return min;
		}
		public int getMax() {
			return max;
		}
		public Integer getListType() {
			return listType;
		}
		@Override
		public String toString() {
			return "Params "+ tagName + " : class = " + nbtClass.getSimpleName() + ", min = " + min + ", max = " + max + ", list :" + listType;
		}
	}
}
