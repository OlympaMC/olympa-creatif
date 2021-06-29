package fr.olympa.olympacreatif.commandblocks.commands;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.utils.JSONtextUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class CmdTellraw extends CbCommand {
	
	public CmdTellraw(Entity sender, Location loc, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(CommandType.tellraw, sender, loc, plugin, plot, args);
	}
	
	@Override
	public int execute() {	
		
		if (args.length < 2)
			return 0;	
		
		TextComponent text = new TextComponent();
		
		targetEntities = parseSelector(args[0], true);
		
		if (targetEntities.size() == 0)
			return 0;
		
		TextComponent mark = new TextComponent();
		mark.setColor(ChatColor.GRAY);
		mark.addExtra("[CB] ");
		text.addExtra(mark);
		
		if (args[1].contains("{")) 
			text.addExtra(JSONtextUtil.getJsonText(this, args[1].replace('&', '§')));
		else {
			String str = "";
			for (int i = 1 ; i < args.length ; i++)
				str += args[i] + " ";
			
			text.addExtra(org.bukkit.ChatColor.translateAlternateColorCodes('&', str));
		}
			
		
		//Bukkit.broadcastMessage("listAsString : " + listAsString);
		for (Entity e : targetEntities)
			if (e.getType() == EntityType.PLAYER)
				e.spigot().sendMessage(text);
		
		return targetEntities.size();
	}
}









