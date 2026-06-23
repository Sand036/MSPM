package test;

import src.datastructure.HistoryStack;
import src.model.Song;
import src.service.NavigationService;
import src.service.NavigationService.RepeatMode;
import src.service.PlaylistManager;

/**
 * NavigationServiceTest — kiểm tra toàn bộ hành vi của NavigationService.
 * Pass/Fail in ra console.
 */
public class NavigationServiceTest {

    private static int passed = 0;
    private static int failed = 0;

    // ─── Helper ───────────────────────────────────────────────────────
    private static Song song(String id, String title) {
        return new Song(id, title, "Artist", 200);
    }

    /** Tạo PlaylistManager có sẵn n bài (id = "1".."n") */
    private static PlaylistManager managerWith(Song... songs) {
        PlaylistManager pm = new PlaylistManager();
        for (Song s : songs) {
            pm.addSong(s);
        }
        return pm;
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

    /** TC-NS-01: Constructor null PlaylistManager -> throw */
    static void testConstructorNullManagerThrows() {
        System.out.println("\n[TC-NS-01] Constructor with null PlaylistManager throws");
        boolean thrown = false;
        try {
            new NavigationService(null, new HistoryStack());
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue("throws IllegalArgumentException for null manager", thrown);
    }

    /** TC-NS-02: Constructor null HistoryStack -> throw */
    static void testConstructorNullHistoryStackThrows() {
        System.out.println("\n[TC-NS-02] Constructor with null HistoryStack throws");
        boolean thrown = false;
        try {
            new NavigationService(new PlaylistManager(), null);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue("throws IllegalArgumentException for null historyStack", thrown);
    }

    /** TC-NS-03: Constructor với playlist có bài -> currentSong là bài đầu */
    static void testConstructorSetCurrentSongToFirst() {
        System.out.println("\n[TC-NS-03] Constructor sets currentSong to first song");
        Song s1 = song("1", "First");
        Song s2 = song("2", "Second");
        PlaylistManager pm = managerWith(s1, s2);
        NavigationService ns = new NavigationService(pm, new HistoryStack());

        assertEquals("currentSong == first song", s1, ns.getCurrentSong());
    }

    /** TC-NS-04: Constructor với playlist rỗng -> currentSong == null */
    static void testConstructorEmptyPlaylist() {
        System.out.println("\n[TC-NS-04] Constructor with empty playlist -> currentSong null");
        PlaylistManager pm = new PlaylistManager();
        NavigationService ns = new NavigationService(pm, new HistoryStack());

        assertNull("currentSong == null", ns.getCurrentSong());
    }

    /** TC-NS-05: Default RepeatMode là OFF */
    static void testDefaultRepeatModeIsOff() {
        System.out.println("\n[TC-NS-05] Default RepeatMode is OFF");
        PlaylistManager pm = managerWith(song("1", "A"));
        NavigationService ns = new NavigationService(pm, new HistoryStack());

        assertEquals("repeatMode == OFF", RepeatMode.OFF, ns.getRepeatMode());
    }

    /** TC-NS-06: setRepeatMode thay đổi đúng mode */
    static void testSetRepeatMode() {
        System.out.println("\n[TC-NS-06] setRepeatMode changes mode correctly");
        PlaylistManager pm = managerWith(song("1", "A"));
        NavigationService ns = new NavigationService(pm, new HistoryStack());

        ns.setRepeatMode(RepeatMode.ONE);
        assertEquals("mode == ONE", RepeatMode.ONE, ns.getRepeatMode());

        ns.setRepeatMode(RepeatMode.ALL);
        assertEquals("mode == ALL", RepeatMode.ALL, ns.getRepeatMode());

        ns.setRepeatMode(null);
        assertEquals("null -> defaults to OFF", RepeatMode.OFF, ns.getRepeatMode());
    }

    /** TC-NS-07: playNext trên playlist rỗng trả null */
    static void testPlayNextOnEmptyPlaylist() {
        System.out.println("\n[TC-NS-07] playNext on empty playlist returns null");
        PlaylistManager pm = new PlaylistManager();
        NavigationService ns = new NavigationService(pm, new HistoryStack());

        assertNull("playNext() == null", ns.playNext());
    }

    /** TC-NS-08: playNext (RepeatMode.OFF) chuyển qua bài kế, dừng ở cuối */
    static void testPlayNextRepeatOff() {
        System.out.println("\n[TC-NS-08] playNext RepeatMode.OFF - stops at last song");
        Song s1 = song("1", "Song A");
        Song s2 = song("2", "Song B");
        Song s3 = song("3", "Song C");
        PlaylistManager pm = managerWith(s1, s2, s3);
        NavigationService ns = new NavigationService(pm, new HistoryStack());
        ns.setRepeatMode(RepeatMode.OFF);

        // Bài đầu là s1 (từ constructor)
        assertEquals("current == s1", s1, ns.getCurrentSong());

        // Next -> s2
        Song next1 = ns.playNext();
        assertEquals("playNext 1st == s2", s2, next1);

        // Next -> s3
        Song next2 = ns.playNext();
        assertEquals("playNext 2nd == s3", s3, next2);

        // Đang ở bài cuối (tail), next -> null (OFF)
        Song next3 = ns.playNext();
        assertNull("playNext at tail (OFF) == null", next3);
    }

    /** TC-NS-09: playNext (RepeatMode.ALL) vòng lại đầu */
    static void testPlayNextRepeatAll() {
        System.out.println("\n[TC-NS-09] playNext RepeatMode.ALL - wraps around");
        Song s1 = song("1", "Song A");
        Song s2 = song("2", "Song B");
        PlaylistManager pm = managerWith(s1, s2);
        NavigationService ns = new NavigationService(pm, new HistoryStack());
        ns.setRepeatMode(RepeatMode.ALL);

        // Current = s1 -> next = s2
        assertEquals("playNext == s2", s2, ns.playNext());
        // s2 -> vòng lại s1 (CDLL wraps)
        assertEquals("playNext wraps == s1", s1, ns.playNext());
    }

    /** TC-NS-10: playNext (RepeatMode.ONE) trả bài hiện tại, không đổi vị trí */
    static void testPlayNextRepeatOne() {
        System.out.println("\n[TC-NS-10] playNext RepeatMode.ONE - repeats current");
        Song s1 = song("1", "Song A");
        Song s2 = song("2", "Song B");
        PlaylistManager pm = managerWith(s1, s2);
        NavigationService ns = new NavigationService(pm, new HistoryStack());
        ns.setRepeatMode(RepeatMode.ONE);

        assertEquals("playNext == s1 (repeat same)", s1, ns.playNext());
        assertEquals("playNext again == s1", s1, ns.playNext());
        assertEquals("currentSong unchanged", s1, ns.getCurrentSong());
    }

    /** TC-NS-11: playNext push bài hiện tại vào history trước khi chuyển */
    static void testPlayNextPushesToHistory() {
        System.out.println("\n[TC-NS-11] playNext pushes current song to history");
        Song s1 = song("1", "Song A");
        Song s2 = song("2", "Song B");
        Song s3 = song("3", "Song C");
        PlaylistManager pm = managerWith(s1, s2, s3);
        HistoryStack hs = new HistoryStack();
        NavigationService ns = new NavigationService(pm, hs);
        ns.setRepeatMode(RepeatMode.ALL);

        // Current = s1, play next -> push s1 to history, current = s2
        ns.playNext();
        assertEquals("history has s1 on top", s1, hs.peek());
        assertEquals("history size == 1", 1, hs.size());

        // Current = s2, play next -> push s2 to history, current = s3
        ns.playNext();
        assertEquals("history has s2 on top", s2, hs.peek());
        assertEquals("history size == 2", 2, hs.size());
    }

    /** TC-NS-12: playPrevious trên playlist rỗng trả null */
    static void testPlayPreviousOnEmptyPlaylist() {
        System.out.println("\n[TC-NS-12] playPrevious on empty playlist returns null");
        PlaylistManager pm = new PlaylistManager();
        NavigationService ns = new NavigationService(pm, new HistoryStack());

        assertNull("playPrevious() == null", ns.playPrevious());
    }

    /** TC-NS-13: playPrevious (RepeatMode.ONE) trả bài hiện tại */
    static void testPlayPreviousRepeatOne() {
        System.out.println("\n[TC-NS-13] playPrevious RepeatMode.ONE - repeats current");
        Song s1 = song("1", "Song A");
        PlaylistManager pm = managerWith(s1);
        NavigationService ns = new NavigationService(pm, new HistoryStack());
        ns.setRepeatMode(RepeatMode.ONE);

        assertEquals("playPrevious == s1 (repeat same)", s1, ns.playPrevious());
    }

    /** TC-NS-14: playPrevious khi history rỗng trả null */
    static void testPlayPreviousEmptyHistory() {
        System.out.println("\n[TC-NS-14] playPrevious with empty history returns null");
        Song s1 = song("1", "Song A");
        PlaylistManager pm = managerWith(s1);
        NavigationService ns = new NavigationService(pm, new HistoryStack());

        assertNull("playPrevious() == null (no history)", ns.playPrevious());
    }

    /** TC-NS-15: playPrevious pop từ history, quay lại bài trước */
    static void testPlayPreviousNavigatesBack() {
        System.out.println("\n[TC-NS-15] playPrevious navigates back through history");
        Song s1 = song("1", "Song A");
        Song s2 = song("2", "Song B");
        Song s3 = song("3", "Song C");
        PlaylistManager pm = managerWith(s1, s2, s3);
        HistoryStack hs = new HistoryStack();
        NavigationService ns = new NavigationService(pm, hs);
        ns.setRepeatMode(RepeatMode.ALL);

        // Chuyển tiến: s1 -> s2 -> s3
        ns.playNext(); // s1 pushed, current = s2
        ns.playNext(); // s2 pushed, current = s3

        // Quay lại: s3 -> s2
        Song prev1 = ns.playPrevious();
        assertEquals("playPrevious == s2", s2, prev1);
        assertEquals("currentSong == s2", s2, ns.getCurrentSong());

        // Quay lại: s2 -> s1
        Song prev2 = ns.playPrevious();
        assertEquals("playPrevious == s1", s1, prev2);
        assertEquals("currentSong == s1", s1, ns.getCurrentSong());

        // History hết, quay lại nữa -> null
        Song prev3 = ns.playPrevious();
        assertNull("playPrevious == null (no more history)", prev3);
    }

    /** TC-NS-16: pushToHistory bỏ qua song null */
    static void testPushToHistoryIgnoresNull() {
        System.out.println("\n[TC-NS-16] pushToHistory ignores null song");
        PlaylistManager pm = managerWith(song("1", "A"));
        HistoryStack hs = new HistoryStack();
        NavigationService ns = new NavigationService(pm, hs);

        ns.pushToHistory(null);
        assertTrue("history still empty", hs.isEmpty());
    }

    /** TC-NS-17: pushToHistory bỏ qua khi stack đầy (không crash) */
    static void testPushToHistorySkipsWhenFull() {
        System.out.println("\n[TC-NS-17] pushToHistory skips when stack is full");
        HistoryStack hs = new HistoryStack(2); // capacity = 2
        PlaylistManager pm = managerWith(song("1", "A"));
        NavigationService ns = new NavigationService(pm, hs);

        ns.pushToHistory(song("x", "X"));
        ns.pushToHistory(song("y", "Y")); // stack đầy
        ns.pushToHistory(song("z", "Z")); // bỏ qua, không crash

        assertEquals("history size == 2 (capped)", 2, hs.size());
    }

    /** TC-NS-18: popFromHistory trả null khi stack rỗng */
    static void testPopFromHistoryEmptyReturnsNull() {
        System.out.println("\n[TC-NS-18] popFromHistory returns null when empty");
        PlaylistManager pm = managerWith(song("1", "A"));
        NavigationService ns = new NavigationService(pm, new HistoryStack());

        assertNull("popFromHistory() == null", ns.popFromHistory());
    }

    /** TC-NS-19: resetNavigation đặt lại trạng thái ban đầu */
    static void testResetNavigation() {
        System.out.println("\n[TC-NS-19] resetNavigation resets state");
        Song s1 = song("1", "Song A");
        Song s2 = song("2", "Song B");
        PlaylistManager pm = managerWith(s1, s2);
        HistoryStack hs = new HistoryStack();
        NavigationService ns = new NavigationService(pm, hs);
        ns.setRepeatMode(RepeatMode.ALL);

        // Chuyển bài và tạo history
        ns.playNext();
        assertEquals("currentSong == s2 before reset", s2, ns.getCurrentSong());
        assertTrue("history not empty before reset", !hs.isEmpty());

        // Reset
        ns.resetNavigation();

        assertEquals("currentSong == s1 after reset", s1, ns.getCurrentSong());
        assertTrue("history empty after reset", hs.isEmpty());
        assertEquals("repeatMode == OFF after reset", RepeatMode.OFF, ns.getRepeatMode());
    }

    /** TC-NS-20: resetNavigation trên playlist rỗng */
    static void testResetNavigationEmptyPlaylist() {
        System.out.println("\n[TC-NS-20] resetNavigation on empty playlist");
        PlaylistManager pm = new PlaylistManager();
        NavigationService ns = new NavigationService(pm, new HistoryStack());

        ns.resetNavigation();
        assertNull("currentSong == null after reset", ns.getCurrentSong());
    }

    /** TC-NS-21: shuffle trả về mảng và reset history */
    static void testShuffleClearsHistoryAndReturnsSongs() {
        System.out.println("\n[TC-NS-21] shuffle clears history and returns songs");
        Song s1 = song("1", "Song A");
        Song s2 = song("2", "Song B");
        Song s3 = song("3", "Song C");
        PlaylistManager pm = managerWith(s1, s2, s3);
        HistoryStack hs = new HistoryStack();
        NavigationService ns = new NavigationService(pm, hs);
        ns.setRepeatMode(RepeatMode.ALL);

        // Tạo history
        ns.playNext();
        ns.playNext();
        assertTrue("history not empty before shuffle", !hs.isEmpty());

        // Shuffle
        Song[] shuffled = ns.shuffle();

        assertNotNull("shuffled array not null", shuffled);
        assertEquals("shuffled length == 3", 3, shuffled.length);
        assertTrue("history cleared after shuffle", hs.isEmpty());
        assertNotNull("currentSong not null after shuffle", ns.getCurrentSong());
    }

    /** TC-NS-22: playNext chỉ với 1 bài, RepeatMode.OFF -> dừng ngay */
    static void testPlayNextSingleSongRepeatOff() {
        System.out.println("\n[TC-NS-22] playNext single song RepeatMode.OFF - stops");
        Song s1 = song("1", "Only Song");
        PlaylistManager pm = managerWith(s1);
        NavigationService ns = new NavigationService(pm, new HistoryStack());
        ns.setRepeatMode(RepeatMode.OFF);

        // Current = s1, s1 cũng là tail -> next trả null
        Song next = ns.playNext();
        assertNull("playNext at tail (single song, OFF) == null", next);
    }

    /** TC-NS-23: playNext chỉ với 1 bài, RepeatMode.ALL -> lặp bài đó */
    static void testPlayNextSingleSongRepeatAll() {
        System.out.println("\n[TC-NS-23] playNext single song RepeatMode.ALL - loops");
        Song s1 = song("1", "Only Song");
        PlaylistManager pm = managerWith(s1);
        HistoryStack hs = new HistoryStack();
        NavigationService ns = new NavigationService(pm, hs);
        ns.setRepeatMode(RepeatMode.ALL);

        // CDLL có 1 node -> getNextNode trả chính nó, push s1 rồi current vẫn s1
        Song next = ns.playNext();
        assertEquals("playNext loops back == s1", s1, next);
    }

    // ─── Main ─────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════");
        System.out.println("  NavigationService Test Suite");
        System.out.println("═══════════════════════════════════════");

        testConstructorNullManagerThrows();
        testConstructorNullHistoryStackThrows();
        testConstructorSetCurrentSongToFirst();
        testConstructorEmptyPlaylist();
        testDefaultRepeatModeIsOff();
        testSetRepeatMode();
        testPlayNextOnEmptyPlaylist();
        testPlayNextRepeatOff();
        testPlayNextRepeatAll();
        testPlayNextRepeatOne();
        testPlayNextPushesToHistory();
        testPlayPreviousOnEmptyPlaylist();
        testPlayPreviousRepeatOne();
        testPlayPreviousEmptyHistory();
        testPlayPreviousNavigatesBack();
        testPushToHistoryIgnoresNull();
        testPushToHistorySkipsWhenFull();
        testPopFromHistoryEmptyReturnsNull();
        testResetNavigation();
        testResetNavigationEmptyPlaylist();
        testShuffleClearsHistoryAndReturnsSongs();
        testPlayNextSingleSongRepeatOff();
        testPlayNextSingleSongRepeatAll();

        System.out.println("\n═══════════════════════════════════════");
        System.out.printf("  Results: %d passed, %d failed%n", passed, failed);
        System.out.println("═══════════════════════════════════════");

        if (failed > 0)
            System.exit(1);
    }
}
