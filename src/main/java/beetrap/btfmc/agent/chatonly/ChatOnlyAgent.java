package beetrap.btfmc.agent.chatonly;

import beetrap.btfmc.agent.Agent;
import beetrap.btfmc.agent.chatonly.state.COAS0Initial;
import beetrap.btfmc.agent.chatonly.state.ChatOnlyAgentState;
import beetrap.btfmc.openai.OpenAiUtil;
import beetrap.btfmc.state.BeetrapStateManager;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import java.util.concurrent.CompletableFuture;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class ChatOnlyAgent extends Agent implements AutoCloseable {
    private final ServerWorld world;
    private final BeetrapStateManager beetrapStateManager;
    private final String name = "Bip Buzzley";
    private final OpenAIClient client;
    private Response previousResponse;

    private ChatOnlyAgentState currentState;

    public ChatOnlyAgent(ServerWorld world, BeetrapStateManager beetrapStateManager) {
        this.world = world;
        this.beetrapStateManager = beetrapStateManager;
        this.client = OpenAiUtil.getClient();
        this.currentState = new COAS0Initial(this);
    }

    public BeetrapStateManager getBeetrapStateManager() {
        return this.beetrapStateManager;
    }

    public void sendGpt4oLatestResponseToInputToChat(String input) {
        this.getGpt4oLatestResponseAsync(input).whenComplete(this::onGptResponseReceived);
    }

    public void tick() {
        if(this.currentState.hasNextState()) {
            this.currentState = this.currentState.getNextState();
        }

        this.currentState.tick();
    }

    @Override
    public void onChatMessageReceived(ServerPlayerEntity serverPlayerEntity, String message) {
        this.currentState.onChatMessageReceived(serverPlayerEntity, message);
    }

    @Override
    public void close() {

    }

    public void onGptResponseReceived(Response response, Throwable throwable) {
        String s = response.output().getFirst().asMessage().content().getFirst().asOutputText().text();

        for(ServerPlayerEntity player : ChatOnlyAgent.this.world.getPlayers()) {
            player.sendMessage(Text.literal("<" + this.name + "> " + s));
        }
    }

    public CompletableFuture<Response> getGpt4oLatestResponseAsync(String input) {
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
        return response.whenComplete((response1, throwable) -> this.previousResponse = response1);
    }
}
