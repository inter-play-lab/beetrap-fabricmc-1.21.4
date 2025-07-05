package beetrap.btfmc.agent;

import net.minecraft.server.network.ServerPlayerEntity;

public class EmptyAgent extends Agent {
    @Override
    public void onChatMessageReceived(ServerPlayerEntity serverPlayerEntity, String message) {

    }

    @Override
    public void close() throws Exception {

    }
}
