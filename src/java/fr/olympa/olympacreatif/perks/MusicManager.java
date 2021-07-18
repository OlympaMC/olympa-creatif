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
import java.util.function.Consumer;

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

import fr.olympa.api.spigot.gui.templates.PagedGUI;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.olympacreatif.OlympaCreatifMain;
import fr.olympa.olympacreatif.data.OlympaPlayerCreatif;
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
	private Map<String, Song> songsName = new HashMap<String, Song>();
	private Map<ItemStack, Entry<String, Song>> songsItem = new LinkedHashMap<ItemStack, Map.Entry<String,Song>>();
	
	private Map<UUID, RadioSongPlayer> radios = new HashMap<UUID, RadioSongPlayer>();
	
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
			
			songsItem.put(it, new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue()));
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
		new MusicGui(plugin, plot, p, plot == null ? new HashMap<ItemStack, Entry<String, Song>>() : songsItem).create(p);	
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

	public class MusicGui extends PagedGUI<ItemStack> {

		private Map<ItemStack, Consumer<Player>> items = new HashMap<ItemStack, Consumer<Player>>();
		
		protected MusicGui(OlympaCreatifMain plugin, Plot plot, Player p0, Map<ItemStack, Entry<String, Song>> songsMap) {
			super("Musiques disponibles", DyeColor.GREEN, new ArrayList<ItemStack>(songsMap.keySet()), 6);

			if (PlotPerm.DEFINE_MUSIC.has(plot, AccountProviderAPI.getter().get(p0.getUniqueId())))
				songsMap.forEach((it, songEntry) -> items.put(it, p -> {					
					PlotParamType.SONG.setValue(plot, songEntry.getKey());
					ItemUtils.name(getInventory().getItem(17), "§eMusique sélectionnée : §a" + songEntry.getKey());
					
					plot.getPlayers().forEach(player -> startSong(player, songEntry.getValue()));
				}));
			
			//set current selected music
			ItemStack it = ItemUtils.item(Material.JUKEBOX, "§eMusique sélectionnée : §caucune", "§7Tous les joueurs entrant sur", "§7la parcelle entendront cette musique !");
			
			if (!"".equals(plot.getParameters().getParameter(PlotParamType.SONG)))
				ItemUtils.name(it, "§eMusique sélectionnée : §d" + plot.getParameters().getParameter(PlotParamType.SONG));
			
			getInventory().setItem(17, it);
			
			//create remove music item
			getInventory().setItem(26, it = ItemUtils.item(Material.RED_WOOL, "§cSupprimer la musique actuelle"));
			
			if (PlotPerm.DEFINE_MUSIC.has(plot, AccountProviderAPI.getter().get(p0.getUniqueId())))
				items.put(it, p -> {
					PlotParamType.SONG.setValue(plot, "");
					getInventory().setItem(17, ItemUtils.name(getInventory().getItem(17), "§eMusique sélectionnée : §caucune"));
					plot.getPlayers().forEach(pp -> stopSong(pp));
				});
			
			//add back item
			getInventory().setItem(44, it = IGui.getBackItem());
			items.put(it, p -> {
				new PlotParametersGui(MainGui.getMainGui(AccountProviderAPI.getter().get(p.getUniqueId()), plot)).create(p);
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











