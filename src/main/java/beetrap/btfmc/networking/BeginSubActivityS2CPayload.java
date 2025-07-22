package beetrap.btfmc.networking;

import beetrap.btfmc.Beetrapfabricmc;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record BeginSubActivityS2CPayload(int subActivityId) implements CustomPayload {

    public static final Identifier BEGIN_SUB_ACTIVITY_ID = Identifier.of(Beetrapfabricmc.MOD_ID,
            "begin_sub_activity");
    public static final Id<BeginSubActivityS2CPayload> ID = new Id<>(BEGIN_SUB_ACTIVITY_ID);
    public static final PacketCodec<RegistryByteBuf, BeginSubActivityS2CPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, BeginSubActivityS2CPayload::subActivityId,
            BeginSubActivityS2CPayload::new);

    public static final int SUB_ACTIVITY_NULL = 0;
    public static final int SUB_ACTIVITY_PRESS_B_TO_INCREASE_POLLINATION_RADIUS = 1;

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
