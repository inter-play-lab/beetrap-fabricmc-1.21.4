package beetrap.btfmc.networking;

import static beetrap.btfmc.Beetrapfabricmc.MOD_ID;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record EndSubActivityC2SPayload(int subActivityId) implements CustomPayload {
    public static Identifier END_SUB_ACTIVITY = Identifier.of(MOD_ID, "end_sub_activity");
    public static Id<EndSubActivityC2SPayload> ID = new Id<>(END_SUB_ACTIVITY);
    public static PacketCodec<RegistryByteBuf, EndSubActivityC2SPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, EndSubActivityC2SPayload::subActivityId, EndSubActivityC2SPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
