package app;

import java.time.LocalDate;


/**
* Executes a daily chron routine, contains the conditions for:
* Calling chatgpt to web scrape content
* Parsing the llm content into an intelligble discord message
* Packaging the message into something intelligble for discord to send to my private server
*/
public final class ChronRoutine{
    private final DiscordNotifier notifier;

    public ChronRoutine(DiscordNotifier notifier) {
        this.notifier = notifier;
    }

    public void run(LocalDate day) {
        // MVP: placeholder logic
        double value = 92.3;
        double threshold = 90.0;

        String msg = (value > threshold)
                ? "🚨 Daily check for " + day + ": value " + value + " > " + threshold
                : "✅ Daily check for " + day + ": all good.";

        notifier.send(msg);
    }
}
