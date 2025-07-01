package beetrap.btfmc.networking;

import beetrap.btfmc.Beetrapfabricmc;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.encoding.StringEncoding;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ShowMultipleChoiceScreenS2CPayload(String questionId, String question, String... choices) implements
        CustomPayload {
    public static final Identifier SHOW_MULTIPLE_CHOICE_SCREEN_ID = Identifier.of(Beetrapfabricmc.MOD_ID, "show_multiple_choice_screen");
    public static final Id<ShowMultipleChoiceScreenS2CPayload> ID = new Id<>(SHOW_MULTIPLE_CHOICE_SCREEN_ID);
    public static final PacketCodec<RegistryByteBuf, ShowMultipleChoiceScreenS2CPayload> CODEC = new PacketCodec<>() {
        @Override
        public ShowMultipleChoiceScreenS2CPayload decode(RegistryByteBuf buf) {
            String questionId = StringEncoding.decode(buf, 32768);
            String question = StringEncoding.decode(buf, 32768);
            int n = buf.readInt();
            String[] choices = new String[n];
            for(int i = 0; i < choices.length; ++i) {
                choices[i] = StringEncoding.decode(buf, 32768);
            }

            return new ShowMultipleChoiceScreenS2CPayload(questionId, question, choices);
        }

        @Override
        public void encode(RegistryByteBuf buf, ShowMultipleChoiceScreenS2CPayload value) {
            StringEncoding.encode(buf, value.questionId, 32768);
            StringEncoding.encode(buf, value.question, 32768);
            buf.writeInt(value.choices.length);

            for(String s : value.choices) {
                StringEncoding.encode(buf, s, 32768);
            }
        }
    };
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
