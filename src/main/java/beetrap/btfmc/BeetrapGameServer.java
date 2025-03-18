package beetrap.btfmc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.player.PlayerPosition;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardCriterion.RenderType;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3i;

public class BeetrapGameServer {
    private static final double EPSILON = 0.00001;
    private static final Constructor<FallingBlockEntity> fallingBlockEntityClassPrivateConstructor;
    private final MinecraftServer server;
    private final ServerWorld world;
    private final Vector3i topLeft, bottomRight;
    private final Map<UUID, Entity> managedEntities;
    private final BeetrapState state;
    private int turn;
    private final Scoreboard scoreboard;
    private ScoreboardObjective flowerValues;
    private final FallingBlockEntity beeNest;
    private final double width;
    private final double length;
    private final double baseYLevel;
    private boolean isPollinationInProcess;
    private Vec3d beeNestVelocity;
    private Vec3d beeNestDestination;

    public BeetrapGameServer(MinecraftServer server, Vector3i topLeft, Vector3i bottomRight) {
        this.server = server;
        this.world = this.server.getOverworld();

        this.topLeft = new Vector3i(topLeft);
        this.bottomRight = new Vector3i(bottomRight);

        this.width = this.bottomRight.x - this.topLeft.x + 1;
        this.length = this.bottomRight.z - this.topLeft.z + 1;
        this.baseYLevel = Math.min(this.topLeft.y, this.bottomRight.y);

        this.managedEntities = new HashMap<>();

        this.state = new BeetrapState(null, "Garden " + this.turn);
        this.state.addRandomFlowers(20);
        this.placeFlowers();

        this.scoreboard = this.server.getScoreboard();
        this.flowerValues = this.scoreboard.getNullableObjective("flower_values");

        if(this.flowerValues == null) {
            this.flowerValues = this.scoreboard.addObjective("flower_values", ScoreboardCriterion.DUMMY, Text.of("Flower Values"), RenderType.INTEGER, true, null);
            this.scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, this.flowerValues);
        }

        //noinspection IntegerDivisionInFloatingPointContext
        this.beeNest = spawnBlockAsEntity(this.world, (topLeft.x + bottomRight.x) / 2 + 0.5, this.baseYLevel + 3, (topLeft.z + bottomRight.z) / 2 + 0.5, Blocks.BEE_NEST.getDefaultState());
        this.world.spawnEntity(this.beeNest);
    }

    public void placeFlower(UUID uuid, double x, double y, double z, BlockState blockState) {
        FallingBlockEntity fb = spawnBlockAsEntity(this.world, x, y, z, blockState);
        this.managedEntities.put(uuid, fb);
        this.world.spawnEntity(fb);
    }

    private BlockState getBlockStateByFlower(Flower f) {
        return f.y <= 0 ? Blocks.WITHER_ROSE.getDefaultState() : Blocks.POPPY.getDefaultState();
    }

    private Vec3d getMinecraftCoordinates(Flower flower) {
        double x = topLeft.x + width * flower.x;
        double z = topLeft.z + length * flower.z;
        return new Vec3d(x, this.baseYLevel, z);
    }

    public void placeFlowers() {
        for(Flower f : this.state.flowers()) {
            Vec3d v = this.getMinecraftCoordinates(f);
            this.placeFlower(f.getUuid(), v.x, v.y, v.z, this.getBlockStateByFlower(f));
        }
    }

    public void destroyManagedEntities() {
        for(Entity e : this.managedEntities.values()) {
            e.kill(this.world);
        }
    }

    public void dispose() {
        this.destroyManagedEntities();
        this.server.getScoreboard().removeObjective(this.server.getScoreboard()
                .getNullableObjective("flower_values"));
        this.beeNest.kill(this.world);
    }

    private UUID getFlowerUuid(int entityId) {
        Entity e = this.world.getEntityById(entityId);

        if(e == null) {
            return null;
        }

        UUID beetrapUuid = null;

        for(Entry<UUID, Entity> entry : this.managedEntities.entrySet()) {
            if(entry.getValue().getId() == e.getId()) {
                beetrapUuid = entry.getKey();
            }
        }

        return beetrapUuid;
    }

    private void clearScores() {
        for(ScoreHolder sh : this.scoreboard.getKnownScoreHolders()) {
            this.scoreboard.removeScores(sh);
        }
    }

    private void displayFlowerValues(Flower f) {
        this.scoreboard.getOrCreateScore(ScoreHolder.fromName(String.format("v: %.2f", f.v)), this.flowerValues).setScore(5);
        this.scoreboard.getOrCreateScore(ScoreHolder.fromName(String.format("w: %.2f", f.w)), this.flowerValues).setScore(4);
        this.scoreboard.getOrCreateScore(ScoreHolder.fromName(String.format("x: %.2f", f.x)), this.flowerValues).setScore(3);
        this.scoreboard.getOrCreateScore(ScoreHolder.fromName(String.format("y: %.2f", f.y)), this.flowerValues).setScore(2);
        this.scoreboard.getOrCreateScore(ScoreHolder.fromName(String.format("z: %.2f", f.z)), this.flowerValues).setScore(1);
    }

    private void removeBeeNest(ServerPlayerEntity player) {
        player.getInventory().setStack(4, new ItemStack(Items.AIR, 1));
    }

    private void putBeeNest(ServerPlayerEntity player) {
        ItemStack is = new ItemStack(Items.BEE_NEST, 1);
        is.set(DataComponentTypes.CUSTOM_NAME, Text.of("Pollinate"));
        player.getInventory().setStack(4, is);
    }

    public void onPlayerTargetNewEntity(ServerPlayerEntity player, boolean entityExists, int entityId) {
        this.clearScores();
        this.removeBeeNest(player);

        if(!entityExists) {
            return;
        }

        UUID uuid = this.getFlowerUuid(entityId);

        if(uuid == null) {
            return;
        }

        Flower f = this.state.getFlower(uuid);
        this.displayFlowerValues(f);

        this.putBeeNest(player);
    }

    private void sendPacketToAllPlayers(Packet<?> p) {
        for(ServerPlayerEntity player : this.world.getPlayers()) {
            player.networkHandler.sendPacket(p);
        }
    }

    private void tickPollination() {
        if(this.isPollinationInProcess) {
            if(this.beeNest.getPos().distanceTo(this.beeNestDestination) >= EPSILON) {
                this.beeNest.setVelocity(this.beeNestVelocity.x, this.beeNestVelocity.y, this.beeNestVelocity.z);
                EntityVelocityUpdateS2CPacket evus2cp = new EntityVelocityUpdateS2CPacket(this.beeNest);
                this.sendPacketToAllPlayers(evus2cp);
            } else {
                this.beeNest.setVelocity(0, 0, 0);
                this.beeNest.setPos(this.beeNestDestination.x, this.beeNestDestination.y, this.beeNestDestination.z);
                EntityVelocityUpdateS2CPacket evus2cp = new EntityVelocityUpdateS2CPacket(this.beeNest);
                this.sendPacketToAllPlayers(evus2cp);
                this.isPollinationInProcess = false;
            }
        }
    }

    public void onWorldTick() {
        this.tickPollination();
    }

    public void onPlayerPollinate(ServerPlayerEntity player, boolean entityExists, int entityId) {
        if(!entityExists) {
            return;
        }

        UUID uuid = this.getFlowerUuid(entityId);

        if(uuid == null) {
            return;
        }

        Flower f = this.state.getFlower(uuid);

        Vec3d destination = this.getMinecraftCoordinates(f);
        Vec3d velocity = destination.subtract(this.beeNest.getPos()).multiply(1 / 20., 0, 1 / 20.);

        this.beeNestVelocity = new Vec3d(velocity.x, velocity.y, velocity.z);
        this.beeNestDestination = new Vec3d(destination.x, this.baseYLevel + 3, destination.z);
        this.beeNest.setVelocity(velocity);
        EntityVelocityUpdateS2CPacket evus2cp = new EntityVelocityUpdateS2CPacket(this.beeNest);
        this.sendPacketToAllPlayers(evus2cp);
        this.isPollinationInProcess = true;

        Flower[] g = this.state.getFlowersCloseTo(uuid, (t1, t2) -> {
            double t1x = topLeft.x + width * t1.x;
            double t1z = topLeft.z + length * t1.z;
            double t2x = topLeft.x + width * t2.x;
            double t2z = topLeft.z + length * t2.z;

            double dx = t1x - t2x;
            double dz = t1z - t2z;

            return Math.sqrt(dx * dx + dz + dz);
        }, 3);
    }

    public static FallingBlockEntity spawnFallingBlock(World world, double x, double y, double z, BlockState blockState) {
        try {
            return fallingBlockEntityClassPrivateConstructor.newInstance(world, x, y, z, blockState);
        } catch(InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static FallingBlockEntity spawnBlockAsEntity(World world, double x, double y, double z, BlockState blockState) {
        FallingBlockEntity fbe = spawnFallingBlock(world, x, y, z, blockState);
        fbe.setNoGravity(true);
        fbe.timeFalling = Integer.MAX_VALUE;
        return fbe;
    }

    static {
        try {
            fallingBlockEntityClassPrivateConstructor = FallingBlockEntity.class.getDeclaredConstructor(World.class, double.class, double.class, double.class, BlockState.class);
            fallingBlockEntityClassPrivateConstructor.setAccessible(true);
        } catch(NoSuchMethodException e) {
            // this should exist
            throw new RuntimeException(e);
        }
    }
}
