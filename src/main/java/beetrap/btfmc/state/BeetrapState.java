package beetrap.btfmc.state;

import beetrap.btfmc.flower.Flower;
import beetrap.btfmc.flower.FlowerPool;
import beetrap.btfmc.util.AlgorithmOfFloyd;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.math3.linear.RealVector;
import org.jetbrains.annotations.NotNull;

public class BeetrapState implements Iterable<Flower> {
    private FlowerPool flowerPool;
    private BeetrapState parent;
    private int number;
    private BeetrapState child;
    private boolean[] flowers;
    private Vec3d beeNestMinecraftPosition;

    /**
     * Create a new root BeetrapState with a flower pool
     * @param flowerPool the flower pool
     */
    public BeetrapState(FlowerPool flowerPool) {
        this.flowerPool = flowerPool;
        this.parent = null;
        this.number = 0;
        this.flowers = new boolean[this.flowerPool.size()];
    }

    private BeetrapState() {

    }

    public BeetrapState createChild(Vec3d beeNestPosition) {
        this.beeNestMinecraftPosition = beeNestPosition;
        this.child = new BeetrapState();
        this.child.flowerPool = new FlowerPool(this.flowerPool);
        this.child.parent = this;
        this.child.number = this.number + 1;
        this.child.child = null;
        this.child.flowers = new boolean[this.flowers.length];
        System.arraycopy(this.flowers, 0, child.flowers, 0, this.flowers.length);
        this.child.beeNestMinecraftPosition = beeNestPosition;
        return this.child;
    }

    public Vec3d getBeeNestMinecraftPosition() {
        return this.beeNestMinecraftPosition;
    }

    public int getNumber() {
        return this.number;
    }

    public boolean isRoot() {
        return this.parent == null;
    }

    public void populateFlowers(int n) {
        AlgorithmOfFloyd aof = new AlgorithmOfFloyd(this.flowers.length);

        for(int i : aof.sample(n)) {
            this.flowers[i] = true;
        }
    }

    public boolean hasFlower(int flowerNumber) {
        return this.flowers[flowerNumber];
    }

    public void setFlower(int flowerNumber, boolean b) {
        this.flowers[flowerNumber] = b;
    }

    public Flower[] getNFlowersNotInGardenClosestToFSortedByG(Flower f, int n, Comparator<Flower> g) {
        Flower[] r = new Flower[n];
        PriorityQueue<Flower> flowers = new PriorityQueue<>(g);
        for(Flower flower : this.flowerPool) {
            if(flower.equals(f)) {
                continue;
            }

            if(this.flowers[flower.getNumber()]) {
                continue;
            }

            flowers.add(flower);
        }

        for(int i = 0; i < n; ++i) {
            r[i] = flowers.poll();
        }

        return r;
    }

    public Flower[] getNFlowersNotInGardenClosestToF(Flower f, int n) {
        return this.getNFlowersNotInGardenClosestToFSortedByG(f, n, (o1, o2) -> {
            double d1 = f.distanceTo(o1);
            double d2 = f.distanceTo(o2);
            return Double.compare(d1, d2);
        });
    }

    public Flower[] getNFlowersNotInGardenClosestToFByMappedNormalFlowerPosition(Flower f, int n) {
        return this.getNFlowersNotInGardenClosestToFSortedByG(f, n, (o1, o2) -> {
            RealVector fp = this.flowerPool.getMappedNormalFlowerPosition(f.getNumber());
            RealVector o1p = this.flowerPool.getMappedNormalFlowerPosition(o1.getNumber());
            RealVector o2p = this.flowerPool.getMappedNormalFlowerPosition(o2.getNumber());
            double d1 = fp.getDistance(o1p);
            double d2 = fp.getDistance(o2p);
            return Double.compare(d1, d2);
        });
    }

    public double computeDiversityScore() {
        double s = 0;

        for(Flower f : this) {
            if(f.hasWithered()) {
                continue;
            }

            for(Flower g : this) {
                if(g.hasWithered()) {
                    continue;
                }

                s = s + this.flowerPool.getMappedNormalFlowerPosition(f.getNumber()).getDistance(this.flowerPool.getMappedNormalFlowerPosition(g.getNumber())) * 10;
            }
        }

        return s;
    }

    @NotNull
    @Override
    public Iterator<Flower> iterator() {
        return new FlowerIterator();
    }

    public boolean hasChild() {
        return this.child != null;
    }

    public BeetrapState getChild() {
        return this.child;
    }

    public BeetrapState getParent() {
        return this.parent;
    }

    public FlowerPool getFlowerPool() {
        return this.flowerPool;
    }

    public void setBeeNestMinecraftPosition(Vec3d position) {
        this.beeNestMinecraftPosition = position;
    }

    public boolean isLeaf() {
        return this.child == null;
    }

    private class FlowerIterator implements Iterator<Flower> {
        private int i;

        @Override
        public boolean hasNext() {
            while(true) {
                if(this.i >= BeetrapState.this.flowers.length) {
                    return false;
                }

                if(BeetrapState.this.flowers[this.i]) {
                    return true;
                }

                ++this.i;
            }
        }

        @Override
        public Flower next() {
            while(true) {
                if(this.i >= BeetrapState.this.flowers.length) {
                    return null;
                }

                if(BeetrapState.this.flowers[this.i]) {
                    return BeetrapState.this.flowerPool.getFlowerByNumber(this.i++);
                }

                ++this.i;
            }
        }
    }
}
