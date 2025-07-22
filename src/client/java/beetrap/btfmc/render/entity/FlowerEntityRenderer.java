package beetrap.btfmc.render.entity;

import static beetrap.btfmc.Beetrapfabricmc.MOD_ID;

import beetrap.btfmc.entity.FlowerEntity;
import beetrap.btfmc.render.entity.model.BeetrapEntityModelLayers;
import beetrap.btfmc.render.entity.model.FlowerEntityModel;
import beetrap.btfmc.render.entity.state.FlowerEntityRenderState;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class FlowerEntityRenderer extends
        MobEntityRenderer<FlowerEntity, FlowerEntityRenderState, FlowerEntityModel> {

    public FlowerEntityRenderer(Context context) {
        super(context, new FlowerEntityModel(context.getPart(BeetrapEntityModelLayers.FLOWER)), 0);
    }

    @Override
    public Identifier getTexture(FlowerEntityRenderState state) {
        return Identifier.of(MOD_ID, "textures/entity/flower_entity/flower_entity.png");
    }

    @Override
    public FlowerEntityRenderState createRenderState() {
        return new FlowerEntityRenderState();
    }
}
