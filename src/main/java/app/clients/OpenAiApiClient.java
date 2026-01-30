package app.clients;

import java.util.Optional;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.JsonSchemaLocalValidation;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.WebSearchPreviewTool;
import com.openai.models.responses.WebSearchTool;
import com.openai.models.responses.WebSearchTool.Type;


public class OpenAiApiClient {
  private final OpenAIClient client;

  public OpenAiApiClient (String apiKey){
    this.client =  OpenAIOkHttpClient.builder().apiKey(apiKey).build();
  }
  
  /**
  * Takes a full prompt and calls openAi's api to get the gen ai result of a prompt.
  */
  public Response getChatGPTResponse(final String prompt, ChatModel chatModel){

    ResponseCreateParams params =  ResponseCreateParams.builder()
      .input(prompt)
      .model(chatModel)
      .addTool(WebSearchTool.builder()
            .type(Type.WEB_SEARCH)
            .build())
      .build();

    return client.responses().create(params);
  }

  /** 
   * TODO: I'm not sure this needs to live here, but for the mean time im leaving this here 
   */
  public static String fromChatGptResponseToString(Response response) {
   return Optional.ofNullable(response.output())
        .flatMap(output ->  output
          .stream()
          .filter(ResponseOutputItem::isMessage)
          .findFirst()
        )
        .map(ResponseOutputItem::asMessage)
        .map(ResponseOutputMessage::content)
        .flatMap(content -> content
          .stream()
          .filter(ResponseOutputMessage.Content::isOutputText)
          .findFirst()
        )
        .flatMap(firstContent -> firstContent.outputText())
        .map(responseOutputText -> responseOutputText.text())
        .orElse("Error retrieving LLM response for routine");
    }
}