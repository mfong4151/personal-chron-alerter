package app.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;

public final class PromptUtils {

  public static final String STOCK_ALERTS_PROMPT_PATH = "prompts/stockAlerts.txt"; 
  public static final String STOCK_GAP_ALERTS_PROMPT_PATH = "prompts/stockAlerts.txt"; 
  public static final String STOCK_RESEARCH_ROLE_PATH = "prompts/stockResearcherRole.txt"; 
  public static final String LINE_BREAK = "\n";

  public static String loadResource(String path) {
    try (InputStream is = PromptUtils.class.getClassLoader().getResourceAsStream(path)) {
      if (is == null)
        throw new IllegalArgumentException("Missing resource: " + path);
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  // Very small “template engine”: replaces {key} with values.get("key")
  public static String render(String template, Map<String, ?> values) {
    String out = template;
    for (var e : values.entrySet()) {
      out = out.replace("{" + e.getKey() + "}", String.valueOf(e.getValue()));
    }
    return out;
  }

  public static String getStockGapPrompt(){

    String roleText = loadResource(STOCK_RESEARCH_ROLE_PATH);
    String promptText = String.format(loadResource(STOCK_ALERTS_PROMPT_PATH), LocalDate.now().toString() );
    return String.join(LINE_BREAK, roleText, promptText);
  }
}
