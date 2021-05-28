package fr.olympa.olympacreatif.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringEscapeUtils;
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
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagList;

public abstract class JSONtextUtil {
	
	@Deprecated
	public static TextComponent getJsonText(String component) {
		return getJsonText(null, component);
	}
	
	public static TextComponent getJsonText(CbCommand cmd, String component) {
		component = StringEscapeUtils.unescapeJava(component.replace("\"\",", "").replace(",\"\"", ""));
		
		TextComponent text = new TextComponent();
		
		if (!component.startsWith("["))
			component = "{rawText:[" + component + "]}";
		else
			component = "{rawText:" + component + "}";
		
		try {
			
			//Bukkit.broadcastMessage("try to parse : " + component);
			 
			NBTTagList mainTag = new OcMojangsonParser(component.replace("=", ":"))
					.parse(cmd != null ? cmd.getSender() instanceof Player ? (Player) cmd.getSender() : null : null)
					.getList("rawText", NBT.TAG_COMPOUND);
			
			//System.out.println("PARSED : " + mainTag.asString());
			
			for (int i = 0 ; i < mainTag.size() ; i++) {
				
				NBTTagCompound tag = mainTag.getCompound(i);
				TextComponent textPart = applyUniversalTags(tag);
				
				
				if (tag.hasKey("text")) {
					textPart.addExtra(tag.getString("text"));

					//ajout HoverEvent
					if (tag.hasKey("hoverEvent")) {
						NBTTagCompound subTag = tag.getCompound("hoverEvent");
						
						if (subTag == null)
							continue;
						
						String subString = subTag.getString("contents");
						
						if (subString == null)
							continue;
						
						//System.out.println("SUB STRING : " + subString);
						
						textPart.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', subString.replace("{", ""))).create()));
					}
					
					//ajout ClickEvent
					if (tag.hasKey("clickEvent")) {
						NBTTagCompound subTag = tag.getCompound("clickEvent");
						
						if (subTag == null)
							continue;

						String value = subTag.getString("value");
						String clickType = subTag.getString("action");
						
						if (value == null || clickType == null)
							continue;
						
						if (clickType.equals("run_command") && (value.startsWith("/trigger ") || value.startsWith("/oc visit")))
							textPart.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, value));
						else
							textPart.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "Action non autorisée. Seules les commandes /trigger et /oc visit sont permises dans les messages Json."));
					}
					
				}else if (tag.hasKey("selector") && cmd != null) {
					List<Entity> list = cmd.parseSelector(tag.getString("selector").replace(":", "="), false);
					
					String concat = list.stream().map(e -> {
						if (e.getType() != EntityType.PLAYER) {
							return e.getCustomName() == null ? e.getName() : e.getCustomName();
						} else
							return ((Player)e).getName();
					}).collect(Collectors.joining(", "));
					
					//Bukkit.broadcastMessage("selector : '" + concat + "'");
					textPart.addExtra(concat);
					
				}else if (tag.hasKey("score") && cmd != null) {
					NBTTagCompound subTag = tag.getCompound("score");
					
					if (subTag == null)
						continue;
					
					String name = subTag.getString("name");
					String obj = subTag.getString("objective");
					
					/*System.out.println("SCORE TELLAW objective : " + obj + ", entity name : " + name 
							+ ", entity parsed : " + cmd.parseSelector(name.replace(":", "="), false));*/
					
					if (name == null || obj == null)
						continue;

					List<Entity> list = cmd.parseSelector(name.replace(":", "="), false);

					CbObjective cbObj = cmd.getPlot().getCbData().getObjective(obj);
					
					if (cbObj == null)
						continue;
					
					if (list.size() > 0)
						textPart.addExtra(cbObj.get(list.get(0)) + "");
					else if (!name.startsWith("@"))
						textPart.addExtra(cbObj.get(name) + "");
				}
				
				text.addExtra(textPart);
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
