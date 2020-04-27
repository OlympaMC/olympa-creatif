package fr.olympa.olympacreatif.perks;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.Message;
import fr.olympa.olympacreatif.plot.Plot;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.NBTTagList;
import net.minecraft.server.v1_15_R1.TileEntity;
import net.minecraft.server.v1_15_R1.World;

public class SchematicCreator {

	  private OlympaCreatifMain plugin;
	  private int plotXsize;
	  private int plotZsize;
	  
	public SchematicCreator(OlympaCreatifMain plugin) {
	    this.plugin = plugin;
	    plotXsize = Integer.valueOf(Message.PARAM_PLOT_X_SIZE.getValue());
	    plotZsize = Integer.valueOf(Message.PARAM_PLOT_Z_SIZE.getValue());
	}
	    
	public String export(Plot plot) {
		String fileName = plot.getMembers().getOwner().getName() + "-" + plot.getId().getAsString();
		
		return export(fileName, plot.getId().getLocation().getBlockX(), 1, plot.getId().getLocation().getBlockZ(), 
				plot.getId().getLocation().getBlockX() + plotXsize - 1, 255, plot.getId().getLocation().getBlockZ() + plotZsize - 1);
	}
	

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
    }
}


