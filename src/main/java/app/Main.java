package app;

import java.nio.file.Path;
import java.time.*;
import java.util.concurrent.Executors;


import app.clients.OpenAiApiClient;
import app.utils.Env;

import java.util.Map;

public class Main {
  private static final String DEFAULT_RUN_AT = "06:30";
  private static final String APP_TZ = "APP_TZ";
  private static final String OPENAI_API_KEY = "OPENAI_API_KEY"; 
  private static final String DISCORD_WEBHOOK_URL = "DISCORD_WEBHOOK_URL";
  private static final String DOT_ENV = "DOT_ENV";
  private static final String DEFAULT_DOT_ENV_PATH = "/home/pi/chron/.env";  
  private static final String RUN_AT = "RUN_AT";
  private static final String DEFAULT_TIMEZONE = "America/Los_Angeles";
  private static final String SCHEDULER = "scheduler";
  public static void main(String[] args) {
    Path envPath = Env.getDotEnvPath(DOT_ENV, DEFAULT_DOT_ENV_PATH);
    Map<String, String> dotenv = Env.load(envPath);

    ZoneId zone = ZoneId.of(
        Env.getOrDefault(APP_TZ, dotenv, DEFAULT_TIMEZONE));

    LocalTime runAt = LocalTime.parse(
        Env.getOrDefault(RUN_AT, dotenv, DEFAULT_RUN_AT));

    String openAiApiKey = Env.getRequired(OPENAI_API_KEY, dotenv);

    String webhookUrl = Env.getRequired(DISCORD_WEBHOOK_URL, dotenv);


    System.out.println(webhookUrl);
    System.out.println(openAiApiKey);
    
    DiscordNotifier notifier = new DiscordNotifier(webhookUrl);
    OpenAiApiClient openAiApiClient = new OpenAiApiClient(openAiApiKey);
    ChronRoutine routine = new ChronRoutine(notifier, openAiApiClient);

    notifier.send("🟢 Pi alerts service started. Daily run at " + runAt + " " + zone);

    var exec = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r);
      t.setName(SCHEDULER);
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
