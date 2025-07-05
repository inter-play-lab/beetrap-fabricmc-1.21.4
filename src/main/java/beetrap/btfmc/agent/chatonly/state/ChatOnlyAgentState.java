package beetrap.btfmc.agent.chatonly.state;

import beetrap.btfmc.agent.chatonly.ChatOnlyAgent;
import net.minecraft.server.network.ServerPlayerEntity;

public abstract class ChatOnlyAgentState {
    protected ChatOnlyAgent agent;

    public ChatOnlyAgentState(ChatOnlyAgent agent) {
        this.agent = agent;
        this.onInitialize();
    }

    public void onInitialize() {

    }

    public boolean hasNextState() {
        return false;
    }

    public ChatOnlyAgentState getNextState() {
        return null;
    }

    public void tick() {

    }

    public void onChatMessageReceived(ServerPlayerEntity serverPlayerEntity, String message) {

    }
}
