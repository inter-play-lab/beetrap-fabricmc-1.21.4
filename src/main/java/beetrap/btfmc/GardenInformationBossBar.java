package beetrap.btfmc;

import beetrap.btfmc.state.BeetrapState;
import net.minecraft.entity.boss.BossBar.Color;
import net.minecraft.entity.boss.BossBarManager;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GardenInformationBossBar {
    private final BeetrapGame game;
    private final BossBarManager manager;
    private final ServerBossBar bossBar;

    public GardenInformationBossBar(BeetrapGame game, MinecraftServer server) {
        this.game = game;
        this.manager = server.getBossBarManager();
        this.bossBar = this.manager.add(Identifier.of("garden"), this.getTitle(this.game.getState()));
        this.bossBar.setPercent(1);
        this.bossBar.setColor(Color.GREEN);

        for(ServerPlayerEntity player : server.getOverworld().getPlayers()) {
            this.bossBar.addPlayer(player);
        }

        this.bossBar.setVisible(true);
    }

    private Text getTitle(BeetrapState state) {
        return Text.of("Garden " + state.getNumber() + " - Diversity score: " + state.computeDiversityScore());
    }

    public void updateBossBar() {
        this.bossBar.setName(this.getTitle(this.game.getState()));
    }

    public void dispose() {
        this.bossBar.setVisible(false);
        this.manager.remove((CommandBossBar)this.bossBar);
    }
}
