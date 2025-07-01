package beetrap.btfmc.state;

import beetrap.btfmc.flower.Flower;
import beetrap.btfmc.networking.MultipleChoiceSelectionResultC2SPayload;
import beetrap.btfmc.networking.ShowMultipleChoiceScreenS2CPayload;
import beetrap.btfmc.networking.ShowTextScreenS2CPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class RecommendationSystemPollinationReadyState extends PollinationReadyState {
    public static final int RECOMMENDATION_SYSTEM_ACTIVITY_STAGE_BEFORE_PLAYER_LOOK_AT_BEE_NEST = 0;
    public static final int RECOMMENDATION_SYSTEM_ACTIVITY_STAGE_AFTER_PLAYER_LOOK_AT_BEE_NEST = 1;
    public static final int RECOMMENDATION_SYSTEM_ACTIVITY_STAGE_POLLINATION_TRULY_READY = 2;
    private int subStage;

    public RecommendationSystemPollinationReadyState(BeetrapState state, int stage, int subStage) {
        super(state, stage);
        this.subStage = subStage;

        if(this.subStage < RECOMMENDATION_SYSTEM_ACTIVITY_STAGE_POLLINATION_TRULY_READY) {
            this.net.broadcastCustomPayload(new ShowTextScreenS2CPayload(
                    ShowTextScreenS2CPayload.lineWrap(
                            "You've learned about the Filter Bubble effect in the previous activity. Now let's learn how it is formed in a recommendation system. To do this, we need to dive into the inner workings of AI recommendation.",
                            50)));
            this.net.broadcastCustomPayload(new ShowTextScreenS2CPayload(
                    ShowTextScreenS2CPayload.lineWrap("Find the Beehive and take a closer look.",
                            50)));
        }
    }

    @Override
    public void onMultipleChoiceSelectionResultReceived(String questionId, int option) {
        switch(questionId) {
            case "user_profile" -> {
                if(option == 0) {
                    this.showTextScreenToAllPlayers("Correct!");
                } else {
                    this.showTextScreenToAllPlayers("That's incorrect, try again next time.");
                }

                this.net.broadcastCustomPayload(
                        new ShowMultipleChoiceScreenS2CPayload("flower_buds_on_ground",
                                "What do the flower buds on the ground represent?",
                                "Available flowers to grow in the garden",
                                "The flowers that I like", "The flowers that I dislike"));
            }
            case "flower_buds_on_ground" -> {
                if(option == 0) {
                    this.showTextScreenToAllPlayers("Correct!");
                } else {
                    this.showTextScreenToAllPlayers("That's incorrect, try again next time.");
                }

                this.net.broadcastCustomPayload(
                        new ShowMultipleChoiceScreenS2CPayload("numbers_above_flower_buds",
                                "What do numbers above flower buds represent?",
                                "The ranking of flowers' height.", "The value of a flower feature",
                                "The ranking of the distance between a flower and the beehive"));
            }
            case "numbers_above_flower_buds" -> {
                if(option == 2) {
                    this.showTextScreenToAllPlayers("Correct!");
                } else {
                    this.showTextScreenToAllPlayers("That's incorrect, try again next time.");
                }

                this.net.broadcastCustomPayload(
                        new ShowMultipleChoiceScreenS2CPayload("pollen_circle",
                                "What does the pollen circle represent?",
                                "The range for me the fly.", "The range for new flowers to grow.",
                                "The range for flowers to die out."));
            }
            case "pollen_circle" -> {
                if(option == 1) {
                    this.showTextScreenToAllPlayers("Correct!");
                } else {
                    this.showTextScreenToAllPlayers("That's incorrect, try again next time.");
                }

                this.net.broadcastCustomPayload(
                        new ShowMultipleChoiceScreenS2CPayload("flowers_located_in_garden",
                                "How are the flowers located in the garden?",
                                "Flowers with the same colors are close together.",
                                "Flowers with multiple similar features are close together.",
                                "Flowers with the same sweetness values are closer together."));
            }
            case "flowers_located_in_garden" -> {
                if(option == 1) {
                    this.showTextScreenToAllPlayers("Correct!");
                } else {
                    this.showTextScreenToAllPlayers("That's incorrect, try again next time.");
                }
            }
        }
    }

    @Override
    public void onPlayerPollinate(Flower flower, Vec3d flowerMinecraftPosition) {
        if(this.subStage < RECOMMENDATION_SYSTEM_ACTIVITY_STAGE_POLLINATION_TRULY_READY) {
            return;
        }

        this.hasNextState = true;
        this.pastPollinationLocations.add(flowerMinecraftPosition);
        Vec3d pl = this.computeAveragePastPollinationPositions();
        this.nextState = new RecommendationSystemPollinationHappeningState(this, pl, this.stage);

        if(this.stage < 2) {
            this.showTextScreenToAllPlayers("What goes into the beehive?");
            this.showTextScreenToAllPlayers("What do flowers on the ground represent?");
            this.showTextScreenToAllPlayers("What do numbers above the flower buds represent?");
            this.showTextScreenToAllPlayers("What does the pollen circle represent?");
            this.showTextScreenToAllPlayers("How are the flowers placed in the garden?");
        } else {
            this.net.broadcastCustomPayload(new ShowMultipleChoiceScreenS2CPayload("user_profile", "What goes into the User Profile(Beehive)?", "The flowers pollinated by me.", "The flowers in the garden.", "The flowers I didn't pollinate."));
        }
    }

    @Override
    public void onPlayerTargetNewEntity(ServerPlayerEntity player, boolean exists, int id) {
        if(this.subStage < RECOMMENDATION_SYSTEM_ACTIVITY_STAGE_AFTER_PLAYER_LOOK_AT_BEE_NEST) {
            if(this.beeNestController.getBeeNest().getId() == id) {
                this.net.broadcastCustomPayload(new ShowTextScreenS2CPayload(ShowTextScreenS2CPayload.lineWrap("Walk in the garden, discuss and make a guess to what do the distances between flowers represent?", 50)));
                this.subStage = RECOMMENDATION_SYSTEM_ACTIVITY_STAGE_POLLINATION_TRULY_READY;
            }
        }

        if(this.subStage < RECOMMENDATION_SYSTEM_ACTIVITY_STAGE_POLLINATION_TRULY_READY) {
            super.onPlayerTargetNewEntity(player, false, id);
        } else {
            super.onPlayerTargetNewEntity(player, exists, id);
        }
    }

    @Override
    public boolean timeTravelAvailable() {
        return this.subStage >= RECOMMENDATION_SYSTEM_ACTIVITY_STAGE_POLLINATION_TRULY_READY;
    }
}
