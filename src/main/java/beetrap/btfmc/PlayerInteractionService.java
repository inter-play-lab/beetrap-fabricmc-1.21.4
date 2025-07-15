package beetrap.btfmc;

import static beetrap.btfmc.BeetrapGame.CHANGE_RANKING_METHOD_LEVER_POSITION;
import static beetrap.btfmc.networking.BeetrapLogS2CPayload.BEETRAP_LOG_ID_RANKING_METHOD_LEVER_FLICKED;

import beetrap.btfmc.flower.Flower;
import beetrap.btfmc.flower.FlowerManager;
import beetrap.btfmc.flower.FlowerValueScoreboardDisplayerService;
import beetrap.btfmc.networking.NetworkingService;
import beetrap.btfmc.state.BeetrapState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeverBlock;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class PlayerInteractionService {
    private final ServerWorld world;
    private boolean changeRankingMethodLeverPowered;
    private NetworkingService net;

    public PlayerInteractionService(ServerWorld world) {
        this.world = world;
        this.net = new NetworkingService(this.world);
    }

    public boolean rankingMethodLeverChanged() {
        BlockState blockState = this.world.getBlockState(CHANGE_RANKING_METHOD_LEVER_POSITION);
        Block b = blockState.getBlock();

        if(!(b instanceof LeverBlock)) {
            return false;
        }

        boolean f = blockState.get(LeverBlock.POWERED);
        boolean r = f != this.changeRankingMethodLeverPowered;

        if(r) {
            this.changeRankingMethodLeverPowered = f;
            this.net.beetrapLog(BEETRAP_LOG_ID_RANKING_METHOD_LEVER_FLICKED, "Current lever state: " + (f ? "POWERED" : "UNPOWERED"));
        }

        return r;
    }

    public boolean isChangeRankingMethodLeverPowered() {
        return this.changeRankingMethodLeverPowered;
    }

    public void giveNestToPlayer(ServerPlayerEntity player) {
        ItemStack nest = new ItemStack(Items.BEE_NEST);
        nest.set(DataComponentTypes.CUSTOM_NAME, Text.of("Pollinate"));
        player.getInventory().setStack(4, nest);
    }

    public void removeNestFromPlayer(ServerPlayerEntity player) {
        player.getInventory().setStack(4, new ItemStack(Items.AIR));
    }

    public void giveTimeTravelItemsToPlayer(ServerPlayerEntity player) {
        ItemStack backward = new ItemStack(Items.CLOCK);
        backward.set(DataComponentTypes.CUSTOM_NAME, Text.of("Back"));
        player.getInventory().setStack(0, backward);

        ItemStack forward = new ItemStack(Items.CLOCK);
        forward.set(DataComponentTypes.CUSTOM_NAME, Text.of("Forward"));
        player.getInventory().setStack(8, forward);
    }

    public void giveRestartGameItemToPlayer(ServerPlayerEntity player) {
        ItemStack restart = new ItemStack(Items.BARRIER);
        restart.set(DataComponentTypes.CUSTOM_NAME, Text.of("restart"));
        player.getInventory().setStack(6, restart);
    }

    public void dispose() {
        for(ServerPlayerEntity player : this.world.getPlayers()) {
            player.getInventory().clear();
        }
    }
}
