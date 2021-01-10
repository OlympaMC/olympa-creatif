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
import fr.olympa.olympacreatif.perks.KitsManager.KitType;

public class WorldEditEventHandler {

	private OlympaCreatifMain plugin;
	
	protected WorldEditEventHandler(OlympaCreatifMain plugin) {
		this.plugin = plugin;
	}

	@Subscribe
	public void onEditSession(EditSessionEvent e) {
		if (!(BukkitAdapter.adapt(e.getActor()) instanceof Player))
			return;
		
		BlockVector3 vecMin = e.getExtent().getMinimumPoint();
		BlockVector3 vecMax = e.getExtent().getMaximumPoint();
		
		Bukkit.broadcastMessage("vec 1 : " + vecMin + " --- " + vecMax);
		
		OlympaPlayerCreatif p = AccountProvider.get(e.getActor().getUniqueId());
		
		for (int x = vecMin.getBlockX() ; x <= vecMax.getBlockX() ; x++)
			for (int y = vecMin.getBlockY() ; y <= vecMax.getBlockY() ; y++)
				for (int z = vecMin.getBlockZ() ; z <= vecMax.getBlockZ() ; z++) {
					Material mat = BukkitAdapter.adapt(e.getExtent().getBlock(x, y, z).getBlockType());
					KitType kit = plugin.getPerksManager().getKitsManager().getKitOf(mat);

					
					if (p.hasKit(kit)) {
						p.getPlayer().sendMessage(OCmsg.WE_NO_KIT_FOR_MATERIAL.getValue(kit.getName(), mat.toString().toLowerCase()));
						
						e.setExtent(new AbstractDelegateExtent(e.getExtent()) {
					        @Override
					        public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 pos, T block) throws WorldEditException {
					            return false;
					        }
					    });
						
						return;
					}
				}
	}
}
