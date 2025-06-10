package beetrap.btfmc;

import static beetrap.btfmc.BeetrapGame.AMOUNT_OF_BUDS_RANKED;
import static beetrap.btfmc.BeetrapGame.AMOUNT_OF_BUDS_TO_PLACE;

import beetrap.btfmc.flower.Flower;
import beetrap.btfmc.flower.FlowerManager;
import beetrap.btfmc.state.BeetrapState;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class PollinationController {
    private final BeetrapGame game;
    private final ServerWorld serverWorld;
    private final FlowerManager flowerManager;
    private final BeeNestController beeNestController;
    private final PlayerInteractionService playerInteractionService;
    private GardenInformationBossBar gardenInformationBossBar;
    private long ticks;
    private boolean active;
    private Flower targetFlower;
    private Flower[] newFlowerCandidates;
    private Flower[] newFlowers;

    public PollinationController(BeetrapGame game, ServerWorld world, FlowerManager manager, BeeNestController controller, PlayerInteractionService interaction, GardenInformationBossBar gardenInformationBossBar) {
        this.game = game;
        this.serverWorld = world;
        this.flowerManager = manager;
        this.beeNestController = controller;
        this.playerInteractionService = interaction;
        this.gardenInformationBossBar = gardenInformationBossBar;
    }

    public void onPollinationStart(Flower flower, Vec3d target) {
        if(this.active) {
            return;
        }

        this.targetFlower = flower;
        this.beeNestController.startPollination(target);
        this.active = true;
        this.ticks = 0;

        this.serverWorld.getPlayers().forEach(
                PollinationController.this.playerInteractionService::giveInteractablesToPlayer);
        this.game.regenerateState();
    }

    private void tickGrowBuds() {
        BeetrapState state = this.game.getState();
        this.newFlowerCandidates = state.getNFlowersNotInGardenClosestToFByMappedNormalFlowerPosition(this.targetFlower, AMOUNT_OF_BUDS_TO_PLACE);
        this.flowerManager.placeBuds(this.newFlowerCandidates);
    }

    private boolean isNewFlowerCandidate(Flower f) {
        for(Flower g : this.newFlowerCandidates) {
            if(f.equals(g)) {
                return true;
            }
        }

        return false;
    }

    private void tickRankBuds() {
        FallingBlockEntity[] fbe;

        if(this.game.isUsingDiversifyingRankingMethod()) {
            fbe = this.flowerManager.findAllFlowerEntitiesWithinRSortedByMostDistanceToCenter(
                    this.flowerManager.getFlowerEntity(this.targetFlower).getPos(),
                    this.game.getPollinationCircleRadius());
        } else {
            fbe = this.flowerManager.findAllFlowerEntitiesWithinRSortedByLeastDistanceToCenter(
                    this.flowerManager.getFlowerEntity(this.targetFlower).getPos(),
                    this.game.getPollinationCircleRadius());
        }

        this.newFlowers = new Flower[AMOUNT_OF_BUDS_RANKED];

        int r = 0;
        for(int i = 0; i < fbe.length && r < AMOUNT_OF_BUDS_RANKED; ++i) {
            Flower f = this.flowerManager.getFlowerByEntityId(fbe[i].getId());

            if(f.equals(this.targetFlower)) {
                continue;
            }

            if(!this.isNewFlowerCandidate(f)) {
                continue;
            }

            fbe[i].setCustomName(Text.of(String.valueOf(r + 1)));
            fbe[i].setCustomNameVisible(true);
            this.newFlowers[r] = f;
            ++r;
        }
    }

    private void onTick20() {
        if(this.ticks != 20) {
            return;
        }

        this.tickGrowBuds();
        this.tickRankBuds();
    }

    private void tickPlaceNewFlowers() {
        this.flowerManager.removeFlowerEntities(this.newFlowerCandidates);
        this.flowerManager.placeFlowerEntities(this.newFlowers);

        for(Flower f : this.newFlowers) {
            if(f == null) {
                return;
            }

            this.game.getState().setFlower(f.getNumber(), true);
        }
    }

    private void tickWitherFlowers() {
        FallingBlockEntity[] fbe = this.flowerManager.findAllFlowerEntitiesWithinRSortedByLeastDistanceToCenter(this.flowerManager.getFlowerEntity(this.targetFlower).getPos(), Double.POSITIVE_INFINITY);

        int r = 0;
        for(int i = fbe.length - 1; i >= 0 && r < this.game.getAmountOfFlowersToWither(); --i) {
            Flower f = this.flowerManager.getFlowerByEntityId(fbe[i].getId());

            if(f.hasWithered()) {
                continue;
            }

            f.setWithered(true);
            this.flowerManager.placeFlowerEntity(f);
            ++r;
        }
    }

    private void onTick220() {
        if(this.ticks != 220) {
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

        // this.ticks is in 0..19
        this.beeNestController.tickMovementAnimation(this.ticks);
        // this.ticks == 20
        this.onTick20();
        // this.ticks is in 20..219
        this.beeNestController.tickCircle(this.ticks);
        // this.ticks == 220
        this.onTick220();
        ++this.ticks;
    }

    private void onPollinationEnd() {
        this.active = false;
        this.game.getState().setBeeNestMinecraftPosition(this.beeNestController.getBeeNestPosition());
        this.gardenInformationBossBar.updateBossBar();
    }

    public boolean isActive() {
        return this.active;
    }
}
