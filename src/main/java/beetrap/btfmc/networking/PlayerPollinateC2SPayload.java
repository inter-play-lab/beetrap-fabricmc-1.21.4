package beetrap.btfmc.networking;

import beetrap.btfmc.Beetrapfabricmc;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record PlayerPollinateC2SPayload(boolean exists, int entityId) implements CustomPayload {
    public static final Identifier PLAYER_POLLINATE_PAYLOAD_ID = Identifier.of(
            Beetrapfabricmc.MOD_ID, "player_pollinate");
    public static final Id<PlayerPollinateC2SPayload> ID = new Id<>(PLAYER_POLLINATE_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, PlayerPollinateC2SPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOLEAN, PlayerPollinateC2SPayload::exists, PacketCodecs.INTEGER, PlayerPollinateC2SPayload::entityId, PlayerPollinateC2SPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
