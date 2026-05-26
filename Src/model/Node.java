package Src.model;

/**
 * Node bọc Song dùng cho Circular Doubly Linked List.
 * Mỗi node chứa một bài hát và hai con trỏ: next và prev.
 */
public class Node {
    public Song song;
    public Node next;
    public Node prev;

    public Node(Song song) {
        this.song = song;
        this.next = null;
        this.prev = null;
    }

    @Override
    public String toString() {
        return "Node{song=" + (song != null ? song.getTitle() : "null") + "}";
    }
}
