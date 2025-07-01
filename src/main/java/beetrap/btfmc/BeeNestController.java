package beetrap.btfmc;

import beetrap.btfmc.entity.PollenEntity;
import beetrap.btfmc.factories.FallingBlockFactory;
import beetrap.btfmc.flower.Flower;
import beetrap.btfmc.flower.FlowerManager;
import beetrap.btfmc.networking.EntityPositionUpdateS2CPayload;
import beetrap.btfmc.networking.NetworkingService;
import beetrap.btfmc.util.TicksUtil;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

public class BeeNestController {
    private static final double EPSILON = 0.05;
    private static final long MAX_ANIMATION_TICKS = 20;
    private static final long MAX_CIRCLE_TICKS = 20 + 10 * 20;
    private final ServerWorld world;
    private final Vec3d basePos;
    private FallingBlockEntity nest;
    private Vec3d destination;
    private final NetworkingService net;
    private boolean beeNestMoving;

    public BeeNestController(ServerWorld world, NetworkingService net, Vec3d basePosition) {
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

    private void spawnCircleParticles(double r) {
        for(double t = 0; t < Math.TAU; t = t + EPSILON) {
            this.world.spawnParticles(ParticleTypes.FALLING_HONEY, this.nest.getX() + r * Math.cos(t), this.nest.getY(), this.nest.getZ() + r * Math.sin(t), 1, 0, 0, 0, 0);
        }
    }

    public void tickCircle(long ticks, double r) {
        if(!(ticks % 20 == 0 && TicksUtil.inInterval(ticks, MAX_ANIMATION_TICKS, MAX_CIRCLE_TICKS))) {
            return;
        }

        this.spawnCircleParticles(r);
    }

    private void spawnPollen(Vec3d startingPosition, Vec3d destination, double speed) {
        Vec3d nv = destination.subtract(startingPosition);
        ExperienceOrbEntity eoe = new PollenEntity(this.world, startingPosition.x, startingPosition.y, startingPosition.z, 0, nv.multiply(speed));
        eoe.setNoGravity(true);
        this.world.spawnEntity(eoe);
    }

    public void tickSpawnPollensThatFlyTowardsNest(long ticks, FlowerManager flowerManager, Flower[] positions) {
        if(!(ticks % 20 == 0 && TicksUtil.inInterval(ticks, MAX_ANIMATION_TICKS, MAX_CIRCLE_TICKS))) {
            return;
        }

        Flower f = positions[(int)(Math.random() * positions.length)];

        if(f == null) {
            return;
        }

        this.spawnPollen(flowerManager.getFlowerEntity(f).getPos(), this.nest.getPos(), 1 / 20f);
    }

    public void tickCircleBetweenAAndInfinity(long ticks, double r, long a) {
        if(!(ticks % 20 == 0 && ticks >= a)) {
            return;
        }

        this.spawnCircleParticles(r);
    }

    public void dispose() {
        if (nest != null) nest.kill(world);
    }

    public Vec3d getBeeNestPosition() {
        return this.nest.getPos();
    }

    public void setBeeNestPosition(Vec3d position) {
        this.nest.teleportTo(new TeleportTarget(this.world, position, new Vec3d(0, 0, 0), 0, 0, TeleportTarget.NO_OP));
        this.net.broadcastCustomPayload(new EntityPositionUpdateS2CPayload(this.nest.getId(), this.nest.getX(), this.nest.getY(), this.nest.getZ()));
    }

    public FallingBlockEntity getBeeNest() {
        return this.nest;
    }
}
