package test;

import src.model.Song;
import src.model.Node;
import src.datastructure.CircularLinkedList;

import java.util.List;

public class CircularLinkedListTest {

    private static int totalTests = 0;
    private static int passedTests = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("       CIRCULAR DOUBLY LINKED LIST TEST          ");
        System.out.println("==================================================");

        try {
            testGroup1_InsertEnd();
            testGroup2_InsertHead();
            testGroup3_InsertAt();
            testGroup4_DeleteById();
            testGroup5_DeleteAt();
            testGroup6_Circularity();
            testGroup7_Navigation();
            testGroup8_FindById();
            testGroup9_ToListAndDisplay();

            System.out.println("\n==================================================");
            System.out.printf("RESULT: %d/%d tests PASSED.%n", passedTests, totalTests);
            if (passedTests == totalTests) {
                System.out.println("Status: ALL TESTS PASSED SUCCESSFULLY! 🎉");
            } else {
                System.out.println("Status: SOME TESTS FAILED. ❌");
            }
            System.out.println("==================================================");
        } catch (Exception e) {
            System.out.println("\n❌ Test execution interrupted due to unexpected exception: " + e.toString());
            for (StackTraceElement element : e.getStackTrace()) {
                System.out.println("\tat " + element);
            }
        }
    }

    private static void verify(String testName, boolean condition) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.printf("[PASS] %s%n", testName);
        } else {
            System.out.printf("[FAIL] %s%n", testName);
        }
    }

    // ─────────────────────────────────────────────
    // TEST GROUPS
    // ─────────────────────────────────────────────

    private static void testGroup1_InsertEnd() {
        System.out.println("\n--- Group 1: Insert at End (Tail) ---");
        CircularLinkedList list = new CircularLinkedList();
        verify("Empty list size is 0", list.getSize() == 0);
        verify("Empty list head is null", list.getHead() == null);

        Song s1 = new Song("S01", "Song A", "Artist A", 180);
        list.insert(s1);
        verify("Size after 1 insert is 1", list.getSize() == 1);
        verify("Head is not null", list.getHead() != null);
        verify("Head song matches s1", list.getHead().song.equals(s1));
        verify("Tail matches head when size is 1", list.getTail() == list.getHead());

        Song s2 = new Song("S02", "Song B", "Artist B", 200);
        list.insert(s2);
        verify("Size after 2 inserts is 2", list.getSize() == 2);
        verify("Tail song matches s2", list.getTail().song.equals(s2));
        verify("Head.next matches tail", list.getHead().next == list.getTail());
    }

    private static void testGroup2_InsertHead() {
        System.out.println("\n--- Group 2: Insert at Head ---");
        CircularLinkedList list = new CircularLinkedList();
        Song s1 = new Song("S01", "Song A", "Artist A", 180);
        Song s2 = new Song("S02", "Song B", "Artist B", 200);

        list.insertAtHead(s1);
        verify("Size after 1 head insert is 1", list.getSize() == 1);
        verify("Head matches s1", list.getHead().song.equals(s1));

        list.insertAtHead(s2);
        verify("Size after 2 head inserts is 2", list.getSize() == 2);
        verify("Head matches s2", list.getHead().song.equals(s2));
        verify("Tail matches s1", list.getTail().song.equals(s1));
    }

    private static void testGroup3_InsertAt() {
        System.out.println("\n--- Group 3: Insert At Index ---");
        CircularLinkedList list = new CircularLinkedList();
        Song s1 = new Song("S01", "Song A", "Artist A", 180);
        Song s2 = new Song("S02", "Song B", "Artist B", 200);
        Song s3 = new Song("S03", "Song C", "Artist C", 220);

        // Test insertAt(0) in empty list
        list.insertAt(s1, 0);
        verify("InsertAt(0) in empty list sets head", list.getHead().song.equals(s1));

        // Test insertAt(index >= size) -> inserts at tail
        list.insertAt(s2, 10);
        verify("InsertAt larger than size appends to tail", list.getTail().song.equals(s2));

        // Test insertAt(1) in the middle
        list.insertAt(s3, 1);
        verify("Size is now 3", list.getSize() == 3);
        verify("Element at index 1 is s3", list.getHead().next.song.equals(s3));
        verify("Element at index 2 is s2", list.getHead().next.next.song.equals(s2));
    }

    private static void testGroup4_DeleteById() {
        System.out.println("\n--- Group 4: Delete by Song ID ---");
        CircularLinkedList list = new CircularLinkedList();
        Song s1 = new Song("S01", "Song A", "Artist A", 180);
        Song s2 = new Song("S02", "Song B", "Artist B", 200);
        Song s3 = new Song("S03", "Song C", "Artist C", 220);

        list.insert(s1);
        list.insert(s2);
        list.insert(s3);

        // Delete non-existent
        verify("Delete non-existent returns false", !list.delete("S99"));

        // Delete middle node
        verify("Delete middle node (S02) returns true", list.delete("S02"));
        verify("Size drops to 2", list.getSize() == 2);
        verify("S01 next points to S03", list.getHead().next.song.equals(s3));

        // Delete head node
        verify("Delete head node (S01) returns true", list.delete("S01"));
        verify("Size drops to 1", list.getSize() == 1);
        verify("New head is S03", list.getHead().song.equals(s3));

        // Delete last node
        verify("Delete last node (S03) returns true", list.delete("S03"));
        verify("Size is 0", list.getSize() == 0);
        verify("Head is null", list.getHead() == null);
    }

    private static void testGroup5_DeleteAt() {
        System.out.println("\n--- Group 5: Delete At Index ---");
        CircularLinkedList list = new CircularLinkedList();
        Song s1 = new Song("S01", "Song A", "Artist A", 180);
        Song s2 = new Song("S02", "Song B", "Artist B", 200);
        Song s3 = new Song("S03", "Song C", "Artist C", 220);

        list.insert(s1);
        list.insert(s2);
        list.insert(s3);

        // Delete out of bounds
        verify("Delete out of bounds (<0) returns false", !list.deleteAt(-1));
        verify("Delete out of bounds (>=size) returns false", !list.deleteAt(3));

        // Delete at index 1 (middle)
        verify("Delete at index 1 returns true", list.deleteAt(1));
        verify("Size is 2", list.getSize() == 2);
        verify("S01 next is S03", list.getHead().next.song.equals(s3));
    }

    private static void testGroup6_Circularity() {
        System.out.println("\n--- Group 6: Circularity Property ---");
        CircularLinkedList list = new CircularLinkedList();
        Song s1 = new Song("S01", "Song A", "Artist A", 180);
        Song s2 = new Song("S02", "Song B", "Artist B", 200);

        list.insert(s1);
        list.insert(s2);

        // Verify circular pointers
        verify("tail.next == head", list.getTail().next == list.getHead());
        verify("head.prev == tail", list.getHead().prev == list.getTail());

        // Delete head and check circularity
        list.delete("S01");
        verify("After delete, tail.next == head", list.getTail().next == list.getHead());
        verify("After delete, head.prev == tail", list.getHead().prev == list.getTail());
    }

    private static void testGroup7_Navigation() {
        System.out.println("\n--- Group 7: Navigation (Next/Prev Node) ---");
        CircularLinkedList list = new CircularLinkedList();
        Song s1 = new Song("S01", "Song A", "Artist A", 180);
        Song s2 = new Song("S02", "Song B", "Artist B", 200);
        Song s3 = new Song("S03", "Song C", "Artist C", 220);

        list.insert(s1);
        list.insert(s2);
        list.insert(s3);

        Node current = list.getHead();
        verify("Current is S01", current.song.equals(s1));

        current = list.getNextNode(current);
        verify("Next of S01 is S02", current.song.equals(s2));

        current = list.getNextNode(current);
        verify("Next of S02 is S03", current.song.equals(s3));

        current = list.getNextNode(current);
        verify("Circular Next of S03 (tail) is S01 (head)", current.song.equals(s1));

        current = list.getPrevNode(current);
        verify("Circular Prev of S01 (head) is S03 (tail)", current.song.equals(s3));
    }

    private static void testGroup8_FindById() {
        System.out.println("\n--- Group 8: Find by ID ---");
        CircularLinkedList list = new CircularLinkedList();
        Song s1 = new Song("S01", "Song A", "Artist A", 180);
        Song s2 = new Song("S02", "Song B", "Artist B", 200);

        list.insert(s1);
        list.insert(s2);

        Node found = list.findById("S02");
        verify("Found node for S02 is not null", found != null);
        verify("Found node song matches S02", found.song.equals(s2));

        Node notFound = list.findById("S99");
        verify("S99 is not found (null)", notFound == null);
    }

    private static void testGroup9_ToListAndDisplay() {
        System.out.println("\n--- Group 9: To List & Display ---");
        CircularLinkedList list = new CircularLinkedList();
        Song s1 = new Song("S01", "Song A", "Artist A", 180);
        Song s2 = new Song("S02", "Song B", "Artist B", 200);

        list.insert(s1);
        list.insert(s2);

        List<Song> songs = list.toList();
        verify("toList() contains 2 elements", songs.size() == 2);
        verify("First song in list is S01", songs.get(0).equals(s1));
        verify("Second song in list is S02", songs.get(1).equals(s2));

        System.out.println("Displaying playlist below:");
        list.displayPlaylist();
    }
}
