package beetrap.btfmc.factories;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import net.minecraft.block.BlockState;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.world.World;

public class FallingBlockFactory {

    private static final Constructor<FallingBlockEntity> constructor;

    static {
        try {
            constructor = FallingBlockEntity.class
                    .getDeclaredConstructor(World.class, double.class, double.class, double.class,
                            BlockState.class);
            constructor.setAccessible(true);
        } catch(NoSuchMethodException e) {
            throw new RuntimeException("Could not access FallingBlockEntity constructor", e);
        }
    }

    public static FallingBlockEntity create(World world, double x, double y, double z,
            BlockState state) {
        try {
            return constructor.newInstance(world, x, y, z, state);
        } catch(InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to spawn falling block", e);
        }
    }

    public static FallingBlockEntity createNoGravity(World world, double x, double y, double z,
            BlockState state) {
        FallingBlockEntity entity = create(world, x, y, z, state);
        entity.setNoGravity(true);
        entity.timeFalling = Integer.MAX_VALUE;
        return entity;
    }
}
