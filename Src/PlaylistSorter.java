package Src;

public class PlaylistSorter {
    
    // Enum để xác định tiêu chí sắp xếp (Tên bài, Ca sĩ, Lượt nghe)
    public enum SortCriteria {
        TITLE, ARTIST, PLAY_COUNT
    }

    /**
     * Triển khai thuật toán Merge Sort để sắp xếp mảng bài hát
     * @param playlist Mảng các bài hát (Song)
     * @param criteria Tiêu chí sắp xếp
     */
    public static void mergeSort(Song[] playlist, SortCriteria criteria) {
        if (playlist == null || playlist.length <= 1) {
            return;
        }
        Song[] temp = new Song[playlist.length];
        mergeSortHelper(playlist, temp, 0, playlist.length - 1, criteria);
    }

    private static void mergeSortHelper(Song[] playlist, Song[] temp, int left, int right, SortCriteria criteria) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            
            // Chia nhỏ mảng thành 2 nửa
            mergeSortHelper(playlist, temp, left, mid, criteria);
            mergeSortHelper(playlist, temp, mid + 1, right, criteria);
            
            // Gộp 2 nửa đã sắp xếp lại với nhau
            merge(playlist, temp, left, mid, right, criteria);
        }
    }

    private static void merge(Song[] playlist, Song[] temp, int left, int mid, int right, SortCriteria criteria) {
        // Sao chép sang mảng phụ
        for (int i = left; i <= right; i++) {
            temp[i] = playlist[i];
        }

        int i = left;       // Con trỏ duyệt nửa trái
        int j = mid + 1;    // Con trỏ duyệt nửa phải
        int k = left;       // Con trỏ lưu vào mảng chính

        // So sánh và gộp
        while (i <= mid && j <= right) {
            if (compare(temp[i], temp[j], criteria) <= 0) {
                playlist[k] = temp[i];
                i++;
            } else {
                playlist[k] = temp[j];
                j++;
            }
            k++;
        }

        // Sao chép các phần tử còn lại của nửa trái (nếu có)
        while (i <= mid) {
            playlist[k] = temp[i];
            k++;
            i++;
        }
    }

    /**
     * Hàm so sánh 2 bài hát dựa trên tiêu chí
     */
    private static int compare(Song s1, Song s2, SortCriteria criteria) {
        switch (criteria) {
            case TITLE:
                return s1.getTitle().compareToIgnoreCase(s2.getTitle());
            case ARTIST:
                return s1.getArtist().compareToIgnoreCase(s2.getArtist());
            case PLAY_COUNT:
                // Sắp xếp giảm dần (từ lượt nghe cao nhất xuống thấp nhất)
                return Integer.compare(s2.getPlayCount(), s1.getPlayCount());
            default:
                return 0;
        }
    }
}
