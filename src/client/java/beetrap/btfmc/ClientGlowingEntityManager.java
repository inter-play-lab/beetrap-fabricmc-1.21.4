package beetrap.btfmc;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.entity.Entity;

public class ClientGlowingEntityManager {
    private static final Set<Integer> glowingEntityIds;

    public static boolean shouldGlow(Entity entity) {
        return glowingEntityIds.contains(entity.getId());
    }

    public static void addGlowingEntity(Entity entity) {
        glowingEntityIds.add(entity.getId());
    }

    public static void removeGlowingEntity(Entity entity) {
        glowingEntityIds.remove(entity.getId());
    }

    static {
        glowingEntityIds = new HashSet<>();
    }
}
