package beetrap.btfmc.handler;

import beetrap.btfmc.networking.EntityPositionUpdateS2CPayload;
import beetrap.btfmc.networking.PlayerPollinateC2SPayload;
import beetrap.btfmc.networking.PlayerTargetNewEntityC2SPayload;
import beetrap.btfmc.networking.PlayerTimeTravelRequestC2SPayload;
import beetrap.btfmc.networking.PollinationCircleRadiusChangeRequestC2SPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class NetworkHandler {
    private NetworkHandler() {
        throw new AssertionError();
    }
    
    public static void registerCustomPayloads() {
        PayloadTypeRegistry.playC2S().register(PlayerTargetNewEntityC2SPayload.ID, PlayerTargetNewEntityC2SPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PlayerTargetNewEntityC2SPayload.ID, BeetrapGameHandler::onPlayerTargetNewEntity);

        PayloadTypeRegistry.playC2S().register(PlayerPollinateC2SPayload.ID, PlayerPollinateC2SPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PlayerPollinateC2SPayload.ID, BeetrapGameHandler::onPlayerPollinate);

        PayloadTypeRegistry.playS2C().register(EntityPositionUpdateS2CPayload.ID, EntityPositionUpdateS2CPayload.CODEC);

        PayloadTypeRegistry.playC2S().register(PlayerTimeTravelRequestC2SPayload.ID, PlayerTimeTravelRequestC2SPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PlayerTimeTravelRequestC2SPayload.ID, BeetrapGameHandler::onPlayerRequestTimeTravel);

        PayloadTypeRegistry.playC2S().register(PollinationCircleRadiusChangeRequestC2SPayload.ID, PollinationCircleRadiusChangeRequestC2SPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PollinationCircleRadiusChangeRequestC2SPayload.ID, BeetrapGameHandler::onPollinationCircleRadiusChangeRequested);
    }
}
