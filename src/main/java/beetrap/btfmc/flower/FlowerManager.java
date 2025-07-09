package beetrap.btfmc.flower;

import beetrap.btfmc.state.BeetrapState;
import beetrap.btfmc.factories.FallingBlockFactory;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.function.ToDoubleFunction;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.math3.linear.RealVector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3i;

public class FlowerManager {
    private final Logger LOG = LogManager.getLogger(FlowerManager.class);
    private final ServerWorld world;
    private final Vector3i bottomLeft, topRight;
    private final double width, length, baseY;
    private final FallingBlockEntity[] flowers;

    public FlowerManager(ServerWorld world, int maximumFlowerCount, Vector3i bottomLeft, Vector3i topRight) {
        this.world = world;
        this.bottomLeft = bottomLeft;
        this.topRight = topRight;
        this.width = topRight.x - bottomLeft.x + 1;
        this.length = topRight.z - bottomLeft.z + 1;
        this.baseY = Math.min(bottomLeft.y, topRight.y);
        this.flowers = new FallingBlockEntity[maximumFlowerCount];
    }

    private BlockState getBlockState(BeetrapState beetrapState, Flower f) {
        if(f.hasWithered()) {
            return Blocks.WITHER_ROSE.getDefaultState();
        }

        switch((int)(f.v)) {
            case 0 -> {
                return Blocks.POPPY.getDefaultState();
            }

            case 1 -> {
                return Blocks.ORANGE_TULIP.getDefaultState();
            }

            case 2 -> {
                return Blocks.DANDELION.getDefaultState();
            }

            case 3 -> {
                return Blocks.BLUE_ORCHID.getDefaultState();
            }

            case 4 -> {
                return Blocks.CORNFLOWER.getDefaultState();
            }

            case 5 -> {
                return Blocks.ALLIUM.getDefaultState();
            }
        }

        return Blocks.MANGROVE_PROPAGULE.getDefaultState();
    }

    public void placeFlowerEntity(Flower flower, BlockState blockState, double x, double y, double z) {
        FallingBlockEntity e = FallingBlockFactory.createNoGravity(this.world, x, y, z, blockState);

        int i = flower.getNumber();
        if(this.flowers[i] != null) {
            this.flowers[i].kill(this.world);
        }

        this.flowers[flower.getNumber()] = e;
        this.world.spawnEntity(e);
    }

    public void placeFlowerEntity(BeetrapState bs, Flower f, BlockState blockState) {
        RealVector np = bs.getFlowerPool().getMappedNormalFlowerPosition(f.getNumber());
        this.placeFlowerEntity(f,
                blockState,
                this.bottomLeft.x + np.getEntry(0) * this.width,
                this.baseY,
                this.bottomLeft.z + np.getEntry(1) * this.length
        );
    }

    public void placeFlowerEntities(BeetrapState bs) {
        for(Flower f : bs) {
            if(f == null) {
                continue;
            }

            if(!bs.hasFlower(f.getNumber())) {
                return;
            }

            this.placeFlowerEntity(bs, f, this.getBlockState(bs, f));
        }
    }

    public void placeFlowerEntity(BeetrapState state, Flower f) {
        if(f == null) {
            return;
        }

        this.placeFlowerEntity(state, f, this.getBlockState(null, f));
    }

    public void placeFlowerEntities(BeetrapState state, Flower[] flowers) {
        for(Flower f : flowers) {
            this.placeFlowerEntity(state, f);
        }
    }

    public void placeBuds(BeetrapState state, Flower[] flowers) {
        FlowerPool flowerPool = state.getFlowerPool();
        for(Flower f : flowers) {
            RealVector np = flowerPool.getMappedNormalFlowerPosition(f.getNumber());
            this.placeFlowerEntity(f,
                    Blocks.MANGROVE_PROPAGULE.getDefaultState(),
                    this.bottomLeft.x + np.getEntry(0) * this.width,
                    this.baseY,
                    this.bottomLeft.z + np.getEntry(1) * this.length
            );
        }
    }

    public void destroyAll() {
        for(int i = 0; i < this.flowers.length; ++i) {
            if(this.flowers[i] != null) {
                this.flowers[i].kill(this.world);
                this.flowers[i] = null;
            }
        }
    }

    public Flower getFlowerByEntityId(BeetrapState state, int entityId) {
        FlowerPool flowerPool = state.getFlowerPool();
        for(int i = 0; i < this.flowers.length; ++i) {
            FallingBlockEntity fbe = this.flowers[i];

            if(fbe == null) {
                continue;
            }

            if(fbe.getId() == entityId) {
                return flowerPool.getFlowerByNumber(i);
            }
        }

        return null;
    }

    public FallingBlockEntity[] getFlowerEntities(Flower[] newFlowerCandidates) {
        FallingBlockEntity[] fbe = new FallingBlockEntity[newFlowerCandidates.length];

        int i = 0;
        for(Flower f : newFlowerCandidates) {
            fbe[i++] = this.flowers[f.getNumber()];
        }

        return fbe;
    }

    public FallingBlockEntity[] findAllFlowerEntitiesWithinRSortedByG(Vec3d center, double r, Comparator<FallingBlockEntity> g) {
        PriorityQueue<FallingBlockEntity> resultPq = new PriorityQueue<>(g);

        for(FallingBlockEntity fbe : this.flowers) {
            if(fbe == null) {
                continue;
            }

            if(center.distanceTo(fbe.getPos()) < r) {
                resultPq.add(fbe);
            }
        }

        FallingBlockEntity[] resultArray = new FallingBlockEntity[resultPq.size()];

        for(int i = 0, n = resultPq.size(); i < n; ++i) {
            resultArray[i] = resultPq.poll();
        }

        return resultArray;
    }

    public FallingBlockEntity[] findAllFlowerEntitiesWithinRSortedByLeastDistanceToCenter(Vec3d center, double r) {
        return this.findAllFlowerEntitiesWithinRSortedByG(center, r, Comparator.comparingDouble(o -> center.squaredDistanceTo(o.getPos())));
    }

    public FallingBlockEntity[] findAllFlowerEntitiesWithinRSortedByMostDistanceToCenter(Vec3d center, double r) {
        return this.findAllFlowerEntitiesWithinRSortedByG(center, r, Comparator.comparingDouble(
                (ToDoubleFunction<FallingBlockEntity>)value -> center.squaredDistanceTo(value.getPos())).reversed());
    }

    public FallingBlockEntity getFlowerEntity(Flower f) {
        return this.flowers[f.getNumber()];
    }

    public void removeFlowerEntity(Flower f) {
        FallingBlockEntity fbe = this.flowers[f.getNumber()];

        if(fbe == null) {
            return;
        }

        fbe.kill(this.world);
        this.flowers[f.getNumber()] = null;
    }

    public void removeFlowerEntities(Flower[] flowers) {
        for(Flower f : flowers){
            this.removeFlowerEntity(f);
        }
    }

    public Vec3d getFlowerMinecraftPosition(BeetrapState state, Flower f) {
        FlowerPool flowerPool = state.getFlowerPool();
        RealVector np = flowerPool.getMappedNormalFlowerPosition(f.getNumber());
        return new Vec3d(this.bottomLeft.x + np.getEntry(0) * this.width,
                this.baseY,
                this.bottomLeft.z + np.getEntry(1) * this.length);
    }

    public double getBaseY() {
        return this.baseY;
    }

    public String getFlowerMinecraftColor(Flower f) {
        if(f.hasWithered()) {
            return "Withered";
        }

        switch((int)(f.v)) {
            case 0 -> {
                return "Red";
            }

            case 1 -> {
                return "Orange";
            }

            case 2 -> {
                return "Yellow";
            }

            case 3 -> {
                return "Light blue";
            }

            case 4 -> {
                return "Dark blue";
            }

            case 5 -> {
                return "Purple";
            }
        }

        return "Green";
    }
}
