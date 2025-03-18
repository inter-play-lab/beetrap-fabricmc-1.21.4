package beetrap.btfmc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BeetrapState {
    private final BeetrapState parent;
    private final UUID uuid;
    private final String name;
    private final List<BeetrapState> children;
    private final Map<UUID, Flower> flowers;

    public BeetrapState(BeetrapState parent, String name) {
        this.parent = parent;
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.children = new ArrayList<>();
        this.flowers = new HashMap<>();
    }

    public BeetrapState(BeetrapState bs) {
        this.parent = bs.parent;
        this.uuid = bs.uuid;
        this.name = bs.name;
        this.children = new ArrayList<>(bs.children);
        this.flowers = new HashMap<>(bs.flowers);
    }

    public void addRandomFlowers(int n) {
        for(int i = 0; i < n; ++i) {
            Flower f = Flower.createRandomFlower();
            this.flowers.put(f.getUuid(), f);
        }
    }

    public Collection<Flower> flowers() {
        return this.flowers.values();
    }

    public Flower getFlower(UUID uuid) {
        return this.flowers.get(uuid);
    }

    public Flower[] getFlowersCloseTo(UUID uuid, Distance<Flower> d, double r) {
        Flower f = this.getFlower(uuid);
        List<Flower> flowers = new ArrayList<>();

        for(Flower g : this.flowers.values()) {
            if(d.distance(f, g) <= r) {
                flowers.add(g);
            }
        }

        return flowers.toArray(Flower[]::new);
    }
}
