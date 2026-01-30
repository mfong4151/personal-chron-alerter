package app;

import java.time.LocalDate;

import com.openai.models.ChatModel;

import app.clients.OpenAiApiClient;
import app.utils.PromptUtils;


/**
* Executes a daily chron routine, contains the conditions for:
* Calling chatgpt to web scrape content
* Parsing the llm content into an intelligble discord message
* Packaging the message into something intelligble for discord to send to my private server
*/
public final class ChronRoutine{
    private final DiscordNotifier notifier;
    private final OpenAiApiClient openAiApiClient;

    public ChronRoutine(DiscordNotifier notifier, OpenAiApiClient openAiApiClient) {
        this.notifier = notifier;
        this.openAiApiClient = openAiApiClient;
    }

    public void run(LocalDate day) {
        final String prompt = PromptUtils.getStockGapPrompt();
        final String result = OpenAiApiClient.fromChatGptResponseToString( 
          openAiApiClient.getChatGPTResponse(prompt, ChatModel.GPT_5_2)
        );

        notifier.send(result);
    }
}
