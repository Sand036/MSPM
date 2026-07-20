package test;

/**
 * RQ1Benchmark -- TV2
 *
 * So sanh DLL vs CDLL khi thuc hien next() va prev() o che do RepeatMode.ALL.
 * Metric do:
 * 1. Branch check count -- so lan phai kiem tra null (DLL) vs khong can (CDLL)
 * 2. Thoi gian thuc thi -- System.nanoTime(), don vi ms
 * 3. Speedup -- DLL_time / CDLL_time
 *
 * Cach chay:
 * javac -d out test/RQ1Benchmark.java
 * java -cp out test.RQ1Benchmark
 */
public class RQ1Benchmark {

    // -------------------------------------------------------------------------
    // Cau hinh benchmark
    // -------------------------------------------------------------------------

    /** So buoc next() moi lan do. Cang lon ket qua cang on dinh. */
    private static final int TOTAL_STEPS = 500_000;

    /** So lan lap lai moi test de lay trung binh (giam nhieu do). */
    private static final int REPEAT = 5;

    /** Cac kich co playlist can do. */
    private static final int[] SIZES = { 10, 50, 100, 500 };

    // -------------------------------------------------------------------------
    // Node don gian -- khong phu thuoc vao Song.java hay Node.java cua TV1
    // Chi luu index de nhe, tranh overhead tu object Song that
    // -------------------------------------------------------------------------

    static class DllNode {
        int index;
        DllNode next;
        DllNode prev;

        DllNode(int index) {
            this.index = index;
        }
    }

    static class CdllNode {
        int index;
        CdllNode next;
        CdllNode prev;

        CdllNode(int index) {
            this.index = index;
        }
    }

    // -------------------------------------------------------------------------
    // Xay dung DLL
    // -------------------------------------------------------------------------

    /**
     * Tao DLL size phan tu.
     * Dac diem: tail.next = null, head.prev = null.
     *
     * @return head cua DLL
     */
    static DllNode buildDLL(int size) {
        DllNode head = new DllNode(0);
        DllNode current = head;
        for (int i = 1; i < size; i++) {
            DllNode node = new DllNode(i);
            node.prev = current;
            current.next = node;
            current = node;
        }
        // tail.next van la null -- DLL khong co lien ket vong
        return head;
    }

    // -------------------------------------------------------------------------
    // Xay dung CDLL
    // -------------------------------------------------------------------------

    /**
     * Tao CDLL size phan tu.
     * Dac diem: tail.next = head, head.prev = tail (vong tron).
     *
     * @return head cua CDLL
     */
    static CdllNode buildCDLL(int size) {
        CdllNode head = new CdllNode(0);
        CdllNode current = head;
        for (int i = 1; i < size; i++) {
            CdllNode node = new CdllNode(i);
            node.prev = current;
            current.next = node;
            current = node;
        }
        // Khep vong: tail.next = head, head.prev = tail
        current.next = head;
        head.prev = current;
        return head;
    }

    // -------------------------------------------------------------------------
    // Do DLL next()
    // -------------------------------------------------------------------------

    /**
     * Gia lap TOTAL_STEPS buoc next() tren DLL voi RepeatMode.ALL.
     * Moi lan o tail phai check null va nhay ve head -- day la branch.
     *
     * @param head head cua DLL
     * @param size so phan tu (de tim tail)
     * @return long[2] { branchCount, timeNs }
     */
    static long[] benchmarkDLL(DllNode head, int size) {
        // Tim tail
        DllNode tail = head;
        while (tail.next != null) {
            tail = tail.next;
        }

        long branchCount = 0;
        DllNode current = head;

        long startTime = System.nanoTime();
        for (int step = 0; step < TOTAL_STEPS; step++) {
            branchCount++; // moi buoc deu phai check: if (current.next == null)?
            if (current.next == null) {
                current = head; // het list, quay lai dau (RepeatMode.ALL)
            } else {
                current = current.next;
            }
        }
        long endTime = System.nanoTime();

        return new long[] { branchCount, endTime - startTime };
    }

    // -------------------------------------------------------------------------
    // Do CDLL next()
    // -------------------------------------------------------------------------

    /**
     * Gia lap TOTAL_STEPS buoc next() tren CDLL voi RepeatMode.ALL.
     * Khong can check null -- tail.next = head tu dong.
     * branchCount luon = 0.
     *
     * @param head head cua CDLL
     * @return long[2] { branchCount, timeNs }
     */
    static long[] benchmarkCDLL(CdllNode head) {
        long branchCount = 0; // CDLL khong can branch, luon = 0
        CdllNode current = head;

        long startTime = System.nanoTime();
        for (int step = 0; step < TOTAL_STEPS; step++) {
            // Khong co if-check, chi don gian:
            current = current.next;
        }
        long endTime = System.nanoTime();

        return new long[] { branchCount, endTime - startTime };
    }

    // -------------------------------------------------------------------------
    // Do DLL prev()
    // -------------------------------------------------------------------------

    /**
     * Gia lap TOTAL_STEPS buoc prev() tren DLL voi RepeatMode.ALL.
     * Moi lan o head phai check null va nhay ve tail.
     *
     * @param head head cua DLL
     * @param size so phan tu
     * @return long[2] { branchCount, timeNs }
     */
    static long[] benchmarkDLLPrev(DllNode head, int size) {
        // Tim tail
        DllNode tail = head;
        while (tail.next != null) {
            tail = tail.next;
        }

        long branchCount = 0;
        DllNode current = tail; // bat dau tu tail de test prev()

        long startTime = System.nanoTime();
        for (int step = 0; step < TOTAL_STEPS; step++) {
            branchCount++; // moi buoc phai check: if (current.prev == null)?
            if (current.prev == null) {
                current = tail; // het list, quay lai cuoi (RepeatMode.ALL)
            } else {
                current = current.prev;
            }
        }
        long endTime = System.nanoTime();

        return new long[] { branchCount, endTime - startTime };
    }

    // -------------------------------------------------------------------------
    // Do CDLL prev()
    // -------------------------------------------------------------------------

    /**
     * Gia lap TOTAL_STEPS buoc prev() tren CDLL voi RepeatMode.ALL.
     * Khong can check null -- head.prev = tail tu dong.
     *
     * @param head head cua CDLL
     * @return long[2] { branchCount, timeNs }
     */
    static long[] benchmarkCDLLPrev(CdllNode head) {
        long branchCount = 0;
        CdllNode current = head;

        long startTime = System.nanoTime();
        for (int step = 0; step < TOTAL_STEPS; step++) {
            current = current.prev;
        }
        long endTime = System.nanoTime();

        return new long[] { branchCount, endTime - startTime };
    }

    // -------------------------------------------------------------------------
    // Warmup JVM
    // -------------------------------------------------------------------------

    /**
     * Chay mot so vong lap truoc khi do that de JIT compiler on dinh.
     * Neu khong warmup, lan do dau tien thuong bi cham bat thuong.
     */
    static void warmup() {
        CdllNode head = buildCDLL(100);
        CdllNode cur = head;
        for (int i = 0; i < 100_000; i++) {
            cur = cur.next;
        }
    }

    // -------------------------------------------------------------------------
    // In bang ket qua
    // -------------------------------------------------------------------------

    static void printHeader() {
        System.out.println();
        System.out.println("=================================================================");
        System.out.printf("%-8s  %-14s  %-14s  %-8s  %-10s  %-10s  %-8s%n",
                "Size", "DLL branches", "CDLL branches", "Red.%",
                "DLL (ms)", "CDLL (ms)", "Speedup");
        System.out.println("-----------------------------------------------------------------");
    }

    static void printRow(int size,
            long dllBranch, long cdllBranch,
            double dllMs, double cdllMs) {
        double reduction = 100.0 * (dllBranch - cdllBranch) / dllBranch;
        double speedup = dllMs / cdllMs;
        System.out.printf("%-8d  %-14d  %-14d  %-8.1f  %-10.2f  %-10.2f  %-8.2fx%n",
                size, dllBranch, cdllBranch, reduction, dllMs, cdllMs, speedup);
    }

    // -------------------------------------------------------------------------
    // MAIN
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("  RQ1 Benchmark -- DLL vs CDLL");
        System.out.println("  Steps per test : " + TOTAL_STEPS);
        System.out.println("  Repeat per size: " + REPEAT);
        System.out.println("  Mode           : RepeatMode.ALL (next + prev)");
        System.out.println("=================================================================");

        warmup();

        // ── next() benchmark ─────────────────────────────────────────────────
        System.out.println("\n[ next() -- RepeatMode.ALL ]");
        printHeader();

        for (int size : SIZES) {
            long totalDllBranch = 0, totalCdllBranch = 0;
            long totalDllNs = 0, totalCdllNs = 0;

            for (int r = 0; r < REPEAT; r++) {
                DllNode dllHead = buildDLL(size);
                CdllNode cdllHead = buildCDLL(size);

                long[] dllResult = benchmarkDLL(dllHead, size);
                long[] cdllResult = benchmarkCDLL(cdllHead);

                totalDllBranch += dllResult[0];
                totalCdllBranch += cdllResult[0];
                totalDllNs += dllResult[1];
                totalCdllNs += cdllResult[1];
            }

            long avgDllBranch = totalDllBranch / REPEAT;
            long avgCdllBranch = totalCdllBranch / REPEAT;
            double avgDllMs = totalDllNs / REPEAT / 1_000_000.0;
            double avgCdllMs = totalCdllNs / REPEAT / 1_000_000.0;

            printRow(size, avgDllBranch, avgCdllBranch, avgDllMs, avgCdllMs);
        }

        System.out.println("-----------------------------------------------------------------");

        // ── prev() benchmark ─────────────────────────────────────────────────
        System.out.println("\n[ prev() -- RepeatMode.ALL ]");
        printHeader();

        for (int size : SIZES) {
            long totalDllBranch = 0, totalCdllBranch = 0;
            long totalDllNs = 0, totalCdllNs = 0;

            for (int r = 0; r < REPEAT; r++) {
                DllNode dllHead = buildDLL(size);
                CdllNode cdllHead = buildCDLL(size);

                long[] dllResult = benchmarkDLLPrev(dllHead, size);
                long[] cdllResult = benchmarkCDLLPrev(cdllHead);

                totalDllBranch += dllResult[0];
                totalCdllBranch += cdllResult[0];
                totalDllNs += dllResult[1];
                totalCdllNs += cdllResult[1];
            }

            long avgDllBranch = totalDllBranch / REPEAT;
            long avgCdllBranch = totalCdllBranch / REPEAT;
            double avgDllMs = totalDllNs / REPEAT / 1_000_000.0;
            double avgCdllMs = totalCdllNs / REPEAT / 1_000_000.0;

            printRow(size, avgDllBranch, avgCdllBranch, avgDllMs, avgCdllMs);
        }

        System.out.println("-----------------------------------------------------------------");

        // ── Ket luan nhanh ───────────────────────────────────────────────────
        System.out.println();
        System.out.println("Ghi chu:");
        System.out.println("  DLL branches  : so lan phai check if(next==null) hoac if(prev==null)");
        System.out.println("  CDLL branches : luon = 0, khong can check (tail.next=head tu dong)");
        System.out.println("  Red.%         : % giam branch so voi DLL");
        System.out.println("  Speedup       : DLL_time / CDLL_time");
        System.out.println("  Playlist nho (size=10) cho thay su chenh lech ro nhat.");
        System.out.println("=================================================================");
    }
}