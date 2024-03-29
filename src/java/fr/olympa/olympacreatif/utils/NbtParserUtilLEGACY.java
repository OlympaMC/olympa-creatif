package fr.olympa.olympacreatif.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fr.olympa.olympacreatif.commandblocks.CbTeam;
import net.minecraft.server.v1_16_R3.MojangsonParser;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagList;

public class NbtParserUtilLEGACY {

	private static List<String> copyValue = new ArrayList<String>();
	
	public NbtParserUtilLEGACY() {
		copyValue.add("CustomNameVisible");
		copyValue.add("NoAI");
		copyValue.add("Glowing");
		copyValue.add("Health");
		copyValue.add("HandDropChances");
		copyValue.add("ArmorDropChances");
		copyValue.add("Invulnerable");
		copyValue.add("Silent");
		copyValue.add("powered");
		copyValue.add("PersistenceRequired");
		copyValue.add("NoGravity");
		copyValue.add("ShowParticles");

		copyValue.add("VillagerData");
		
		copyValue.add("generic.knockbackResistance");
		copyValue.add("generic.maxHealth");
		copyValue.add("generic.attackDamage");
		copyValue.add("generic.armor");
		copyValue.add("generic.armorToughness");

		copyValue.add("id");
		copyValue.add("Count");
		copyValue.add("Unbreakable");
		copyValue.add("HideFlags");
		copyValue.add("AttributeModifiers");
		copyValue.add("CanPlaceOn");
		copyValue.add("CanDestroy");
	}
	
	public static NBTTagCompound getEntityNbtDatae(NBTTagCompound oldTag, EntitySourceType sourceType) {
		NBTTagCompound newTag = new NBTTagCompound();
		
		try {			
			//récupération tag nbt initial entré par le joueur
			if (oldTag.hasKey("EntityTag"))
				oldTag = oldTag.getCompound("EntityTag");

			if (oldTag.hasKey("SpawnData"))
				oldTag = oldTag.getCompound("SpawnData");
			
			//copie des tags qui n'ont pas à être modifiés
			for (String s : copyValue)
				if (oldTag.hasKey(s))
					newTag.set(s, oldTag.get(s));
			
			//check customname
			if (oldTag.hasKey("CustomName")) {
				if (!oldTag.getString("CustomName").startsWith("[")) {
					String name = oldTag.getString("CustomName").replace("{", "").replace("}", "").replace("_", " ");
					
					if (!name.startsWith("\""))
						name = "\"" + name;
					
					if (!name.endsWith("\""))
						name = name + "\"";
					
					name = ChatColor.translateAlternateColorCodes('&', name);

					newTag.setString("CustomName", name);	
				}else {
					String name = oldTag.getString("CustomName");
					newTag.setString("CustomName", name);
				}
					
			}
			
			//vérification de la taille du slime
			if (oldTag.hasKey("Size"))
				newTag.setInt("Size", Math.min(oldTag.getInt("Size"), 10));
			
			//vérification des tags d'items
			//items dans la main
			if (oldTag.hasKey("HandItems")) {
				NBTTagList list = new NBTTagList();
				
				int iMax = oldTag.getList("HandItems", 10).size();
				for (int i = 0 ; i < iMax ; i++) 
					list.add(getValidItemm(oldTag.getList("HandItems", 10).getCompound(i)));
				
				newTag.set("HandItems", list);
			}
			
			//items d'armure
			if (oldTag.hasKey("ArmorItems")) {
				NBTTagList list = new NBTTagList();
				
				int iMax = oldTag.getList("ArmorItems", 10).size();
				for (int i = 0 ; i < iMax ; i++) 
					list.add(getValidItemm(oldTag.getList("ArmorItems", 10).getCompound(i)));
				
				newTag.set("ArmorItems", list);
			}
			
			if (oldTag.hasKey("ActiveEffects")) {
				NBTTagList list = new NBTTagList();
				
				int iMax = oldTag.getList("ActiveEffects", 10).size();
				for (int i = 0 ; i < iMax ; i++) 
					list.add(getValidEffect(oldTag.getList("ActiveEffects", 10).getCompound(i)));
				
				newTag.set("ActiveEffects", list);
			}
			
			if (oldTag.hasKey("Attributes")) {
				NBTTagList list = new NBTTagList();
				
				int iMax = oldTag.getList("Attributes", 10).size();
				for (int i = 0 ; i < iMax ; i++) {
					NBTTagCompound tag = new NBTTagCompound();
					list.add(getValidAttributee(oldTag.getList("Attributes", 10).getCompound(i)));	
				}
				
				newTag.set("Attributes", list);
			}
			
			if (oldTag.hasKey("Offers")) {
				NBTTagList list = new NBTTagList();
				int iMax = oldTag.getCompound("Offers").getList("Recipes", 10).size();

				for (int i = 0 ; i < iMax ; i++) {
					NBTTagCompound tag = new NBTTagCompound();
					list.add(getValidShopkeeperExchanges(oldTag.getCompound("Offers").getList("Recipes", 10).getCompound(i)));	
				}
				
				NBTTagCompound tag2 = new NBTTagCompound();
				tag2.set("Recipes", list);
				newTag.set("Offers", tag2);
			}
			
			NBTTagCompound finalTag = new NBTTagCompound();
			
			switch (sourceType) {
			case EGG:
				finalTag.set("EntityTag", newTag);
				break;
			case SPAWNER:
				if (oldTag.hasKey("id"))
					newTag.set("id", oldTag.get("id"));
				
				finalTag.set("SpawnData", newTag);

				finalTag.setInt("SpawnCount", 1);
				finalTag.setInt("MaxNearbyEntities", 6);
				finalTag.setInt("RequiredPlayerRange", 10);
				finalTag.setInt("SpawnRange", 4);
				finalTag.setInt("Delay", 20);
				finalTag.setInt("MinSpawnDelay", 80);
				finalTag.setInt("MaxSpawnDelay", 120);
				break;
			case SUMMON:
				finalTag = newTag;
				break;
			}

			//Bukkit.broadcastMessage("OLD TAG : " + oldTag.asString());
			//Bukkit.broadcastMessage("FINAL TAG : " + finalTag.asString());
			
			return finalTag;
		} catch (Exception e) {
			return null;
		}
	}
	
	public static NBTTagCompound getValidAttributee(NBTTagCompound oldTag) {
		NBTTagCompound newTag = new NBTTagCompound();
		
		if (!oldTag.hasKey("Base") || !oldTag.hasKey("Name"))
			return newTag;
		
		for (String s : copyValue)
			if (oldTag.getString("Name").equals(s)) {
				newTag.setString("Name", s);
				newTag.set("Base", oldTag.get("Base"));
			}
		
		if (oldTag.getString("Name").equals("generic.attackKnockback")) {
			newTag.setString("Name", "generic.attackKnockback");	
			newTag.setFloat("Base", Math.min(oldTag.getFloat("Base"), 2));
		}
		

		if (oldTag.getString("Name").equals("generic.movementSpeed")) {
			newTag.setString("Name", "generic.movementSpeed");	
			newTag.setFloat("Base", Math.min(oldTag.getFloat("Base"), 0.6f));
		}
		
		return newTag;
	}
	
	//TODO
	public static NBTTagCompound getValidItemm(NBTTagCompound oldTag) {
		NBTTagCompound newTag = new NBTTagCompound();

		for (String s : copyValue)
			if (oldTag.hasKey(s))
				newTag.set(s, oldTag.get(s));
		
		if (oldTag.hasKey("tag"))
			if (oldTag.getCompound("tag").hasKey("display")) {
				NBTTagCompound tagDisplay = oldTag.getCompound("tag").getCompound("display");
				
				JSONObject jsonName = null;

				//recopiage nom
				if (tagDisplay.hasKey("Name")) {
					String nameTag = oldTag.getCompound("tag").getCompound("display").getString("Name");
					
					try {
						jsonName = (JSONObject) new JSONParser().parse(nameTag);
					} catch (ParseException e) {
					}
				}
			}
				if (oldTag.getCompound("tag").getCompound("display").hasKey("Name")) {
					NBTTagCompound tag1 = new NBTTagCompound();
					NBTTagCompound tag2 = new NBTTagCompound();
					
					String nameTag = oldTag.getCompound("tag").getCompound("display").getString("Name");
					
					JSONObject json;
					
					try {
						json = (JSONObject) new JSONParser().parse(nameTag);
					} catch (ParseException e) {
						return new NBTTagCompound();
					}
					
					//ajout lore
					if (oldTag.getCompound("tag").getCompound("display").hasKey("Lore"))
						tag2.set("Lore", oldTag.getCompound("tag").getCompound("display").get("Lore"));
					
					//coloration du nom de l'item
					if (json.get("text") != null) {
						tag2.setString("Name", "{\"text\":\"" + ChatColor.translateAlternateColorCodes('&', (String) json.get("text")) + "\"}");
						tag1.set("display", tag2);
						newTag.set("tag", tag1);	
					}
				}
		
		if (oldTag.hasKey("Enchantments")) {
			NBTTagList list = new NBTTagList();
			
			int iMax = oldTag.getList("ActiveEffects", 10).size();
			for (int i = 0 ; i < iMax ; i++) {
				NBTTagCompound tag = oldTag.getList("Enchantments", 10).getCompound(i);
				
				tag.setInt("Amplifier", Math.min(tag.getInt("Amplifier"), 4));
				list.add(tag);
			}
			
			newTag.set("Enchantments", list);
		}
		return newTag;
	}
	
	public static NBTTagCompound getValidEffect(NBTTagCompound oldTag) {
		NBTTagCompound newTag = new NBTTagCompound();

		if (oldTag.hasKey("Id"))
			newTag.set("Id", oldTag.get("Id"));

		if (oldTag.hasKey("Duration"))
			newTag.set("Duration", oldTag.get("Duration"));

		if (oldTag.hasKey("Amplifier"))
			newTag.setInt("Amplifier", Math.min(oldTag.getInt("Amplifier"), 4));
		
		if (oldTag.hasKey("ShowParticles"))
			newTag.set("ShowParticles", oldTag.get("ShowParticles"));
		
		return newTag;
	}
	
	public static NBTTagCompound getValidShopkeeperExchanges(NBTTagCompound oldTag) {
		NBTTagCompound tag = new NBTTagCompound();

		if (oldTag.hasKey("buy"))
			tag.set("buy", getValidItemm(oldTag.getCompound("buy")));
		if (oldTag.hasKey("buyB"))
			tag.set("buyB", getValidItemm(oldTag.getCompound("buyB")));
		if (oldTag.hasKey("sell"))
			tag.set("sell", getValidItemm(oldTag.getCompound("sell")));

		if (oldTag.hasKey("maxUses"))
			tag.set("maxUses", oldTag.get("maxUses"));
		if (oldTag.hasKey("rewardExp"))
			tag.set("rewardExp", oldTag.get("rewardExp"));
			
		return tag;
	}
	
	public enum EntitySourceType{
		SPAWNER,
		EGG,
		SUMMON;
	}
	/*
	//renvoie le NBTTagCompound compris dans la liste de strings
	public static NBTTagCompound getTagFromStringss(String[] args) {
		
		String concat = "";
		for (String s : args) {
			concat += s + " ";
		}
		
		return getTagFromStringg(concat);
	}
	
	*/
	public static NBTTagCompound getTagFromStringg(String arg) {
		try {
			if (!arg.contains("{") || !arg.contains("}")) 
				return new NBTTagCompound();
			
			arg = arg.substring(arg.indexOf("{"), arg.lastIndexOf("}")+1);
			
			return MojangsonParser.parse(arg);
		} catch (CommandSyntaxException e) {
			return new NBTTagCompound();
		}
	}
	
	//renvoie un string comprenant les parses de tous les tags de la liste
	public static String parseJsonFromListt(NBTTagList nbtList) {
		String text = "";

		for (int i = 0 ; i < nbtList.size() ; i++) 	
			text += parseJsonFromCompoundd(nbtList.getCompound(i));
		
		
		return text;
	}
	
	//renvoie un string formaté selon les paramètres json du tag
	public static String parseJsonFromCompoundd(NBTTagCompound tag) {
		
		String text = "§r";
		
		if (tag.hasKey("italic"))
			text += ChatColor.ITALIC;
		if (tag.hasKey("bold"))
			text += ChatColor.BOLD;
		if (tag.hasKey("strikethrough"))
			text += ChatColor.STRIKETHROUGH;
		if (tag.hasKey("underlined"))
			text += ChatColor.UNDERLINE;
		if (tag.hasKey("obfuscated"))
			text += ChatColor.MAGIC;
		if (tag.hasKey("color"))
			text += CbTeam.ColorType.getColor(tag.getString("color"));
		
		if (tag.hasKey("text"))
			text += tag.getString("text");	
		
		return text;
	}
	
	//renvoie un NBTTagList à partir d'arguments d'une commande
	/*
	public static NBTTagList getListCompoundFromString(String[] args) {
		String concat = "";
		int k = 0;
		for (String s : args) {
			if (k > 0 && k < args.length - 1)
				concat += s + " ";
			k++;
		}
		
		concat += args[args.length-1];
		
		return getListCompoundFromString(concat);
	}
	*/
	/*
	//renvoie un NBTTagList à partir d'arguments d'un string json
	public static NBTTagList getListCompoundFromStringg(String concat) {

		//si le concat est formé comme un NBTTagCompound
		if (concat.startsWith("{")) {
			try {
				NBTTagCompound tag = MojangsonParser.parse(concat);
				NBTTagList list = new NBTTagList();
				
				list.add(tag);
				return list;
			} catch(CommandSyntaxException e) {
				return new NBTTagList();
			}	
		}
		
		//si le concat est formaté comme une liste
		if (concat.startsWith("[")) {
			try {
				concat = "{list:" + concat.replace("\"\",", "").replace(",\"\"", "") + "}";
				
				return MojangsonParser.parse(concat).getList("list", 10);
			} catch (CommandSyntaxException e) {
				return new NBTTagList();
			}	
		}
		
		return new NBTTagList();
	}*/
}
