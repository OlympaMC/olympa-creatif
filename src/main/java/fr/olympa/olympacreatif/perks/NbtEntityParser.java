package fr.olympa.olympacreatif.perks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import net.minecraft.server.v1_15_R1.MojangsonParser;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.NBTTagList;

public class NbtEntityParser {

	private OlympaCreatifMain plugin;
	private List<String> tagsCopyValue = new ArrayList<String>();
	
	public NbtEntityParser(OlympaCreatifMain plugin) {
		this.plugin = plugin;

		tagsCopyValue.add("CustomNameVisible");
		tagsCopyValue.add("NoAI");
		tagsCopyValue.add("Glowing");
		tagsCopyValue.add("Health");
		tagsCopyValue.add("HandDropChances");
		tagsCopyValue.add("Invulnerable");
		tagsCopyValue.add("Silent");
		tagsCopyValue.add("powered");
		tagsCopyValue.add("PersistenceRequired");
		tagsCopyValue.add("NoGravity");
		tagsCopyValue.add("ShowParticles");
		
		tagsCopyValue.add("generic.knockbackResistance");
		tagsCopyValue.add("generic.maxHealth");
		tagsCopyValue.add("generic.attackDamage");
		tagsCopyValue.add("generic.armor");
		tagsCopyValue.add("generic.armorToughness");
	}
	
	public NBTTagCompound getEntityNbtData(String data) {
		NBTTagCompound oldTag = new NBTTagCompound();
		NBTTagCompound newTag = new NBTTagCompound();
		
		try {
			oldTag = MojangsonParser.parse(data).getCompound("EntityTag");
		} catch (CommandSyntaxException e) {
			return null;
		}
		
		//copie des tags qui n'ont pas à être modifiés
		for (String s : tagsCopyValue)
			if (oldTag.hasKey(s))
				newTag.set(s, oldTag.get(s));
		
		//check customname
		if (oldTag.hasKey("CustomName")) {
			String name = "\"" + oldTag.getString("CustomName").replace("{", "").replace("}", "").replace("_", " ") + "\"";
			
			name = ChatColor.translateAlternateColorCodes('&', name);
			
			newTag.setString("CustomName", name);	
		}
		
		//vérification des tags d'items
		//items dans la main
		if (oldTag.hasKey("HandItems")) {
			NBTTagList list = new NBTTagList();
			
			int iMax = oldTag.getList("HandItems", 10).size();
			for (int i = 0 ; i < iMax ; i++) 
				list.add(getValidItem(oldTag.getList("HandItems", 10).getCompound(i)));
			
			newTag.set("HandItems", list);
		}
		//items d'armure
		if (oldTag.hasKey("ArmorItems")) {
			NBTTagList list = new NBTTagList();
			
			int iMax = oldTag.getList("ArmorItems", 10).size();
			for (int i = 0 ; i < iMax ; i++) 
				list.add(getValidItem(oldTag.getList("ArmorItems", 10).getCompound(i)));
			
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
				list.add(getValidAttribute(oldTag.getList("Attributes", 10).getCompound(i)));	
			}
			
			newTag.set("Attributes", list);
		}

		Bukkit.broadcastMessage(oldTag.asString());
		Bukkit.broadcastMessage(newTag.asString());
		
		NBTTagCompound finalTag = new NBTTagCompound();
		finalTag.set("EntityTag", newTag);
				
		return finalTag;
	}
	
	private NBTTagCompound getValidAttribute(NBTTagCompound oldTag) {
		NBTTagCompound newTag = new NBTTagCompound();
		
		if (!oldTag.hasKey("Base") || !oldTag.hasKey("Name"))
			return newTag;
		
		for (String s : tagsCopyValue)
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
	

	private NBTTagCompound getValidItem(NBTTagCompound oldTag) {
		NBTTagCompound newTag = new NBTTagCompound();
		
		if (oldTag.hasKey("id"))
			newTag.set("id", oldTag.get("id"));
		
		if (oldTag.hasKey("tag"))
			if (oldTag.getCompound("tag").hasKey("display"))
				if (oldTag.getCompound("tag").getCompound("display").hasKey("Name")) {
					NBTTagCompound tag1 = new NBTTagCompound();
					NBTTagCompound tag2 = new NBTTagCompound();
					NBTTagCompound tag3 = new NBTTagCompound();

					//Bukkit.broadcastMessage(oldTag.getCompound("tag").getCompound("display").get("Name").toString());
					//Bukkit.broadcastMessage(oldTag.getCompound("tag").getCompound("display").getString("Name").toString());
					
					tag3.setString("Name", oldTag.getCompound("tag").getCompound("display").get("Name").toString().replace("'", "").replace("{", "").replace("}", ""));
					tag2.set("display", tag3);
					tag1.set("tag", tag2);
					newTag.set("tag", tag1);
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
	
	private NBTTagCompound getValidEffect(NBTTagCompound oldTag) {
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
}
