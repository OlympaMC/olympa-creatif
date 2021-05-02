package fr.olympa.olympacreatif.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.EnumUtils;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers.NBT;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import fr.olympa.olympacreatif.commandblocks.CbObjective;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_16_R3.MojangsonParser;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagList;

public abstract class JSONtextUtil {
	
	public static TextComponent getJsonText(String component) {
		return getJsonText(null, component);
	}
	
	public static TextComponent getJsonText(CbCommand cmd, String component) {
		
		TextComponent text = new TextComponent();
		
		if (!component.startsWith("["))
			component = "{rawText:[" + component + "]}";
		else
			component = "{rawText:" + component + "}";

		
		//Bukkit.broadcastMessage("basic string tag : " + listAsString);
		
		try {
			
			//Bukkit.broadcastMessage("try to parse : " + component);
			
			NBTTagList mainTag = MojangsonParser.parse(component.replace("=", ":")).getList("rawText", NBT.TAG_COMPOUND);
			
			Bukkit.broadcastMessage("parse : " + mainTag.asString());
			
			for (int i = 0 ; i < mainTag.size() ; i++) {
				
				NBTTagCompound tag = mainTag.getCompound(i);
				TextComponent textPart = applyUniversalTags(tag);
				
				
				if (tag.hasKey("text")) {
					textPart.addExtra(tag.getString("text"));

					//ajout HoverEvent
					if (tag.hasKey("hoverEvent") && cmd != null) {
						NBTTagCompound subTag = tag.getCompound("hoverEvent");
						
						if (subTag == null)
							continue;
						
						String subString = subTag.getString("contents");
						
						if (subString == null)
							continue;
						
						textPart.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', subString.replace("{", ""))).create()));
					}
					
					//ajout ClickEvent
					if (tag.hasKey("clickEvent") && cmd != null) {
						NBTTagCompound subTag = tag.getCompound("clickEvent");
						
						if (subTag == null)
							continue;
						
						String value = subTag.getString("value");
						
						if (value == null || !value.startsWith("/trigger "))
							continue;
						
						textPart.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, value));
					}
					
				}else if (tag.hasKey("selector") && cmd != null) {
					List<Entity> list = cmd.parseSelector(tag.getString("selector").replace(":", "="), false);
					
					String concat = list.stream().map(e -> {
						if (e.getType() != EntityType.PLAYER) {
							return e.getCustomName() == null ? e.getName() : e.getCustomName();
						} else
							return ((Player)e).getName();
					}).collect(Collectors.joining(", "));
					
					/*for (int j = 0 ; j < list.size() - 1 ; j++)
						if (list.get(j).getType() == EntityType.PLAYER)
							concat += ((Player)list.get(j)).getDisplayName() + ", ";
						else
							if (list.get(j).getCustomName() != null)
								concat += list.get(j).getCustomName() + ", ";
							else
								concat += list.get(j).getName() + ", ";

					if (list.size() > 0)
						if (list.get(list.size() - 1).getType() == EntityType.PLAYER)
							concat += ((Player)list.get(list.size() - 1)).getDisplayName();
						else
							if (list.get(list.size() - 1).getCustomName() != null)
								concat += list.get(list.size() - 1).getCustomName() + ", ";
							else
								concat += list.get(list.size() - 1).getName() + ", ";*/
					
					//Bukkit.broadcastMessage("selector : '" + concat + "'");
					textPart.addExtra(concat);
					
				}else if (tag.hasKey("score") && cmd != null) {
					NBTTagCompound subTag = tag.getCompound("score");
					
					if (subTag == null)
						continue;
					
					String name = subTag.getString("name");
					String obj = subTag.getString("objective");
					
					if (name == null || obj == null)
						continue;

					List<Entity> list = cmd.parseSelector(name.replace(":", "="), false);
					//System.out.println("LIST for json parser : " + list + " FROM " + name);
					CbObjective cbObj = cmd.getPlot().getCbData().getObjective(obj);
					
					if (list.size() == 0 || cbObj == null)
						continue;
					
					textPart.addExtra("" + cbObj.get(list.get(0)));
				}
				
				//Bukkit.broadcastMessage("textPart : " + textPart.toString());
				text.addExtra(textPart);
				//Bukkit.broadcastMessage("text : " + text.toString());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return text;
	}
	
	private static TextComponent applyUniversalTags(NBTTagCompound tag) {
		TextComponent subText = new TextComponent();
		
		//Bukkit.broadcastMessage("tag : " + tag.asString());
		
		if (tag.hasKey("bold"));
			subText.setBold(tag.getBoolean("bold"));
		if (tag.hasKey("italic"));
		subText.setItalic(tag.getBoolean("italic"));
		if (tag.hasKey("strikethrough"));
		subText.setStrikethrough(tag.getBoolean("strikethrough"));
		if (tag.hasKey("underlined"));
		subText.setUnderlined(tag.getBoolean("underlined"));
		if (tag.hasKey("obfuscated"));
		subText.setObfuscated(tag.getBoolean("obfuscated"));
		
		if (tag.hasKey("color"))
			try {
				subText.setColor(ChatColor.of(tag.getString("color").toUpperCase()));				
			}catch(Exception e) {
			}
		
		return subText;
	}
}
