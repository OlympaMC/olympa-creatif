package fr.olympa.olympacreatif.commandblocks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_15_R1.command.CraftBlockCommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand.CommandType;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.perks.KitsManager.KitType;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotMembers.PlotRank;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.MinecraftServer;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.TileEntity;

public class CbCommandListener implements Listener {

	private OlympaCreatifMain plugin;
	
	//liste des commandblocks ayant exécuté une commande dans les PARAM_CB_MIN_TICKS_BETWEEN_EACH_CB_EXECUTION derniers ticks
	Map<Location, Integer> blockedExecutionLocs;	
	
	public CbCommandListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		
		blockedExecutionLocs =  new HashMap<Location, Integer>();
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				//ajoute aux plots le nombre de commandes par tick défini

				//for (Plot plot : plugin.getPlotsManager().getPlots())
					//Bukkit.broadcastMessage("plot : " + plot.getPlotId() + " - " + plot.getCbData().getCommandsLeft());
				for (Plot plot : plugin.getPlotsManager().getPlots()) 
					plot.getCbData().addCommandTickets();
				
				//retire de la liste les commandblocks ayant attendu le nombre de ticks nécessaires avant la prochaine commande
				Iterator<Entry<Location, Integer>> iter = blockedExecutionLocs.entrySet().iterator();
				while(iter.hasNext()) {
					Entry<Location, Integer> e = iter.next();
					if (e.getValue() + CommandBlocksManager.minTickBetweenEachCbExecution <= MinecraftServer.currentTick)
						iter.remove();
				}					
			}
		}.runTaskTimer(plugin, 10, 1);
	}
	
	
	@EventHandler //Handle commandes des commandsblocks
	public void onPreprocessCommandServer(ServerCommandEvent e) {
		if (!(e.getSender() instanceof CraftBlockCommandSender))
			return;
		
		e.setCancelled(true);
		Bukkit.broadcastMessage("CB : " + e.getSender() + " - " + blockedExecutionLocs.get(((CraftBlockCommandSender) e.getSender()).getBlock().getLocation()));
		
		CommandBlock cb = ((CommandBlock)((CraftBlockCommandSender)e.getSender()).getBlock().getState());
		
		CbCommand cmd = CbCommand.getCommand(plugin, e.getSender(), cb.getLocation(), e.getCommand());
		
		if (cmd != null && !cmd.getPlot().hasStoplag()) {
			
			//si le commandblock va trop vite, cancel de la commande et maintien des valeurs NBT du commandblock
			if (blockedExecutionLocs.containsKey(cb.getLocation())) {
				//if (cb.getType() == Material.REPEATING_COMMAND_BLOCK)
				//	maintainCbTags(e.getSender());
				return;	
			}else
				//commandblock lents, max 1 cmd/s
				if (plugin.getWorldManager().getWorld().getBlockAt(cb.getLocation().add(0, 1, 0)).getType() == Material.COBWEB)
					blockedExecutionLocs.put(cb.getLocation(), MinecraftServer.currentTick + 20 - CommandBlocksManager.minTickBetweenEachCbExecution);
				else
					blockedExecutionLocs.put(cb.getLocation(), MinecraftServer.currentTick);
			
			
			executeCommandBlockCommand(cmd, e.getSender());		
		}	
		
	}
	
	@EventHandler //Handle commandes des joueurs
	public void onPreprocessCommandPlayer(PlayerCommandPreprocessEvent e) {
		
		//cancel commande si c'est une commande commandblock
		if (CbCommand.getCommandType(e.getMessage()) != null)
			e.setCancelled(true);
		else
			return;
		
		CbCommand cmd = CbCommand.getCommand(plugin, e.getPlayer(), e.getPlayer().getLocation(), e.getMessage());
		
		//return si la commande est nulle
		if (cmd == null) {
			e.getPlayer().sendMessage(Message.CB_INVALID_CMD.getValue()); 
			return;	
		}
		 
		OlympaPlayerCreatif p = AccountProvider.get(e.getPlayer().getUniqueId());
		 
		//si la commandes est un trigger, ou si le joueur a la perm d'exécuter cette commande (selon kit et type cmd)
		if ((cmd.getPlot().getMembers().getPlayerLevel(p) >= cmd.getMinPlotLevelToExecute() && p.hasKit(KitType.COMMANDBLOCK)) || 
				cmd.getType() == CommandType.give || cmd.getType() == CommandType.trigger)
			executeCommandBlockCommand(cmd, e.getPlayer());
		else
			e.getPlayer().sendMessage(Message.INSUFFICIENT_PLOT_PERMISSION.getValue());
	}	
	
	//exécute la commande et si le CommandSender est un commandblock, mise à jour des ses NBTTags
	private void executeCommandBlockCommand(CbCommand cmd, CommandSender sender) {
		
		Message message = Message.CB_RESULT_FAILED;
		
		//Bukkit.broadcastMessage(cmd.getType().toString());

		int neededCmdTickets;
		if (cmd.getType() == CommandType.setblock)
			neededCmdTickets = CommandBlocksManager.cmdTicketByCmdSetblock;
		else
			neededCmdTickets = 1;
		
		if (cmd.getPlot().getCbData().getCommandsTicketsLeft() < neededCmdTickets) {
			//si le plot n'a plus assez de commandes restantes, cancel exécution
			sender.sendMessage(Message.CB_NO_COMMANDS_LEFT.getValue());
			return;
		}else
			//si le plot a assez de commandes restantes, retrait d'une d'entre elles avant de passer à l'exécution
			cmd.getPlot().getCbData().removeCommandTickets(neededCmdTickets);
		
		int result = cmd.execute();
		
		if (result > 0)
			message = Message.CB_RESULT_SUCCESS;
		
		sender.sendMessage(message.getValue(cmd.getType(), result));
		
		//mise à jour NBTTags command block
		if (!(sender instanceof CraftBlockCommandSender))
			return;
		
		setCbTags(sender, result);
		
		/*
		BlockState cb = (BlockState) ((CraftBlockCommandSender) sender).getBlock().getState();
		
		//update valeurs NBT commandblock
		CommandBlock b;

		TileEntity tile = plugin.getWorldManager().getNmsWorld().getTileEntity(
				new BlockPosition(cb.getLocation().getX(), cb.getLocation().getY(), cb.getLocation().getZ()));
		
		NBTTagCompound tag = new NBTTagCompound();
		
		tile.save(tag);
		
		//Bukkit.broadcastMessage(tag.asString());
		
		if (tag.hasKey("SuccessCount"))
			tag.setInt("SuccessCount", (byte) Math.max(0, result));
		
		if (tag.hasKey("LastExecution"))
			tag.setLong("LastExecution", MinecraftServer.currentTick);
		
		if (tag.hasKey("conditionMet"))
			if (result == 0)
				tag.setBoolean("conditionMet", false);
			else
				tag.setBoolean("conditionMet", true);
		
		tile.load(tag);
		*/
	}
	
	private void maintainCbTags(CommandSender sender) {
		
		BlockState cb = (BlockState) ((CraftBlockCommandSender) sender).getBlock().getState();

		TileEntity tile = plugin.getWorldManager().getNmsWorld().getTileEntity(
				new BlockPosition(cb.getLocation().getX(), cb.getLocation().getY(), cb.getLocation().getZ()));
		
		NBTTagCompound tag = new NBTTagCompound();
		
		tile.save(tag);
		
		if (tag.hasKey("SuccessCount"))
			setCbTags(tile, tag.getInt("SuccessCount"));
	}
	
	private void setCbTags(CommandSender sender, int cmdResult) {
		
		BlockState cb = (BlockState) ((CraftBlockCommandSender) sender).getBlock().getState();

		TileEntity tile = plugin.getWorldManager().getNmsWorld().getTileEntity(
				new BlockPosition(cb.getLocation().getX(), cb.getLocation().getY(), cb.getLocation().getZ()));
		
		setCbTags(tile, cmdResult);
	}
	
	private void setCbTags(TileEntity cb, int cmdResult) {
		
		NBTTagCompound tag = new NBTTagCompound();
	
		cb.save(tag);
		
		//Bukkit.broadcastMessage("save tag : " + tag);
		
		//Bukkit.broadcastMessage(tag.asString());
		
		if (tag.hasKey("SuccessCount"))
			tag.setInt("SuccessCount", (byte) Math.max(0, cmdResult));
		
		if (tag.hasKey("LastExecution"))
			tag.setLong("LastExecution", MinecraftServer.currentTick);
		
		if (tag.hasKey("conditionMet"))
			if (cmdResult == 0)
				tag.setBoolean("conditionMet", false);
			else
				tag.setBoolean("conditionMet", true);
		
		cb.load(tag);
	}
}
