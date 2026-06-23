package src.datastructure;

import src.model.Song;

public class PlaylistSorter {

    public enum SortCriteria {
        ID, TITLE, ARTIST, PLAY_COUNT
    }

    public static void mergeSort(Song[] playlist, SortCriteria criteria) {
        if (playlist == null || playlist.length <= 1) {
            return;
        }
        Song[] temp = new Song[playlist.length];
        mergeSortHelper(playlist, temp, 0, playlist.length - 1, criteria);
    }

    public static Song[] sortById(Song[] songs) {
        if (songs == null) return null;
        Song[] copy = songs.clone();
        mergeSort(copy, SortCriteria.ID);
        return copy;
    }

    public static Song[] sortByTitle(Song[] songs) {
        if (songs == null) return null;
        Song[] copy = songs.clone();
        mergeSort(copy, SortCriteria.TITLE);
        return copy;
    }

    public static Song[] sortByArtist(Song[] songs) {
        if (songs == null) return null;
        Song[] copy = songs.clone();
        mergeSort(copy, SortCriteria.ARTIST);
        return copy;
    }

    public static Song[] sortByPlayCount(Song[] songs) {
        if (songs == null) return null;
        Song[] copy = songs.clone();
        mergeSort(copy, SortCriteria.PLAY_COUNT);
        return copy;
    }

    private static void mergeSortHelper(Song[] playlist, Song[] temp, int left, int right, SortCriteria criteria) {
        if (left < right) {
            int mid = left + (right - left) / 2;

            mergeSortHelper(playlist, temp, left, mid, criteria);
            mergeSortHelper(playlist, temp, mid + 1, right, criteria);

            merge(playlist, temp, left, mid, right, criteria);
        }
    }

    private static void merge(Song[] playlist, Song[] temp, int left, int mid, int right, SortCriteria criteria) {
        for (int i = left; i <= right; i++) {
            temp[i] = playlist[i];
        }

        int i = left;
        int j = mid + 1;
        int k = left;

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

        while (i <= mid) {
            playlist[k] = temp[i];
            k++;
            i++;
        }
    }

    private static int compare(Song s1, Song s2, SortCriteria criteria) {
        return switch (criteria) {
            case ID -> s1.getId().compareToIgnoreCase(s2.getId());
            case TITLE -> s1.getTitle().compareToIgnoreCase(s2.getTitle());
            case ARTIST -> s1.getArtist().compareToIgnoreCase(s2.getArtist());
            case PLAY_COUNT -> Integer.compare(s2.getPlayCount(), s1.getPlayCount());
            default -> 0;
        };
    }
}
