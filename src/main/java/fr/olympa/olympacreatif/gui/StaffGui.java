package fr.olympa.olympacreatif.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.text.DefaultEditorKit.PasteAction;

import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.BlockVector;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacerListener;
import org.primesoft.asyncworldedit.api.blockPlacer.IJobEntryListener;
import org.primesoft.asyncworldedit.api.blockPlacer.entries.IJobEntry;
import org.primesoft.asyncworldedit.api.blockPlacer.entries.JobStatus;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.utils.IFuncParamEx;
import org.primesoft.asyncworldedit.api.worldedit.IAsyncEditSessionFactory;
import org.primesoft.asyncworldedit.api.worldedit.ICancelabeEditSession;
import org.primesoft.asyncworldedit.api.worldedit.IEditSession;
import org.primesoft.asyncworldedit.api.worldedit.IThreadSafeEditSession;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.util.EditSessionBuilder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BlockTypes;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.region.tracking.BypassCommand;
import fr.olympa.olympacreatif.data.PermissionsList;
import fr.olympa.olympacreatif.plot.PlotId;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.utils.NBTcontrollerUtil;
import fr.olympa.olympacreatif.world.WorldManager;

public class StaffGui extends IGui {
	
	private static final Set<PlotId> resetingPlots = new HashSet<PlotId>();
	
	public StaffGui(IGui gui) {
		super(gui, "Interface staff", 2);
		
		inv.setItem(inv.getSize() - 1, new ItemStack(Material.AIR));
		
		//set items pour staff perms
		OlympaPermission perm = PermissionsList.STAFF_BYPASS_PLOT_KICK_AND_BAN;
		StaffPerm sPerm = StaffPerm.BYPASS_KICK_AND_BAN;
		
		setItem(0, ItemUtils.item(Material.ACACIA_FENCE_GATE, "§6Bypass kick/ban plot", "§7Permet d'entrer sur les plots même", "§7si le propriétaire vous en a banni"), getConsumer(perm, sPerm));	
		setItem(0 + 9, getStateIndicator(p.hasStaffPerm(sPerm), perm), null);

		perm = PermissionsList.STAFF_BYPASS_VANILLA_COMMANDS;
		sPerm = StaffPerm.BYPASS_VANILLA_COMMANDS;
		setItem(1, ItemUtils.item(Material.COMMAND_BLOCK, "§6Bypass commandes vanilla", "§7Permet de ne pas être affecté par", "§7les commandes vanilla du type /kill, /tp, ..."), getConsumer(perm, sPerm));	
		setItem(1 + 9, getStateIndicator(p.hasStaffPerm(sPerm), perm), null);

		perm = PermissionsList.STAFF_BYPASS_WORLDEDIT;
		sPerm = StaffPerm.BYPASS_WORLDEDIT;
		setItem(2, ItemUtils.item(Material.WOODEN_AXE, "§6Bypass WorldEdit", "§7Permet d'utiliser les fonctionnalités WorldEdit", "§7sur tous les plots et la route"), getConsumer(perm, sPerm));	
		setItem(2 + 9, getStateIndicator(p.hasStaffPerm(sPerm), perm), null);

		perm = PermissionsList.STAFF_PLOT_FAKE_OWNER;
		sPerm = StaffPerm.FAKE_OWNER_EVERYWHERE;
		setItem(3, ItemUtils.item(Material.REDSTONE_TORCH, "§6Fake owner", "§7Permet d'éditer les paramètres du plot comme si", "§7vous en étiez le propriétaire"), getConsumer(perm, sPerm));	
		setItem(3 + 9, getStateIndicator(p.hasStaffPerm(sPerm), perm), null);

		setItem(4, ItemUtils.item(Material.WHITE_STAINED_GLASS_PANE, " "), null);
		setItem(4 + 9, ItemUtils.item(Material.WHITE_STAINED_GLASS_PANE, " "), null);
		
		//désactivation worldedit et tags custom

		final OlympaPermission p1 = PermissionsList.STAFF_DEACTIVATE_CUSTOM_TAGS;
		setItem(5, ItemUtils.item(Material.PAPER, "§6Désactivation de tous les tags custom", "§2Fonction de sécurité.", "§2Clic molette pour modifier.", " ", "§7Permet de désactiver tous", "§7les tags NBT custom sur le serveur en cas", "§7de problème.", " ", "§cAttention : au redémarage, les tags custom seront de nouveau activés !"), 
				(it, c, s) -> {
					if (!p1.hasPermission(p) || c != ClickType.MIDDLE)
						return;
					
					NBTcontrollerUtil.setDenyAllCustomFlags(!NBTcontrollerUtil.getDenyAllCustomFlags());
					setItem(5 + 9, getStateIndicator(NBTcontrollerUtil.getDenyAllCustomFlags(), p1), null);
				});	
		setItem(5 + 9, getStateIndicator(NBTcontrollerUtil.getDenyAllCustomFlags(), p1), null);

		
		final OlympaPermission p2 = PermissionsList.STAFF_DEACTIVATE_WORLD_EDIT;
		setItem(6, ItemUtils.item(Material.DIAMOND_AXE, "§6Désactivation totale de WorldEdit", "§2Fonction de sécurité.", "§2Clic molette pour modifier.", " ", "§7Permet d'interromptre instantanément", "§7toutes les tâches WorldEdit sur le serveur", "§7et de désactiver le plugin.", " ", "§cAttention : au redémarage, WorldEdit sera de nouveau activé !"),
				(it, c, s) -> {
					if (!p2.hasPermission(p) || c != ClickType.MIDDLE)
						return;
					
					plugin.getWEManager().toggleWeActivation();
					
					setItem(6 + 9, getStateIndicator(!plugin.getWEManager().isWeEnabled(), p2), null);
				});
		setItem(6 + 9, getStateIndicator(!plugin.getWEManager().isWeEnabled(), p2), null);
		
		
		if (plot != null) {
			final OlympaPermission p3 = PermissionsList.STAFF_RESET_PLOT;
			setItem(7, ItemUtils.item(Material.TNT, "§6Reset de la parcelle " + plot + " (§7" + plot.getMembers().getOwner().getName() + "§6)", "§2Dropper cet item pour reset la parcelle.", " ", "§7Lance le reset complet de la parcelle", "§7à son état d'origine (herbe seule).", " ", "§cAttention : cette action ne peut pas être annulée !"),
					(it, c, s) -> {
						if (!p3.hasPermission(p) || c != ClickType.CONTROL_DROP || resetingPlots.contains(plot.getPlotId()))
							return;	
						
						plugin.getTask().runTaskAsynchronously(() -> {
							
							int xMin = plot.getPlotId().getLocation().getBlockX();
							int zMin = plot.getPlotId().getLocation().getBlockZ();
							int xMax = xMin + WorldManager.plotSize - 1;
							int zMax = zMin + WorldManager.plotSize - 1;

							try (EditSession session = new EditSession(new EditSessionBuilder(FaweAPI.getWorld(plugin.getWorldManager().getWorld().getName())))) {
								for (int x = xMin ; x <= xMax ; x++)
									for (int z = zMin ; z <= zMax ; z++)
										session.setBlock(x, 0, z, BlockTypes.BEDROCK);

								for (int x = xMin ; x <= xMax ; x++)
									for (int z = zMin ; z <= zMax ; z++)
										for (int y = 1 ; y < WorldManager.worldLevel ; y++)
											session.setBlock(x, y, z, BlockTypes.DIRT);

								for (int x = xMin ; x <= xMax ; x++)
									for (int z = zMin ; z <= zMax ; z++)
										session.setBlock(x, WorldManager.worldLevel, z, BlockTypes.GRASS_BLOCK);

								for (int x = xMin ; x <= xMax ; x++)
									for (int z = zMin ; z <= zMax ; z++)
										for (int y = WorldManager.worldLevel + 1 ; y < 256 ; y++)
											session.setBlock(x, y, z, BlockTypes.AIR);							
							}
							
							p.getPlayer().sendMessage("§dLa réinitialisation de la parcelle " + plot + " est terminé !");
							resetingPlots.remove(plot.getPlotId());
						});

						resetingPlots.add(plot.getPlotId());
						
						p.getPlayer().sendMessage("§dLa parcelle " + plot + " va bientôt se réinitialiser.");
						setItem(7 + 9, getStateIndicator(true, p3), null);
					});
			setItem(7 + 9, getStateIndicator(resetingPlots.contains(plot.getPlotId()), p3), null);	
		}	
	}
	
	private TriConsumer<ItemStack, ClickType, Integer> getConsumer(OlympaPermission perm, StaffPerm sPerm){
		return (it, c, s) -> {
			if (!perm.hasPermission(p))
				return;
			
			p.toggleStaffPerm(sPerm);
			setItem(s + 9, getStateIndicator(p.hasStaffPerm(sPerm), perm), null);
		};
	}
	
	private ItemStack getStateIndicator(boolean state, OlympaPermission perm) {
		if (state)
			return ItemUtils.item(Material.LIME_WOOL, "§aActif", "§7Rang nécessaire : " + perm.getMinGroup().getName(p.getGender()), " ", "§7Pour modifier la valeur, cliquez", "§7sur l'item au dessus");
		else
			return ItemUtils.item(Material.RED_WOOL, "§cInactif", "§7Rang nécessaire : " + perm.getMinGroup().getName(p.getGender()), " ", "§7Pour modifier la valeur, cliquez", "§7sur l'item au dessus");
	}
}
