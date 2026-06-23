package src.service;

import src.datastructure.HistoryStack;
import src.model.Song;

import java.util.List;
import java.util.Random;

/**
 * ShuffleService — TV2
 *
 * Chuc nang: xao tron danh sach bai hat bang thuat toan Fisher-Yates.
 * Sau khi shuffle -> xoa lich su (historyStack.clear()) vi thu tu cu khong con
 * y nghia.
 *
 * DSA: Fisher-Yates shuffle O(n), uniform distribution.
 */
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
    // Public methods (theo UML)
    // -------------------------------------------------------------------------

    /**
     * Lay danh sach bai hat da xao tron tu PlaylistManager.
     *
     * Luong xu ly:
     * 1. Lay toan bo Song tu manager.getAllSongs()
     * 2. Chuyen sang Song[] (mang phu, khong thay doi CDLL goc)
     * 3. Fisher-Yates shuffle in-place
     * 4. historyStack.clear() -- thu tu cu khong con y nghia
     * 5. Tra ve Song[] da xao tron
     *
     * @param manager PlaylistManager cua TV1
     * @return Song[] da duoc xao tron, mang rong neu playlist trong
     */
    public Song[] getShuffledPlaylist(PlaylistManager manager) {
        if (manager == null || manager.isEmpty()) {
            songs = new Song[0];
            return songs;
        }

        // Lay danh sach tu TV1 qua getAllSongs() -> List<Song>
        List<Song> songList = manager.getAllSongs();
        songs = songList.toArray(new Song[0]);

        // Shuffle neu co it nhat 2 bai (1 bai khong can xao tron)
        if (songs.length > 1) {
            fisherYatesShuffle(songs);
        }

        // Xoa lich su -- sau shuffle, lich su cu (thu tu phat truoc do) mat y nghia
        historyStack.clear();

        return songs;
    }

    /**
     * Fisher-Yates shuffle -- xao tron in-place tren mang dau vao.
     *
     * Thuat toan:
     * for i tu (n-1) xuong 1:
     * j = random trong [0, i] <- j co the bang i (no-swap)
     * swap(songs[i], songs[j])
     *
     * Do phuc tap: O(n) thoi gian, O(1) bo nho phu.
     * Dam bao uniform distribution: moi hoan vi xuat hien voi xac suat 1/n!
     *
     * @param songs mang Song can xao tron (chinh sua truc tiep)
     */
    public void fisherYatesShuffle(Song[] songs) {
        if (songs == null || songs.length <= 1) {
            return;
        }
        int n = songs.length;
        for (int i = n - 1; i >= 1; i--) {
            // j thuoc [0, i] -- index 0 luon co kha nang duoc chon lam swap target
            int j = random.nextInt(i + 1);
            Song temp = songs[i];
            songs[i] = songs[j];
            songs[j] = temp;
        }
    }

    // -------------------------------------------------------------------------
    // Getter
    // -------------------------------------------------------------------------

    /**
     * Tra ve ket qua shuffle gan nhat.
     * Null neu chua goi getShuffledPlaylist() lan nao.
     */
    public Song[] getSongs() {
        return songs;
    }
}