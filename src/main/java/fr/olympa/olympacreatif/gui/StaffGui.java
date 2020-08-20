package fr.olympa.olympacreatif.gui;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;

public class StaffGui extends IGui {
	
	public StaffGui(IGui gui) {
		super(gui, "Interface staff", 2);
		
		inv.setItem(inv.getSize() - 1, new ItemStack(Material.AIR));
		
		//set items pour staff perms
		inv.setItem(0, ItemUtils.item(Material.ACACIA_FENCE_GATE, "§6Bypass kick/ban plot", "§7Permet d'entrer sur les plots même", "§7si le propriétaire vous en a banni"));
		inv.setItem(0 + 9, ItemUtils.item(Material.RED_WOOL, "§cInactif", "§7Rang nécessaire : " + StaffPerm.BYPASS_KICK_AND_BAN.getOlympaPerm().getMinGroup().getName(p.getGender())));
		
		inv.setItem(1, ItemUtils.item(Material.COMMAND_BLOCK, "§6Bypass commandes vanilla", "§7Permet de ne pas être affecté par", "§7les commandes vanilla du type /kill, /tp, ..."));
		inv.setItem(1 + 9, ItemUtils.item(Material.RED_WOOL, "§cInactif", "§7Rang nécessaire : " + StaffPerm.BYPASS_VANILLA_COMMANDS.getOlympaPerm().getMinGroup().getName(p.getGender())));
		
		inv.setItem(2, ItemUtils.item(Material.WOODEN_AXE, "§6Bypass WorldEdit", "§7Permet d'utiliser les fonctionnalités WorldEdit", "§7sur tous les plots et la route"));
		inv.setItem(2 + 9, ItemUtils.item(Material.RED_WOOL, "§cInactif", "§7Rang nécessaire : " + StaffPerm.BYPASS_WORLDEDIT.getOlympaPerm().getMinGroup().getName(p.getGender())));
		
		inv.setItem(3, ItemUtils.item(Material.REDSTONE_TORCH, "§6Fake owner", "§7Permet d'éditer le plot comme si vous", "§7en étiez le propriétaire"));
		inv.setItem(3 + 9, ItemUtils.item(Material.RED_WOOL, "§cInactif", "§7Rang nécessaire : " + StaffPerm.BYPASS_WORLDEDIT.getOlympaPerm().getMinGroup().getName(p.getGender())));

		inv.setItem(4, ItemUtils.item(Material.WHITE_STAINED_GLASS_PANE, " "));
		inv.setItem(4 + 9, ItemUtils.item(Material.WHITE_STAINED_GLASS_PANE, " "));
		
		//inv.setItem(5, ItemUtils.item(Material.TNT, "§4Reset de la parcelle " + plot, "§cPour reset la parcelle, cliquez ici", "§cavec une TNT dans la main", "§4ATTENTION : Cette action est irréversible !"));
		
		//TODO clear plot, stoplag plot, ...
		
		if (p.hasStaffPerm(StaffPerm.BYPASS_KICK_AND_BAN))
			toggleSwitch(0);
		if (p.hasStaffPerm(StaffPerm.BYPASS_VANILLA_COMMANDS))
			toggleSwitch(1);
		if (p.hasStaffPerm(StaffPerm.BYPASS_WORLDEDIT))
			toggleSwitch(2);
		if (p.hasStaffPerm(StaffPerm.FAKE_OWNER_EVERYWHERE))
			toggleSwitch(3);
	}

	@Override
	public boolean onClick(Player player, ItemStack current, int slot, ClickType click) {
		
		switch (slot) {
		case 0:
			if (p.toggleStaffPerm(StaffPerm.BYPASS_KICK_AND_BAN)) 
				toggleSwitch(slot);
			
			break;
		case 1:
			if (p.toggleStaffPerm(StaffPerm.BYPASS_VANILLA_COMMANDS))
				toggleSwitch(slot);
			
			break;
		case 2:
			if (p.toggleStaffPerm(StaffPerm.BYPASS_WORLDEDIT))
				toggleSwitch(slot);
			
			break;
		case 3:
			if (p.toggleStaffPerm(StaffPerm.FAKE_OWNER_EVERYWHERE))	
				toggleSwitch(slot);
				
			break;
		}
		return true;
	}
	
	/*
	@SuppressWarnings("deprecation")
	@Override
	public boolean onClickCursor(Player player, ItemStack current, ItemStack cursor, int slot) {
		if (slot != 5 || cursor.getType() != Material.TNT || plot == null || !p.hasStaffPerm(StaffPerm.BYPASS_WORLDEDIT))
			return true;
		
		if (plugin.getWorldEditManager() == null)
			return true;
		
		current.setAmount(0);
		player.closeInventory();
		player.sendMessage("§cLa parcelle " + plot + " est en train de se régénérer. Merci de ne pas relancer le processus.");


    	IAsyncWorldEdit awe = (IAsyncWorldEdit) Bukkit.getPluginManager().getPlugin("AsyncWorldEdit");
    	IThreadSafeEditSession tsSession = ((IAsyncEditSessionFactory)plugin.getWorldEditManager().getSession(player))
    			.getThreadSafeEditSession(new BukkitWorld(plugin.getWorldManager().getWorld()), 0);
	
    	awe.getBlockPlacer().performAsAsyncJob(tsSession, awe.getPlayerManager().getConsolePlayer(), "reset_plot_" + plot, 
    			new IFuncParamEx<Integer, ICancelabeEditSession, MaxChangedBlocksException>() {
					
					@Override
					public Integer execute(ICancelabeEditSession editSession) throws MaxChangedBlocksException {
						
						Location pos1 = plot.getPlotId().getLocation().clone();
						pos1.setY(0);
						Location pos2 = plot.getPlotId().getLocation().clone();
						pos2.setY(256);
						pos2 = pos2.add(WorldManager.plotSize - 1, 0, WorldManager.plotSize - 1);
						
						Bukkit.broadcastMessage("pos1 : " + pos1 + " - pos2 : " + pos2);
						
					    Region region = new CuboidRegion(editSession.getWorld(), getBlockVector(pos1), getBlockVector(pos2));
					    BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
					    try {
						    
						    for (int x = clipboard.getMinimumPoint().getBlockX() ; x <= clipboard.getMaximumPoint().getBlockX() ; x++)
							    for (int y = clipboard.getMinimumPoint().getBlockY() ; y <= clipboard.getMaximumPoint().getBlockY() ; y++)
								    for (int z = clipboard.getMinimumPoint().getBlockZ() ; z <= clipboard.getMaximumPoint().getBlockZ() ; z++)
								    	if (y > WorldManager.worldLevel)
											clipboard.setBlock(BlockVector3.at(x, y, z), BlockTypes.AIR.getDefaultState());
								    	else if (y == 0)
											clipboard.setBlock(BlockVector3.at(x, y, z), BlockTypes.BEDROCK.getDefaultState());
								    	else if (y < WorldManager.worldLevel)
											clipboard.setBlock(BlockVector3.at(x, y, z), BlockTypes.DIRT.getDefaultState());
								    	else //if (y == WorldManager.worldLevel)
											clipboard.setBlock(BlockVector3.at(x, y, z), BlockTypes.GRASS.getDefaultState());
						    

						    Operation operation = new ClipboardHolder(clipboard)
						            .createPaste(editSession)
						            .to(clipboard.getMinimumPoint())
						            // configure here
						            .build();
						    Operations.complete(operation);
					        //IAsyncWorldEditHandler.registerCompletion(player);
					    }catch(WorldEditException e) {
						    	e.printStackTrace();
						}
						return 1;
					}
	    		});
	    return true;
	}

	
	private BlockVector3 getBlockVector(Location loc) {
		return BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}
	*/
	
	//switch l'apparence du switch
	private void toggleSwitch(int slot) {
		if (inv.getItem(slot) == null)
			return;
		
		ItemStack it = inv.getItem(slot);
		
		if (ItemUtils.hasEnchant(it, Enchantment.DURABILITY)) {
			it = ItemUtils.removeEnchant(it, Enchantment.DURABILITY);
			
			inv.getItem(slot + 9).setType(Material.RED_WOOL);
			inv.setItem(slot + 9, ItemUtils.name(inv.getItem(slot + 9), "§cInactif"));
		}else {
			it = ItemUtils.addEnchant(it, Enchantment.DURABILITY, 1);
			
			ItemMeta itMeta = it.getItemMeta();
			itMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			it.setItemMeta(itMeta);
			
			inv.getItem(slot + 9).setType(Material.GREEN_WOOL);
			inv.setItem(slot + 9, ItemUtils.name(inv.getItem(slot + 9), "§aActif"));
		}
	}
}
