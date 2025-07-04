package beetrap.btfmc.state;

import static beetrap.btfmc.networking.BeetrapLogS2CPayload.BEETRAP_LOG_ID_DIVERSITY_SCORE;

import beetrap.btfmc.flower.Flower;
import net.minecraft.util.math.Vec3d;

public abstract class PollinationReadyState extends BeetrapState {
    protected boolean hasNextState;
    protected BeetrapState nextState;
    protected long ticks;
    protected int stage;

    public PollinationReadyState(BeetrapState parent, int stage) {
        super(parent);
        this.stage = stage;
        this.net.beetrapLog(BEETRAP_LOG_ID_DIVERSITY_SCORE,
                String.valueOf(this.computeDiversityScore()));
    }

    @Override
    public void tick() {

    }

    @Override
    public boolean hasNextState() {
        return this.hasNextState;
    }

    @Override
    public BeetrapState getNextState() {
        return this.nextState;
    }

    @Override
    public abstract void onPlayerPollinate(Flower flower, Vec3d flowerMinecraftPosition);
}
