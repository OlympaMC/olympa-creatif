package fr.olympa.olympacreatif.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftMagicNumbers.NBT;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldedit.extent.clipboard.io.NBTSchematicReader;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import net.minecraft.server.v1_15_R1.NBTBase;
import net.minecraft.server.v1_15_R1.NBTTagByte;
import net.minecraft.server.v1_15_R1.NBTTagDouble;
import net.minecraft.server.v1_15_R1.NBTTagFloat;
import net.minecraft.server.v1_15_R1.NBTTagInt;
import net.minecraft.server.v1_15_R1.NBTTagList;
import net.minecraft.server.v1_15_R1.NBTTagShort;
import net.minecraft.server.v1_15_R1.NBTTagString;

public class TagsValues {

	private Map<String, TagParams> tags = new HashMap<String, TagsValues.TagParams>();
	
	@SuppressWarnings("rawtypes")
	public TagsValues() {
		/*
		tags.put("Name", new TagParams(NBTTagString.class, 0, 200, null));
		tags.put("Lore", new TagParams(NBTTagString.class, 0, 200, NBT.TAG_STRING));

		tags.put("CustomName", new TagParams(NBTTagString.class, 0, 200, null));
		tags.put("CustomNameVisible", new TagParams(NBTTagInt.class, 0, 1, null));
		tags.put("Invulnerable", new TagParams(NBTTagInt.class, 0, 1, null));
		
		tags.put("HideFlags", new TagParams(NBTTagInt.class, 0, 10000, null));
		*/
		
		//chargement des tags depuis le fichier config
		FileConfiguration config = YamlConfiguration.loadConfiguration(new File(OlympaCreatifMain.getMainClass().getDataFolder(), "tags.yml"));
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
			case "NBTTagString":
				nbt = NBTTagString.class;
				break;
			case "NBTTagDouble":
				nbt = NBTTagDouble.class;
				break;
			case "NBTTagFloat":
				nbt = NBTTagFloat.class;
				break;
			case "NBTTagList":
				nbt = NBTTagList.class;
				break;
			case "NBTTagByte":
				nbt = NBTTagByte.class;
				break;
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
			case "NBT_COMPOUND":
				listType = NBT.TAG_COMPOUND;
				break;
			case "NBT_FLOAT":
				listType = NBT.TAG_FLOAT;
				break;
			case "NBT_BYTE":
				listType = NBT.TAG_BYTE;
				break;
			}
			
			tags.put(e.getKey(), new TagParams(nbt, min, max, listType));
		}
		
		//Bukkit.getLogger().log(Level.SEVERE, "après parse config : " + tags.toString());
	}
	
	public TagParams getTagParams(String key) {
			return tags.get(key);
	}

	@SuppressWarnings("rawtypes")
	public class TagParams{
		
		private Object min;
		private Object max;
		private Integer listType;
		private Class nbtClass;
		
		public TagParams(Class nbtClass, Object min, Object max, Integer listContainType) {
			this.nbtClass= nbtClass; 
			this.min = min;
			this.max = max;
			this.listType = listContainType;
		}

		public Class getTagNbtClass() {
			return nbtClass;
		}
		
		public Object getMin() {
			return min;
		}
		public Object getMax() {
			return max;
		}
		public Integer getListType() {
			return listType;
		}
		@Override
		public String toString() {
			return "[Params: NbtClass:" + nbtClass.getName() + ", min:" + min + ", max:" + max + ", ListType:" + listType + "]";
		}
	}
}
