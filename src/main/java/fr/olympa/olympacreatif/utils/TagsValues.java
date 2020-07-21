package fr.olympa.olympacreatif.utils;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.craftbukkit.v1_15_R1.util.CraftMagicNumbers.NBT;

import com.sk89q.worldedit.extent.clipboard.io.NBTSchematicReader;

import net.minecraft.server.v1_15_R1.NBTTagByte;
import net.minecraft.server.v1_15_R1.NBTTagInt;
import net.minecraft.server.v1_15_R1.NBTTagList;
import net.minecraft.server.v1_15_R1.NBTTagShort;
import net.minecraft.server.v1_15_R1.NBTTagString;

public class TagsValues {

	private Map<String, TagParams> tags = new HashMap<String, TagsValues.TagParams>();
	
	public TagsValues() {
		tags.put("Name", new TagParams(NBTTagString.class, 0, 200, null));
		tags.put("Lore", new TagParams(NBTTagString.class, 0, 200, NBT.TAG_STRING));

		tags.put("CustomName", new TagParams(NBTTagString.class, 0, 200, null));
		tags.put("CustomNameVisible", new TagParams(NBTTagInt.class, 0, 1, null));
		tags.put("Invulnerable", new TagParams(NBTTagInt.class, 0, 1, null));
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
