package fr.olympa.olympacreatif.worldedit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.eventbus.EventHandler;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.commandblocks.commands.CbCommand;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotId;

public class WorldEditListener extends EventHandler {

	private OlympaCreatifMain plugin;
	
	public WorldEditListener(OlympaCreatifMain plugin) {
		super(Priority.NORMAL);
		
		this.plugin = plugin;
	}

	@Subscribe
	public void onEditSession(EditSessionEvent e) {
		e.setExtent(new AbstractDelegateExtent(e.getExtent()) {

	        @Override
	        public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 pos, T block) throws WorldEditException {
	        	
				Actor actor = e.getActor();
				if (actor == null || !actor.isPlayer())
					return false;
				
				OlympaPlayerCreatif p = AccountProvider.get(actor.getUniqueId());
				
				//Bukkit.broadcastMessage("actor : " + p);
				
				if (p == null)
					return false;

				//Bukkit.broadcastMessage("\nmin point" + getExtent().getMinimumPoint());
				//Bukkit.broadcastMessage("max point" + getExtent().getMaximumPoint());
				
				Plot plotMin = plugin.getPlotsManager().getPlot(getLoc(pos));
				Plot plotMax = plugin.getPlotsManager().getPlot(getLoc(pos));

				//Bukkit.broadcastMessage("plot min : " + plotMin);
				//Bukkit.broadcastMessage("plot max : " + plotMax);
				
				if (plotMin == null || !plotMin.equals(plotMax) || plotMin.getMembers().getPlayerLevel(p) < 2) 
					return false;
				
				//Bukkit.broadcastMessage("block as string : " + block.getAsString());
				//Bukkit.broadcastMessage("block name : " + block.getBlockType().);
				
				String blockName = CbCommand.getUndomainedString(block.getAsString());
				int splitIndex = blockName.indexOf("[");
				if (splitIndex >= 0)
					blockName = blockName.substring(0, splitIndex);
				
				//Bukkit.broadcastMessage("block spigot : " + Material.getMaterial(blockName));
				
				if (plugin.getPerksManager().getKitsManager().hasPlayerPermissionFor(p, Material.getMaterial(blockName)))
					return getExtent().setBlock(pos, block);
				else
					return false;
	        }
		});
	}
	
	public Location getLoc(BlockVector3 pos) {
		return new Location(plugin.getWorldManager().getWorld(), pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
	}
	
	@Override
	public void dispatch(Object event) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return false;
	}
}
