package fr.olympa.olympacreatif.worldedit;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif.StaffPerm;
import fr.olympa.olympacreatif.perks.KitsManager.KitType;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotPerm;

@Deprecated
public class WorldEditEventHandlerLEGACY {

	private OlympaCreatifMain plugin;
	
	protected WorldEditEventHandlerLEGACY(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		plugin.getLogger().info("§aEditSessionEvent handler enregistré");
	}

	@Subscribe
	public void onEditSession(EditSessionEvent e) {
		if (!(BukkitAdapter.adapt(e.getActor()) instanceof Player))
			return;
		
		BlockVector3 vecMin = e.getExtent().getMinimumPoint();
		BlockVector3 vecMax = e.getExtent().getMaximumPoint();
		
		Bukkit.broadcastMessage("vec 1 : " + vecMin + " --- " + vecMax);
		
		OlympaPlayerCreatif p = AccountProvider.get(e.getActor().getUniqueId());
		Plot plot = plugin.getPlotsManager().getPlot(p.getPlayer().getLocation());

		/*Material mat = BukkitAdapter.adapt(e.getExtent().getBlock(BlockVector3.at(x, y, z)).getBlockType());
		KitType kit = plugin.getPerksManager().getKitsManager().getKitOf(mat);*/
		
		if (!p.hasStaffPerm(StaffPerm.WORLDEDIT) && (plot == null || !PlotPerm.USE_WE.has(plot, p)))
			e.setExtent(new AbstractDelegateExtent(e.getExtent()) {
		        @Override
		        public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 pos, T block) throws WorldEditException {
		        	return false;
		        }
		    });
		else
			e.setExtent(new AbstractDelegateExtent(e.getExtent()) {
		        @Override
		        public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 pos, T block) throws WorldEditException {
		        	return plot.getPlotId().isInPlot(pos.getBlockX(), pos.getBlockZ()) ? super.setBlock(pos, block) : false;
		        }
		    });		
	}
}
