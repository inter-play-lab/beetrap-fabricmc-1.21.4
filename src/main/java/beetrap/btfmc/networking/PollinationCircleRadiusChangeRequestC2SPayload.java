package beetrap.btfmc.networking;

import beetrap.btfmc.Beetrapfabricmc;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record PollinationCircleRadiusChangeRequestC2SPayload()  implements CustomPayload {
    public static final Identifier POLLINATION_RADIUS_CHANGE_REQUEST_PAYLOAD_ID = Identifier.of(
            Beetrapfabricmc.MOD_ID, "pollination_radius_change_request");
    public static final Id<PollinationCircleRadiusChangeRequestC2SPayload> ID = new Id<>(POLLINATION_RADIUS_CHANGE_REQUEST_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, PollinationCircleRadiusChangeRequestC2SPayload> CODEC = PacketCodec.unit(new PollinationCircleRadiusChangeRequestC2SPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
