package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.CbObjective;
import fr.olympa.olympacreatif.commandblocks.CbTeam;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand.CommandType;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdTeam extends CbCommand {

	private String[] args;
	
	public CmdTeam(CommandType type, CommandSender sender, Location loc, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(type, sender, loc, plugin, plot, args);
	
		this.args = args;
	}
	
	@Override 
	public int execute() {
		switch (args[0]) {
		case "list":
			sender.sendMessage("§6  >>>  Equipes du plot " + plot.getId().getAsString() + " <<<");
			for (CbTeam t : plugin.getCommandBlocksManager().getTeams(plot))
				sender.sendMessage("   §e> " + t.getId() + " (" + t.getName() + "§r§e) : " + t.getMembers().size() + " membre(s)");
			return 1;
			
		case "empty":
			if (args.length >= 2) {
				CbTeam t = plugin.getCommandBlocksManager().getTeam(plot, args[1]);
				if (t != null) {
					t.removeTeamNameForAll();
					t.getMembers().clear();
					return 1;
				}
			}
			break;
			
		case "join":
			if (args.length >= 2) {
				CbTeam t = plugin.getCommandBlocksManager().getTeam(plot, args[1]);
				
				if (t == null)
					break;
				
				if (args.length == 3) {
					List<Entity> list = parseSelector(args[2], false);

					for (Entity e : list)
						t.addMember(e);
				
					return list.size();
				}
				else if(sender instanceof Player) {
					t.addMember(((Player)sender).getDisplayName());
					return 1;
				}
			}
			break;
			
		case "leave":
			if (args.length >= 2) {
				CbTeam t = plugin.getCommandBlocksManager().getTeam(plot, args[1]);
				
				if (t == null)
					break;
				
				if (args.length == 3) {
					List<Entity> list = parseSelector(args[2], false);

					for (Entity e : list)
						t.removeMember(e);
				
					return list.size();
				}
				else if(sender instanceof Player) {
					t.removeMember(((Player)sender).getDisplayName());
					return 1;
				}
			}
			break;
			
		case "add":
			if (args.length >= 2) {
				String display = "";
				if (args.length == 3)
					display = ChatColor.translateAlternateColorCodes('&', args[2]);
				
				CbTeam t = new CbTeam(plugin, plot, args[1], display);
				
				if (plugin.getCommandBlocksManager().registerTeam(plot, t))
					return 1;
			}
			break;
			
		case "remove":
			if (args.length >= 2) {
				for (CbTeam t : new ArrayList<CbTeam>(plugin.getCommandBlocksManager().getTeams(plot)))
					if (t.getId().equals(args[1])) {
						t.removeTeamNameForAll();
						plugin.getCommandBlocksManager().getTeams(plot).remove(t);
						return 1;
					}
			}
			break;
			
		case "modify"://ne modifie que le nom (prefix) de l'équipe, aucune autre personnalisation du menu "modify" n'est prise en compte.
			if (args.length >= 3 && args[2].equals("prefix")) {
				CbTeam t = plugin.getCommandBlocksManager().getTeam(plot, args[1]);
				
				if (t == null)
					break;
				
				if (args.length == 4)
					t.setName(args[3]);
				else
					t.setName("");
			}
			break;
		}
		
		return 0;
	}

}
