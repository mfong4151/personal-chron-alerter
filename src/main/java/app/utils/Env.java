package app.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class Env {
    private Env() {}

    public static Map<String, String> load(Path path) {
        Map<String, String> out = new HashMap<>();
        if (!Files.exists(path)) return out;

        try {
            for (String line : Files.readAllLines(path)) {
                String s = line.trim();
                if (s.isEmpty() || s.startsWith("#")) continue;

                int idx = s.indexOf('=');
                if (idx <= 0) continue;

                String key = s.substring(0, idx).trim();
                String val = s.substring(idx + 1).trim();

                // optional quotes
                if ((val.startsWith("\"") && val.endsWith("\"")) ||
                    (val.startsWith("'") && val.endsWith("'"))) {
                    val = val.substring(1, val.length() - 1);
                }
                out.put(key, val);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + path, e);
        }
        return out;
    }

    public static String get(String key, Map<String, String> dotenv) {
        String v = System.getenv(key);
        if (v != null && !v.isBlank()) return v;
        v = dotenv.get(key);
        return (v == null || v.isBlank()) ? null : v;
    }
}

