package src.service;

import src.datastructure.CircularLinkedList;
import src.model.Node;
import src.model.Song;

import java.util.List;

/**
 * PlaylistManager điều phối tất cả thao tác trên playlist:
 * thêm, xóa, lấy bài hiện tại, di chuyển next/prev.
 *
 * Đây là lớp trung gian giữa tầng server/API và CDLL bên dưới.
 */
public class PlaylistManager {

    public enum RepeatMode { OFF, ONE, ALL }
    private RepeatMode repeatMode = RepeatMode.ALL;

    private final CircularLinkedList playlist;
    private Node currentNode; // bài đang phát

    public PlaylistManager() {
        this.playlist = new CircularLinkedList();
        this.currentNode = null;
    }

    public void setRepeatMode(RepeatMode mode) {
        this.repeatMode = mode;
    }

    // ─────────────────────────────────────────────
    // ADD
    // ─────────────────────────────────────────────

    /**
     * Thêm bài hát vào cuối playlist.
     */
    public void addSong(Song song) {
        if (song == null)
            throw new IllegalArgumentException("Song không được null");
        playlist.insert(song);
        // Nếu đây là bài đầu tiên, đặt làm currentNode luôn
        if (currentNode == null) {
            currentNode = playlist.getHead();
        }
    }

    /**
     * Thêm bài hát vào vị trí cụ thể (0-based).
     */
    public void addSongAt(Song song, int index) {
        if (song == null)
            throw new IllegalArgumentException("Song không được null");
        playlist.insertAt(song, index);
        if (currentNode == null) {
            currentNode = playlist.getHead();
        }
    }

    // ─────────────────────────────────────────────
    // REMOVE
    // ─────────────────────────────────────────────

    /**
     * Xóa bài hát theo id.
     * Nếu bài đang xóa là bài hiện tại, chuyển sang bài tiếp theo.
     * 
     * @return true nếu xóa thành công
     */
    public boolean removeSong(String songId) {
        if (songId == null)
            return false;

        // Nếu bài bị xóa đang được phát → chuyển sang next trước
        if (currentNode != null && currentNode.song.getId().equals(songId)) {
            Node nextNode = playlist.getNextNode(currentNode);
            // Nếu chỉ còn 1 bài thì sau khi xóa currentNode = null
            currentNode = (playlist.getSize() > 1) ? nextNode : null;
        }

        return playlist.delete(songId);
    }

    /**
     * Xóa tất cả bài trong playlist.
     */
    public void clearPlaylist() {
        List<Song> songs = playlist.toList();
        for (Song s : songs) {
            playlist.delete(s.getId());
        }
        currentNode = null;
    }

    // ─────────────────────────────────────────────
    // CURRENT SONG
    // ─────────────────────────────────────────────

    /**
     * Lấy bài hát hiện tại đang được phát.
     * 
     * @return Song hiện tại, hoặc null nếu playlist trống.
     */
    public Song getCurrentSong() {
        return (currentNode != null) ? currentNode.song : null;
    }

    /**
     * Đặt bài hát hiện tại theo id.
     * 
     * @return true nếu tìm thấy và đặt thành công.
     */
    public boolean setCurrentSong(String songId) {
        Node node = playlist.findById(songId);
        if (node != null) {
            currentNode = node;
            return true;
        }
        return false;
    }

    // ─────────────────────────────────────────────
    // NAVIGATION (dùng nội bộ; NavigationService cũng có thể gọi)
    // ─────────────────────────────────────────────

    /**
     * Chuyển sang bài tiếp theo (vòng vòng nhờ CDLL).
     * 
     * @return Song tiếp theo, hoặc null nếu playlist trống.
     */
    public Song nextSong() {
        if (playlist.isEmpty())
            return null;
            
        if (currentNode == null) {
            currentNode = playlist.getHead();
            return currentNode.song;
        }
            
        if (repeatMode == RepeatMode.ONE) {
            return currentNode.song;
        }
        
        if (repeatMode == RepeatMode.OFF) {
            if (currentNode == playlist.getTail()) {
                currentNode = null;
                return null;
            }
        }
        
        currentNode = playlist.getNextNode(currentNode);
        return currentNode.song;
    }

    /**
     * Quay lại bài trước.
     * 
     * @return Song trước đó, hoặc null nếu playlist trống.
     */
    public Song prevSong() {
        if (playlist.isEmpty()) return null;
        if (currentNode == null) {
            currentNode = playlist.getTail();
            return currentNode.song;
        }
        
        if (repeatMode == RepeatMode.OFF) {
            if (currentNode == playlist.getHead()) {
                return currentNode.song;
            }
        }
        
        currentNode = playlist.getPrevNode(currentNode);
        return currentNode.song;
    }

    // ─────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────

    /**
     * Tìm bài hát theo id.
     */
    public Song findSongById(String songId) {
        Node node = playlist.findById(songId);
        return (node != null) ? node.song : null;
    }

    /**
     * Lấy toàn bộ danh sách bài hát.
     */
    public List<Song> getAllSongs() {
        return playlist.toList();
    }

    /**
     * Trả về danh sách bài hát dưới dạng mảng Song[]
     * phục vụ cho các thuật toán tìm kiếm và sắp xếp.
     */
    public Song[] getSongsArray() {
        return playlist.toArray();
    }

    /**
     * Số lượng bài hát trong playlist.
     */
    public int getSize() {
        return playlist.getSize();
    }

    /**
     * Playlist có trống không?
     */
    public boolean isEmpty() {
        return playlist.isEmpty();
    }

    /**
     * Truy cập trực tiếp CDLL (dành cho các service khác như Sorter, Searcher).
     */
    public CircularLinkedList getPlaylist() {
        return playlist;
    }

    /**
     * In toàn bộ playlist ra console.
     */
    public void displayPlaylist() {
        playlist.displayPlaylist();
        if (currentNode != null) {
            System.out.println("▶ Đang phát: " + currentNode.song.getTitle()
                    + " - " + currentNode.song.getArtist());
        }
    }
}
