package src.service;

// NOTE: TV1 dung package 'src' (chu thuong). TV2 SESSION_CONTEXT ghi 'Src' (chu hoa).
// File nay theo convention cua TV1. Toan nhom can thong nhat truoc khi submit.

import src.datastructure.CircularLinkedList;
import src.datastructure.HistoryStack;
import src.model.Node;
import src.model.Song;

/**
 * NavigationService -- TV2
 *
 * Dieu phoi toan bo logic phat nhac:
 * - playNext() : push bai hien tai vao lich su, chuyen sang bai ke
 * - playPrevious() : pop tu lich su, quay lai bai truoc
 * - shuffle() : uy quyen cho ShuffleService (Fisher-Yates)
 * - RepeatMode : OFF / ONE / ALL
 *
 * TV4 chi goi playNext() va playPrevious(), khong biet logic ben trong.
 */
public class NavigationService {

    // -------------------------------------------------------------------------
    // Inner enum RepeatMode (gop vao day, khong can file RepeatMode.java rieng)
    // -------------------------------------------------------------------------

    public enum RepeatMode {
        /** Phat mot lan, dung o bai cuoi (khong vong lai). */
        OFF,
        /** Lap lai bai hien tai lien tuc. */
        ONE,
        /** Lap lai toan bo playlist (hanh vi mac dinh cua CDLL). */
        ALL
    }

    // -------------------------------------------------------------------------
    // Fields (theo UML)
    // -------------------------------------------------------------------------

    private CircularLinkedList playlist;
    private HistoryStack historyStack;
    private Song currentSong;
    private RepeatMode repeatMode;

    // -------------------------------------------------------------------------
    // Fields (internal -- khong co trong UML, can thiet cho implementation)
    // -------------------------------------------------------------------------

    /** Node dang duoc chon trong CDLL -- dung de goi getNextNode() cua TV1. */
    private Node currentNode;

    /** Giu tham chieu PlaylistManager de goi getAllSongs() khi shuffle. */
    private PlaylistManager playlistManager;

    /** Uy quyen shuffle cho ShuffleService (Fisher-Yates). */
    private ShuffleService shuffleService;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Khoi tao NavigationService.
     *
     * Nhan PlaylistManager thay vi thang CircularLinkedList vi:
     * - shuffle() can goi manager.getAllSongs() de lay Song[]
     * - CLL duoc lay tu manager.getPlaylist() -> luu vao this.playlist
     *
     * @param manager      PlaylistManager cua TV1 (chua CircularLinkedList)
     * @param historyStack HistoryStack cua TV2 (da khoi tao san)
     */
    public NavigationService(PlaylistManager manager, HistoryStack historyStack) {
        if (manager == null) {
            throw new IllegalArgumentException("PlaylistManager must not be null");
        }
        if (historyStack == null) {
            throw new IllegalArgumentException("HistoryStack must not be null");
        }

        this.playlistManager = manager;
        this.playlist = manager.getPlaylist();
        this.historyStack = historyStack;
        this.shuffleService = new ShuffleService(historyStack);
        this.repeatMode = RepeatMode.OFF;

        // Dat bai dau tien lam currentSong (neu playlist khong trong)
        this.currentNode = playlist.isEmpty() ? null : playlist.getHead();
        this.currentSong = (currentNode != null) ? currentNode.song : null;
    }

    // -------------------------------------------------------------------------
    // Playback navigation (theo UML + SESSION_CONTEXT)
    // -------------------------------------------------------------------------

    /**
     * Chuyen sang bai ke tiep.
     *
     * Logic theo RepeatMode:
     * ONE : tra ve bai hien tai, khong doi vi tri
     * OFF : neu dang o bai cuoi (tail) -> tra null (dung phat)
     * ALL : vong lai tu dau (CDLL xu ly tu dong)
     *
     * Luong xu ly (OFF / ALL):
     * 1. Push bai hien tai vao HistoryStack
     * 2. Goi playlist.getNextNode(currentNode) [TV1]
     * 3. Cap nhat currentNode, currentSong
     * 4. Tra ve currentSong moi
     *
     * @return Song ke tiep, hoac null neu het playlist (RepeatMode.OFF)
     */
    public Song playNext() {
        if (playlist.isEmpty())
            return null;

        // Neu chua co bai hien tai, bat dau tu dau CDLL
        if (currentNode == null) {
            currentNode = playlist.getHead();
            currentSong = currentNode.song;
            return currentSong;
        }

        // RepeatMode.ONE: lap bai hien tai, khong push lich su
        if (repeatMode == RepeatMode.ONE) {
            return currentSong;
        }

        // RepeatMode.OFF: kiem tra xem da o bai cuoi chua
        if (repeatMode == RepeatMode.OFF && currentNode == playlist.getTail()) {
            return null; // het danh sach, dung phat
        }

        // Push bai hien tai vao lich su truoc khi chuyen
        pushToHistory(currentSong);

        // Goi TV1's CDLL de lay node ke tiep (vong lai neu RepeatMode.ALL)
        currentNode = playlist.getNextNode(currentNode);
        currentSong = currentNode.song;
        return currentSong;
    }

    /**
     * Quay lai bai truoc (lay tu HistoryStack).
     *
     * Logic:
     * ONE : tra ve bai hien tai
     * Else: pop tu historyStack -> tim Node tuong ung trong CDLL
     * -> cap nhat currentNode, currentSong
     *
     * @return Song truoc do, hoac null neu khong con lich su
     */
    public Song playPrevious() {
        if (playlist.isEmpty())
            return null;

        // RepeatMode.ONE: lap bai hien tai
        if (repeatMode == RepeatMode.ONE) {
            return currentSong;
        }

        // Pop tu HistoryStack
        Song previous = popFromHistory();
        if (previous == null) {
            return null; // stack rong, khong co lich su de quay lai
        }

        // Tim Node tuong ung trong CDLL (phong truong hop bai bi xoa)
        Node prevNode = playlist.findById(previous.getId());
        if (prevNode == null) {
            // Bai da bi xoa khoi playlist -> bo qua, giu nguyen vi tri hien tai
            return null;
        }

        currentNode = prevNode;
        currentSong = previous;
        return currentSong;
    }

    // -------------------------------------------------------------------------
    // RepeatMode
    // -------------------------------------------------------------------------

    /**
     * Dat che do lap lai.
     *
     * @param mode RepeatMode.OFF / ONE / ALL (null -> mac dinh OFF)
     */
    public void setRepeatMode(RepeatMode mode) {
        this.repeatMode = (mode != null) ? mode : RepeatMode.OFF;
    }

    /** @return RepeatMode hien tai */
    public RepeatMode getRepeatMode() {
        return repeatMode;
    }

    // -------------------------------------------------------------------------
    // History wrappers (theo UML)
    // -------------------------------------------------------------------------

    /**
     * Them bai hat vao dinh HistoryStack.
     * Neu stack day (isFull) -> bo qua, khong push (khong crash).
     *
     * @param song bai hat can luu lich su
     */
    public void pushToHistory(Song song) {
        if (song == null)
            return;
        if (historyStack.isFull())
            return; // stack day, bo qua entry cu nhat
        historyStack.push(song);
    }

    /**
     * Lay bai hat gan nhat tu HistoryStack (xoa khoi stack).
     *
     * @return Song da pop, hoac null neu stack rong
     */
    public Song popFromHistory() {
        return historyStack.pop();
    }

    // -------------------------------------------------------------------------
    // Current song
    // -------------------------------------------------------------------------

    /**
     * @return bai hat dang phat hien tai, null neu playlist trong
     */
    public Song getCurrentSong() {
        return currentSong;
    }

    // -------------------------------------------------------------------------
    // Reset
    // -------------------------------------------------------------------------

    /**
     * Dat lai trang thai ve mac dinh:
     * - currentSong = bai dau tien trong playlist
     * - lich su bi xoa
     * - repeatMode = OFF
     */
    public void resetNavigation() {
        this.currentNode = playlist.isEmpty() ? null : playlist.getHead();
        this.currentSong = (currentNode != null) ? currentNode.song : null;
        historyStack.clear();
        this.repeatMode = RepeatMode.OFF;
    }

    // -------------------------------------------------------------------------
    // Shuffle (uy quyen cho ShuffleService)
    // -------------------------------------------------------------------------

    /**
     * Xao tron playlist bang Fisher-Yates (uy quyen cho ShuffleService).
     *
     * Sau khi goi:
     * - HistoryStack bi xoa (ShuffleService goi historyStack.clear())
     * - currentSong = bai dau tien trong mang da xao tron
     * - playNext() tiep tuc theo thu tu CDLL tu vi tri currentNode moi
     *
     * @return Song[] da xao tron (TV4 dung de hien thi hang doi shuffle)
     */
    public Song[] shuffle() {
        Song[] shuffled = shuffleService.getShuffledPlaylist(playlistManager);

        if (shuffled.length > 0) {
            // Nhay den bai dau tien trong mang shuffle
            Node node = playlist.findById(shuffled[0].getId());
            if (node != null) {
                currentNode = node;
                currentSong = node.song;
            }
        }

        return shuffled;
    }
}