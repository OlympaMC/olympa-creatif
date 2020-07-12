package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.CbObjective;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.utils.NbtParserUtil;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.NBTTagList;

public class CmdTellraw extends CbCommand {
	
	private String text = "";
	
	public CmdTellraw(CommandType type, CommandSender sender, Location loc, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(type, sender, loc, plugin, plot, args);
		
		targetEntities = parseSelector(args[0], true);
		
		if (args.length < 2)
			return;
		
		NBTTagList mainTag = NbtParserUtil.getListCompoundFromString(args);
		
		try {
			for (int i = 0 ; i < mainTag.size() ; i++) {
				
				NBTTagCompound tag = mainTag.getCompound(i);
				
				for (String key : tag.getKeys()) {
					switch(key) {
					case "text":
						text += NbtParserUtil.parseJsonFromCompound(tag);
						break;
						
					case "selector":
						
						List<Entity> list = parseSelector(tag.getString(key), false);

						int j = 0;
						
						for (Entity e : parseSelector(tag.getString(key), false)) {
							text += ChatColor.translateAlternateColorCodes('&', e.getCustomName());
							
							j++;
							if (j != list.size())
								text += "§r, ";
						}
						break;
						
					case "score":
						NBTTagCompound score = tag.getCompound(key);
						
						String objOwner = score.getString("name");
						if (objOwner.startsWith("@")) {
							List<Entity> l = parseSelector(objOwner, false);
							
							if (l.size() != 1)
								break;
							
							if (l.get(0) instanceof Player) 
								objOwner = ((Player) l.get(0)).getDisplayName();	
							else
								objOwner = l.get(0).getCustomName();	
							
						}
						
						CbObjective obj = plotCbData.getObjective(score.getString("objective"));
						if (obj == null) 
							break;
						
						text += obj.get(objOwner);
						break;
					}
				}	
			}
			
		} catch (Exception e) {
			text = "";
			return;
		}
	}

	
	@Override
	public int execute() {
		if (text.length() == 0)
			return 0;
		
		for (Entity e : targetEntities)
			e.sendMessage("§7[CB]§r " + text);
		
		return targetEntities.size();
	}
}
