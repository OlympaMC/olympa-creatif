package fr.olympa.olympacreatif.perks;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.xxmicloxx.NoteBlockAPI.event.SongNextEvent;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;

import fr.olympa.api.gui.templates.PagedGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.gui.IGui;
import fr.olympa.olympacreatif.gui.MainGui;
import fr.olympa.olympacreatif.gui.PlotParametersGui;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotParamType;
import fr.olympa.olympacreatif.plot.PlotPerm;

public class MusicManager implements Listener {

	private static final List<Material> discs = new ArrayList<Material>(Arrays.asList(new Material[] {
			Material.MUSIC_DISC_BLOCKS,
			Material.MUSIC_DISC_CAT,
			Material.MUSIC_DISC_CHIRP,
			Material.MUSIC_DISC_FAR,
			Material.MUSIC_DISC_MALL,
			Material.MUSIC_DISC_MELLOHI,
			Material.MUSIC_DISC_STAL,
			Material.MUSIC_DISC_WAIT,
			Material.MUSIC_DISC_WARD,
			Material.MUSIC_DISC_STRAD
	}));
	
	private OlympaCreatifMain plugin;
	private Map<String, Song> songs = new HashMap<String, Song>();
	private Map<Player, RadioSongPlayer> radios = new HashMap<Player, RadioSongPlayer>();

	private int i = 0;
	
	public MusicManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
		if (plugin.getServer().getPluginManager().getPlugin("NoteBlockAPI") == null) {
			Bukkit.getLogger().warning("NoteBlockAPI wasn't loaded successfully. Music perks may not work.");
			return;	
		}
		
		//get all songs from config and parse them for later use
		File path = new File(plugin.getDataFolder() + "/songs/");
		
		if (!path.exists()) path.mkdir();
		
		List<File> list = new ArrayList<File>(Arrays.asList(path.listFiles()));
		list.sort((s1, s2) -> {
			return s1.getName().compareTo(s2.getName());
		});
		
		
		//store songs and their representative items
		for (int i = 0 ; i < list.size() ; i++) {
			String str = list.get(i).getName().replace(".nbs", "");
			songs.put(str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase(), NBSDecoder.parse(list.get(i)));
		}
		
		Bukkit.getLogger().log(Level.INFO, "§aLoaded " + songs.size() + " songs.");
	}

	/**
	 * Start the music with the defined name for the defined player
	 * @param p
	 * @param music
	 * 
	 * @return true if music started, false if the sound wasn't found
	 */
	public void startSong(Player p, String music) {
		stopSong(p);
		
		Song song = songs.get(music);
		
		if (song == null)
			return;
		
		RadioSongPlayer rsp = new RadioSongPlayer(song);
		rsp.setVolume((byte)20);
		rsp.setAutoDestroy(true);
		rsp.setPlaying(true);
		
		radios.put(p, rsp);
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
			Map<ItemStack, Song> songsMap = new LinkedHashMap<ItemStack, Song>();
			
			i = 0;
			
			songs.forEach((songName, song) -> {
				i++;
				ItemStack it = ItemUtils.item(discs.get(plugin.random.nextInt(discs.size())), "§7" + i + ". §d" + songName);
				
				if (!song.getOriginalAuthor().equals(""))
					it = ItemUtils.loreAdd(it, "§7Musique par " + song.getOriginalAuthor());
				if (!song.getAuthor().equals(""))
					it = ItemUtils.loreAdd(it, "§7Transcription par " + song.getAuthor());
				
				songsMap.put(it, song);
			});
			
			new MusicGui(plugin, plot, p, songsMap).create(p);	
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		RadioSongPlayer radio = radios.remove(e.getPlayer());
		if (radio != null)
			radio.destroy();
	}

	public class MusicGui extends PagedGUI<ItemStack> {
	
		private Map<ItemStack, Consumer<Player>> items = new HashMap<ItemStack, Consumer<Player>>();
		
		protected MusicGui(OlympaCreatifMain plugin, Plot plot, Player p0, Map<ItemStack, Song> songsMap) {
			super("Musiques disponibles", DyeColor.GREEN, new ArrayList<ItemStack>(songsMap.keySet()), 6);
			//this.plot = plot;

			//building music discs
			if (PlotPerm.DEFINE_MUSIC.has(plot, AccountProvider.get(p0.getUniqueId())))
				songsMap.forEach((it, song) -> items.put(it, p -> {
					String songName = it.getItemMeta().getDisplayName().split("\\. §d")[1];
					
					PlotParamType.SONG.setValue(plot, songName);
					ItemUtils.name(getInventory().getItem(17), "§eMusique sélectionnée : §a" + songName);
					
					plot.getPlayers().forEach(player -> startSong(player, songName));
				}));
			
			//set current selected music
			ItemStack it = ItemUtils.item(Material.JUKEBOX, "§eMusique sélectionnée : §caucune", "§7Tous les joueurs entrant sur", "§7la parcelle entendront cette musique !");
			
			if (!"".equals(plot.getParameters().getParameter(PlotParamType.SONG)))
				ItemUtils.name(it, "§eMusique sélectionnée : §d" + plot.getParameters().getParameter(PlotParamType.SONG));
			
			getInventory().setItem(17, it);
			
			//create remove music item
			getInventory().setItem(26, it = ItemUtils.item(Material.RED_WOOL, "§cSupprimer la musique actuelle"));
			
			if (PlotPerm.DEFINE_MUSIC.has(plot, AccountProvider.get(p0.getUniqueId())))
				items.put(it, p -> {
					PlotParamType.SONG.setValue(plot, "");
					getInventory().setItem(17, ItemUtils.name(getInventory().getItem(17), "§eMusique sélectionnée : §caucune"));
					plot.getPlayers().forEach(pp -> stopSong(pp));
				});
			
			//add back item
			getInventory().setItem(44, it = IGui.getBackItem());
			items.put(it, p -> {
				new PlotParametersGui(MainGui.getMainGui(AccountProvider.get(p.getUniqueId()), plot)).create(p);
			});
		}

		@Override
		public ItemStack getItemStack(ItemStack object) {
			return object;
		}

		@Override
		public void click(ItemStack existing, Player p, ClickType click) {
		}

		@Override
		public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
			super.onClick(p, current, slot, click);

			if (items.get(current) != null)
				items.get(current).accept(p);
			
			return true;
		}
	}
}











