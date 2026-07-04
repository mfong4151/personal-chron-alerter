package app.routines;

import java.time.LocalDate;

import com.openai.models.ChatModel;

import app.DiscordNotifier;
import app.clients.OpenAiApiClient;
import app.market.PremarketAssessment;
import app.market.PremarketGapService;
import app.utils.PromptUtils;


/**
* Executes a daily chron routine, contains the conditions for:
* Computing the premarket QQQ/SPY gap from live market data
* Calling chatgpt to research causes for the (pre-measured) move
* Parsing the llm content into an intelligble discord message
* Packaging the message into something intelligble for discord to send to my private server
*/
public final class PremarketStockRoutine implements ChronRoutine{
    private static final String NO_GAP_MESSAGE =
        "Pre market price was not above your threshold.";

    private final DiscordNotifier notifier;
    private final OpenAiApiClient openAiApiClient;
    private final PremarketGapService premarketGapService;

    public PremarketStockRoutine(DiscordNotifier notifier, OpenAiApiClient openAiApiClient,
        PremarketGapService premarketGapService) {
        this.notifier = notifier;
        this.openAiApiClient = openAiApiClient;
        this.premarketGapService = premarketGapService;
    }

    @Override
    public void run(LocalDate day) {
        final PremarketAssessment assessment = premarketGapService.assess();

        if (!assessment.breached()) {
            notifier.send(NO_GAP_MESSAGE);
            return;
        }

        final String prompt = PromptUtils.getStockGapPrompt(assessment.description());
        final String result = OpenAiApiClient.fromChatGptResponseToString( 
          openAiApiClient.getChatGPTResponse(prompt, ChatModel.GPT_5_2)
        );

        notifier.send(result);
    }
}
