package app;

import java.time.*;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) {
        String webhookUrl = System.getenv("DISCORD_WEBHOOK_URL");

        // Config (MVP): daily time + timezone
        ZoneId zone = ZoneId.of(System.getenv().getOrDefault("APP_TZ", "America/Los_Angeles"));
        LocalTime runAt = LocalTime.parse(System.getenv().getOrDefault("RUN_AT", "09:00"));

        DiscordNotifier notifier = new DiscordNotifier(webhookUrl);
        ChronRoutine routine = new ChronRoutine(notifier);

        // Optional: send a boot message so you know it started
        notifier.send("🟢 Pi alerts service started. Daily run at " + runAt + " " + zone);

        var exec = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("scheduler");
            t.setDaemon(false);
            return t;
        });

        DailyScheduler scheduler = new DailyScheduler(exec);

        scheduler.scheduleDaily(runAt, zone, () -> {
            LocalDate day = LocalDate.now(zone).minusDays(1); // "yesterday"
            routine.run(day);
        });
    }
}
