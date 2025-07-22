package beetrap.btfmc.handler;

import static beetrap.btfmc.Beetrapfabricmc.MOD_ID;

import beetrap.btfmc.entity.FlowerEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class EntityHandler {

    public static Identifier FLOWER_ID = Identifier.of(MOD_ID, "flower_entity");
    public static EntityType<FlowerEntity> FLOWER = Registry.register(
            Registries.ENTITY_TYPE,
            FLOWER_ID,
            EntityType.Builder.create(FlowerEntity::new, SpawnGroup.MISC).dimensions(0.75f, 0.75f)
                    .build(
                            RegistryKey.of(RegistryKeys.ENTITY_TYPE, FLOWER_ID))
    );

    private EntityHandler() {
        throw new AssertionError();
    }

    public static void registerEntities() {
        FabricDefaultAttributeRegistry.register(FLOWER, FlowerEntity.createMobAttributes());
    }
}
