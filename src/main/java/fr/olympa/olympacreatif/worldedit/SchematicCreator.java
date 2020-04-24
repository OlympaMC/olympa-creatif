package fr.olympa.olympacreatif.worldedit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import fr.olympa.olympacreatif.OlympaCreatifMain;

public class SchematicCreator {

	  private OlympaCreatifMain plugin;
	  private String name;
//	  private LoadType loadType;
	  private int radius;
	  private int xDiff;
	  
	public SchematicCreator(OlympaCreatifMain plugin, String name, int radius, int xDiff, int yDiff, int zDiff, List<String> blocks) {
	    this.plugin = plugin;	    
	    this.name = name;
	    //this.loadType = loadType;
	    this.radius = radius;
	    this.xDiff = xDiff;
	    this.yDiff = yDiff;
	    this.zDiff = zDiff;
	    this.blocks = blocks;
	    
	    File dir = new File(plugin.getDataFolder() + "/schematics");
	    
	    if (!dir.exists()) {
	      dir.mkdir();
	    }
	    this.sf = new File(dir.getAbsolutePath(), String.valueOf(name) + ".schematic");
	    
	    if (!this.sf.exists()) {
	      try {
	        this.sf.createNewFile();
	      } catch (Exception e) {}
	    }


	    
	    this.s = YamlConfiguration.loadConfiguration(this.sf);
	  }
	
	
	  private int yDiff; 
	  private int zDiff; 
	  private List<String> blocks; 
	  private FileConfiguration s; 
	  private File sf;
	  
	  public void create() {
	    //this.plugin.getInitializer().getFilesManager().getSchematicFile().addSchematic(this.name);
	    
	    getSchematic().set("Name", this.name);
	    getSchematic().set("Load", "RADIUS");
	    getSchematic().set("Radius", Integer.valueOf(this.radius));
	    getSchematic().set("Diff.x", Integer.valueOf(this.xDiff));
	    getSchematic().set("Diff.y", Integer.valueOf(this.yDiff));
	    getSchematic().set("Diff.z", Integer.valueOf(this.zDiff));
	    getSchematic().set("Blocks", this.blocks);
	    
	    saveSchematic();
	  }

	  
	  public FileConfiguration getSchematic() { return this.s; }

	  
	  public void saveSchematic() {
			try {
				this.s.save(this.sf);
			} catch (Exception e) {}
	  }



	  
	  public void reloadSchematic() {
		  this.s = YamlConfiguration.loadConfiguration(this.sf); 
	  }
	  
	  
	  public static void createSchematic(OlympaCreatifMain plugin, String name, int radius, Location center) throws Exception {		    
		    List<String> blocks = new ArrayList<String>();
		    
		    Location point1 = new Location(center.getWorld(), center.getX() - radius, center.getY() - radius, center.getZ() - radius);
		    Location point2 = new Location(center.getWorld(), center.getX() + radius, center.getY() + radius, center.getZ() + radius);
		    
		    int x1 = (int)point1.getX();
		    int x2 = (int)point2.getX();
		    
		    int y1 = (int)point1.getY();
		    int y2 = (int)point2.getY();
		    
		    int z1 = (int)point1.getZ();
		    int z2 = (int)point2.getZ();
		    
		    for (int x = x1; x <= x2; x++) {
		      for (int y = y1; y <= y2; y++) {
		        for (int z = z1; z <= z2; z++) {
		          Block block = point1.getWorld().getBlockAt(x, y, z);
		          
		          blocks.add(block.getType().toString() + " " + block.getData());
		        } 
		      } 
		    } 
		    
		    SchematicCreator schematic = new SchematicCreator(plugin, name, radius, 2147483647, 2147483647, 2147483647, blocks);
		    schematic.create();
		  }
}


