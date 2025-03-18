package beetrap.btfmc;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;

public class BeetrapfabricmcClient implements ClientModInitializer {
    @Override
	public void onInitializeClient() {
        BeetrapGameClient bg = new BeetrapGameClient();
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
        ClientTickEvents.START_WORLD_TICK.register(bg::onStartWorldTick);
        UseItemCallback.EVENT.register(bg::onPlayerUseItem);
    }
}