package beetrap.btfmc.networking;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class NetworkingService {
    private final ServerWorld world;

    public NetworkingService(ServerWorld world) {
        this.world = world;
    }

    public void broadcastPacket(Packet<?> pkt) {
        for(ServerPlayerEntity player : world.getPlayers()) {
            player.networkHandler.sendPacket(pkt);
        }
    }

    public void broadcastCustomPayload(CustomPayload cp) {
        for(ServerPlayerEntity player : world.getPlayers()) {
            ServerPlayNetworking.send(player, cp);
        }
    }

    public void beetrapLog(String id, String log) {
        this.broadcastCustomPayload(new BeetrapLogS2CPayload(id, log));
    }
}
