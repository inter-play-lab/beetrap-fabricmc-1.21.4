package beetrap.btfmc.agent;

import net.minecraft.server.network.ServerPlayerEntity;

public abstract class Agent implements AutoCloseable {
    public static final int AGENT_LEVEL_NO_AGENT = 0;
    public static final int AGENT_LEVEL_CHAT_ONLY = 1;
    public static final int AGENT_LEVEL_CHAT_WITH_VOICE_TO_TEXT = 2;
    public static final int AGENT_LEVEL_PHYSICAL = 3;

    public abstract void onChatMessageReceived(ServerPlayerEntity serverPlayerEntity, String message);

    public void tick() {

    }
}
