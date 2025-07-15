package beetrap.btfmc;

import beetrap.btfmc.state.BeetrapState;
import beetrap.btfmc.state.BeetrapStateManager;
import net.minecraft.entity.boss.BossBar.Color;
import net.minecraft.entity.boss.BossBarManager;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GardenInformationBossBar {
    private final BossBarManager manager;
    private final ServerBossBar bossBar;

    public GardenInformationBossBar(MinecraftServer server) {
        this.manager = server.getBossBarManager();
        this.bossBar = this.manager.add(Identifier.of("garden"), Text.of(""));
        this.bossBar.setPercent(1);
        this.bossBar.setColor(Color.GREEN);

        for(ServerPlayerEntity player : server.getOverworld().getPlayers()) {
            this.bossBar.addPlayer(player);
        }

        this.bossBar.setVisible(true);
    }

    private Text getTitle(BeetrapState state, int stage) {
        int correctStage = stage % 2 == 0 ? stage / 2 : (stage / 2) + 1;
        int correctDiversityScore = (int)state.computeDiversityScore();
        return Text.of("Garden " + correctStage + " - Diversity score: " + correctDiversityScore);
    }

    public void updateBossBar(BeetrapState bs, int stage) {
        this.bossBar.setName(this.getTitle(bs, stage));
    }

    public void dispose() {
        this.bossBar.setVisible(false);
        this.manager.remove((CommandBossBar)this.bossBar);
    }
}
