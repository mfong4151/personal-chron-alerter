package app.clients;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Thin REST core for the Massive/Polygon market-data API. Ported from the
 * rvol-scanner Python client: apiKey query-param auth, timeout, and retry with
 * exponential backoff + jitter on 429/5xx.
 */
public final class MassiveApiClient {

  public static final String BASE_URL = "https://api.polygon.io";

  private static final int MAX_RETRIES = 5;
  private static final double BACKOFF_BASE_SECONDS = 0.5;
  private static final double BACKOFF_CAP_SECONDS = 30.0;
  private static final Set<Integer> RETRYABLE_STATUSES = Set.of(429, 500, 502, 503, 504);

  private final HttpClient client = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(30))
      .build();
  private final ObjectMapper mapper = new ObjectMapper();
  private final String apiKey;

  public MassiveApiClient(String apiKey) {
    if (apiKey == null || apiKey.isBlank()) {
      throw new IllegalArgumentException("MASSIVE_API_KEY is missing/blank");
    }
    this.apiKey = apiKey;
  }

  /** GET a path (interpolated onto BASE_URL), returning the parsed JSON body. */
  public JsonNode get(String path) {
    String sep = path.contains("?") ? "&" : "?";
    URI uri = URI.create(BASE_URL + path + sep + "apiKey="
        + URLEncoder.encode(apiKey, StandardCharsets.UTF_8));

    HttpRequest req = HttpRequest.newBuilder()
        .uri(uri)
        .timeout(Duration.ofSeconds(30))
        .GET()
        .build();

    RuntimeException lastError = null;
    for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
      try {
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        int status = resp.statusCode();

        if (RETRYABLE_STATUSES.contains(status)) {
          lastError = new MassiveApiException(status + " from " + path + ": " + snippet(resp.body()));
          if (attempt < MAX_RETRIES) {
            sleepBackoff(attempt, resp.headers().firstValue("Retry-After").orElse(null));
            continue;
          }
          throw lastError;
        }
        if (status >= 400) {
          throw new MassiveApiException(status + " from " + path + ": " + snippet(resp.body()));
        }
        return mapper.readTree(resp.body());
      } catch (MassiveApiException e) {
        throw e;
      } catch (Exception e) {
        lastError = new MassiveApiException("Request to " + path + " failed", e);
        if (attempt < MAX_RETRIES) {
          sleepBackoff(attempt, null);
          continue;
        }
        throw lastError;
      }
    }
    throw new MassiveApiException("Exhausted retries for " + path, lastError);
  }

  private static void sleepBackoff(int attempt, String retryAfter) {
    double delay;
    if (retryAfter != null) {
      try {
        delay = Double.parseDouble(retryAfter);
      } catch (NumberFormatException e) {
        delay = BACKOFF_BASE_SECONDS * Math.pow(2, attempt);
      }
    } else {
      delay = BACKOFF_BASE_SECONDS * Math.pow(2, attempt);
    }
    delay = Math.min(delay, BACKOFF_CAP_SECONDS);
    delay += ThreadLocalRandom.current().nextDouble(0, BACKOFF_BASE_SECONDS);
    try {
      Thread.sleep((long) (delay * 1000));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private static String snippet(String body) {
    if (body == null) {
      return "";
    }
    return body.length() > 200 ? body.substring(0, 200) : body;
  }

  /** Raised on non-retryable errors or exhausted retries. */
  public static final class MassiveApiException extends RuntimeException {
    public MassiveApiException(String message) {
      super(message);
    }

    public MassiveApiException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
