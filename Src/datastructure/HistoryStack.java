package src.datastructure;

import src.model.Song;

public class HistoryStack {

    // =========================================================
    // PHẦN 1: CẤU TRÚC NỘI BỘ
    // =========================================================

    private static final int DEFAULT_CAPACITY = 50;

    private Song[] stack;
    private int top;
    private int capacity;

    // =========================================================
    // PHẦN 2: CONSTRUCTOR
    // =========================================================

    public HistoryStack() {
        this(DEFAULT_CAPACITY);
    }

    public HistoryStack(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("The capacity must be greater than 0.");
        }
        this.capacity = capacity;
        this.stack = new Song[capacity];
        this.top = -1;
    }

    // =========================================================
    // PHẦN 3: CÁC THAO TÁC CHÍNH (CORE OPERATIONS)
    // =========================================================

    public void push(Song song) {

        if (song == null)
            return;

        if (isFull()) {
            throw new IllegalStateException("Stack Overflow! History is full.");
        }

        top++;
        stack[top] = song;
    }

    public Song pop() {
        if (isEmpty())
            return null;

        Song song = stack[top];
        stack[top] = null;
        top--;
        return song;
    }

    public Song peek() {
        if (isEmpty())
            return null;
        return stack[top];
    }

    // =========================================================
    // PHẦN 4: TRẠNG THÁI STACK (STATE CHECKS)
    // =========================================================

    public boolean isEmpty() {
        return top == -1;
    }

    public boolean isFull() {
        return top == capacity - 1;
    }

    public int size() {
        return top + 1;
    }

    public int getCapacity() {
        return capacity;
    }

    // =========================================================
    // PHẦN 5: TIỆN ÍCH (UTILITIES)
    // =========================================================

    public void clear() {
        for (int i = 0; i <= top; i++) {
            stack[i] = null;
        }
        top = -1;
    }

    public Song[] getHistory() {
        if (isEmpty())
            return new Song[0];

        Song[] history = new Song[top + 1];
        for (int i = 0; i <= top; i++) {
            history[top - i] = stack[i];
        }
        return history;
    }

    @Override
    public String toString() {
        if (isEmpty())
            return "HistoryStack: [Null]";

        StringBuilder sb = new StringBuilder("HistoryStack (top → bottom):\n");
        for (int i = top; i >= 0; i--) {
            sb.append("  [").append(top - i).append("] ")
                    .append(stack[i].getTitle())
                    .append(" — ").append(stack[i].getArtist());
            if (i == top)
                sb.append("  ← TOP");
            sb.append("\n");
        }
        sb.append("  Size: ").append(size()).append("/").append(capacity);
        return sb.toString();
    }
}