package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.CbObjective;
import fr.olympa.olympacreatif.commandblocks.CbTeam;
import fr.olympa.olympacreatif.plot.Plot;
import net.minecraft.server.v1_15_R1.MojangsonParser;
import net.minecraft.server.v1_15_R1.NBTTagCompound;

public class CmdTellraw extends CbCommand {
	
	private String text = "";
	
	public CmdTellraw(CommandSender sender, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(sender, plugin, plot, args);
		targetEntities = parseSelector(plot, args[0], true);
		
		//JSONObject json(JSONObject) new JSONParser().parse(args[1]);
		NBTTagCompound tag;

		String[] parts = args[1].replace("[", "").replace("]", "").split(",");
		
		try {
			for (String s : parts) {
				tag = MojangsonParser.parse(s);

				for (String key : tag.getKeys()) {
					switch(key) {
					case "text":
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
							text += CbTeam.ColorType.getColor(tag.getString(key));
						
						text += ChatColor.translateAlternateColorCodes('&', tag.getString(key));
						break;
						
					case "selector":
						
						List<Entity> list = parseSelector(plot, tag.getString(key), false);

						int i = 0;
						
						for (Entity e : parseSelector(plot, tag.getString(key), false)) {
							text += ChatColor.translateAlternateColorCodes('&', e.getCustomName());
							
							i++;
							if (i != list.size())
								text += "§r, ";
						}
						break;
						
					case "score":
						NBTTagCompound score = tag.getCompound(key);
						
						String objOwner = score.getString("name");
						if (objOwner.startsWith("@")) {
							List<Entity> l = parseSelector(plot, objOwner, false);
							
							if (l.size() != 1)
								break;
							
							if (l.get(0) instanceof Player) 
								objOwner = ((Player) l.get(0)).getDisplayName();	
							else
								objOwner = l.get(0).getCustomName();	
							
						}
						
						CbObjective obj = plugin.getCommandBlocksManager().getObjective(plot, score.getString("objective"));
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
		for (Entity e : targetEntities)
			e.sendMessage(text);
		return targetEntities.size();
	}
}
