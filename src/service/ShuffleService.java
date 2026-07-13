package src.service;

import src.datastructure.HistoryStack;
import src.model.Song;

import java.util.List;
import java.util.Random;

public class ShuffleService {

    // -------------------------------------------------------------------------
    // Fields (theo UML)
    // -------------------------------------------------------------------------

    private Random random;
    private HistoryStack historyStack;
    private Song[] songs; // luu ket qua shuffle gan nhat

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Constructor chinh -- dung trong production.
     *
     * @param historyStack stack lich su cua NavigationService, se bi clear sau
     *                     shuffle
     */
    public ShuffleService(HistoryStack historyStack) {
        if (historyStack == null) {
            throw new IllegalArgumentException("historyStack must not be null");
        }
        this.historyStack = historyStack;
        this.random = new Random();
    }

    /**
     * Constructor voi seed -- dung cho unit test (ket qua tai san xuat duoc).
     * 
     * @param historyStack stack lich su
     * @param seed         gia tri seed de Random co ket qua co dinh khi test
     */
    public ShuffleService(HistoryStack historyStack, long seed) {
        if (historyStack == null) {
            throw new IllegalArgumentException("historyStack must not be null");
        }
        this.historyStack = historyStack;
        this.random = new Random(seed);
    }

    // -------------------------------------------------------------------------
    // Public methods
    // -------------------------------------------------------------------------

    public Song[] getShuffledPlaylist(PlaylistManager manager) {
        if (manager == null || manager.isEmpty()) {
            songs = new Song[0];
            return songs;
        }

        List<Song> songList = manager.getAllSongs();
        songs = songList.toArray(new Song[0]);

        if (songs.length > 1) {
            fisherYatesShuffle(songs);
        }

        historyStack.clear();

        return songs;
    }

    public void fisherYatesShuffle(Song[] songs) {
        if (songs == null || songs.length <= 1) {
            return;
        }
        int n = songs.length;
        for (int i = n - 1; i >= 1; i--) {
            int j = random.nextInt(i + 1);
            Song temp = songs[i];
            songs[i] = songs[j];
            songs[j] = temp;
        }
    }

    // -------------------------------------------------------------------------
    // Getter
    // -------------------------------------------------------------------------

    public Song[] getSongs() {
        return songs;
    }
}