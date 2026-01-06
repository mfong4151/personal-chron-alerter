package app;

import java.time.LocalDate;

import com.openai.models.ChatModel;

import app.clients.OpenAiClient;
import app.utils.PromptUtils;


/**
* Executes a daily chron routine, contains the conditions for:
* Calling chatgpt to web scrape content
* Parsing the llm content into an intelligble discord message
* Packaging the message into something intelligble for discord to send to my private server
*/
public final class ChronRoutine{
    private final DiscordNotifier notifier;
    private final OpenAiClient openAiClient;

    public ChronRoutine(DiscordNotifier notifier, OpenAiClient openAiClient) {
        this.notifier = notifier;
        this.openAiClient = openAiClient;
    }

    public void run(LocalDate day) {
        final String prompt = PromptUtils.getStockGapPrompt();
        final String result = OpenAiClient.fromChatGptResponseToString( 
          openAiClient.getChatGPTResponse(prompt, ChatModel.GPT_5_CHAT_LATEST)
        );

        notifier.send(result);
    }
}
