package src.datastructure;

import src.model.Song;

public class BinarySearcher {

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
                return mid;
            } else if (comparison < 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        return -1;
    }

    public static Song searchByTitle(Song[] playlist, String targetTitle) {
        int index = binarySearchByTitle(playlist, targetTitle);
        if (index != -1) {
            return playlist[index];
        }
        return null;
    }

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
                return mid;
            } else if (comparison < 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        return -1;
    }

    public static Song searchByArtist(Song[] playlist, String targetArtist) {
        int index = binarySearchByArtist(playlist, targetArtist);
        if (index != -1) {
            return playlist[index];
        }
        return null;
    }
}
