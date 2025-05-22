package beetrap.btfmc.flower;

import beetrap.btfmc.util.ClassicalMDS;
import java.util.Arrays;
import java.util.Iterator;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.ejml.simple.SimpleMatrix;
import org.jetbrains.annotations.NotNull;

public class FlowerPool implements Iterable<Flower> {
    private Flower[] flowers;
    private RealVector[] mappedNormalizedPositions;
    private int n;

    public FlowerPool(int n) {
        this.flowers = new Flower[n];

        for(int i = 0; i < n; ++i) {
            this.flowers[i] = Flower.createRandomFlower(i);
        }

        this.n = n;

        this.mappedNormalizedPositions = new RealVector[this.n];
        this.computeCMDS();
    }

    public FlowerPool(FlowerPool fp) {
        this.flowers = new Flower[fp.flowers.length];

        int i = 0;
        for(Flower f : fp.flowers) {
            this.flowers[i++] = new Flower(f);
        }

        this.mappedNormalizedPositions = new RealVector[fp.mappedNormalizedPositions.length];
        System.arraycopy(fp.mappedNormalizedPositions, 0, this.mappedNormalizedPositions, 0, fp.mappedNormalizedPositions.length);
        this.n = fp.n;
    }

    private void computeCMDS() {
        // ---- 1. Build distance matrix ----------------------------------------
        SimpleMatrix D = new SimpleMatrix(this.n, this.n);
        for(int i = 0; i < this.n; ++i) {
            for(int j = 0; j < this.n; ++j) {
                D.set(i, j, this.flowers[i].distanceTo(this.flowers[j]));
            }
        }

        // ---- 2. Classical MDS -------------------------------------------------
        ClassicalMDS mds = new ClassicalMDS(D, 2);
        mds.compute();
        SimpleMatrix X = mds.getResult();   // n × 2

        // ---- 3. Find min/max for each coordinate -------------------------------
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        for(int i = 0; i < this.n; ++i) {
            SimpleMatrix xRow = X.getRow(i);
            double[] d = new double[xRow.getNumCols()];

            for(int j = 0; j < d.length; ++j) {
                d[j] = xRow.get(0, j);
            }

            RealVector row = new ArrayRealVector(d);
            min = Math.min(min, row.getMinValue());
            max = Math.max(max, row.getMaxValue());
            this.mappedNormalizedPositions[i] = row;  // keep a copy
        }

        double extent = max - min;
        double scale = 1.0 / extent;
        RealVector bottomLeft = new ArrayRealVector(new double[]{min, min});

        // ---- 4. Translate to (0,0) and apply uniform scale ---------------------
        for(int i = 0; i < this.n; ++i) {
            RealVector v = this.mappedNormalizedPositions[i];
            v.combineToSelf(1.0, -1.0, bottomLeft);      // v ← v − min  (translate)
            v.mapMultiplyToSelf(scale);           // v ← v · scale (uniform)
            this.mappedNormalizedPositions[i] = v;
        }
    }


    public Flower getFlowerByNumber(int i) {
        return this.flowers[i];
    }

    public RealVector getMappedNormalFlowerPosition(int i) {
        return this.mappedNormalizedPositions[i];
    }

    public int size() {
        return this.n;
    }

    @NotNull
    @Override
    public Iterator<Flower> iterator() {
        return Arrays.stream(this.flowers).iterator();
    }
}
