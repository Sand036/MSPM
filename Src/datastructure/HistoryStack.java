package Src.datastructure;

import Src.model.Song;

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

    /**
     * push() — Thêm bài hát vào đỉnh Stack (khi bấm Next)
     * Nếu Stack đầy: xóa bài cũ nhất ở đáy để nhường chỗ
     */
    public void push(Song song) {
        if (song == null)
            return;

        if (isFull()) {
            // Dịch toàn bộ mảng xuống 1 vị trí (xóa đáy, nhường chỗ đỉnh)
            for (int i = 0; i < capacity - 1; i++) {
                stack[i] = stack[i + 1];
            }
            top = capacity - 2; // top lùi lại 1 vì đã dịch
        }

        top++;
        stack[top] = song;
    }

    /**
     * pop() — Lấy bài hát ở đỉnh Stack ra (khi bấm Previous)
     * Trả về null nếu Stack rỗng (không có lịch sử)
     */
    public Song pop() {
        if (isEmpty())
            return null;

        Song song = stack[top];
        stack[top] = null; // giải phóng tham chiếu, tránh memory leak
        top--;
        return song;
    }

    /**
     * peek() — Xem bài hát ở đỉnh Stack MÀ KHÔNG lấy ra
     * Dùng để hiển thị "bài trước đó là gì" trên UI
     */
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

    /**
     * clear() — Xóa toàn bộ lịch sử (khi bấm Shuffle, reset history)
     */
    public void clear() {
        for (int i = 0; i <= top; i++) {
            stack[i] = null;
        }
        top = -1;
    }

    /**
     * getHistory() — Trả về mảng bài hát từ mới nhất → cũ nhất
     * Dùng để hiển thị danh sách Recently Played trên UI
     */
    public Song[] getHistory() {
        if (isEmpty())
            return new Song[0];

        Song[] history = new Song[top + 1];
        for (int i = 0; i <= top; i++) {
            // Đảo ngược thứ tự: đỉnh stack → index 0 của history
            history[top - i] = stack[i];
        }
        return history;
    }

    /**
     * toString() — In ra Stack để debug
     */
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