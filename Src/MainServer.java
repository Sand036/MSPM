package src;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import src.model.Song;
import src.service.PlaylistManager;
import src.datastructure.HistoryStack;
import src.datastructure.PlaylistSorter;
import src.datastructure.BinarySearcher;
import src.util.SongDataGenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.nio.charset.StandardCharsets;

public class MainServer {

    private static final PlaylistManager playlistManager = new PlaylistManager();
    private static final HistoryStack historyStack = new HistoryStack(100);
    // CSV library: all songs loaded from CSV, available for user to browse and add
    private static List<Song> csvLibrary = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        System.out.println("Loading CSV song library...");
        csvLibrary = loadCsvLibrary();
        System.out.println("CSV library loaded: " + csvLibrary.size() + " songs available.");
        System.out.println("Playlist is empty. Users can add songs from the library.");

        HttpServer server = HttpServer.create(new InetSocketAddress(8085), 0);

        server.createContext("/playlist", exchange -> {
            cors(exchange);
            switch (exchange.getRequestMethod()) {
                case "GET" -> sendResponse(exchange, 200, songsToJson(playlistManager.getAllSongs()));
                case "OPTIONS" -> exchange.sendResponseHeaders(200, -1);
                default -> exchange.sendResponseHeaders(405, -1);
            }
        });

        server.createContext("/song", exchange -> {
            cors(exchange);
            switch (exchange.getRequestMethod()) {
                case "POST" -> {
                    try {
                        String body = readBody(exchange.getRequestBody());
                        String id = extractJsonString(body, "id");
                        String title = extractJsonString(body, "title");
                        String artist = extractJsonString(body, "artist");
                        int duration = extractJsonInt(body, "duration");

                        Song s = new Song(id, title, artist, duration);
                        playlistManager.addSong(s);
                        sendResponse(exchange, 200, songToJson(s));
                    } catch (IOException | NumberFormatException e) {
                        sendResponse(exchange, 400, "{\"error\":\"Invalid request\"}");
                    }
                }
                case "DELETE" -> {
                    String query = exchange.getRequestURI().getQuery(); // id=...
                    if (query != null && query.startsWith("id=")) {
                        String id = query.substring(3);
                        playlistManager.removeSong(id);
                        sendResponse(exchange, 200, "{\"success\":true}");
                    } else {
                        sendResponse(exchange, 400, "{\"error\":\"Missing id\"}");
                    }
                }
                case "OPTIONS" -> exchange.sendResponseHeaders(200, -1);
                default -> exchange.sendResponseHeaders(405, -1);
            }
        });

        server.createContext("/next", exchange -> {
            cors(exchange);
            switch (exchange.getRequestMethod()) {
                case "POST" -> {
                    Song current = playlistManager.getCurrentSong();
                    if (current != null) {
                        historyStack.push(current);
                    }
                    Song next = playlistManager.nextSong();
                    sendResponse(exchange, 200, songToJson(next));
                }
                case "OPTIONS" -> exchange.sendResponseHeaders(200, -1);
                default -> exchange.sendResponseHeaders(405, -1);
            }
        });

        server.createContext("/previous", exchange -> {
            cors(exchange);
            switch (exchange.getRequestMethod()) {
                case "POST" -> {
                    Song current = playlistManager.getCurrentSong();
                    if (current != null) {
                        historyStack.push(current);
                    }
                    Song prev = playlistManager.prevSong();
                    sendResponse(exchange, 200, songToJson(prev));
                }
                case "OPTIONS" -> exchange.sendResponseHeaders(200, -1);
                default -> exchange.sendResponseHeaders(405, -1);
            }
        });

        server.createContext("/current-song", exchange -> {
            cors(exchange);
            switch (exchange.getRequestMethod()) {
                case "GET" -> {
                    Song current = playlistManager.getCurrentSong();
                    sendResponse(exchange, 200, songToJson(current));
                }
                case "OPTIONS" -> exchange.sendResponseHeaders(200, -1);
                default -> exchange.sendResponseHeaders(405, -1);
            }
        });

        server.createContext("/play", exchange -> {
            cors(exchange);
            switch (exchange.getRequestMethod()) {
                case "POST" -> {
                    Song current = playlistManager.getCurrentSong();
                    if (current == null && !playlistManager.isEmpty()) {
                        List<Song> all = playlistManager.getAllSongs();
                        if (!all.isEmpty()) {
                            playlistManager.setCurrentSong(all.get(0).getId());
                            current = playlistManager.getCurrentSong();
                        }
                    }
                    sendResponse(exchange, 200, songToJson(current));
                }
                case "OPTIONS" -> exchange.sendResponseHeaders(200, -1);
                default -> exchange.sendResponseHeaders(405, -1);
            }
        });

        server.createContext("/set-current", exchange -> {
            cors(exchange);
            switch (exchange.getRequestMethod()) {
                case "POST" -> {
                    try {
                        String body = readBody(exchange.getRequestBody());
                        String id = extractJsonString(body, "id");
                        boolean ok = playlistManager.setCurrentSong(id);
                        sendResponse(exchange, 200, "{\"success\":" + ok + "}");
                    } catch (IOException e) {
                        sendResponse(exchange, 400, "{\"error\":\"Invalid request\"}");
                    }
                }
                case "OPTIONS" -> exchange.sendResponseHeaders(200, -1);
                default -> exchange.sendResponseHeaders(405, -1);
            }
        });

        server.createContext("/shuffle", exchange -> {
            cors(exchange);
            switch (exchange.getRequestMethod()) {

                case "POST" -> {
                    Song current = playlistManager.getCurrentSong();
                    String currentId = (current != null) ? current.getId() : null;

                    List<Song> all = playlistManager.getAllSongs();
                    Collections.shuffle(all);
                    playlistManager.clearPlaylist();
                    for (Song s : all) {
                        playlistManager.addSong(s);
                    }

                    if (currentId != null) {
                        playlistManager.setCurrentSong(currentId);
                    }

                    sendResponse(exchange, 200, songsToJson(playlistManager.getAllSongs()));
                }
                case "OPTIONS" -> exchange.sendResponseHeaders(200, -1);
                default -> exchange.sendResponseHeaders(405, -1);
            }
        });

        server.createContext("/repeat", exchange -> {
            cors(exchange);
            switch (exchange.getRequestMethod()) {
                case "POST" -> {
                    try {
                        String body = readBody(exchange.getRequestBody());
                        String modeStr = extractJsonString(body, "mode");
                        PlaylistManager.RepeatMode mode = PlaylistManager.RepeatMode
                                .valueOf(modeStr.toUpperCase().trim());
                        playlistManager.setRepeatMode(mode);
                        sendResponse(exchange, 200, "{\"success\":true}");
                    } catch (IOException | IllegalArgumentException | NullPointerException e) {
                        sendResponse(exchange, 400, "{\"error\":\"Invalid repeat mode\"}");
                    }
                }
                case "OPTIONS" -> exchange.sendResponseHeaders(200, -1);
                default -> exchange.sendResponseHeaders(405, -1);
            }
        });

        server.createContext("/history", exchange -> {
            cors(exchange);
            switch (exchange.getRequestMethod()) {
                case "GET" -> {
                    Song[] hist = historyStack.getHistory();
                    List<Song> hList = new ArrayList<>();
                    for (Song s : hist) {
                        if (s != null)
                            hList.add(s);
                    }
                    sendResponse(exchange, 200, songsToJson(hList));
                }
                case "OPTIONS" -> exchange.sendResponseHeaders(200, -1);
                default -> exchange.sendResponseHeaders(405, -1);
            }
        });

        server.createContext("/search", exchange -> {
            cors(exchange);
            switch (exchange.getRequestMethod()) {
                case "GET" -> {
                    String query = exchange.getRequestURI().getQuery();
                    if (query != null && query.startsWith("title=")) {
                        String title = java.net.URLDecoder.decode(query.substring(6), StandardCharsets.UTF_8);

                        Song[] arr = playlistManager.getSongsArray();
                        Song[] sorted = PlaylistSorter.sortByTitle(arr);
                        Song found = BinarySearcher.searchByTitle(sorted, title);

                        if (found != null) {
                            List<Song> res = new ArrayList<>();
                            res.add(found);
                            sendResponse(exchange, 200, songsToJson(res));
                        } else {
                            // fallback linear search for partial matches
                            List<Song> res = new ArrayList<>();
                            for (Song s : arr) {
                                if (s.getTitle().toLowerCase().contains(title.toLowerCase())) {
                                    res.add(s);
                                }
                            }
                            sendResponse(exchange, 200, songsToJson(res));
                        }
                    } else {
                        sendResponse(exchange, 200, "[]");
                    }
                }
                case "OPTIONS" -> exchange.sendResponseHeaders(200, -1);
                default -> exchange.sendResponseHeaders(405, -1);
            }
        });

        server.createContext("/sort", exchange -> {
            cors(exchange);
            switch (exchange.getRequestMethod()) {
                case "GET" -> {
                    String query = exchange.getRequestURI().getQuery();
                    String type = "id";
                    if (query != null && query.startsWith("type=")) {
                        type = query.substring(5);
                    }

                    Song[] arr = playlistManager.getSongsArray();
                    Song[] sorted;
                    if ("title".equalsIgnoreCase(type)) {
                        sorted = PlaylistSorter.sortByTitle(arr);
                    } else if ("artist".equalsIgnoreCase(type)) {
                        sorted = PlaylistSorter.sortByArtist(arr);
                    } else if ("playcount".equalsIgnoreCase(type)) {
                        sorted = PlaylistSorter.sortByPlayCount(arr);
                    } else {
                        sorted = PlaylistSorter.sortById(arr);
                    }

                    playlistManager.clearPlaylist();
                    if (sorted != null) {
                        for (Song s : sorted) {
                            if (s != null)
                                playlistManager.addSong(s);
                        }
                    }

                    sendResponse(exchange, 200, songsToJson(playlistManager.getAllSongs()));
                }
                case "OPTIONS" -> exchange.sendResponseHeaders(200, -1);
                default -> exchange.sendResponseHeaders(405, -1);
            }
        });

        // ─── CSV Library: Browse & Filter ────────────────────────────────
        server.createContext("/csv-songs", exchange -> {
            cors(exchange);
            String path = exchange.getRequestURI().getPath();

            if (path.equals("/csv-songs/add") && exchange.getRequestMethod().equals("POST")) {
                // Add specific songs by IDs
                try {
                    String body = readBody(exchange.getRequestBody());
                    // Parse JSON array of IDs: {"ids":["S00001","S00002"]}
                    String idsStr = extractJsonArrayString(body, "ids");
                    String[] ids = idsStr.split(",");
                    int added = 0;
                    for (String rawId : ids) {
                        String id = rawId.trim().replace("\"", "");
                        if (id.isEmpty())
                            continue;
                        // Check not already in playlist
                        if (playlistManager.findSongById(id) != null)
                            continue;
                        // Find in CSV library
                        for (Song s : csvLibrary) {
                            if (s.getId().equals(id)) {
                                playlistManager
                                        .addSong(new Song(s.getId(), s.getTitle(), s.getArtist(), s.getDuration()));
                                added++;
                                break;
                            }
                        }
                    }
                    sendResponse(exchange, 200,
                            "{\"added\":" + added + ",\"total\":" + playlistManager.getSize() + "}");
                } catch (IOException | NumberFormatException e) {
                    sendResponse(exchange, 400, "{\"error\":\"Invalid request\"}");
                }
            } else if (path.equals("/csv-songs/add-filtered") && exchange.getRequestMethod().equals("POST")) {
                // Add all songs matching the filter criteria
                try {
                    String body = readBody(exchange.getRequestBody());
                    String titleFilter = extractJsonString(body, "title");
                    String artistFilter = extractJsonString(body, "artist");
                    int minDur = extractJsonInt(body, "minDuration");
                    int maxDur = extractJsonInt(body, "maxDuration");
                    if (maxDur == 0)
                        maxDur = Integer.MAX_VALUE;

                    int added = 0;
                    for (Song s : csvLibrary) {
                        if (!titleFilter.isEmpty() && !s.getTitle().toLowerCase().contains(titleFilter.toLowerCase()))
                            continue;
                        if (!artistFilter.isEmpty()
                                && !s.getArtist().toLowerCase().contains(artistFilter.toLowerCase()))
                            continue;
                        if (s.getDuration() < minDur || s.getDuration() > maxDur)
                            continue;
                        if (playlistManager.findSongById(s.getId()) != null)
                            continue;
                        playlistManager.addSong(new Song(s.getId(), s.getTitle(), s.getArtist(), s.getDuration()));
                        added++;
                    }
                    sendResponse(exchange, 200,
                            "{\"added\":" + added + ",\"total\":" + playlistManager.getSize() + "}");
                } catch (IOException | NumberFormatException e) {
                    sendResponse(exchange, 400, "{\"error\":\"Invalid request\"}");
                }
            } else if (exchange.getRequestMethod().equals("GET")) {
                // Browse CSV library with optional filters
                String query = exchange.getRequestURI().getQuery();
                String titleFilter = getQueryParam(query, "title");
                String artistFilter = getQueryParam(query, "artist");
                int minDur = parseIntSafe(getQueryParam(query, "minDuration"), 0);
                int maxDur = parseIntSafe(getQueryParam(query, "maxDuration"), Integer.MAX_VALUE);
                int page = parseIntSafe(getQueryParam(query, "page"), 1);
                int pageSize = parseIntSafe(getQueryParam(query, "pageSize"), 50);

                List<Song> filtered = new ArrayList<>();
                for (Song s : csvLibrary) {
                    if (!titleFilter.isEmpty() && !s.getTitle().toLowerCase().contains(titleFilter.toLowerCase()))
                        continue;
                    if (!artistFilter.isEmpty() && !s.getArtist().toLowerCase().contains(artistFilter.toLowerCase()))
                        continue;
                    if (s.getDuration() < minDur || s.getDuration() > maxDur)
                        continue;
                    filtered.add(s);
                }

                int totalFiltered = filtered.size();
                int start = (page - 1) * pageSize;
                int end = Math.min(start + pageSize, totalFiltered);
                List<Song> pageList = (start < totalFiltered) ? filtered.subList(start, end) : new ArrayList<>();

                // Collect unique artists for filter dropdown
                java.util.Set<String> artistSet = new java.util.TreeSet<>();
                for (Song s : csvLibrary) {
                    artistSet.add(s.getArtist());
                }

                StringBuilder sb = new StringBuilder();
                sb.append("{\"songs\":").append(songsToJsonWithInPlaylist(pageList));
                sb.append(",\"total\":").append(totalFiltered);
                sb.append(",\"page\":").append(page);
                sb.append(",\"pageSize\":").append(pageSize);
                sb.append(",\"totalPages\":").append((int) Math.ceil((double) totalFiltered / pageSize));
                sb.append(",\"artists\":[");
                int ai = 0;
                for (String a : artistSet) {
                    if (ai > 0)
                        sb.append(",");
                    sb.append("\"").append(escapeJson(a)).append("\"");
                    ai++;
                }
                sb.append("]}");
                sendResponse(exchange, 200, sb.toString());
            } else if (exchange.getRequestMethod().equals("OPTIONS")) {
                exchange.sendResponseHeaders(200, -1);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        });

        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }
            if (path.contains("..")) {
                exchange.sendResponseHeaders(403, -1);
                return;
            }
            String resourcePath = "/frontend" + path;
            InputStream tempIs = MainServer.class.getResourceAsStream(resourcePath);
            if (tempIs == null) {
                java.io.File file = new java.io.File("src/resources" + resourcePath);
                if (file.exists()) {
                    try {
                        tempIs = new java.io.FileInputStream(file);
                    } catch (java.io.FileNotFoundException e) {
                    }
                }
            }
            if (tempIs == null) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            try (InputStream is = tempIs) {
                byte[] bytes = is.readAllBytes();
                String contentType = contentTypeForPath(path);

                exchange.getResponseHeaders().add("Content-Type", contentType);
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Server started on http://localhost:8085");
    }

    private static List<Song> loadCsvLibrary() throws IOException {
        try (InputStream is = MainServer.class.getResourceAsStream("/songs_10k.csv")) {
            if (is != null) {
                return SongDataGenerator.importFromCSV(is);
            }
        }
        java.io.File file = new java.io.File("src/resources/songs_10k.csv");
        if (file.exists()) {
            return SongDataGenerator.importFromCSV("src/resources/songs_10k.csv");
        }
        return SongDataGenerator.importFromCSV("songs_10k.csv");
    }

    private static String contentTypeForPath(String path) {
        if (path.endsWith(".html"))
            return "text/html; charset=UTF-8";
        if (path.endsWith(".css"))
            return "text/css; charset=UTF-8";
        if (path.endsWith(".js"))
            return "application/javascript; charset=UTF-8";
        return "text/plain; charset=UTF-8";
    }

    private static void cors(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String songToJson(Song s) {
        if (s == null)
            return "null";
        return String.format("{\"id\":\"%s\",\"title\":\"%s\",\"artist\":\"%s\",\"duration\":%d}",
                escapeJson(s.getId()), escapeJson(s.getTitle()), escapeJson(s.getArtist()), s.getDuration());
    }

    private static String songsToJson(List<Song> songs) {
        if (songs == null)
            return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < songs.size(); i++) {
            if (songs.get(i) != null) {
                sb.append(songToJson(songs.get(i)));
                if (i < songs.size() - 1)
                    sb.append(",");
            }
        }
        // Remove trailing comma if last element was null
        if (sb.length() > 1 && sb.charAt(sb.length() - 1) == ',') {
            sb.setLength(sb.length() - 1);
        }
        sb.append("]");
        return sb.toString();
    }

    private static String escapeJson(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /** Convert songs to JSON, with an `inPlaylist` flag for each song */
    private static String songsToJsonWithInPlaylist(List<Song> songs) {
        if (songs == null)
            return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < songs.size(); i++) {
            Song s = songs.get(i);
            if (s == null)
                continue;
            boolean inPlaylist = playlistManager.findSongById(s.getId()) != null;
            if (sb.length() > 1)
                sb.append(",");
            sb.append(String.format(
                    "{\"id\":\"%s\",\"title\":\"%s\",\"artist\":\"%s\",\"duration\":%d,\"inPlaylist\":%b}",
                    escapeJson(s.getId()), escapeJson(s.getTitle()), escapeJson(s.getArtist()), s.getDuration(),
                    inPlaylist));
        }
        sb.append("]");
        return sb.toString();
    }

    /** Extract a JSON array value as raw string (without outer brackets) */
    private static String extractJsonArrayString(String json, String key) {
        String search = "\"" + key + "\":[";
        int start = json.indexOf(search);
        if (start == -1)
            return "";
        start += search.length();
        int end = json.indexOf("]", start);
        return end == -1 ? "" : json.substring(start, end);
    }

    /** Parse a query parameter from a URL query string */
    private static String getQueryParam(String query, String key) {
        if (query == null)
            return "";
        for (String param : query.split("&")) {
            String[] parts = param.split("=", 2);
            if (parts.length == 2 && parts[0].equals(key)) {
                return java.net.URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
            }
        }
        return "";
    }

    /** Safe int parsing with default */
    private static int parseIntSafe(String s, int defaultValue) {
        if (s == null || s.isEmpty())
            return defaultValue;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static String readBody(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private static String extractJsonString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1)
            return "";
        start += search.length();
        int end = json.indexOf("\"", start);
        return end == -1 ? "" : json.substring(start, end);
    }

    private static int extractJsonInt(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1)
            return 0;
        start += search.length();
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }
        if (start == end)
            return 0;
        return Integer.parseInt(json.substring(start, end));
    }
}
