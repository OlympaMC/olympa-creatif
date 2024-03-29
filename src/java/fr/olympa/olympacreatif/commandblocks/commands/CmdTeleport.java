package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdTeleport extends CbCommand {

	private Location tpLoc = null;
	//private Supplier<List<Entity>> getEntitiesSupplier = null;
	
	public CmdTeleport(Entity sender, Location loc, OlympaCreatifMain plugin, Plot plot, final String[] args) {
		super(CommandType.teleport, sender, loc, plugin, plot, args);
	}
	
	private Location getLocFromSelector(String selector) {
		List<Entity> list = parseSelector(selector, false);
		return list.size() > 0 ? list.get(0).getLocation() : null;
	}
	
	@Override
	public int execute() {
		targetEntities = new ArrayList<Entity>();
		
		if (args.length == 1) {
			if (sender instanceof Entity) {
				targetEntities.add((Entity) sender);
				tpLoc = getLocFromSelector(args[0]);	
			}
		}
		
		else if (args.length == 2) {
			targetEntities = parseSelector(args[0], false);
			tpLoc = getLocFromSelector(args[1]);
			
			//System.out.println("Command selector parse : " + args[0] + "(" + false + ") : " +targetEntities);
		} 
		
		else if (args.length == 3) {
			if (sender instanceof Entity) {
				targetEntities.add((Entity) sender);
				tpLoc = parseLocation(args[0], args[1], args[2]);	
			}
		}
		
		else if (args.length == 4) {
			targetEntities = parseSelector(args[0], false);
			tpLoc = parseLocation(args[1], args[2], args[3]);
			
			//System.out.println("Command selector parse : " + args[0] + "(" + false + ") : " +targetEntities);
		}
		
		else if (args.length == 5) {
			if (sender instanceof Entity) {
				targetEntities.add((Entity) sender);
				tpLoc = parseLocation(args[0], args[1], args[2], args[3], args[4]);	
			}
		}
		
		else if (args.length == 6) {
			targetEntities = parseSelector(args[0], false);
			tpLoc = parseLocation(args[1], args[2], args[3], args[4], args[5]);
			
			//System.out.println("Command selector parse : " + args[0] + "(" + false + ") : " +targetEntities);
		}

		//System.out.println("TP loc : " + tpLoc + ", targets : " + targetEntities);

		if (tpLoc == null)
			return 0;

		targetEntities.forEach(ent -> ent.teleportAsync(tpLoc));
		
		return targetEntities.size();
	}
}
