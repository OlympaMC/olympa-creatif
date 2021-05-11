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

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSession.Stage;
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
import fr.olympa.olympacreatif.data.OcPermissions;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotId;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OCparam;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.data.PermissionsManager.ComponentCreatif;
import fr.olympa.olympacreatif.utils.NBTcontrollerUtil;
import fr.olympa.olympacreatif.world.WorldManager;

@Deprecated(forRemoval = true)
public class StaffGui extends IGui {
	
	private static final Set<PlotId> resetingPlots = new HashSet<PlotId>();
	
	public StaffGui(IGui gui) {
		super(gui, "Interface staff", 2, gui.staffPlayer);
		
		inv.setItem(inv.getSize() - 1, new ItemStack(Material.AIR));
		
		//set items pour staff perms
		OlympaPermission perm = OcPermissions.STAFF_BYPASS_PLOT_KICK_AND_BAN;
		StaffPerm sPerm = StaffPerm.BYPASS_KICK_BAN;
		
		setItem(0, ItemUtils.item(Material.ACACIA_FENCE_GATE, "§6Bypass kick/ban plot", "§7Permet d'entrer sur les plots même", "§7si le propriétaire vous en a banni"), getStaffPermSwitchConsumer(perm, sPerm));	
		setItem(0 + 9, getStateIndicator(p.hasStaffPerm(sPerm), perm), null);

		perm = OcPermissions.STAFF_BYPASS_VANILLA_COMMANDS;
		sPerm = StaffPerm.GHOST_MODE;
		setItem(1, ItemUtils.item(Material.COMMAND_BLOCK, "§6Bypass commandes vanilla", "§7Permet de ne pas être affecté par", "§7les commandes vanilla du type /kill, /tp, ..."), getStaffPermSwitchConsumer(perm, sPerm));	
		setItem(1 + 9, getStateIndicator(p.hasStaffPerm(sPerm), perm), null);

		perm = OcPermissions.STAFF_BYPASS_WORLDEDIT;
		sPerm = StaffPerm.WORLDEDIT;
		setItem(2, ItemUtils.item(Material.WOODEN_AXE, "§6Bypass WorldEdit", "§7Permet d'utiliser les fonctionnalités WorldEdit", "§7sur tous les plots et la route"), getStaffPermSwitchConsumer(perm, sPerm));	
		setItem(2 + 9, getStateIndicator(p.hasStaffPerm(sPerm), perm), null);

		perm = OcPermissions.STAFF_PLOT_FAKE_OWNER;
		sPerm = StaffPerm.OWNER_EVERYWHERE;
		setItem(3, ItemUtils.item(Material.REDSTONE_TORCH, "§6Fake owner", "§7Permet d'éditer les paramètres du plot comme si", "§7vous en étiez le propriétaire"), getStaffPermSwitchConsumer(perm, sPerm));	
		setItem(3 + 9, getStateIndicator(p.hasStaffPerm(sPerm), perm), null);

		setItem(4, ItemUtils.item(Material.WHITE_STAINED_GLASS_PANE, " "), null);
		setItem(4 + 9, ItemUtils.item(Material.WHITE_STAINED_GLASS_PANE, " "), null);
		
		//désactivation worldedit et tags custom

		final OlympaPermission p1 = OcPermissions.STAFF_MANAGE_COMPONENT;
		setItem(5, ItemUtils.item(Material.PAPER, "§6Désactivation commandblocks et commandes vanilla", "§2Fonction de sécurité.", "§2Clic molette pour modifier.", " ", "§7Permet de désactiver les commandblocks", "§7ainsi que toutes les commandes ", "§7vanilla (/tellraw, /kill, ...)", "§7en cas de de problème.", " ", "§cAttention : au redémarage, les commandblocks et ", "§ccommandes vanilla seront de nouveau activés !"), 
				(it, c, s) -> {
					if (!p1.hasPermission(p) || c != ClickType.MIDDLE)
						return;
					
					ComponentCreatif.COMMANDBLOCKS.toggle();
					
					setItem(5 + 9, getStateIndicator(ComponentCreatif.COMMANDBLOCKS.isActivated(), p1), null);
				});	
		setItem(5 + 9, getStateIndicator(ComponentCreatif.COMMANDBLOCKS.isActivated(), p1), null);

		
		if (plugin.getWEManager() != null) {
			final OlympaPermission p2 = OcPermissions.STAFF_BYPASS_WORLDEDIT;
			setItem(6, ItemUtils.item(Material.DIAMOND_AXE, "§6Désactivation totale de WorldEdit", "§2Fonction de sécurité.", "§2Clic molette pour modifier.", " ", "§7Permet d'interromptre instantanément", "§7toutes les tâches WorldEdit sur le serveur", "§7et de désactiver le plugin.", " ", "§cAttention : au redémarage, WorldEdit sera de nouveau activé !"),
					(it, c, s) -> {
						if (!p2.hasPermission(p) || c != ClickType.MIDDLE)
							return;

						ComponentCreatif.WORLDEDIT.toggle();
						
						setItem(6 + 9, getStateIndicator(!ComponentCreatif.WORLDEDIT.isActivated(), p2), null);
					});
			setItem(6 + 9, getStateIndicator(!ComponentCreatif.WORLDEDIT.isActivated(), p2), null);
		}
		
		
		if (plot != null) {
			final OlympaPermission p3 = OcPermissions.STAFF_RESET_PLOT;
			setItem(7, ItemUtils.item(Material.TNT, "§6Reset de la parcelle " + plot + " (§7" + plot.getMembers().getOwner().getName() + "§6)", "§2Faire CTRL+drop sur cet item pour reset la parcelle.", " ", "§7Lance le reset complet de la parcelle", "§7à son état d'origine (herbe seule).", " ", "§cAttention : cette action ne peut pas être annulée !"),
					(it, c, s) -> {
						if (plot == null || !p3.hasPermission(p) || c != ClickType.CONTROL_DROP || resetingPlots.contains(plot.getId()))
							return;	
						
						plugin.getWEManager().resetPlot(p, plot);
						//resetingPlots.remove(plot.getPlotId());
						//resetingPlots.add(plot.getPlotId());
						
						//OCmsg.WE_PLOT_RESETING.send(p, plot);
						
						setItem(7 + 9, getStateIndicator(true, p3), null);
					});
			setItem(7 + 9, getStateIndicator(resetingPlots.contains(plot.getId()), p3), null);	
		}	
	}
	
	private TriConsumer<ItemStack, ClickType, Integer> getStaffPermSwitchConsumer(OlympaPermission perm, StaffPerm sPerm){
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
