package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.CbObjective;
import fr.olympa.olympacreatif.commandblocks.CbTeam;
import fr.olympa.olympacreatif.commandblocks.CbTeam.ColorType;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand.CommandType;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.utils.NbtParserUtil;

public class CmdTeam extends CbCommand {
	
	public CmdTeam(CommandType type, CommandSender sender, Location loc, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(type, sender, loc, plugin, plot, args);
	}
	
	@Override 
	public int execute() {
		switch (args[0]) {
		case "list":
			sender.sendMessage("§6  >>>  Equipes du plot " + plot.getPlotId() + " <<<");
			for (CbTeam t : plotCbData.getTeams()) {
				
				ColorType color = t.getColor();
				
				String colorParam = "";
				
				if (color != null)
					colorParam = " de couleur " + t.getColor().getColorCode() + t.getColor() + "§r";
					
				sender.sendMessage("   §e> " + t.getId() + " (§r" + t.getName() + "§r§e)" + colorParam +
						" §e: " + t.getMembers().size() + " membre(s)");
				
			}
			
			return 1;
			
		case "empty":
			if (args.length >= 2) {
				CbTeam t = plotCbData.getTeamById(args[1]);
				if (t != null) {
					for (Entity e : new ArrayList<Entity>(t.getMembers()))
						t.removeMember(e);
					return 1;
				}
			}
			break;
			
		case "join":
			if (args.length >= 2) {
				CbTeam t = plotCbData.getTeamById(args[1]);
				
				if (t == null)
					break;
				
				
				if (args.length == 3) {
					List<Entity> list = parseSelector(args[2], false);
					
					int nbAddedEntities = 0;
					
					for (Entity e : list) 
						if (t.addMember(e))
							nbAddedEntities++;
				
					return nbAddedEntities;
				}
				else if(sender instanceof Player) {
					if (t.addMember((Entity) sender))
						return 1;
					else 
						return 0;
				}
			}
			break;
			
		case "leave":
			if (args.length == 2) { //supression de l'équipe pour les entités du sélecteur
				List<Entity> list = parseSelector(args[1], false);

				int removedPlayers = 0;
				
				for (Entity e : list) {
					CbTeam t = plotCbData.getTeamOf(e);
					if (t != null) {
						t.removeMember(e);
						removedPlayers++;
					}	
				}
				
				return removedPlayers;
				
			}else if(sender instanceof Player) { //supression de l'équipe de l'exécutant de la commande
				CbTeam t = plotCbData.getTeamOf((Entity) sender);
				if (t != null) {
					t.removeMember((Player) sender);
					return 1;	
				}
			}
			break;
			
		case "add":
			if (args.length >= 2) {
				CbTeam t;
				
				if (args.length >= 3)
					t = new CbTeam(plugin, plot, args[1], args[2]);
				else
					t = new CbTeam(plugin, plot, args[1], null);
				
				if (plotCbData.registerTeam(t))
					return 1;
			}
			break;
			
		case "remove":
			if (args.length >= 2) {
				for (CbTeam t : new ArrayList<CbTeam>(plotCbData.getTeams()))
					
					if (t.getId().equals(args[1])) {
						
						t.executeDeletionActions();
						plotCbData.getTeams().remove(t);
						return 1;
					}
			}
			break;
			
		case "modify"://ne modifie que le nom (prefix) de l'équipe, aucune autre personnalisation du menu "modify" n'est prise en compte.
			if (args.length >= 3) {
				CbTeam t = plotCbData.getTeamById(args[1]);
				
				if (t == null)
					return 0;	
				
				switch (args[2]) {
				case "suffix":					
					if (args.length >= 4) 
						t.setName(NbtParserUtil.parseJsonFromCompound(NbtParserUtil.getTagFromStrings(args)));
					
					return 1;
					
				case "friendlyFire":
					if (args.length == 4) {
						if (args[3].equals("true"))
							t.setFriendlyFire(true);
						else
							t.setFriendlyFire(false);
						
						return 1;
					}
					break;
					
				case "color":
					if (args.length < 4) {
						t.setColor(null);
						return 1;
					}else if (t.setColor(args[3]))
							return 1; 
						
				}
			}
			break;
		}
		
		return 0;
	}

}
