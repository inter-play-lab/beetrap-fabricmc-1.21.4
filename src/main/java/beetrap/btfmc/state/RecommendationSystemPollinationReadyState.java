package beetrap.btfmc.state;

import static beetrap.btfmc.networking.BeetrapLogS2CPayload.BEETRAP_LOG_ID_POLLINATION_INITIATED;

import beetrap.btfmc.flower.Flower;
import beetrap.btfmc.networking.ShowTextScreenS2CPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class RecommendationSystemPollinationReadyState extends PollinationReadyState {

    public static final int RECOMMENDATION_SYSTEM_ACTIVITY_STAGE_BEFORE_PLAYER_LOOK_AT_BEE_NEST = 0;
    public static final int RECOMMENDATION_SYSTEM_ACTIVITY_STAGE_AFTER_PLAYER_LOOK_AT_BEE_NEST = 1;
    public static final int RECOMMENDATION_SYSTEM_ACTIVITY_STAGE_POLLINATION_TRULY_READY = 2;
    private int subStage;

    public RecommendationSystemPollinationReadyState(BeetrapState state, int stage, int subStage) {
        super(state, stage);
        this.subStage = subStage;

        if(this.subStage < RECOMMENDATION_SYSTEM_ACTIVITY_STAGE_POLLINATION_TRULY_READY) {
            this.net.broadcastCustomPayload(new ShowTextScreenS2CPayload(
                    ShowTextScreenS2CPayload.lineWrap(
                            "You've learned about the Filter Bubble effect in the previous activity. Now let's learn how it is formed in a recommendation system. To do this, we need to dive into the inner workings of AI recommendation.",
                            50)));
            this.net.broadcastCustomPayload(new ShowTextScreenS2CPayload(
                    ShowTextScreenS2CPayload.lineWrap("Find the Beehive and take a closer look.",
                            50)));
        }
    }

    @Override
    public void onPlayerPollinate(Flower flower, Vec3d flowerMinecraftPosition) {
        if(this.subStage < RECOMMENDATION_SYSTEM_ACTIVITY_STAGE_POLLINATION_TRULY_READY) {
            return;
        }

        this.hasNextState = true;
        this.pastPollinationLocations.add(flowerMinecraftPosition);
        Vec3d pl = this.computeAveragePastPollinationPositions();
        this.nextState = new RecommendationSystemPollinationHappeningState(this, pl, this.stage);

        this.net.beetrapLog(BEETRAP_LOG_ID_POLLINATION_INITIATED, "");
    }

    @Override
    public void onPlayerTargetNewEntity(ServerPlayerEntity player, boolean exists, int id) {
        if(this.subStage < RECOMMENDATION_SYSTEM_ACTIVITY_STAGE_AFTER_PLAYER_LOOK_AT_BEE_NEST) {
            if(this.beeNestController.getBeeNest().getId() == id) {
                this.net.broadcastCustomPayload(new ShowTextScreenS2CPayload(
                        ShowTextScreenS2CPayload.lineWrap(
                                "Walk in the garden, look at all the flowers colors and other attributes then make a guess on what do the distances between flowers represent?",
                                50)));
                this.subStage = RECOMMENDATION_SYSTEM_ACTIVITY_STAGE_POLLINATION_TRULY_READY;
            }
        }

        if(this.subStage < RECOMMENDATION_SYSTEM_ACTIVITY_STAGE_POLLINATION_TRULY_READY) {
            super.onPlayerTargetNewEntity(player, false, id);
        } else {
            super.onPlayerTargetNewEntity(player, exists, id);
        }
    }

    @Override
    public boolean timeTravelAvailable() {
        return this.subStage >= RECOMMENDATION_SYSTEM_ACTIVITY_STAGE_POLLINATION_TRULY_READY;
    }
}
