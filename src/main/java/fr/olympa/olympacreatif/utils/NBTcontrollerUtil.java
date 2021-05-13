package fr.olympa.olympacreatif.utils;

import java.util.HashSet;

import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.EnumUtils;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers.NBT;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import fr.olympa.olympacreatif.commandblocks.commands.CbCommand;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.utils.TagsValues.TagParams;
import net.minecraft.server.v1_16_R3.NBTBase;
import net.minecraft.server.v1_16_R3.NBTTagByte;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagDouble;
import net.minecraft.server.v1_16_R3.NBTTagFloat;
import net.minecraft.server.v1_16_R3.NBTTagInt;
import net.minecraft.server.v1_16_R3.NBTTagList;
import net.minecraft.server.v1_16_R3.NBTTagLong;
import net.minecraft.server.v1_16_R3.NBTTagShort;
import net.minecraft.server.v1_16_R3.NBTTagString;

public abstract class NBTcontrollerUtil {

	private final static int recurIndexMax = 20;
	private final static int maxListSize = 10;
	
	private final static TagsValues allowedTags = new TagsValues();
	
	//set default minecraft JsonReader lenient
	/*static {
		try {
			Field gsonField = ChatDeserializer.class.getDeclaredField("a");
			gsonField.setAccessible(true);
			
			Field fieldModifiers = Field.class.getDeclaredField("modifiers");
			fieldModifiers.setAccessible(true);
			fieldModifiers.setInt(gsonField, gsonField.getModifiers() & ~Modifier.FINAL);
			
			gsonField.set(null, new GsonBuilder().setLenient().create());
			
			OlympaCreatifMain.getInstance().getLogger().info("§aDefault ChatDeserializer Gson instance set lenient.");
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			OlympaCreatifMain.getInstance().getLogger().warning("§cFailed to set default ChatDeserializer Gson instance lenient!");
			e.printStackTrace();
		}
	}*/

	public static NBTTagCompound getValidTags(String string, Player requester) {
		return getValidTags(new OcMojangsonParser(string.replace("minecraft:", "")).parse(requester), requester);
	}
	
	public static NBTTagCompound getValidTags(String string) {
		return getValidTags(string, null);
		/*
		try {
			//Bukkit.broadcastMessage("String tag : " + string + " - parsed tag : " + MojangsonParser.parse(string.replace("minecraft:", "")));
			return getValidTags(MojangsonParser.parse(string.replace("minecraft:", "")));
		} catch (CommandSyntaxException e) {
			return new NBTTagCompound();
		}*/
	}
	 
	public static NBTTagCompound getValidTags(NBTTagCompound tag, Player requester) {
		return getValidTags(tag, 0, requester);
	}
	
	private static NBTTagCompound getValidTags(NBTTagCompound tag, int recurIndex, Player requester) {
		
		//Bukkit.broadcastMessage("Tag étudié : " + tag.asString());
		if (tag == null)
			return new NBTTagCompound();		
		
		for (String key : new HashSet<String>(tag.getKeys())) {
			TagParams params = allowedTags.getTagParams(key);
			
			//récursivité pour les clés contenant d'autres tags
			if (tag.get(key) instanceof NBTTagCompound)
				if (params != null && params.getTagNbtClass().equals(NBTTagCompound.class))
					continue;
				else
					if (recurIndex < recurIndexMax)
						tag.set(key, getValidTags(tag.getCompound(key), recurIndex + 1, requester));
					else
						return new NBTTagCompound();
			
			//traitement si le tag n'est pas un compound
			else {
				
				//System.out.println("key : " + key + " - class : " + tag.get(key).getClass().getName() + " - params : " + params);
				
				if (params == null) {
					tag.remove(key);
					if (requester != null)
						OCmsg.TAG_CHECKER_UNAUTHORIZED_TAG.send(requester, key);
					continue;
				}
				//remove tag "id" si c'est un oeuf (pas possible de le faire dans la liste, le tag id est utilisé pour d'autres choses...)
				if (key.equals("id") && EnumUtils.isValidEnum(EntityType.class, CbCommand.getUndomainedString(tag.getString(key)))) {
					tag.remove(key);
					if (requester != null)
						OCmsg.TAG_CHECKER_UNAUTHORIZED_TAG.send(requester, key);
					continue;
				}
				
				//Bukkit.broadcastMessage("tag : " + tag.get(key).asString() + " - " + params.toString());
				//Bukkit.broadcastMessage("classe : " + tag.get(key).getClass().getName());
				
				//si le compound est une liste
				if (NBTTagList.class.equals(tag.get(key).getClass()) && params.getListType() != null) {
					NBTTagList oldList = tag.getList(key, params.getListType());
					NBTTagList newList = new NBTTagList();
					
					System.out.println("LIST : " + oldList.asString());
					
					//pour chaque élément de la liste, vérification
					for (int i = 0 ; i < Math.min(oldList.size(), maxListSize) ; i++) {

						if (params.getListType() == NBT.TAG_COMPOUND) {
							//Bukkit.broadcastMessage("tag liste : " + oldList.getCompound(i).asString() + " - " + getValidTags(oldList.getCompound(i)).asString());
							newList.add(getValidTags(oldList.getCompound(i), recurIndex + 1, requester));	
						}else {
							//System.out.println("list contains : " + oldList.get(i).asString() + " ----- parameter : " + params + " ----- is valid : " + isValueValid(params, oldList.get(i)));
							if (isValueValid(params, oldList.get(i)))
								newList.add(oldList.get(i));
							else
								if (requester != null)
									OCmsg.TAG_CHECKER_UNAUTHORIZED_VALUE.send(requester, params);
						}
					}
					
					tag.set(key, newList);
				}else {
					//transformation du tag si c'est un entier mais d'un mauvais type
					
					if (!params.getTagNbtClass().equals(tag.get(key).getClass()) && isTagInteger(tag.get(key)))
						if (tag.get(key) instanceof NBTTagShort)
							tag.setInt(key, tag.getShort(key));
						else if (tag.get(key) instanceof NBTTagByte)
							tag.setInt(key, tag.getByte(key));
					
					//si le tag n'est ni un compound ni une liste, vérification
					if (!isValueValid(params, tag.get(key)))
						tag.remove(key);
					else if (key.equals("CustomName") && tag.get(key) instanceof NBTTagString && !(tag.getString(key).startsWith("\"") && tag.getString(key).endsWith("\"")))
						tag.setString(key, "\"" + tag.getString(key) + "\"");
				}				
			}
		}
		
		//System.out.println("Returned tag : " + tag.asString());
		return tag;
	}
	
	@SuppressWarnings("rawtypes")
	private static boolean isValueValid(TagParams params, NBTBase nbt) {
		
		//Bukkit.broadcastMessage(params + "\nnbt class : " + nbt.getClass().getName() + " - nbt value : " + nbt.asString());
		
		if (!params.getTagNbtClass().equals(nbt.getClass()))
			return false;
		
		Class tagClass = nbt.getClass();
		
		//Bukkit.broadcastMessage("tag : " + value.asString() + " - paramType : " + params.getTagNbtClass().toString());
		
		if (tagClass.equals(NBTTagInt.class))
			if (((NBTTagInt)nbt).asInt() >= (Integer)params.getMin() && ((NBTTagInt)nbt).asInt() <= (Integer)params.getMax())
				return true;
		
		if (tagClass.equals(NBTTagString.class))
			if (((NBTTagString)nbt).asString().length() >= (Integer)params.getMin() && ((NBTTagString)nbt).asString().length() <= (Integer)params.getMax())
				return true;
		
		if (tagClass.equals(NBTTagFloat.class))
			if (((NBTTagFloat)nbt).asFloat() >= params.getMin() && ((NBTTagFloat)nbt).asFloat() <= params.getMax())
				return true;
		
		if (tagClass.equals(NBTTagLong.class))
			if (((NBTTagLong)nbt).asLong() >= params.getMin() && ((NBTTagLong)nbt).asLong() <= params.getMax())
				return true;
		
		if (tagClass.equals(NBTTagByte.class))
			if (((NBTTagByte)nbt).asByte() >= params.getMin() && ((NBTTagByte)nbt).asByte() <= params.getMax())
				return true;
		
		if (tagClass.equals(NBTTagDouble.class))
			if (((NBTTagDouble)nbt).asDouble() >= params.getMin() && ((NBTTagDouble)nbt).asDouble() <= params.getMax())
				return true;
		
		if (tagClass.equals(NBTTagShort.class))
			if (((NBTTagShort)nbt).asShort() >= params.getMin() && ((NBTTagShort)nbt).asShort() <= params.getMax())
				return true;
		
		return false;
	}
	
	private static boolean isTagInteger(NBTBase nbt) {
		if (nbt instanceof NBTTagInt || nbt instanceof NBTTagShort || nbt instanceof NBTTagLong || nbt instanceof NBTTagByte)
			return true;
		else
			return false;
	}
}