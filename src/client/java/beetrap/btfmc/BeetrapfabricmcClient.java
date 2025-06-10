package beetrap.btfmc;

import beetrap.btfmc.handler.EntityHandler;
import beetrap.btfmc.networking.EntityPositionUpdateS2CPayload;
import beetrap.btfmc.render.entity.FlowerEntityRenderer;
import beetrap.btfmc.render.entity.model.BeetrapEntityModelLayers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.player.UseItemCallback;

public class BeetrapfabricmcClient implements ClientModInitializer {
    private BeetrapGameClient bg;

    private void onEntityPositionUpdate(EntityPositionUpdateS2CPayload payload, Context context) {
        this.bg.onEntityPositionUpdate(payload.entityId(), payload.posX(), payload.posY(),
                payload.posZ());
    }

    @Override
	public void onInitializeClient() {
        this.bg = new BeetrapGameClient();
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
        ClientTickEvents.START_WORLD_TICK.register(bg::onStartWorldTick);
        UseItemCallback.EVENT.register(bg::onPlayerUseItem);
        ClientPlayNetworking.registerGlobalReceiver(EntityPositionUpdateS2CPayload.ID, this::onEntityPositionUpdate);

        EntityRendererRegistry.register(EntityHandler.FLOWER, FlowerEntityRenderer::new);
        BeetrapEntityModelLayers.registerModelLayers();
    }
}