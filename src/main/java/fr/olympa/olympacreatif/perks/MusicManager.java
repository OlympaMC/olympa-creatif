package fr.olympa.olympacreatif.perks;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;

import fr.olympa.api.gui.templates.PagedGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotParamType;

public class MusicManager implements Listener {

	private static final List<Material> discs = new ArrayList<Material>(Arrays.asList(new Material[] {
			Material.MUSIC_DISC_11,
			Material.MUSIC_DISC_13,
			Material.MUSIC_DISC_BLOCKS,
			Material.MUSIC_DISC_CAT,
			Material.MUSIC_DISC_CHIRP,
			Material.MUSIC_DISC_FAR,
			Material.MUSIC_DISC_MALL,
			Material.MUSIC_DISC_MELLOHI,
			Material.MUSIC_DISC_STAL,
			Material.MUSIC_DISC_STRAD,
			Material.MUSIC_DISC_WAIT,
			Material.MUSIC_DISC_WARD
	}));
	
	private OlympaCreatifMain plugin;
	private Map<ItemStack, Song> songs = new LinkedHashMap<ItemStack, Song>();
	private Map<Player, RadioSongPlayer> radios = new HashMap<Player, RadioSongPlayer>();
	
	public MusicManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
		if (plugin.getServer().getPluginManager().getPlugin("NoteBlockAPI") == null) 
			return;
		
		//get all songs from config and parse them for later use
		File path = new File(plugin.getDataFolder() + "\\songs\\");
		
		if (!path.exists()) path.mkdir();
		
		List<File> list = new ArrayList<File>(Arrays.asList(path.listFiles()));
		list.sort((s1, s2) -> {
			return s1.getName().compareTo(s2.getName());
		});
		
		
		//store songs and their representative items
		for (int i = 0 ; i < list.size() ; i++) {
			Song song = NBSDecoder.parse(list.get(i));

			ItemStack it = ItemUtils.item(discs.get(plugin.random.nextInt(discs.size())), "§7" + i + ". §d" + song.getTitle());

			if (!song.getOriginalAuthor().equals(""))
				it = ItemUtils.loreAdd(it, "§7Musique par " + song.getOriginalAuthor());
			if (!song.getAuthor().equals(""))
				it = ItemUtils.loreAdd(it, "§7Transcription par " + song.getAuthor());
			
			songs.put(it, song);	
		}
	}

	/**
	 * Start the music with the defined name for the defined player
	 * @param p
	 * @param music
	 * 
	 * @return true if music started, false if the sound wasn't found
	 */
	public void startSong(Player p, String music) {
		
		songs.values().forEach(song -> {
			if (song.getTitle().equals(music)) {
				
				stopSong(p);
				
				RadioSongPlayer rsp = new RadioSongPlayer(song);
				rsp.addPlayer(p);
				rsp.setAutoDestroy(true);
				rsp.setPlaying(true);
				
				radios.put(p, rsp);
				
				return;
			}
		});
	}
	
	/**
	 * Stop music stream for defined player
	 * @param p
	 */
	private void stopSong(Player p) {
		RadioSongPlayer rsp = radios.get(p);
		if (rsp == null)
			return;
		
		rsp.removePlayer(p);
	}
	
	
	/**
	 * Open the song selection GUI to select a music for the specified plot
	 * @param p
	 * @param plot
	 */
	public void openGui(Player p, Plot plot) {
		if (plot != null) {
			List<ItemStack> list = new ArrayList<ItemStack>();
			songs.keySet().forEach(it -> list.add(it.clone()));
			
			new MusicGui(plugin, plot, list).create(p);	
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		radios.remove(e.getPlayer());
	}

	public class MusicGui extends PagedGUI<ItemStack> {
	
		private Plot plot;
		private List<ItemStack> items;
		
		protected MusicGui(OlympaCreatifMain plugin, Plot plot, List<ItemStack> items) {
			super("Musiques disponibles", DyeColor.GREEN, items, 6);
			this.plot = plot;
			this.items = items;
		}

		@Override
		public ItemStack getItemStack(ItemStack object) {
			return object;
		}
	
		@Override
		public void click(ItemStack existing, Player p) {
			Song song = songs.get(existing);
			
			if (song == null)
				return;
			
			PlotParamType.SONG.setValue(plot, song.getTitle());
			
			items.forEach(it -> it = ItemUtils.removeEnchant(it, Enchantment.DURABILITY));
			existing = ItemUtils.addEnchant(existing, Enchantment.DURABILITY, 1);
		}
	}
}











