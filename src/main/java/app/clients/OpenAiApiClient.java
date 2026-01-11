package app.clients;

import java.util.Optional;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

public class OpenAiApiClient {
  private final OpenAIClient client;

  public OpenAiApiClient (){
    this.client =  OpenAIOkHttpClient.fromEnv();
  }
  
  /**
  * Takes a full prompt and calls openAi's api to get the gen ai result of a prompt.
  */
  public Response getChatGPTResponse(final String prompt, ChatModel chatModel){

    ResponseCreateParams params =  ResponseCreateParams.builder()
      .input(prompt)
      .model(chatModel)
      .build();

    return client.responses().create(params);
  }

  /** 
   * TODO: I'm not sure this needs to live here, but for the mean time im leaving this here 
   */
  public static String fromChatGptResponseToString(Response response){
    return  Optional.ofNullable(response.output())
        .map(output -> output.get(0))
        .map(first -> first.asMessage())
        .map(message -> message.content())
        .map(content -> content.get(0))
        .flatMap(firstContent -> firstContent.outputText())
        .map(responseOutputText -> responseOutputText.text())
        .orElse("");
  }

}