package beetrap.btfmc.state;

import static beetrap.btfmc.BeetrapGame.AMOUNT_OF_BUDS_RANKED;
import static beetrap.btfmc.BeetrapGame.AMOUNT_OF_BUDS_TO_PLACE_DIVERSIFYING_MODE;
import static beetrap.btfmc.BeetrapGame.AMOUNT_OF_FLOWERS_TO_WITHER_DEFAULT_MODE;
import static beetrap.btfmc.BeetrapGame.AMOUNT_OF_FLOWERS_TO_WITHER_DIVERSIFYING_MODE;
import static beetrap.btfmc.BeetrapGame.CHANGE_RANKING_METHOD_LEVER_POSITION;
import static beetrap.btfmc.networking.BeetrapLogS2CPayload.BEETRAP_LOG_ID_POLLINATION_CIRCLE_RADIUS_INCREASED;

import beetrap.btfmc.flower.Flower;
import beetrap.btfmc.networking.BeginSubActivityS2CPayload;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeverBlock;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class DiversificationPollinationHappeningState extends BeetrapState {
    private Flower[] newFlowerCandidates;
    private Flower[] newFlowers;
    private int ticks;
    private boolean active;
    private BeetrapState nextState;
    private final Vec3d pollinationCenter;
    private final int stage;
    private final double targetDiversityScore;
    private int subStage;
    public static final int SUB_STAGE_BEFORE_TARGET_CIRCLE_RADIUS_HIT = 0;
    public static final int SUB_STAGE_BEFORE_RANKING_METHOD_CHANGED = 1;
    public static final int SUB_STAGE_FINISH_CHANGING_EVERYTHING_LETS_GO_FORWARD = 2;
    private long pollinationTrulyReadyTick;

    public DiversificationPollinationHappeningState(BeetrapState state, Vec3d pollinationCenter, int stage, double targetDiversityScore, int subStage) {
        super(state);
        this.pollinationCenter = pollinationCenter;
        this.active = true;
        this.stage = stage;
        this.subStage = subStage;
        this.targetDiversityScore = targetDiversityScore;
    }

    private void onTick0() {
        if(this.ticks != 0) {
            return;
        }

        this.beeNestController.startPollination(this.pollinationCenter);
    }

    private void tickGrowBuds() {
        this.newFlowerCandidates = this.findAtMostNClosestFlowersNotInGardenToCenterByLeastMinecraftDistance(this.pollinationCenter,
                AMOUNT_OF_BUDS_TO_PLACE_DIVERSIFYING_MODE);

        this.flowerManager.placeBuds(this, this.newFlowerCandidates);
    }

    private boolean isNewFlowerCandidate(Flower f) {
        if(f == null) {
            return false;
        }

        if(this.newFlowerCandidates == null) {
            return false;
        }

        for(Flower g : this.newFlowerCandidates) {
            if(g == null) {
                return false;
            }

            if(f.equals(g)) {
                return true;
            }
        }

        return false;
    }

    private void tickRankBuds() {
        FallingBlockEntity[] fbe;

        if(this.usingDiversifyingRankingMethod) {
            fbe = this.flowerManager.findAllFlowerEntitiesWithinRSortedByMostDistanceToCenter(
                    this.pollinationCenter,
                    this.pollinationCircleRadius);
        } else {
            fbe = this.flowerManager.findAllFlowerEntitiesWithinRSortedByLeastDistanceToCenter(
                    this.pollinationCenter,
                    this.pollinationCircleRadius);
        }

        this.newFlowers = new Flower[AMOUNT_OF_BUDS_RANKED];

        int r = 0;
        for(int i = 0; i < fbe.length && r < AMOUNT_OF_BUDS_RANKED; ++i) {
            Flower f = this.flowerManager.getFlowerByEntityId(this, fbe[i].getId());
            if(!this.isNewFlowerCandidate(f)) {
                continue;
            }

            fbe[i].setCustomName(Text.of(String.valueOf(r + 1)));
            fbe[i].setCustomNameVisible(true);
            this.newFlowers[r] = f;
            ++r;
        }
    }

    private void tickPlaceNewFlowers() {
        this.flowerManager.removeFlowerEntities(this.newFlowerCandidates);
        this.flowerManager.placeFlowerEntities(this, this.newFlowers);

        for(Flower f : this.newFlowers) {
            if(f == null) {
                return;
            }

            this.setFlower(f.getNumber(), true);
        }
    }

    private void tickWitherFlowers() {
        FallingBlockEntity[] fbe = this.flowerManager.findAllFlowerEntitiesWithinRSortedByLeastDistanceToCenter(this.pollinationCenter, Double.POSITIVE_INFINITY);

        int r = 0;
        for(int i = fbe.length - 1; i >= 0 && r < this.amountOfFlowersToWither; --i) {
            Flower f = this.flowerManager.getFlowerByEntityId(this, fbe[i].getId());

            if(f.hasWithered()) {
                continue;
            }

            f.setWithered(true);
            this.flowerManager.placeFlowerEntity(this, f);
            ++r;
        }
    }

    private void on200TicksLater(long initialTick) {
        if(this.ticks != initialTick + 200) {
            return;
        }

        this.tickPlaceNewFlowers();
        this.tickWitherFlowers();
        this.onPollinationEnd();
    }

    public void tick() {
        if(!this.active) {
            return;
        }

        this.onTick0();
        this.beeNestController.tickMovementAnimation(this.ticks);

        if(this.ticks == 20) {
            this.tickGrowBuds();
            this.showTextScreenToAllPlayers("Let's first change the radius of the pollination circle by going near it and type B!");
            this.net.broadcastCustomPayload(new BeginSubActivityS2CPayload(BeginSubActivityS2CPayload.SUB_ACTIVITY_PRESS_B_TO_INCREASE_POLLINATION_RADIUS));
        }

        this.beeNestController.tickCircleBetweenAAndInfinity(this.ticks, this.pollinationCircleRadius, 20);

        switch(this.subStage) {
            case SUB_STAGE_BEFORE_TARGET_CIRCLE_RADIUS_HIT -> {
                if(this.pollinationCircleRadius >= 5) {
                    this.subStage = SUB_STAGE_BEFORE_RANKING_METHOD_CHANGED;
                    this.showTextScreenToAllPlayers("Now let's change our method of ranking the buds by flicking the lever in the world!");

                    this.world.setBlockState(new BlockPos(CHANGE_RANKING_METHOD_LEVER_POSITION.getX(), CHANGE_RANKING_METHOD_LEVER_POSITION.getY() - 1, CHANGE_RANKING_METHOD_LEVER_POSITION.getZ() - 1),
                            Blocks.STONE.getDefaultState());
                    this.world.setBlockState(new BlockPos(CHANGE_RANKING_METHOD_LEVER_POSITION.getX(), CHANGE_RANKING_METHOD_LEVER_POSITION.getY(), CHANGE_RANKING_METHOD_LEVER_POSITION.getZ() - 1),
                            Blocks.STONE.getDefaultState());
                    BlockState bs = Blocks.LEVER.getDefaultState().with(LeverBlock.FACING, Direction.SOUTH);
                    this.world.setBlockState(new BlockPos(CHANGE_RANKING_METHOD_LEVER_POSITION.getX(), CHANGE_RANKING_METHOD_LEVER_POSITION.getY(), CHANGE_RANKING_METHOD_LEVER_POSITION.getZ()),
                            bs);
                }
            }

            case SUB_STAGE_BEFORE_RANKING_METHOD_CHANGED -> {
                if(this.interaction.rankingMethodLeverChanged()) {
                    boolean b = this.interaction.isChangeRankingMethodLeverPowered();

                    this.usingDiversifyingRankingMethod = b;

                    if(b) {
                        this.sendMessageToAllPlayers("Diversifying ranking method enabled!");
                        this.amountOfFlowersToWither = AMOUNT_OF_FLOWERS_TO_WITHER_DIVERSIFYING_MODE;
                        this.subStage = SUB_STAGE_FINISH_CHANGING_EVERYTHING_LETS_GO_FORWARD;
                        this.pollinationTrulyReadyTick = this.ticks;
                    } else {
                        this.sendMessageToAllPlayers("Diversifying ranking method disabled!");
                        this.amountOfFlowersToWither = AMOUNT_OF_FLOWERS_TO_WITHER_DEFAULT_MODE;
                    }
                }
            }

            case SUB_STAGE_FINISH_CHANGING_EVERYTHING_LETS_GO_FORWARD -> {
                this.tickRankBuds();
                this.beeNestController.tickSpawnPollensThatFlyTowardsNest(this.ticks, this.flowerManager, this.newFlowerCandidates);
                this.on200TicksLater(this.pollinationTrulyReadyTick);
            }
        }

        ++this.ticks;
    }

    private boolean activityShouldEnd() {
        return this.computeDiversityScore() > this.targetDiversityScore;
    }

    private void onPollinationEnd() {
        this.active = false;

        if(this.activityShouldEnd()) {
            this.nextState = new TimeTravelableBeetrapState(this);
            this.showTextScreenToAllPlayers("Wow!");
        } else {
            this.nextState = new DiversificationPollinationReadyState(this, this.stage + 1, this.targetDiversityScore);
        }

        this.setBeeNestMinecraftPosition(this.beeNestController.getBeeNestPosition());

        for(ServerPlayerEntity player : this.world.getPlayers()) {
            this.interaction.giveTimeTravelItemsToPlayer(player);
        }
    }

    @Override
    public void onPollinationCircleRadiusIncreaseRequested(double a) {
        if(this.subStage == SUB_STAGE_BEFORE_TARGET_CIRCLE_RADIUS_HIT) {
            this.pollinationCircleRadius = this.pollinationCircleRadius + a;
            this.net.beetrapLog(BEETRAP_LOG_ID_POLLINATION_CIRCLE_RADIUS_INCREASED, "Current radius: " + this.pollinationCircleRadius);
        }
    }

    @Override
    public boolean hasNextState() {
        return !this.active;
    }

    @Override
    public BeetrapState getNextState() {
        return this.nextState;
    }

    @Override
    public void onPlayerTargetNewEntity(ServerPlayerEntity player, boolean exists, int id) {
        super.onPlayerTargetNewEntity(player, false, id);
    }

    @Override
    public boolean timeTravelAvailable() {
        return false;
    }
}
