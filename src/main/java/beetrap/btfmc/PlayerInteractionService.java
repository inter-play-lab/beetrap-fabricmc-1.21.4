package beetrap.btfmc;

import beetrap.btfmc.flower.Flower;
import beetrap.btfmc.flower.FlowerManager;
import beetrap.btfmc.flower.FlowerValueScoreboardDisplayerService;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.text.Text;

public class PlayerInteractionService {
    private final FlowerManager flowers;
    private final FlowerValueScoreboardDisplayerService scoreboard;

    public PlayerInteractionService(FlowerManager flowers,
            FlowerValueScoreboardDisplayerService scoreboard) {
        this.flowers = flowers;
        this.scoreboard = scoreboard;
    }

    public void handleTarget(BeetrapState bs, ServerPlayerEntity player, boolean exists, int entityId) {
        this.scoreboard.clearScores();
        this.removeNestFromPlayer(player);
        if(!exists) {
            return;
        }

        Flower flower = this.flowers.getFlowerByEntityId(entityId);
        if(flower == null) {
            return;
        }

        this.scoreboard.displayFlowerValues(bs, flower);
        this.giveNestToPlayer(player);
    }

    private void giveNestToPlayer(ServerPlayerEntity player) {
        ItemStack nest = new ItemStack(Items.BEE_NEST);
        nest.set(DataComponentTypes.CUSTOM_NAME, Text.of("Pollinate"));
        player.getInventory().setStack(4, nest);
    }

    private void removeNestFromPlayer(ServerPlayerEntity player) {
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
}
