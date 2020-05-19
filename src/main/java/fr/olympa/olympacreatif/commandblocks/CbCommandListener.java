package fr.olympa.olympacreatif.commandblocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_15_R1.block.CraftBlock;
import org.bukkit.entity.Player;
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
	private Map<Plot, Double> commandsLeft = new HashMap<Plot, Double>(); //représente le nombre de commandes encore exécutables pour la parcelle (chaque tick le nombre de commandes resnantes est augmenté jusqu'au max)
	private int maxCommandsLeft;
	private double perTickAddedCommandsLeft;
	
	public CbCommandListener(OlympaCreatifMain plugin) {
		this.plugin = plugin;

		maxCommandsLeft = Integer.valueOf(Message.PARAM_CB_MAX_CMDS_LEFT.getValue());
		perTickAddedCommandsLeft = Integer.valueOf(Message.PARAM_CB_PER_TICK_ADDED_CMDS.getValue());
		
		//chaque seconde, ajoute aux plots le nombre de commandes par tick défini
		new BukkitRunnable() {
			
			@Override
			public void run() {
				for (Entry<Plot, Double> e : new HashMap<Plot, Double>(commandsLeft).entrySet()) 
					commandsLeft.put(e.getKey(), Math.min(e.getValue() + perTickAddedCommandsLeft, maxCommandsLeft));
				
			}
		}.runTaskTimer(plugin, 0, 1);
	}
	
	@EventHandler
	public void onPreprocessCommandServer(ServerCommandEvent e) {
		if (!(e.getSender() instanceof CommandBlock))
			return;
		
		e.setCancelled(true);
			
		CbCommand cmd = getCommand(e.getSender(), ((CommandBlock)e.getSender()).getLocation(), e.getCommand());
		
		if (cmd != null) 
			executeCommandBlockCommand(cmd, e.getSender());			
		
	}
	
	@EventHandler
	public void onPreprocessCommandPlayer(PlayerCommandPreprocessEvent e ) {
		
		CbCommand cmd = getCommand(e.getPlayer(), e.getPlayer().getLocation(), e.getMessage());
		
		if (cmd != null) {
			e.setCancelled(true);
			executeCommandBlockCommand(cmd, e.getPlayer());			
		}
		
	}

	private CbCommand getCommand(CommandSender sender, Location sendLoc, String command) {
		return CbCommand.getCommand(plugin, sender, sendLoc, command);
	}
	
	//retourne true si la commande est bien une commande de commandblock
	private void executeCommandBlockCommand(CbCommand cmd, CommandSender sender) {
		
		Message message = Message.CB_RESULT_FAILED;
		
		Bukkit.broadcastMessage(cmd.getType().toString());
		
		if (!commandsLeft.containsKey(cmd.getPlot()))
			//on ajoute le plot à la liste s'il n'y est pas encore
			commandsLeft.put(cmd.getPlot(), (double) (maxCommandsLeft-1));
		else
			if (commandsLeft.get(cmd.getPlot()) < 1) {
				//si le plot n'a plus assez de commandes restantes, cancel exécution
				sender.sendMessage(Message.CB_NO_COMMANDS_LEFT.getValue());
				return;
			}else
				//si le plot a assez de commandes restantes, retrait d'une d'entre elles avant de passer à l'exécution
				commandsLeft.put(cmd.getPlot(), commandsLeft.get(cmd.getPlot()) - 1);
		
		int result = cmd.execute();
		
		if (result > 0)
			message = Message.CB_RESULT_SUCCESS;
		
		sender.sendMessage(message.getValue().replace("%command%", cmd.getType().toString().toLowerCase()).replace("%result%", result + ""));
		
		//update valeurs NBT commandblock
		if (sender instanceof CommandBlock) {
			CommandBlock cb = (CommandBlock) sender;
			
			TileEntity tile = plugin.getWorldManager().getNmsWorld().getTileEntity(new BlockPosition(cb.getLocation().getX(), cb.getLocation().getY(), cb.getLocation().getZ()));
			
			NBTTagCompound tag = new NBTTagCompound();
			
			tile.save(tag);

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
		}
			
		return;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
}
