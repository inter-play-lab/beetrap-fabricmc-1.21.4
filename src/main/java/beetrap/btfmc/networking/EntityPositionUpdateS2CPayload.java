package beetrap.btfmc.networking;

import beetrap.btfmc.Beetrapfabricmc;
import net.minecraft.entity.Entity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record EntityPositionUpdateS2CPayload(int entityId, double posX, double posY, double posZ) implements CustomPayload {
    public static final Identifier ENTITY_POSITION_UPDATE_PAYLOAD_ID = Identifier.of(
            Beetrapfabricmc.MOD_ID, "entity_position_update");
    public static final Id<EntityPositionUpdateS2CPayload> ID = new Id<>(ENTITY_POSITION_UPDATE_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, EntityPositionUpdateS2CPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, EntityPositionUpdateS2CPayload::entityId, PacketCodecs.DOUBLE, EntityPositionUpdateS2CPayload::posX, PacketCodecs.DOUBLE, EntityPositionUpdateS2CPayload::posY, PacketCodecs.DOUBLE, EntityPositionUpdateS2CPayload::posZ, EntityPositionUpdateS2CPayload::new);

    public static EntityPositionUpdateS2CPayload create(Entity entity) {
        return new EntityPositionUpdateS2CPayload(
                entity.getId(),
                entity.getX(),
                entity.getY(),
                entity.getZ()
        );
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
