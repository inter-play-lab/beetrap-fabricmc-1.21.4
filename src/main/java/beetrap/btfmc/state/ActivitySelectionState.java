package beetrap.btfmc.state;

import static beetrap.btfmc.state.RecommendationSystemPollinationReadyState.RECOMMENDATION_SYSTEM_ACTIVITY_STAGE_BEFORE_PLAYER_LOOK_AT_BEE_NEST;

import beetrap.btfmc.BeeNestController;
import beetrap.btfmc.GardenInformationBossBar;
import beetrap.btfmc.PlayerInteractionService;
import beetrap.btfmc.flower.FlowerManager;
import beetrap.btfmc.flower.FlowerPool;
import beetrap.btfmc.flower.FlowerValueScoreboardDisplayerService;
import beetrap.btfmc.networking.ShowMultipleChoiceScreenS2CPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class ActivitySelectionState extends BeetrapState {
    private static final String ACTIVITY_SELECTION_SCREEN_ID = "activity_selection";
    private static final int NO_ACTIVITY = -1;
    private static final int OBSERVE_FLOWERS_ONLY = 0;
    private static final int FILTER_BUBBLE = 1;
    private static final int RECOMMENDATION_SYSTEM = 2;
    private static final int DIVERSIFICATION = 3;
    private int activityNumber;

    public ActivitySelectionState(ServerWorld world, BeetrapStateManager manager,
            FlowerPool flowerPool,
            FlowerManager flowerManager, PlayerInteractionService interaction,
            BeeNestController beeNestController,
            GardenInformationBossBar gardenInformationBossBar,
            FlowerValueScoreboardDisplayerService flowerValueScoreboardDisplayerService,
            boolean usingDiversifyingRankingMethod, double pollinationCircleRadius,
            int amountOfFlowersToWither) {
        super(world, manager, flowerPool, flowerManager, interaction, beeNestController,
                gardenInformationBossBar, flowerValueScoreboardDisplayerService, usingDiversifyingRankingMethod, pollinationCircleRadius,
                amountOfFlowersToWither);
        this.activityNumber = NO_ACTIVITY;
        this.net.broadcastCustomPayload(new ShowMultipleChoiceScreenS2CPayload(ACTIVITY_SELECTION_SCREEN_ID, "Select an activity!", "Observe flowers only", "What is the filter bubble effect?", "How does the garden recommend flowers?", "How do we break the filter bubble?"));
    }

    @Override
    public void onMultipleChoiceSelectionResultReceived(String questionId, int option) {
        if(questionId.equals(ACTIVITY_SELECTION_SCREEN_ID)) {
            this.activityNumber = option;
        }
    }

    @Override
    public void tick() {

    }

    @Override
    public boolean hasNextState() {
        return this.activityNumber != NO_ACTIVITY;
    }

    @Override
    public BeetrapState getNextState() {
        return switch(this.activityNumber) {
            case OBSERVE_FLOWERS_ONLY -> new ObserveFlowersOnlyState(this);
            case FILTER_BUBBLE -> new ExploreFilterBubbleEffectPollinationReadyState(this, 0);
            case RECOMMENDATION_SYSTEM -> new RecommendationSystemPollinationReadyState(this, 0, RECOMMENDATION_SYSTEM_ACTIVITY_STAGE_BEFORE_PLAYER_LOOK_AT_BEE_NEST);
            case DIVERSIFICATION -> new DiversificationPollinationReadyState(this);
            default -> null;
        };
    }

    @Override
    public boolean timeTravelAvailable() {
        return false;
    }

    @Override
    public void onPlayerTargetNewEntity(ServerPlayerEntity player, boolean exists, int id) {
        super.onPlayerTargetNewEntity(player, false, id);
    }
}
