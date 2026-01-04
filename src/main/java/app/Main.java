package app;

import java.time.*;
import java.util.concurrent.Executors;
import app.utils.Env;

public class Main {

  public static void main(String[] args) {

    var dotenv = Env.load(java.nio.file.Path.of(".env"));

    String webhookUrl = Env.get("DISCORD_WEBHOOK_URL", dotenv);

    ZoneId zone = ZoneId.of(
        Env.get("APP_TZ", dotenv) != null ? Env.get("APP_TZ", dotenv) : "America/Los_Angeles");

    String runAtStr = Env.get("RUN_AT", dotenv);
    LocalTime runAt = LocalTime.parse(runAtStr != null ? runAtStr : "09:00");

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
