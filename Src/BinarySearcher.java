package Src;

public class BinarySearcher {

    /**
     * Thuật toán Tìm kiếm nhị phân theo Tên bài hát (Title)
     * Yêu cầu: Mảng playlist ĐÃ ĐƯỢC SẮP XẾP theo tiêu chí Title trước khi gọi hàm này.
     * 
     * @param playlist    Mảng các bài hát đã được sắp xếp
     * @param targetTitle Tên bài hát cần tìm
     * @return Chỉ số (index) của bài hát trong mảng nếu tìm thấy, ngược lại trả về -1
     */
    public static int binarySearchByTitle(Song[] playlist, String targetTitle) {
        if (playlist == null || playlist.length == 0) {
            return -1;
        }

        int left = 0;
        int right = playlist.length - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            int comparison = playlist[mid].getTitle().compareToIgnoreCase(targetTitle);

            if (comparison == 0) {
                return mid; // Tìm thấy bài hát
            } else if (comparison < 0) {
                left = mid + 1; // Tìm ở nửa phải (các bài hát có tên đứng sau targetTitle trong từ điển)
            } else {
                right = mid - 1; // Tìm ở nửa trái
            }
        }
        
        return -1; // Không tìm thấy
    }

    /**
     * Thuật toán Tìm kiếm nhị phân theo Tên Ca sĩ (Artist)
     * Yêu cầu: Mảng playlist ĐÃ ĐƯỢC SẮP XẾP theo tiêu chí Artist.
     * 
     * @param playlist     Mảng các bài hát đã được sắp xếp
     * @param targetArtist Tên ca sĩ cần tìm
     * @return Chỉ số (index) của bài hát đầu tiên tìm thấy, ngược lại trả về -1
     */
    public static int binarySearchByArtist(Song[] playlist, String targetArtist) {
        if (playlist == null || playlist.length == 0) {
            return -1;
        }

        int left = 0;
        int right = playlist.length - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            int comparison = playlist[mid].getArtist().compareToIgnoreCase(targetArtist);

            if (comparison == 0) {
                return mid; // Tìm thấy
            } else if (comparison < 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        
        return -1; // Không tìm thấy
    }
}
