package fr.olympa.olympacreatif.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.CommandBlocksManager;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand;
import fr.olympa.olympacreatif.commandblocks.commands.CmdGive;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.perks.KitsManager;
import fr.olympa.olympacreatif.perks.KitsManager.KitType;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;
import fr.olympa.olympacreatif.plot.PlotsManager;
import fr.olympa.olympacreatif.utils.NbtParserUtil;
import fr.olympa.olympacreatif.utils.NbtParserUtil.EntitySourceType;
import fr.olympa.olympacreatif.world.WorldManager;
import net.minecraft.server.v1_15_R1.IMaterial;
import net.minecraft.server.v1_15_R1.NBTTagCompound;

public class OcoCommand extends OlympaCommand {

	private OlympaCreatifMain plugin;
	
	public OcoCommand(OlympaCreatifMain plugin, String cmd, String[] aliases) {
		super(plugin, cmd, aliases);
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return false;
		
		OlympaPlayerCreatif p = AccountProvider.get(((Player)sender).getUniqueId());
		
		switch (args.length) {
		case 1:
			switch (args[0]) {
			case "hat":
				/*if (!p.hasPermission(PermissionsList.USE_HAT_COMMAND)) {
					p.getPlayer().sendMessage(Message.INSUFFICIENT_GROUP_PERMISSION.getValue().replace("%group%", PermissionsList.USE_SKULL_COMMAND.getGroup().getName(p.getGender())));
					return false;
				}*/
				p.getPlayer().sendMessage(Message.OCO_HAT_SUCCESS.getValue());
				p.getPlayer().getInventory().setHelmet(new ItemStack(p.getPlayer().getInventory().getItemInMainHand().getType()));
				break;
				
				
			case "mb":
				/*if (!p.hasPermission(PermissionsList.USE_MICRO_BLOCKS)) {
					p.getPlayer().sendMessage(Message.INSUFFICIENT_GROUP_PERMISSION.getValue().replace("%group%", PermissionsList.USE_MICRO_BLOCKS.getGroup().getName(p.getGender())));
					return false;
				}*/
				plugin.getPerksManager().getMicroBlocks().openGui(p.getPlayer());
				break;
				
				
			case "export":
				/*if (!p.hasPermission(PermissionsList.USE_PLOT_EXPORTATION)) {
					p.getPlayer().sendMessage(Message.INSUFFICIENT_GROUP_PERMISSION.getValue().replace("%group%", PermissionsList.USE_PLOT_EXPORTATION.getGroup().getName(p.getGender())));
					return false;
				}*/
				Plot plot = plugin.getPlotsManager().getPlot(p.getPlayer().getLocation());
				if (plot != null && plot.getMembers().getPlayerRank(p.getPlayer()) == PlotRank.OWNER) {
					plugin.getPerksManager().getSchematicCreator().export(plot);
					p.getPlayer().sendMessage(Message.OCO_EXPORT_SUCCESS.getValue());
				}else
					p.getPlayer().sendMessage(Message.OCO_EXPORT_FAILED.getValue());					
					
				break;
				
				
			case "debug":
				Plot plot2 = plugin.getPlotsManager().getPlot(p.getPlayer().getLocation());
				if (plot2 == null) {
					sender.sendMessage(Message.ERROR_PLOT_NOT_LOADED.getValue());
					break;
				}
				
				String debug = "\n   §6>>> Débug plot " + plot2.getPlotId() + " :";
				debug += "\n   §e> Joueurs : §a" + plot2.getPlayers().size();
				debug += "\n   §e> Entités : §a" + plot2.getEntities().size() + "/" + WorldManager.maxTotalEntitiesPerPlot;
				debug += "\n   §e> Equipes : §a" + plot2.getCbData().getTeams().size() + "/" + CommandBlocksManager.maxTeamsPerPlot;
				debug += "\n   §e> Objectifs : §a" + plot2.getCbData().getObjectives().size() + "/" + CommandBlocksManager.maxObjectivesPerPlot;
				debug += "\n   §e> Tickets commandblocks : §a" + plot2.getCbData().getCommandsTicketsLeft() + "/" + CommandBlocksManager.maxCommandsTicketst;
				
				sender.sendMessage(debug);
				break;
				
			default:
				sender.sendMessage(Message.OCO_COMMAND_HELP.getValue());
				break;
			}
			break;
		case 2:
			switch(args[0]) {
			case "mb":
				/*if (!p.hasPermission(PermissionsList.USE_MICRO_BLOCKS)) {
					p.getPlayer().sendMessage(Message.INSUFFICIENT_GROUP_PERMISSION.getValue().replace("%group%", PermissionsList.USE_MICRO_BLOCKS.getGroup().getName()));
					return false;
				}*/
				ItemStack item = plugin.getPerksManager().getMicroBlocks().getMb(args[1]);
				if (item != null) {
					p.getPlayer().getInventory().addItem(item);
					p.getPlayer().sendMessage(Message.OCO_BLOCK_GIVED.getValue());
				}else
					p.getPlayer().sendMessage(Message.OCO_UNKNOWN_MB.getValue());
				break;
				
			case "skull":
				Consumer<ItemStack> consumer = sk -> p.getPlayer().getInventory().addItem(sk);
				ItemUtils.skull(consumer, "§6Tête de " + args[1], args[1]);
				p.getPlayer().sendMessage(Message.OCO_BLOCK_GIVED.getValue());
				break;
			
				/*
			case "give":
				args[1] = args[1].toUpperCase();
				if (plugin.getWorldManager().getRestrictedItems().keySet().contains(Material.getMaterial(args[1])))
					if (plugin.getWorldManager().getRestrictedItems().get(Material.getMaterial(args[1])).hasPermission(p.getPlayer().getUniqueId())) {
						p.getPlayer().getInventory().addItem(new ItemStack(Material.getMaterial(args[1])));
						p.getPlayer().sendMessage(Message.OCO_GIVE_SUCCESSFUL.getValue().replace("%item%", args[1].toLowerCase().replace("_", " ")));
					}
					else
						p.getPlayer().sendMessage(Message.INSUFFICIENT_KIT_PERMISSION.getValue().replace("%kit%", 
								plugin.getWorldManager().getRestrictedItems().get(Material.getMaterial(args[1])).toString().toLowerCase()));
				else
					p.getPlayer().sendMessage(Message.OCO_GIVE_INDISPONIBLE_BLOCK.getValue());
				break;
				*/
				
			case "speed":
				Plot plot = plugin.getPlotsManager().getPlot(p.getPlayer().getLocation());
				
				if (plot != null)
					if (plot.getMembers().getPlayerLevel(p) == 0)
						p.getPlayer().sendMessage(Message.INSUFFICIENT_PLOT_PERMISSION.getValue());
				
				float level = 0.1f;
				
				try {
					level = Math.min(Math.max(Float.valueOf(args[1])/18f, 0.1f), 1f);
				}catch(NumberFormatException e) {
				}
				
				p.getPlayer().setFlySpeed(level);
				p.getPlayer().sendMessage(Message.OCO_SET_FLY_SPEED.getValue());
				
				break;
				
			default:
				sender.sendMessage(Message.OCO_COMMAND_HELP.getValue());
				break;
			}
			break;
		default:
			sender.sendMessage(Message.OCO_COMMAND_HELP.getValue());
			break;
		}
		
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> list = new ArrayList<String>();
		List<String> response = new ArrayList<String>();
		
		if (args.length == 1) {
			list.add("skull");
			list.add("hat");
			list.add("mb");
			list.add("export");
			list.add("speed");
			list.add("debug");
		}
		else if (args.length == 2 && args[0].equals("mb")) {
			for (Entry<String, ItemStack> e : plugin.getPerksManager().getMicroBlocks().getAllMbs().entrySet())
				list.add(e.getKey());
		}else
			for (Player p : Bukkit.getOnlinePlayers())
				list.add(p.getName());

		for (String s : list)
			if (s.startsWith(args[args.length-1]))
				response.add(s);
		
		return response;
	}
}
