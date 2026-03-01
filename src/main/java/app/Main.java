package app;

import java.nio.file.Path;
import java.time.*;
import java.util.concurrent.Executors;


import app.clients.OpenAiApiClient;
import app.routines.PremarketStockRoutine;
import app.routines.TodoChronRoutine;
import app.utils.Env;

import java.util.List;
import java.util.Map;


import static app.DailyScheduler.*;

public class Main {
  private static final String DEFAULT_RUN_AT = "06:30";
  private static final String APP_TZ = "APP_TZ";
  private static final String OPENAI_API_KEY = "OPENAI_API_KEY"; 
  private static final String RUN_AT = "RUN_AT";
  private static final String DEFAULT_TIMEZONE = "America/Los_Angeles";
  private static final String SCHEDULER = "scheduler";
  private static final String DISCORD_WEBHOOK_URL = "DISCORD_WEBHOOK_URL";
  private static final String TODO_WEBHOOK_URL = "TODO_WEBHOOK_URL";
  
  public static void main(String[] args) {
    Path envPath = Env.findEnvFile();
    Map<String, String> dotenv = Env.load(envPath);

    ZoneId zone = ZoneId.of(
        Env.getOrDefault(APP_TZ, dotenv, DEFAULT_TIMEZONE));

    LocalTime runAt = LocalTime.parse(
        Env.getOrDefault(RUN_AT, dotenv, DEFAULT_RUN_AT));

    String openAiApiKey = Env.getRequired(OPENAI_API_KEY, dotenv);

    
    DiscordNotifier notifier = new DiscordNotifier(Env.getRequired(DISCORD_WEBHOOK_URL, dotenv));
    DiscordNotifier todoNotifier = new DiscordNotifier(Env.getRequired(TODO_WEBHOOK_URL, dotenv));
    OpenAiApiClient openAiApiClient = new OpenAiApiClient(openAiApiKey);
    PremarketStockRoutine premarketStockRoutine = new PremarketStockRoutine(notifier, openAiApiClient);
    TodoChronRoutine todoRoutine = new TodoChronRoutine(todoNotifier);


    var exec = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r);
      t.setName(SCHEDULER);
      t.setDaemon(false);
      return t;
    });

    DailyScheduler scheduler = new DailyScheduler(exec);


    scheduler.scheduleDaily(runAt, zone, List.of(WEEKEND_DAYS), List.of(HOLIDAYS), () -> {
      LocalDate day = LocalDate.now(zone).minusDays(1); // "yesterday"
      premarketStockRoutine.run(day);
    });

    notifier.send("🟢 Sanity check log, pi is operational");

    scheduler.scheduleDaily(LocalTime.of(6, 0), zone, List.of(WEEKEND_DAYS), List.of(), () -> {
      LocalDate day = LocalDate.now(zone).minusDays(1);
      todoRoutine.run(day);
    });

    todoNotifier.send("Sanity check for todo routine");
  }
}
