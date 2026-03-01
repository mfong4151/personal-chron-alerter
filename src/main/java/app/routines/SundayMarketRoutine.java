package app.routines;

import java.time.LocalDate;

import app.DiscordNotifier;
import app.clients.OpenAiApiClient;
import app.utils.PromptUtils;

public class SundayMarketRoutine implements ChronRoutine {

    private final DiscordNotifier notifier; 
    private final OpenAiApiClient openAiApiClient;

    public SundayMarketRoutine(DiscordNotifier notifier, OpenAiApiClient openAiApiClient) {
      this.notifier = notifier;
      this.openAiApiClient = openAiApiClient;
    } 

    @Override
    public void run(LocalDate date) {
        // No-op for now, just a placeholder routine to test weekend exclusion

        final String prompt = PromptUtils.getSundayStoryResearchPrompt() + date.toString();
        final String result = OpenAiApiClient.fromChatGptResponseToString( 
          openAiApiClient.getChatGPTResponse(prompt, com.openai.models.ChatModel.GPT_5_2)
        );

        notifier.send(result);
    }
}