package beetrap.btfmc.networking;

import beetrap.btfmc.Beetrapfabricmc;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record PlayerTargetNewEntityC2SPayload(boolean exists, int entityId) implements CustomPayload {
    public static final Identifier PLAYER_TARGET_NEW_ENTITY_PAYLOAD_ID = Identifier.of(Beetrapfabricmc.MOD_ID, "player_target_new_entity");
    public static final Id<PlayerTargetNewEntityC2SPayload> ID = new Id<>(PLAYER_TARGET_NEW_ENTITY_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, PlayerTargetNewEntityC2SPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOLEAN, PlayerTargetNewEntityC2SPayload::exists, PacketCodecs.INTEGER, PlayerTargetNewEntityC2SPayload::entityId, PlayerTargetNewEntityC2SPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
