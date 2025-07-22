package beetrap.btfmc.flower;

import beetrap.btfmc.state.BeetrapState;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardCriterion.RenderType;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

public class FlowerValueScoreboardDisplayerService {

    private final Scoreboard scoreboard;
    private final MinecraftServer server;
    private ScoreboardObjective flowerValues;

    public FlowerValueScoreboardDisplayerService(MinecraftServer server) {
        this.server = server;
        this.scoreboard = this.server.getScoreboard();
        this.flowerValues = this.scoreboard.getNullableObjective("flower_values");

        if(this.flowerValues == null) {
            this.flowerValues = this.scoreboard.addObjective("flower_values",
                    ScoreboardCriterion.DUMMY, Text.of("Flower Values"), RenderType.INTEGER, true,
                    null);
            this.scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, this.flowerValues);
        }
    }

    public void clearScores() {
        for(ScoreHolder sh : this.scoreboard.getKnownScoreHolders()) {
            this.scoreboard.removeScores(sh);
        }
    }

    public void displayFlowerValues(BeetrapState bs, Flower f) {
        if(f == null) {
            return;
        }

        if(f.hasWithered()) {
            return;
        }

        this.scoreboard.getOrCreateScore(
                ScoreHolder.fromName(String.format("Flower number: %d", f.getNumber())),
                this.flowerValues).setScore(6);
        this.scoreboard.getOrCreateScore(ScoreHolder.fromName(String.format("Color: %.2f", f.v)),
                this.flowerValues).setScore(5);
        this.scoreboard.getOrCreateScore(
                        ScoreHolder.fromName(String.format("Smell strength: %.2f", f.w)), this.flowerValues)
                .setScore(4);
        this.scoreboard.getOrCreateScore(
                ScoreHolder.fromName(String.format("Nectar sweetness: %.2f", f.x)),
                this.flowerValues).setScore(3);
        this.scoreboard.getOrCreateScore(
                        ScoreHolder.fromName(String.format("Water needed: %.2f", f.y)), this.flowerValues)
                .setScore(2);
        this.scoreboard.getOrCreateScore(
                ScoreHolder.fromName(String.format("Sunlight needed: %.2f", f.z)),
                this.flowerValues).setScore(1);
    }

    public void dispose() {
        this.scoreboard.removeObjective(this.flowerValues);
    }
}
