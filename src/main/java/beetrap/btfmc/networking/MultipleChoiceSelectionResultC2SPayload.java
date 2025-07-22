package beetrap.btfmc.networking;

import beetrap.btfmc.Beetrapfabricmc;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record MultipleChoiceSelectionResultC2SPayload(String questionId, int option) implements
        CustomPayload {

    public static final Identifier MULTIPLE_CHOICE_SELECTION_RESULT_ID = Identifier.of(
            Beetrapfabricmc.MOD_ID, "multiple_choice_selection_result");
    public static final Id<MultipleChoiceSelectionResultC2SPayload> ID = new Id<>(
            MULTIPLE_CHOICE_SELECTION_RESULT_ID);
    public static final PacketCodec<RegistryByteBuf, MultipleChoiceSelectionResultC2SPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, MultipleChoiceSelectionResultC2SPayload::questionId,
            PacketCodecs.INTEGER, MultipleChoiceSelectionResultC2SPayload::option,
            MultipleChoiceSelectionResultC2SPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
