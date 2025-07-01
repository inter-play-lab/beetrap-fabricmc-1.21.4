package beetrap.btfmc.state;

import static beetrap.btfmc.BeetrapGame.AMOUNT_OF_FLOWERS_TO_WITHER_DEFAULT_MODE;
import static beetrap.btfmc.BeetrapGame.FLOWER_POOL_FLOWER_COUNT;
import static beetrap.btfmc.BeetrapGame.INITIAL_FLOWER_COUNT;
import static beetrap.btfmc.BeetrapGame.INITIAL_POLLINATION_CIRCLE_RADIUS;

import beetrap.btfmc.BeeNestController;
import beetrap.btfmc.GardenInformationBossBar;
import beetrap.btfmc.PlayerInteractionService;
import beetrap.btfmc.flower.Flower;
import beetrap.btfmc.flower.FlowerManager;
import beetrap.btfmc.flower.FlowerPool;
import beetrap.btfmc.flower.FlowerValueScoreboardDisplayerService;
import beetrap.btfmc.networking.PlayerTimeTravelRequestC2SPayload.Operations;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BeetrapStateManager {
    private static final Logger LOG = LogManager.getLogger(BeetrapState.class);
    private final ServerWorld world;
    private final List<BeetrapState> oldBeetrapStates;
    private int pointer;
    private final FlowerManager flowerManager;
    private final GardenInformationBossBar gardenInformationBossBar;
    private BeetrapState state;
    private final double initialDiversityScore;

    private void recordState() {
        this.oldBeetrapStates.add(this.state);
        LOG.info("Recording state {}...", this.pointer + 1);
        LOG.info("State: {}", this.state);
        LOG.info("StateType: {}", this.state.getClass());
        ++this.pointer;
    }

    public BeetrapStateManager(ServerWorld world, FlowerManager flowerManager, PlayerInteractionService interaction, BeeNestController beeNestController, GardenInformationBossBar gardenInformationBossBar, FlowerValueScoreboardDisplayerService flowerValueScoreboardDisplayerService) {
        this.world = world;
        this.flowerManager = flowerManager;
        this.gardenInformationBossBar = gardenInformationBossBar;
        this.pointer = -1;
        this.state = new ActivitySelectionState(world, this, new FlowerPool(FLOWER_POOL_FLOWER_COUNT), flowerManager, interaction,
                beeNestController, gardenInformationBossBar, flowerValueScoreboardDisplayerService, false,
                INITIAL_POLLINATION_CIRCLE_RADIUS, AMOUNT_OF_FLOWERS_TO_WITHER_DEFAULT_MODE);
        this.state.populateFlowers(INITIAL_FLOWER_COUNT);
        this.oldBeetrapStates = new ArrayList<>();
        flowerManager.placeFlowerEntities(this.state);
        this.initialDiversityScore = this.state.computeDiversityScore();
        this.recordState();
    }

    public double getInitialDiversityScore() {
        return this.initialDiversityScore;
    }

    public void tick() {
        if(this.state.hasNextState()) {
            this.state = this.state.getNextState();
            this.recordState();
            this.gardenInformationBossBar.updateBossBar(state, this.pointer);
        }

        this.state.tick();
    }

    public BeetrapState getState() {
        return this.state;
    }

    public ServerWorld getWorld() {
        return this.world;
    }

    public void onPlayerPollinate(FlowerManager flowerManager, boolean exists, int id) {
        if(!exists) {
            return;
        }

        Flower f = flowerManager.getFlowerByEntityId(this.state, id);

        if(f == null) {
            return;
        }

        Entity e = flowerManager.getFlowerEntity(f);

        if(e == null) {
            return;
        }

        this.state.onPlayerPollinate(f, e.getPos());
    }

    public void onPlayerTargetNewEntity(ServerPlayerEntity player, boolean exists, int id) {
        this.state.onPlayerTargetNewEntity(player, exists, id);
    }

    private PollinationReadyState getPreviousPollinationReadyState(int[] index) {
        for(int i = this.pointer - 1; i >= 0; --i) {
            if(this.oldBeetrapStates.get(i) instanceof PollinationReadyState prs) {
                index[0] = i;
                return prs;
            }
        }

        return null;
    }

    private PollinationReadyState getNextPollinationReadyState(int[] index) {
        for(int i = this.pointer + 1; i < this.oldBeetrapStates.size(); ++i) {
            if(this.oldBeetrapStates.get(i) instanceof PollinationReadyState prs) {
                index[0] = i;
                return prs;
            }
        }

        return null;
    }

    private void setState(BeetrapState state) {
        this.state = state;
        this.flowerManager.destroyAll();
        this.flowerManager.placeFlowerEntities(state);
        this.gardenInformationBossBar.updateBossBar(state, this.pointer);
    }

    public void onPlayerRequestTimeTravel(ServerPlayerEntity player, int n, int operation) {
        if(!this.state.timeTravelAvailable()) {
            return;
        }

        if(operation == Operations.ADD) {
            if(n == 1) {
                int[] newPointer = new int[1];
                PollinationReadyState prs = this.getNextPollinationReadyState(newPointer);

                if(prs == null) {
                    this.world.getPlayers().forEach(
                            playerEntity -> playerEntity.sendMessage(Text.of("This is the newest garden!")));
                    return;
                }

                this.pointer = newPointer[0];

                if(this.pointer == this.oldBeetrapStates.size() - 1) {
                    this.setState(this.oldBeetrapStates.get(this.pointer));
                } else {
                    this.setState(new TimeTravelableBeetrapState(prs));
                }
            }

            if(n == -1) {
                int[] newPointer = new int[1];
                PollinationReadyState prs = this.getPreviousPollinationReadyState(newPointer);

                if(prs == null) {
                    this.world.getPlayers().forEach(
                            playerEntity -> playerEntity.sendMessage(Text.of("This is the oldest garden!")));
                    return;
                }

                this.pointer = newPointer[0];
                this.setState(new TimeTravelableBeetrapState(prs));
            }
        }
    }

    public void onMultipleChoiceSelectionResultReceived(String questionId, int option) {
        this.state.onMultipleChoiceSelectionResultReceived(questionId, option);
    }

    public void onPollinationCircleRadiusIncreaseRequested(double a) {
        this.state.onPollinationCircleRadiusIncreaseRequested(a);
    }
}
