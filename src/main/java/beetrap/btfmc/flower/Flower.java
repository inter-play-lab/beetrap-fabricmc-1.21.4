package beetrap.btfmc.flower;

import java.util.Random;

public class Flower {

    private static final Random r = new Random();
    public final double v, w, x, y, z;
    private final int number;
    private boolean withered;

    public Flower(int number, double v, double w, double x, double y, double z) {
        this.number = number;
        this.v = v;
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Flower(Flower f) {
        this.number = f.number;
        this.v = f.v;
        this.w = f.w;
        this.x = f.x;
        this.y = f.y;
        this.z = f.z;
        this.withered = f.withered;
    }

    public static Flower createRandomFlower(int number) {
        return new Flower(
                number,
                r.nextInt(0, 6), // color
                r.nextDouble(2, 6 + Math.ulp(1)), // petal_size
                r.nextDouble(2, 6 + Math.ulp(1)), // height
                r.nextDouble(2, 6 + Math.ulp(1)), //leaf_size
                r.nextInt(1, 7) / 3.0 / 10 // petal_shape
        );
    }

    public double squaredDistanceTo(Flower f) {
        double vv = (this.v - f.v);
        double ww = (this.w - f.w);
        double xx = (this.x - f.x);
        double yy = (this.y - f.y);
        double zz = (this.z - f.z);

        vv = vv * vv;
        ww = ww * ww;
        xx = xx * xx;
        yy = yy * yy;
        zz = zz * zz;

        return vv + ww + xx + yy + zz;
    }

    public double distanceTo(Flower f) {
        return Math.sqrt(this.squaredDistanceTo(f));
    }

    public int getNumber() {
        return this.number;
    }

    public boolean hasWithered() {
        return this.withered;
    }

    public void setWithered(boolean withered) {
        this.withered = withered;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }

        if(!(obj instanceof Flower f)) {
            return false;
        }

        return this.number == f.number;
    }

    @Override
    public String toString() {
        return "Flower_" + this.number + "=(" + this.v + ", " + this.w + ", " + this.x + ", "
                + this.y + ", " + this.z + ")";
    }
}
