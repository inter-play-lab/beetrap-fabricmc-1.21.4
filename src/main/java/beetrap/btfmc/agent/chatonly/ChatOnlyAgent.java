package beetrap.btfmc.agent.chatonly;

import beetrap.btfmc.agent.Agent;
import beetrap.btfmc.agent.chatonly.state.COAS0Initial;
import beetrap.btfmc.agent.AgentState;
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
    public ChatOnlyAgent(ServerWorld world, BeetrapStateManager beetrapStateManager) {
        super(world, beetrapStateManager, AGENT_NAME, new COAS0Initial());
    }
}
