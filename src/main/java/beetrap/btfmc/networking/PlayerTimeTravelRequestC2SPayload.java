package beetrap.btfmc.networking;

import beetrap.btfmc.Beetrapfabricmc;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record PlayerTimeTravelRequestC2SPayload(int n, int operation) implements CustomPayload {

    public static final Identifier PLAYER_TIME_TRAVEL_REQUEST_PAYLOAD_ID = Identifier.of(
            Beetrapfabricmc.MOD_ID, "player_time_travel_request");
    public static final Id<PlayerTimeTravelRequestC2SPayload> ID = new Id<>(
            PLAYER_TIME_TRAVEL_REQUEST_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, PlayerTimeTravelRequestC2SPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, PlayerTimeTravelRequestC2SPayload::n, PacketCodecs.INTEGER,
            PlayerTimeTravelRequestC2SPayload::operation, PlayerTimeTravelRequestC2SPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static final class Operations {

        public static final int SET = 0;
        public static final int ADD = 1;

        private Operations() {
            throw new AssertionError();
        }
    }
}
