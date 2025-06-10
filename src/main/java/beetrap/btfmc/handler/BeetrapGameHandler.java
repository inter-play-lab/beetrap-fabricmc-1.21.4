package beetrap.btfmc.handler;

import beetrap.btfmc.BeetrapGame;
import beetrap.btfmc.networking.PlayerPollinateC2SPayload;
import beetrap.btfmc.networking.PlayerTargetNewEntityC2SPayload;
import beetrap.btfmc.networking.PlayerTimeTravelRequestC2SPayload;
import beetrap.btfmc.networking.PollinationCircleRadiusChangeRequestC2SPayload;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.joml.Vector3i;

public final class BeetrapGameHandler {
    private static BeetrapGame game;

    private BeetrapGameHandler() {
        throw new AssertionError();
    }

    public static boolean hasGame() {
        return game != null;
    }

    public static void createGame(MinecraftServer server) {
        if(hasGame()) {
            return;
        }

        game = new BeetrapGame(server, new Vector3i(-10, 0, -10), new Vector3i(10, 0, 10));
    }

    public static void destroyGame() {
        if(!hasGame()) {
            return;
        }

        game.dispose();
        game = null;
    }

    public static void onPlayerTargetNewEntity(
            PlayerTargetNewEntityC2SPayload payload, Context context) {
        if(!hasGame()) {
            return;
        }

        game.onPlayerTargetNewEntity(context.player(), payload.exists(), payload.entityId());
    }

    public static void onPlayerPollinate(PlayerPollinateC2SPayload payload,
            Context context) {
        if(!hasGame()) {
            return;
        }

        game.onPlayerPollinate(context.player(), payload.exists(), payload.entityId());
    }

    public static void onPlayerRequestTimeTravel(PlayerTimeTravelRequestC2SPayload payload, Context context) {
        if(!hasGame()) {
            return;
        }

        game.onPlayerRequestTimeTravel(context.player(), payload.n(), payload.operation());
    }

    public static void onPollinationCircleRadiusChangeRequested(
            PollinationCircleRadiusChangeRequestC2SPayload payload, Context context) {
        if(!hasGame()) {
            return;
        }

        game.onPollinationCircleRadiusChangeRequested();
    }

    public static void onWorldTick(ServerWorld world) {
        if(!hasGame()) {
            return;
        }

        game.onWorldTick();
    }

    public static void registerEvents() {
        ServerTickEvents.START_WORLD_TICK.register(BeetrapGameHandler::onWorldTick);
    }
}
