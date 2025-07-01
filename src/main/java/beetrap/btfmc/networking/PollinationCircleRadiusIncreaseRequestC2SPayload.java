package beetrap.btfmc.networking;

import beetrap.btfmc.Beetrapfabricmc;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record PollinationCircleRadiusIncreaseRequestC2SPayload(double a)  implements CustomPayload {
    public static final Identifier POLLINATION_RADIUS_CHANGE_REQUEST_PAYLOAD_ID = Identifier.of(
            Beetrapfabricmc.MOD_ID, "pollination_radius_change_request");
    public static final Id<PollinationCircleRadiusIncreaseRequestC2SPayload> ID = new Id<>(POLLINATION_RADIUS_CHANGE_REQUEST_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, PollinationCircleRadiusIncreaseRequestC2SPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.DOUBLE, PollinationCircleRadiusIncreaseRequestC2SPayload::a, PollinationCircleRadiusIncreaseRequestC2SPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
