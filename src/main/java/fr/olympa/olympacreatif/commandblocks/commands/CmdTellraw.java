package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.EnumUtils;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftMagicNumbers.NBT;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.CbObjective;
import fr.olympa.olympacreatif.plot.Plot;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_15_R1.MojangsonParser;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.NBTTagList;

public class CmdTellraw extends CbCommand {
	
	private TextComponent text = new TextComponent();
	
	public CmdTellraw(CommandType type, CommandSender sender, Location loc, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(type, sender, loc, plugin, plot, args);
		
		targetEntities = parseSelector(args[0], true);
		
		if (args.length < 2)
			return;
		
		TextComponent mark = new TextComponent();
		mark.setColor(ChatColor.GRAY);
		mark.addExtra("[CB] ");
		text.addExtra(mark);
		
		String listAsString = StringEscapeUtils.unescapeJava(args[1].replace("\"\",", "").replace(",\"\"", ""));//.replace("\\", "\\\\");
		
		Bukkit.broadcastMessage("listAsString : " + listAsString);
		
		if (!listAsString.startsWith("["))
			listAsString = "{rawText:[" + listAsString + "]}";
		else
			listAsString = "{rawText:" + listAsString + "}";

		
		//Bukkit.broadcastMessage("basic string tag : " + listAsString);
		
		try {
			
			NBTTagList mainTag = MojangsonParser.parse(listAsString).getList("rawText", NBT.TAG_COMPOUND);
			
			//Bukkit.broadcastMessage("tags : " + mainTag.asString());
			
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
						
						textPart.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', subString.replace("{", ""))).create()));
					}
					
					//ajout ClickEvent
					if (tag.hasKey("clickEvent")) {
						NBTTagCompound subTag = tag.getCompound("clickEvent");
						
						if (subTag == null)
							continue;
						
						String value = subTag.getString("value");
						
						if (value == null || !value.startsWith("/trigger "))
							continue;
						
						textPart.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, value));
					}
					
				}else if (tag.hasKey("selector")) {
					List<Entity> list = parseSelector(tag.getString("selector"), false);
					String concat = "";
					for (int j = 0 ; j < list.size() - 1 ; j++)
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
								concat += list.get(list.size() - 1).getName() + ", ";
					
					//Bukkit.broadcastMessage("selector : '" + concat + "'");
					textPart.addExtra(concat);
					
				}else if (tag.hasKey("score")) {
					NBTTagCompound subTag = tag.getCompound("score");
					
					if (subTag == null)
						continue;
					
					String name = subTag.getString("name");
					String obj = subTag.getString("objective");
					
					if (name == null || obj == null)
						continue;

					List<Entity> list = parseSelector(name, false);
					CbObjective cbObj = plot.getCbData().getObjective(obj);
					
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
	}

	private TextComponent applyUniversalTags(NBTTagCompound tag) {
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
			if (EnumUtils.isValidEnum(ChatColor.class, tag.getString("color").toUpperCase()))
				subText.setColor(ChatColor.valueOf(tag.getString("color").toUpperCase()));
		
		return subText;
	}
	
	@Override
	public int execute() {		
		for (Entity e : targetEntities)
			if (e.getType() == EntityType.PLAYER)
				e.spigot().sendMessage(text);
		
		return targetEntities.size();
	}
}
