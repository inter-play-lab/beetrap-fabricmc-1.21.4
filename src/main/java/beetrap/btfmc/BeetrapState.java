package beetrap.btfmc;

import beetrap.btfmc.flower.Flower;
import beetrap.btfmc.flower.FlowerPool;
import beetrap.btfmc.util.AlgorithmOfFloyd;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public class BeetrapState implements Iterable<Flower> {
    private FlowerPool flowerPool;
    private BeetrapState parent;
    private int number;
    private BeetrapState child;
    private boolean[] flowers;
    private double pollinationCircleRadius;
    private int amountOfFlowersToWither;

    /**
     * Create a new root BeetrapState with a flower pool
     * @param flowerPool the flower pool
     */
    public BeetrapState(FlowerPool flowerPool) {
        this.flowerPool = flowerPool;
        this.parent = null;
        this.number = 0;
        this.flowers = new boolean[this.flowerPool.size()];
        this.pollinationCircleRadius = 3;
        this.amountOfFlowersToWither = 3;
    }

    private BeetrapState() {

    }

    public BeetrapState createChild() {
        this.child = new BeetrapState();
        this.child.flowerPool = new FlowerPool(this.flowerPool);
        this.child.parent = this;
        this.child.number = this.number + 1;
        this.child.child = null;
        this.child.flowers = new boolean[this.flowers.length];
        System.arraycopy(this.flowers, 0, child.flowers, 0, this.flowers.length);
        this.child.pollinationCircleRadius = this.pollinationCircleRadius;
        this.child.amountOfFlowersToWither = this.amountOfFlowersToWither;
        return this.child;
    }

    public int getNumber() {
        return this.number;
    }

    public double getPollinationCircleRadius() {
        return this.pollinationCircleRadius;
    }

    public int getAmountOfFlowersToWither() {
        return this.amountOfFlowersToWither;
    }

    public void setPollinationCircleRadius(double pcr) {
        this.pollinationCircleRadius = pcr;
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

    public Flower[] getNFlowersClosestTo(Flower f, int n) {
        Flower[] r = new Flower[n];

        PriorityQueue<Flower> flowers = new PriorityQueue<>((o1, o2) -> {
            double d1 = f.distanceTo(o1);
            double d2 = f.distanceTo(o2);
            return Double.compare(d1, d2);
        });

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
