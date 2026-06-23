package test;

import src.datastructure.HistoryStack;
import src.model.Song;
import src.service.PlaylistManager;
import src.service.ShuffleService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * ShuffleServiceTest — kiểm tra toàn bộ hành vi của ShuffleService.
 * Pass/Fail in ra console.
 */
public class ShuffleServiceTest {

    private static int passed = 0;
    private static int failed = 0;

    // ─── Helper ───────────────────────────────────────────────────────
    private static Song song(String id, String title) {
        return new Song(id, title, "Artist", 200);
    }

    /** Tạo PlaylistManager có sẵn các bài */
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

    /** TC-SS-01: Constructor null historyStack -> throw */
    static void testConstructorNullHistoryStackThrows() {
        System.out.println("\n[TC-SS-01] Constructor with null historyStack throws");
        boolean thrown = false;
        try {
            new ShuffleService(null);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue("throws IllegalArgumentException for null historyStack", thrown);
    }

    /** TC-SS-02: Constructor với seed null historyStack -> throw */
    static void testConstructorWithSeedNullHistoryStackThrows() {
        System.out.println("\n[TC-SS-02] Constructor(null, seed) throws");
        boolean thrown = false;
        try {
            new ShuffleService(null, 42L);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue("throws IllegalArgumentException for null historyStack (seed ctor)", thrown);
    }

    /** TC-SS-03: getShuffledPlaylist với null manager -> mảng rỗng */
    static void testShuffleNullManagerReturnsEmpty() {
        System.out.println("\n[TC-SS-03] getShuffledPlaylist(null) returns empty array");
        ShuffleService ss = new ShuffleService(new HistoryStack());

        Song[] result = ss.getShuffledPlaylist(null);
        assertNotNull("result not null", result);
        assertEquals("result length == 0", 0, result.length);
    }

    /** TC-SS-04: getShuffledPlaylist với manager rỗng -> mảng rỗng */
    static void testShuffleEmptyManagerReturnsEmpty() {
        System.out.println("\n[TC-SS-04] getShuffledPlaylist(empty manager) returns empty array");
        ShuffleService ss = new ShuffleService(new HistoryStack());
        PlaylistManager pm = new PlaylistManager();

        Song[] result = ss.getShuffledPlaylist(pm);
        assertNotNull("result not null", result);
        assertEquals("result length == 0", 0, result.length);
    }

    /** TC-SS-05: Shuffle 1 bài -> trả mảng 1 phần tử, không thay đổi */
    static void testShuffleSingleSong() {
        System.out.println("\n[TC-SS-05] Shuffle single song - no change");
        Song s1 = song("1", "Only Song");
        PlaylistManager pm = managerWith(s1);
        ShuffleService ss = new ShuffleService(new HistoryStack());

        Song[] result = ss.getShuffledPlaylist(pm);
        assertEquals("result length == 1", 1, result.length);
        assertEquals("result[0] == s1", s1, result[0]);
    }

    /** TC-SS-06: Shuffle giữ đúng các phần tử (không mất, không thêm) */
    static void testShufflePreservesAllElements() {
        System.out.println("\n[TC-SS-06] Shuffle preserves all elements");
        Song s1 = song("1", "Song A");
        Song s2 = song("2", "Song B");
        Song s3 = song("3", "Song C");
        Song s4 = song("4", "Song D");
        Song s5 = song("5", "Song E");
        PlaylistManager pm = managerWith(s1, s2, s3, s4, s5);
        ShuffleService ss = new ShuffleService(new HistoryStack(), 2024L);

        Song[] result = ss.getShuffledPlaylist(pm);
        assertEquals("result length == 5", 5, result.length);

        // Kiểm tra tất cả bài gốc đều có mặt
        Set<String> originalIds = new HashSet<>(Arrays.asList("1", "2", "3", "4", "5"));
        Set<String> resultIds = new HashSet<>();
        for (Song s : result) {
            resultIds.add(s.getId());
        }
        assertEquals("all original ids present", originalIds, resultIds);
    }

    /** TC-SS-07: Shuffle với seed cố định cho kết quả tái lập được */
    static void testShuffleWithSeedIsReproducible() {
        System.out.println("\n[TC-SS-07] Shuffle with same seed is reproducible");
        Song s1 = song("1", "A");
        Song s2 = song("2", "B");
        Song s3 = song("3", "C");
        Song s4 = song("4", "D");
        Song s5 = song("5", "E");

        // Shuffle lần 1
        PlaylistManager pm1 = managerWith(s1, s2, s3, s4, s5);
        ShuffleService ss1 = new ShuffleService(new HistoryStack(), 12345L);
        Song[] result1 = ss1.getShuffledPlaylist(pm1);

        // Shuffle lần 2 với cùng seed
        PlaylistManager pm2 = managerWith(
                song("1", "A"), song("2", "B"), song("3", "C"),
                song("4", "D"), song("5", "E"));
        ShuffleService ss2 = new ShuffleService(new HistoryStack(), 12345L);
        Song[] result2 = ss2.getShuffledPlaylist(pm2);

        assertEquals("same length", result1.length, result2.length);
        boolean sameOrder = true;
        for (int i = 0; i < result1.length; i++) {
            if (!result1[i].getId().equals(result2[i].getId())) {
                sameOrder = false;
                break;
            }
        }
        assertTrue("same order with same seed", sameOrder);
    }

    /** TC-SS-08: Shuffle thực sự thay đổi thứ tự (seed chọn sao cho đổi) */
    static void testShuffleChangesOrder() {
        System.out.println("\n[TC-SS-08] Shuffle actually changes order (statistical)");
        Song s1 = song("1", "A");
        Song s2 = song("2", "B");
        Song s3 = song("3", "C");
        Song s4 = song("4", "D");
        Song s5 = song("5", "E");

        // Thử nhiều seed, ít nhất 1 seed phải cho thứ tự khác
        boolean orderChanged = false;
        for (long seed = 0; seed < 10; seed++) {
            PlaylistManager pm = managerWith(
                    song("1", "A"), song("2", "B"), song("3", "C"),
                    song("4", "D"), song("5", "E"));
            ShuffleService ss = new ShuffleService(new HistoryStack(), seed);
            Song[] result = ss.getShuffledPlaylist(pm);

            // So sánh thứ tự với thứ tự gốc 1,2,3,4,5
            for (int i = 0; i < result.length; i++) {
                if (!result[i].getId().equals(String.valueOf(i + 1))) {
                    orderChanged = true;
                    break;
                }
            }
            if (orderChanged) break;
        }
        assertTrue("order changed in at least one seed", orderChanged);
    }

    /** TC-SS-09: Shuffle xóa HistoryStack */
    static void testShuffleClearsHistory() {
        System.out.println("\n[TC-SS-09] Shuffle clears history stack");
        HistoryStack hs = new HistoryStack();
        hs.push(song("x", "Old History 1"));
        hs.push(song("y", "Old History 2"));
        assertTrue("history not empty before shuffle", !hs.isEmpty());

        PlaylistManager pm = managerWith(song("1", "A"), song("2", "B"));
        ShuffleService ss = new ShuffleService(hs);

        ss.getShuffledPlaylist(pm);
        assertTrue("history empty after shuffle", hs.isEmpty());
        assertEquals("history size == 0", 0, hs.size());
    }

    /** TC-SS-10: getSongs() trả null trước khi gọi shuffle lần đầu */
    static void testGetSongsBeforeShuffleIsNull() {
        System.out.println("\n[TC-SS-10] getSongs() before shuffle returns null");
        ShuffleService ss = new ShuffleService(new HistoryStack());

        assertNull("getSongs() == null initially", ss.getSongs());
    }

    /** TC-SS-11: getSongs() trả kết quả sau shuffle */
    static void testGetSongsAfterShuffle() {
        System.out.println("\n[TC-SS-11] getSongs() after shuffle returns result");
        Song s1 = song("1", "A");
        Song s2 = song("2", "B");
        PlaylistManager pm = managerWith(s1, s2);
        ShuffleService ss = new ShuffleService(new HistoryStack(), 42L);

        Song[] shuffled = ss.getShuffledPlaylist(pm);
        Song[] stored = ss.getSongs();

        assertNotNull("getSongs() not null", stored);
        assertEquals("getSongs() length matches", shuffled.length, stored.length);

        // Cùng tham chiếu mảng
        boolean sameArray = (shuffled == stored);
        assertTrue("getSongs() returns same array reference", sameArray);
    }

    /** TC-SS-12: fisherYatesShuffle với null -> không crash */
    static void testFisherYatesShuffleNull() {
        System.out.println("\n[TC-SS-12] fisherYatesShuffle(null) does not crash");
        ShuffleService ss = new ShuffleService(new HistoryStack());
        boolean noCrash = true;
        try {
            ss.fisherYatesShuffle(null);
        } catch (Exception e) {
            noCrash = false;
        }
        assertTrue("no exception on null input", noCrash);
    }

    /** TC-SS-13: fisherYatesShuffle với mảng 1 phần tử -> không đổi */
    static void testFisherYatesShuffleSingleElement() {
        System.out.println("\n[TC-SS-13] fisherYatesShuffle single element - unchanged");
        ShuffleService ss = new ShuffleService(new HistoryStack());
        Song s1 = song("1", "Solo");
        Song[] arr = { s1 };

        ss.fisherYatesShuffle(arr);
        assertEquals("arr[0] unchanged", s1, arr[0]);
    }

    /** TC-SS-14: fisherYatesShuffle với mảng rỗng -> không crash */
    static void testFisherYatesShuffleEmptyArray() {
        System.out.println("\n[TC-SS-14] fisherYatesShuffle empty array - no crash");
        ShuffleService ss = new ShuffleService(new HistoryStack());
        Song[] arr = {};
        boolean noCrash = true;
        try {
            ss.fisherYatesShuffle(arr);
        } catch (Exception e) {
            noCrash = false;
        }
        assertTrue("no exception on empty array", noCrash);
        assertEquals("array still empty", 0, arr.length);
    }

    /** TC-SS-15: Shuffle mảng lớn (100 bài) giữ nguyên tất cả phần tử */
    static void testShuffleLargeArray() {
        System.out.println("\n[TC-SS-15] Shuffle large array (100 songs) preserves all");
        PlaylistManager pm = new PlaylistManager();
        for (int i = 1; i <= 100; i++) {
            pm.addSong(song(String.valueOf(i), "Song " + i));
        }
        ShuffleService ss = new ShuffleService(new HistoryStack(), 999L);

        Song[] result = ss.getShuffledPlaylist(pm);
        assertEquals("result length == 100", 100, result.length);

        // Kiểm tra không trùng id
        Set<String> ids = new HashSet<>();
        for (Song s : result) {
            ids.add(s.getId());
        }
        assertEquals("100 unique ids", 100, ids.size());
    }

    /** TC-SS-16: Gọi shuffle nhiều lần liên tiếp không crash */
    static void testMultipleShufflesNoCrash() {
        System.out.println("\n[TC-SS-16] Multiple consecutive shuffles do not crash");
        Song s1 = song("1", "A");
        Song s2 = song("2", "B");
        Song s3 = song("3", "C");
        HistoryStack hs = new HistoryStack();
        ShuffleService ss = new ShuffleService(hs);

        boolean noCrash = true;
        try {
            for (int i = 0; i < 5; i++) {
                PlaylistManager pm = managerWith(
                        song("1", "A"), song("2", "B"), song("3", "C"));
                // Thêm vào history trước mỗi shuffle
                hs.push(song("h" + i, "History " + i));
                ss.getShuffledPlaylist(pm);
            }
        } catch (Exception e) {
            noCrash = false;
        }
        assertTrue("no exception after 5 shuffles", noCrash);
        assertTrue("history cleared after last shuffle", hs.isEmpty());
    }

    // ─── Main ─────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════");
        System.out.println("  ShuffleService Test Suite");
        System.out.println("═══════════════════════════════════════");

        testConstructorNullHistoryStackThrows();
        testConstructorWithSeedNullHistoryStackThrows();
        testShuffleNullManagerReturnsEmpty();
        testShuffleEmptyManagerReturnsEmpty();
        testShuffleSingleSong();
        testShufflePreservesAllElements();
        testShuffleWithSeedIsReproducible();
        testShuffleChangesOrder();
        testShuffleClearsHistory();
        testGetSongsBeforeShuffleIsNull();
        testGetSongsAfterShuffle();
        testFisherYatesShuffleNull();
        testFisherYatesShuffleSingleElement();
        testFisherYatesShuffleEmptyArray();
        testShuffleLargeArray();
        testMultipleShufflesNoCrash();

        System.out.println("\n═══════════════════════════════════════");
        System.out.printf("  Results: %d passed, %d failed%n", passed, failed);
        System.out.println("═══════════════════════════════════════");

        if (failed > 0)
            System.exit(1);
    }
}
