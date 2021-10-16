package fr.olympa.olympacreatif.perks;

import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.xxmicloxx.NoteBlockAPI.NoteBlockAPI;
import com.xxmicloxx.NoteBlockAPI.event.SongEndEvent;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;

import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.spigot.gui.OlympaGUI;
import fr.olympa.api.spigot.gui.templates.PagedView;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
import fr.olympa.olympacreatif.gui.IGui;
import fr.olympa.olympacreatif.gui.MainGui;
import fr.olympa.olympacreatif.gui.PlotParametersGui;
import fr.olympa.olympacreatif.plot.Plot;
import fr.olympa.olympacreatif.plot.PlotParamType;
import fr.olympa.olympacreatif.plot.PlotPerm;

public class MusicManager implements Listener {

	private static final List<Material> discs = Arrays.asList(
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
	);
	
	private OlympaCreatifMain plugin;
	private Map<String, Song> songsName = new HashMap<>();
	private Map<Song, Entry<String, ItemStack>> songs = new LinkedHashMap<>();
	
	private Map<UUID, RadioSongPlayer> radios = new HashMap<>();
	
	public MusicManager(OlympaCreatifMain plugin) {
		this.plugin = plugin;
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
		if (plugin.getServer().getPluginManager().getPlugin("NoteBlockAPI") == null) {
			plugin.getLogger().warning("NoteBlockAPI wasn't loaded successfully. Music perks may not work.");
			return;	
		}
		
		/*try {
			Field api = NoteBlockAPI.class.getDeclaredField("plugin");
			api.setAccessible(true);
			api.set(null, plugin.getServer().getPluginManager().getPlugin("NoteBlockAPI"));
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		
		//get all songs from config and parse them for later use
		File path = new File(plugin.getDataFolder() + "/songs/");
		
		if (!path.exists()) path.mkdir();
		
		List<File> list = new ArrayList<File>(Arrays.asList(path.listFiles()));
		list.sort((s1, s2) -> {
			return s1.getName().compareTo(s2.getName());
		});

		
		
		//store songs and their representative items
		for (int i = 0 ; i < list.size() ; i++) {
			try{
				Song s = NBSDecoder.parse(list.get(i));
				songsName.put(list.get(i).getName().replace(".nbs", "").toLowerCase(), s);
			}catch (Exception ex){
				plugin.getLogger().warning("Failed to load song " + list.get(i));
				ex.printStackTrace();
			}
		}
		
		int i = 0;
		
		for (Entry<String, Song> e : songsName.entrySet()) {
			i++;
			ItemStack it = ItemUtils.item(discs.get(ThreadLocalRandom.current().nextInt(discs.size())), "§7" + i + ". §d" + e.getKey());
			
			if (!e.getValue().getOriginalAuthor().equals(""))
				it = ItemUtils.loreAdd(it, "§7Musique par " + e.getValue().getOriginalAuthor());
			if (!e.getValue().getAuthor().equals(""))
				it = ItemUtils.loreAdd(it, "§7Transcription par " + e.getValue().getAuthor());
			
			songs.put(e.getValue(), new AbstractMap.SimpleEntry<>(e.getKey(), it));
		}
		
		plugin.getLogger().info("§aLoaded " + songsName.size() + " songs.");
	}

	/**
	 * Start the music with the defined name for the defined player
	 * @param p
	 * @param music
	 * 
	 * @return true if music started, false if the sound wasn't found
	 */
	public void startSong(Player p, String songName) {
		if (songsName.containsKey(songName))
			startSong(p, songsName.get(songName));
	}
	
	public void startSong(Player p, Song song) {
		stopSong(p);
		
		if (song == null) {
			try {
				throw new UnsupportedOperationException("Trying to play a null song on plot " + ((OlympaPlayerCreatif)AccountProviderAPI.getter().get(p.getUniqueId())).getCurrentPlot());
			}catch (UnsupportedOperationException ex) {
				ex.printStackTrace();
				return;
			}
		}

		/*plugin.getLogger().info("Starting song " + song.getTitle() + " for " + p.getName());
		plugin.getLogger().info("NoteblockAPI : " + Bukkit.getPluginManager().getPlugin("NoteBlockAPI"));*/
		
		RadioSongPlayer rsp = new RadioSongPlayer(song);
		rsp.setVolume((byte)80);
		rsp.setAutoDestroy(true);
		rsp.addPlayer(p);
		rsp.setPlaying(true);
		
		radios.put(p.getUniqueId(), rsp);
	}
	
	/**
	 * Stop music stream for defined player
	 * @param p
	 */
	public void stopSong(Player p) {
		RadioSongPlayer rsp = radios.remove(p.getUniqueId());
		if (rsp == null)
			return;
		rsp.destroy();
	}
	
	
	/**
	 * Open the song selection GUI to select a music for the specified plot
	 * @param p
	 * @param plot
	 */
	public void openGui(Player p, Plot plot) {			
		new MusicGui(plot, p).toGUI().create(p);
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		/*RadioSongPlayer radio = radios.remove(e.getPlayer().getUniqueId());
		if (radio != null)
			radio.destroy();*/
		NoteBlockAPI.stopPlaying(e.getPlayer());
	}
	
	@EventHandler
	public void onSongStop(SongEndEvent e) {
		e.getSongPlayer().getPlayerUUIDs().forEach(p -> startSong(Bukkit.getPlayer(p), e.getSongPlayer().getSong()));
	}

	public class MusicGui extends PagedView<Song> {
		
		private Plot plot;
		private Player p0;

		protected MusicGui(Plot plot, Player p0) {
			super(DyeColor.GREEN, new ArrayList<>(songs.keySet()));
			this.plot = plot;
			this.p0 = p0;
		}
		
		@Override
		public void init() {
			super.init();
			
			//set current selected music
			ItemStack it = ItemUtils.item(Material.JUKEBOX, "§eMusique sélectionnée : §caucune", "§7Tous les joueurs entrant sur", "§7la parcelle entendront cette musique !");
			
			if (!"".equals(plot.getParameters().getParameter(PlotParamType.SONG)))
				ItemUtils.name(it, "§eMusique sélectionnée : §d" + plot.getParameters().getParameter(PlotParamType.SONG));
			
			right.setItem(1, it);
			
			//create remove music item
			right.setItem(2, ItemUtils.item(Material.RED_WOOL, "§cSupprimer la musique actuelle"));
			
			//add back item
			right.setItem(4, IGui.getBackItem());
		}

		@Override
		public ItemStack getItemStack(Song object) {
			return songs.get(object).getValue();
		}

		@Override
		public void click(Song existing, Player p, ClickType click) {
			if (PlotPerm.DEFINE_MUSIC.has(plot, AccountProviderAPI.getter().get(p0.getUniqueId()))) {
				String selected = songs.get(existing).getKey();
				PlotParamType.SONG.setValue(plot, selected);
				ItemUtils.name(right.getItem(1), "§eMusique sélectionnée : §a" + selected);
				
				plot.getPlayers().forEach(player -> startSong(player, existing));
			}
		}
		
		@Override
		protected boolean onBarItemClick(Player p, ItemStack current, int barSlot, ClickType click) {
			if (barSlot == 2) {
				if (PlotPerm.DEFINE_MUSIC.has(plot, AccountProviderAPI.getter().get(p0.getUniqueId()))) {
					PlotParamType.SONG.setValue(plot, "");
					ItemUtils.name(right.getItem(1), "§eMusique sélectionnée : §caucune");
					plot.getPlayers().forEach(MusicManager.this::stopSong);
				}
			}else if (barSlot == 4) {
				new PlotParametersGui(MainGui.getMainGui(AccountProviderAPI.getter().get(p.getUniqueId()), plot)).create(p);
			}
			return true;
		}
		
		public OlympaGUI toGUI() {
			return super.toGUI("Musiques disponibles", 6);
		}
	}
}











