package beetrap.btfmc.networking;

import beetrap.btfmc.Beetrapfabricmc;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ShowTextScreenS2CPayload(String text) implements CustomPayload {
    public static final Identifier SHOW_TEXT_SCREEN_ID = Identifier.of(Beetrapfabricmc.MOD_ID, "show_text_screen");
    public static final Id<ShowTextScreenS2CPayload> ID = new Id<>(SHOW_TEXT_SCREEN_ID);
    public static final PacketCodec<RegistryByteBuf, ShowTextScreenS2CPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, ShowTextScreenS2CPayload::text, ShowTextScreenS2CPayload::new);

    public static String lineWrap(String s, int n) {
        StringBuilder sb = new StringBuilder();
        String[] t = s.split("\\s");
        int l = 0;

        for(int i = 0; i < t.length; ++i) {
            String u = t[i];

            if(i != 0) {
                if(l + u.length() + 1 > n) {
                    sb.append("\n");
                    l = 0;
                }

                sb.append(" ");
            }

            sb.append(u);
            l = l + u.length();
        }

        return sb.toString();
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
