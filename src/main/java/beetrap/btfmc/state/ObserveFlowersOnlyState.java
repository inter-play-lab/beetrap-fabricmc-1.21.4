package beetrap.btfmc.state;

import beetrap.btfmc.flower.Flower;
import net.minecraft.server.network.ServerPlayerEntity;

public class ObserveFlowersOnlyState extends BeetrapState {

    public ObserveFlowersOnlyState(BeetrapState state) {
        super(state);
    }

    @Override
    public void tick() {

    }

    @Override
    public boolean hasNextState() {
        return false;
    }

    @Override
    public BeetrapState getNextState() {
        return null;
    }

    @Override
    public void onPlayerTargetNewEntity(ServerPlayerEntity player, boolean exists, int id) {
        this.flowerValueScoreboardDisplayerService.clearScores();
        Flower flower = this.flowerManager.getFlowerByEntityId(this, id);

        if(flower == null) {
            this.interaction.removeNestFromPlayer(player);
            return;
        }

        if(!this.hasFlower(flower.getNumber()) || flower.hasWithered()) {
            return;
        }

        this.flowerValueScoreboardDisplayerService.displayFlowerValues(this, flower);

        if(!exists) {
            this.interaction.removeNestFromPlayer(player);
        }
    }

    @Override
    public boolean timeTravelAvailable() {
        return false;
    }
}
