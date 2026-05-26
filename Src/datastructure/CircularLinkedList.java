package datastructure;

import model.Node;
import model.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * Circular Doubly Linked List (CDLL) quản lý danh sách bài hát.
 * - Head và tail luôn trỏ vòng: tail.next = head, head.prev = tail.
 * - Hỗ trợ insert (đầu/cuối/theo vị trí), delete, display, getNextNode.
 */
public class CircularLinkedList {

    private Node head;
    private Node tail;
    private int size;

    public CircularLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    // ─────────────────────────────────────────────
    //  INSERT
    // ─────────────────────────────────────────────

    /**
     * Thêm bài hát vào cuối danh sách.
     */
    public void insert(Song song) {
        if (song == null) throw new IllegalArgumentException("Song không được null");
        Node newNode = new Node(song);

        if (isEmpty()) {
            head = newNode;
            tail = newNode;
            newNode.next = newNode;
            newNode.prev = newNode;
        } else {
            newNode.prev = tail;
            newNode.next = head;
            tail.next = newNode;
            head.prev = newNode;
            tail = newNode;
        }
        size++;
    }

    /**
     * Thêm bài hát vào đầu danh sách.
     */
    public void insertAtHead(Song song) {
        if (song == null) throw new IllegalArgumentException("Song không được null");
        Node newNode = new Node(song);

        if (isEmpty()) {
            head = newNode;
            tail = newNode;
            newNode.next = newNode;
            newNode.prev = newNode;
        } else {
            newNode.next = head;
            newNode.prev = tail;
            head.prev = newNode;
            tail.next = newNode;
            head = newNode;
        }
        size++;
    }

    /**
     * Thêm bài hát vào vị trí index (0-based).
     * index = 0 tương đương insertAtHead; index >= size tương đương insert (cuối).
     */
    public void insertAt(Song song, int index) {
        if (index <= 0) {
            insertAtHead(song);
            return;
        }
        if (index >= size) {
            insert(song);
            return;
        }

        Node newNode = new Node(song);
        Node current = head;
        for (int i = 0; i < index - 1; i++) {
            current = current.next;
        }
        Node nextNode = current.next;
        newNode.prev = current;
        newNode.next = nextNode;
        current.next = newNode;
        nextNode.prev = newNode;
        size++;
    }

    // ─────────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────────

    /**
     * Xóa bài hát theo id.
     * @return true nếu xóa thành công, false nếu không tìm thấy.
     */
    public boolean delete(String songId) {
        if (isEmpty() || songId == null) return false;

        Node current = head;
        for (int i = 0; i < size; i++) {
            if (current.song.getId().equals(songId)) {
                removeNode(current);
                return true;
            }
            current = current.next;
        }
        return false;
    }

    /**
     * Xóa node tại vị trí index (0-based).
     */
    public boolean deleteAt(int index) {
        if (isEmpty() || index < 0 || index >= size) return false;

        Node current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        removeNode(current);
        return true;
    }

    /**
     * Helper: tách một node khỏi danh sách.
     */
    private void removeNode(Node node) {
        if (size == 1) {
            head = null;
            tail = null;
        } else {
            node.prev.next = node.next;
            node.next.prev = node.prev;
            if (node == head) head = node.next;
            if (node == tail) tail = node.prev;
        }
        node.next = null;
        node.prev = null;
        size--;
    }

    // ─────────────────────────────────────────────
    //  DISPLAY
    // ─────────────────────────────────────────────

    /**
     * In toàn bộ danh sách ra console (theo chiều thuận).
     */
    public void displayPlaylist() {
        if (isEmpty()) {
            System.out.println("[Playlist trống]");
            return;
        }
        System.out.println("=== PLAYLIST (" + size + " bài) ===");
        Node current = head;
        int index = 0;
        do {
            System.out.printf("%2d. [%s] %s - %s (%s)%n",
                    index + 1,
                    current.song.getId(),
                    current.song.getTitle(),
                    current.song.getArtist(),
                    current.song.getFormattedDuration());
            current = current.next;
            index++;
        } while (current != head);
        System.out.println("================================");
    }

    /**
     * Trả về danh sách Song dưới dạng List (để serialize hoặc test).
     */
    public List<Song> toList() {
        List<Song> list = new ArrayList<>();
        if (isEmpty()) return list;
        Node current = head;
        do {
            list.add(current.song);
            current = current.next;
        } while (current != head);
        return list;
    }

    // ─────────────────────────────────────────────
    //  NAVIGATION
    // ─────────────────────────────────────────────

    /**
     * Lấy node tiếp theo sau node hiện tại.
     * Vì là CDLL nên sau tail sẽ quay về head.
     * @param current node hiện tại
     * @return node tiếp theo, hoặc null nếu danh sách trống
     */
    public Node getNextNode(Node current) {
        if (current == null || isEmpty()) return null;
        return current.next;
    }

    /**
     * Lấy node trước node hiện tại.
     */
    public Node getPrevNode(Node current) {
        if (current == null || isEmpty()) return null;
        return current.prev;
    }

    /**
     * Tìm node theo song id.
     * @return Node tìm thấy, hoặc null.
     */
    public Node findById(String songId) {
        if (isEmpty() || songId == null) return null;
        Node current = head;
        do {
            if (current.song.getId().equals(songId)) return current;
            current = current.next;
        } while (current != head);
        return null;
    }

    // ─────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────

    public boolean isEmpty() {
        return size == 0;
    }

    public int getSize() {
        return size;
    }

    public Node getHead() {
        return head;
    }

    public Node getTail() {
        return tail;
    }
}
