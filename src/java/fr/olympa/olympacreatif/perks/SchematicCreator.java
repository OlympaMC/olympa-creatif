package fr.olympa.olympacreatif.perks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import com.boydti.fawe.util.EditSessionBuilder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCmsg;
import fr.olympa.olympacreatif.data.OCparam;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.data.PermissionsManager.ComponentCreatif;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotId;

public class SchematicCreator {

	  private OlympaCreatifMain plugin;
	  private Set<PlotId> exportingPlotsCache = new HashSet<PlotId>();
	  private Set<PlotId> restoringPlotCache = new HashSet<PlotId>();
	  
	public SchematicCreator(OlympaCreatifMain plugin) {
	    this.plugin = plugin;
	}
	    
	public void export(final Plot plot, final OlympaPlayerCreatif p) {
	    if (!ComponentCreatif.WORLDEDIT.isActivated()) {
	    	OCmsg.WE_DISABLED.send(p);
	    	return;
	    }else if (exportingPlotsCache.contains(plot.getId())) {
	    	OCmsg.WAIT_BEFORE_REEXECUTE_COMMAND.send(p, plot);
	    	return;	
	    }

		OCmsg.WE_START_GENERATING_PLOT_SCHEM.send(p, plot);
		exportingPlotsCache.add(plot.getId());
		
	    plugin.getTask().runTaskAsynchronously(() -> {
			
			//création fichier & dir si existants
		    File dir = new File(plugin.getDataFolder() + "/schematics");
		    File schemFile = new File(dir.getAbsolutePath(), plot.getMembers().getOwner().getName() + "_" + plot.getId() + ".schem");
		    plugin.getDataFolder().mkdir();
		    dir.mkdir();
		    try {
				schemFile.delete();
				schemFile.createNewFile();
				schemFile.deleteOnExit();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		    //create the Clipboard to copy
		    BlockVector3 v1 = BlockVector3.at(plot.getId().getLocation().getBlockX(), 0, plot.getId().getLocation().getBlockZ());
		    BlockVector3 v2 = BlockVector3.at(plot.getId().getLocation().getBlockX() + OCparam.PLOT_SIZE.get() - 1, 255, plot.getId().getLocation().getBlockZ() + OCparam.PLOT_SIZE.get() - 1);
		    
		    CuboidRegion region = new CuboidRegion(BukkitAdapter.adapt(plugin.getWorldManager().getWorld()), v1, v2);
		    BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

		    EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(plugin.getWorldManager().getWorld()), -1);

		    ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(session, region, clipboard, region.getMinimumPoint());
		    forwardExtentCopy.setCopyingEntities(true);
		    
		    
		    //Generates the .schematic file from the clipboard
			try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(schemFile))) {
			    Operations.complete(forwardExtentCopy);
			    writer.write(clipboard);
			    
			} catch (IOException | WorldEditException e) {
				e.printStackTrace();
			}
			
			plugin.getDataManager().saveSchemToDb(p, plot, schemFile);
			OCmsg.WE_COMPLETE_GENERATING_PLOT_SCHEM.send(p, plot);
	    	plugin.getTask().runTaskLater(() -> exportingPlotsCache.remove(plot.getId()), 20 * 60 * 60);
	    });
		//return "§4La fonctionnalité d'export de la parcelle est indisponible pendant la bêta, désolé ¯\\_༼ ಥ ‿ ಥ ༽_/¯";
	}
    
	public void restore(final Plot plot, final OlympaPlayerCreatif p) {
	    if (!ComponentCreatif.WORLDEDIT.isActivated()) {
	    	OCmsg.WE_DISABLED.send(p);
	    	return;
	    }else if (restoringPlotCache.contains(plot.getId())) {
	    	OCmsg.WAIT_BEFORE_REEXECUTE_COMMAND.send(p, plot);
	    	return;	
	    }

		OCmsg.WE_START_RESTORING_PLOT.send(p, plot);
	    restoringPlotCache.add(plot.getId());
	    
	    plugin.getTask().runTaskAsynchronously(() -> {
	    	Blob blob = plugin.getDataManager().loadSchemFromDb(p, plot);
	    	
	    	if (blob == null) {
	    		OCmsg.WE_NO_PLOT_SCHEM_FOUND.send(p);
	    		return;	
	    	}
	    	
	    	try (ClipboardReader reader = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getReader(blob.getBinaryStream())) {
				Clipboard clipboard = reader.read();
			    BlockVector3 origin = BlockVector3.at(plot.getId().getLocation().getBlockX(), 0, plot.getId().getLocation().getBlockZ());

			    try (EditSession editSession = new EditSessionBuilder(BukkitAdapter.adapt(plugin.getWorldManager().getWorld()))
			    		.allowedRegionsEverywhere().limitUnlimited().build()) {
			        Operation operation = new ClipboardHolder(clipboard)
			                .createPaste(editSession)
			                .to(origin)
			                .ignoreAirBlocks(false)
			                .build();
			        
			        Operations.complete(operation);
			    }
			    
				OCmsg.WE_COMPLETE_RESTORING_PLOT.send(p, plot);
			    
			} catch (IOException | SQLException e) {
				OCmsg.WE_FAIL_RESTORING_PLOT.send(p, plot);
				e.printStackTrace();
			}
	    	
	    	plugin.getTask().runTaskLater(() -> restoringPlotCache.remove(plot.getId()), 20 * 60 * 60);
	    });
	}
	

	/*
	//renvoie le nom du fichier si créé correctement, sinon null
	public String export(String fileName, int x1, int y1, int z1, int x2, int y2, int z2) {
		
		
		//création fichier & dir si existants
	    File dir = new File(plugin.getDataFolder() + "/schematics");
	    File file = new File(dir.getAbsolutePath(), fileName + ".schem");
	    plugin.getDataFolder().mkdir();
	    dir.mkdir();
	    try {
			file.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    
	    NBTTagCompound schematicTag = new NBTTagCompound();
	    
	    //création metadata
        NBTTagCompound metadata = new NBTTagCompound();
        metadata.setInt("WEOffsetX", 1);
        metadata.setInt("WEOffsetY", 0);
        metadata.setInt("WEOffsetZ", 1);
	    
        schematicTag.set("MetaData", metadata);
        
        NBTTagCompound palette = new NBTTagCompound();
        NBTTagList tileEntitiesList = new NBTTagList();
        List<String> paletteList = new ArrayList<String>();
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(3 * 3);
		
		World cw = plugin.getWorldManager().getNmsWorld();
		
		for (int y = Math.min(y1, y2) ; y <= Math.max(y1, y2) ; y++)
			for (int z = Math.min(z1, z2) ; z <= Math.max(z1, z2) ; z++)
				for (int x = Math.min(x1, x2) ; x <= Math.max(x1, x2) ; x++) {
					
					String blockData = plugin.getWorldManager().getWorld().getBlockAt(x, y, z).getBlockData().getAsString();
					
					if (!paletteList.contains(blockData)) {
						palette.setInt(blockData, paletteList.size());
						paletteList.add(blockData);
					}

					//enregistrement du block (en référence à la palette), bytes signés (merci java)
					int blockId = paletteList.indexOf(blockData);
                    
                    while ((blockId & -128) != 0) {
                        buffer.write(blockId & 127 | 128);
                        blockId >>>= 7;
                    }
                    buffer.write(blockId);
                    
                    //enregistrement BlockEntities
                    TileEntity tileEntity = cw.getTileEntity(new BlockPosition(x, y, z));
                    
                    if (tileEntity != null) {
                    	NBTTagCompound comp = new NBTTagCompound();

                    	tileEntity.save(comp);
                    	
                    	int[] pos = new int[] {x-Math.min(x1, x2),y-Math.min(y1, y2),z-Math.min(z1, z2)};
                    	
                    	comp.set("Id", comp.get("id"));
                    	comp.setIntArray("Pos", pos);
                    	comp.remove("x");
                    	comp.remove("y");
                    	comp.remove("z");
                    	comp.remove("id");
                    	
                    	tileEntitiesList.add(tileEntity.save(comp));
                    }
				}
		
		schematicTag.set("Palette", palette);
		schematicTag.setByteArray("BlockData", buffer.toByteArray());
		schematicTag.set("BlockEntities", tileEntitiesList);
		
        schematicTag.setInt("PaletteMax", paletteList.size());
        
        schematicTag.setInt("DataVersion", 2230);
        schematicTag.setInt("Version", 2);
        
        schematicTag.setShort("Height", (short) (Math.abs(y1-y2)+1));
        schematicTag.setShort("Length", (short) (Math.abs(z1-z2)+1));
        schematicTag.setShort("Width", (short) (Math.abs(x1-x2)+1));
		
        try {
        	NBTTagCompound finalTag = new NBTTagCompound();
        	finalTag.set("Schematic", schematicTag);
        	
        	File intermediateFile = new File(dir.getAbsolutePath(), "TEMP-" + file.getName());
        	
        	DataOutputStream os = new DataOutputStream(new FileOutputStream(intermediateFile));

			//NBTCompressedStreamTools.a(finalTag, fos);
        	finalTag.write(os);
        	
        	compressGZIP(intermediateFile, file);

        	intermediateFile.deleteOnExit();
        	
        	return file.getName();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return null;
	}
	
	//compression format Gzip
    public static void compressGZIP(File input, File output) throws IOException {
        try (GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(output))){
            try (FileInputStream in = new FileInputStream(input)){
                byte[] buffer = new byte[1024];
                int len;
                while((len=in.read(buffer)) != -1){
                    out.write(buffer, 0, len);
                }
            }
        }
    }*/
}

