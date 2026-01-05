package app.clients;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

public class OpenAiClient {
  private final OpenAIClient client;

  public OpenAiClient (){
    this.client =  OpenAIOkHttpClient.fromEnv();
  }
  
  /**
  * Takes a full prompt and calls openAi's api to get the gen ai result of a prompt.
  */
  public Response getChatGPTResponse(final String prompt){

    return null;
  }


}