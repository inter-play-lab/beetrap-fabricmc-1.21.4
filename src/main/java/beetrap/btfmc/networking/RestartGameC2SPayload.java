package beetrap.btfmc.networking;

import static beetrap.btfmc.Beetrapfabricmc.MOD_ID;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RestartGameC2SPayload() implements CustomPayload {
    public static final Identifier RESTART_GAME_PAYLOAD = Identifier.of(MOD_ID, "restart_game_payload");
    public static final Id<RestartGameC2SPayload> ID = new Id<>(RESTART_GAME_PAYLOAD);
    public static final PacketCodec<RegistryByteBuf, RestartGameC2SPayload> CODEC = PacketCodec.unit(new RestartGameC2SPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
