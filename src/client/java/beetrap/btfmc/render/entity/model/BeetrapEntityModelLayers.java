package beetrap.btfmc.render.entity.model;

import beetrap.btfmc.Beetrapfabricmc;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class BeetrapEntityModelLayers {

    public static final EntityModelLayer FLOWER = createModelLayer("flower_entity");

    private static EntityModelLayer createModelLayer(String name) {
        return new EntityModelLayer(Identifier.of(Beetrapfabricmc.id(name)), "main");
    }

    public static void registerModelLayers() {
        EntityModelLayerRegistry.registerModelLayer(FLOWER,
                FlowerEntityModel::getTexturedModelData);
    }
}
