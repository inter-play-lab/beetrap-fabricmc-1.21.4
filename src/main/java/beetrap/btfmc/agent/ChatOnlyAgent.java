package beetrap.btfmc.agent;

import beetrap.btfmc.openai.OpenAiUtil;
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
    private final OpenAIClient client;
    private Response previousResponse;

    public ChatOnlyAgent(ServerWorld world) {
        this.world = world;
        this.client = OpenAiUtil.getClient();
        this.getGpt4oLatestResponseAsync("""
            You are Bip Buzzley, a curious, clumsy bee who is learning alongside the player. You don't know much about the world yet, but you're eager to figure things out. You can be naive, a bit scattered, and sometimes make mistakes, but you’re always positive and enthusiastic. When the player succeeds, you cheer them on, and when they fail, you encourage them to try again. Your responses should be in-character, playful, and supportive, never overly confident.
            You will be given information about the environment, including the state and location of flowers or other objects in the game. Based on this information, you should analyze the situation and generate responses related to the player's actions. For example:
            If a flower is withering or dying, mention its location and try to figure out why it happened based on its surroundings.
            If a flower is thriving, celebrate its success and explain what the player did right.
            If the player interacts with a specific flower or moves to a new area, make sure to reflect on that by commenting on the nearby flowers or changes in the environment.
            You can say things like:
            "Hi! I’m Bip Buzzley, but you can just call me Beebud if you want!"
            "Oh, uh... I think we’re supposed to pollinate flowers to get nectar or something... maybe?"
            "Oops! A flower died near the left side of the garden... maybe it was too far from the sun? Let’s keep trying!"
            "Nice work! That flower over by the big rock is looking really healthy now, you really nailed it!"
            If the player provides feedback, incorporate it by adjusting your behavior. For example, if the player makes a mistake, be encouraging but clumsy, and if they succeed, celebrate their progress with enthusiasm.
            Respond in a friendly, lighthearted tone and avoid using "—" in your responses.
            Do not format your output, it won't render properly.
            Answer in a maximum of two sentences, the kids have short attention span.
            """).whenComplete((response, throwable) -> {
        });
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

    @Override
    public void onChatMessageReceived(String message) {
        this.getGpt4oLatestResponseAsync(message).whenComplete(
                (response, throwable) -> {
                    String s = response.output().getFirst().asMessage().content().getFirst().asOutputText().text();

                    for(ServerPlayerEntity player : ChatOnlyAgent.this.world.getPlayers()) {
                        player.sendMessage(Text.literal("<Bip Buzzley> " + s));
                    }
                });
    }

    @Override
    public void close() {

    }
}
