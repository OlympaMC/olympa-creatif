package fr.olympa.olympacreatif.commandblocks.commands;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand.CommandType;
import fr.olympa.olympacreatif.plot.Plot;

public class CmdReplaceitem extends CbCommand {

	public CmdReplaceitem(Entity sender, Location sendingLoc, OlympaCreatifMain plugin,
			Plot plot, String[] commandString) {
		super(CommandType.replaceitem, sender, sendingLoc, plugin, plot, commandString);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public int execute() {
		if (args.length == 0)
			return 0;
		
		//Bukkit.broadcastMessage("args : " + new ArrayList<String>(Arrays.asList(args)).toString());
		
		if (args[0].equals("entity") && args.length >= 4) {
			
			int amount = 1;
			
			//définition quantité
			if (args.length == 5) {
				Double[] range = getDoubleRange(args[4]);
				if (range != null)
					amount = (int)(double)range[1];
				else
					return 0;
			}
			
			//définition item
			ItemStack item = getItemFromString(args[3], sender instanceof Player ? (Player) sender : null);
			
			if (item == null || amount < 0 || amount > 64)
				return 0;
			
			item.setAmount(amount);
			
			//Bukkit.broadcastMessage("item : " + item.toString());
			
			//set item sur slot
			targetEntities = parseSelector(args[1], false);
			
			for (Entity e : targetEntities)
				if (e instanceof LivingEntity)
					setItemEntityOnSlot((LivingEntity) e, item, args[2]);
			
			return targetEntities.size();	
		
		//set item pour un bloc
		}else if (args[0].equals("block") && args.length >= 6) {
			
			//définition loc block et item à set
			Location loc = parseLocation(args[1], args[2], args[3]);
			ItemStack item = getItemFromString(args[5], sender instanceof Player ? (Player) sender : null);
			
			//Bukkit.broadcastMessage("loc : " + loc.toString());
			//Bukkit.broadcastMessage("item : " +item.toString());
			
			if (loc == null || item == null)
				return 0;
			
			BlockState block = plugin.getWorldManager().getWorld().getBlockAt(loc).getState();
			
			Bukkit.broadcastMessage("block : " + block.toString());
			
			if (!(block instanceof Container))
				return 0;

			//définition quantité item à set
			int amount = 1;
			
			if (args.length == 7) {
				Double[] range = getDoubleRange(args[6]);
				
				if (range == null)
					return 0;
				
				amount = (int)(double)range[1];
			}
			
			if (amount < 0 || amount > 64)
				return 0;
			
			item.setAmount(amount);

			//définition slot du coffre
			Double[] slots = getDoubleRange(args[4].split("\\.")[args[4].split("\\.").length - 1]);
			
			if (slots == null)
				return 0;
			
			int slot = (int)(double)slots[1];
			
			if (slot < 0 || slot >= ((Container)block).getInventory().getSize())
				return 0;
			
			//si l'item existe déjà sur ce slot
			if (item.equals(((Container)block).getInventory().getItem(slot)))
				return 0;
			else
				((Container)block).getInventory().setItem(slot, item);
			
			return 1;
		}
		
		return 0;
	}
	
	private void setItemEntityOnSlot(LivingEntity e, ItemStack item, String slot) {
		
		switch(slot) {
		case "armor.head":
			e.getEquipment().setHelmet(item);
			break;
		case "armor.chest":
			e.getEquipment().setChestplate(item);
			break;
		case "armor.legs":
			e.getEquipment().setLeggings(item);
			break;
		case "armor.feet":
			e.getEquipment().setBoots(item);
			break;
		case "weapon.mainhand":
			e.getEquipment().setItemInMainHand(item);
			break;
		case "weapon":
			e.getEquipment().setItemInMainHand(item);
			break;
		case "weapon.offhand":
			e.getEquipment().setItemInOffHand(item);
			break;
			
		default:
			if (!(e instanceof Player))
				return;
			
			Double[] range = getDoubleRange(slot.split("\\.")[slot.split("\\.").length - 1]);
			
			if (range == null)
				return;
			
			int index = (int)(double)range[1];
			
			if (slot.startsWith("inventory."))
				index += 9;
			
			if (index < 0 || index > 35)
				return;
			
			((Player)e).getInventory().setItem(index, item);
			break;
		}
	}
}
