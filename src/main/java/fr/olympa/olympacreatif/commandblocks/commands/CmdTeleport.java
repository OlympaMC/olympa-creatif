package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand.CommandType;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdTeleport extends CbCommand {

	private Location tpLoc = null;
	private Supplier<List<Entity>> getEntities = null;
	
	public CmdTeleport(CommandSender sender, Location loc, OlympaCreatifMain plugin, Plot plot, String[] args) {
		super(CommandType.teleport, sender, loc, plugin, plot, args);
		
		if (args.length == 1) {
			if (sender instanceof Entity) {
				targetEntities.add((Entity) sender);
				tpLoc = getLocFromSelector(args[0]);	
			}
		}
		
		else if (args.length == 2) {
			getEntities = () -> parseSelector(args[0], false);
			tpLoc = getLocFromSelector(args[1]);
		} 
		
		else if (args.length == 3) {
			if (sender instanceof Entity) {
				targetEntities.add((Entity) sender);
				tpLoc = parseLocation(args[0], args[1], args[2]);	
			}
		}
		
		else if (args.length == 4) {
			getEntities = () -> parseSelector(args[0], false);
			tpLoc = parseLocation(args[1], args[2], args[3]);
		}
		
		else if (args.length == 5) {
			if (sender instanceof Entity) {
				targetEntities.add((Entity) sender);
				tpLoc = parseLocation(args[0], args[1], args[2], args[3], args[4]);	
			}
		}
		
		else if (args.length == 6) {
			getEntities = () -> parseSelector(args[0], false);
			tpLoc = parseLocation(args[1], args[2], args[3], args[4], args[5]);
		}
	}
	
	private Location getLocFromSelector(String selector) {
		List<Entity> list = parseSelector(selector, false);
		return list.size() > 0 ? list.get(0).getLocation() : null;
	}
	
	@Override
	public int execute() {
		if (tpLoc == null)
			return 0;
		
		if (getEntities == null)
			targetEntities.forEach(ent -> ent.teleport(tpLoc));
		else
			(targetEntities = getEntities.get()).forEach(ent -> ent.teleport(tpLoc));
		
		return targetEntities.size();
	}
}
