package fr.olympa.olympacreatif.commandblocks;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdScoreboard extends CbCommand {

	CmdType cmdType;
	ObjectivesType objType;
	PlayersType playerType;
	
	public CmdScoreboard(OlympaCreatifMain plugin, Plot plot, String[] commandString) {
		super(plugin, plot, commandString);
		
		if (args.length >= 3)
			cmdType = CmdType.get(args[0]);
		
		if (cmdType == CmdType.OBJECTIVES) {
			objType = ObjectivesType.get(args[1]);

			switch (objType) {
			case ADD:
				break;
			case LIST:
				break;
			case MODIFY:
				break;
			case REMOVE:
				break;
			case SETDISPLAY:
				break;
			}
		}
	}

	private enum CmdType{
		PLAYERS,
		OBJECTIVES;
		
		public static CmdType get(String s) {
			for (CmdType t : CmdType.values())
				if (t.toString().equalsIgnoreCase(s))
					return t;
			return null;
		}
	}
	
	private enum ObjectivesType{
		ADD,
		LIST,
		MODIFY,
		REMOVE,
		SETDISPLAY;
		
		public static ObjectivesType get(String s) {
			for (ObjectivesType t : ObjectivesType.values())
				if (t.toString().equalsIgnoreCase(s))
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
