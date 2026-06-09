package test;

import src.datastructure.HistoryStack;
import src.model.Song;

/**
 * HistoryStackTest — kiểm tra toàn bộ hành vi của HistoryStack.
 * Pass/Fail in ra console.
 */
public class HistoryStackTest {

    private static int passed = 0;
    private static int failed = 0;

    // ─── Helper ───────────────────────────────────────────────────────
    private static Song song(String id, String title) {
        return new Song(id, title, "Artist", 200);
    }

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            passed++;
        } else {
            System.out.println("  [FAIL] " + testName);
            failed++;
        }
    }

    private static void assertNull(String testName, Object obj) {
        assertTrue(testName, obj == null);
    }

    private static void assertNotNull(String testName, Object obj) {
        assertTrue(testName, obj != null);
    }

    private static void assertEquals(String testName, Object expected, Object actual) {
        boolean ok = (expected == null && actual == null)
                || (expected != null && expected.equals(actual));
        if (ok) {
            System.out.println("  [PASS] " + testName);
            passed++;
        } else {
            System.out.println("  [FAIL] " + testName
                    + " | expected=" + expected + " actual=" + actual);
            failed++;
        }
    }

    // ─── Test cases ───────────────────────────────────────────────────

    /** TC-HS-01: Stack mới phải rỗng */
    static void testNewStackIsEmpty() {
        System.out.println("\n[TC-HS-01] New stack is empty");
        HistoryStack hs = new HistoryStack();
        assertTrue("isEmpty() == true", hs.isEmpty());
        assertEquals("size() == 0", 0, hs.size());
        assertNull("peek() == null", hs.peek());
    }

    /** TC-HS-02: Push vào stack rỗng */
    static void testPushToEmptyStack() {
        System.out.println("\n[TC-HS-02] Push to empty stack");
        HistoryStack hs = new HistoryStack();
        Song s = song("1", "Shape of You");

        hs.push(s);
        assertTrue("not empty after push", !hs.isEmpty());
        assertEquals("size == 1", 1, hs.size());
        assertEquals("peek == pushed song", s, hs.peek());
    }

    /** TC-HS-03: Pop từ stack rỗng phải trả null */
    static void testPopFromEmptyStackReturnsNull() {
        System.out.println("\n[TC-HS-03] Pop from empty stack returns null");
        HistoryStack hs = new HistoryStack();
        Song result = hs.pop();
        assertNull("pop() == null", result);
        assertEquals("size still 0", 0, hs.size());
    }

    /** TC-HS-04: Push nhiều bài, pop đúng thứ tự LIFO */
    static void testPushPopOrder() {
        System.out.println("\n[TC-HS-04] Push/pop LIFO order");
        HistoryStack hs = new HistoryStack();
        Song s1 = song("1", "Song A");
        Song s2 = song("2", "Song B");
        Song s3 = song("3", "Song C");

        hs.push(s1);
        hs.push(s2);
        hs.push(s3);

        assertEquals("pop 1st == s3", s3, hs.pop());
        assertEquals("pop 2nd == s2", s2, hs.pop());
        assertEquals("pop 3rd == s1", s1, hs.pop());
        assertTrue("stack empty after all pops", hs.isEmpty());
    }

    /** TC-HS-05: Peek không làm thay đổi size */
    static void testPeekDoesNotChangeSize() {
        System.out.println("\n[TC-HS-05] Peek does not change size");
        HistoryStack hs = new HistoryStack();
        hs.push(song("1", "A"));
        hs.push(song("2", "B"));

        int sizeBefore = hs.size();
        Song top = hs.peek();
        int sizeAfter = hs.size();

        assertEquals("size unchanged", sizeBefore, sizeAfter);
        assertNotNull("peek returns non-null", top);
    }

    /**
     * TC-HS-06: Push đến đầy → isFull() == true, push thêm throw
     * IllegalStateException.
     * Behavior thực tế của HistoryStack: strict overflow, không tự shift.
     */
    static void testOverflowThrowsException() {
        System.out.println("\n[TC-HS-06] Push when full throws IllegalStateException");
        int cap = 3;
        HistoryStack hs = new HistoryStack(cap);

        hs.push(song("1", "Song A"));
        hs.push(song("2", "Song B"));
        hs.push(song("3", "Song C")); // FULL

        // Kiểm tra isFull() trước khi push
        assertTrue("isFull() == true at capacity", hs.isFull());
        assertEquals("size == capacity", cap, hs.size());

        // Push thêm phải throw exception
        boolean exceptionThrown = false;
        try {
            hs.push(song("4", "Song D"));
        } catch (IllegalStateException e) {
            exceptionThrown = true;
        }
        assertTrue("push() throws IllegalStateException when full", exceptionThrown);

        // Stack không bị thay đổi sau exception
        assertEquals("size unchanged after failed push", cap, hs.size());
        assertEquals("top unchanged after failed push", song("3", "Song C"), hs.peek());
    }

    /**
     * TC-HS-06b: Pop rồi push lại được — full không bị kẹt vĩnh viễn.
     */
    static void testPushAfterPopWhenFull() {
        System.out.println("\n[TC-HS-06b] Can push again after pop from full stack");
        int cap = 2;
        HistoryStack hs = new HistoryStack(cap);
        Song s1 = song("1", "A");
        Song s2 = song("2", "B");
        Song s3 = song("3", "C");

        hs.push(s1);
        hs.push(s2); // FULL

        hs.pop(); // tạo chỗ trống
        assertTrue("not full after pop", !hs.isFull());

        // Giờ push lại được, không throw
        boolean noException = true;
        try {
            hs.push(s3);
        } catch (IllegalStateException e) {
            noException = false;
        }
        assertTrue("push succeeds after pop", noException);
        assertEquals("top == s3", s3, hs.peek());
    }

    /** TC-HS-07: clear() → isEmpty() == true */
    static void testClearMakesEmpty() {
        System.out.println("\n[TC-HS-07] clear() results in isEmpty()");
        HistoryStack hs = new HistoryStack();
        hs.push(song("1", "A"));
        hs.push(song("2", "B"));
        hs.push(song("3", "C"));

        hs.clear();
        assertTrue("isEmpty() after clear", hs.isEmpty());
        assertEquals("size == 0 after clear", 0, hs.size());
        assertNull("peek == null after clear", hs.peek());
        assertNull("pop == null after clear", hs.pop());
    }

    /** TC-HS-08: getHistory() trả về đúng thứ tự mới nhất → cũ nhất */
    static void testGetHistoryOrder() {
        System.out.println("\n[TC-HS-08] getHistory() order: newest first");
        HistoryStack hs = new HistoryStack();
        Song s1 = song("1", "First");
        Song s2 = song("2", "Second");
        Song s3 = song("3", "Third");

        hs.push(s1);
        hs.push(s2);
        hs.push(s3);

        Song[] history = hs.getHistory();
        assertEquals("history length == 3", 3, history.length);
        assertEquals("history[0] == s3 (newest)", s3, history[0]);
        assertEquals("history[1] == s2", s2, history[1]);
        assertEquals("history[2] == s1 (oldest)", s1, history[2]);
    }

    /** TC-HS-09: getHistory() trên stack rỗng trả về mảng rỗng, không null */
    static void testGetHistoryOnEmpty() {
        System.out.println("\n[TC-HS-09] getHistory() on empty stack");
        HistoryStack hs = new HistoryStack();
        Song[] history = hs.getHistory();
        assertNotNull("getHistory() not null", history);
        assertEquals("getHistory().length == 0", 0, history.length);
    }

    /** TC-HS-10: Custom capacity constructor */
    static void testCustomCapacity() {
        System.out.println("\n[TC-HS-10] Custom capacity constructor");
        HistoryStack hs = new HistoryStack(10);
        assertEquals("getCapacity() == 10", 10, hs.getCapacity());
        assertTrue("isEmpty at start", hs.isEmpty());
    }

    // ─── Main ─────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════");
        System.out.println("  HistoryStack Test Suite");
        System.out.println("═══════════════════════════════════════");

        testNewStackIsEmpty();
        testPushToEmptyStack();
        testPopFromEmptyStackReturnsNull();
        testPushPopOrder();
        testPeekDoesNotChangeSize();
        testOverflowThrowsException();
        testPushAfterPopWhenFull();
        testClearMakesEmpty();
        testGetHistoryOrder();
        testGetHistoryOnEmpty();
        testCustomCapacity();

        System.out.println("\n═══════════════════════════════════════");
        System.out.printf("  Results: %d passed, %d failed%n", passed, failed);
        System.out.println("═══════════════════════════════════════");

        if (failed > 0)
            System.exit(1);
    }
}