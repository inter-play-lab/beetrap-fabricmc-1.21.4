package beetrap.btfmc.agent.empty;

import beetrap.btfmc.agent.Agent;
import beetrap.btfmc.agent.empty.state.EmptyState;
import beetrap.btfmc.state.BeetrapStateManager;
import net.minecraft.server.world.ServerWorld;

public class EmptyAgent extends Agent {

    public EmptyAgent(ServerWorld world, BeetrapStateManager beetrapStateManager) {
        super(world, beetrapStateManager, new EmptyState());
    }
}
