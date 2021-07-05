package fr.olympa.olympacreatif.commandblocks.commands;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commands.StructureCommand;
import fr.olympa.olympacreatif.data.Position;
import fr.olympa.olympacreatif.plot.Plot;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class CmdStructure extends CbCommand {

    public CmdStructure(Entity entity, Location location, OlympaCreatifMain plugin, Plot plot, String[] strings) {
        super(CommandType.structure, entity, location, plugin, plot, strings);
    }

    @Override
    public int execute() {
        if (args.length == 8 && args[0].equals("save")) { // /structure save <name> ~ ~ ~ ~ ~ ~
            StructureCommand.save(null, args[1], plot,
                    new Position(parseLocation(args[2], args[3], args[4])),
                    new Position(parseLocation(args[5], args[6], args[7])));

        }else if (args.length == 5 && args[0].equals("paste")) {
            StructureCommand.paste(null, args[1], plot, new Position(parseLocation(args[2], args[3], args[4])));

        }else if (args.length == 2 && args[0].equals("delete")) {
            StructureCommand.delete(null, args[1], plot);
        }

        return 1;
    }
}
