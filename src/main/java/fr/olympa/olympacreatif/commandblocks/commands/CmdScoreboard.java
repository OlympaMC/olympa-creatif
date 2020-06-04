package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.command.OcCommand;
import fr.olympa.olympacreatif.commandblocks.CbObjective;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand.CommandType;
import fr.olympa.olympacreatif.perks.NbtParserUtil;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdScoreboard extends CbCommand {

	ScbCmdType scbType;
	ObjectivesType objType;
	PlayersType playerType;
	
	public CmdScoreboard(CommandType type, CommandSender sender, Location loc, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(type, sender, loc, plugin, plot, args);
	}
	
	@Override
	public int execute() {
		if (args.length >= 2)
			scbType = ScbCmdType.get(args[0]);
		
		//instructions sous commande objective
		if (scbType == ScbCmdType.objectives) {
			objType = ObjectivesType.get(args[1]);
			
			switch (objType) {
			case add:
				if (args.length >= 4) {
					CbObjective obj = null;
					if (args.length >= 5)
						obj = new CbObjective(plugin, plot, args[3], args[2], NbtParserUtil.parseJsonFromCompound(NbtParserUtil.getTagFromStrings(args)));
					else
						obj = new CbObjective(plugin, plot, args[3], args[2], args[2]);
					
					if (obj.getType() == null)
						return 0;

					if (plugin.getCommandBlocksManager().registerObjective(plot, obj))
						return 1;
				}
				break;
				
			case list:
				sender.sendMessage("§6  >>>  Objectifs du plot " + plot.getId().getAsString() + " <<<");
				for (CbObjective o : plugin.getCommandBlocksManager().getObjectives(plot)) {
					String paramType = "";
					if (o.getParamType() != null)
						paramType = " - " + o.getParamType().toString();
					
					sender.sendMessage("   §e> " + o.getId() + "(§r" + o.getName() + "§r§e) : " + o.getType().toString() + paramType);	
				}
				return plugin.getCommandBlocksManager().getObjectives(plot).size();
				
			case remove:
				if (args.length >= 3) {
					for (CbObjective o : new ArrayList<CbObjective>(plugin.getCommandBlocksManager().getObjectives(plot))){
						if (o.getId().equals(args[2])) {
							plugin.getCommandBlocksManager().clearScoreboardSlot(plot, o.getDisplaySlot());
							plugin.getCommandBlocksManager().getObjectives(plot).remove(o);
							return 1;
						}
					}
				}
				break;
					
			case setdisplay:
				if (args.length >= 4) {
					for (CbObjective o : plugin.getCommandBlocksManager().getObjectives(plot)){
						if (o.getId().equals(args[3])) {
							if (args[2].equals("belowName")) {
								o.setDisplay(DisplaySlot.BELOW_NAME);
								return 1;
							}
							if (args[2].equals("sidebar")) {
								o.setDisplay(DisplaySlot.SIDEBAR);
								return 1;
							}
						}
					}
				}
				break;
				
			case modify:
				if (args.length >= 5 && args[3].equals("displayname")) {
					CbObjective obj = plugin.getCommandBlocksManager().getObjective(plot, args[2]);
					
					if (obj == null)
						return 0;
					
					obj.setName(NbtParserUtil.parseJsonFromCompound(NbtParserUtil.getTagFromStrings(args)));
					return 1;
				}
				break;
				
			default:
				return 0;
			}
		}
		
		if (scbType == ScbCmdType.players) {
			playerType = PlayersType.get(args[1]);
			
			switch (playerType) {
			case ADD:
				if (args.length >= 5) {
					CbObjective obj = plugin.getCommandBlocksManager().getObjective(plot, args[3]);
					
					int value = 0;
					if (StringUtils.isNumeric(args[4]))
						value = (int)(double)Double.valueOf(args[4]);
					
					if (obj != null) {
						
						//ajout du score aux entités sélectionnées si un sélecteur a bien été utilisé
						if (args[2].startsWith("@")) {
							List<Entity> list = parseSelector(args[2], false);
							
							for (Entity e : list) {
								obj.add(e, value);
							}	
							return list.size();	
						}else {
							obj.add(args[2], value);
							return 1;
						}
					}
				}
				break;
			case ENABLE:
				
				//TODO
				break;
			case GET:
				if (args.length >= 4) {
					CbObjective obj = plugin.getCommandBlocksManager().getObjective(plot, args[3]);

					if (obj != null) 
						return obj.get(args[2]);
				}
				break;
			case LIST:
				if (args.length >= 3) {
					sender.sendMessage("§6  >>>  Objectifs pour" + args[2] + " <<<");
					for (CbObjective o : plugin.getCommandBlocksManager().getObjectives(plot))
						sender.sendMessage("   §e> " + o.getId() + " : " + o.get(args[2]));
					return 1;
				}
				break;
				
			case OPERATION:
				if (args.length >= 7) {
					CbObjective obj1 = plugin.getCommandBlocksManager().getObjective(plot, args[3]);
					CbObjective obj2 = plugin.getCommandBlocksManager().getObjective(plot, args[6]);
					
					if (obj1 != null && obj2 != null) {
						String e1 = null;
						
						if (args[2].startsWith("@")) {
							List<Entity> list = parseSelector(args[2], false);
							if (list.size() != 1)
								return 0;
							
							if (list.get(0) instanceof Player)
								e1 = ((Player)list.get(0)).getDisplayName();
							else
								e1 = list.get(0).getCustomName();
						}
						
						List<String> e2 = new ArrayList<String>();
						
						if (args[5].startsWith("@")){
							List<Entity> list = parseSelector(args[5], false);
							for (Entity e : list)
								e2.add(e.getCustomName());
						}else {
							e2.add(args[5]);
						}
						
						if (e1 != null && e2.size() > 0)
							return evaluateOperation(obj1, obj2, args[4], e1, e2);
						else
							return 0;
					}
				}
				break;
				
			case REMOVE:
				if (args.length >= 5) {
					CbObjective obj = plugin.getCommandBlocksManager().getObjective(plot, args[3]);
					
					int value = 0;
					if (StringUtils.isNumeric(args[4]))
						value = -(int)(double)Double.valueOf(args[4]);
					
					if (obj != null) {
						
						//ajout du score aux entités sélectionnées si un sélecteur a bien été utilisé
						if (args[2].startsWith("@")) {
							List<Entity> list = parseSelector(args[2], false);
							
							for (Entity e : list) {
								obj.add(e, value);
							}	
							return list.size();	
						}else {
							obj.add(args[2], value);
							return 1;
						}
					}
				}
				break;
				
			case RESET:
				if (args.length >= 3) {
					
					if (args[2].startsWith("@")) {
						List<Entity> list = parseSelector(args[2], false);
						
						for (CbObjective o : plugin.getCommandBlocksManager().getObjectives(plot))
							for (Entity e : list)
								o.reset(e);
					}else {
						for (CbObjective o : plugin.getCommandBlocksManager().getObjectives(plot))
							o.reset(args[2]);
					}
					return 1;
				}
				break;
				
			case SET:
				if (args.length >= 5) {
					CbObjective obj = plugin.getCommandBlocksManager().getObjective(plot, args[3]);
					
					int value = 0;
					if (StringUtils.isNumeric(args[4]))
						value = (int)(double)Double.valueOf(args[4]);
					
					if (obj != null) {
						
						//ajout du score aux entités sélectionnées si un sélecteur a bien été utilisé
						if (args[2].startsWith("@")) {
							List<Entity> list = parseSelector(args[2], false);
							
							for (Entity e : list) {
								obj.set(e, value);
							}	
							return list.size();	
						}else {
							obj.set(args[2], value);
							return 1;
						}
					}
				}
				break;
			default:
				return 0;
			}
			
		}
		
		return 0;
	}
	
	private int evaluateOperation(CbObjective obj1, CbObjective obj2, String operat, String e1, List<String> e2) {
		int val = 0;
		
		switch(operat) {
		case "%=":
			if (e2.size() != 1)
				return 0;
			obj1.set(e1, obj1.get(e1) % obj2.get(e2.get(0)));
			return 1;
			
		case "*=":
			if (e2.size() != 1)
				return 0;
			obj1.set(e1, obj1.get(e1) * obj2.get(e2.get(0)));
			return 1;
			
		case "/=":
			if (e2.size() != 1)
				return 0;
			obj1.set(e1, obj1.get(e1) / obj2.get(e2.get(0)));
			return 1;
			
		case "+=":
			if (e2.size() != 1)
				return 0;
			obj1.set(e1, obj1.get(e1) + obj2.get(e2.get(0)));
			return 1;
			
		case "-=":
			if (e2.size() != 1)
				return 0;
			obj1.set(e1, obj1.get(e1) - obj2.get(e2.get(0)));
			return 1;
			
		case "<":
			if (e2.size() == 0)
				return 0;
			
			val = obj2.get(e2.get(0));
			for (String s : e2)
				if (obj2.get(s) < val)
					val = obj2.get(s);
			
			obj1.set(e1, val);
			return 1;
			
		case ">":
			if (e2.size() == 0)
				return 0;
			
			val = obj2.get(e2.get(0));
			for (String s : e2)
				if (obj2.get(s) > val)
					val = obj2.get(s);
			
			obj1.set(e1, val);
			return 1;
			
		case "=":
			if (e2.size() != 1)
				return 0;
			obj1.set(e1, obj2.get(e2.get(0)));
			return 1;
			
		default:
			return 0;
		}
	}
	

	private enum ScbCmdType{
		players,
		objectives;
		
		public static ScbCmdType get(String s) {
			for (ScbCmdType t : ScbCmdType.values())
				if (t.toString().equals(s))
					return t;
			return null;
		}
	}
	
	private enum ObjectivesType{
		add,
		list,
		remove,
		setdisplay, 
		modify;
		
		public static ObjectivesType get(String s) {
			for (ObjectivesType t : ObjectivesType.values())
				if (t.toString().equals(s))
					return t;
			return null;
		}
	}
	
	private enum PlayersType{
		ADD,
		ENABLE,
		GET,
		LIST,
		OPERATION,
		REMOVE,
		RESET,
		SET;
		
		public static PlayersType get(String s) {
			for (PlayersType t : PlayersType.values())
				if (t.toString().equalsIgnoreCase(s))
					return t;
			return null;
		}
	}
}
