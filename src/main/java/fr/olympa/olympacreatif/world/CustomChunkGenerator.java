package fr.olympa.olympacreatif.world;

import java.io.File;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;

import fr.olympa.api.utils.spigot.Schematic;
import fr.olympa.olympacreatif.OlympaCreatifMain;

@Deprecated //this class is now used in an external standalone plugin to generate map
public class CustomChunkGenerator extends ChunkGenerator {

	private OlympaCreatifMain plugin;

	Schematic roadXschem = null;
	Schematic roadZschem = null;
	
    public CustomChunkGenerator(OlympaCreatifMain plugin) {
		this.plugin = plugin;

		File fileX = new File(plugin.getDataFolder() + "/roadX.schem");
		File fileZ = new File(plugin.getDataFolder() + "/roadZ.schem");
		
		try {
			roadXschem = Schematic.load(fileX);
			roadZschem = Schematic.load(fileZ);
			Bukkit.getLogger().info("Les fichier roadX.schem et roadZ.schem se sont chargés correctement.");
		} catch (Exception e) {
			e.printStackTrace();
			Bukkit.getLogger().severe("Attention : L'un des fichiers roadX|Z.schem n'a pas été trouvé. Veuillez le renseigner pour créer des routes personnalisées.");
		}
	}

	@Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {

        ChunkData chunk = createChunkData(world);

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

    			if ((Math.floorMod(chunkX*16+x, WorldManager.plotSize + WorldManager.roadSize) >= WorldManager.plotSize && 
    					Math.floorMod(chunkX*16+x, WorldManager.plotSize + WorldManager.roadSize) < WorldManager.plotSize + WorldManager.roadSize))
    				onZroad = true;
    			
    			if ((Math.floorMod(chunkZ*16+z, WorldManager.plotSize + WorldManager.roadSize) >= WorldManager.plotSize && 
    					Math.floorMod(chunkZ*16+z, WorldManager.plotSize + WorldManager.roadSize) < WorldManager.plotSize + WorldManager.roadSize))
    				onXroad = true;
    			
    			//placement blocks en dessous de la route
    			if (onXroad || onZroad)
    				for (int y = 1 ; y < WorldManager.worldLevel - 1 ; y++)
    					chunk.setBlock(x, y, z, Bukkit.createBlockData(Material.STONE));
    			
    			if (onXroad) {
    				if (roadXschem != null) {
        				//placement blocs de route selon sens schematic
        				int xRoadBlockIndex = Math.floorMod(chunkX*16+x + 1, roadXschem.width);
        				int zRoadBlockIndex = Math.floorMod(Math.floorMod(chunkZ*16+z, WorldManager.plotSize + WorldManager.roadSize) - WorldManager.plotSize, roadXschem.length);
        				
        				for (int y2 = 0 ; y2 < roadXschem.height ; y2++) {
        					Schematic.EmptyBuildBlock block = roadXschem.blocks[xRoadBlockIndex][y2][zRoadBlockIndex];
        					
        					if (block instanceof Schematic.DataBuildBlock)
        						chunk.setBlock(x, y2 + WorldManager.worldLevel - 1, z, ((Schematic.DataBuildBlock)block).data);
        					
        				}
    				}else
						chunk.setBlock(x, WorldManager.worldLevel, z, Bukkit.createBlockData(Material.SAND));
    					
    					
    					
    			}else if (onZroad) {
    				if (roadZschem != null) {
        				//placement blocs de route selon sens schematic
        				int xRoadBlockIndex = Math.floorMod(Math.floorMod(chunkX*16+x, WorldManager.plotSize + WorldManager.roadSize) - WorldManager.plotSize, roadXschem.width);
        				int zRoadBlockIndex = Math.floorMod(chunkZ*16+z + 1, roadZschem.length);
        				
        				for (int y2 = 0 ; y2 < roadZschem.height ; y2++) {
        					Schematic.EmptyBuildBlock block = roadZschem.blocks[xRoadBlockIndex][y2][zRoadBlockIndex];
        					
        					if (block != null && block instanceof Schematic.DataBuildBlock) 
        						chunk.setBlock(x, y2 + WorldManager.worldLevel - 1, z, ((Schematic.DataBuildBlock)block).data);	
         					
        				}
    				}else
						chunk.setBlock(x, WorldManager.worldLevel, z, Bukkit.createBlockData(Material.SAND));
    				
    				
    			}else {
    				//placement blocs d'herbe du plot
            		for (int y = 1 ; y < WorldManager.worldLevel ; y++) {
            			chunk.setBlock(x, y, z, Material.DIRT);
            		}
            		chunk.setBlock(x, WorldManager.worldLevel, z, Material.GRASS_BLOCK);
    			}
    			
    			
    			/*
    			if ((Math.floorMod(chunkX*16+x, WorldManager.plotSize + WorldManager.roadSize) >= WorldManager.plotSize && 
    					Math.floorMod(chunkX*16+x, WorldManager.plotSize + WorldManager.roadSize) < WorldManager.plotSize + WorldManager.roadSize) || 
    					(Math.floorMod(chunkZ*16+z, WorldManager.plotSize + WorldManager.roadSize) >= WorldManager.plotSize && 
    					Math.floorMod(chunkZ*16+z, WorldManager.plotSize + WorldManager.roadSize) < WorldManager.plotSize + WorldManager.roadSize)) {
    				
    				//set stone pour les routes
    				//si un schematic est fourni
    				if (roadSchem == null)
    					for (int y = 1 ; y <= WorldManager.worldLevel ; y++) 
    						chunk.setBlock(x, y, z, Material.STONE);
    				
            		
    			}else { //si plot, set terre et herbe pour les plots
    				
            		for (int y = 1 ; y < WorldManager.worldLevel ; y++) {
            			chunk.setBlock(x, y, z, Material.DIRT);
            		}
            		chunk.setBlock(x, WorldManager.worldLevel, z, Material.GRASS_BLOCK);
    			
    			}*/
    			
    			//si bord de plot, set demies dalles
    			
    			/*
        		if (((Math.floorMod(chunkX*16+x, WorldManager.plotSize + WorldManager.roadSize) == WorldManager.plotSize || 
        				Math.floorMod(chunkX*16+x, WorldManager.plotSize + WorldManager.roadSize) == WorldManager.plotSize + WorldManager.roadSize - 1) && 
    					!(Math.floorMod(chunkZ*16+z, WorldManager.plotSize + WorldManager.roadSize) > WorldManager.plotSize && 
    					Math.floorMod(chunkZ*16+z, WorldManager.plotSize + WorldManager.roadSize) < WorldManager.plotSize + WorldManager.roadSize)) || 
        				((Math.floorMod(chunkZ*16+z, WorldManager.plotSize + WorldManager.roadSize) == WorldManager.plotSize || 
        				Math.floorMod(chunkZ*16+z, WorldManager.plotSize + WorldManager.roadSize) == WorldManager.plotSize + WorldManager.roadSize - 1) && 
    					!(Math.floorMod(chunkX*16+x, WorldManager.plotSize + WorldManager.roadSize) > WorldManager.plotSize && 
    					Math.floorMod(chunkX*16+x, WorldManager.plotSize + WorldManager.roadSize) < WorldManager.plotSize + WorldManager.roadSize)) ||
    					(Math.floorMod(chunkX*16+x, WorldManager.plotSize + WorldManager.roadSize) == WorldManager.plotSize + WorldManager.roadSize - 1 && 
    					Math.floorMod(chunkZ*16+z, WorldManager.plotSize + WorldManager.roadSize) == WorldManager.plotSize + WorldManager.roadSize - 1)) {
            		
        			chunk.setBlock(x, WorldManager.worldLevel+1, z, Material.GRANITE_SLAB);
        		}
        		*/
        		
            }
        return chunk;
    }
}
