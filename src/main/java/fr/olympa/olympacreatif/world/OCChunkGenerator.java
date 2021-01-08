package fr.olympa.olympacreatif.world;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OCparam;

public class OCChunkGenerator extends ChunkGenerator {
	
	public int plotSize;
	public int roadSize;
	
	public int worldLevel;
	
	Schematic roadXschem = null;
	Schematic roadZschem = null;
	
    public OCChunkGenerator(OlympaCreatifMain plugin) {
    	plotSize = OCparam.PLOT_SIZE.get();
    	roadSize = WorldManager.roadSize;
    	worldLevel = WorldManager.worldLevel;
    	
		try {
			File fileX = File.createTempFile("creatif_schematic_X", ".schem");
			File fileZ = File.createTempFile("creatif_schematic_Z", ".schem");

			copyResource("schematics/roadX.schem", fileX.getAbsolutePath());
			copyResource("schematics/roadZ.schem", fileZ.getAbsolutePath());
			
			roadXschem = Schematic.load(new FileInputStream(fileX));
			roadZschem = Schematic.load(new FileInputStream(fileZ));
			plugin.getLogger().info("Les fichier roadX.schem et roadZ.schem se sont chargés correctement.");
		} catch (IOException e) {
			plugin.getLogger().severe("Attention : L'un des fichiers road<X|Z>.schem n'a pas été trouvé dans le dossier 'resources'. Veuillez le(s) renseigner pour créer des routes personnalisées.");
			e.printStackTrace();
		}
	}
    
    
	public void copyResource(String res, String dest) throws IOException {
        InputStream src = getClass().getClassLoader().getResourceAsStream(res);        
        Files.copy(src, Paths.get(dest), StandardCopyOption.REPLACE_EXISTING);
    }
    
	@Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {

        ChunkData chunk = createChunkData(world);

        int worldX = chunkX * 16;
        int worldZ = chunkZ * 16;
        
        for (int x = 0; x < 16; x++)
            for (int z = 0; z < 16; z++) {
            	
            	//set biome plaine partout
            	for (int y = 0 ; y<= 255 ; y++)
            		biome.setBiome(x, y, z, Biome.PLAINS);
            	
            	//set couche basse
        		chunk.setBlock(x, 0, z, Material.BEDROCK);

        		//set couches hautes
        		//si route, set stone
        		boolean onXroad = false;
        		boolean onZroad = false;

    			if ((Math.floorMod(worldX + x, plotSize + roadSize) >= plotSize && 
    					Math.floorMod(worldX + x, plotSize + roadSize) < plotSize + roadSize))
    				onZroad = true;
    			
    			if ((Math.floorMod(worldZ + z, plotSize + roadSize) >= plotSize && 
    					Math.floorMod(worldZ + z, plotSize + roadSize) < plotSize + roadSize))
    				onXroad = true;
    			
    			//placement blocks en dessous de la route
    			if (onXroad || onZroad)
    				for (int y = 1 ; y < worldLevel - 1 ; y++)
    					chunk.setBlock(x, y, z, Bukkit.createBlockData(Material.STONE));
    			
    			if (onXroad) {
    				if (roadXschem != null) {
        				//placement blocs de route selon sens schematic
        				int xRoadBlockIndex = Math.floorMod(worldX + x + 1, roadXschem.width);
        				int zRoadBlockIndex = Math.floorMod(Math.floorMod(worldZ + z, plotSize + roadSize) - plotSize, roadXschem.length);
        				
        				for (int y2 = 0 ; y2 < roadXschem.height ; y2++) {
        					Schematic.EmptyBuildBlock block = roadXschem.blocks[xRoadBlockIndex][y2][zRoadBlockIndex];
        					
        					if (block instanceof Schematic.DataBuildBlock)
        						chunk.setBlock(x, y2 + worldLevel - 1, z, ((Schematic.DataBuildBlock)block).data);
        					
        				}
    				}else
						chunk.setBlock(x, worldLevel, z, Bukkit.createBlockData(Material.SAND));
    					
    					
    					
    			}else if (onZroad) {
    				if (roadZschem != null) {
        				//placement blocs de route selon sens schematic
        				int xRoadBlockIndex = Math.floorMod(Math.floorMod(chunkX*16+x, plotSize + roadSize) - plotSize, roadXschem.width);
        				int zRoadBlockIndex = Math.floorMod(chunkZ*16+z + 1, roadZschem.length);
        				
        				for (int y2 = 0 ; y2 < roadZschem.height ; y2++) {
        					Schematic.EmptyBuildBlock block = roadZschem.blocks[xRoadBlockIndex][y2][zRoadBlockIndex];
        					
        					if (block != null && block instanceof Schematic.DataBuildBlock) 
        						chunk.setBlock(x, y2 + worldLevel - 1, z, ((Schematic.DataBuildBlock)block).data);	
         					
        				}
    				}else
						chunk.setBlock(x, worldLevel, z, Bukkit.createBlockData(Material.SAND));
    				
    				
    			}else {
    				//placement blocs d'herbe du plot
            		for (int y = 1 ; y < worldLevel ; y++) {
            			chunk.setBlock(x, y, z, Material.DIRT);
            		}
            		chunk.setBlock(x, worldLevel, z, Material.GRASS_BLOCK);
    			}
        		
            }
        return chunk;
    }
}
