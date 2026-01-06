package app;

import java.time.*;
import java.util.concurrent.Executors;

import app.clients.OpenAiClient;
import app.utils.Env;
import app.utils.PromptUtils;

import java.util.Optional;

public class Main {
  private static final String DEFAULT_TIME = "09:00";
  // private static final String DEFAULT_TIME = "05:30";
  public static void main(String[] args) {

    var dotenv = Env.load(java.nio.file.Path.of(".env"));

    ZoneId zone = ZoneId.of(
        Env.get("APP_TZ", dotenv) != null ? Env.get("APP_TZ", dotenv) : "America/Los_Angeles");

    String runAtStr = Optional.ofNullable(Env.get("RUN_AT", dotenv)).map(envVar -> envVar).orElse(DEFAULT_TIME);

    LocalTime runAt = LocalTime.parse(runAtStr);

    DiscordNotifier notifier = new DiscordNotifier(Env.get("DISCORD_WEBHOOK_URL", dotenv));
    OpenAiClient openAiClient = new OpenAiClient();
    ChronRoutine routine = new ChronRoutine(notifier, openAiClient);

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
