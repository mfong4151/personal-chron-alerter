package app.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Env {
  private Env() {
  }

  private static final Pattern VAR_PATTERN = Pattern
      .compile("\\$\\{([A-Za-z_][A-Za-z0-9_]*)\\}|\\$([A-Za-z_][A-Za-z0-9_]*)");

  /** Loads KEY=VALUE pairs from a .env file. Missing file => empty map. */
  public static Map<String, String> load(Path path) {
    if (path == null)
      return Map.of();
    if (!Files.exists(path))
      return Map.of();

    Map<String, String> out = new LinkedHashMap<>();
    List<String> lines;
    try {
      lines = Files.readAllLines(path, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException("Failed to read env file: " + path.toAbsolutePath(), e);
    }

    for (int i = 0; i < lines.size(); i++) {
      String raw = lines.get(i);
      String s = raw.trim();
      if (s.isEmpty())
        continue;
      if (s.startsWith("#") || s.startsWith(";"))
        continue;

      // allow "export KEY=VALUE"
      if (s.startsWith("export ")) {
        s = s.substring("export ".length()).trim();
      }

      int idxEq = s.indexOf('=');
      int idxColon = s.indexOf(':');

      int idx;
      if (idxEq >= 0 && idxColon >= 0)
        idx = Math.min(idxEq, idxColon);
      else
        idx = Math.max(idxEq, idxColon);

      if (idx <= 0) {
        // ignore malformed line
        continue;
      }

      String key = s.substring(0, idx).trim();
      if (!isValidKey(key))
        continue;

      String val = s.substring(idx + 1).trim();

      // Strip inline comments, but only if they're not inside quotes.
      val = stripInlineComment(val);

      // Unquote if quoted
      val = unquote(val);

      out.put(key, val);
    }

    return Collections.unmodifiableMap(out);
  }

  /**
   * Gets key from OS env first, then dotenv map.
   * Returns null if missing/blank.
   */
  public static String get(String key, Map<String, String> dotenv) {
    String v = System.getenv(key);
    if (isNonBlank(v))
      return v;

    if (dotenv != null) {
      v = dotenv.get(key);
      if (isNonBlank(v))
        return v;
    }
    return null;
  }

  /** Like get(), but expands $VARS using OS env first, then dotenv. */
  public static String getExpanded(String key, Map<String, String> dotenv) {
    String v = get(key, dotenv);
    if (v == null)
      return null;
    return expandVars(v, dotenv);
  }

  public static String getOrDefault(String key, Map<String, String> dotenv, String defaultValue) {
    String v = getExpanded(key, dotenv);
    return v != null ? v : defaultValue;
  }

  public static String getRequired(String key, Map<String, String> dotenv) {
    String v = getExpanded(key, dotenv);
    if (v == null) {
      throw new IllegalStateException(
          "Missing required environment variable: " + key +
              " (set it in the OS env or in your .env file)");
    }
    return v;
  }

  /**
   * Reads an env var pointing to a dotenv file path. If absent, uses defaultPath.
   * If defaultPath is relative, it's resolved against the current working dir.
   */
  public static Path getDotEnvPath(String envVarName, String defaultPath) {
    String p = System.getenv(envVarName);
    if (!isNonBlank(p))
      p = defaultPath;
    Path path = Path.of(p);
    return path.isAbsolute() ? path : path.toAbsolutePath().normalize();
  }

  // ---------------- helpers ----------------

  private static boolean isNonBlank(String s) {
    return s != null && !s.isBlank();
  }

  private static boolean isValidKey(String key) {
    if (key == null || key.isBlank())
      return false;
    // dotenv keys are usually A-Z0-9_ ; allow dots for convenience too
    for (int i = 0; i < key.length(); i++) {
      char c = key.charAt(i);
      boolean ok = (c >= 'A' && c <= 'Z')
          || (c >= 'a' && c <= 'z')
          || (c >= '0' && c <= '9')
          || c == '_' || c == '.' || c == '-';
      if (!ok)
        return false;
    }
    return true;
  }

  private static String stripInlineComment(String val) {
    if (val == null)
      return null;
    boolean inSingle = false;
    boolean inDouble = false;

    for (int i = 0; i < val.length(); i++) {
      char c = val.charAt(i);
      if (c == '\'' && !inDouble)
        inSingle = !inSingle;
      else if (c == '"' && !inSingle)
        inDouble = !inDouble;

      if (!inSingle && !inDouble && (c == '#' || c == ';')) {
        // comment starts here
        return val.substring(0, i).trim();
      }
    }
    return val.trim();
  }

  private static String unquote(String val) {
    if (val == null)
      return null;
    String s = val.trim();
    if (s.length() >= 2) {
      if ((s.startsWith("\"") && s.endsWith("\"")) ||
          (s.startsWith("'") && s.endsWith("'"))) {
        s = s.substring(1, s.length() - 1);
      }
    }
    return s;
  }

  private static String expandVars(String input, Map<String, String> dotenv) {
    Matcher m = VAR_PATTERN.matcher(input);
    StringBuilder sb = new StringBuilder();
    int last = 0;

    while (m.find()) {
      sb.append(input, last, m.start());
      String name = (m.group(1) != null) ? m.group(1) : m.group(2);

      // Expansion precedence: OS env first, then dotenv, else empty string
      String repl = System.getenv(name);
      if (!isNonBlank(repl) && dotenv != null)
        repl = dotenv.get(name);
      if (repl == null)
        repl = "";

      sb.append(repl);
      last = m.end();
    }

    sb.append(input, last, input.length());
    return sb.toString();
  }
}
