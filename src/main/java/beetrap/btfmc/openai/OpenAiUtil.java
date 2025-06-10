package beetrap.btfmc.openai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import java.util.concurrent.CompletableFuture;

public final class OpenAiUtil {
    private static final OpenAIClient client;
    private static Response previousResponse;

    private OpenAiUtil() {
        throw new AssertionError();
    }

    public static void load() {

    }

    public static void clearHistory() {
        previousResponse = null;
    }

    public static CompletableFuture<Response> getGpt4oLatestResponseAsync(String input) {
        ResponseCreateParams params;

        if(previousResponse != null) {
            params = ResponseCreateParams.builder()
                    .input(input)
                    .model(ChatModel.CHATGPT_4O_LATEST)
                    .previousResponseId(previousResponse.id())
                    .build();
        } else {
            params = ResponseCreateParams.builder()
                    .input(input)
                    .model(ChatModel.CHATGPT_4O_LATEST)
                    .build();
        }

        CompletableFuture<Response> response = client.async().responses().create(params);
        return response.whenComplete((response1, throwable) -> OpenAiUtil.previousResponse = response1);
    }

    public static OpenAIClient getClient() {
        return client;
    }

    static {
        client = OpenAIOkHttpClient.builder().fromEnv().build();
    }
}
