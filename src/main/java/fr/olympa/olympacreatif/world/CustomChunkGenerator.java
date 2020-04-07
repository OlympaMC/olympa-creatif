package fr.olympa.olympacreatif.world;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.datas.Message;

public class CustomChunkGenerator extends ChunkGenerator {

	private OlympaCreatifMain plugin;
	private int plotXwidth = Integer.parseInt(Message.PARAM_PLOT_X_SIZE.getValue());
	private int plotZwidth = Integer.parseInt(Message.PARAM_PLOT_Z_SIZE.getValue());
	private int roadWidth = Integer.parseInt(Message.PARAM_ROAD_SIZE.getValue());
	private int worldLevel = Integer.parseInt(Message.PARAM_WORLD_LEVEL.getValue());
	
    public CustomChunkGenerator(OlympaCreatifMain plugin) {
		this.plugin = plugin;
	}

	@Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
    	SimplexOctaveGenerator generator = new SimplexOctaveGenerator(new Random(world.getSeed()), 8);
        ChunkData chunk = createChunkData(world);
        generator.setScale(0.005D);
        for (int x = 0; x < 16; x++)
            for (int z = 0; z < 16; z++) {
            	
            	//set biome plaine partout
            	for (int y = 0 ; y<= 255 ; y++)
            		biome.setBiome(x, y, z, Biome.PLAINS);
            	
            	//set couche basse
        		chunk.setBlock(x, 0, z, Material.BEDROCK);
        		
        		//set couches hautes
        		//si route, set stone
    			if ((Math.floorMod(chunkX*16+x, plotXwidth + roadWidth + 1) >= plotXwidth && 
    					Math.floorMod(chunkX*16+x, plotXwidth + roadWidth + 1) <= plotXwidth + roadWidth) || 
    					(Math.floorMod(chunkZ*16+z, plotZwidth + roadWidth + 1) >= plotZwidth && 
    					Math.floorMod(chunkZ*16+z, plotZwidth + roadWidth + 1) <= plotZwidth + roadWidth)) {
    				
    				//set stone pour les routes
            		for (int y = 1 ; y <= worldLevel ; y++) {
            			chunk.setBlock(x, y, z, Material.STONE);
            		}
    			}else { //si plot, set terre et herbe pour les plots
    				
            		for (int y = 1 ; y < worldLevel ; y++) {
            			chunk.setBlock(x, y, z, Material.DIRT);
            		}
            		chunk.setBlock(x, worldLevel, z, Material.GRASS_BLOCK);
    			}
    			
    			//si bord de plot, set demies dalles
        		if (((Math.floorMod(chunkX*16+x, plotXwidth + roadWidth + 1) == plotXwidth || 
        				Math.floorMod(chunkX*16+x, plotXwidth + roadWidth + 1) == plotXwidth + roadWidth) && 
    					!(Math.floorMod(chunkZ*16+z, plotZwidth + roadWidth + 1) > plotZwidth && 
    					Math.floorMod(chunkZ*16+z, plotZwidth + roadWidth + 1) < plotZwidth + roadWidth)) || 
        				((Math.floorMod(chunkZ*16+z, plotZwidth + roadWidth + 1) == plotZwidth || 
        				Math.floorMod(chunkZ*16+z, plotZwidth + roadWidth + 1) == plotZwidth + roadWidth) && 
    					!(Math.floorMod(chunkX*16+x, plotXwidth + roadWidth + 1) > plotXwidth && 
    					Math.floorMod(chunkX*16+x, plotXwidth + roadWidth + 1) < plotXwidth + roadWidth))) {
            		
        			chunk.setBlock(x, worldLevel, z, Material.GRANITE_SLAB);
        		}
        		
            }
        return chunk;
    }
}
