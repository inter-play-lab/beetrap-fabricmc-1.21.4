package beetrap.btfmc.state;

import static beetrap.btfmc.networking.BeetrapLogS2CPayload.BEETRAP_LOG_ID_POLLINATION_INITIATED;

import beetrap.btfmc.flower.Flower;
import java.util.List;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Vec3d;

public class MysteriousFifthPollinationReadyState extends PollinationReadyState {

    public MysteriousFifthPollinationReadyState(BeetrapState state, int stage) {
        super(state, stage);
    }


    private void clearItems() {
        List<? extends ItemEntity> entities = this.world.getEntitiesByType(
                TypeFilter.instanceOf(ItemEntity.class),
                itemEntity -> true);

        for(ItemEntity ie : entities) {
            ie.kill(this.world);
        }

        if(!entities.isEmpty()) {
            for(ServerPlayerEntity spe : this.world.getPlayers()) {
                this.interaction.giveTimeTravelItemsToPlayer(spe);
            }
        }
    }

    @Override
    public void tick() {
        this.clearItems();
        ++this.ticks;
        this.beeNestController.tickPollinationLines(this.ticks, this.pastPollinationLocations);
    }

    @Override
    public void onPlayerPollinate(Flower flower, Vec3d flowerMinecraftPosition) {
        this.hasNextState = true;
        this.pastPollinationLocations.add(flowerMinecraftPosition);
        Vec3d pl = this.computeAveragePastPollinationPositions();
        this.nextState = new MysteriousFifthPollinationHappeningState(this, pl, this.stage);
        this.net.beetrapLog(BEETRAP_LOG_ID_POLLINATION_INITIATED, "");
    }

    @Override
    public boolean timeTravelAvailable() {
        return this.stage != 0;
    }
}
