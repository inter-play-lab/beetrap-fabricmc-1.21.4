package beetrap.btfmc.state;

import static beetrap.btfmc.BeetrapGame.AMOUNT_OF_FLOWERS_TO_WITHER_DEFAULT_MODE;
import static beetrap.btfmc.BeetrapGame.AMOUNT_OF_FLOWERS_TO_WITHER_DIVERSIFYING_MODE;
import static beetrap.btfmc.networking.BeetrapLogS2CPayload.BEETRAP_LOG_ID_POLLINATION_INITIATED;

import beetrap.btfmc.flower.Flower;
import beetrap.btfmc.networking.ShowTextScreenS2CPayload;
import java.util.List;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Vec3d;

public class ExploreFilterBubbleEffectPollinationReadyState extends PollinationReadyState {
    public ExploreFilterBubbleEffectPollinationReadyState(BeetrapState parent, int stage) {
        super(parent, stage);
    }

    private void clearItems() {
        List<? extends ItemEntity> entities = this.world.getEntitiesByType(TypeFilter.instanceOf(ItemEntity.class),
                itemEntity -> true);

        for(ItemEntity ie : entities) {
            ie.kill(this.world);
        }

        if(!entities.isEmpty()) {
            for(ServerPlayerEntity spe : this.world.getPlayers()) {
                this.interaction.giveInteractablesToPlayer(spe);
            }
        }
    }

    private void onTick20() {
        if(this.ticks != 20) {
            return;
        }

        this.net.broadcastCustomPayload(new ShowTextScreenS2CPayload(ShowTextScreenS2CPayload.lineWrap("Hi Bee! Welcome to the magic garden. Pollinate flowers that you like and pay attention to what happens.", 50)));
    }

    @Override
    public void tick() {
        this.clearItems();
        if(this.stage == 0) {
            this.onTick20();
        }

        ++this.ticks;
    }

    @Override
    public void onPlayerPollinate(Flower flower, Vec3d flowerMinecraftPosition) {
        this.hasNextState = true;
        this.pastPollinationLocations.add(flowerMinecraftPosition);
        Vec3d pl = this.computeAveragePastPollinationPositions();
        this.nextState = new ExploreFilterBubbleEffectPollinationHappeningState(this, pl, this.stage);
        this.net.beetrapLog(BEETRAP_LOG_ID_POLLINATION_INITIATED, "");
    }

    @Override
    public boolean timeTravelAvailable() {
        return this.stage != 0;
    }
}
