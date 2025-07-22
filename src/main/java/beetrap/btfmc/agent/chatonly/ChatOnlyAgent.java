package beetrap.btfmc.agent.chatonly;

import beetrap.btfmc.agent.Agent;
import beetrap.btfmc.agent.chatonly.state.COAS0Initial;
import beetrap.btfmc.state.BeetrapStateManager;
import net.minecraft.server.world.ServerWorld;

public class ChatOnlyAgent extends Agent implements AutoCloseable {

    public ChatOnlyAgent(ServerWorld world, BeetrapStateManager beetrapStateManager) {
        super(world, beetrapStateManager, AGENT_NAME, new COAS0Initial());
    }
}
