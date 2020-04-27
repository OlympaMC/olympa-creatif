package fr.olympa.olympacreatif.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;

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
		
		OlympaPlayer p = AccountProvider.get(((Player)sender).getUniqueId());
		
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
				/*if (!p.hasPermission(PermissionsList.USE_SKULL_COMMAND)) {
					p.getPlayer().sendMessage(Message.INSUFFICIENT_GROUP_PERMISSION.getValue().replace("%group%", PermissionsList.USE_SKULL_COMMAND.getGroup().getName(p.getGender())));
					return false;
				}*/
				p.getPlayer().getInventory().addItem(ItemUtils.skull("", args[1]));
				p.getPlayer().sendMessage(Message.OCO_BLOCK_GIVED.getValue());
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
