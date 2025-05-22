// beetrap/btfmc/BeeNestController.java
package beetrap.btfmc;

import beetrap.btfmc.factories.FallingBlockFactory;
import beetrap.btfmc.networking.NetworkingService;
import beetrap.btfmc.util.TicksUtil;
import net.minecraft.block.Blocks;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class BeeNestController {
    private static final double EPSILON = 0.05;
    private static final long MAX_ANIMATION_TICKS = 20;
    private static final long MAX_CIRCLE_TICKS = 20 + 10 * 20;
    private final BeetrapGame game;
    private final ServerWorld world;
    private final Vec3d basePos;
    private FallingBlockEntity nest;
    private Vec3d destination;
    private final NetworkingService net;
    private boolean beeNestMoving;

    public BeeNestController(BeetrapGame game, ServerWorld world, NetworkingService net, Vec3d basePosition) {
        this.game = game;
        this.world = world;
        this.net = net;
        this.basePos = basePosition;
    }

    public void spawnNest() {
        nest = FallingBlockFactory.createNoGravity(world,
                basePos.x, basePos.y, basePos.z,
                Blocks.BEE_NEST.getDefaultState());
        world.spawnEntity(nest);
    }

    public void startPollination(Vec3d target) {
        this.destination = new Vec3d(target.x, basePos.y, target.z);
        Vec3d delta = destination.subtract(nest.getPos()).multiply(1/20.0, 0, 1/20.0);
        nest.setVelocity(delta.x, delta.y, delta.z);
        net.broadcastPacket(new EntityVelocityUpdateS2CPacket(nest));
        this.beeNestMoving = true;
    }

    public void tickMovementAnimation(long ticks) {
        if(!this.beeNestMoving) {
            return;
        }

        if(TicksUtil.inInterval(ticks, 0, MAX_ANIMATION_TICKS)) {
            return;
        }

        this.nest.setVelocity(0, 0, 0);
        this.net.broadcastPacket(new EntityVelocityUpdateS2CPacket(nest));
        this.nest.requestTeleport(destination.x, destination.y, destination.z);
        this.net.broadcastCustomPayload(beetrap.btfmc.networking.EntityPositionUpdateS2CPayload.create(nest));
        this.beeNestMoving = false;
    }

    private void spawnCircleParticles() {
        double r = this.game.getState().getPollinationCircleRadius();

        for(double t = 0; t < Math.TAU; t = t + EPSILON) {
            this.world.spawnParticles(ParticleTypes.FALLING_HONEY, this.nest.getX() + r * Math.cos(t), this.nest.getY(), this.nest.getZ() + r * Math.sin(t), 1, 0, 0, 0, 0);
        }
    }

    public void tickCircle(long ticks) {
        if(!(ticks % 20 == 0 && TicksUtil.inInterval(ticks, MAX_ANIMATION_TICKS, MAX_CIRCLE_TICKS))) {
            return;
        }

        this.spawnCircleParticles();
    }

    public void dispose() {
        if (nest != null) nest.kill(world);
    }
}
