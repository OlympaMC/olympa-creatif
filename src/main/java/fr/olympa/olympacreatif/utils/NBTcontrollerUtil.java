package fr.olympa.olympacreatif.utils;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftMagicNumbers.NBT;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fr.olympa.olympacreatif.utils.TagsValues.TagParams;
import net.minecraft.server.v1_15_R1.MojangsonParser;
import net.minecraft.server.v1_15_R1.NBTBase;
import net.minecraft.server.v1_15_R1.NBTTagByte;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.NBTTagDouble;
import net.minecraft.server.v1_15_R1.NBTTagFloat;
import net.minecraft.server.v1_15_R1.NBTTagInt;
import net.minecraft.server.v1_15_R1.NBTTagList;
import net.minecraft.server.v1_15_R1.NBTTagLong;
import net.minecraft.server.v1_15_R1.NBTTagShort;
import net.minecraft.server.v1_15_R1.NBTTagString;

public abstract class NBTcontrollerUtil {

	private final static int recurIndexMax = 50;
	private final static int maxListSize = 10;
	
	private final static TagsValues allowedTags = new TagsValues();

	public static NBTTagCompound getValidTags(String string) {
		try {
			return getValidTags(MojangsonParser.parse(string));
		} catch (CommandSyntaxException e) {
			return new NBTTagCompound();
		}
	}
	
	public static NBTTagCompound getValidTags(NBTTagCompound tag) {
		//Bukkit.broadcastMessage("Tag initial : " + tag);
		//Bukkit.broadcastMessage("Tag vérifié : " + getValidTags(tag, 0));
		return getValidTags(getValidTags(tag, 0), 0);
	}
	
	private static NBTTagCompound getValidTags(NBTTagCompound tag, int recurIndex) {
		
		//Bukkit.broadcastMessage("Tag étudié : " + tag.asString());
		if (tag == null)
			return new NBTTagCompound();		
		
		for (String key : tag.getKeys()) {
			//récursivité pour les clés contenant d'autres tags
			if (tag.get(key) instanceof NBTTagCompound)
				if (recurIndex < recurIndexMax)
					tag.set(key, getValidTags(tag.getCompound(key), recurIndex + 1));
				else
					return new NBTTagCompound();
			
			//traitement si le tag n'est pas un compound
			else {
				TagParams params = allowedTags.getTagParams(key);
				
				if (params == null)
					return new NBTTagCompound();
				
				//Bukkit.broadcastMessage("tag : " + tag.get(key).asString() + " - " + params.toString());
				//Bukkit.broadcastMessage("classe : " + tag.get(key).getClass().getName());
				
				//si le compound est une liste
				if (NBTTagList.class.equals(tag.get(key).getClass()) && params.getListType() != null) {
					NBTTagList oldList = tag.getList(key, params.getListType());
					NBTTagList newList = new NBTTagList();
					
					//Bukkit.broadcastMessage("LIST : " + oldList.asString());
					
					//pour chaque élément de la liste, vérification
					for (int i = 0 ; i < Math.min(oldList.size(), maxListSize) ; i++) 

						if (params.getListType() == NBT.TAG_COMPOUND) {
							//Bukkit.broadcastMessage("tag liste : " + oldList.getCompound(i).asString() + " - " + getValidTags(oldList.getCompound(i)).asString());
							newList.add(getValidTags(oldList.getCompound(i), recurIndex + 1));	
						}else {
							//Bukkit.broadcastMessage("list contains : " + oldList.get(i).asString());
							if (isValueValid(params, oldList.get(i)))
								newList.add(oldList.get(i));
					
					tag.set(key, newList);
					}						
				}else {
					//Bukkit.broadcastMessage("Clé étudiée : " + key + " - valeur : " + tag.get(key).asString());
					
					//si le tag n'est ni un compound ni une liste, vérification				
					if (!isValueValid(params, tag.get(key)))
						tag.remove(key);	
				}				
			}
		}
		return tag;
	}
	
	@SuppressWarnings("rawtypes")
	private static boolean isValueValid(TagParams params, NBTBase value) {
		
		//Bukkit.broadcastMessage("param value : " + params.getTagNbtClass().toString() + " - value class : " + value.getClass().toString());
		
		if (!params.getTagNbtClass().equals(value.getClass()))
			return false;
		
		Class tagClass = value.getClass();
		
		//Bukkit.broadcastMessage("tag : " + value.asString() + " - paramType : " + params.getTagNbtClass().toString());
		
		if (tagClass.equals(NBTTagInt.class))
			if (((NBTTagInt)value).asInt() >= (Integer)params.getMin() && ((NBTTagInt)value).asInt() <= (Integer)params.getMax())
				return true;
		
		if (tagClass.equals(NBTTagByte.class))
			if (((NBTTagByte)value).asByte() >= (Byte)params.getMin() && ((NBTTagByte)value).asByte() <= (Byte)params.getMax())
				return true;
		
		if (tagClass.equals(NBTTagDouble.class))
			if (((NBTTagDouble)value).asDouble() >= (Double)params.getMin() && ((NBTTagDouble)value).asDouble() <= (Double)params.getMax())
				return true;
		
		if (tagClass.equals(NBTTagFloat.class))
			if (((NBTTagFloat)value).asFloat() >= (Float)params.getMin() && ((NBTTagFloat)value).asFloat() <= (Float)params.getMax())
				return true;
		
		if (tagClass.equals(NBTTagLong.class))
			if (((NBTTagLong)value).asLong() >= (Long)params.getMin() && ((NBTTagLong)value).asLong() <= (Long)params.getMax())
				return true;
		
		if (tagClass.equals(NBTTagShort.class))
			if (((NBTTagShort)value).asShort() >= (Short)params.getMin() && ((NBTTagShort)value).asShort() <= (Short)params.getMax())
				return true;
		
		if (tagClass.equals(NBTTagString.class))
			if (((NBTTagString)value).asString().length() >= (Integer)params.getMin() && ((NBTTagString)value).asString().length() <= (Integer)params.getMax())
				return true;
		
		return false;
	}
}
