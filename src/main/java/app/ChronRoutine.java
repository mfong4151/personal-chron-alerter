package app;

import java.time.LocalDate;

import app.utils.PromptUtils;


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
        final String prompt = PromptUtils.getStockGapPrompt();


        
        notifier.send(prompt);
    }
}
