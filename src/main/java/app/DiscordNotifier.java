package app;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Takes the message and sends it to the discord api
 */
public final class DiscordNotifier {
    private final HttpClient client = HttpClient.newHttpClient();
    private final URI webhookUri;

    public DiscordNotifier(String webhookUrl) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            throw new IllegalArgumentException("DISCORD_WEBHOOK_URL is missing/blank");
        }
        this.webhookUri = URI.create(webhookUrl);
    }

    public void send(String content) {
        String json = "{\"content\":\"" + escape(content) + "\"}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(webhookUri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 300) {
                throw new RuntimeException("Discord webhook failed: " + resp.statusCode() + " " + resp.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to send Discord message", e);
        }
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}
