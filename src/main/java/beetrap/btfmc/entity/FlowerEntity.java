package beetrap.btfmc.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;

public class FlowerEntity extends MobEntity {
    public FlowerEntity(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }
}
