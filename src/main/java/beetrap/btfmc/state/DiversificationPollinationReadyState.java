package beetrap.btfmc.state;

import static beetrap.btfmc.BeetrapGame.AMOUNT_OF_FLOWERS_TO_WITHER_DEFAULT_MODE;
import static beetrap.btfmc.BeetrapGame.AMOUNT_OF_FLOWERS_TO_WITHER_DIVERSIFYING_MODE;
import static beetrap.btfmc.networking.BeetrapLogS2CPayload.BEETRAP_LOG_ID_POLLINATION_INITIATED;

import beetrap.btfmc.flower.Flower;
import java.util.Arrays;
import java.util.Random;
import net.minecraft.util.math.Vec3d;

public class DiversificationPollinationReadyState extends PollinationReadyState {
    private final double targetDiversityScore;

    DiversificationPollinationReadyState(BeetrapState parent, int stage, double targetDiversityScore) {
        super(parent, stage);
        this.targetDiversityScore = targetDiversityScore;
    }

    public DiversificationPollinationReadyState(BeetrapState parent) {
        super(parent, 0);
        this.flowerManager.destroyAll();
        Arrays.fill(this.flowers, false);

        Random r = new Random();
        Flower f = this.flowerPool.getFlowerByNumber(r.nextInt(this.flowers.length));
        Flower[] g = this.getNFlowersNotInGardenClosestToF(f, 15);

        for(Flower h : g) {
            this.flowers[h.getNumber()] = true;
        }

        this.flowerManager.placeFlowerEntities(this);
        this.targetDiversityScore = (Math.ceil(this.computeDiversityScore() / 100.0) + 7) * 100;

        if(this.stage == 0) {
            this.showTextScreenToAllPlayers("You learned about how the inner workings of AI recommendation form filter bubbles. Now let's learn how to break filter bubbles in the garden. Let's try to make the flower diversity go up. Let's try to make it go above " + (int)this.targetDiversityScore + "! <- this is not a factorial symbol.");
            this.showTextScreenToAllPlayers("For this part, you will switch back and forth from being a bee and being an environmental scientist. The scientist can help the bee increase the flower diversity.");
        }
    }

    @Override
    public void tick() {
        if(this.interaction.rankingMethodLeverChanged()) {
            boolean b = this.interaction.isChangeRankingMethodLeverPowered();

            this.usingDiversifyingRankingMethod = b;

            if(b) {
                this.sendMessageToAllPlayers("Diversifying ranking method enabled!");
                this.amountOfFlowersToWither = AMOUNT_OF_FLOWERS_TO_WITHER_DIVERSIFYING_MODE;
            } else {
                this.sendMessageToAllPlayers("Diversifying ranking method disabled!");
                this.amountOfFlowersToWither = AMOUNT_OF_FLOWERS_TO_WITHER_DEFAULT_MODE;
            }
        }
    }

    @Override
    public boolean timeTravelAvailable() {
        return true;
    }

    @Override
    public void onPlayerPollinate(Flower flower, Vec3d flowerMinecraftPosition) {
        this.hasNextState = true;
        this.pastPollinationLocations.add(flowerMinecraftPosition);

        Vec3d pl = this.computeAveragePastPollinationPositions();

        if(this.stage == 0) {
            this.nextState = new DiversificationPollinationHappeningState(this,
                    pl, this.stage, this.targetDiversityScore,
                    DiversificationPollinationHappeningState.SUB_STAGE_BEFORE_TARGET_CIRCLE_RADIUS_HIT);
        } else {
            this.nextState = new DiversificationPollinationHappeningState(this,
                    pl, this.stage, this.targetDiversityScore,
                    DiversificationPollinationHappeningState.SUB_STAGE_FINISH_CHANGING_EVERYTHING_LETS_GO_FORWARD);
        }

        this.net.beetrapLog(BEETRAP_LOG_ID_POLLINATION_INITIATED, "");
    }
}
