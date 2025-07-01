package beetrap.btfmc;

import beetrap.btfmc.handler.EntityHandler;
import beetrap.btfmc.networking.BeginSubActivityS2CPayload;
import beetrap.btfmc.networking.EntityPositionUpdateS2CPayload;
import beetrap.btfmc.networking.ShowMultipleChoiceScreenS2CPayload;
import beetrap.btfmc.networking.ShowTextScreenS2CPayload;
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

    private void onShowMultipleChoiceScreenReceived(ShowMultipleChoiceScreenS2CPayload showMultipleChoiceScreenS2CPayload, Context context) {
        this.bg.showMultipleChoiceScreen(showMultipleChoiceScreenS2CPayload.questionId(), showMultipleChoiceScreenS2CPayload.question(), showMultipleChoiceScreenS2CPayload.choices());
    }

    @Override
	public void onInitializeClient() {
        this.bg = new BeetrapGameClient();
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
        ClientTickEvents.START_WORLD_TICK.register(bg::onStartWorldTick);
        UseItemCallback.EVENT.register(bg::onPlayerUseItem);
        ClientPlayNetworking.registerGlobalReceiver(EntityPositionUpdateS2CPayload.ID, this::onEntityPositionUpdate);
        ClientPlayNetworking.registerGlobalReceiver(ShowTextScreenS2CPayload.ID, this::onShowTextScreenReceived);
        ClientPlayNetworking.registerGlobalReceiver(ShowMultipleChoiceScreenS2CPayload.ID, this::onShowMultipleChoiceScreenReceived);
        ClientPlayNetworking.registerGlobalReceiver(BeginSubActivityS2CPayload.ID, this::beginSubActivity);

        EntityRendererRegistry.register(EntityHandler.FLOWER, FlowerEntityRenderer::new);
        BeetrapEntityModelLayers.registerModelLayers();
    }

    private void beginSubActivity(BeginSubActivityS2CPayload beginSubActivityS2CPayload,
            Context context) {
        this.bg.beginSubActivity(beginSubActivityS2CPayload.subActivityId());
    }

    private void onShowTextScreenReceived(ShowTextScreenS2CPayload showTextScreenS2CPacket,
            Context context) {
        this.bg.showTextScreen(showTextScreenS2CPacket.text());
    }
}