package beetrap.btfmc.entity;

import java.lang.reflect.Field;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PollenEntity extends ExperienceOrbEntity {
    private static final Field targetField;
    private int orbAge;
    private final Vec3d velocity;

    public PollenEntity(World world, double x, double y, double z, int amount, Vec3d velocity) {
        super(world, x, y, z, amount);
        this.velocity = velocity;
    }

    public Vec3d getVelocity() {
        return this.velocity;
    }

    public void tick() {
        super.baseTick();
        this.prevX = this.getX();
        this.prevY = this.getY();
        this.prevZ = this.getZ();

        if (!this.getWorld().isSpaceEmpty(this.getBoundingBox())) {
            this.pushOutOfBlocks(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
        }

        try {
            if(targetField.get(this) != null) {
                targetField.set(this, null);
            }
        } catch(IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        this.move(MovementType.SELF, this.getVelocity());
        this.tickBlockCollision();

        ++this.orbAge;
        if (this.orbAge >= 20) {
            this.discard();
        }
    }

    static {
        try {
            targetField = ExperienceOrbEntity.class.getDeclaredField("target");
            targetField.setAccessible(true);
        } catch(NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
