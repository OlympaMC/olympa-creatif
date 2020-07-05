package fr.olympa.olympacreatif.commandblocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_15_R1.command.CraftBlockCommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.plot.Plot;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.MinecraftServer;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.TileEntity;

public class CbCommandListener implements Listener {

	private OlympaCreatifMain plugin;
	
	//liste des commandblocks ayant exécuté une commande dans les PARAM_CB_MIN_TICKS_BETWEEN_EACH_CB_EXECUTION derniers ticks
	Map<Location, Integer> blockedExecutionLocs = new HashMap<Location, Integer>();
	
	public CbCommandListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				//ajoute aux plots le nombre de commandes par tick défini
				for (Plot plot : plugin.getPlotsManager().getPlots()) 
					plot.getCbData().addOneCommandLeft();
				
				//retire de la liste les commandblocks ayant attendu le nombre de ticks nécessaires avant la prochaine commande
				for (Entry<Location, Integer> e : blockedExecutionLocs.entrySet())
					if (e.getValue() + CommandBlocksManager.minTickBetweenEachCbExecution <= MinecraftServer.currentTick)
						blockedExecutionLocs.remove(e.getKey());
					
			}
		}.runTaskTimer(plugin, 10, 1);
	}
	
	
	@EventHandler //Handle commandes des commandsblocks
	public void onPreprocessCommandServer(ServerCommandEvent e) {
		
		if (!(e.getSender() instanceof CraftBlockCommandSender) || !(((CraftBlockCommandSender) e.getSender()).getBlock().getState() instanceof CommandBlock))
			return;
		
		e.setCancelled(true);
		
		CommandBlock cb = ((CommandBlock)((CraftBlockCommandSender)e.getSender()).getBlock().getState());
		
		//si le commandblock va trop vite, cancel de la commande
		if (blockedExecutionLocs.containsKey(cb.getLocation()))
			return;
		else
			blockedExecutionLocs.put(cb.getLocation(), MinecraftServer.currentTick);
		
		CbCommand cmd = getCommand(e.getSender(), cb.getLocation(), e.getCommand());
		
		if (cmd != null) 
			executeCommandBlockCommand(cmd, e.getSender());			
		
	}
	
	@EventHandler //Handle commandes des joueurs
	public void onPreprocessCommandPlayer(PlayerCommandPreprocessEvent e ) {
		
		if (CbCommand.getCommandType(e.getMessage()) == null)
			return;

		e.setCancelled(true);
		
		CbCommand cmd = getCommand(e.getPlayer(), e.getPlayer().getLocation(), e.getMessage());
		
		if (cmd != null && cmd.getPlot().getMembers().getPlayerLevel(e.getPlayer()) >= 3) 
			executeCommandBlockCommand(cmd, e.getPlayer());
		else
			e.getPlayer().sendMessage(Message.INSUFFICIENT_PLOT_PERMISSION.getValue());
	}

	private CbCommand getCommand(CommandSender sender, Location sendLoc, String command) {
		return CbCommand.getCommand(plugin, sender, sendLoc, command);
	}
	
	
	
	//exécute la commande et si le CommandSender est un commandblock, mise à jour des ses NBTTags
	private void executeCommandBlockCommand(CbCommand cmd, CommandSender sender) {
		
		Message message = Message.CB_RESULT_FAILED;
		
		//Bukkit.broadcastMessage(cmd.getType().toString());

		if (cmd.getPlot().getCbData().getCommandsLeft() < 1) {
			//si le plot n'a plus assez de commandes restantes, cancel exécution
			sender.sendMessage(Message.CB_NO_COMMANDS_LEFT.getValue());
			return;
		}else
			//si le plot a assez de commandes restantes, retrait d'une d'entre elles avant de passer à l'exécution
			cmd.getPlot().getCbData().removeOneCommandLeft();
		
		int result = cmd.execute();
		
		if (result > 0)
			message = Message.CB_RESULT_SUCCESS;
		
		sender.sendMessage(message.getValue().replace("%command%", cmd.getType().toString().toLowerCase()).replace("%result%", result + ""));
		
		//mise à jour NBTTags command block
		if (!(sender instanceof CraftBlockCommandSender))
			return;
		
		BlockState cb = (BlockState) ((CraftBlockCommandSender) sender).getBlock().getState();
		
		//update valeurs NBT commandblock
			
		TileEntity tile = plugin.getWorldManager().getNmsWorld().getTileEntity(
				new BlockPosition(cb.getLocation().getX(), cb.getLocation().getY(), cb.getLocation().getZ()));
		
		NBTTagCompound tag = new NBTTagCompound();
		
		tile.save(tag);
		
		//Bukkit.broadcastMessage(tag.asString());
		
		if (tag.hasKey("SuccessCount"))
			tag.setInt("SuccessCount", Math.max(0, result));
		
		if (tag.hasKey("LastExecution"))
			tag.setLong("LastExecution", MinecraftServer.currentTick);
		
		if (tag.hasKey("conditionMet"))
			if (result == 0)
				tag.setBoolean("conditionMet", false);
			else
				tag.setBoolean("conditionMet", true);
		
		tile.load(tag);
			
		return;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
}
