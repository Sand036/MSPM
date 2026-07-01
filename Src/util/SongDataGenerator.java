package src.util;

import src.model.Song;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Utility class to generate mock data for Songs.
 * Generates 10,000 songs with unique titles, diverse durations,
 * and a shared pool of artists (some artists have multiple songs).
 */
public class SongDataGenerator {

    private static final String[] ADJECTIVES = {
        "Golden", "Silver", "Midnight", "Sunset", "Sunrise", "Ocean", "River", "Broken", 
        "Lost", "Silent", "Dancing", "Running", "Flying", "Fallen", "Rising", "Sweet", 
        "Bitter", "Wild", "Mystic", "Electric", "Dark", "Light", "Neon", "Cosmic", 
        "Lunar", "Solar", "Autumn", "Winter", "Spring", "Summer", "Empty", "Hidden", 
        "Secret", "Velvet", "Crystal", "Faded", "Dreamy", "Stormy", "Windy", "Rainy", 
        "Cloudy", "Starry", "Drifting", "Burning", "Glowing", "Chasing", "Whispering", 
        "Singing", "Screaming", "Laughing", "Crying", "Living", "Dying", "Waiting", 
        "Searching", "Finding", "Leaving", "Returning", "Forgotten", "Remembered",
        "Ethereal", "Serene", "Infinite", "Crimson", "Shadowy", "Luminous", "Haunted"
    };

    private static final String[] NOUNS = {
        "Heart", "Soul", "Dream", "Night", "Day", "Sky", "Star", "Moon", "Sun", "Rain", 
        "Wind", "Storm", "Fire", "Ice", "Water", "River", "Sea", "Ocean", "Wave", "Road", 
        "Path", "Bridge", "Gate", "Door", "Window", "Room", "House", "City", "Town", 
        "Street", "Forest", "Tree", "Flower", "Rose", "Garden", "Mountain", "Valley", 
        "Stone", "Dust", "Shadow", "Light", "Echo", "Whisper", "Song", "Melody", "Rhythm", 
        "Beat", "Sound", "Silence", "Voice", "Word", "Story", "Book", "Page", "Line", 
        "Picture", "Mirror", "Glass", "Gold", "Silver", "Firefly", "Phoenix", "Illusion"
    };

    private static final String[] VERBS = {
        "Chasing", "Finding", "Losing", "Watching", "Waiting For", "Listening To", 
        "Walking In", "Running Through", "Dreaming Of", "Sailing On", "Flying Above", 
        "Singing Under", "Dancing With", "Hiding From", "Remembering", "Forgetting"
    };

    private static final String[] ARTIST_PREFIXES = {
        "DJ", "MC", "Lil", "Big", "The", "Lady", "Sir", "Captain", "Agent", "Sister", "Brother"
    };

    private static final String[] ARTIST_NAMES = {
        "Luna", "Solaris", "Echo", "Nova", "Aero", "Zephyr", "Rhythm", "Vibe", "Phoenix", 
        "Shadow", "Storm", "Frost", "Blaze", "Orion", "Lyra", "Vega", "Sirius", "Atlas", 
        "Apex", "Zenith", "Pulse", "Sonic", "Beat", "Frequency", "Melody", "Harmony", 
        "Cascade", "Drift", "Glide", "Wave", "Tide", "Crest", "Apex", "Climax", "Fable", 
        "Myth", "Legend", "Ghost", "Specter", "Phantom", "Spirit", "Angel", "Demon"
    };

    /**
     * Generates a pool of unique artist names.
     * @param count number of artists to generate in the pool.
     * @return a list of unique artist names.
     */
    public static List<String> generateArtistPool(int count) {
        Set<String> artists = new HashSet<>();
        Random rand = new Random(42); // fixed seed for reproducibility

        while (artists.size() < count) {
            int type = rand.nextInt(3);
            String artist = switch (type) {
                case 0 -> // e.g. "DJ Luna"
                        ARTIST_PREFIXES[rand.nextInt(ARTIST_PREFIXES.length)] + " " + 
                        ARTIST_NAMES[rand.nextInt(ARTIST_NAMES.length)];
                case 1 -> // e.g. "Luna Nova"
                        ARTIST_NAMES[rand.nextInt(ARTIST_NAMES.length)] + " " + 
                        ARTIST_NAMES[rand.nextInt(ARTIST_NAMES.length)];
                default -> // e.g. "The Solaris"
                        "The " + ARTIST_NAMES[rand.nextInt(ARTIST_NAMES.length)];
            };
            artists.add(artist);
        }
        return new ArrayList<>(artists);
    }

    /**
     * Generates a list of 10,000 Song objects with the following properties:
     * - Unique ID (S00001 to S10000)
     * - Distinct/Unique Titles
     * - Shared Artist Pool (some songs have the same artist)
     * - Diverse Durations (between 120 and 480 seconds)
     */
    public static List<Song> generate10kSongs() {
        return generateSongs(10000, 400); // 10,000 songs, 400 unique artists
    }

    /**
     * Generates a list of Song objects.
     * @param songCount Number of songs to generate.
     * @param artistPoolSize Number of unique artists in the pool.
     * @return List of generated Song objects.
     */
    public static List<Song> generateSongs(int songCount, int artistPoolSize) {
        List<Song> songs = new ArrayList<>(songCount);
        List<String> artistPool = generateArtistPool(artistPoolSize);
        Set<String> generatedTitles = new HashSet<>();
        Random rand = new Random(1337); // fixed seed for reproducibility

        for (int i = 1; i <= songCount; i++) {
            String id = String.format("S%05d", i);
            String title = generateUniqueTitle(generatedTitles, rand);
            String artist = artistPool.get(rand.nextInt(artistPool.size()));
            
            // Duration between 120 seconds (2 mins) and 480 seconds (8 mins)
            int duration = 120 + rand.nextInt(361); 

            Song song = new Song(id, title, artist, duration);
            songs.add(song);
        }

        return songs;
    }

    /**
     * Helper to generate a unique song title.
     */
    private static String generateUniqueTitle(Set<String> existingTitles, Random rand) {
        while (true) {
            int pattern = rand.nextInt(4);
            String title = switch (pattern) {
                case 0 -> // "Golden Heart"
                        ADJECTIVES[rand.nextInt(ADJECTIVES.length)] + " " + 
                        NOUNS[rand.nextInt(NOUNS.length)];
                case 1 -> // "Chasing the Moon"
                        VERBS[rand.nextInt(VERBS.length)] + " the " + 
                        NOUNS[rand.nextInt(NOUNS.length)];
                case 2 -> // "Midnight Storm of Dust"
                        ADJECTIVES[rand.nextInt(ADJECTIVES.length)] + " " + 
                        NOUNS[rand.nextInt(NOUNS.length)] + " of " + 
                        NOUNS[rand.nextInt(NOUNS.length)];
                default -> // "Whispering Dreams in the Sea"
                        ADJECTIVES[rand.nextInt(ADJECTIVES.length)] + " " + 
                        NOUNS[rand.nextInt(NOUNS.length)] + " in the " + 
                        NOUNS[rand.nextInt(NOUNS.length)];
            };

            // If we run into collisions, we can append a random qualifier or try again
            if (!existingTitles.contains(title)) {
                existingTitles.add(title);
                return title;
            }

            // Fallback suffix if collision occurs and we want to keep trying
            String fallbackTitle = title + " (Mix " + (rand.nextInt(99) + 1) + ")";
            if (!existingTitles.contains(fallbackTitle)) {
                existingTitles.add(fallbackTitle);
                return fallbackTitle;
            }
        }
    }

    /**
     * Imports a list of songs from a CSV file.
     * @param filePath Path of the CSV file.
     * @return List of parsed Song objects.
     */
    public static List<Song> importFromCSV(String filePath) {
        List<Song> songs = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            return parseSongsFromReader(reader);
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error importing songs from CSV: " + e.getMessage());
        }
        return songs;
    }

    /**
     * Imports a list of songs from a CSV input stream on the classpath.
     * @param inputStream CSV input stream (caller may close after return).
     * @return List of parsed Song objects.
     */
    public static List<Song> importFromCSV(InputStream inputStream) {
        List<Song> songs = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return parseSongsFromReader(reader);
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error importing songs from CSV: " + e.getMessage());
        }
        return songs;
    }

    private static List<Song> parseSongsFromReader(BufferedReader reader) throws IOException {
        List<Song> songs = new ArrayList<>();
        reader.readLine(); // skip header
        String line;
        while ((line = reader.readLine()) != null) {
            int firstComma = line.indexOf(',');
            int lastComma = line.lastIndexOf(',');
            if (firstComma == -1 || lastComma == -1 || firstComma == lastComma) {
                continue;
            }

            String id = line.substring(0, firstComma);
            int duration = Integer.parseInt(line.substring(lastComma + 1).trim());

            String middle = line.substring(firstComma + 1, lastComma);
            String[] middleParts = middle.split("\",\"");
            if (middleParts.length >= 2) {
                String title = middleParts[0].startsWith("\"") ? middleParts[0].substring(1) : middleParts[0];
                String artist = middleParts[1].endsWith("\"") ? middleParts[1].substring(0, middleParts[1].length() - 1) : middleParts[1];

                title = title.replace("\"\"", "\"");
                artist = artist.replace("\"\"", "\"");

                Song song = new Song(id, title, artist, duration);
                songs.add(song);
            }
        }
        return songs;
    }

    /**
     * Exports a list of songs to a CSV file.
     * @param songs List of songs to export.
     * @param filePath Path of the destination CSV file.
     */
    public static void exportToCSV(List<Song> songs, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write CSV header
            writer.write("id,title,artist,duration\n");
            for (Song song : songs) {
                // Escape quotes if title or artist contain comma or quotes
                String titleEscaped = song.getTitle().replace("\"", "\"\"");
                String artistEscaped = song.getArtist().replace("\"", "\"\"");
                writer.write(String.format("%s,\"%s\",\"%s\",%d\n",
                        song.getId(), titleEscaped, artistEscaped, song.getDuration()));
            }
            System.out.println("Successfully exported " + songs.size() + " songs to " + filePath);
        } catch (IOException e) {
            System.err.println("Error exporting songs to CSV: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("Generating 10,000 Song objects...");
        List<Song> songs = generate10kSongs();
        
        System.out.println("\n--- Generation Statistics ---");
        System.out.println("Total Songs Generated: " + songs.size());
        
        Set<String> uniqueTitles = new HashSet<>();
        Set<String> uniqueArtists = new HashSet<>();
        long totalDuration = 0;
        int minDuration = Integer.MAX_VALUE;
        int maxDuration = Integer.MIN_VALUE;

        for (Song song : songs) {
            uniqueTitles.add(song.getTitle());
            uniqueArtists.add(song.getArtist());
            totalDuration += song.getDuration();
            if (song.getDuration() < minDuration) minDuration = song.getDuration();
            if (song.getDuration() > maxDuration) maxDuration = song.getDuration();
        }

        System.out.println("Unique Titles: " + uniqueTitles.size() + " / " + songs.size());
        System.out.println("Unique Artists: " + uniqueArtists.size());
        System.out.println("Average Duration: " + (totalDuration / songs.size()) + " seconds (" + 
                           String.format("%02d:%02d", (totalDuration / songs.size()) / 60, (totalDuration / songs.size()) % 60) + ")");
        System.out.println("Min Duration: " + minDuration + " seconds (" + String.format("%02d:%02d", minDuration / 60, minDuration % 60) + ")");
        System.out.println("Max Duration: " + maxDuration + " seconds (" + String.format("%02d:%02d", maxDuration / 60, maxDuration % 60) + ")");

        System.out.println("\n--- First 5 Songs ---");
        for (int i = 0; i < 5; i++) {
            System.out.println(songs.get(i));
        }

        System.out.println("\n--- Last 5 Songs ---");
        for (int i = songs.size() - 5; i < songs.size(); i++) {
            System.out.println(songs.get(i));
        }

        // Export to a CSV file in the project directory
        String csvPath = "songs_10k.csv";
        exportToCSV(songs, csvPath);

        // Verify CSV Import
        System.out.println("\n--- Testing CSV Import ---");
        List<Song> importedSongs = importFromCSV(csvPath);
        System.out.println("Total Songs Imported: " + importedSongs.size());
        if (importedSongs.size() == songs.size()) {
            System.out.println("Success! All 10,000 songs imported and parsed correctly from CSV. 🎉");
            System.out.println("First imported song: " + importedSongs.get(0));
        } else {
            System.out.println("Failed! CSV Import size mismatch.");
        }
    }
}
