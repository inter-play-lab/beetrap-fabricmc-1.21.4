package beetrap.btfmc;

import java.util.Random;
import java.util.UUID;

public class Flower {
    private static final Random random = new Random();
    private final UUID uuid;
    public double v, w, x, y, z;

    public Flower(double v, double w, double x, double y, double z) {
        this.uuid = UUID.randomUUID();
        this.v = v;
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Flower createRandomFlower() {
        return new Flower(random.nextDouble(), random.nextDouble(), random.nextDouble(), random.nextDouble(), random.nextDouble());
    }

    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }

        if(!(obj instanceof Flower f)) {
            return false;
        }

        return this.uuid == f.uuid;
    }
}
