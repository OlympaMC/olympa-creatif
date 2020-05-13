package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.CbObjective;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdExecute extends CbCommand {

	private List<String> subCommands = new ArrayList<String>(Arrays.asList(new String[] {"if", "unless", "at", "as", "facing", "positioned", "store", "run"}));
	
	public CmdExecute(CommandSender sender, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(sender, plugin, plot, args);
	}

	@Override
	public int execute() {
		
		boolean stop = false;
		
		List<CommandSender> sendEntities = new ArrayList<CommandSender>();
		List<Location> sendLocations = new ArrayList<Location>();
		
		List<String> args = new ArrayList<String>(Arrays.asList(this.args));
		
		List<Integer> lastResults = new ArrayList<Integer>();
		
		if (sender instanceof CommandBlock)
			sendLocations.add(((CommandBlock) sender).getLocation());
		else if (sender instanceof Player)
			sendLocations.add(((Player) sender).getLocation());
		
		sendEntities.add(sender);
		
		//tant que la commande n'est pas terminée, évaluation des sous commandes une par une
		while (args.size() > 0) {
			
			List<String> subArgs = new ArrayList<String>();
			subArgs.add(args.get(0));
			args.remove(0);
			
			//extraction des paramètres de la commande
			for (String s : new ArrayList<String>(args))
				if (!subCommands.contains(s)) {
					subArgs.add(s);
					args.remove(0);
				}else
					break;
					
			//traitement de la sous commande
			switch (subArgs.get(0)) {
			case "if":
				if (subArgs.size() < 2)
					return 0;
				
				lastResults.clear();
				
				for (CommandSender s : sendEntities)
					for (Location loc : new ArrayList<Location>(sendLocations)) 
						if (isTestValid(subArgs, loc))
							lastResults.add(1);
						else {
							lastResults.add(0);
							sendLocations.remove(loc);
						}
				
				break;
				
			case "unless":
				if (subArgs.size() < 2)
					return 0;
				
				lastResults.clear();
				
				for (CommandSender s : sendEntities)
					for (Location loc : new ArrayList<Location>(sendLocations)) 
						if (!isTestValid(subArgs, loc))
							lastResults.add(1);
						else {
							lastResults.add(0);
							sendLocations.remove(loc);
						}
				
				break;
				
			case "at":
				if (subArgs.size() != 2)
					return 0;
				
				sendLocations.clear();
				lastResults.clear();
				
				for (CommandSender s : sendEntities){
					sendLocations.addAll(getExecuteLocations(subArgs));

					lastResults.add(sendLocations.size());
				}
				break;
				
			case "as":
				if (subArgs.size() != 2)
					return 0;
				
				sendEntities.clear();
				lastResults.clear();
				
				for (Location loc : sendLocations){
					sendEntities.addAll(parseSelector(plot, subArgs.get(1), false));

					lastResults.add(sendEntities.size());
				}
				break;
				
			case "facing":
				break;
				
			case "positioned":
				break;
				
			case "store":
				if (subArgs.size() == 5 && (subArgs.get(1).equals("result") || subArgs.get(1).equals("success"))) {
					
					if (subArgs.get(2).equals("bossbar") && subArgs.get(4).equals("value")) {//stockage du résultat de la commande suivante dans une bossbar
						BossBar bar = plot.getBossBar(subArgs.get(3));
						
						if (bar != null)
							for (Location loc : sendLocations)
								for (CommandSender s : sendEntities) {
									
									//récupération de la commande suivante
									List<String >subArgsBis = new ArrayList<String>();
									
									for (String str : new ArrayList<String>(subArgs))
										if (!subCommands.contains(s)) {
											subArgsBis.add(str);
											subArgs.remove(0);
										}else //exécution de la commande suivante et stockage du résultat dans la bossbar
											bar.setProgress(new CmdExecute(s, plugin, plot, (String[]) subArgsBis.toArray()).execute());
								}
						else
							return 0;
									
					}else if (subArgs.get(2).equals("score")) { //stockage du résultat de la commande suivante dans un score
						CbObjective obj = plugin.getCommandBlocksManager().getObjective(plot, subArgs.get(5));
						
						if (obj != null)
							for (Location loc : sendLocations)
								for (CommandSender s : sendEntities) {
									
									//récupération de la commande suivante
									List<String >subArgsBis = new ArrayList<String>();
									
									for (String str : new ArrayList<String>(subArgs))
										if (!subCommands.contains(s)) {
											subArgsBis.add(str);
											subArgs.remove(0);
										}else //exécution de la commande suivante et stockage du résultat dans le score de l'entité
											if (s instanceof Entity)
												obj.set((Entity) s, new CmdExecute(s, plugin, plot, (String[]) subArgsBis.toArray()).execute());
								}
						else
							return 0;
					}else
						return 0;
				}
				break;
				
			case "run":
				String cmdStr = "";
				for (String s : subArgs)
					cmdStr += s + " ";
				
				lastResults.clear();
				
				for (Location loc : sendLocations)
					for (CommandSender s : sendEntities) {
						CbCommand cmd = CbCommand.getCommand(plugin, s, cmdStr);
						
						if (cmd != null && !(cmd instanceof CmdExecute))
							lastResults.add(cmd.execute());	
					}
				
				break;
			default:
				return 0;
			}
		}
		return lastResults.get(lastResults.size()-1);
	}
	
	//test effectués pour les commandes if et unless
	//accepte actuellement les sous commandes : entity, block, blocks, score
	private boolean isTestValid(List<String> subArgs, Location sendLoc) {
		//sous commandes de if
		switch (subArgs.get(1)) {
		case "score": //compare des scores
			if (subArgs.size() != 7)
				return false;
			
			CbObjective obj1;
			CbObjective obj2;
			
			//récupération des objectifs renseignés
			obj1 = plugin.getCommandBlocksManager().getObjective(plot, subArgs.get(3));
			obj2 = plugin.getCommandBlocksManager().getObjective(plot, subArgs.get(6));
			
			int score1;
			int score2;
			
			if (obj1 == null || obj2 == null)
				return false;
			
			//récupération des scores à comparer
			if (subArgs.get(2).startsWith("@")) {
				List<Entity> list = parseSelector(plot, subArgs.get(2), false);
				if (list.size() == 1)
					score1 = obj1.get(list.get(0));
				else
					return false;
			}else
				score1 = obj1.get(subArgs.get(2));

			
			if (subArgs.get(5).startsWith("@")) {
				List<Entity> list = parseSelector(plot, subArgs.get(5), false);
				if (list.size() == 1)
					score2 = obj2.get(list.get(0));
				else
					return false;
			}else
				score2 = obj2.get(subArgs.get(5));
				
			//comparaison des scores
			switch (subArgs.get(4)) {
			case "<=":
				if (score1 <= score2)
					return true;
				else
					return false;
			case ">=":
				if (score1 >= score2)
					return true;
				else
					return false;
			case "<":
				if (score1 < score2)
					return true;
				else
					return false;
			case ">":
				if (score1 > score2)
					return true;
				else
					return false;
			case "=":
				if (score1 == score2)
					return true;
				else
					return false;
			}
			
			
		case "entity":
			if (subArgs.size() != 3)
				return false;
			
			if (parseSelector(plot, subArgs.get(2), false).size() > 0)
				return true;
			else
				return false;
			
			
		case "block":
			if (subArgs.size() != 6)
				return false;
			
			Material mat = Material.getMaterial(subArgs.get(5));
			Location loc = getLocation(sendLoc, subArgs.get(2), subArgs.get(3), subArgs.get(4));
			
			if (mat == null || loc == null)
			return false;

			Block b = plugin.getWorldManager().getWorld().getBlockAt(loc);
			
			if (b.getType() == mat)
				return true;
			else
				return false;
			
			
		case "blocks":
			if (subArgs.size() != 12)
				return false;
			
			List<BlockData> blocks = new ArrayList<BlockData>();

			//définition des 3 points servant de référence à la comparaiosn
			Location loc1 = getLocation(sendLoc, subArgs.get(2), subArgs.get(3), subArgs.get(4));
			if (loc1 == null)
				return false;
			Location loc2 = getLocation(sendLoc, subArgs.get(5), subArgs.get(6), subArgs.get(7));
			if (loc2 == null)
				return false;
			Location finalLoc3 = getLocation(sendLoc, subArgs.get(8), subArgs.get(9), subArgs.get(10));
			if (finalLoc3 == null)
				return false;

			Location finalLoc1 = new Location(plugin.getWorldManager().getWorld(), Math.min(loc1.getBlockX(), loc2.getBlockX()), Math.min(loc1.getBlockY(), loc2.getBlockY()), Math.min(loc1.getBlockZ(), loc2.getBlockZ()));
			Location finalLoc2 = new Location(plugin.getWorldManager().getWorld(), Math.max(loc1.getBlockX(), loc2.getBlockX()), Math.max(loc1.getBlockY(), loc2.getBlockY()), Math.max(loc1.getBlockZ(), loc2.getBlockZ()));
			Location finalLoc4 = finalLoc3.clone().add(finalLoc2.getBlockX() - finalLoc1.getBlockX(), finalLoc2.getBlockY() - finalLoc1.getBlockY(), finalLoc2.getBlockZ() - finalLoc1.getBlockZ());

			//enregistre les blocks à comparer dans une liste 
			for (int x = finalLoc1.getBlockX() ; x <= finalLoc2.getBlockX() ; x++)
				for (int y = finalLoc1.getBlockY() ; y <= finalLoc2.getBlockY() ; y++)
					for (int z = finalLoc1.getBlockZ() ; z <= finalLoc2.getBlockZ() ; z++)
						blocks.add(plugin.getWorldManager().getWorld().getBlockAt(x, y, z).getBlockData());
			
			int i = 0;
			
			//su un seul des blocks à tester est différent des ceux enregistrés, return false
			for (int x = finalLoc3.getBlockX() ; x <= finalLoc4.getBlockX() ; x++)
				for (int y = finalLoc3.getBlockY() ; y <= finalLoc4.getBlockY() ; y++)
					for (int z = finalLoc3.getBlockZ() ; z <= finalLoc4.getBlockZ() ; z++) { 
						if (!plugin.getWorldManager().getWorld().getBlockAt(x, y, z).getBlockData().equals(blocks.get(i)))
							return false;
						i++;
					}
			return true;
		}
		return false;
	}
	
	//renvoie la liste des localisation correspondant au sélecteur/entité en paramètre
	public List<Location> getExecuteLocations(List<String> subArgs){
		List<Location> list = new ArrayList<Location>();
		
		if (subArgs.get(1).startsWith("@"))
			for (Entity e : parseSelector(plot, subArgs.get(1), false))
				list.add(e.getLocation());
		else {
			Player p = Bukkit.getPlayer(subArgs.get(1));
			if (p != null)
				list.add(p.getLocation());
		}
		
		return list;
	}
}
